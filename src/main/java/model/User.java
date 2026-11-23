package model;

public abstract class User {

    private final String userId;
    private String name;
    private String email;
    private String passwordHash;
    private String phoneNumber;

    public User(String userId, String name, String phoneNumber, String password) {
        this.userId = userId;
        this.name = name;
        this.email = "ASU@" + userId;
        this.phoneNumber = phoneNumber;
        checkPassword(password);
        this.passwordHash = hash(password);
    }

    protected User(String userId, String name, String phoneNumber, HashedPassword passwordHash) {
        this.userId = userId;
        this.name = name;
        this.email = "ASU@" + userId;
        this.phoneNumber = phoneNumber;
        this.passwordHash = passwordHash.value();
    }

    public static final class HashedPassword {

        private final String value;

        private HashedPassword(String value) {
            if (value == null || value.trim().isEmpty()) {
                throw new IllegalArgumentException("Password hash must not be empty");
            }
            this.value = value.trim();
        }

        public static HashedPassword of(String storedHash) {
            return new HashedPassword(storedHash);
        }

        public String value() {
            return value;
        }
    }

    private static void checkPassword(String password) {
        if (password == null || password.length() != 4) {
            throw new IllegalArgumentException("Password must be exactly 4 digits");
        }
        for (int i = 0; i < password.length(); i++) {
            if (!Character.isDigit(password.charAt(i))) {
                throw new IllegalArgumentException("Password must be exactly 4 digits");
            }
        }
    }

    public static String hash(String password) {
        checkPassword(password);
        return "" + (Integer.parseInt(password) * 7 + 3);
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name must not be empty");
        }
        this.name = name.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean verifyPassword(String password) {
        if (password == null) {
            return false;
        }
        return passwordHash.equals(hash(password));
    }

    public void changePassword(String oldPassword, String newPassword) {
        if (!verifyPassword(oldPassword)) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        checkPassword(newPassword);
        this.passwordHash = hash(newPassword);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof User) {
            User other = (User) obj;
            return this.userId.equals(other.userId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return userId.hashCode();
    }

    @Override
    public String toString() {
        return "[id = " + userId + " , name = " + name + "]";
    }
}
