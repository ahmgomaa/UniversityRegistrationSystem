package com.university.registration.port;

import com.university.registration.model.AcademicStaff;
import com.university.registration.model.Instructor;

import java.util.List;
import java.util.Optional;

/** Persistence port for teaching and academic staff. */
public interface StaffRepository {

    List<Instructor> findAllInstructors();

    List<AcademicStaff> findAllStaff();

    Optional<Instructor> findInstructorById(String userId);

    Optional<AcademicStaff> findStaffById(String userId);
}
