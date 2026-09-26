package com.resolveai.repository;

import com.resolveai.entity.ComplaintCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComplaintCategoryRepository extends JpaRepository<ComplaintCategory, Long> {
}
