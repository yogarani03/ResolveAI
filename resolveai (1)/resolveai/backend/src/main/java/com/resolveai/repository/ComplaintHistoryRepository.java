package com.resolveai.repository;

import com.resolveai.entity.Complaint;
import com.resolveai.entity.ComplaintHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplaintHistoryRepository extends JpaRepository<ComplaintHistory, Long> {
    List<ComplaintHistory> findByComplaintOrderByChangedAtAsc(Complaint complaint);
}
