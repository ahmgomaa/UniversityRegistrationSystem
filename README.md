# University Registration System

A comprehensive Java-based university registration system that models students, courses, enrollments, grading, graduation, and academic reports following clean architecture principles.

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
│   ├── CourseSection    # Offered section with capacity, meetings, enrollments
│   ├── MeetingTime     # Day/time with conflict detection
│   ├── Semester         # Term + year + registration period + deadlines
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
├── repository/         # Data access interfaces
│   └── StudentRepository       # Find students by ID or student number
├── dto/                # Data Transfer Objects
│   ├── Notification            # Notification message
│   ├── GraduationReport        # Eligibility report with reasons
│   ├── Transcript              # Academic transcript
│   ├── TranscriptEntry         # Single course in a transcript
│   └── CourseSummary           # Per-semester credit/GPA summary
└── exception/          # Custom exceptions
    └── RegistrationException   # Business rule violation
```

## Features

### Registration
- Register students in course sections with full validation
- Academic standing check (ACTIVE/PROBATION can register; SUSPENDED/DISMISSED cannot)
- Registration period enforcement
- Seat availability tracking
- Prerequisite verification
- Duplicate enrollment prevention
- Withdraw from courses before deadline

### Grading
- Instructor-gated grade recording
- Score-to-letter-grade conversion (A+ through F)
- Class roster retrieval

### Academic Standing
- Role-based authorization (only registrars can update standing)
- Automatic standing calculation based on CGPA vs program thresholds
- Advisor assignment (only staff with ADVISOR role)

### Graduation
- Eligibility check against program requirements
- Unfulfilled reasons report

### Reporting
- Academic transcript generation
- Cumulative GPA and credit tracking

### Scheduling
- Meeting time conflict detection
- Per-section meeting schedules

## Build and test

```bash
mvn clean test
```

## Requirements

- Java 17+
- Maven 3.8+
