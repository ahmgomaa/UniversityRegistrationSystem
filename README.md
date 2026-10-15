# University Registration System

A comprehensive Java & JavaFX-based university registration and academic management system built according to clean architecture principles and styled with **Material Design 3 (M3)** matching the UI architecture in [Agono0/gym-management-system](http://github.com/Agono0/gym-management-system).

---

## 🎨 UI Architecture: Material Design 3 (M3fx Style)

The desktop interface adopts Google's Material Design 3 design system with dynamic JavaFX CSS token injection (`-md-sys-color-*`), matching the patterns in `m3fx`:

- **Dynamic Theme Engine (`ThemeManager` & `M3Palette`)**:
  - **Light & Dark Mode**: One-click runtime toggle (`🌙 Dark` / `☀️ Light`) affecting every component without application restart.
  - **Dynamic Seed Color Palettes**: Switch among 4 M3 tonal palettes at runtime:
    - 🔵 **University Blue** (`#1a73e8`)
    - 🟣 **Indigo** (`#4f46e5`)
    - 🟢 **Emerald** (`#059669`)
    - 🔴 **Crimson** (`#dc2626`)
- **M3 Navigation Drawer**:
  - Full-height navigation drawer with categorized sections (`CORE PLATFORM`, `REGISTRATION & GRADING`, `RECORDS & AUDITS`).
  - Active destination pill indicators (`m3-nav-item-active`) with smooth hover states.
  - Embedded current user badge with role authorization indicator (`REGISTRAR`).
- **M3 Top App Bar**:
  - Branding header with title and subtitle.
  - Interactive **Simulated Date Widget** (`📅 DatePicker` + quick presets) allowing testing of registration open/close windows, class start dates, and withdrawal deadlines on the fly.
  - Runtime **Theme & Seed Palette Switchers**.
  - **Notification Center Bell** with dynamic badge count.
- **M3 Component Suite**:
  - `m3-card-elevated`, `m3-card-filled`, `m3-card-outlined` cards.
  - `m3-button-filled`, `m3-button-tonal`, `m3-button-outlined`, `m3-button-text`, `m3-button-danger` buttons.
  - Pill-shaped `m3-search-bar` with inline search icons.
  - Interactive `m3-chip` filter bars.
  - Color-coded status badges (`m3-badge-active`, `m3-badge-probation`, `m3-badge-suspended`, `m3-badge-success`, `m3-badge-warning`).
  - Styled `TableView`, `ListView`, `ComboBox`, `ProgressBar`, `Slider`, and `TabPane`.

---

## Architecture

```
com.university.registration
├── model/                      # Domain entities
│   ├── enums/                  # Term, AcademicStatus, EnrollmentStatus, LetterGrade, StaffRole
│   ├── User                    # Abstract base class for all users
│   ├── Student                 # Student with academic record, program, standing
│   ├── Instructor              # Course section instructor
│   ├── AcademicStaff           # Advisor / Registrar with role-based authorization
│   ├── Department              # Academic department
│   ├── Program                 # Degree program with requirements
│   ├── Course                  # Course with prerequisites (self-referencing)
│   ├── CourseSection           # Offered section with capacity, meetings, enrollments
│   ├── MeetingTime             # Day/time with conflict detection
│   ├── Semester                # Term + year + registration period + deadlines
│   ├── RegistrationPeriod      # Date range for registration windows
│   ├── AcademicRecord          # Student's enrollment history + CGPA calculation
│   ├── Enrollment              # Section enrollment with status lifecycle
│   └── Grade                   # Numeric score → letter grade conversion
├── service/                    # Application services
│   ├── RegistrationService     # Register / withdraw with full rule enforcement
│   ├── GradingService          # Record grades, retrieve rosters
│   ├── GraduationService       # Check graduation eligibility
│   ├── ReportService           # Generate transcripts
│   ├── NotificationService     # Notification interface
│   └── AppNotificationService  # In-memory notification implementation
├── repository/                 # Data access interfaces & implementations
│   ├── StudentRepository       # Find students by ID or student number
│   └── InMemoryStudentRepository # Thread-safe concurrent storage
├── dto/                        # Data Transfer Objects
│   ├── Notification            # Notification message
│   ├── GraduationReport        # Eligibility report with reasons
│   ├── Transcript              # Academic transcript
│   ├── TranscriptEntry         # Single course in a transcript
│   └── CourseSummary           # Per-semester credit/GPA summary
├── exception/                  # Custom exceptions
│   └── RegistrationException   # Business rule violation
└── ui/                         # JavaFX Material 3 UI Layer
    ├── Launcher                # Safe JavaFX application launcher
    ├── UniversityApp           # Main JavaFX shell with Top App Bar, Drawer, & Theme Engine
    ├── theme/                  # Material Design 3 Styling
    │   ├── M3Palette           # Color token generator for Light/Dark modes & seeds
    │   └── ThemeManager        # Runtime CSS injector for JavaFX Scene
    ├── context/
    │   └── UniversityDataStore # In-memory reactive state & realistic sample data
    └── views/
        ├── DashboardView       # Executive KPIs, active term timeline, recent activity
        ├── StudentsView        # Search bar, filter chips, student profile, registrar standing updates
        ├── CoursesView         # Catalog & sections schedule tabs, capacity tracker
        ├── RegistrationView    # Course registration desk with live preflight validation
        ├── GradingView         # Instructor desk, class roster, score slider & grade chips
        ├── TranscriptView      # Student academic records, CGPA & credits cards, formatted transcript
        ├── GraduationView      # Degree audit & graduation eligibility checker
        └── NotificationsView   # System notification audit log with category filter chips
```

---

## Views & Capabilities

1. **Executive Dashboard (`DashboardView`)**:
   - Elevated KPI cards for Total Enrolled Students, Offerings, Sections, and Completed Quality Credits.
   - Academic Session Overview displaying current system date, active term, and registration status badge.
   - Live activity stream showing real-time system actions.
2. **Student Directory (`StudentsView`)**:
   - M3 pill search bar and filter chips (`All Students`, `Active`, `Probation`, `Suspended`).
   - Student directory table with colored standing badges.
   - Detailed profile card with cumulative GPA, earned credits, and advisor assignment.
   - Role-gated registrar dialog to change academic standing (enforces `StaffRole.REGISTRAR`).
3. **Courses & Schedules (`CoursesView`)**:
   - Material tabs for **Course Catalog** and **Scheduled Sections**.
   - Capacity monitor chips (`Open`, `Waitlist Only`, `Full`).
   - Prerequisites breakdown and schedule conflict indicators.
4. **Registration Desk (`RegistrationView`)**:
   - Student candidate and section selector.
   - **Real-Time Preflight Validation**: Evaluates standing, capacity, prerequisite completion, schedule conflicts, and registration window before submission.
   - Registered sections table with one-click course drop.
5. **Instructor Grading (`GradingView`)**:
   - Course section selector and enrolled student class roster.
   - Interactive score slider (0–100) with automatic M3 letter grade preview chip (`A+`, `A`, `B`, `C`, `F`).
   - Grade submission with automatic CGPA recalculation.
6. **Transcripts & Records (`TranscriptView`)**:
   - Student selector with cumulative GPA card, total credits earned card, and academic standing badge.
   - Formatted transcript table categorized by semester with numeric score, letter grade, and quality points.
7. **Degree Audit & Graduation Checker (`GraduationView`)**:
   - Evaluates graduation eligibility against required curriculum courses and minimum credit thresholds.
   - Status badge (`✓ GRADUATION ELIGIBLE` or `⚠ REQUIREMENTS PENDING`).
   - Progress bar showing percentage of required degree credits completed.
   - Two-column breakdown of completed courses vs outstanding deficiencies.
8. **Notifications & Audit Log (`NotificationsView`)**:
   - Filterable chronological audit log of all system events.
   - Pill search bar and category chips (`All Events`, `Registrations`, `Grading`, `Standing Alerts`).
   - Test broadcast dispatcher and log clear functionality.

---

## How to Run

### Run the JavaFX Application

Execute the following Maven command:

```bash
mvn javafx:run
```

Or run `com.university.registration.ui.Launcher` directly from your IDE.

### Run Tests

Execute the JUnit 5 test suite:

```bash
mvn clean test
```

All 22 unit tests validate business rules, prereqs, grading, GPA calculations, withdrawals, and graduation audits.

---

## Requirements

- **Java**: JDK 17 or higher (tested on Java 17 and JDK 25)
- **Maven**: 3.8+
- **JavaFX**: 21.0.2 (managed via Maven dependencies)
