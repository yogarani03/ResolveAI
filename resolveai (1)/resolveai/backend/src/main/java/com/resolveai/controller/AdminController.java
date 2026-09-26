package com.resolveai.controller;

import com.resolveai.dto.*;
import com.resolveai.entity.ComplaintCategory;
import com.resolveai.entity.Department;
import com.resolveai.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** All endpoints here require ROLE_ADMIN - enforced centrally in SecurityConfig
 *  ("/api/admin/**" -> hasRole("ADMIN")), not just checked in the frontend. */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> listUsers() {
        return ResponseEntity.ok(adminService.listUsers());
    }

    @GetMapping("/staff")
    public ResponseEntity<List<UserResponse>> listStaff() {
        return ResponseEntity.ok(adminService.listStaff());
    }

    @PostMapping("/staff")
    public ResponseEntity<UserResponse> createStaff(@Valid @RequestBody CreateStaffRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createStaff(request));
    }

    @GetMapping("/departments")
    public ResponseEntity<List<Department>> listDepartments() {
        return ResponseEntity.ok(adminService.listDepartments());
    }

    @PostMapping("/departments")
    public ResponseEntity<Department> createDepartment(@Valid @RequestBody DepartmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createDepartment(request));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<ComplaintCategory>> listCategories() {
        return ResponseEntity.ok(adminService.listCategories());
    }

    @PostMapping("/categories")
    public ResponseEntity<ComplaintCategory> createCategory(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createCategory(request));
    }
}
