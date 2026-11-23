package model;

public class Department {

    private final String code;
    private final String name;

    public Department(String code, String name) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Department code must not be empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Department name must not be empty");
        }
        this.code = code.trim().toUpperCase();
        this.name = name.trim();
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Department) {
            Department other = (Department) obj;
            return this.code.equals(other.code);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }

    @Override
    public String toString() {
        return code + " - " + name;
    }
}
