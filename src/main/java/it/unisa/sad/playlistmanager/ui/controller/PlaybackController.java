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
import it.unisa.sad.playlistmanager.domain.model.PlaybackSnapshot;
import it.unisa.sad.playlistmanager.domain.model.PlaybackState;
import it.unisa.sad.playlistmanager.domain.model.PlaybackMode;
import it.unisa.sad.playlistmanager.domain.model.Track;

/**
 * Sotto-controllore della UI deputato alla gestione del pannello del lettore musicale
 * (Pulsanti Play, Pause, Skip ed aggiornamento real-time dei metadati grafici della traccia in riproduzione).
 * <p><b>Revisione Sprint 2:</b> Riadattato per aderire alla Constructor Injection. Sfrutta 
 * i modelli immutabili di snapshot provenienti dallo strato di dominio per aggiornare atomicamente 
 * la vista a seguito di un evento.</p>
 * @version 2.1
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
    
    // T-145: Riferimento FXML al bottone Shuffle
    @FXML private Button btnShuffle;
    
    private Track currentTrack;
    private Timeline playbackTimeline;

    /**
     * Costruttore uniforme per l'attivazione della Constructor Injection (Task T-63).
     *
     * @param facade L'istanza dell'Application Facade ad alto livello.
     */
    public PlaybackController(MusicPlaylistManagerFacade facade) {
        this.facade = facade;
    }

    @FXML
    private void initialize() {
        startPlaybackRefreshLoop();
    }

    /**
     * Comanda l'avvio immediato della riproduzione audio per una specifica traccia di dominio.
     *
     * @param track L'oggetto Track da riprodurre.
     */
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
     * Intercetta le richieste di Play/Pause provenienti dalla UI, esaminando
     * lo stato dello snapshot corrente per determinare la transizione applicativa corretta.
     *
     * @param event Evento di click sul pulsante grafico di riproduzione.
     */
    @FXML
    private void PlayPauseTrack(ActionEvent event) {
        if (facade == null) {
            showPlaybackError("Errore interno: facade non inizializzata.");
            return;
        }
        try {
            PlaybackSnapshot currentSnapshot = facade.getPlaybackSnapshot();
            if (currentSnapshot.state() == PlaybackState.PLAYING) {
                PlaybackSnapshot snapshot = facade.pausePlayback();
                updatePlaybackView(snapshot);
                return;
            }
            if (currentSnapshot.state() == PlaybackState.PAUSED) {
                PlaybackSnapshot snapshot = facade.resumePlayback();
                updatePlaybackView(snapshot);
                return;
            }
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

    /**
     * T-145: Intercetta il click sul pulsante Shuffle, alternando la modalità di playback
     * e aggiornando istantaneamente l'interfaccia.
     */
    @FXML
    private void toggleShuffle(ActionEvent event) {
        if (facade == null) return;
        
        PlaybackSnapshot currentSnapshot = facade.getPlaybackSnapshot();
        
        // Se è già in Shuffle, torna Sequenziale. Altrimenti attiva Shuffle.
        PlaybackMode newMode = (currentSnapshot.mode() == PlaybackMode.SHUFFLE) 
                                ? PlaybackMode.SEQUENTIAL 
                                : PlaybackMode.SHUFFLE;
                                
        PlaybackSnapshot updatedSnapshot = facade.setPlaybackMode(newMode);
        updatePlaybackView(updatedSnapshot);
    }

    /**
     * Intercetta la richiesta di skip della riproduzione in avanti.
     * La logica interna verrà espansa nello Sprint 2 in conformità con i pattern Strategy di riproduzione.
     *
     * @param event Evento d'azione sul pulsante Skip.
     */
    @FXML
    private void handleNext(ActionEvent event) {
        // Sarà oggetto di espansione con le logiche di Shuffle/Loop dello Sprint 2
    }

    /**
     * Scompone lo snapshot immutabile ricevuto dal core di business, sincronizzando
     * atomicamente i testi e le icone degli elementi FXML dello stage.
     *
     * @param snapshot Il DTO strutturato contenente lo stato istantaneo del motore di riproduzione.
     */
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
            // CORREZIONE: Se la traccia è null (es. cancellata), resetta i campi grafici del Player
            currentTrack = null;
            labelTitle.setText("Nessun brano in riproduzione");
            labelArtist.setText("-");
            durationTrack.setText("0:00");
        }

        if (timerTrack != null) {
            timerTrack.setText(formatDuration(snapshot.elapsedSeconds()));
        }

        if (lblPlaybackStatus != null) {
            // T-110: Aggiorna l'etichetta testuale con lo stato esatto (es. PAUSED)
            lblPlaybackStatus.setText(snapshot.state().name()); 
        }

        if (btnPlayPauseTrack != null) {
            if (snapshot.state() == PlaybackState.PLAYING) {
                btnPlayPauseTrack.setText("⏸");
            } else {
                // T-110: Rimette l'icona Play (▶) se lo stato è PAUSED o STOPPED
                btnPlayPauseTrack.setText("▶");
            }
        }
        
        // T-145: Aggiorna l'estetica del pulsante Shuffle
        if (btnShuffle != null) {
            if (snapshot.mode() == PlaybackMode.SHUFFLE) {
                btnShuffle.setText("🔀 ON");
                btnShuffle.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            } else {
                btnShuffle.setText("🔀 OFF");
                btnShuffle.setStyle("-fx-text-fill: black; -fx-font-weight: normal;");
            }
        }
    }

    /**
     * Avvia il refresh periodico della vista playback.
     */
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

    /**
     * Consente al coordinatore centrale di forzare il rinfresco 
     * della vista del lettore recuperando lo snapshot aggiornato dalla Facade.
     */
    public void refresh() {
        if (facade != null) {
            updatePlaybackView(facade.getPlaybackSnapshot());
        }
    }

    /**
     * Propaga la notifica testuale di un errore di riproduzione sul pannello.
     */
    private void showPlaybackError(String message) {
        if (lblPlaybackStatus != null) {
            lblPlaybackStatus.setText(message);
        }
    }

    /**
     * Formatta un valore espresso in secondi nel classico formato MM:SS per scopi di visualizzazione.
     */
    private String formatDuration(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}