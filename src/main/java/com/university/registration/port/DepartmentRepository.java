package com.university.registration.port;

import com.university.registration.model.Department;

import java.util.List;
import java.util.Optional;

/** Persistence port for departments. */
public interface DepartmentRepository {

    List<Department> findAll();

    Optional<Department> findById(String deptCode);
}
