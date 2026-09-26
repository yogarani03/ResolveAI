package com.resolveai.service;

import com.resolveai.dto.*;
import com.resolveai.entity.*;
import com.resolveai.exception.DuplicateResourceException;
import com.resolveai.exception.ResourceNotFoundException;
import com.resolveai.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final ComplaintCategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    // ---------- Users ----------

    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream().map(UserResponse::from).toList();
    }

    public List<UserResponse> listStaff() {
        return userRepository.findByRole(Role.STAFF).stream().map(UserResponse::from).toList();
    }

    @Transactional
    public UserResponse createStaff(CreateStaffRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        }

        User staff = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.STAFF)
                .department(department)
                .build();

        return UserResponse.from(userRepository.save(staff));
    }

    // ---------- Departments ----------

    public List<Department> listDepartments() {
        return departmentRepository.findAll();
    }

    @Transactional
    public Department createDepartment(DepartmentRequest request) {
        Department department = Department.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        return departmentRepository.save(department);
    }

    // ---------- Categories ----------

    public List<ComplaintCategory> listCategories() {
        return categoryRepository.findAll();
    }

    @Transactional
    public ComplaintCategory createCategory(CategoryRequest request) {
        ComplaintCategory category = ComplaintCategory.builder()
                .name(request.getName())
                .subCategory(request.getSubCategory())
                .build();
        return categoryRepository.save(category);
    }
}
