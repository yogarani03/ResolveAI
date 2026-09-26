package com.resolveai.service;

import com.resolveai.ai.AIAnalysisResult;
import com.resolveai.ai.AIService;
import com.resolveai.dto.*;
import com.resolveai.entity.*;
import com.resolveai.exception.BadRequestException;
import com.resolveai.exception.ForbiddenActionException;
import com.resolveai.exception.InvalidStatusTransitionException;
import com.resolveai.exception.ResourceNotFoundException;
import com.resolveai.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintHistoryRepository historyRepository;
    private final ComplaintCategoryRepository categoryRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final AIAnalysisRepository aiAnalysisRepository;
    private final FeedbackRepository feedbackRepository;
    private final AIService aiService;
    private final NotificationService notificationService;

    @Value("${app.sla.default-hours}")
    private long slaDefaultHours;

    /** Valid forward transitions. Anything not listed here is rejected. REOPENED
     *  is only reachable from CLOSED/RESOLVED via the explicit reopen path. */
    private static final Map<ComplaintStatus, Set<ComplaintStatus>> ALLOWED_TRANSITIONS = Map.of(
            ComplaintStatus.OPEN, Set.of(ComplaintStatus.ASSIGNED, ComplaintStatus.IN_PROGRESS, ComplaintStatus.ESCALATED),
            ComplaintStatus.ASSIGNED, Set.of(ComplaintStatus.IN_PROGRESS, ComplaintStatus.ESCALATED),
            ComplaintStatus.IN_PROGRESS, Set.of(ComplaintStatus.RESOLVED, ComplaintStatus.ESCALATED),
            ComplaintStatus.ESCALATED, Set.of(ComplaintStatus.IN_PROGRESS, ComplaintStatus.RESOLVED),
            ComplaintStatus.RESOLVED, Set.of(ComplaintStatus.CLOSED, ComplaintStatus.REOPENED),
            ComplaintStatus.CLOSED, Set.of(ComplaintStatus.REOPENED),
            ComplaintStatus.REOPENED, Set.of(ComplaintStatus.ASSIGNED, ComplaintStatus.IN_PROGRESS)
    );

    // ==================== CREATE ====================

    @Transactional
    public ComplaintResponse createComplaint(ComplaintRequest request, User currentUser) {
        ComplaintCategory category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        }

        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        }

        Complaint complaint = Complaint.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .referenceNumber(request.getReferenceNumber())
                .status(ComplaintStatus.OPEN)
                .priority(Priority.MEDIUM) // default; may be refined by AI/staff below
                .user(currentUser)
                .category(category)
                .department(department)
                .dueAt(LocalDateTime.now().plusHours(slaDefaultHours))
                .build();

        // IMPORTANT: the complaint is saved FIRST and unconditionally. Everything
        // after this point (AI analysis) is best-effort and must never roll this back.
        complaint = complaintRepository.save(complaint);

        recordHistory(complaint, null, ComplaintStatus.OPEN, currentUser, "Complaint created");

        runAiAnalysis(complaint);

        return getById(complaint.getId(), currentUser);
    }

    /** Runs AI analysis defensively: any failure results in a stored FAILED/UNAVAILABLE
     *  AIAnalysis row, never an exception that could affect the complaint itself. */
    private void runAiAnalysis(Complaint complaint) {
        AIAnalysisResult result;
        try {
            result = aiService.analyzeComplaint(complaint.getTitle(), complaint.getDescription());
        } catch (Exception e) {
            log.warn("AI analysis threw an unexpected exception: {}", e.getMessage());
            result = AIAnalysisResult.unavailable("AI service threw an unexpected error");
        }

        AIAnalysis analysis = AIAnalysis.builder()
                .complaint(complaint)
                .status(result.getStatus())
                .suggestedCategory(result.getCategory())
                .suggestedSubCategory(result.getSubCategory())
                .suggestedPriority(result.getPriority())
                .summary(result.getSummary())
                .errorMessage(result.getErrorMessage())
                .build();
        aiAnalysisRepository.save(analysis);

        // AI's priority is only a SUGGESTION. If the complaint doesn't already have a
        // category assigned by the user, and AI succeeded, apply the suggested priority
        // as the complaint's working priority - staff/admin can still change it freely.
        if (result.getStatus() == AIAnalysisStatus.SUCCESS && result.getPriority() != null) {
            complaint.setPriority(result.getPriority());
            complaintRepository.save(complaint);
        }
    }

    // ==================== READ ====================

    public ComplaintResponse getById(Long id, User currentUser) {
        Complaint complaint = findComplaintOrThrow(id);
        authorizeView(complaint, currentUser);

        ComplaintResponse response = ComplaintResponse.from(complaint);
        aiAnalysisRepository.findByComplaint(complaint)
                .ifPresent(a -> response.setAiAnalysis(AIAnalysisResponse.from(a)));
        return response;
    }

    public List<ComplaintResponse> listForCurrentUser(User currentUser) {
        List<Complaint> complaints = switch (currentUser.getRole()) {
            case ADMIN -> complaintRepository.findAll();
            case STAFF -> complaintRepository.findByAssignedStaff(currentUser);
            case USER -> complaintRepository.findByUser(currentUser);
        };
        return complaints.stream().map(this::toResponseWithAi).collect(Collectors.toList());
    }

    private ComplaintResponse toResponseWithAi(Complaint c) {
        ComplaintResponse r = ComplaintResponse.from(c);
        aiAnalysisRepository.findByComplaint(c).ifPresent(a -> r.setAiAnalysis(AIAnalysisResponse.from(a)));
        return r;
    }

    private void authorizeView(Complaint complaint, User currentUser) {
        if (currentUser.getRole() == Role.ADMIN) return;
        if (currentUser.getRole() == Role.STAFF
                && complaint.getAssignedStaff() != null
                && complaint.getAssignedStaff().getId().equals(currentUser.getId())) return;
        if (currentUser.getRole() == Role.USER && complaint.getUser().getId().equals(currentUser.getId())) return;

        throw new ForbiddenActionException("You do not have access to this complaint");
    }

    // ==================== STATUS WORKFLOW ====================

    @Transactional
    public ComplaintResponse updateStatus(Long id, StatusUpdateRequest request, User currentUser) {
        Complaint complaint = findComplaintOrThrow(id);

        if (currentUser.getRole() == Role.STAFF
                && (complaint.getAssignedStaff() == null || !complaint.getAssignedStaff().getId().equals(currentUser.getId()))) {
            throw new ForbiddenActionException("You are not assigned to this complaint");
        }

        ComplaintStatus current = complaint.getStatus();
        ComplaintStatus target = request.getStatus();

        validateTransition(current, target);

        complaint.setStatus(target);
        if (target == ComplaintStatus.ESCALATED) {
            complaint.setEscalatedAt(LocalDateTime.now());
        }
        if (target == ComplaintStatus.RESOLVED) {
            complaint.setResolvedAt(LocalDateTime.now());
        }
        complaintRepository.save(complaint);

        recordHistory(complaint, current, target, currentUser, request.getNote());
        notificationService.notify(complaint.getUser(), "Your complaint #" + complaint.getId()
                + " status changed to " + target);

        return getById(id, currentUser);
    }

    private void validateTransition(ComplaintStatus from, ComplaintStatus to) {
        if (from == to) {
            throw new InvalidStatusTransitionException("Complaint is already in status " + to);
        }
        Set<ComplaintStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(from, Set.of());
        if (!allowed.contains(to)) {
            throw new InvalidStatusTransitionException(
                    "Cannot move complaint from " + from + " to " + to + ". Allowed next states: " + allowed);
        }
    }

    private void recordHistory(Complaint complaint, ComplaintStatus from, ComplaintStatus to, User changedBy, String note) {
        ComplaintHistory history = ComplaintHistory.builder()
                .complaint(complaint)
                .fromStatus(from)
                .toStatus(to)
                .changedBy(changedBy)
                .note(note)
                .build();
        historyRepository.save(history);
    }

    // ==================== ASSIGNMENT ====================

    @Transactional
    public ComplaintResponse assignStaff(Long id, AssignRequest request, User currentUser) {
        Complaint complaint = findComplaintOrThrow(id);

        User staff = userRepository.findById(request.getStaffId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff member not found"));

        if (staff.getRole() != Role.STAFF) {
            throw new BadRequestException("Selected user is not a staff member");
        }

        if (complaint.getStatus() == ComplaintStatus.CLOSED || complaint.getStatus() == ComplaintStatus.RESOLVED) {
            throw new InvalidStatusTransitionException("Cannot assign staff to a " + complaint.getStatus() + " complaint");
        }

        ComplaintStatus previousStatus = complaint.getStatus();
        complaint.setAssignedStaff(staff);
        if (previousStatus == ComplaintStatus.OPEN || previousStatus == ComplaintStatus.REOPENED) {
            complaint.setStatus(ComplaintStatus.ASSIGNED);
        }
        complaintRepository.save(complaint);

        recordHistory(complaint, previousStatus, complaint.getStatus(), currentUser, "Assigned to " + staff.getName());

        notificationService.notify(staff, "New complaint assigned to you: #" + complaint.getId() + " - " + complaint.getTitle());
        notificationService.notify(complaint.getUser(), "Your complaint has been assigned to a staff member.");

        return getById(id, currentUser);
    }

    // ==================== RESOLUTION ====================

    @Transactional
    public ComplaintResponse resolve(Long id, ResolveRequest request, User currentUser) {
        Complaint complaint = findComplaintOrThrow(id);

        if (currentUser.getRole() == Role.STAFF
                && (complaint.getAssignedStaff() == null || !complaint.getAssignedStaff().getId().equals(currentUser.getId()))) {
            throw new ForbiddenActionException("You are not assigned to this complaint");
        }

        ComplaintStatus current = complaint.getStatus();
        validateTransition(current, ComplaintStatus.RESOLVED);

        complaint.setStatus(ComplaintStatus.RESOLVED);
        complaint.setResolutionNotes(request.getResolutionNotes());
        complaint.setResolvedAt(LocalDateTime.now());
        complaintRepository.save(complaint);

        recordHistory(complaint, current, ComplaintStatus.RESOLVED, currentUser, "Resolved: " + request.getResolutionNotes());
        notificationService.notify(complaint.getUser(), "Good news - your complaint #" + complaint.getId() + " has been resolved. Please confirm and share feedback.");

        return getById(id, currentUser);
    }

    // ==================== FEEDBACK ====================

    @Transactional
    public void submitFeedback(Long complaintId, FeedbackRequest request, User currentUser) {
        Complaint complaint = findComplaintOrThrow(complaintId);

        if (!complaint.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenActionException("Only the complaint owner can submit feedback");
        }

        if (complaint.getStatus() != ComplaintStatus.RESOLVED && complaint.getStatus() != ComplaintStatus.CLOSED) {
            throw new BadRequestException("Feedback can only be given after the complaint is resolved");
        }

        if (feedbackRepository.existsByComplaint(complaint)) {
            throw new BadRequestException("Feedback has already been submitted for this complaint. Reopen it to give new feedback.");
        }

        Feedback feedback = Feedback.builder()
                .complaint(complaint)
                .user(currentUser)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();
        feedbackRepository.save(feedback);

        if (complaint.getStatus() == ComplaintStatus.RESOLVED) {
            complaint.setStatus(ComplaintStatus.CLOSED);
            complaintRepository.save(complaint);
            recordHistory(complaint, ComplaintStatus.RESOLVED, ComplaintStatus.CLOSED, currentUser, "Closed after feedback submitted");
        }
    }

    // ==================== HISTORY ====================

    public List<ComplaintHistoryResponse> getHistory(Long id, User currentUser) {
        Complaint complaint = findComplaintOrThrow(id);
        authorizeView(complaint, currentUser);
        return historyRepository.findByComplaintOrderByChangedAtAsc(complaint).stream()
                .map(ComplaintHistoryResponse::from)
                .collect(Collectors.toList());
    }

    // ==================== RELATED / DUPLICATE DETECTION ====================

    /**
     * Simple, reliable keyword-overlap similarity within the same category - deliberately
     * NOT a vector/embedding search for v1 (per spec), but isolated here so it can be
     * swapped for an embedding-based approach later without touching callers.
     */
    public List<RelatedComplaintResponse> findRelated(Long complaintId, User currentUser) {
        Complaint complaint = findComplaintOrThrow(complaintId);
        authorizeView(complaint, currentUser);

        if (complaint.getCategory() == null) {
            return List.of();
        }

        Set<String> baseWords = tokenize(complaint.getTitle() + " " + complaint.getDescription());

        return complaintRepository.findTop20ByCategory_IdOrderByCreatedAtDesc(complaint.getCategory().getId()).stream()
                .filter(c -> !c.getId().equals(complaint.getId()))
                .map(c -> {
                    Set<String> otherWords = tokenize(c.getTitle() + " " + c.getDescription());
                    double score = jaccardSimilarity(baseWords, otherWords);
                    return RelatedComplaintResponse.builder()
                            .complaintId(c.getId())
                            .title(c.getTitle())
                            .similarityScore(Math.round(score * 100.0) / 100.0)
                            .build();
                })
                .filter(r -> r.getSimilarityScore() >= 0.2)
                .sorted(Comparator.comparingDouble(RelatedComplaintResponse::getSimilarityScore).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    private Set<String> tokenize(String text) {
        return Arrays.stream(text.toLowerCase().split("\\W+"))
                .filter(w -> w.length() > 3) // skip short/stop-word-like tokens
                .collect(Collectors.toSet());
    }

    private double jaccardSimilarity(Set<String> a, Set<String> b) {
        if (a.isEmpty() || b.isEmpty()) return 0.0;
        Set<String> intersection = new HashSet<>(a);
        intersection.retainAll(b);
        Set<String> union = new HashSet<>(a);
        union.addAll(b);
        return (double) intersection.size() / union.size();
    }

    // ==================== RESOLUTION SUGGESTION (AI FEATURE 4) ====================

    public String suggestResolutionForComplaint(Long complaintId, User currentUser) {
        Complaint complaint = findComplaintOrThrow(complaintId);
        authorizeView(complaint, currentUser);

        Optional<Complaint> similarResolved = complaint.getCategory() == null ? Optional.empty() :
                complaintRepository.findTop20ByCategory_IdOrderByCreatedAtDesc(complaint.getCategory().getId()).stream()
                        .filter(c -> c.getStatus() == ComplaintStatus.RESOLVED || c.getStatus() == ComplaintStatus.CLOSED)
                        .filter(c -> !c.getId().equals(complaint.getId()))
                        .findFirst();

        try {
            return aiService.suggestResolution(
                    complaint.getDescription(),
                    similarResolved.map(Complaint::getResolutionNotes).orElse(null));
        } catch (Exception e) {
            log.warn("Resolution suggestion failed: {}", e.getMessage());
            return "AI suggestion unavailable right now - please investigate manually.";
        }
    }

    // ==================== HELPERS ====================

    private Complaint findComplaintOrThrow(Long id) {
        return complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + id));
    }
}
