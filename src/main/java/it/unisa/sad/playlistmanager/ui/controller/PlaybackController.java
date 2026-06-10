package it.unisa.sad.playlistmanager.ui.controller;

import it.unisa.sad.playlistmanager.application.exceptions.TrackNotFoundException;
import it.unisa.sad.playlistmanager.application.exceptions.ValidationException;
import it.unisa.sad.playlistmanager.application.facade.MusicPlaylistManagerFacade;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.util.Duration;
import it.unisa.sad.playlistmanager.domain.model.PlaybackMode;
import it.unisa.sad.playlistmanager.domain.model.PlaybackSnapshot;
import it.unisa.sad.playlistmanager.domain.model.PlaybackState;
import it.unisa.sad.playlistmanager.domain.model.Track;

/**
 * Sotto-controllore della UI deputato alla gestione del pannello del lettore musicale.
 */
public class PlaybackController {
    
    private final MusicPlaylistManagerFacade facade;

    @FXML private Label lblPlaybackStatus;
    @FXML private Label labelTitle;
    @FXML private Label timerTrack;
    @FXML private Label durationTrack;
    @FXML private Button btnPlayPauseTrack;
    @FXML private Label labelArtist;
    @FXML private Button skipButton;
    @FXML private ToggleButton tglQueueLoop;
    @FXML private Button btnShuffle;
    
    private Track currentTrack;
    private Timeline playbackTimeline;

    public PlaybackController(MusicPlaylistManagerFacade facade) {
        this.facade = facade;
    }

    @FXML
    private void initialize() {
        updateLoopToggleVisual(false);
        startPlaybackRefreshLoop();
    }
    
    public void playTrack(Track track){
        if (facade == null || track == null) return;
        try {
            this.currentTrack = track;
            PlaybackSnapshot snapshot = facade.playTrack(track.getId());
            updatePlaybackView(snapshot);
        } catch (TrackNotFoundException | ValidationException | IllegalArgumentException e){ 
            showPlaybackError(e.getMessage()); 
        }
    }

    /**
     * Gestione Play/Pause robusta che discrimina lo stato PAUSED senza invocare playTrack da zero.
     */
    @FXML
    private void PlayPauseTrack(ActionEvent event) {
        if (facade == null) {
            showPlaybackError("Errore interno: facade non inizializzata.");
            return;
        }
        try {
            PlaybackSnapshot currentSnapshot = facade.getPlaybackSnapshot();
            
            // Caso 1: Sta suonando -> Metti in Pausa
            if (currentSnapshot.state() == PlaybackState.PLAYING) {
                PlaybackSnapshot snapshot = facade.pausePlayback();
                updatePlaybackView(snapshot);
                return;
            }
            
            // Caso 2: È in pausa -> Riprendi (Invochiamo il resume se presente, o usiamo playTrack)
            if (currentSnapshot.state() == PlaybackState.PAUSED) {
                PlaybackSnapshot snapshot = facade.playTrack(currentSnapshot.currentTrack().getId());
                updatePlaybackView(snapshot);
                return;
            }
            
            // Caso 3: È fermo (STOPPED) -> Avvia la traccia selezionata
            if (currentTrack == null) {
                showPlaybackError("Seleziona una traccia da riprodurre.");
                return;
            }
            PlaybackSnapshot snapshot = facade.playTrack(currentTrack.getId());
            updatePlaybackView(snapshot);
        } catch (TrackNotFoundException | ValidationException | IllegalArgumentException e) { 
            showPlaybackError(e.getMessage()); 
        }
    }

    @FXML
    private void toggleShuffle(ActionEvent event) {
        if (facade == null) return;
        
        PlaybackSnapshot currentSnapshot = facade.getPlaybackSnapshot();
        PlaybackMode newMode = (currentSnapshot.mode() == PlaybackMode.SHUFFLE) 
                                ? PlaybackMode.SEQUENTIAL 
                                : PlaybackMode.SHUFFLE;
                                
        PlaybackSnapshot updatedSnapshot = facade.setPlaybackMode(newMode);
        updatePlaybackView(updatedSnapshot);
    }

    /**
     * Intercetta la richiesta di skip della riproduzione in avanti.
     * Richiama la strategia corrente (Sequential o Shuffle) tramite la Facade.
     */
    @FXML
    private void handleNext(ActionEvent event) {
        if (facade == null) return;
        try {
            // 1. Diciamo al modulo Application di avanzare (penserà lui ad azzerare il tempo)
            facade.skipToNext();
            
            // 2. Prendiamo lo snapshot aggiornato e aggiorniamo atomicamente la UI
            PlaybackSnapshot snapshot = facade.getPlaybackSnapshot();
            updatePlaybackView(snapshot);
            
        } catch (Exception e) {
            showPlaybackError(e.getMessage());
        }
    }

    @FXML
    private void handleQueueLoopToggle(ActionEvent event) {
        if (facade == null || tglQueueLoop == null) return;
        PlaybackMode mode = tglQueueLoop.isSelected()
                ? PlaybackMode.REPEAT_ALL
                : PlaybackMode.SEQUENTIAL;
        PlaybackSnapshot snapshot = facade.setPlaybackMode(mode);
        updatePlaybackView(snapshot);
    }

    private void updatePlaybackView(PlaybackSnapshot snapshot) {
        if (snapshot == null) return;
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
        } else {
            currentTrack = null;
            labelTitle.setText("Nessun brano in riproduzione");
            labelArtist.setText("-");
            durationTrack.setText("0:00");
        }

        if (timerTrack != null) {
            timerTrack.setText(formatDuration(snapshot.elapsedSeconds()));
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
        
        if (btnShuffle != null) {
            btnShuffle.setDisable(facade != null && !facade.isShuffleAvailable());
            if (snapshot.mode() == PlaybackMode.SHUFFLE) {
                btnShuffle.setText("🔀 ON");
                btnShuffle.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            } else {
                btnShuffle.setText("🔀 OFF");
                btnShuffle.setStyle("-fx-text-fill: black; -fx-font-weight: normal;");
            }
        }

        if (tglQueueLoop != null) {
            boolean loopEnabled = snapshot.mode() == PlaybackMode.REPEAT_ALL;
            tglQueueLoop.setSelected(loopEnabled);
            updateLoopToggleVisual(loopEnabled);
        }
    }

    private void startPlaybackRefreshLoop() {
        if (playbackTimeline != null) {
            playbackTimeline.stop();
        }
        playbackTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            if (facade == null) return;
            PlaybackSnapshot snapshot = facade.tickPlayback();
            updatePlaybackView(snapshot);
        }));
        playbackTimeline.setCycleCount(Timeline.INDEFINITE);
        playbackTimeline.play();
    }

    private void updateLoopToggleVisual(boolean loopEnabled) {
        if (tglQueueLoop == null) return;
        if (loopEnabled) {
            tglQueueLoop.setText("Loop: ON");
            tglQueueLoop.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        } else {
            tglQueueLoop.setText("Loop: OFF");
            tglQueueLoop.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #333333; -fx-font-weight: bold;");
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
