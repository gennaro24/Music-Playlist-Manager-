package it.unisa.sad.playlistmanager.application.service;

import java.util.List;
import java.util.Objects;

import it.unisa.sad.playlistmanager.application.exceptions.PlaylistNotFoundException;
import it.unisa.sad.playlistmanager.application.exceptions.TrackNotFoundException;
import it.unisa.sad.playlistmanager.application.exceptions.ValidationException;
import it.unisa.sad.playlistmanager.domain.model.PlaybackMode;
import it.unisa.sad.playlistmanager.domain.model.PlaybackQueue;
import it.unisa.sad.playlistmanager.domain.model.PlaybackSnapshot;
import it.unisa.sad.playlistmanager.domain.model.PlaybackSource;
import it.unisa.sad.playlistmanager.domain.model.PlaybackState;
import it.unisa.sad.playlistmanager.domain.model.Playlist;
import it.unisa.sad.playlistmanager.domain.model.Track;
import it.unisa.sad.playlistmanager.domain.strategy.PlaybackStrategy;
import it.unisa.sad.playlistmanager.domain.strategy.SequentialPlaybackStrategy;
import it.unisa.sad.playlistmanager.persistence.repository.PlaylistRepository;

/**
 * Gestisce lo stato logico del playback. 
 */
public class PlaybackService {
    private PlaybackState currentState = PlaybackState.STOPPED;
    private PlaybackMode currentMode = PlaybackMode.SEQUENTIAL;
    private Track currentTrack = null;
    private Playlist currentPlaylist = null;
    private final PlaylistRepository playlistRepository;
    private PlaybackQueue currentQueue = null;
    private int elapsedSeconds = 0;
    private PlaybackStrategy playbackStrategy = new SequentialPlaybackStrategy();

    public PlaybackService(PlaylistRepository playlistRepository) {
        this.playlistRepository = Objects.requireNonNull(
                playlistRepository,
                "playlistRepository non può essere null"
        );
    }

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
        this.currentMode = Objects.requireNonNull(mode, "mode non può essere null");
        if (mode == PlaybackMode.SEQUENTIAL) {
            this.playbackStrategy = new SequentialPlaybackStrategy();
        }
    }

    /**
     * Restituisce i secondi trascorsi dall'inizio della traccia corrente.
     *
     * @return secondi trascorsi
     */
    public int getElapsedSeconds() {
        return elapsedSeconds;
    }

    /**
     * Restituisce l'indice della traccia corrente nella coda di riproduzione.
     *
     * @return indice della traccia corrente, oppure {@code -1} se la coda è vuota
     */
    public int getCurrentQueueIndex() {
        return currentQueue == null ? -1 : currentQueue.getCurrentIndex();
    }   

    /**
     * Aggiorna lo stato del playback.
     * 
     */
    public void tick() {
        if (currentState != PlaybackState.PLAYING || currentTrack == null) {
            return;
        }

        if (elapsedSeconds < currentTrack.getDuration()) {
            elapsedSeconds++;
        }

        if (elapsedSeconds >= currentTrack.getDuration()) {
            if (currentMode == PlaybackMode.REPEAT_ONE) {
                elapsedSeconds = 0;
            } else {
                elapsedSeconds = currentTrack.getDuration();
                currentState = PlaybackState.STOPPED;
            }
        }
    }   

    /**
     * Avvia il playback della playlist indicata.
     *
     * @param playlistId identificativo della playlist da riprodurre
     * @throws PlaylistNotFoundException se non esiste una playlist con l'identificativo specificato
     * @throws ValidationException se la playlist è vuota
     */
    public void playPlaylist(String playlistId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new PlaylistNotFoundException("Playlist non trovata."));

        List<Track> tracks = playlistRepository.findTracksByPlaylistId(playlistId);

        if (tracks.isEmpty()) {
            throw new ValidationException("Impossibile avviare una playlist vuota.");
        }

        currentPlaylist = playlist;
        currentQueue = new PlaybackQueue(tracks, PlaybackSource.PLAYLIST);
        currentTrack = currentQueue.getCurrentTrack();
        currentMode = PlaybackMode.SEQUENTIAL;
        playbackStrategy = new SequentialPlaybackStrategy();
        currentState = PlaybackState.PLAYING;
        elapsedSeconds = 0;
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
        currentPlaylist = null;
        currentQueue = new PlaybackQueue(List.of(track), PlaybackSource.SINGLE);
        currentMode = PlaybackMode.SEQUENTIAL;
        playbackStrategy = new SequentialPlaybackStrategy();
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
            currentPlaylist = null;
            currentQueue = null;
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
            currentQueue = null;
            elapsedSeconds = 0;
        }
    }
}