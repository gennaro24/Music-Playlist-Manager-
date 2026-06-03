package it.unisa.sad.playlistmanager.application.service;

import it.unisa.sad.playlistmanager.domain.model.PlaybackMode;
import it.unisa.sad.playlistmanager.domain.model.PlaybackSnapshot;
import it.unisa.sad.playlistmanager.domain.model.PlaybackState;
import it.unisa.sad.playlistmanager.domain.model.Track;
/**
 * Gestisce lo stato logico del playback. 
 */
public class PlaybackService {
    private PlaybackState currentState = PlaybackState.STOPPED;
    private PlaybackMode currentMode = PlaybackMode.SEQUENTIAL;
    private Track currentTrack = null;

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
     * @throws IllegalArgumentException se {@code track} è {@code null}
     */
    public void playTrack(Track track) {
        if (track == null) {
            throw new IllegalArgumentException("Track cannot be null");
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
        return new PlaybackSnapshot(currentState, currentTrack);
    }
}
