package it.unisa.sad.playlistmanager.ui.controller;

import it.unisa.sad.playlistmanager.application.exceptions.TrackNotFoundException;
import it.unisa.sad.playlistmanager.application.exceptions.ValidationException;
import it.unisa.sad.playlistmanager.application.facade.MusicPlaylistManagerFacade;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import it.unisa.sad.playlistmanager.domain.model.PlaybackSnapshot;
import it.unisa.sad.playlistmanager.domain.model.PlaybackState;
import it.unisa.sad.playlistmanager.domain.model.Track;

/**
 * Sotto-controllore della UI deputato alla gestione del pannello del lettore musicale
 * (Pulsanti Play, Pause, Skip ed aggiornamento real-time dei metadati grafici della traccia in riproduzione).
 * * <p><b>Revisione Sprint 2:</b> Riadattato per aderire alla Constructor Injection. Sfrutta 
 * i modelli immutabili di snapshot provenienti dallo strato di dominio per aggiornare atomicamente 
 * la vista a seguito di un evento.</p>
 * @version 2.0
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
    
    private Track currentTrack;

    /**
     * Costruttore uniforme per l'attivazione della Constructor Injection (Task T-63).
     *
     * @param facade L'istanza dell'Application Facade ad alto livello.
     */
    public PlaybackController(MusicPlaylistManagerFacade facade) {
        this.facade = facade;
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
        }

        // T-110: Aggiornamento timer visivo (tempo congelato)
        // Il backend non avanza l'elapsedSeconds se lo stato è PAUSED,
        // quindi la UI rimarrà "congelata" sullo stesso secondo.
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