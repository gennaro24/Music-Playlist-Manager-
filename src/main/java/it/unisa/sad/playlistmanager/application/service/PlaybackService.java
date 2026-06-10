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
 * Gestisce lo stato logico del playback e coordina le strategie di
 * riproduzione.
 * Agisce come Contesto per il pattern Strategy.
 * 
 * @version 1.3
 */
public class PlaybackService {
    private PlaybackState currentState = PlaybackState.STOPPED;
    private PlaybackMode currentMode = PlaybackMode.SEQUENTIAL;
    private Track currentTrack = null;
    private Playlist currentPlaylist = null;
    private PlaybackQueue currentQueue = null;
    private final PlaylistRepository playlistRepository;
    private int elapsedSeconds = 0;

    // Mantiene il riferimento di stato della strategia corrente (US-15)
    private PlaybackStrategy playbackStrategy = new SequentialPlaybackStrategy();

    public PlaybackService(PlaylistRepository playlistRepository) {
        this.playlistRepository = Objects.requireNonNull(
                playlistRepository,
                "playlistRepository non può essere null");
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
     * Fonde la logica di Loop con l'inizializzazione dello Shuffle senza
     * interrompere il brano.
     */
    public void setPlaybackMode(PlaybackMode mode) {
        this.currentMode = Objects.requireNonNull(mode, "mode non può essere null");

        if (mode == PlaybackMode.SEQUENTIAL) {
            this.playbackStrategy = new SequentialPlaybackStrategy();
        } else if (mode == PlaybackMode.SHUFFLE) {
            List<Track> currentTracks = java.util.Collections.emptyList();

            // SOLUZIONE ARCHITETTURALE: Leggiamo dalla coda di riproduzione correntemente
            // attiva
            if (currentQueue != null && !currentQueue.isEmpty()) {
                currentTracks = currentQueue.getTracks();
            } else if (currentTrack != null) {
                currentTracks = List.of(currentTrack);
            }

            this.playbackStrategy = new it.unisa.sad.playlistmanager.domain.strategy.ShufflePlaybackStrategy(
                    currentTracks);
        }
    }

    /**
     * Avvia il playback dell'intero catalogo musicale, preservando l'eventuale
     * stato pre-esistente dello Shuffle.
     */
    public void playCatalog(List<Track> tracks) {
        if (tracks == null || tracks.isEmpty()) {
            throw new ValidationException("Impossibile avviare un catalogo vuoto.");
        }
        this.currentPlaylist = null;
        this.currentQueue = new PlaybackQueue(tracks, PlaybackSource.CATALOG);

        // FIX: Preserviamo la modalità Shuffle se già attiva nella UI
        if (this.currentMode == PlaybackMode.SHUFFLE) {
            this.playbackStrategy = new it.unisa.sad.playlistmanager.domain.strategy.ShufflePlaybackStrategy(tracks);
            Track firstTrack = this.playbackStrategy.getNextTrack(currentQueue);
            if (firstTrack != null) {
                int nextIndex = currentQueue.getTracks().indexOf(firstTrack);
                currentQueue.setCurrentIndex(nextIndex);
                this.currentTrack = firstTrack;
            } else {
                this.currentTrack = currentQueue.getCurrentTrack();
            }
        //preservo la modalità Repeat_one
        }else if(this.currentMode == PlaybackMode.REPEAT_ONE){
            this.currentTrack = currentQueue.getCurrentTrack();
        }else if(this.currentMode == PlaybackMode.REPEAT_ALL){
            this.playbackStrategy = new RepeatAllPlaybackStrategy();
            this.currentTrack = currentQueue.getCurrentTrack();
        //preserviamo la modalità loop all
        }else if(this.currentMode == PlaybackMode.REPEAT_ALL){
            this.playbackStrategy = new RepeatAllPlaybackStrategy();
            this.currentTrack = currentQueue.getCurrentTrack();
        }else if(this.currentMode == PlaybackMode.SEQUENTIAL){
            this.playbackStrategy = new SequentialPlaybackStrategy();
            this.currentTrack = currentQueue.getCurrentTrack();
        }else{
            this.currentMode = PlaybackMode.SEQUENTIAL;
            this.playbackStrategy = new SequentialPlaybackStrategy();
            this.currentTrack = currentQueue.getCurrentTrack();
        }

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

    /**
     * Avvia una playlist tramite identificativo ID, preservando l'eventuale stato
     * pre-esistente dello Shuffle.
     */
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

        this.currentPlaylist = playlist;
        this.currentQueue = new PlaybackQueue(tracks, PlaybackSource.PLAYLIST);
        applyCurrentModeToQueue(tracks);
        this.currentState = PlaybackState.PLAYING;
        this.elapsedSeconds = 0;
    }

    /**
     * Avvia una playlist passando l'entità di dominio, preservando la modalità attiva (usato nei test).
     */
    public void playPlaylist(Playlist playlist, List<Track> tracks) {
        if (playlist == null || tracks == null || tracks.isEmpty()) {
            throw new IllegalArgumentException("Playlist o tracce non valide.");
        }

        this.currentPlaylist = playlist;
        this.currentQueue = new PlaybackQueue(tracks, PlaybackSource.PLAYLIST);
        applyCurrentModeToQueue(tracks);
        this.currentState = PlaybackState.PLAYING;
        this.elapsedSeconds = 0;
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
            this.elapsedSeconds = 0; // Il timer si azzera correttamente nel core di business
        } else {
            // Gestiamo la transizione pulita di fermo sia in modalità sequenziale che in
            // shuffle
            if (currentMode == PlaybackMode.SEQUENTIAL || currentMode == PlaybackMode.SHUFFLE) {
                this.currentState = PlaybackState.STOPPED;
                this.currentTrack = null;
                this.elapsedSeconds = 0;
            }
            if (currentMode == PlaybackMode.REPEAT_ALL) {
                // se sono arrivato alla fine della coda, torno alla prima traccia
                if (currentQueue.getCurrentIndex() == currentQueue.getTracks().size() - 1) {
                    currentQueue.setCurrentIndex(0);
                    this.currentTrack = currentQueue.getCurrentTrack();
                } else {
                    currentQueue.setCurrentIndex(currentQueue.getCurrentIndex() + 1);
                    this.currentTrack = currentQueue.getCurrentTrack();
                }
            }
        }
    }

    public void playTrack(Track track) {
        if (track == null) {
            throw new TrackNotFoundException("Track non trovata.");
        }

        // Se la traccia è la stessa ed è in riproduzione o in pausa, gestisce la
        // ripresa
        if (currentTrack != null && currentTrack.getId().equals(track.getId())) {
            if (currentState == PlaybackState.PAUSED || currentState == PlaybackState.STOPPED) {
                currentState = PlaybackState.PLAYING;
            }
            return;
        }

        // Configurazione per una traccia singola completamente nuova
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
     * Inizializza la prima traccia della nuova coda rispettando la modalità
     * corrente (SHUFFLE, REPEAT_ONE, REPEAT_ALL, SEQUENTIAL).
     * Non modifica mai currentMode: preserva sempre l'impostazione dell'utente.
     */
    private void applyCurrentModeToQueue(List<Track> tracks) {
        if (this.currentMode == PlaybackMode.SHUFFLE) {
            this.playbackStrategy = new it.unisa.sad.playlistmanager.domain.strategy.ShufflePlaybackStrategy(tracks);
            Track firstTrack = this.playbackStrategy.getNextTrack(currentQueue);
            if (firstTrack != null) {
                currentQueue.setCurrentIndex(currentQueue.getTracks().indexOf(firstTrack));
                this.currentTrack = firstTrack;
            } else {
                this.currentTrack = currentQueue.getCurrentTrack();
            }
        } else {
            // SEQUENTIAL, REPEAT_ONE, REPEAT_ALL: la strategia si basa sulla coda
            // nell'ordine originale; la modalità rimane quella già impostata.
            this.playbackStrategy = new SequentialPlaybackStrategy();
            this.currentTrack = currentQueue.getCurrentTrack();
        }
    }

    /**
     * Risolve dinamicamente la strategia di riproduzione in base alla modalità
     * corrente.
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
                // resetto il timer
                elapsedSeconds = 0;
                skipToNext();
                break;
            case SEQUENTIAL:
            default:
                elapsedSeconds = 0;
                skipToNext();

                if (currentTrack != null) {
                    currentState = PlaybackState.PLAYING;
                } else {
                    currentState = PlaybackState.STOPPED;
                }
                break;
        }
        return getSnapshot();
    }

    public void enableRepeatAllMode() {
        currentMode = PlaybackMode.REPEAT_ALL;
    }

    public void disableRepeatAllMode() {
        currentMode = PlaybackMode.SEQUENTIAL;
    }
}