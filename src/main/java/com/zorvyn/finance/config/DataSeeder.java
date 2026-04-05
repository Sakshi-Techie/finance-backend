package com.zorvyn.finance.config;

import com.zorvyn.finance.model.FinancialRecord;
import com.zorvyn.finance.model.Role;
import com.zorvyn.finance.model.TransactionType;
import com.zorvyn.finance.model.User;
import com.zorvyn.finance.repository.FinancialRecordRepository;
import com.zorvyn.finance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final FinancialRecordRepository recordRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Seeding initial data...");

        // Create Admin user
        User admin = User.builder()
                .name("Admin User")
                .email("admin@zorvyn.com")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .active(true)
                .build();
        admin = userRepository.save(admin);

        // Create Analyst user
        User analyst = User.builder()
                .name("Analyst User")
                .email("analyst@zorvyn.com")
                .password(passwordEncoder.encode("analyst123"))
                .role(Role.ANALYST)
                .active(true)
                .build();
        userRepository.save(analyst);

        // Create Viewer user
        User viewer = User.builder()
                .name("Viewer User")
                .email("viewer@zorvyn.com")
                .password(passwordEncoder.encode("viewer123"))
                .role(Role.VIEWER)
                .active(true)
                .build();
        userRepository.save(viewer);

        // Seed sample financial records
        LocalDate now = LocalDate.now();

        recordRepository.save(FinancialRecord.builder()
                .amount(new BigDecimal("50000.00"))
                .type(TransactionType.INCOME)
                .category("Salary")
                .date(now.minusDays(5))
                .notes("Monthly salary")
                .createdBy(admin).build());

        recordRepository.save(FinancialRecord.builder()
                .amount(new BigDecimal("12000.00"))
                .type(TransactionType.EXPENSE)
                .category("Rent")
                .date(now.minusDays(4))
                .notes("Office rent")
                .createdBy(admin).build());

        recordRepository.save(FinancialRecord.builder()
                .amount(new BigDecimal("3500.00"))
                .type(TransactionType.EXPENSE)
                .category("Utilities")
                .date(now.minusDays(3))
                .notes("Electricity and internet")
                .createdBy(admin).build());

        recordRepository.save(FinancialRecord.builder()
                .amount(new BigDecimal("8000.00"))
                .type(TransactionType.INCOME)
                .category("Consulting")
                .date(now.minusDays(2))
                .notes("Client consulting fee")
                .createdBy(admin).build());

        recordRepository.save(FinancialRecord.builder()
                .amount(new BigDecimal("1500.00"))
                .type(TransactionType.EXPENSE)
                .category("Marketing")
                .date(now.minusDays(1))
                .notes("Digital marketing campaign")
                .createdBy(admin).build());

        recordRepository.save(FinancialRecord.builder()
                .amount(new BigDecimal("25000.00"))
                .type(TransactionType.INCOME)
                .category("Sales")
                .date(now.minusMonths(1))
                .notes("Product sales revenue")
                .createdBy(admin).build());

        recordRepository.save(FinancialRecord.builder()
                .amount(new BigDecimal("4200.00"))
                .type(TransactionType.EXPENSE)
                .category("Software")
                .date(now.minusMonths(1).plusDays(5))
                .notes("SaaS subscriptions")
                .createdBy(admin).build());

        log.info("Data seeding complete!");
        log.info("==============================================");
        log.info("Test Credentials:");
        log.info("  ADMIN   -> admin@zorvyn.com   / admin123");
        log.info("  ANALYST -> analyst@zorvyn.com / analyst123");
        log.info("  VIEWER  -> viewer@zorvyn.com  / viewer123");
        log.info("==============================================");
    }
}
