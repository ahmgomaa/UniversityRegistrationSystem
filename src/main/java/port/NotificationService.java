package port;

import model.User;

public interface NotificationService {

    void notify(User user, String message);
}
