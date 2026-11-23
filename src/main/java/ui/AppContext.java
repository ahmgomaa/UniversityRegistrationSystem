package ui;

import data.DataStore;
import service.GradingService;
import service.GraduationService;
import service.InAppNotificationService;
import service.RegistrationService;
import service.ReportService;

/**
 * Holds the objects the whole UI shares: the data store and the services.
 * The wiring is exactly the same as in Main — the UI never reimplements
 * registration rules, grading, GPA or graduation logic.
 */
public class AppContext {

    private final DataStore store;
    private final InAppNotificationService notifications;
    private final RegistrationService registration;
    private final GradingService grading;
    private final GraduationService graduation;
    private final ReportService reports;

    public AppContext(DataStore store, InAppNotificationService notifications,
                      RegistrationService registration, GradingService grading,
                      GraduationService graduation, ReportService reports) {
        this.store = store;
        this.notifications = notifications;
        this.registration = registration;
        this.grading = grading;
        this.graduation = graduation;
        this.reports = reports;
    }

    public DataStore getStore() {
        return store;
    }

    public InAppNotificationService getNotifications() {
        return notifications;
    }

    public RegistrationService getRegistration() {
        return registration;
    }

    public GradingService getGrading() {
        return grading;
    }

    public GraduationService getGraduation() {
        return graduation;
    }

    public ReportService getReports() {
        return reports;
    }
}
