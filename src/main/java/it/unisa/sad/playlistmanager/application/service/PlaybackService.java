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
import it.unisa.sad.playlistmanager.domain.strategy.RepeatAllPlaybackStrategy;
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
            List<Track> currentTracks = java.util.Collections.emptyList();
            
            // SOLUZIONE ARCHITETTURALE: Leggiamo dalla coda di riproduzione correntemente attiva!
            if (currentQueue != null && !currentQueue.isEmpty()) {
                currentTracks = currentQueue.getTracks(); // Prende le tracce già in memoria (niente DB!)
            } else if (currentTrack != null) {
                currentTracks = List.of(currentTrack);
            }
            
            this.playbackStrategy = new it.unisa.sad.playlistmanager.domain.strategy.ShufflePlaybackStrategy(currentTracks);
        }
    }

    public void playCatalog(List<Track> tracks) {
        if (tracks == null || tracks.isEmpty()) {
            throw new ValidationException("Impossibile avviare un catalogo vuoto.");
        }
        this.currentPlaylist = null; // Nessuna playlist associata
        this.currentQueue = new PlaybackQueue(tracks, PlaybackSource.CATALOG);
        this.currentTrack = currentQueue.getCurrentTrack();
        this.currentMode = PlaybackMode.SEQUENTIAL;
        this.playbackStrategy = new SequentialPlaybackStrategy();
        this.currentState = PlaybackState.PLAYING;
        this.elapsedSeconds = 0;
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

        // In PlaybackService.java, dentro skipToNext()
        if (nextTrack != null) {
            int nextIndex = currentQueue.getTracks().indexOf(nextTrack);
            currentQueue.setCurrentIndex(nextIndex);
            this.currentTrack = nextTrack;
            this.elapsedSeconds = 0;
        } else {
            // CORREZIONE: Gestiamo il fermo sia per la riproduzione sequenziale che per lo shuffle
            if (currentMode == PlaybackMode.SEQUENTIAL || currentMode == PlaybackMode.SHUFFLE) {
                this.currentState = PlaybackState.STOPPED;
                this.currentTrack = null;
                this.elapsedSeconds = 0;
            }
        }
    }

    public void playTrack(Track track) {
        if (track == null) {
            throw new TrackNotFoundException("Track non trovata.");
        }

        // Se la traccia è la stessa ed è in riproduzione o in pausa, non azzerare nulla!
        if (currentTrack != null && currentTrack.getId().equals(track.getId())) {
            if (currentState == PlaybackState.PAUSED || currentState == PlaybackState.STOPPED) {
                currentState = PlaybackState.PLAYING;
            }
            return;
        }

        // Configurazione per una traccia completamente nuova
        currentTrack = track;
        currentPlaylist = null;
        currentQueue = new PlaybackQueue(List.of(track), PlaybackSource.SINGLE);
        
        // Mantieni lo shuffle se l'utente lo ha attivato prima di lanciare la traccia
        if (currentMode != PlaybackMode.SHUFFLE) {
            currentMode = PlaybackMode.SEQUENTIAL;
            playbackStrategy = new SequentialPlaybackStrategy();
        }
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
            case REPEAT_ALL:
                return new RepeatAllPlaybackStrategy();
            // Le altre strategie (Shuffle, RepeatOne) verranno mappate qui dai rispettivi assegnatari
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
                
            case SHUFFLE:
            case REPEAT_ALL:
            case SEQUENTIAL:
            default:
                // 1. Reset dei secondi per il brano che sta per entrare
                elapsedSeconds = 0;
                
                // 2. Chiediamo alla strategia (Shuffle o Sequential) la prossima traccia
                skipToNext();
                
                // 3. Se c'è una nuova traccia (es. la playlist non è finita), la mettiamo in PLAYING
                if (currentTrack != null) {
                    currentState = PlaybackState.PLAYING;
                } else {
                    // Se le canzoni sono finite e non c'è REPEAT_ALL attivo, ci fermiamo
                    currentState = PlaybackState.STOPPED;
                }
                break;
        }
        return getSnapshot();
    }
}