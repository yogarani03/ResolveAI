package com.resolveai.repository;

import com.resolveai.entity.Complaint;
import com.resolveai.entity.ComplaintStatus;
import com.resolveai.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    List<Complaint> findByUser(User user);

    List<Complaint> findByAssignedStaff(User staff);

    List<Complaint> findByStatus(ComplaintStatus status);

    List<Complaint> findByStatusIn(List<ComplaintStatus> statuses);

    // Used for SLA escalation job: still open/in-progress but past due date
    List<Complaint> findByStatusInAndDueAtBefore(List<ComplaintStatus> statuses, LocalDateTime now);

    // Used for simple related/duplicate detection: recent complaints in same category
    List<Complaint> findTop20ByCategory_IdOrderByCreatedAtDesc(Long categoryId);

    long countByStatus(ComplaintStatus status);
}
