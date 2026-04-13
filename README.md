# Ecombend

## Overview

Ecombend is a Spring Boot 4 e-commerce REST API built with Java 17. It supports authentication, product catalog management, orders, coupons, reviews, profile management, and file uploads, with JWT-based security and role-aware access control.


---

## Pride Points

- Feature-based architecture under `feature/*` keeps business logic modular and scalable.
- Security-first flow using stateless JWT auth with controlled public routes (`/auth/**`, public GET browsing).
- Clear role boundaries with `CUSTOMER` and `ADMIN` access enforced via `@PreAuthorize`.
- Consistent response contract through `ApiResponse<T>` for success and error handling.
- Email verification + password reset workflows integrated with templated mailing.
- Profile and product media upload support through a pluggable storage abstraction.

---

## Technologies Used

- Java 17
- Spring Boot 4
- Spring Data JPA
- Spring Security + JWT (`jjwt`)
- Spring Mail + Thymeleaf
- H2 (dev) and PostgreSQL (prod)
- Cloudinary integration + local file storage
- Springdoc OpenAPI (Swagger)
- Lombok, Apache Commons Lang
- JUnit 5, Mockito, Spring Security Test
- Maven + Spotless (Google Java Format AOSP)

---

## Requirement Mapping

- `6+ models persisted`: user, address, category, product, product image, order, order item, coupon, payment, review.
- `Spring Profiles`: `dev` and `prod` properties are separated.
- `Concurrency control`: inventory/order operations are coordinated in service layer workflows.
- `CRUD with REST conventions`: provided across auth, catalog, user, order, coupon, and review domains.
- `Exception handling`: centralized via global exception handling and structured API messages.
- `Security`: JWT-protected routes with public login/register/verification/password reset endpoints.
- `Role management`: at least `ADMIN` and `CUSTOMER` roles.
- `Email verification`: required before login.
- `File upload`: profile picture and product image uploads.
- `Soft delete`: admin-driven user deactivation and soft-delete behavior.
- `Password recovery + change password`: available through auth/user flows.
- `Seed data`: development profile includes initial seeded records.

---

## API Snapshot

| Request Type | URL | Functionality | Access |
|--------------|-----|---------------|--------|
| POST | `/auth/register` | Register account | Public |
| POST | `/auth/login` | Login and receive JWT | Public |
| GET | `/auth/verify?token=...` | Verify email | Public |
| POST | `/auth/forgot-password` | Request reset link | Public |
| POST | `/auth/reset-password` | Reset password with token | Public |
| GET | `/api/categories` | List categories | Public |
| GET | `/api/products` | List/filter products | Public |
| POST | `/api/orders` | Place order | Private/CUSTOMER |
| PUT | `/api/users/profile` | Update own profile | Private |
| DELETE | `/api/admin/users/{id}` | Soft-delete user | Private/ADMIN |

---

## Planning and Tracking

- ERD Diagram: `[Add ERD link here]`
- Jira Board: `[Add Jira board link here]`

---

## Setup and Installation

### Prerequisites

- Java 17
- Maven 3.9+
- SMTP credentials (for verification/reset mail)
- Cloudinary credentials (if cloud media is enabled)
- PostgreSQL (for production profile)

### Run Locally (dev)

```bash
mvn clean package
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

### Run with Production Profile

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"
```

### Environment Variables

- `JWT_SECRET_KEY`
- `MY_EMAIL`
- `API_KEY`
- `API_CLOUDNAME`
- `CLOUDINARY_KEY`
- `API_SECRET`
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `APP_BASE_URL`

---

## Seed Data (dev profile)

- Admin: `admin@ecombend.com` / `Admin1234!`
- Customers: `alice@example.com`, `bob@example.com`, `carol@example.com` / `Password1!`

---

## Planning and Development Process

### System Design

- Domain-driven modules (`auth`, `catalog`, `coupon`, `order`, `payment`, `review`, `user`, `storage`)
- Service interfaces + implementations for business logic boundaries
- Global response wrapper (`ApiResponse<T>`) and centralized exception handling
- Stateless auth pipeline with JWT filter and method-level authorization
- Reusable storage abstraction to support local and external providers

### Implementation Sequence

1. Authentication, JWT pipeline, and role-based authorization
2. User account lifecycle (register, verify, reset, change password)
3. Catalog and category CRUD endpoints
4. Upload flows for profile and product media
5. Order placement and order status workflows
6. Coupon/review/payment integrations
7. Seed data, profile separation, and test hardening

### Problem-Solving Strategy

- Kept endpoint contracts consistent with a unified response envelope
- Applied feature-module boundaries to prevent cross-domain coupling
- Used validation + domain exceptions to keep API errors actionable
- Prioritized public-read/private-write security posture
- Added pagination/filter support on product and admin order listing

---

## Known Hurdles / Next Steps

- Expand endpoint documentation into a complete API reference for reviewers.
- Increase integration test coverage for auth + order edge cases.
- Continue improving pagination/sorting/filtering across more list endpoints.
- Evolve payment flow into full third-party webhook processing.

