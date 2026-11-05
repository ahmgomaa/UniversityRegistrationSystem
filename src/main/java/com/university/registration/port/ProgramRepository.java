package com.university.registration.port;

import com.university.registration.model.Program;

import java.util.List;
import java.util.Optional;

/** Persistence port for degree programs. */
public interface ProgramRepository {

    List<Program> findAll();

    Optional<Program> findById(int programId);
}
