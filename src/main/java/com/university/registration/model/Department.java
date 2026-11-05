package com.university.registration.model;

import java.util.Objects;

/** Academic department (e.g. Computer Science). */
public class Department {

    private final String code;
    private final String name;

    public Department(String code, String name) {
        this.code = Objects.requireNonNull(code, "code");
        this.name = Objects.requireNonNull(name, "name");
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
        return code.equals(that.code);
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }

    @Override
    public String toString() {
        return "%s - %s".formatted(code, name);
    }
}
