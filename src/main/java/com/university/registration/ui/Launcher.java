package com.university.registration.ui;

/**
 * Main application launcher class.
 * Starting the application from a class that does not extend Application
 * avoids JavaFX module-path runtime initialization issues.
 */
public class Launcher {

    public static void main(String[] args) {
        UniversityApp.main(args);
    }
}
