package com.university.registration.model;

import java.util.Objects;

/**
 * Represents an academic department within the university.
 */
public class Department {

    private final String code;
    private final String name;

    public Department(String code, String name) {
        this.code = Objects.requireNonNull(code, "code must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Department that)) return false;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return "Department{code='" + code + "', name='" + name + "'}";
    }
}
