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
    @FXML private Button btnLoop;
    
    // T-145: Riferimento FXML al bottone Shuffle
    @FXML private Button btnShuffle;
    
    private Track currentTrack;
    private Timeline playbackTimeline;

    public PlaybackController(MusicPlaylistManagerFacade facade) {
        this.facade = facade;
    }

    @FXML
    private void initialize() {
        startPlaybackRefreshLoop();
        updateLoopToggleVisual(PlaybackMode.SEQUENTIAL);
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
     * Delega al service la logica di avanzamento (che rispetta la modalità attiva),
     * poi applica le correzioni UX necessarie:
     * - SEQUENTIAL / SHUFFLE : avanza; si ferma a fine coda
     * - REPEAT_ALL           : avanza e torna al primo brano a fine coda
     * - REPEAT_ONE           : avanza al brano successivo (override del loop); il loop
     *                          riparte sul nuovo brano al prossimo completamento
     * - PAUSED + skip        : lo skip manuale riprende la riproduzione sulla nuova traccia
     */
    @FXML
    private void handleNext(ActionEvent event) {
        if (facade == null) return;
        try {
            PlaybackSnapshot before = facade.getPlaybackSnapshot();

            // Nessuna traccia attiva: lo skip non ha senso
            if (before.currentTrack() == null) {
                showPlaybackError("Nessuna traccia in riproduzione.");
                return;
            }

            facade.skipToNext();
            PlaybackSnapshot after = facade.getPlaybackSnapshot();

            // Skip manuale mentre in pausa: riprendi la nuova traccia
            if (after.currentTrack() != null && before.state() == PlaybackState.PAUSED) {
                after = facade.playTrack(after.currentTrack().getId());
            }

            updatePlaybackView(after);

        } catch (Exception e) {
            showPlaybackError(e.getMessage());
        }
    }

    @FXML
    private void handleQueueLoopToggle(ActionEvent event) {
        if (facade == null || btnLoop == null) return;

        PlaybackMode currentMode = facade.getPlaybackSnapshot().mode();
        PlaybackMode nextMode;

        switch (currentMode) {
            case SEQUENTIAL:
            case SHUFFLE:
                nextMode = PlaybackMode.REPEAT_ONE;
                break;
            case REPEAT_ONE:
                nextMode = PlaybackMode.REPEAT_ALL;
                break;
            case REPEAT_ALL:
            default:
                nextMode = PlaybackMode.SEQUENTIAL;
                break;
        }

        PlaybackSnapshot snapshot = facade.setPlaybackMode(nextMode);
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
            if (snapshot.mode() == PlaybackMode.SHUFFLE) {
                btnShuffle.setText("🔀 ON");
                btnShuffle.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            } else {
                btnShuffle.setText("🔀 OFF");
                btnShuffle.setStyle("-fx-text-fill: black; -fx-font-weight: normal;");
            }
        }

        if (btnLoop != null) {
            updateLoopToggleVisual(snapshot.mode());
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

    private void updateLoopToggleVisual(PlaybackMode mode) {
        if (btnLoop == null) return;
        if (mode == PlaybackMode.REPEAT_ONE) {
            btnLoop.setText("🔂 ONE");
            btnLoop.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        } else if (mode == PlaybackMode.REPEAT_ALL) {
            btnLoop.setText("🔁 ALL");
            btnLoop.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        } else {
            btnLoop.setText("🔂 OFF");
            btnLoop.setStyle("-fx-text-fill: black; -fx-font-weight: normal;");
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
