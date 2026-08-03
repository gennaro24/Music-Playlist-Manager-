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
import it.unisa.sad.playlistmanager.domain.strategy.ShufflePlaybackStrategy;
import java.util.ArrayList;
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
        PlaybackMode requestedMode = Objects.requireNonNull(mode, "mode non può essere null");
        this.currentMode = requestedMode;

        if (mode == PlaybackMode.SEQUENTIAL) {
            this.playbackStrategy = new SequentialPlaybackStrategy();
        } else if (mode == PlaybackMode.SHUFFLE) {
            // Se la sorgente è single, la modalità di riproduzione deve essere sequenziale
            if (currentQueue != null && currentQueue.getSource() == PlaybackSource.SINGLE) {
                this.playbackStrategy = new SequentialPlaybackStrategy();
                return;
            }

            //se la coda non è vuota, prendo le tracce dalla coda, altrimenti prendo la traccia corrente
            List<Track> currentTracks = java.util.Collections.emptyList();
            // Se la coda non è vuota, prendo le tracce dalla coda, altrimenti prendo la traccia corrente
            if (currentQueue != null && !currentQueue.isEmpty()) {
                currentTracks = currentQueue.getTracks();
            } else if (currentTrack != null) {
                //se c'è una traccia in riproduzione, la prendo
                currentTracks = List.of(currentTrack);
            }

            this.playbackStrategy = new ShufflePlaybackStrategy(currentTracks);
        } else if (mode == PlaybackMode.REPEAT_ALL) {
            this.playbackStrategy = new RepeatAllPlaybackStrategy();
        }
    }

    /**
     * Avvia il playback dell'intero catalogo musicale preservando la modalità
     * selezionata prima dell'avvio.
     */
    public void playCatalog(List<Track> tracks) {
        if (tracks == null || tracks.isEmpty()) {
            throw new ValidationException("Impossibile avviare un catalogo vuoto.");
        }

        this.currentPlaylist = null;
        startQueue(tracks, PlaybackSource.CATALOG);
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

    public boolean isShuffleAvailable() {
        return true;
    }

    /**
     * Avanza di un "tick" il playback e restituisce lo snapshot aggiornato.
     *
     * @return snapshot aggiornato del playback
     */
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
     * Avvia una playlist tramite identificativo ID preservando la modalità
     * selezionata prima dell'avvio.
     */
    public void playPlaylist(String playlistId) {
        //se il repository non è inizializzato, lancio un'eccezione
        if (playlistRepository == null) {
            throw new ValidationException("PlaylistRepository non inizializzato.");
        }

        //cerco la playlist nel repository
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new PlaylistNotFoundException("Playlist non trovata."));

        //cerco le tracce nella playlist
        List<Track> tracks = playlistRepository.findTracksByPlaylistId(playlistId);

        //se la playlist è vuota, lancio un'eccezione
        if (tracks.isEmpty()) {
            throw new ValidationException("Impossibile avviare una playlist vuota.");
        }

        //imposto la playlist corrente
        this.currentPlaylist = playlist;
        //avvio la coda di riproduzione
        startQueue(tracks, PlaybackSource.PLAYLIST);
    }

    /**
     * Avvia una playlist passando l'entità di dominio e preservando la modalità
     * selezionata prima dell'avvio (usato nei test).
     */
    public void playPlaylist(Playlist playlist, List<Track> tracks) {
        if (playlist == null || tracks == null || tracks.isEmpty()) {
            throw new IllegalArgumentException("Playlist o tracce non valide.");
        }

        this.currentPlaylist = playlist;
        startQueue(tracks, PlaybackSource.PLAYLIST);
    }

    /**
     * Pausa il playback.
     */
    public void pause() {
        //se sto riproducendo, pauso
        if (currentState == PlaybackState.PLAYING) {
            currentState = PlaybackState.PAUSED;
        }
    }

    public void skipToNext() {
        //se la coda è vuota o la sorgente è null, non faccio nulla
        if (currentQueue == null || currentQueue.isEmpty()) {
            return;
        }
        //recupero la strategia di riproduzione in base alla modalità corrente
        PlaybackStrategy strategy = getStrategyForMode(currentMode);
        //vado alla prossima traccia
        Track nextTrack = strategy.getNextTrack(currentQueue);
        //se c'è una prossima traccia, imposto l'indice della traccia corrente
        if (nextTrack != null) {
            int nextIndex = currentQueue.getTracks().indexOf(nextTrack);
            currentQueue.setCurrentIndex(nextIndex);
            //imposto la traccia corrente
            this.currentTrack = nextTrack;
            //imposto il tempo trascorso
            this.elapsedSeconds = 0; // Il timer si azzera correttamente nel core di business
        } else {
            //se non c'è una prossima traccia, stoppo la riproduzione
            // Gestiamo la transizione pulita di fermo sia in modalità sequenziale che in
            // shuffle
            if (currentMode == PlaybackMode.SEQUENTIAL || currentMode == PlaybackMode.SHUFFLE) {
                this.currentState = PlaybackState.STOPPED;
                this.currentTrack = null;
                //imposto il tempo trascorso
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
            //se la modalità è repeat one, riavvio la stessa traccia
            if (currentMode == PlaybackMode.REPEAT_ONE) {
                currentQueue.setCurrentIndex(0);
                this.currentTrack = currentQueue.getCurrentTrack();
                this.elapsedSeconds = 0;
            }
        }
    }

    public void playTrack(Track track) {
        if (track == null) {
            throw new TrackNotFoundException("Track non trovata.");
        }

        // Se la traccia è la stessa, aggiorna i metadati e gestisce l'eventuale
        // ripresa senza ricreare la sorgente di playback.
        if (currentTrack != null && currentTrack.getId().equals(track.getId())) {
            refreshCurrentTrack(track);
            
            if (currentState == PlaybackState.PAUSED || currentState == PlaybackState.STOPPED) {
                currentState = PlaybackState.PLAYING;
            }
            return;
        }

        //imposto la traccia corrente
        currentTrack = track;
        //imposto la playlist a null perchè sto riproducendo una singola traccia
        currentPlaylist = null;
        //current queue è la coda di riproduzione corrente e la sorgente è SINGLE
        currentQueue = new PlaybackQueue(List.of(track), PlaybackSource.SINGLE);
        //imposto la strategia di riproduzione in base alla modalità corrente
        playbackStrategy = currentMode == PlaybackMode.SHUFFLE
                ? new SequentialPlaybackStrategy() //perche sto riproducendo una sola traccia
                : getStrategyForMode(currentMode); //altrimenti stratey corrente
        currentState = PlaybackState.PLAYING;
        elapsedSeconds = 0;
    }

    /**
     * 
     * @param updatedTrack
     */
    private void refreshCurrentTrack(Track updatedTrack) {
        //aggiorno la traccia corrente
        this.currentTrack = updatedTrack;

        //se la coda è vuota, non faccio nulla
        if (currentQueue == null || currentQueue.isEmpty()) {
            return;
        }

        //cerco la traccia nella coda
        List<Track> updatedTracks = new ArrayList<>(currentQueue.getTracks());
        int currentIndex = currentQueue.getCurrentIndex();
        int indexToUpdate = -1;

        //se la traccia è nella coda, aggiorno la traccia corrente
        if (currentIndex >= 0
                && currentIndex < updatedTracks.size()
                && updatedTracks.get(currentIndex).getId().equals(updatedTrack.getId())) {
            indexToUpdate = currentIndex;
        } else {
            //itero tutta la coda per trovare la traccia
            for (int i = 0; i < updatedTracks.size(); i++) {
                if (updatedTracks.get(i).getId().equals(updatedTrack.getId())) {
                    indexToUpdate = i;
                    break;
                }
            }
        }

        //se la traccia non è nella coda, non faccio nulla
        if (indexToUpdate == -1) {
            return;
        }

        //aggiorno la traccia nella coda
        updatedTracks.set(indexToUpdate, updatedTrack);

        //aggiorno la sorgente della coda
        PlaybackSource source = currentQueue.getSource();
        //creo una nuova coda con le tracce aggiornate
        currentQueue = new PlaybackQueue(updatedTracks, source);
        //imposto l'indice della traccia corrente
        currentQueue.setCurrentIndex(indexToUpdate);
    }

    /**
     * Inizializza una nuova coda senza sovrascrivere la modalità scelta dalla UI.
     */
    private void startQueue(List<Track> tracks, PlaybackSource source) {
        //creo una nuova coda con le tracce e la sorgente
        this.currentQueue = new PlaybackQueue(tracks, source);

        //se la modalità è shuffle, imposto la strategia di riproduzione
        if (currentMode == PlaybackMode.SHUFFLE) {
            this.playbackStrategy = new ShufflePlaybackStrategy(tracks);
            //vado alla prossima traccia
            Track firstTrack = playbackStrategy.getNextTrack(currentQueue);
            if (firstTrack != null) {
                //imposto l'indice della traccia corrente
                currentQueue.setCurrentIndex(currentQueue.getTracks().indexOf(firstTrack));
                //imposto la traccia corrente
                this.currentTrack = firstTrack;
            } else {
                //se non c'è una prossima traccia, imposto la traccia corrente
                this.currentTrack = currentQueue.getCurrentTrack();
            }
        } else {
            //imposto la strategia di riproduzione in base alla modalità corrente
            this.playbackStrategy = getStrategyForMode(currentMode);
            //imposto la traccia corrente
            this.currentTrack = currentQueue.getCurrentTrack();
        }

        //imposto lo stato di riproduzione
        this.currentState = PlaybackState.PLAYING;
        //imposto il tempo trascorso
        this.elapsedSeconds = 0;
    }

    /**
     * Risolve dinamicamente la strategia di riproduzione in base alla modalità corrente.
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
        if (currentQueue == null || currentQueue.isEmpty()) {
            return;
        }

        boolean isCurrent = currentTrack != null && currentTrack.getId().equals(trackId);
        boolean isInQueue = currentQueue.getTracks().stream()
                .anyMatch(track -> track.getId().equals(trackId));

        if (!isInQueue) {
            return;
        }

        if (isCurrent) {
            currentTrack = null;
            currentQueue = null;
            currentState = PlaybackState.STOPPED;
            elapsedSeconds = 0;
            return;
        }

        String currentTrackId = currentTrack != null ? currentTrack.getId() : null;
        PlaybackSource source = currentQueue.getSource();
        List<Track> updatedTracks = new ArrayList<>(currentQueue.getTracks());
        updatedTracks.removeIf(track -> track.getId().equals(trackId));

        if (updatedTracks.isEmpty()) {
            currentTrack = null;
            currentQueue = null;
            currentState = PlaybackState.STOPPED;
            elapsedSeconds = 0;
            return;
        }

        currentQueue = new PlaybackQueue(updatedTracks, source);

        if (currentTrackId == null) {
            currentTrack = currentQueue.getCurrentTrack();
            return;
        }

        for (int i = 0; i < updatedTracks.size(); i++) {
            if (updatedTracks.get(i).getId().equals(currentTrackId)) {
                currentQueue.setCurrentIndex(i);
                currentTrack = currentQueue.getCurrentTrack();
                break;
            }
        }

        if (currentMode == PlaybackMode.SHUFFLE) {
            playbackStrategy = new ShufflePlaybackStrategy(updatedTracks);
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

    /**
     * Gestione dell'evento di completamento di una traccia.
     * @return lo snapshot aggiornato del playback
     */
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
