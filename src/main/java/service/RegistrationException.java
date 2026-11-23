package service;

import java.util.ArrayList;

public class RegistrationException extends Exception {

    private static final long serialVersionUID = 1L;

    private final ArrayList<String> errors;

    public RegistrationException() {
        super("Registration failed");
        this.errors = new ArrayList<String>();
    }

    public RegistrationException(ArrayList<String> errors) {
        super(join(errors));
        this.errors = errors;
    }

    public ArrayList<String> getErrors() {
        return errors;
    }

    private static String join(ArrayList<String> errors) {
        String text = "";
        for (int i = 0; i < errors.size(); i++) {
            if (i > 0) {
                text += "; ";
            }
            text += errors.get(i);
        }
        return text;
    }
}
