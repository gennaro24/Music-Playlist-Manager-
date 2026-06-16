package it.unisa.sad.playlistmanager.ui.util;

import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;

/**
 * Crea {@link Alert} con il foglio di stile applicativo condiviso.
 */
public final class StyledAlertFactory {

    private static final String APP_CSS =
            StyledAlertFactory.class.getResource("/it/unisa/sad/playlistmanager/ui/styles/app.css")
                    .toExternalForm();

    private StyledAlertFactory() {
    }

    /** Crea un alert tipizzato e applica il tema dell'applicazione. */
    public static Alert create(Alert.AlertType type) {
        Alert alert = new Alert(type);
        applyTheme(alert);
        return alert;
    }

    /** Applica il tema all'alert indicato. */
    public static void applyTheme(Alert alert) {
        if (alert == null) {
            return;
        }
        applyTheme(alert.getDialogPane());
    }

    /** Applica il tema a un dialog pane generico (Dialog, Alert). */
    public static void applyTheme(DialogPane dialogPane) {
        if (dialogPane == null) {
            return;
        }
        if (!dialogPane.getStylesheets().contains(APP_CSS)) {
            dialogPane.getStylesheets().add(APP_CSS);
        }
        if (!dialogPane.getStyleClass().contains("app-alert")) {
            dialogPane.getStyleClass().add("app-alert");
        }
    }
}
