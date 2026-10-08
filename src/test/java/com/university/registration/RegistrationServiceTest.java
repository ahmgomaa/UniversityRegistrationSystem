package com.university.registration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RegistrationServiceTest {
    private RegistrationService service;

    @BeforeEach
    void setUp() {
        service = new RegistrationService();

        service.addCourse(new Course("CS101", 2, Set.of()));
        service.addCourse(new Course("CS201", 1, Set.of("CS101")));

        service.addStudent(new Student("s1", Set.of("CS101")));
        service.addStudent(new Student("s2", Set.of()));
        service.addStudent(new Student("s3", Set.of("CS101")));
    }

    @Test
    void registerSucceedsWhenStudentMeetsRequirements() {
        RegistrationResult result = service.register("s1", "CS201");

        assertEquals(RegistrationResult.SUCCESS, result);
        assertTrue(service.isRegistered("s1", "CS201"));
        assertEquals(1, service.getEnrolledCount("CS201"));
    }

    @Test
    void registerFailsWhenPrerequisiteMissing() {
        RegistrationResult result = service.register("s2", "CS201");

        assertEquals(RegistrationResult.MISSING_PREREQUISITES, result);
        assertFalse(service.isRegistered("s2", "CS201"));
    }

    @Test
    void registerFailsWhenCourseIsFull() {
        assertEquals(RegistrationResult.SUCCESS, service.register("s1", "CS201"));

        RegistrationResult secondAttempt = service.register("s3", "CS201");

        assertEquals(RegistrationResult.COURSE_FULL, secondAttempt);
        assertFalse(service.isRegistered("s3", "CS201"));
    }

    @Test
    void registerFailsWhenAlreadyRegistered() {
        assertEquals(RegistrationResult.SUCCESS, service.register("s1", "CS101"));

        RegistrationResult secondAttempt = service.register("s1", "CS101");

        assertEquals(RegistrationResult.ALREADY_REGISTERED, secondAttempt);
    }

    @Test
    void dropRemovesEnrollmentAndFreesSeat() {
        assertEquals(RegistrationResult.SUCCESS, service.register("s1", "CS201"));
        assertTrue(service.drop("s1", "CS201"));

        RegistrationResult newEnrollment = service.register("s3", "CS201");

        assertEquals(RegistrationResult.SUCCESS, newEnrollment);
        assertTrue(service.isRegistered("s3", "CS201"));
        assertEquals(1, service.getEnrolledCount("CS201"));
    }
}
