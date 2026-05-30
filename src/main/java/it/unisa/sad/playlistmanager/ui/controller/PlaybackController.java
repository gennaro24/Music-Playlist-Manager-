package it.unisa.sad.playlistmanager.ui.controller;
import it.unisa.sad.playlistmanager.application.facade.MusicPlaylistManagerFacade;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;

public class PlaybackController {
    private MusicPlaylistManagerFacade facade;
    @FXML
    private Label lblPlaybackStatus;
    @FXML
    private Button btnPlayPause;

    public void setFacade(MusicPlaylistManagerFacade facade) {
        this.facade = facade;
    }

    private enum PlaybackState {
        PLAYING, PAUSED, STOPPED
    }

    private PlaybackState currentState = PlaybackState.STOPPED;

    @FXML
    private void handleNext(ActionEvent event) {
        // Quando premi Next, la riproduzione va in stop prima di ripartire (oppure puoi scegliere la logica che vuoi)
        setPlaybackState(PlaybackState.STOPPED);
        // logica per ripartire subito (esempio passa subito a PLAYING)
        setPlaybackState(PlaybackState.PLAYING);
    }

    @FXML
    private void handlePlayPause(ActionEvent event) {
        if (currentState == PlaybackState.PLAYING) {
            setPlaybackState(PlaybackState.PAUSED);
        } else if (currentState == PlaybackState.PAUSED || currentState == PlaybackState.STOPPED) {
            setPlaybackState(PlaybackState.PLAYING);
        }
    }

    private void setPlaybackState(PlaybackState newState) {
        currentState = newState;
        switch (currentState) {
            case PLAYING:
                lblPlaybackStatus.setText("Riproduzione in corso");
                btnPlayPause.setText("▶");
                break;
            case PAUSED:
                lblPlaybackStatus.setText("In pausa");
                btnPlayPause.setText("⏸");
                break;
            case STOPPED:
                lblPlaybackStatus.setText("Riproduzione fermata");
                
                break;
        }
    }

    private String setPlaybackStateIcon(PlaybackState state) {
        switch (state) {
            case PLAYING:
                return "⏸️";
       
            case PAUSED:
                return "▶️";
            case STOPPED:
                return "⏹️";
        }
        return "";
    }
}
