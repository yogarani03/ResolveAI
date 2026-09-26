package com.resolveai.service;

import com.resolveai.dto.ComplaintResponse;
import com.resolveai.dto.DashboardResponse;
import com.resolveai.entity.Complaint;
import com.resolveai.entity.ComplaintStatus;
import com.resolveai.entity.User;
import com.resolveai.repository.ComplaintRepository;
import com.resolveai.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ComplaintRepository complaintRepository;
    private final FeedbackRepository feedbackRepository;

    public DashboardResponse buildAdminDashboard() {
        List<Complaint> all = complaintRepository.findAll();
        return build(all);
    }

    public DashboardResponse buildStaffDashboard(User staff) {
        List<Complaint> assigned = complaintRepository.findByAssignedStaff(staff);
        return build(assigned);
    }

    public DashboardResponse buildUserDashboard(User user) {
        List<Complaint> own = complaintRepository.findByUser(user);
        return build(own);
    }

    private DashboardResponse build(List<Complaint> complaints) {
        Map<String, Long> byCategory = complaints.stream()
                .filter(c -> c.getCategory() != null)
                .collect(Collectors.groupingBy(c -> c.getCategory().getName(), Collectors.counting()));

        Map<String, Long> byDepartment = complaints.stream()
                .filter(c -> c.getDepartment() != null)
                .collect(Collectors.groupingBy(c -> c.getDepartment().getName(), Collectors.counting()));

        List<Complaint> resolved = complaints.stream()
                .filter(c -> c.getResolvedAt() != null)
                .toList();

        Double avgResolutionHours = resolved.isEmpty() ? null : resolved.stream()
                .mapToLong(c -> Duration.between(c.getCreatedAt(), c.getResolvedAt()).toMinutes())
                .average()
                .orElse(0) / 60.0;

        List<ComplaintResponse> recent = complaints.stream()
                .sorted(Comparator.comparing(Complaint::getCreatedAt).reversed())
                .limit(10)
                .map(ComplaintResponse::from)
                .collect(Collectors.toList());

        List<ComplaintResponse> recentEscalations = complaints.stream()
                .filter(c -> c.getStatus() == ComplaintStatus.ESCALATED)
                .sorted(Comparator.comparing(Complaint::getEscalatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(10)
                .map(ComplaintResponse::from)
                .collect(Collectors.toList());

        List<Long> ratings = complaints.stream()
                .map(feedbackRepository::findByComplaint)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .map(f -> (long) f.getRating())
                .toList();

        Double avgRating = ratings.isEmpty() ? null : ratings.stream().mapToLong(Long::longValue).average().orElse(0);

        return DashboardResponse.builder()
                .totalComplaints(complaints.size())
                .openComplaints(countByStatus(complaints, ComplaintStatus.OPEN))
                .assignedComplaints(countByStatus(complaints, ComplaintStatus.ASSIGNED))
                .inProgressComplaints(countByStatus(complaints, ComplaintStatus.IN_PROGRESS))
                .escalatedComplaints(countByStatus(complaints, ComplaintStatus.ESCALATED))
                .resolvedComplaints(countByStatus(complaints, ComplaintStatus.RESOLVED))
                .closedComplaints(countByStatus(complaints, ComplaintStatus.CLOSED))
                .averageResolutionTimeHours(avgResolutionHours)
                .complaintsByCategory(byCategory)
                .complaintsByDepartment(byDepartment)
                .recentComplaints(recent)
                .recentEscalations(recentEscalations)
                .averageFeedbackRating(avgRating)
                .totalFeedbackCount(ratings.size())
                .build();
    }

    private long countByStatus(List<Complaint> complaints, ComplaintStatus status) {
        return complaints.stream().filter(c -> c.getStatus() == status).count();
    }
}
