package ui;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.CourseSection;
import model.MeetingTime;
import model.Semester;

import java.util.ArrayList;

public class BrowseView {

    private static final double COL_COURSE = 330;
    private static final double COL_SECTION = 70;
    private static final double COL_INSTRUCTOR = 190;
    private static final double COL_SCHEDULE = 330;
    private static final double COL_SEATS = 90;
    private static final double COL_STATUS = 90;

    private final AppContext context;
    private final JavaFxApp app;

    public BrowseView(AppContext context, JavaFxApp app) {
        this.context = context;
        this.app = app;
    }

    public VBox build() {
        ArrayList<Semester> semesters = context.getStore().getSemesters();
        ComboBox<Semester> semesterBox = new ComboBox<>(
                FXCollections.observableArrayList(semesters));
        if (!semesters.isEmpty()) {
            semesterBox.setValue(semesters.get(0));
        }
        semesterBox.setMaxWidth(Double.MAX_VALUE);

        VBox sectionsCard = UiKit.statCard("0", "SECTIONS", UiKit.ACCENT);
        VBox seatsCard = UiKit.statCard("0", "SEATS OPEN", UiKit.GREEN);
        VBox fullCard = UiKit.statCard("0", "FULL", UiKit.RED);
        HBox stats = UiKit.statRow(sectionsCard, seatsCard, fullCard);

        VBox table = UiKit.insetCard();

        Runnable refresh = new Runnable() {
            @Override
            public void run() {
            Semester semester = semesterBox.getValue();
            ArrayList<CourseSection> sections = new ArrayList<>();
            if (semester != null) {
                sections.addAll(context.getStore().getSectionsInSemester(semester));
            }

            table.getChildren().clear();
            table.getChildren().add(headerRow());
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
                table.getChildren().add(UiKit.emptyState(
                        "No sections in this semester."));
            }

            Label sectionsValue = (Label) sectionsCard.getChildren().get(0);
            Label seatsValue = (Label) seatsCard.getChildren().get(0);
            Label fullValue = (Label) fullCard.getChildren().get(0);
            sectionsValue.setText(String.valueOf(sections.size()));
            seatsValue.setText(String.valueOf(seats));
            fullValue.setText(String.valueOf(full));
            }
        };
        semesterBox.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                refresh.run();
            }
        });
        refresh.run();

        Button backButton = UiKit.plainButton("Back to Sign In");
        backButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                app.showLogin();
            }
        });

        VBox root = new VBox(12,
                UiKit.largeTitle("Browse Courses"),
                UiKit.caption("Live seat availability, no sign-in needed."),
                stats,
                UiKit.sectionLabel("Semester"), semesterBox,
                UiKit.sectionLabel("Sections"), table,
                backButton);
        root.setPadding(new Insets(20));
        UiKit.applyGroupedBackground(root);
        return root;
    }

    private static HBox headerRow() {
        HBox row = new HBox(12,
                UiKit.columnHeader("Course", COL_COURSE),
                UiKit.columnHeader("Sec", COL_SECTION),
                UiKit.columnHeader("Instructor", COL_INSTRUCTOR),
                UiKit.columnHeader("Schedule", COL_SCHEDULE),
                UiKit.columnHeader("Seats", COL_SEATS),
                UiKit.columnHeader("Status", COL_STATUS));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 16, 10, 16));
        return row;
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

