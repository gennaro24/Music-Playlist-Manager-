package it.unisa.sad.playlistmanager.application.service;

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
        return new PlaybackSnapshot(getCurrentState(), getCurrentTrack());
    }

    /**
     * Gestisce il caso in cui la traccia eliminata sia attualmente in playback. Se la traccia eliminata è quella in questione
     * il playback viene fermato.
     * @param trackId l'identificativo della traccia eliminata 
    */
    public void handleDeletedTrack(String trackId) {
        if (currentTrack != null && currentTrack.getId() == trackId) {
            currentTrack = null;
            currentState = PlaybackState.STOPPED;
        }
    }

    public void handleDeletedPlaylist(String playlistId) {
        if (currentPlaylist != null && currentPlaylist.getId() == playlistId) {
            currentPlaylist = null;
            currentState = PlaybackState.STOPPED;
        }
    }
}
