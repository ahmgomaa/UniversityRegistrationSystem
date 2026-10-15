# University Registration System

A comprehensive Java & JavaFX-based university registration and academic management system that models students, courses, enrollments, grading, graduation audits, and academic transcripts following clean architecture principles.

## Architecture

The system follows a layered architecture derived from the UML class diagram:

```
com.university.registration
├── model/              # Domain entities
│   ├── enums/          # Enumerations (Term, AcademicStatus, EnrollmentStatus, LetterGrade, StaffRole)
│   ├── User            # Abstract base class for all users
│   ├── Student         # Student with academic record, program, standing
│   ├── Instructor      # Course section instructor
│   ├── AcademicStaff   # Advisor / Registrar with role-based authorization
│   ├── Department      # Academic department
│   ├── Program         # Degree program with requirements
│   ├── Course          # Course with prerequisites (self-referencing)
│   ├── CourseSection   # Offered section with capacity, meetings, enrollments
│   ├── MeetingTime     # Day/time with conflict detection
│   ├── Semester        # Term + year + registration period + deadlines
│   ├── RegistrationPeriod # Date range for registration windows
│   ├── AcademicRecord  # Student's enrollment history + CGPA calculation
│   ├── Enrollment      # Section enrollment with status lifecycle
│   └── Grade           # Numeric score → letter grade conversion
├── service/            # Application services
│   ├── RegistrationService     # Register / withdraw with full rule enforcement
│   ├── GradingService          # Record grades, retrieve rosters
│   ├── GraduationService       # Check graduation eligibility
│   ├── ReportService           # Generate transcripts
│   ├── NotificationService     # Notification interface
│   └── AppNotificationService  # In-memory notification implementation
├── repository/         # Data access interfaces & implementations
│   ├── StudentRepository       # Find students by ID or student number
│   └── InMemoryStudentRepository # Thread-safe concurrent storage
├── dto/                # Data Transfer Objects
│   ├── Notification            # Notification message
│   ├── GraduationReport        # Eligibility report with reasons
│   ├── Transcript              # Academic transcript
│   ├── TranscriptEntry         # Single course in a transcript
│   └── CourseSummary           # Per-semester credit/GPA summary
├── exception/          # Custom exceptions
│   └── RegistrationException   # Business rule violation
└── ui/                 # JavaFX UI Layer
    ├── Launcher                # Safe JavaFX application launcher
    ├── UniversityApp           # Main JavaFX shell with sidebar & theme
    ├── context/
    │   └── UniversityDataStore # In-memory reactive state & realistic sample data
    └── views/
        ├── DashboardView       # Overview KPIs, active term, notification feed
        ├── StudentsView        # Student directory, search, registrar standing updates
        ├── CoursesView         # Catalog & sections schedule, capacity monitor
        ├── RegistrationView    # Course registration desk with live preflight validation
        ├── GradingView         # Instructor desk, class roster, letter grade calculation
        ├── TranscriptView      # Student academic records & formatted transcript
        ├── GraduationView      # Degree audit & graduation eligibility checker
        └── NotificationsView   # System notification audit log
```

## Features

### JavaFX Desktop Interface
- **Dark Aesthetic Theme**: Sleek styling with custom cards, badges, and tables.
- **Simulated Date Widget**: Change the simulated system date to test registration windows, class start dates, and withdrawal deadlines interactively.
- **Pre-populated Sample Data**: Preloaded with realistic departments, programs, courses with prerequisites, instructors, academic staff, and students with graded histories.
- **Real-Time Preflight Validation**: The registration desk checks standing, capacity, prerequisite completion, and open windows before registering.

### Core Business Rules
- **Registration**: Enforces academic standing (`ACTIVE` / `PROBATION` vs `SUSPENDED`), registration windows, section capacity, prerequisite completion, and prevents duplicate enrollments.
- **Withdrawal**: Enforces semester withdrawal deadlines.
- **Grading**: Instructor-gated grade submission with automatic score-to-letter grade conversion (A+ down to F) and cumulative GPA recalculation.
- **Standing Management**: Role-based access control (only staff with `REGISTRAR` role can update student standing).
- **Graduation Audit**: Validates completion of all required courses and minimum degree credit thresholds.

## How to Run

### Run the JavaFX Application

Execute the following command in the project root:

```bash
mvn javafx:run
```

Or run `com.university.registration.ui.Launcher` directly from your IDE.

### Run Tests

Execute the unit test suite:

```bash
mvn clean test
```

## Requirements

- Java 17+ (JDK 17 or higher)
- Maven 3.8+
