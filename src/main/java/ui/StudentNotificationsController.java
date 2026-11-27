package ui;

import javafx.fxml.FXML;
import model.Notification;
import model.Student;

public class StudentNotificationsController {

    @FXML
    private RowTable notificationsList;

    public void init(AppContext context, Student student) {
        notificationsList.setSelectable(false);
        notificationsList.setColumns(new String[] {"SENT", "MESSAGE"},
                new double[] {3, 8});
        int count = 0;
        for (Notification notification : context.getNotifications().getInbox(student)) {
            notificationsList.addRow(new String[] {
                    String.valueOf(notification.getSentAt()), notification.getMessage()});
            count++;
        }
        if (count == 0) {
            notificationsList.showEmpty("No notifications.");
        }
    }
}
