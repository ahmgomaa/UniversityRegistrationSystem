# University Registration System

A Java console application that manages university course registration — students register in course sections, instructors record grades, and academic staff manage students, all backed by a MySQL database.

Built for COMP 301 (Advanced Programming) at Ain Shams University.

## What it does

- **Browse sections** — anyone can view the offered course sections for each semester, without signing in.
- **Students** — sign in, register in course sections, withdraw from active enrollments, view the transcript and graduation eligibility, read notifications, and change the password.
- **Instructors** — view their sections, display a section roster, and record grades.
- **Academic staff** — view all students, update a student's academic status (registrar), assign an advisor (advisor), and display any section roster (registrar).

### Registration rules

A registration is accepted only if all of these hold:

1. The student's academic status allows registration (ACTIVE or PROBATION).
2. The registration window is open on the current date.
3. The section has a free seat.
4. No timetable conflict with an already active enrollment.
5. The same course is not taken twice in the same semester.
6. Every prerequisite of the course has been passed.
7. The projected credits for the semester do not exceed the program limit (a lower limit applies on probation).

If any rule fails, the system reports every failed rule at once.

### Grading and graduation

- Recording a grade completes the enrollment and recomputes the GPA and CGPA.
- Academic standing updates automatically: a CGPA below the program threshold puts an ACTIVE student on probation, and reaching the threshold restores ACTIVE status.
- The graduation audit checks earned credits, CGPA, all required courses passed, and elective credits, and reports every unmet requirement.

## How it is built

Layered architecture — each layer depends only on the layers below it.

| Layer | Package / class | Responsibility |
| --- | --- | --- |
| Bootstrap | `Main` | Connects to MySQL, creates the schema when needed, loads the data, wires the objects, and starts the console. |
| Presentation | `ConsoleUI` | Menus, input, and output. |
| Services | `service` | Registration, withdrawal, grading, graduation audit, transcripts, notifications. |
| Domain model | `model`, `model.enums` | Students, courses, sections, enrollments, grades, and their validation and calculations. |
| Data access | `data`, `db` | Loads the object graph from MySQL and writes changes back; creates the schema on first run. |
| Ports | `port` | The `NotificationService` and `StudentRepository` interfaces the services depend on. |

The full design — class structure, relationships, the 13-table database schema, and the object-oriented concepts applied — is in [`docs/Project_Report.md`](docs/Project_Report.md). The class diagram is in [`UML-diagram.pdf`](UML-diagram.pdf).

## Requirements

- Java
- MySQL, running locally
- MySQL Connector/J on the classpath (JDBC driver)

## Setup

1. Start MySQL.
2. Edit `src/main/resources/db.properties` with your connection settings:

   ```properties
   db.url=jdbc:mysql://localhost:3306/university_registration
   db.user=root
   db.password=
   ```

   Environment variables can override these settings — see `DatabaseConnection`.
3. On the first run the application creates the database if it is missing, runs `schema.sql`, and loads the sample data from `demo_data.sql`.

## Run

From the repository root, compile the sources and run `Main`, with the MySQL Connector/J jar and the resources on the classpath:

```bash
javac -cp mysql-connector-j.jar -d out $(find src/main/java -name "*.java")
cp -r src/main/resources/* out/
java -cp out:mysql-connector-j.jar Main
```

Replace `mysql-connector-j.jar` with the path to your Connector/J jar.

## Project structure

```text
src/main/java/
├── Main.java            # Entry point
├── ConsoleUI.java       # Console menus for all four user types
├── data/                # DataStore — loads/saves the object graph, implements StudentRepository
├── db/                  # DatabaseConnection, SchemaInitializer
├── model/               # User, Student, Instructor, AcademicStaff, Course, CourseSection, ...
├── model/enums/         # AcademicStatus, EnrollmentStatus, Term, StaffRole, LetterGrade
├── port/                # NotificationService, StudentRepository
└── service/             # RegistrationService, GradingService, GraduationService, ReportService, ...
src/main/resources/
├── db.properties        # Database connection settings
├── schema.sql           # Database schema (13 tables)
└── demo_data.sql        # Sample data loaded on first run
docs/
└── Project_Report.md    # Full project report
UML-diagram.pdf          # Class diagram
```
