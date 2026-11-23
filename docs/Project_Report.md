# University Student Registration System — Project Report

## 1. Introduction

This report describes the structure and the object-oriented concepts of the University Student Registration System. The system is a Java console application that manages the academic registration process of a university: it stores the course catalog and the offered sections, registers students in sections according to a set of academic rules, records grades, computes the GPA and the CGPA of every student, checks graduation eligibility, and keeps in-app notifications for every user.

The data of the system is stored in a MySQL relational database. The application is organized in layers, so that the user interface, the business logic, the domain classes, and the data access code are clearly separated from each other. The report presents the architecture, the class structure, the relationships between the classes, the database design, and the object-oriented concepts that the implementation applies.

## 2. System Overview

The system serves four kinds of users. A visitor can browse the course sections of every semester without signing in. A student signs in with an identifier and a password, registers in course sections, withdraws from active enrollments, views the transcript and the graduation eligibility, reads the notifications, and changes the password. An instructor views the sections assigned to them, displays the roster of each section, and records the grades of the enrolled students. A member of the academic staff views all students, updates the academic status of a student (registrar role), assigns an advisor to a student (advisor role), and displays the roster of any section (registrar role).

Every user account has a unique identifier, a name, an e-mail address, a phone number, and a four-digit password. The password is not stored as plain text; the class `User` applies a fixed arithmetic transformation to the digits and stores the result, and the method `verifyPassword` compares the stored value with the transformation of the entered password.

## 3. System Architecture

The application follows a layered architecture. Each layer depends only on the layers below it, which keeps the classes independent and easy to change. Table 1 lists the layers and their responsibility.

**Table 1. Layers of the system**

| Layer | Package / Class | Responsibility |
| --- | --- | --- |
| Bootstrap | `Main` | Connects to MySQL, creates the schema when needed, loads the data, wires the objects, and starts the user interface. |
| Presentation | `ConsoleUI` | Displays the menus, reads the user input, and prints the results and the error messages. |
| Application services | `service` | Implements the use cases: registration, withdrawal, grading, graduation audit, transcripts, and notifications. |
| Domain model | `model` | Holds the academic concepts, their state, their validation rules, and their calculations. |
| Data access | `data`, `db` | Reads the object graph from MySQL and writes the changes back; creates the schema on the first run. |
| Ports | `port` | Declares the interfaces `NotificationService` and `StudentRepository` that the services depend on. |

The classes are connected manually in `Main` (manual dependency injection): one `DataStore` object is created first, then the services receive it through their constructors, and finally the `ConsoleUI` receives the store and the services. Because the services depend on the interfaces of the `port` package and not on concrete classes, the implementation of the data access can be replaced without changing the service layer.

## 4. Project Structure

The source code is organized in six packages. Tables 2 to 5 list the classes of every package and the responsibility of each one.

**Table 2. Package `model` — the domain classes**

| Class | Responsibility |
| --- | --- |
| `User` (abstract) | Base class of every person: identifier, name, e-mail, phone, password verification and change. |
| `Student` | Adds the student number, admission year, academic status, program, advisor, and the owned `AcademicRecord`. |
| `Instructor` | Adds the department of the teacher. |
| `AcademicStaff` | Adds the department and the list of roles (ADVISOR, REGISTRAR) with the method `hasRole`. |
| `Department` | Academic department identified by a code. |
| `Program` | Credit rules of a degree: required and elective courses, credit limits, CGPA thresholds, number of levels. |
| `Course` | Catalog course with code, name, credit hours, department, and prerequisites. |
| `CourseSection` | An offering of a course in a semester, taught by one instructor, with meetings and a capacity. |
| `MeetingTime` | Day, start time, end time and room of one meeting; detects time conflicts. |
| `Semester` | Term and year with the start and end dates, the withdrawal deadline, and the registration period. |
| `RegistrationPeriod` | The date window in which registration is open; method `isOpen`. |
| `AcademicRecord` | The enrollment history of one student; computes the GPA, the CGPA, and the earned credits. |
| `Enrollment` | Links a student to a section; controls its own lifecycle (enrolled, withdrawn, completed). |
| `Grade` | Validates a score between 0 and 100 and derives the letter grade and the grade points. |
| `Notification` | One stored message with its recipient and its timestamp. |

**Table 3. Package `model.enums` — the state enumerations**

| Enum | Values |
| --- | --- |
| `AcademicStatus` | ACTIVE, PROBATION, SUSPENDED, GRADUATED; method `canRegister` allows the first two. |
| `EnrollmentStatus` | ENROLLED, WITHDRAWN, COMPLETED. |
| `Term` | SPRING, SUMMER, FALL; the declaration order is the calendar order. |
| `StaffRole` | ADVISOR, REGISTRAR. |
| `LetterGrade` | A, B_PLUS, B, C_PLUS, C, D_PLUS, D, F; each value stores its grade points and minimum score, and the static method `fromScore` maps a score to the letter. |

**Table 4. Packages `service`, `port`, `data` and `db`**

| Class | Responsibility |
| --- | --- |
| `RegistrationService` | Checks the seven registration rules and the withdrawal conditions; throws `RegistrationException` with all the reasons. |
| `RegistrationException` | Custom checked exception that carries the list of rule errors. |
| `GradingService` | Records grades, updates the academic standing automatically, and builds section rosters. |
| `GraduationService` | Runs the graduation audit and reports every unmet reason. |
| `ReportService` | Builds the transcript: one summary per semester plus the CGPA and the earned credits. |
| `InAppNotificationService` | Implements `NotificationService`; stores the messages of every user. |
| `NotificationService` (interface) | Declares `notify(user, message)`; the services depend on this interface. |
| `StudentRepository` (interface) | Declares `findAll`, `findById` and `save(Student)`; the services depend on this interface. |
| `DataStore` | The single data access class: loads the object graph from MySQL and writes the changes back; implements `StudentRepository`. |
| `DatabaseConnection` | Reads the connection settings and provides JDBC connections; creates the database when it does not exist. |
| `SchemaInitializer` | Checks whether the tables exist and executes `schema.sql` and the sample data script when they do not. |

**Table 5. Root package**

| Class | Responsibility |
| --- | --- |
| `Main` | Entry point: connects to MySQL, ensures the schema, loads the data, wires the objects, and starts the console. |
| `ConsoleUI` | Sign-in flow and the three role portals; all the input and the output of the application. |

## 5. Domain Model and Class Relationships

### 5.1 Inheritance Hierarchy

The class `User` is the abstract generalization of every person in the system. `Student`, `Instructor` and `AcademicStaff` extend `User` and inherit the identity, the password handling, and the `toString`, `equals` and `hashCode` implementations. Each subclass adds only the attributes that are specific to it, which avoids repeating the common code three times.

### 5.2 Relationships Between the Classes

**Table 6. Relationships and their implementation**

| Relationship | Between | Implementation |
| --- | --- | --- |
| Inheritance (IS-A) | `Student`, `Instructor`, `AcademicStaff` — `User` | `extends`; the subclass constructors call `super` to initialize the inherited part. |
| Composition (HAS-A) | `Student` — `AcademicRecord` | The record is created inside the `Student` constructor and cannot exist without the student. |
| Composition (HAS-A) | `CourseSection` — `MeetingTime` | The section creates its list of meetings and owns it. |
| Aggregation (HAS-A) | `Student` — `AcademicStaff` (advisor) | The advisor is a reference to a staff object that exists independently in the store. |
| Aggregation (HAS-A) | `Course` — `Course` (prerequisites) | A course keeps references to other catalog courses. |
| Dependency (USES-A) | `RegistrationService` — `CourseSection`, `LocalDate` | The section and the date are method parameters only. |
| Realization | `InAppNotificationService` — `NotificationService` | The class implements the interface. |
| Realization | `DataStore` — `StudentRepository` | The class implements the interface. |

### 5.3 Object Identity and Ordering

Every entity class overrides `equals`, `hashCode` and `toString`. Two objects are equal when they represent the same real entity: two users are equal when their identifiers match, two courses when their codes match, and two enrollments when they point to the same section. These contracts make the collection operations correct: `AcademicRecord.contains`, `Program.isRequired` and the duplicate checks all rely on `equals`. The class `Semester` implements the `Comparable` interface; `compareTo` orders the semesters by year and then by the declaration order of the term, which allows `Collections.sort` to produce the chronological order of the transcript.

## 6. Object-Oriented Concepts Applied

### 6.1 Encapsulation

All attributes of the domain classes are private and are reached through public getters. The setters validate the value before assigning it, for example `setName` refuses an empty name. More important, the classes protect their own state: `Enrollment.withdraw` and `Enrollment.complete` refuse to run when the enrollment is not in the ENROLLED state, `Grade` refuses a score outside the range 0 to 100, and `Student.updateAcademicStatus` refuses a staff member without the REGISTRAR role. Invalid objects cannot exist and invalid transitions cannot happen from outside the class.

### 6.2 Inheritance

The three user types extend the abstract class `User`. The subclass constructors call `super` as their first statement to initialize the inherited attributes. The password hashing, the verification, the e-mail generation, and the equality contract are written once in the superclass and reused by every subclass.

### 6.3 Polymorphism

Run-time polymorphism appears in two places. First, the variable that holds the signed-in user is declared as `User` while the actual object is a `Student`, an `Instructor` or an `AcademicStaff`; calling `getName` or `toString` on it executes the behavior of the actual object (dynamic method dispatch). Second, the `ConsoleUI` uses the `instanceof` operator to test the actual type and casts the reference to the concrete class before opening the corresponding portal. Compile-time polymorphism appears as method overloading: `GradingService` declares two versions of `getRoster`, one that accepts an `Instructor` and one that accepts an `AcademicStaff`, and the compiler selects the version from the argument type. Each version applies the permission rule of its caller.

### 6.4 Abstraction

Abstraction is achieved at two levels. The abstract class `User` defines what every person of the system is without defining a concrete kind of person; it cannot be instantiated, but a variable of type `User` can hold any subclass object. The interfaces `NotificationService` and `StudentRepository` define what the services need (sending a message, finding and saving students) without revealing how the operation is performed; the `ConsoleUI` and the services depend only on these declarations.

### 6.5 Static Members and Constants

Static members belong to the class and not to an object. The project uses them for shared constants and utilities: `Grade.PASS_MARK` is a public static final constant, `User.hash` is a static utility method, and the entry point `Main.main` is static because the JVM calls it before any object exists. Static methods access only static members, as the language requires.

### 6.6 Static Nested Classes

The result objects of the services are declared as static nested classes inside the service that produces them: `GradingService.RosterEntry`, `GraduationService.GraduationReport`, `ReportService.Transcript` and `ReportService.SemesterSummary`. A nested class keeps the result type close to the code that creates it and does not need a reference to the outer class.

### 6.7 Collections and Generics

Every one-to-many relationship is stored in a generic `ArrayList`: the enrollments of a record, the meetings of a section, the prerequisites of a course, the required and elective courses of a program, the roles of a staff member, and all the lists of the data store. The generic type parameter guarantees that only the declared type enters the list, so the elements are retrieved without casting. The iteration uses the enhanced for statement, and the duplicate detection uses the `contains` method, which relies on the overridden `equals`.

### 6.8 String Processing

The identifier rules are implemented with the methods of the `String` class: `startsWith` verifies the prefix of every identifier (CS-, IN-, AS-), `trim` and `isEmpty` validate the user input and the constructor arguments, `equalsIgnoreCase` matches the section identifiers typed by the user, `toUpperCase` normalizes codes and status names, `replace` produces the display form of the letter grades (B_PLUS becomes B+), and `charAt` with `Character.isDigit` verifies that the password is exactly four digits.

### 6.9 Exception Handling

The system separates checked from unchecked exceptions. The checked custom exception `RegistrationException` extends `Exception` and carries every rule error of one registration attempt; `register` and `withdraw` declare it with `throws`, and the user interface must handle it in a try-catch block, where it prints the complete list of reasons. The unchecked exceptions of the standard library report programming errors and bad input: `IllegalArgumentException` for invalid constructor or method arguments (raised by the model validation), `IllegalStateException` for an invalid enrollment transition, and `NumberFormatException` when the entered score is not an integer.

The `ConsoleUI` uses several catch blocks in order of specificity: `NumberFormatException` is caught before `IllegalArgumentException` because it is a subclass of it. By catching `IllegalArgumentException` the interface also catches every subclass thrown deep inside the model validation. The data layer uses try-with-resources for every `Statement`, `ResultSet` and stream, so the resources close automatically even when an exception is thrown.

**Table 7. Exceptions of the system**

| Exception | Kind | Thrown by | Handled by |
| --- | --- | --- | --- |
| `RegistrationException` | Checked (custom) | `RegistrationService` | `ConsoleUI` (try-catch) |
| `IOException`, `FileNotFoundException` | Checked | `SchemaInitializer`, `DatabaseConnection` | `Main` and `DataStore` |
| `SQLException` | Checked | All JDBC operations | `DataStore` and `Main` |
| `IllegalArgumentException` | Unchecked | Model validation | `ConsoleUI` |
| `IllegalStateException` | Unchecked | `Enrollment` transitions | `ConsoleUI` |
| `NumberFormatException` | Unchecked | `Integer.parseInt` of the score | `ConsoleUI` |

### 6.10 Database Connectivity

The data layer talks to MySQL through JDBC. `DatabaseConnection` reads the settings from `db.properties` (with optional environment-variable overrides), builds the connection, and creates the database with `CREATE DATABASE IF NOT EXISTS` when the first connection attempt reports that it is missing. `SchemaInitializer` asks the database metadata whether the students table exists and, when it does not, splits `schema.sql` into statements and executes them one by one. Every query uses a `PreparedStatement` with parameters instead of string concatenation, and every `Statement` and `ResultSet` is opened inside try-with-resources. `DataStore` loads the complete object graph once at startup and writes the changes back after every modifying operation, which keeps the services free of any database code.

## 7. Database Design

The database `university_registration` contains thirteen tables. The `users` table stores the shared identity columns of the three user types together with a type column; the `students` and `staff_members` tables hold the type-specific columns and reference `users` by the primary key. Table 8 lists the tables and their purpose.

**Table 8. Tables of the database**

| Table | Purpose |
| --- | --- |
| `departments` | Department code and name. |
| `users` | Identifier, name, e-mail, password hash, phone and type of every user. |
| `students` | Student number, admission year, academic status, program and advisor of a student. |
| `staff_members` | Department and role list of every instructor and academic staff member. |
| `programs` | Credit and CGPA rules of every degree program. |
| `courses` | Course catalog: code, name, credit hours, department. |
| `course_prerequisites` | Prerequisite relation between two courses. |
| `program_courses` | Membership of a course in a program as REQUIRED or ELECTIVE. |
| `semesters` | Term, year, start and end dates, withdrawal deadline and registration window. |
| `course_sections` | Offering of a course: instructor, semester, section number and capacity. |
| `meeting_times` | Day, time range and room of every meeting of a section. |
| `enrollments` | Student-section relation with status, score and dates; unique per student and section. |
| `notifications` | Recipient, message and timestamp of every stored notification. |

Foreign keys connect the tables (sections to courses, instructors and semesters; enrollments to users and sections; notifications to users), a unique constraint protects the e-mail, the student number and the student-section pair, and the enumeration columns mirror the Java enums. The pair (user_id, section_id) in `enrollments` is unique, which expresses at the database level the rule that a student can hold only one enrollment per section.

## 8. Functional Description

### 8.1 Registration

`RegistrationService.register` evaluates seven rules before creating the enrollment: the academic status must allow registration, the registration window must be open on the current date, the section must have a free seat, no timetable conflict may exist with an already active enrollment, the same course must not be taken twice in the same semester, every prerequisite of the course must have been passed before, and the projected credits of the semester must not exceed the limit of the program (a lower limit applies to students on probation). Every failed rule is collected in a list; when the list is not empty the service throws `RegistrationException` and the interface prints all the reasons.

### 8.2 Withdrawal

A student can withdraw only from an enrollment that belongs to them and is still active, and only between the start date of the semester and its withdrawal deadline. On success the status becomes WITHDRAWN, the withdrawal date is stored, and a notification is created.

### 8.3 Grading and Academic Standing

An instructor records a score only for a section they teach and only for an enrollment that belongs to the named student. Recording the grade completes the enrollment, stores a notification, and recomputes the CGPA. When the CGPA of an ACTIVE student falls below the probation threshold of the program, the status becomes PROBATION; when a PROBATION student reaches the threshold, the status becomes ACTIVE. SUSPENDED and GRADUATED students are never changed automatically. A registrar can set any status explicitly through `updateAcademicStatus`.

### 8.4 Graduation Audit

The audit checks four conditions: the earned credits must reach the program total, the CGPA must reach the program minimum, every required course must have been passed, and the passed elective credits must reach the elective requirement. The result reports the decision and every unmet reason. A student already marked GRADUATED is reported as eligible immediately.

### 8.5 Notifications

Registration, withdrawal, grading and standing changes create a notification for the student. The notifications are stored with the recipient, the message and the timestamp, and the student portal lists the inbox of the signed-in student.

## 9. Conclusion

The University Student Registration System implements a complete academic registration domain in a layered Java application backed by MySQL. The design separates the presentation, the business services, the domain classes and the data access, and connects them through two small interfaces. The domain classes carry their own validation and calculation rules, the services implement the use cases and report every violated rule, and the relational schema preserves the identity and the relationships of the entities. The implementation applies the principal object-oriented concepts — encapsulation, inheritance, polymorphism, abstraction, generic collections, string processing, structured exception handling and relational persistence — in a form that remains small, readable and open to extension, such as a graphical user interface on top of the existing service layer.
