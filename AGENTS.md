# Ecombend AI Agent Guide

**Ecombend** is a Spring Boot 4 e-commerce REST API (Java 17) with JWT auth, Cloudinary-backed storage by default, and domain-driven feature modules.

## Architecture Overview

- **Spring Boot 4** with Data JPA (PostgreSQL/H2), Spring Security, Spring Mail, Springdoc OpenAPI
- **Feature modules** in `src/main/java/com/hashed/ecombend/feature/` — business modules (`auth`, `catalog`, `coupon`, `order`, `payment`, `review`, `user`) own entities/repositories/services/DTOs; `storage` provides pluggable file storage services
- **Unified request/response** via `ApiResponse<T>` (all endpoints return `{ success, message, data }`)
- **JWT-only** (stateless) — `JwtRequestFilter` validates Bearer tokens; see `security/SecurityConfiguration`
- **Entity base classes**: `BaseEntity` (UUID id, createdAt/updatedAt auto-managed) and `SoftDeleteEntity` (deleted_at with `@SQLRestriction`)
- **Config layer** under `config/` (Cloudinary, OpenAPI, storage resource mapping, data seeding)

## Key Patterns

**Service Interfaces & Implementations**
- Most business features use a `Service` + `ServiceImpl` pair (e.g., `CouponService`/`CouponServiceImpl`)
- Admin operations separated into `AdminService` (e.g., `CouponAdminService` for create/delete vs. `CouponService` for validate/calculateDiscount)
- Services are interface-driven, with concrete implementations such as `ManualPaymentService` (`PaymentService`) and `CloudinaryStorageService` (`StorageService`, `@Primary`)

**Exception Handling**
- Domain exceptions in `common/exception/`: `ResourceNotFoundException`, `DuplicateResourceException`, `InsufficientStockException`, `BusinessException`, `StorageException`, `EmailDeliveryException`
- `RestExceptionHandler` (global `@ControllerAdvice`) catches all exceptions and returns `ApiResponse.error()`
- Validation errors from `@Valid` return `ApiResponse.error("Validation failed: {field=message,...}")` (field errors are embedded in the message string; `data` is null)

**Email Service**
- Abstract `AbstractEmailContext` + `EmailService` interface
- Implementations like `AccountVerificationEmailContext`, `AccountPasswordResetEmailContext`
- `DefaultEmailService` renders Thymeleaf templates from `src/main/resources/templates/mailing/`

**Auth & Security**
- Register → email verification token required before login
- Login returns JWT (stored in `Authorization: Bearer <token>`)
- `@PreAuthorize("hasRole('ADMIN')")` gates admin endpoints
- Public routes include `/auth/**`, `/h2-console/**`, `GET /api/products/**`, `GET /api/categories/**`, `GET /files/**`, and Swagger/OpenAPI endpoints (`/swagger-ui.html`, `/swagger-ui/**`, `/v3/api-docs`, `/v3/api-docs/**`, `/v3/api-docs.yaml`, `/webjars/**`); everything else requires JWT

## Essential Commands

```bash
# Build & run
mvn clean package
mvn spring-boot:run

# Code formatting (Google Java Format AOSP style with Spotless)
mvn spotless:apply

# Run tests
mvn test

# Dev profile (H2 in-memory DB with seed data)
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

**Seed Credentials** (dev profile only):
- Admin: `admin@ecombend.com` / `Admin1234!`
- Customers: `alice@example.com`, `bob@example.com`, `carol@example.com` / `Password1!`

## Developer Workflows

1. **New Feature Module**: Create folder in `feature/`, add Entity (extends `BaseEntity` or `SoftDeleteEntity`), Repository (JpaRepository), Service interface + ServiceImpl, Controller with `@RestController` endpoints
2. **DTOs for Input/Output**: Keep in `feature/*/dto/` — always validate with `@Valid` + use Spring's `@NotBlank`, `@Email`, etc.
3. **Database Changes**: Modify entity → Spring Data JPA auto-manages schema (current dev/prod both use `ddl-auto=create-drop`)
4. **Testing**: Place tests in `src/test/java/com/hashed/ecombend/feature/*/` — current pattern is mostly JUnit 5 + Mockito unit tests (`@ExtendWith(MockitoExtension.class)`), plus `EcombendApplicationTests` as a `@SpringBootTest` smoke test

## Code Style & Conventions

- **Spotless + Google Java Format** enforces AOSP style (4-space indent, no mutable state without good reason)
- **Lombok `@Getter @Setter @RequiredArgsConstructor`** for boilerplate
- **No manual timestamps**: use `@PrePersist`/`@PreUpdate` on `BaseEntity`
- **No UUID generation in code**: use `@GeneratedValue(strategy = GenerationType.UUID)` on `@Id`
- **Soft deletes** preferred to hard deletes — extend `SoftDeleteEntity` and call `softDelete()`

## Integration Points

| Component | Config | Notes |
|-----------|--------|-------|
| **JWT** | `application-dev.properties` / `application-prod.properties`: `jwt-secret`, `jwt-expiration-ms` | See `JWTUtils` |
| **Email** | Spring Mail (SMTP) | Template context → `EmailService.sendMail()` |
| **Cloudinary** | `CloudinaryConfiguration` | Default storage provider via `CloudinaryStorageService` (`@Primary`) |
| **Storage** | Cloudinary config + `storage.base-path`, `storage.base-url` | `LocalStorageService` exists but is not primary; `/files/**` is mapped to local disk via `StorageResourceConfiguration`; `S3StorageService` only loads under `s3` profile |
| **App base URL** | `site.base.url` | Used to build verification and reset links in auth emails |
| **Payments** | `ManualPaymentService` | `@Primary` payment service; order placement does not call `PaymentService` yet |
| **Database** | Dev: H2 (in-memory), Prod: PostgreSQL | `application-dev.properties`, `application-prod.properties` |

## Common Tasks

- **Add a new coupon rule**: Implement logic in `CouponServiceImpl.calculateDiscount()` — test edge cases (expired, max uses, min subtotal)
- **New order workflow**: `OrderController` → `OrderServiceImpl` → validates product availability/stock, snapshots address/items, computes subtotal + stub discount/shipping/tax, saves `Order` + `OrderItem` rows (no payment creation yet)
- **Order status transitions**: enforced in `OrderServiceImpl.validateTransition()` (PENDING→CONFIRMED→SHIPPED→DELIVERED; CANCELLED allowed before DELIVERED; REFUNDED only after DELIVERED)
- **Email on action**: Create `MyActionEmailContext extends AbstractEmailContext`, add template in `templates/mailing/`, inject `EmailService` in service
- **Reviews**: `ReviewServiceImpl` auto-sets verified purchase based on delivered orders
- **Protect endpoint**: Add `@PreAuthorize("hasRole('ADMIN')")` or check user role in `SecurityUtil.getCurrentUser()`

## Debugging Tips

- `server.error.include-stacktrace=ALWAYS` is already set in `application.properties` (dev is default profile)
- JWT validation failures logged in `JwtRequestFilter`
- Enable SQL logging: `spring.jpa.show-sql=true` in `application-dev.properties`
- Seed data inserted by `DataSeeder` on first run (check logs for "Seeding database...")

