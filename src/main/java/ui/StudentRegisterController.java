package ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import model.CourseSection;
import model.Semester;
import model.Student;
import service.RegistrationException;

import java.time.LocalDate;
import java.util.ArrayList;

public class StudentRegisterController {

    @FXML
    private Label titleLabel;

    @FXML
    private Label step1Label;

    @FXML
    private Label step2Label;

    @FXML
    private Label step3Label;

    @FXML
    private VBox semesterPane;

    @FXML
    private ComboBox<Semester> semesterBox;

    @FXML
    private VBox sectionPane;

    @FXML
    private Label sectionsLabel;

    @FXML
    private ScrollPane sectionsListScroll;

    private RowTable sectionsList;

    @FXML
    private VBox confirmPane;

    @FXML
    private Label detailCourse;

    @FXML
    private Label detailSection;

    @FXML
    private Label detailInstructor;

    @FXML
    private Label detailSchedule;

    @FXML
    private Label detailSeats;

    @FXML
    private Button confirmButton;

    @FXML
    private Button chooseAnotherButton;

    @FXML
    private Button againButton;

    @FXML
    private Label statusLabel;

    private AppContext context;
    private Student student;
    private StudentController parent;

    private int regStep = 1;
    private Semester regSemester;
    private CourseSection regSection;
    private ArrayList<CourseSection> sections = new ArrayList<>();

    public void init(AppContext context, Student student, StudentController parent) {
        sectionsList = new RowTable(sectionsListScroll);
        this.context = context;
        this.student = student;
        this.parent = parent;
        sectionsList.setColumns(
                new String[] {"CODE", "COURSE", "NO.", "INSTRUCTOR", "SCHEDULE", "SEATS"},
                new double[] {3, 4, 1.5, 3, 5, 2});
        ArrayList<Semester> semesters = context.getStore().getSemesters();
        semesterBox.setItems(FXCollections.observableArrayList(semesters));
        if (!semesters.isEmpty()) {
            semesterBox.setValue(semesters.get(0));
        }
        regStep = 1;
        render();
    }

    private void showPane(VBox pane, boolean visible) {
        pane.setVisible(visible);
        pane.setManaged(visible);
    }

    private void render() {
        UiKit.clearStatus(statusLabel);
        showPane(semesterPane, regStep == 1);
        showPane(sectionPane, regStep == 2);
        showPane(confirmPane, regStep == 3);
        titleLabel.setText("Register — Step " + regStep + " of 3");
        markStep(step1Label, regStep == 1);
        markStep(step2Label, regStep == 2);
        markStep(step3Label, regStep == 3);
        if (regStep == 2) {
            fillSections();
        }
        if (regStep == 3) {
            fillDetail();
        }
    }

    private void markStep(Label label, boolean active) {
        label.getStyleClass().remove("step-pill-active");
        if (active) {
            label.getStyleClass().add("step-pill-active");
        }
    }

    private void fillSections() {
        sections = context.getStore().getSectionsInSemester(regSemester);
        sectionsList.clear();
        for (CourseSection section : sections) {
            StringBuilder schedule = new StringBuilder();
            for (model.MeetingTime meeting : section.getMeetings()) {
                if (schedule.length() > 0) {
                    schedule.append(", ");
                }
                schedule.append(meeting);
            }
            int left = BrowseController.seatsLeft(context, section);
            sectionsList.addRow(new String[] {section.getCourse().getCourseCode(),
                    section.getCourse().getCourseName(),
                    String.valueOf(section.getSectionNo()),
                    section.getInstructor().getName(), schedule.toString(),
                    left <= 0 ? "FULL" : left + " left"});
        }
        if (sections.isEmpty()) {
            sectionsList.showEmpty("No sections in this semester.");
        }
        sectionsLabel.setText("SECTIONS IN " + regSemester.toString().toUpperCase());
    }

    private void fillDetail() {
        StringBuilder meetings = new StringBuilder();
        for (model.MeetingTime meeting : regSection.getMeetings()) {
            if (meetings.length() > 0) {
                meetings.append("  •  ");
            }
            meetings.append(meeting);
        }
        int seatsLeft = BrowseController.seatsLeft(context, regSection);
        detailCourse.setText(regSection.getCourse().getCourseCode() + " — "
                + regSection.getCourse().getCourseName());
        detailSection.setText("Section " + regSection.getSectionNo()
                + "  •  " + regSection.getCourse().getCreditHours() + " credits"
                + "  •  " + regSemester);
        detailInstructor.setText("Instructor: " + regSection.getInstructor().getName());
        detailSchedule.setText("Schedule: " + (meetings.length() == 0
                ? "to be announced" : meetings.toString()));
        detailSeats.setText(seatsLeft <= 0 ? "This section is full."
                : seatsLeft + " seats left.");
        confirmButton.setVisible(true);
        confirmButton.setManaged(true);
        chooseAnotherButton.setVisible(true);
        chooseAnotherButton.setManaged(true);
        againButton.setVisible(false);
        againButton.setManaged(false);
    }

    @FXML
    private void onSemesterContinue() {
        regSemester = semesterBox.getValue();
        if (regSemester == null) {
            UiKit.showStatus(statusLabel, false, "Choose a semester first.");
            return;
        }
        regStep = 2;
        render();
    }

    @FXML
    private void onBackToSemester() {
        regStep = 1;
        render();
    }

    @FXML
    private void onSectionContinue() {
        int index = sectionsList.getSelectedIndex();
        if (index < 0 || index >= sections.size()) {
            UiKit.showStatus(statusLabel, false, "Select a section first.");
            return;
        }
        regSection = sections.get(index);
        regStep = 3;
        render();
    }

    @FXML
    private void onBackToSection() {
        regStep = 2;
        render();
    }

    @FXML
    private void onConfirm() {
        try {
            context.getRegistration().register(student, regSection, LocalDate.now());
            String code = regSection.getCourse().getCourseCode();
            if (SaveCheck.enrollmentSaved(student.getUserId(),
                    regSection.getSectionId(), "ENROLLED")) {
                UiKit.showStatus(statusLabel, true,
                        "Registered for " + code + " section "
                                + regSection.getSectionNo() + ".");
            } else {
                UiKit.showStatus(statusLabel, false,
                        "The change was not saved. Try again — if it keeps "
                                + "failing, restart the app or contact the registrar.");
            }
            titleLabel.setText("Register — Done");
            confirmButton.setVisible(false);
            confirmButton.setManaged(false);
            chooseAnotherButton.setVisible(false);
            chooseAnotherButton.setManaged(false);
            againButton.setVisible(true);
            againButton.setManaged(true);
        } catch (RegistrationException ex) {
            UiKit.showStatus(statusLabel, false,
                    StudentCoursesController.ruleText("Registration failed", ex));
        }
    }

    @FXML
    private void onAgain() {
        parent.showRegister();
    }

    @FXML
    private void onHome() {
        parent.showHome();
    }
}
