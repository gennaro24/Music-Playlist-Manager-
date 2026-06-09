package it.unisa.sad.playlistmanager.application.service;

import java.util.Objects;

import it.unisa.sad.playlistmanager.application.exceptions.TrackNotFoundException;
import it.unisa.sad.playlistmanager.domain.model.PlaybackMode;
import it.unisa.sad.playlistmanager.domain.model.PlaybackSnapshot;
import it.unisa.sad.playlistmanager.domain.model.PlaybackState;
import it.unisa.sad.playlistmanager.domain.model.Track;
import it.unisa.sad.playlistmanager.domain.model.Playlist;
/**
 * Gestisce lo stato logico del playback. 
 */
public class PlaybackService {
    private PlaybackState currentState = PlaybackState.STOPPED;
    private PlaybackMode currentMode = PlaybackMode.SEQUENTIAL;
    private Track currentTrack = null;
    private Playlist currentPlaylist = null;
    private int elapsedSeconds = 0;
    //TODO: Modificare la gestione della playlist per gestire la cancellazione di una playlist.

    /**
     * Restituisce lo stato corrente del playback.
     *
     * @return stato corrente
     */
    public PlaybackState getCurrentState() {
        return currentState;
    }

    /**
     * Restituisce la modalità di playback corrente.
     *
     * @return modalità corrente
     */
    public PlaybackMode getCurrentMode() {
        return currentMode;
    }
    
    /**
     * Restituisce la traccia corrente.
     *
     * @return traccia corrente, oppure {@code null} se assente
     */
    public Track getCurrentTrack() {
        return currentTrack;
    }

    /**
     * Restituisce la playlist corrente.
     *
     * @return playlist corrente, oppure {@code null} se assente
     */
    public Playlist getCurrentPlaylist() {
        return currentPlaylist;
    }

    /**
     * Imposta la modalità di playback.
     *
     * @param mode modalità da impostare
     */
    public void setPlaybackMode(PlaybackMode mode) {
        this.currentMode = mode;
    }

    /**
     * Restituisce i secondi trascorsi dall'inizio della traccia corrente.
     * @return
     */
    public int getElapsedSeconds() {
        return elapsedSeconds;
    }

    /**
     * Aggiorna lo stato del playback.
     */
    public void tick() {
        if (currentState == PlaybackState.PLAYING
                && currentTrack != null
                && elapsedSeconds < currentTrack.getDuration()) {
            elapsedSeconds++;
        }
    }   

    /**
     * Avvia il playback della traccia indicata.
     *
     * @param track traccia da riprodurre
     * @throws TrackNotFoundException se {@code track} è {@code null}
     */
    public void playTrack(Track track) {
        if (track == null) {
            throw new TrackNotFoundException("Track non trovata.");
        }

        currentTrack = track;
        currentState = PlaybackState.PLAYING;
        elapsedSeconds = 0;
    }

    /**
     * Mette in pausa il playback se è in esecuzione.
     */
    public void pause() {
        if (currentState == PlaybackState.PLAYING) {
            currentState = PlaybackState.PAUSED;
        }
    }

    /**
     * Restituisce una fotografia dello stato corrente.
     *
     * @return snapshot del playback
     */
    public PlaybackSnapshot getSnapshot() {
        return new PlaybackSnapshot(getCurrentState(), getCurrentTrack(), getCurrentMode(), getElapsedSeconds());
    }

    /**
     * Gestisce il caso in cui la traccia eliminata sia attualmente in playback. Se la traccia eliminata è quella in questione
     * il playback viene fermato.
     * @param trackId l'identificativo della traccia eliminata 
    */
    public void handleDeletedTrack(String trackId) {
        if (currentTrack != null && Objects.equals(currentTrack.getId(), trackId)) {
            currentTrack = null;
            currentState = PlaybackState.STOPPED;
            elapsedSeconds = 0;
        }
    }

    /**
     * Gestisce il caso in cui la playlist eliminata sia quella attualmente in playback.
     * In questo caso il playback viene fermato e la playlist corrente viene azzerata.
     * @param playlistId l'identificativo della playlist eliminata
     */
public void handleDeletedPlaylist(String playlistId) {
        if (currentPlaylist != null && Objects.equals(currentPlaylist.getId(), playlistId)) {
            currentPlaylist = null;
            currentTrack = null;
            currentState = PlaybackState.STOPPED;
            elapsedSeconds = 0;
        }
    }
}
