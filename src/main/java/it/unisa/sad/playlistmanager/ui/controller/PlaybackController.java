package it.unisa.sad.playlistmanager.ui.controller;

import it.unisa.sad.playlistmanager.application.facade.MusicPlaylistManagerFacade;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import it.unisa.sad.playlistmanager.domain.model.PlaybackSnapshot;
import it.unisa.sad.playlistmanager.domain.model.PlaybackState;
import it.unisa.sad.playlistmanager.domain.model.Track;

public class PlaybackController {
    
    
    private MusicPlaylistManagerFacade facade;
    //private PlayBackService playBackService;


    @FXML
    private Label lblPlaybackStatus;
    
    @FXML
    private Label labelTitle;
    
    @FXML
    private Label timerTrack;
    
    @FXML
    private Label durationTrack;
    
    @FXML
    private Button btnPlayPauseTrack;
    
    @FXML
    private Label labelArtist;
    
    @FXML
    private Button skipButton;
    private Track currentTrack;


    public void setFacade(MusicPlaylistManagerFacade facade) {
        this.facade = facade;
        //this.playBackService = facade.getPlayBackService();

    }

    public void playTrack(Track track){
        if (facade == null || track == null) return;
        try{
            this.currentTrack = track;
            PlaybackSnapshot snapshot = facade.playTrack(track.getId());
            updatePlaybackView(snapshot);
        } catch (IllegalArgumentException e){showPlaybackError(e.getMessage());}
    }
    /**
     * Metodo per il play/pause della traccia
     * @param event quando clicco il pulsante play/pause nel playback view
     */
    @FXML
    private void PlayPauseTrack(ActionEvent event) {
        if (facade == null){
            showPlaybackError("Errore interno: facade non inizializzata.");
            return;
        }
        try{
            PlaybackSnapshot currentSnapshot = facade.getPlaybackSnapshot();
            if (currentSnapshot.state() == PlaybackState.PLAYING){
                PlaybackSnapshot snapshot = facade.pausePlayback();
                updatePlaybackView(snapshot);
                return;
            }
            if (currentTrack == null){
                showPlaybackError("Seleziona una traccia da riprodurre.");
                return;
            }

            PlaybackSnapshot snapshot = facade.playTrack(currentTrack.getId());
            updatePlaybackView(snapshot);

        }catch (IllegalArgumentException e){showPlaybackError(e.getMessage());}
    }

    @FXML
    private void handleNext(ActionEvent event) {
        //
    }


        private void updatePlaybackView(PlaybackSnapshot snapshot) {
        if (snapshot == null) {
            return;
        }

        Track track = snapshot.currentTrack();

        if (track != null) {
            currentTrack = track;

            labelTitle.setText(track.getTitle());
            labelArtist.setText(track.getAuthor());
            durationTrack.setText(formatDuration(track.getDuration()));

            labelTitle.setVisible(true);
            labelTitle.setManaged(true);
            labelArtist.setVisible(true);
            labelArtist.setManaged(true);
        }

        if (lblPlaybackStatus != null) {
            lblPlaybackStatus.setText(snapshot.state().name());
        }

        if (btnPlayPauseTrack != null) {
            if (snapshot.state() == PlaybackState.PLAYING) {
                btnPlayPauseTrack.setText("⏸");
            } else {
                btnPlayPauseTrack.setText("▶");
            }
        }
    }
    private void showPlaybackError(String message) {
    if (lblPlaybackStatus != null) {
        lblPlaybackStatus.setText(message);
        }
    }

    private String formatDuration(int totalSeconds) {
    int minutes = totalSeconds / 60;
    int seconds = totalSeconds % 60;
    return String.format("%d:%02d", minutes, seconds);
    }


}