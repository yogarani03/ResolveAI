package com.resolveai.config;

import com.resolveai.entity.*;
import com.resolveai.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Seeds the database with default accounts and sample data ONLY if it is empty, so the
 * application is immediately demonstrable after `mvn spring-boot:run` against a blank DB.
 * See README "Default test accounts" for the full credential list.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final ComplaintCategoryRepository categoryRepository;
    private final ComplaintRepository complaintRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already has data - skipping seed.");
            return;
        }

        log.info("Seeding ResolveAI database with default accounts and sample data...");

        Department it = departmentRepository.save(Department.builder().name("IT Support").description("Network, hardware and software issues").build());
        Department billing = departmentRepository.save(Department.builder().name("Billing").description("Payments, refunds and invoices").build());
        Department facilities = departmentRepository.save(Department.builder().name("Facilities").description("Building and physical infrastructure").build());

        ComplaintCategory payment = categoryRepository.save(ComplaintCategory.builder().name("PAYMENT").subCategory("PAYMENT_DEDUCTED_ORDER_FAILED").build());
        ComplaintCategory network = categoryRepository.save(ComplaintCategory.builder().name("NETWORK").subCategory("CONNECTIVITY_ISSUE").build());
        categoryRepository.save(ComplaintCategory.builder().name("HARDWARE").subCategory("DEVICE_MALFUNCTION").build());
        categoryRepository.save(ComplaintCategory.builder().name("STAFF_CONDUCT").subCategory("BEHAVIOR_COMPLAINT").build());
        categoryRepository.save(ComplaintCategory.builder().name("GENERAL").subCategory("UNCLASSIFIED").build());

        User admin = userRepository.save(User.builder()
                .name("System Admin")
                .email("admin@resolveai.com")
                .passwordHash(passwordEncoder.encode("Admin@123"))
                .role(Role.ADMIN)
                .build());

        User staff1 = userRepository.save(User.builder()
                .name("Ravi Kumar")
                .email("staff1@resolveai.com")
                .passwordHash(passwordEncoder.encode("Staff@123"))
                .role(Role.STAFF)
                .department(it)
                .build());

        User staff2 = userRepository.save(User.builder()
                .name("Priya Sharma")
                .email("staff2@resolveai.com")
                .passwordHash(passwordEncoder.encode("Staff@123"))
                .role(Role.STAFF)
                .department(billing)
                .build());

        User user1 = userRepository.save(User.builder()
                .name("Arjun Mehta")
                .email("user1@resolveai.com")
                .passwordHash(passwordEncoder.encode("User@123"))
                .role(Role.USER)
                .build());

        User user2 = userRepository.save(User.builder()
                .name("Sneha Iyer")
                .email("user2@resolveai.com")
                .passwordHash(passwordEncoder.encode("User@123"))
                .role(Role.USER)
                .build());

        // Sample complaints covering different states so the app looks realistic on first run.
        Complaint c1 = complaintRepository.save(Complaint.builder()
                .title("Payment deducted but order not confirmed")
                .description("My payment was deducted from my bank account but my order was not confirmed on the app.")
                .status(ComplaintStatus.OPEN)
                .priority(Priority.HIGH)
                .user(user1)
                .category(payment)
                .department(billing)
                .dueAt(LocalDateTime.now().plusHours(48))
                .build());

        Complaint c2 = complaintRepository.save(Complaint.builder()
                .title("Wi-Fi not working in Block A")
                .description("The Wi-Fi connection has been down in Block A since this morning.")
                .status(ComplaintStatus.ASSIGNED)
                .priority(Priority.MEDIUM)
                .user(user2)
                .category(network)
                .department(it)
                .assignedStaff(staff1)
                .dueAt(LocalDateTime.now().plusHours(48))
                .build());

        Complaint c3 = complaintRepository.save(Complaint.builder()
                .title("Internet connection unavailable in Block A")
                .description("Internet has not been working in Block A for the last two hours.")
                .status(ComplaintStatus.OPEN)
                .priority(Priority.MEDIUM)
                .user(user1)
                .category(network)
                .department(it)
                .dueAt(LocalDateTime.now().plusHours(48))
                .build());

        Complaint c4 = complaintRepository.save(Complaint.builder()
                .title("Printer paper jam error with no visible paper")
                .description("The office printer keeps showing a paper jam error even though there is no paper stuck inside.")
                .status(ComplaintStatus.RESOLVED)
                .priority(Priority.LOW)
                .user(user2)
                .department(facilities)
                .assignedStaff(staff1)
                .resolutionNotes("Cleaned the paper sensor and rollers; issue resolved.")
                .resolvedAt(LocalDateTime.now().minusDays(1))
                .dueAt(LocalDateTime.now().plusHours(48))
                .build());

        log.info("Seed complete. Admin login: admin@resolveai.com / Admin@123");
    }
}
