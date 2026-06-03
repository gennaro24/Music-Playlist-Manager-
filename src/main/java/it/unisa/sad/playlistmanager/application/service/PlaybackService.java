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
    private int elapsedSeconds = 0;
    private long lastTickMillis = 0L;

    /**
     * Restituisce lo stato corrente del playback.
     *
     * @return stato corrente
     */
    public synchronized PlaybackState getCurrentState() {
        return currentState;
    }

    /**
     * Restituisce la modalità di playback corrente.
     *
     * @return modalità corrente
     */
    public synchronized PlaybackMode getCurrentMode() {
        return currentMode;
    }
    
    /**
     * Restituisce la traccia corrente.
     *
     * @return traccia corrente, oppure {@code null} se assente
     */
    public synchronized Track getCurrentTrack() {
        return currentTrack;
    }

    public synchronized int getElapsedSeconds() {
        return elapsedSeconds;
    }

    /**
     * Avvia il playback della traccia indicata.
     *
     * @param track traccia da riprodurre
     * @throws IllegalArgumentException se {@code track} è {@code null}
     */
    public synchronized void playTrack(Track track) {
        if (track == null) {
            throw new IllegalArgumentException("Track cannot be null");
        }

        updateElapsedFromClock();

        boolean sameTrack = currentTrack != null && currentTrack.getId().equals(track.getId());
        if (!sameTrack) {
            currentTrack = track;
            elapsedSeconds = 0;
        } else if (currentState == PlaybackState.STOPPED) {
            // Da stopped riparte dall'inizio della stessa traccia.
            elapsedSeconds = 0;
        }

        currentState = PlaybackState.PLAYING;
        lastTickMillis = System.currentTimeMillis();
    }

    /**
     * Mette in pausa il playback se è in esecuzione.
     */
    public synchronized void pause() {
        updateElapsedFromClock();
        if (currentState == PlaybackState.PLAYING) {
            currentState = PlaybackState.PAUSED;
        }
    }

    public synchronized void stop() {
        updateElapsedFromClock();
        currentState = PlaybackState.STOPPED;
        elapsedSeconds = 0;
        lastTickMillis = 0L;
    }

    /**
     * Restituisce una fotografia dello stato corrente.
     *
     * @return snapshot del playback
     */
    public synchronized PlaybackSnapshot getSnapshot() {
        updateElapsedFromClock();
        return new PlaybackSnapshot(getCurrentState(), getCurrentTrack(), getElapsedSeconds());
    }

    private void updateElapsedFromClock() {
        if (currentState != PlaybackState.PLAYING || currentTrack == null) {
            return;
        }
        long nowMillis = System.currentTimeMillis();
        if (lastTickMillis == 0L) {
            lastTickMillis = nowMillis;
            return;
        }
        long elapsedDeltaSeconds = (nowMillis - lastTickMillis) / 1000;
        if (elapsedDeltaSeconds <= 0L) {
            return;
        }
        elapsedSeconds += (int) elapsedDeltaSeconds;
        lastTickMillis += elapsedDeltaSeconds * 1000L;

        int duration = currentTrack.getDuration();
        if (elapsedSeconds >= duration) {
            elapsedSeconds = duration;
            currentState = PlaybackState.STOPPED;
        }
    }
}
