ResolveAI — Intelligent Complaint Resolution Platform
A full-stack complaint resolution platform built with Spring Boot + MySQL (backend) and React (frontend). Users submit complaints, AI suggests category/priority/summary and flags possibly related complaints, staff investigate and resolve, an SLA scheduler escalates overdue complaints, and admins get real analytics. The AI is an assistant, not a dependency — if it's down, complaints still get created and staff categorize manually.

1. Project Overview
ResolveAI demonstrates a realistic, interview-ready complaint-management product: role-based access (USER / STAFF / ADMIN), a guarded status workflow, JWT auth, a clean layered Spring Boot backend, a connected React frontend, and a defensively-designed AI integration layer.

2. Features
Register/login with JWT, BCrypt password hashing, role-based authorization enforced server-side
Complaint CRUD with backend validation (never trusts the frontend alone)
Guarded status workflow: OPEN → ASSIGNED → IN_PROGRESS → ESCALATED → RESOLVED → CLOSED → REOPENED (invalid jumps, e.g. CLOSED → IN_PROGRESS directly, are rejected with 422)
AI complaint categorization + priority + summary (mock offline provider by default, real provider pluggable), stored as a suggestion, never authoritative
Simple, reliable keyword-overlap related/duplicate complaint detection (structured so a vector/embedding approach can be swapped in later without touching callers)
AI resolution suggestions based on similar previously-resolved complaints (staff decides)
Full AI failure handling: timeout / unavailable / malformed / empty response never breaks complaint creation
SLA due dates + a simple @Scheduled job that auto-escalates overdue complaints and notifies admins
In-app notifications
One-time feedback (1–5 rating + comment) per complaint, reopenable
Admin dashboard with real DB-derived analytics (no hardcoded numbers)
Global @RestControllerAdvice exception handling — consistent JSON errors, no stack traces leaked
Seeded demo accounts + sample complaints so the app is demonstrable immediately
3. Technology Stack
Backend: Java 17, Spring Boot 3.3, Spring Web, Spring Data JPA, Spring Security, JWT (jjwt), MySQL, Maven, Bean Validation, Lombok Frontend: React 18, React Router, Axios, plain CSS Database: MySQL 8

4. Architecture
React SPA  →  HTTP (JWT Bearer)  →  Spring Boot REST API
                                        │
                             Controller → Service → Repository → MySQL
                                        │
                                   AIService (interface)
                                   ├─ AIServiceMockImpl (default, offline)
                                   └─ AIServiceOpenAIImpl (opt-in, real provider)
Business logic lives in the service layer only — controllers stay thin.

5. Database Design
Entity	Purpose
User	id, name, email, passwordHash, role (USER/STAFF/ADMIN enum), department (FK, staff only)
Department	Support departments (IT, Billing, Facilities...)
ComplaintCategory	Category + sub-category
Complaint	Core entity — title, description, status, priority, user (FK), category (FK), department (FK), assignedStaff (FK), dueAt, escalatedAt, resolvedAt, resolutionNotes
ComplaintHistory	Full audit trail of every status change
AIAnalysis	1:1 with Complaint — suggested category/priority/summary + AI status (SUCCESS/FAILED/UNAVAILABLE/TIMEOUT)
Feedback	1:1 with Complaint — rating (1–5) + comment
Notification	In-app notifications per user
Constraints: unique email, unique (complaint, feedback), unique (complaint, ai_analysis), indexes on status, user_id, due_at. spring.jpa.hibernate.ddl-auto=update is used deliberately for this portfolio project so the schema auto-creates from JPA entities against an empty database — for a real production system you'd switch to Flyway/Liquibase.

6. Backend Setup
Prerequisites: Java 17+, Maven, MySQL 8 running locally.

# 1. Create the database (or let the app auto-create it — see application.properties)
mysql -u root -p -e "CREATE DATABASE resolveai;"

# 2. Configure environment variables (see section 9) or edit application.properties directly

# 3. Run
cd backend
mvn spring-boot:run
The backend starts on http://localhost:8080. On first run against an empty DB, it seeds demo accounts and sample complaints automatically (see section 13).

7. Frontend Setup
Prerequisites: Node.js 18+.

cd frontend
npm install
npm start
The frontend starts on http://localhost:3000 and talks to the backend at http://localhost:8080/api (override with REACT_APP_API_URL if needed).

8. MySQL Setup
Default connection (edit in backend/src/main/resources/application.properties or via env vars):

spring.datasource.url=jdbc:mysql://localhost:3306/resolveai
spring.datasource.username=root
spring.datasource.password=${DB_PASSWORD:root}
9. AI API Setup
By default, AI_PROVIDER is unset/mock — the app uses a built-in offline mock AI (keyword-based categorization) so it runs and can be demoed with zero external API key.

To use a real AI provider (OpenAI-compatible chat completions API):

Get an API key from your provider.
Set these environment variables before starting the backend:
AI_PROVIDER=openai
AI_API_KEY=sk-...your-key...
AI_API_URL=https://api.openai.com/v1/chat/completions   # optional, this is the default
Never commit your API key to GitHub. Put it in your shell environment, an untracked .env file, or your IDE's run configuration — never in application.properties directly.
The provider-specific code lives entirely in backend/src/main/java/com/resolveai/ai/AIServiceOpenAIImpl.java, isolated behind the AIService interface, so swapping providers never touches business logic.

10. Environment Variables
Variable	Default	Purpose
DB_PASSWORD	root	MySQL password
JWT_SECRET	(dev default in properties)	Change this in production
JWT_EXPIRATION_MS	86400000 (24h)	JWT lifetime
AI_PROVIDER	mock	mock or openai
AI_API_KEY	(empty)	Required only if AI_PROVIDER=openai
AI_API_URL	OpenAI endpoint	Override for a different OpenAI-compatible provider
AI_TIMEOUT_MS	8000	AI request timeout
SLA_DEFAULT_HOURS	48	Default resolution deadline for new complaints
CORS_ORIGIN	http://localhost:3000	Allowed frontend origin
REACT_APP_API_URL (frontend)	http://localhost:8080/api	Backend base URL
11. How to Run Backend
cd backend
mvn spring-boot:run
12. How to Run Frontend
cd frontend
npm install
npm start
13. Default Test Accounts
Role	Email	Password
Admin	admin@resolveai.com	Admin@123
Staff (IT)	staff1@resolveai.com	Staff@123
Staff (Billing)	staff2@resolveai.com	Staff@123
User	user1@resolveai.com	User@123
User	user2@resolveai.com	User@123
(Seeded automatically on first run against an empty database — see DataSeeder.java.)

14. API Overview
POST   /api/auth/register
POST   /api/auth/login

POST   /api/complaints
GET    /api/complaints                    (role-filtered: own / assigned / all)
GET    /api/complaints/{id}
PATCH  /api/complaints/{id}/status
POST   /api/complaints/{id}/assign        (STAFF/ADMIN)
POST   /api/complaints/{id}/resolve       (STAFF/ADMIN)
POST   /api/complaints/{id}/feedback      (owner only, after RESOLVED)
GET    /api/complaints/{id}/related
GET    /api/complaints/{id}/history
GET    /api/complaints/{id}/resolution-suggestion

GET    /api/notifications
GET    /api/dashboard/admin | /staff | /user

GET    /api/categories | /api/departments | /api/staff   (reference data, any authenticated role)

GET/POST /api/admin/users | /api/admin/staff | /api/admin/categories | /api/admin/departments  (ADMIN only)
All responses use consistent HTTP status codes (200/201/204/400/401/403/404/409/422/500) and a shared JSON error shape from GlobalExceptionHandler.

15. Testing Instructions
Manual/API testing (Postman/Bruno): every workflow above is a plain REST call — register, login (copy the token), then send Authorization: Bearer <token> on subsequent requests.

Key scenarios to test:

Register → login → create complaint → check aiAnalysis in the response
Try an invalid status transition (e.g. OPEN → RESOLVED directly) → expect 422
Assign a non-STAFF user to a complaint → expect 400
Submit feedback twice on the same complaint → expect 400 on the second attempt
Log in as a USER and try GET /api/admin/users → expect 403
Stop MySQL or set AI_API_KEY to garbage with AI_PROVIDER=openai → complaint creation still succeeds; aiAnalysis.status becomes FAILED/UNAVAILABLE
Playwright E2E (suggested flow): register/login → create complaint → (as staff) login, assign, update status, resolve → (as user) login, view resolved complaint, submit feedback. Use data-testid attributes as needed (form inputs already use stable id/label pairs).

16. Future Enhancements
Replace keyword-overlap similarity with real vector embeddings for related-complaint detection
File attachments (currently structured for but not implemented — add ComplaintAttachment upload endpoint)
Email/SMS notifications alongside in-app notifications
Flyway/Liquibase migrations instead of ddl-auto=update
Pagination on complaint lists for large datasets
Refresh tokens instead of a single long-lived JWT
Project Structure
resolveai/
  backend/
    pom.xml
    src/main/java/com/resolveai/
      entity/        (User, Complaint, ComplaintHistory, AIAnalysis, Feedback, Notification, ...)
      repository/    (Spring Data JPA repositories)
      dto/           (request/response DTOs)
      security/      (JWT filter/util, UserDetailsService)
      service/       (AuthService, ComplaintService, DashboardService, AdminService, NotificationService)
      controller/     (AuthController, ComplaintController, AdminController, DashboardController, ...)
      ai/            (AIService interface, mock + OpenAI implementations)
      scheduler/     (SlaEscalationScheduler)
      exception/     (custom exceptions + GlobalExceptionHandler)
      config/        (SecurityConfig, DataSeeder)
    src/main/resources/application.properties
  frontend/
    src/
      components/    (Navbar, ProtectedRoute, StatusBadge)
      pages/         (Landing, Login, Register, dashboards, complaint pages, admin pages)
      services/api.js
      context/AuthContext.js
      App.js
Common Errors & Fixes
Symptom	Fix
Communications link failure on backend start	MySQL isn't running, or wrong DB_PASSWORD
Frontend gets CORS errors	Confirm backend CORS_ORIGIN matches http://localhost:3000
401 Unauthorized on every request after login	Token expired (24h default) — log in again
AI analysis always UNAVAILABLE	Expected with default mock provider only if AI_PROVIDER was mistakenly set to openai without a key — leave AI_PROVIDER unset for the mock
mvn spring-boot:run fails to download dependencies	Check internet access to Maven Central is allowed in your environment
