package com.finance.config;

import com.finance.entity.FinancialRecord;
import com.finance.entity.User;
import com.finance.enums.RecordType;
import com.finance.enums.Role;
import com.finance.enums.UserStatus;
import com.finance.repository.FinancialRecordRepository;
import com.finance.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner seedDatabase(UserRepository userRepo,
                                   FinancialRecordRepository recordRepo,
                                   PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepo.count() > 0) {
                log.info("Database already seeded, skipping.");
                return;
            }

            log.info("Seeding database with demo users and financial records...");

            // ── Users ──────────────────────────────────────────────────────────
            User admin = userRepo.save(User.builder()
                    .name("Admin User").email("admin@finance.dev")
                    .passwordHash(passwordEncoder.encode("password123"))
                    .role(Role.ADMIN).status(UserStatus.ACTIVE).build());

            User analyst = userRepo.save(User.builder()
                    .name("Alice Analyst").email("analyst@finance.dev")
                    .passwordHash(passwordEncoder.encode("password123"))
                    .role(Role.ANALYST).status(UserStatus.ACTIVE).build());

            User viewer = userRepo.save(User.builder()
                    .name("Bob Viewer").email("viewer@finance.dev")
                    .passwordHash(passwordEncoder.encode("password123"))
                    .role(Role.VIEWER).status(UserStatus.ACTIVE).build());

            // ── Financial Records ──────────────────────────────────────────────
            LocalDate today = LocalDate.now();

            Object[][] records = {
                // amount, type, category, daysAgo, notes
                {"85000", "INCOME",  "Salary",       0,   "Monthly salary - April"},
                {"12000", "INCOME",  "Freelance",    5,   "Web design project"},
                {"3500",  "INCOME",  "Dividends",    10,  "Quarterly stock dividends"},
                {"2200",  "EXPENSE", "Rent",         1,   "Monthly apartment rent"},
                {"450",   "EXPENSE", "Groceries",    2,   "Weekly grocery run"},
                {"1200",  "EXPENSE", "Utilities",    3,   "Electricity, water, internet"},
                {"3200",  "EXPENSE", "Travel",       7,   "Business trip to Mumbai"},
                {"800",   "EXPENSE", "Food",         4,   "Restaurant dinners"},
                {"5000",  "EXPENSE", "Insurance",    15,  "Annual health insurance premium"},
                {"75000", "INCOME",  "Salary",       30,  "Monthly salary - March"},
                {"900",   "EXPENSE", "Subscriptions",20,  "Netflix, Spotify, cloud tools"},
                {"6000",  "INCOME",  "Freelance",    35,  "Mobile app development"},
                {"2800",  "EXPENSE", "Rent",         31,  "Monthly apartment rent"},
                {"500",   "EXPENSE", "Groceries",    32,  "Supermarket shopping"},
                {"1500",  "EXPENSE", "Healthcare",   40,  "Doctor visits and medicines"},
                {"2000",  "INCOME",  "Dividends",    45,  "Monthly dividend payout"},
                {"700",   "EXPENSE", "Entertainment",25,  "Movies, concerts"},
                {"75000", "INCOME",  "Salary",       60,  "Monthly salary - February"},
                {"3000",  "EXPENSE", "Travel",       55,  "Personal vacation"},
                {"400",   "EXPENSE", "Groceries",    62,  "Organic grocery store"},
                {"1100",  "EXPENSE", "Utilities",    63,  "Utilities bill - February"},
                {"4500",  "INCOME",  "Freelance",    65,  "Logo and branding project"},
            };

            for (Object[] row : records) {
                User creator = (String.valueOf(row[1]).equals("INCOME")) ? admin : analyst;
                recordRepo.save(FinancialRecord.builder()
                        .amount(new BigDecimal((String) row[0]))
                        .type(RecordType.valueOf((String) row[1]))
                        .category((String) row[2])
                        .date(today.minusDays((int) row[3]))
                        .notes((String) row[4])
                        .user(creator)
                        .build());
            }

            log.info("Seeding complete: 3 users, {} records created.", records.length);
            log.info("─────────────────────────────────────────────────");
            log.info("  Admin:    admin@finance.dev    / password123");
            log.info("  Analyst:  analyst@finance.dev  / password123");
            log.info("  Viewer:   viewer@finance.dev   / password123");
            log.info("─────────────────────────────────────────────────");
        };
    }
}
