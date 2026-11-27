package ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.CourseSection;
import model.MeetingTime;
import model.Semester;

import java.util.ArrayList;

public class BrowseController {

    private static final double COL_COURSE = 330;
    private static final double COL_SECTION = 70;
    private static final double COL_INSTRUCTOR = 190;
    private static final double COL_SCHEDULE = 330;
    private static final double COL_SEATS = 90;
    private static final double COL_STATUS = 90;

    @FXML
    private Label sectionsValue;

    @FXML
    private Label seatsValue;

    @FXML
    private Label fullValue;

    @FXML
    private ComboBox<Semester> semesterBox;

    @FXML
    private VBox table;

    @FXML
    private HBox headerRow;

    private AppContext context;
    private JavaFxApp app;
    private boolean ready;

    public void init(AppContext context, JavaFxApp app) {
        this.context = context;
        this.app = app;
        ArrayList<Semester> semesters = context.getStore().getSemesters();
        semesterBox.setItems(FXCollections.observableArrayList(semesters));
        if (!semesters.isEmpty()) {
            semesterBox.setValue(semesters.get(0));
        }
        ready = true;
        refresh();
    }

    @FXML
    private void onSemesterChosen() {
        if (ready) {
            refresh();
        }
    }

    @FXML
    private void onBack() {
        app.showLogin();
    }

    private void refresh() {
        Semester semester = semesterBox.getValue();
        ArrayList<CourseSection> sections = new ArrayList<>();
        if (semester != null) {
            sections.addAll(context.getStore().getSectionsInSemester(semester));
        }

        table.getChildren().setAll(headerRow);
        int open = 0;
        int full = 0;
        int seats = 0;
        for (CourseSection section : sections) {
            int left = seatsLeft(context, section);
            if (left <= 0) {
                full++;
            } else {
                open++;
                seats += left;
            }
            table.getChildren().add(UiKit.separator());
            table.getChildren().add(sectionRow(section, left));
        }
        if (sections.isEmpty()) {
            table.getChildren().add(UiKit.separator());
            table.getChildren().add(UiKit.emptyState("No sections in this semester."));
        }

        sectionsValue.setText(String.valueOf(sections.size()));
        seatsValue.setText(String.valueOf(seats));
        fullValue.setText(String.valueOf(full));
    }

    private HBox sectionRow(CourseSection section, int seatsLeft) {
        Label course = UiKit.columnLabel(
                section.getCourse().getCourseCode()
                        + " — " + section.getCourse().getCourseName()
                        + " (" + section.getCourse().getCreditHours() + " cr)",
                COL_COURSE);
        course.setFont(UiKit.textFont(javafx.scene.text.FontWeight.SEMI_BOLD, 14));

        StringBuilder times = new StringBuilder();
        for (MeetingTime meeting : section.getMeetings()) {
            if (times.length() > 0) {
                times.append(", ");
            }
            times.append(meeting);
        }

        Label status = UiKit.softPill(seatsLeft <= 0 ? "FULL" : "Open",
                seatsLeft <= 0 ? UiKit.RED : UiKit.GREEN);
        HBox statusCell = new HBox(status);
        statusCell.setPrefWidth(COL_STATUS);
        statusCell.setMinWidth(COL_STATUS);
        statusCell.setAlignment(Pos.CENTER_LEFT);

        HBox row = new HBox(12,
                course,
                UiKit.columnLabel(String.valueOf(section.getSectionNo()), COL_SECTION),
                UiKit.columnLabel(section.getInstructor().getName(), COL_INSTRUCTOR),
                UiKit.columnLabel(times.toString(), COL_SCHEDULE),
                UiKit.columnLabel(seatsLeft + " / " + section.getCapacity(), COL_SEATS),
                statusCell);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 16, 12, 16));
        return row;
    }

    public static int seatsLeft(AppContext context, CourseSection section) {
        int taken = 0;
        for (model.Student student : context.getStore().getStudents()) {
            for (model.Enrollment enrollment
                    : student.getAcademicRecord().getEnrollments()) {
                if (enrollment.getCourseSection().equals(section)
                        && enrollment.getStatus()
                                == model.enums.EnrollmentStatus.ENROLLED) {
                    taken++;
                }
            }
        }
        return section.getCapacity() - taken;
    }

    public static String describeWithSeats(AppContext context, CourseSection section) {
        int left = seatsLeft(context, section);
        String seats = left <= 0 ? "FULL" : left + " seats left";
        return (left <= 0 ? "[FULL] " : "") + describe(section) + "  •  " + seats;
    }

    public static String describe(CourseSection section) {
        StringBuilder text = new StringBuilder();
        text.append(section.getCourse().getCourseCode())
                .append(" — ").append(section.getCourse().getCourseName())
                .append(" (").append(section.getCourse().getCreditHours()).append(" credits)")
                .append("  •  Section ").append(section.getSectionNo())
                .append("  •  ").append(section.getInstructor().getName())
                .append("  •  Capacity ").append(section.getCapacity());
        for (MeetingTime meeting : section.getMeetings()) {
            text.append("  •  ").append(meeting);
        }
        return text.toString();
    }
}
