package com.resolveai.repository;

import com.resolveai.entity.AIAnalysis;
import com.resolveai.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AIAnalysisRepository extends JpaRepository<AIAnalysis, Long> {
    Optional<AIAnalysis> findByComplaint(Complaint complaint);
}
