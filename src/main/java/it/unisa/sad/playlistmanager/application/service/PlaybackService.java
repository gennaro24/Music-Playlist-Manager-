package it.unisa.sad.playlistmanager.application.service;

import it.unisa.sad.playlistmanager.application.exceptions.PlaylistNotFoundException;
import it.unisa.sad.playlistmanager.application.exceptions.TrackNotFoundException;
import it.unisa.sad.playlistmanager.application.exceptions.ValidationException;
import it.unisa.sad.playlistmanager.domain.model.PlaybackMode;
import it.unisa.sad.playlistmanager.domain.model.PlaybackQueue;
import it.unisa.sad.playlistmanager.domain.model.PlaybackSource;
import it.unisa.sad.playlistmanager.domain.model.PlaybackSnapshot;
import it.unisa.sad.playlistmanager.domain.model.PlaybackState;
import it.unisa.sad.playlistmanager.domain.model.Playlist;
import it.unisa.sad.playlistmanager.domain.model.Track;
import it.unisa.sad.playlistmanager.domain.strategy.PlaybackStrategy;
import it.unisa.sad.playlistmanager.domain.strategy.SequentialPlaybackStrategy;
import it.unisa.sad.playlistmanager.persistence.repository.PlaylistRepository;

import java.util.List;
import java.util.Objects;

/**
 * Gestisce lo stato logico del playback e coordina le strategie di riproduzione.
 * Agisce come Contesto per il pattern Strategy.
 * @version 1.2
 */
public class PlaybackService {
    private PlaybackState currentState = PlaybackState.STOPPED;
    private PlaybackMode currentMode = PlaybackMode.SEQUENTIAL;
    private Track currentTrack = null;
    private Playlist currentPlaylist = null;
    private PlaybackQueue currentQueue = null;
    private final PlaylistRepository playlistRepository;
    private int elapsedSeconds = 0;

    // Ripristinato per mantenere lo stato della coda rimescolata (US-15)
    private PlaybackStrategy playbackStrategy = new SequentialPlaybackStrategy();

    public PlaybackService(PlaylistRepository playlistRepository) {
        this.playlistRepository = Objects.requireNonNull(
                playlistRepository,
                "playlistRepository non può essere null"
        );
    }

    public PlaybackService() {
        this.playlistRepository = null;
    }

    public PlaybackState getCurrentState() {
        return currentState;
    }

    public PlaybackMode getCurrentMode() {
        return currentMode;
    }

    /**
     * Imposta la modalità di playback corrente.
     * Fonde la logica di Loop (main) con l'inizializzazione dello Shuffle.
     */
    public void setPlaybackMode(PlaybackMode mode) {
        this.currentMode = Objects.requireNonNull(mode, "mode non può essere null");

        if (mode == PlaybackMode.SEQUENTIAL) {
            this.playbackStrategy = new SequentialPlaybackStrategy();
        } else if (mode == PlaybackMode.SHUFFLE) {
            // Recuperiamo le tracce attuali per mescolarle "dietro le quinte"
            List<Track> currentTracks = java.util.Collections.emptyList();
            if (currentPlaylist != null && playlistRepository != null) {
                currentTracks = playlistRepository.findTracksByPlaylistId(currentPlaylist.getId());
            } else if (currentTrack != null) {
                currentTracks = List.of(currentTrack);
            }
            
            // Inizializziamo la strategia Shuffle. 
            // NOTA: Non azzeriamo elapsedSeconds né currentTrack, quindi il playback NON si interrompe!
            this.playbackStrategy = new it.unisa.sad.playlistmanager.domain.model.ShufflePlaybackStrategy(currentTracks);
        }
    }

    public void enableSingleTrackLoopMode() {
        setPlaybackMode(PlaybackMode.REPEAT_ONE);
    }

    public void disableSingleTrackLoopMode() {
        setPlaybackMode(PlaybackMode.SEQUENTIAL);
    }

    public Track getCurrentTrack() {
        return currentTrack;
    }

    public Playlist getCurrentPlaylist() {
        return currentPlaylist;
    }

    public int getElapsedSeconds() {
        return elapsedSeconds;
    }

    public int getCurrentQueueIndex() {
        return currentQueue == null ? -1 : currentQueue.getCurrentIndex();
    }   

    public void tick() {
        if (currentState != PlaybackState.PLAYING || currentTrack == null) {
            return;
        }

        if (elapsedSeconds < currentTrack.getDuration()) {
            elapsedSeconds++;
        }

        if (elapsedSeconds >= currentTrack.getDuration()) {
            handleTrackCompleted();
        }
    }   

    public void playPlaylist(String playlistId) {
        if (playlistRepository == null) {
            throw new ValidationException("PlaylistRepository non inizializzato.");
        }

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

    public void playPlaylist(Playlist playlist, List<Track> tracks) {
        if (playlist == null || tracks == null || tracks.isEmpty()) {
            throw new IllegalArgumentException("Playlist o tracce non valide.");
        }

        currentPlaylist = playlist;
        currentQueue = new PlaybackQueue(tracks, PlaybackSource.PLAYLIST);
        currentTrack = currentQueue.getCurrentTrack();
        currentMode = PlaybackMode.SEQUENTIAL;
        playbackStrategy = new SequentialPlaybackStrategy();
        currentState = PlaybackState.PLAYING;
        elapsedSeconds = 0;
    }

    public void pause() {
        if (currentState == PlaybackState.PLAYING) {
            currentState = PlaybackState.PAUSED;
        }
    }

    public void skipToNext() {
        if (currentQueue == null || currentQueue.isEmpty()) {
            return;
        }

        PlaybackStrategy strategy = getStrategyForMode(currentMode);
        Track nextTrack = strategy.getNextTrack(currentQueue);

        if (nextTrack != null) {
            int nextIndex = currentQueue.getTracks().indexOf(nextTrack);
            currentQueue.setCurrentIndex(nextIndex);
            this.currentTrack = nextTrack;
        } else {
            if (currentMode == PlaybackMode.SEQUENTIAL) {
                this.currentState = PlaybackState.STOPPED;
                this.currentTrack = null;
            }
        }
    }

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
     * Risolve dinamicamente la strategia di riproduzione in base alla modalità corrente.
     * Integrato con la stateful strategy dello Shuffle.
     */
    private PlaybackStrategy getStrategyForMode(PlaybackMode mode) {
        if (mode == PlaybackMode.SHUFFLE) {
            return this.playbackStrategy;
        }
        switch (mode) {
            case SEQUENTIAL:
                return new SequentialPlaybackStrategy();
            default:
                return new SequentialPlaybackStrategy();
        }
    }

    public PlaybackSnapshot getSnapshot() {
        return new PlaybackSnapshot(getCurrentState(), getCurrentTrack(), getCurrentMode(), getElapsedSeconds());
    }

    public void handleDeletedTrack(String trackId) {
        if (currentTrack != null && currentTrack.getId().equals(trackId)) {
            currentTrack = null;
            currentQueue = null;
            currentState = PlaybackState.STOPPED;
            elapsedSeconds = 0;
        }
    }

    public void handleDeletedPlaylist(String playlistId) {
        if (currentPlaylist != null && currentPlaylist.getId().equals(playlistId)) {
            currentPlaylist = null;
            currentQueue = null;
            currentTrack = null;
            currentState = PlaybackState.STOPPED;
            elapsedSeconds = 0;
        }
    }

    public PlaybackSnapshot handleTrackCompleted() {
        if (currentTrack == null) {
            currentState = PlaybackState.STOPPED;
            return getSnapshot();
        }
        switch (currentMode) {
            case REPEAT_ONE:
                currentState = PlaybackState.PLAYING;
                elapsedSeconds = 0;
                break;
            case REPEAT_ALL:
            case SHUFFLE:
            case SEQUENTIAL:
            default:
                currentState = PlaybackState.STOPPED;
                currentTrack = null;
                break;
        }
        return getSnapshot();
    }
}