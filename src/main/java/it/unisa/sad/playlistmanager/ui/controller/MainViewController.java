package it.unisa.sad.playlistmanager.ui.controller;

import it.unisa.sad.playlistmanager.application.facade.MusicPlaylistManagerFacade;
import it.unisa.sad.playlistmanager.application.service.TrackService;
import it.unisa.sad.playlistmanager.domain.model.Track;
import it.unisa.sad.playlistmanager.persistence.repository.TrackRepository;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
public class MainViewController {
    @FXML
    private Label lblFeedback;

    @FXML
    private VBox trackForm;
    @FXML
    private TrackController trackFormController;
    @FXML
    private PlaybackController playbackViewController;
    @FXML
    private PlaylistController playlistViewController;
    @FXML
    private Button btnShowTracks;
    @FXML
    private Button btnUpdateTrack;
    @FXML
    private Button btnDeleteTrack;



    private MusicPlaylistManagerFacade facade;

    @FXML
    private void initialize() {
        TrackRepository trackRepository = new TrackRepository() {
            @Override
            public void save(Track track) {
                // Placeholder repository per bootstrap UI
            }
        };
        TrackService trackService = new TrackService(trackRepository);
        facade = new MusicPlaylistManagerFacade(trackService);
        trackFormController.setFacade(facade);
        playbackViewController.setFacade(facade);
        playlistViewController.setFacade(facade);
    }

    @FXML
    private void handleShowTracks(ActionEvent event) {
        boolean show = !trackForm.isVisible();
        trackForm.setVisible(show);
        trackForm.setManaged(show);
        trackFormController.clearForm();
        btnShowTracks.setText(show ? "Chiudi Catalogo" : "Visualizza Catalogo");

        lblFeedback.setStyle("-fx-text-fill: #1f7a1f;");
        lblFeedback.setText(show ? "Sezione traccia aperta." : "Sezione traccia chiusa.");
    }

    public void setFacade(MusicPlaylistManagerFacade facade) {
        this.facade = facade;
    }

}