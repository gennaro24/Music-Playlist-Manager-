package it.unisa.sad.playlistmanager.ui.controller;

import it.unisa.sad.playlistmanager.application.facade.MusicPlaylistManagerFacade;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class MainViewController {
    @FXML
    private Label lblFeedback;

    @FXML
    private VBox trackForm;
    @FXML
    private TrackController trackFormController;
    @FXML
    private PlaybackController playbackViewController;

    private MusicPlaylistManagerFacade facade;

    @FXML
    private void initialize() {
        facade = new MusicPlaylistManagerFacade();
        trackFormController.setFacade(facade);
        playbackViewController.setFacade(facade);
    }

    @FXML
    private void handleCreateSong(ActionEvent event) {
        boolean show = !trackForm.isVisible();
        trackForm.setVisible(show);
        trackForm.setManaged(show);
        trackFormController.clearForm();
        lblFeedback.setStyle("-fx-text-fill: #1f7a1f;");
        lblFeedback.setText(show ? "Sezione traccia aperta." : "Sezione traccia chiusa.");
    }

    public void setFacade(MusicPlaylistManagerFacade facade) {
        this.facade = facade;
    }

}