package it.unisa.sad.playlistmanager.ui.controller;

import it.unisa.sad.playlistmanager.application.facade.MusicPlaylistManagerFacade;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import it.unisa.sad.playlistmanager.domain.model.Track;
import it.unisa.sad.playlistmanager.application.facade.MusicPlaylistManagerFacade;
//import it.unisa.sad.playlistmanager.application.service.PlayBackService;

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


    public void setFacade(MusicPlaylistManagerFacade facade) {
        this.facade = facade;
        //this.playBackService = facade.getPlayBackService();

    }

    private enum PlaybackState {
        PLAYING, PAUSED, STOPPED
    }

    private PlaybackState currentState = PlaybackState.STOPPED;


    /**
     * Metodo per il play/pause della traccia
     * @param event quando clicco il pulsante play/pause nel playback view
     */
    @FXML
    private void PlayPauseTrack(ActionEvent event) {
        //se la traccia è in riproduzione, setto come nuovo stato la pausa
        if (currentState == PlaybackState.PLAYING) {
            setPlaybackState(PlaybackState.PAUSED);
            //facade.pause();
        } else {
            //se la traccia è in pausa, setto come nuovo stato play
            setPlaybackState(PlaybackState.PLAYING);
            //facade.play();
        }
    }

    @FXML
    private void handleNext(ActionEvent event) {
        //
    }

    /**
     * metodo che gestisce il playback in base allo stato corrente
     * @param newState
     */
    private void setPlaybackState(PlaybackState newState) {
        currentState = newState;
        
        if (btnPlayPauseTrack == null || labelArtist == null || labelTitle == null) {
            return;
        }

        //switch per gestire il playback in base allo stato corrente
        switch (currentState) {
            case PLAYING:
                //setto il titolo della traccia in riproduzione
                //labelTitle.setText(playBackService.getTrack().getTitle() + " in riproduzione");
                labelTitle.setText("Titolo traccia in riproduzione");
                labelArtist.setVisible(true);
                labelArtist.setManaged(true);
                labelArtist.setText("Artista traccia in riproduzione");
                labelTitle.setText("Titolo traccia in riproduzione");
                btnPlayPauseTrack.setText("⏸");
                break;
                
            case PAUSED:
                labelTitle.setText("Titolo traccia in pausa");
                btnPlayPauseTrack.setText("▶");
                labelArtist.setText("Artista traccia in pausa");
                labelArtist.setVisible(true);
                labelArtist.setManaged(true);
                break;
                
            case STOPPED:
                // Opzionale: nasconde nuovamente il titolo se la riproduzione viene interrotta
                labelTitle.setVisible(false);
                labelTitle.setManaged(false);
                labelArtist.setVisible(false);
                labelArtist.setManaged(false);
                break;
        }
    }
}