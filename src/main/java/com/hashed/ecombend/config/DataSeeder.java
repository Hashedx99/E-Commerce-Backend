package com.hashed.ecombend.config;

import com.hashed.ecombend.common.util.SlugUtil;
import com.hashed.ecombend.feature.catalog.category.Category;
import com.hashed.ecombend.feature.catalog.category.CategoryRepository;
import com.hashed.ecombend.feature.catalog.product.Product;
import com.hashed.ecombend.feature.catalog.product.ProductRepository;
import com.hashed.ecombend.feature.user.User;
import com.hashed.ecombend.feature.user.UserRepository;
import com.hashed.ecombend.feature.user.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Seeds the database with baseline data on first startup.
 * Credentials:
 * Admin    → admin@ecombend.com  / Admin1234!
 * Customer → alice@example.com   / Password1!
 * Customer → bob@example.com     / Password1!
 * Customer → carol@example.com   / Password1!
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmail("admin@ecombend.com")) {
            log.info("Seed data already present — skipping.");
            return;
        }
        log.info("Seeding database...");
        seedUsers();
        seedCategoriesAndProducts();
        log.info("Database seeded successfully.");
    }


    private void seedUsers() {
        createUser("Admin User", "admin@ecombend.com", "Admin1234!", UserRole.ADMIN);
        createUser("Alice Johnson", "alice@example.com", "Password1!", UserRole.CUSTOMER);
        createUser("Bob Smith", "bob@example.com", "Password1!", UserRole.CUSTOMER);
        createUser("Carol White", "carol@example.com", "Password1!", UserRole.CUSTOMER);
        log.info("Seeded 4 users (1 admin, 3 customers)");
    }


    private void seedCategoriesAndProducts() {
        Category electronics = createCategory("Electronics", "Phones, laptops, gadgets");
        Category clothing = createCategory("Clothing", "Apparel for all seasons");
        Category books = createCategory("Books", "Fiction, non-fiction, textbooks");

        // Electronics
        createProduct("Apple Watch Series 9", "ELEC-AW-001",
                "GPS smartwatch with health monitoring",
                new BigDecimal("399.99"), null, 50, electronics);

        createProduct("Sony WH-1000XM5 Headphones", "ELEC-SH-001",
                "Noise-cancelling wireless headphones",
                new BigDecimal("279.99"), new BigDecimal("349.99"), 30, electronics);

        // Clothing
        createProduct("Classic White T-Shirt", "CLO-WT-001",
                "100% organic cotton, unisex fit",
                new BigDecimal("19.99"), null, 200, clothing);

        createProduct("Slim Fit Jeans", "CLO-SFJ-001",
                "Stretch denim in dark wash",
                new BigDecimal("59.99"), new BigDecimal("79.99"), 80, clothing);

        // Books
        createProduct("Clean Code", "BOOK-CC-001",
                "Robert C. Martin — A Handbook of Agile Software Craftsmanship",
                new BigDecimal("34.99"), null, 100, books);

        createProduct("Effective Java", "BOOK-EJ-001",
                "Joshua Bloch — 3rd Edition",
                new BigDecimal("44.99"), null, 75, books);

        log.info("Seeded 3 categories and 6 products");
    }


    private User createUser(String name, String email, String rawPassword, UserRole role) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setEmailVerified(true);
        return userRepository.save(user);
    }

    private Category createCategory(String name, String description) {
        Category c = new Category();
        c.setName(name);
        c.setSlug(SlugUtil.generate(name));
        c.setDescription(description);
        c.setActive(true);
        return categoryRepository.save(c);
    }

    private Product createProduct(String name, String sku, String description,
                                  BigDecimal price, BigDecimal compareAtPrice,
                                  int stock, Category category) {
        Product p = new Product();
        p.setName(name);
        p.setSlug(SlugUtil.generate(name));
        p.setSku(sku);
        p.setDescription(description);
        p.setPrice(price);
        p.setCompareAtPrice(compareAtPrice);
        p.setStock(stock);
        p.setCategory(category);
        p.setActive(true);
        return productRepository.save(p);
    }
}
