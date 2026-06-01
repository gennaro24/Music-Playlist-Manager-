package it.unisa.sad.playlistmanager.ui.controller;

import it.unisa.sad.playlistmanager.application.facade.MusicPlaylistManagerFacade;
import it.unisa.sad.playlistmanager.application.service.TrackService;
import it.unisa.sad.playlistmanager.domain.model.Playlist;
import it.unisa.sad.playlistmanager.persistence.repository.TrackRepository;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;


public class MainViewController {

    private MusicPlaylistManagerFacade facade;
    private Playlist selectedPlaylist;

    @FXML
    private Label lblFeedback;
    @FXML
    private VBox trackContainer;
    @FXML
    private TrackController trackContainerController;
    @FXML
    private PlaybackController playbackViewController;
    @FXML
    private PlaylistController playlistViewController;
    @FXML
    private Button btnShowTracks;
    

    

    @FXML
    private void initialize() {
        TrackRepository inMemoryTrackRepository = new TrackRepository() {
            @Override
            public void save(it.unisa.sad.playlistmanager.domain.model.Track track) {
                // temporaneo: addTrack/addTrack2 non usa ancora persistenza reale
            }
        };
        TrackService trackService = new TrackService(inMemoryTrackRepository);
        facade = new MusicPlaylistManagerFacade(trackService);

        if (trackContainerController != null) {
            trackContainerController.setFacade(facade);
        }
        if (playlistViewController != null) {
            playlistViewController.setFacade(facade);
        }
        if (playbackViewController != null) {
            playbackViewController.setFacade(facade);
        }

        if (playlistViewController != null) {
            playlistViewController.setOnPlaylistSelected(playlist -> {
                if (trackContainer != null) {
                    trackContainer.setVisible(true);
                    trackContainer.setManaged(true);
                }
        
                if (playlist != null) {
                    // Playlist selezionata
                    this.selectedPlaylist = playlist;
        
                    if (trackContainerController != null) {
                        trackContainerController.displayPlaylistTracks(playlist);
                    }
                    if (btnShowTracks != null) {
                        btnShowTracks.setText("Visualizza Catalogo");
                    }
                    if (lblFeedback != null) {
                        lblFeedback.setStyle("-fx-text-fill: #1f7a1f;");
                        lblFeedback.setText("Visualizzazione tracce playlist: " + playlist.getName());
                    }
                } else {
                    // Playlist deselezionata (toggle)
                    this.selectedPlaylist = null;
        
                    if (trackContainerController != null) {
                        trackContainerController.showCatalogView();
                    }
                    if (btnShowTracks != null) {
                        btnShowTracks.setText("Chiudi Catalogo");
                    }
                    if (lblFeedback != null) {
                        lblFeedback.setStyle("-fx-text-fill: #1f7a1f;");
                        lblFeedback.setText("Playlist deselezionata. Ritorno al catalogo completo.");
                    }
                }
            });
        }
    }

    @FXML
    private void handleShowTracks(ActionEvent event) {
        if (trackContainer == null || btnShowTracks == null || lblFeedback == null) {
            return;
        }
        // Toggle apertura/chiusura sezione catalogo.
        boolean show = !trackContainer.isVisible();
        trackContainer.setVisible(show);
        trackContainer.setManaged(show);

        if (show) {
            // Quando riapro dal bottone, forzo sempre la vista catalogo completo.
            if (trackContainerController != null) {
                trackContainerController.clearForm();
                trackContainerController.showCatalogView();
            }
            btnShowTracks.setText("Chiudi Catalogo");
            lblFeedback.setStyle("-fx-text-fill: #1f7a1f;");
            if (selectedPlaylist != null) {
                lblFeedback.setText("Catalogo completo visualizzato (playlist selezionata: " + selectedPlaylist.getName() + ").");
            } else {
                lblFeedback.setText("Catalogo completo visualizzato.");
            }
        } else {
            btnShowTracks.setText("Visualizza Catalogo");
            lblFeedback.setStyle("-fx-text-fill: #1f7a1f;");
            lblFeedback.setText("Catalogo chiuso.");
        }
    }

    public void setFacade(MusicPlaylistManagerFacade facade) {
        this.facade = facade;
    }


    

}