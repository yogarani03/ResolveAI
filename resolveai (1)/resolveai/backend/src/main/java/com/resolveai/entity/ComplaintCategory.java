package com.resolveai.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "complaint_categories", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name", "sub_category"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ComplaintCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "sub_category", length = 100)
    private String subCategory;
}
