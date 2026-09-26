package com.resolveai.controller;

import com.resolveai.entity.ComplaintCategory;
import com.resolveai.entity.Department;
import com.resolveai.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Read-only lookup data every authenticated role needs (e.g. populating dropdowns
 *  on the "Create Complaint" form). Separate from /api/admin/** which is write-access
 *  restricted to ADMIN only. */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReferenceDataController {

    private final AdminService adminService;

    @GetMapping("/categories")
    public ResponseEntity<List<ComplaintCategory>> categories() {
        return ResponseEntity.ok(adminService.listCategories());
    }

    @GetMapping("/departments")
    public ResponseEntity<List<Department>> departments() {
        return ResponseEntity.ok(adminService.listDepartments());
    }

    @GetMapping("/staff")
    public ResponseEntity<?> staffForAssignment() {
        return ResponseEntity.ok(adminService.listStaff());
    }
}
