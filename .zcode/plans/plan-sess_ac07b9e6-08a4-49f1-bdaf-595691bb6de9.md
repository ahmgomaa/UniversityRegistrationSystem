# University Registration System — Implementation Plan (v2)

## Context
The repo (`D:\java\UniversityRegistrationSystem`) is empty except for a README stub. The attached `UML-diagram.pdf` (workspace root) is the spec: a **Student Registration System** domain model, 5 enums, 4 application services, 2 interface ports, «record» DTOs. Per your approval: **Java 17 + Maven + MySQL (JDBC)**, full **JUnit 5** suite, **console menu UI now — JavaFX UI in a later phase** (services/domain stay UI-agnostic so JavaFX can later replace ConsoleUI without touching them).

## Architecture
- **Domain layer** — pure Java, 1:1 with the diagram (no SQL imports).
- **Service layer** — depends only on port interfaces.
- **Ports** — `NotificationService` + `StudentRepository` exactly as drawn (plus `save()`), extended with sibling repo interfaces (`Course`, `Section`, `Semester`, `Staff`, `Program`, `Department`, `Enrollment`) needed for MySQL persistence; domain model untouched.
- **Infrastructure** — JDBC implementations over `com.mysql:mysql-connector-j`; `DatabaseConnection` reads `db.properties` (env-var overridable).
- **UI** — `ConsoleUI` menu (swappable for JavaFX later); **DTOs** — Java records.

## Package layout
```
pom.xml  (Java 17, junit-jupiter 5.10, mysql-connector-j, exec-maven-plugin)
src/main/resources/  db.properties, schema.sql, seed.sql
src/main/java/com/university/registration/
  model/           User, Student, Instructor, AcademicStaff, Department, Program, Course,
                   CourseSection, Semester, RegistrationPeriod, MeetingTime, AcademicRecord, Enrollment, Grade
  model/enums/     AcademicStatus, EnrollmentStatus, Term, StaffRole, LetterGrade
  dto/             RosterEntry, GraduationReport, Transcript, SemesterSummary, Notification (records)
  service/         RegistrationService, GradingService, GraduationService, ReportService, RegistrationException
  port/            NotificationService, StudentRepository + sibling interfaces
  notification/    InAppNotificationService
  repository/mysql/  Jdbc implementations
  db/              DatabaseConnection, SchemaInitializer (idempotent schema + seed)
  ui/              ConsoleUI
  Main.java
src/test/java/...  JUnit 5 with in-memory fakes (no DB needed)
```

## Key domain rules (from the diagram)
- **register(student, section, today)** aggregates *all* violations into `RegistrationException.getErrors()`: `canRegister()` (SUSPENDED blocked); period `isOpen(today)`; `hasSeat` capacity; meeting conflicts vs current enrollments; unmet prerequisites (`!hasPassed(prereq)`); duplicate enrollment; credit limit — `maxCreditsPerSemester`, `probationMaxCredits` when PROBATION (from `Program`).
- **withdraw**: `isWithdrawalAllowed(today)` (before `withdrawalDeadline`), ENROLLED → `Enrollment.withdraw(date)`.
- **Grade**: `PASS_MARK=60`; LetterGrade scale A≥90/B+≥85/B≥80/C+≥75/C≥70/D+≥65/D≥60 else F; points 4.0/3.5/3.0/2.5/2.0/1.5/1.0/0.0.
- **GradingService**: `recordGrade` (instructor must teach section → `complete(grade)`), `updateStanding` (CGPA < `probationCGPA` → PROBATION, recovery → ACTIVE via `applyStanding`), `getRoster` (instructor-authorized) → `RosterEntry` records.
- **GraduationService**: credits ≥ `totalCreditsRequired`, CGPA ≥ `minimumCGPA`, all required courses passed → `GraduationReport(eligible, unmetReasons)`.
- **ReportService.transcript** → `Transcript` with per-semester `SemesterSummary` (GPA, registered credits), CGPA, earned credits.
- `equals/hashCode` on natural IDs; `Student.getLevel()` → `Program.levelFor(earnedCredits)`; `AcademicRecord.calculateGPA/CGPA`.

## MySQL schema
`users` (STUDENT/INSTRUCTOR/ACADEMIC_STAFF discriminator) · `students` (program, advisor, status) · `academic_staff_roles` · `departments` · `programs` · `program_courses` (REQUIRED/ELECTIVE) · `courses` · `course_prerequisites` · `semesters` · `registration_periods` · `course_sections` · `meeting_times` · `enrollments` (status, score, dates) · `notifications`. `seed.sql` demo: CS dept, 2 programs, ~8 courses with prerequisites, open FALL-2026 semester (dates relative to CURDATE() so the demo always runs), past SPRING-2026 with completed grades for transcript/graduation demos, 3 instructors, registrar/advisor staff, 4 students (password `pass123`).

## Console UI (menu)
List students · sections per semester · register (shows aggregated `RegistrationException` errors) · withdraw · record grade · roster · transcript · graduation check · notifications inbox · reseed · exit.

## Tests (`mvn test`, no DB required)
LetterGrade boundaries · Grade.isPassed · canRegister · period/withdrawal windows · meeting/section conflicts + capacity · AcademicRecord GPA/CGPA/credits · RegistrationService per-rule + aggregation · GradingService (grades, standing, roster auth) · GraduationService (eligible + each unmet reason) · ReportService (transcript structure) · InAppNotificationService. Plus a DB smoke test that self-skips when MySQL isn't reachable.

## Verification
1. `mvn test` green.
2. Load schema+seed on local MySQL (default `localhost:3306/university_registration`, credentials in `db.properties`).
3. `mvn compile exec:java` → walk the menu: rejected registration (aggregated errors shown), successful registration, withdraw, grade entry, transcript, graduation check.