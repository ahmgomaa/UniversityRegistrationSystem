package model;

public class Instructor extends User {

    private final Department department;

    public Instructor(String userId, String name, String phoneNumber, String password,
                      Department department) {
        super(userId, name, phoneNumber, password);
        if (!userId.startsWith("IN-")) {
            throw new IllegalArgumentException("Instructor id must look like IN-101");
        }
        if (department == null) {
            throw new IllegalArgumentException("Department must not be null");
        }
        this.department = department;
    }

    public Instructor(String userId, String name, String phoneNumber, User.HashedPassword passwordHash,
                      Department department) {
        super(userId, name, phoneNumber, passwordHash);
        if (!userId.startsWith("IN-")) {
            throw new IllegalArgumentException("Instructor id must look like IN-101");
        }
        if (department == null) {
            throw new IllegalArgumentException("Department must not be null");
        }
        this.department = department;
    }

    public Department getDepartment() {
        return department;
    }

    @Override
    public String toString() {
        return "Name = " + getName() + ", Department = " + department.getCode();
    }
}
