# University Registration System

A minimal Java-based university registration system that models students, courses, and enrollment rules.

## Features

- Add students and courses
- Register students in courses
- Validate prerequisite completion
- Enforce course capacity
- Prevent duplicate registration
- Drop enrolled students from courses

## Project structure

- `src/main/java/com/university/registration`
  - `Student` – student id and completed courses
  - `Course` – course code, capacity, prerequisites
  - `RegistrationService` – registration/drop logic
  - `RegistrationResult` – registration outcomes
- `src/test/java/com/university/registration`
  - `RegistrationServiceTest` – focused unit tests

## Build and test

```bash
mvn test
```
