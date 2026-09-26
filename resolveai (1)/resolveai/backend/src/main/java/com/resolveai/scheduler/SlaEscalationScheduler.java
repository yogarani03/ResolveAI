package com.resolveai.scheduler;

import com.resolveai.entity.Complaint;
import com.resolveai.entity.ComplaintHistory;
import com.resolveai.entity.ComplaintStatus;
import com.resolveai.entity.Role;
import com.resolveai.repository.ComplaintHistoryRepository;
import com.resolveai.repository.ComplaintRepository;
import com.resolveai.repository.UserRepository;
import com.resolveai.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Simple, single-node scheduled job (no distributed scheduling framework needed for
 * a portfolio project). Every 5 minutes it looks for OPEN/ASSIGNED/IN_PROGRESS
 * complaints whose due date has passed and escalates them, notifying admins.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SlaEscalationScheduler {

    private final ComplaintRepository complaintRepository;
    private final ComplaintHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    private static final List<ComplaintStatus> ESCALATABLE_STATUSES =
            List.of(ComplaintStatus.OPEN, ComplaintStatus.ASSIGNED, ComplaintStatus.IN_PROGRESS);

    @Scheduled(fixedDelayString = "${app.sla.check-interval-ms:300000}") // default: every 5 minutes
    @Transactional
    public void escalateOverdueComplaints() {
        List<Complaint> overdue = complaintRepository.findByStatusInAndDueAtBefore(
                ESCALATABLE_STATUSES, LocalDateTime.now());

        if (overdue.isEmpty()) {
            return;
        }

        log.info("SLA scheduler: escalating {} overdue complaint(s)", overdue.size());

        List<com.resolveai.entity.User> admins = userRepository.findByRole(Role.ADMIN);

        for (Complaint complaint : overdue) {
            ComplaintStatus previous = complaint.getStatus();
            complaint.setStatus(ComplaintStatus.ESCALATED);
            complaint.setEscalatedAt(LocalDateTime.now());
            complaintRepository.save(complaint);

            ComplaintHistory history = ComplaintHistory.builder()
                    .complaint(complaint)
                    .fromStatus(previous)
                    .toStatus(ComplaintStatus.ESCALATED)
                    .note("Automatically escalated - SLA deadline exceeded")
                    .build();
            historyRepository.save(history);

            notificationService.notify(complaint.getUser(),
                    "Your complaint #" + complaint.getId() + " has exceeded its resolution deadline and was escalated.");

            if (complaint.getAssignedStaff() != null) {
                notificationService.notify(complaint.getAssignedStaff(),
                        "Complaint #" + complaint.getId() + " assigned to you was escalated due to SLA breach.");
            }

            for (com.resolveai.entity.User admin : admins) {
                notificationService.notify(admin,
                        "Complaint #" + complaint.getId() + " has exceeded its SLA and was escalated.");
            }
        }
    }
}
