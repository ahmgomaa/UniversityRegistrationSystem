package com.university.registration.ui.theme;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

/**
 * Manages runtime Material Design 3 theme switching (Light / Dark mode and Seed Colors).
 * Injects dynamic theme CSS variables so all controls update live.
 */
public class ThemeManager {

    private static ThemeManager instance;

    private final BooleanProperty darkMode = new SimpleBooleanProperty(true);
    private final ObjectProperty<M3Palette.SeedColor> seedColor =
            new SimpleObjectProperty<>(M3Palette.SeedColor.UNIVERSITY_BLUE);

    private File tempThemeFile;

    private ThemeManager() {}

    public static synchronized ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    public boolean isDarkMode() {
        return darkMode.get();
    }

    public BooleanProperty darkModeProperty() {
        return darkMode;
    }

    public void setDarkMode(boolean dark) {
        darkMode.set(dark);
    }

    public M3Palette.SeedColor getSeedColor() {
        return seedColor.get();
    }

    public ObjectProperty<M3Palette.SeedColor> seedColorProperty() {
        return seedColor;
    }

    public void setSeedColor(M3Palette.SeedColor color) {
        seedColor.set(color);
    }

    public void toggleDarkMode() {
        setDarkMode(!isDarkMode());
    }

    /**
     * Applies the current M3 theme to the given JavaFX Scene.
     *
     * @param scene the active Scene
     */
    public void applyTheme(Scene scene) {
        Objects.requireNonNull(scene, "scene must not be null");

        try {
            String css = M3Palette.generateCss(getSeedColor(), isDarkMode());
            if (tempThemeFile == null) {
                tempThemeFile = File.createTempFile("m3fx_theme_", ".css");
                tempThemeFile.deleteOnExit();
            }
            Files.writeString(tempThemeFile.toPath(), css);

            String themeUri = tempThemeFile.toURI().toURL().toExternalForm();

            // Replace or add theme stylesheet at the beginning
            scene.getStylesheets().removeIf(url -> url.contains("m3fx_theme_"));
            scene.getStylesheets().add(0, themeUri);

            // Ensure base m3fx.css is loaded
            String m3BaseCss = getClass().getResource("/styles/m3fx.css") != null
                    ? getClass().getResource("/styles/m3fx.css").toExternalForm()
                    : null;
            if (m3BaseCss != null && !scene.getStylesheets().contains(m3BaseCss)) {
                scene.getStylesheets().add(m3BaseCss);
            }
        } catch (IOException e) {
            System.err.println("Failed to write dynamic M3 theme CSS: " + e.getMessage());
        }
    }
}
