package com.university.registration.service;

import java.util.List;

/**
 * Thrown by the registration flow. Carries <em>all</em> validation errors so
 * the caller can show them at once instead of one at a time.
 */
public class RegistrationException extends RuntimeException {

    private final List<String> errors;

    public RegistrationException(List<String> errors) {
        super(String.join("; ", errors));
        this.errors = List.copyOf(errors);
    }

    public List<String> getErrors() {
        return errors;
    }
}
