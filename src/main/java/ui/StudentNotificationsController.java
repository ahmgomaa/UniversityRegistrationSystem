package ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import model.Notification;
import model.Student;

import java.util.ArrayList;

public class StudentNotificationsController {

    @FXML
    private ListView<String> notificationsList;

    public void init(AppContext context, Student student) {
        ArrayList<String> lines = new ArrayList<>();
        for (Notification notification : context.getNotifications().getInbox(student)) {
            lines.add(notification.getSentAt() + " — " + notification.getMessage());
        }
        if (lines.isEmpty()) {
            lines.add("No notifications.");
        }
        UiKit.prepareList(notificationsList);
        notificationsList.setItems(FXCollections.observableArrayList(lines));
    }
}
