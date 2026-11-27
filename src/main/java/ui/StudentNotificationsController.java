package ui;

import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import model.Notification;
import model.Student;

public class StudentNotificationsController {

    @FXML
    private ScrollPane notificationsListScroll;

    private RowTable notificationsList;

    public void init(AppContext context, Student student) {
        notificationsList = new RowTable(notificationsListScroll);
        notificationsList.setSelectable(false);
        notificationsList.setColumns(new String[] {"SENT", "MESSAGE"},
                new double[] {3, 8});
        int count = 0;
        for (Notification notification : context.getNotifications().getInbox(student)) {
            notificationsList.addRow(new String[] {
                    String.valueOf(notification.getSentAt()).replace('T', ' ').substring(0, 16), notification.getMessage()});
            count++;
        }
        if (count == 0) {
            notificationsList.showEmpty("No notifications.");
        }
    }
}
