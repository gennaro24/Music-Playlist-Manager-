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
 * * @version 1.1
 */
public class PlaybackService {
    private PlaybackState currentState = PlaybackState.STOPPED;
    private PlaybackMode currentMode = PlaybackMode.SEQUENTIAL;
    private Track currentTrack = null;
    private Playlist currentPlaylist = null;
    private PlaybackQueue currentQueue = null;
    private final PlaylistRepository playlistRepository;
    private int elapsedSeconds = 0;


    public PlaybackService(PlaylistRepository playlistRepository) {
        this.playlistRepository = Objects.requireNonNull(
                playlistRepository,
                "playlistRepository non può essere null"
        );
    }

    public PlaybackService() {
        this.playlistRepository = null;
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
     * Imposta la modalità di playback corrente.
     * 
     * @param mode La modalità di playback da impostare.
     * @throws ValidationException se la modalità di playback è nulla.
     */
    public void setPlaybackMode(PlaybackMode mode) {
        this.currentMode = Objects.requireNonNull(mode, "mode non può essere null");
    }

    /**
     * Attiva la modalità di loop sulla singola traccia corrente (REPEAT_ONE).
     */
    public void enableSingleTrackLoopMode() {
        setPlaybackMode(PlaybackMode.REPEAT_ONE);
    }

    /**
     * Disattiva il loop sulla singola traccia tornando alla modalità sequenziale.
     */
    public void disableSingleTrackLoopMode() {
        setPlaybackMode(PlaybackMode.SEQUENTIAL);
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
            //se la traccia è finita, gestisci il completamento della traccia
            handleTrackCompleted();
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
        currentState = PlaybackState.PLAYING;
        elapsedSeconds = 0;
    }

    /**
     * Overload di compatibilità per i test che avviano il playback passando
     * direttamente playlist e tracce già risolte.
     *
     * @param playlist playlist da riprodurre
     * @param tracks tracce della playlist in ordine
     * @throws IllegalArgumentException se playlist o tracce non sono validi
     */
    public void playPlaylist(Playlist playlist, List<Track> tracks) {
        if (playlist == null || tracks == null || tracks.isEmpty()) {
            throw new IllegalArgumentException("Playlist o tracce non valide.");
        }

        currentPlaylist = playlist;
        currentQueue = new PlaybackQueue(tracks, PlaybackSource.PLAYLIST);
        currentTrack = currentQueue.getCurrentTrack();
        currentMode = PlaybackMode.SEQUENTIAL;
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
     * Avanza alla traccia successiva nella coda delegando il calcolo alla strategia attiva.
     */
    public void skipToNext() {
        // Se non c'è una coda attiva o è vuota, l'operazione di skip è un no-op
        if (currentQueue == null || currentQueue.isEmpty()) {
            return;
        }

        // Delega alla strategia attiva il calcolo della prossima traccia
        PlaybackStrategy strategy = getStrategyForMode(currentMode);
        Track nextTrack = strategy.getNextTrack(currentQueue);

        if (nextTrack != null) {
            // Esiste una traccia successiva. La impostiamo mantenendo lo stato corrente
            int nextIndex = currentQueue.getTracks().indexOf(nextTrack);
            currentQueue.setCurrentIndex(nextIndex);
            this.currentTrack = nextTrack;
            // Lo stato (PLAYING o PAUSED) viene volutamente mantenuto inalterato
        } else {
            // Gestisce lo skip dall'ultima traccia in modalità SEQUENTIAL portando lo stato a STOPPED
            if (currentMode == PlaybackMode.SEQUENTIAL) {
                this.currentState = PlaybackState.STOPPED;
                this.currentTrack = null;
            }
            // (Nota: Altre modalità come REPEAT_ALL gestiranno il loop restituendo una traccia non nulla)
        }
    }

    /**
     * Avvia il playback della traccia indicata come traccia singola.
     * Inizializza una coda a traccia singola.
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
        currentState = PlaybackState.PLAYING;
        elapsedSeconds = 0;
    }

    /**
     * Risolve dinamicamente la strategia di riproduzione in base alla modalità corrente.
     * * @param mode La modalità di riproduzione attiva
     * @return L'istanza concreta di PlaybackStrategy
     */
    private PlaybackStrategy getStrategyForMode(PlaybackMode mode) {
        switch (mode) {
            case SEQUENTIAL:
                return new SequentialPlaybackStrategy();
            // Le altre strategie (Shuffle, RepeatOne, RepeatAll) verranno mappate qui dai rispettivi assegnatari
            default:
                return new SequentialPlaybackStrategy();
        }
    }

    /**
     * Restituisce una fotografia dello stato corrente del player.
     * Risolve il Task T-156 e T-157 della US-17.
     *
     * @return snapshot immutabile del playback (DTO)
     */
    public PlaybackSnapshot getSnapshot() {
        return new PlaybackSnapshot(getCurrentState(), getCurrentTrack());
    }

    /**
     * Gestisce il caso in cui la traccia eliminata sia attualmente in playback. Se
     * la traccia eliminata è quella in questione
     * il playback viene fermato.
     *
     * @param trackId l'identificativo della traccia eliminata
     */
    public void handleDeletedTrack(String trackId) {
        if (currentTrack != null && currentTrack.getId().equals(trackId)) {
            currentTrack = null;
            currentQueue = null;
            currentState = PlaybackState.STOPPED;
            elapsedSeconds = 0;
        }
    }

    /**
     * Gestisce il caso in cui la playlist eliminata sia quella attualmente in playback.
     * In questo caso il playback viene fermato e la playlist corrente viene azzerata.
     *
     * @param playlistId l'identificativo della playlist eliminata
     */
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
     * Da chiamare quando la traccia corrente termina.
     * @return Lo snapshot corrente del playback.
     */
    public PlaybackSnapshot handleTrackCompleted() {
        //se non ho una traccia corrente, stoppo il playback
        if (currentTrack == null) {
            //imposto lo stato di stop
            currentState = PlaybackState.STOPPED;
            //ritorno lo snapshot corrente
            return getSnapshot();
        }
        //switch per la modalità di playback
        switch (currentMode) {
            //se la modalità è repeat one, riparte dalla traccia corrente
            case REPEAT_ONE:
                // stessa traccia, riparte da capo, con lo stato di playing e timer resettato
                currentState = PlaybackState.PLAYING;
                elapsedSeconds = 0;
                break;
            case REPEAT_ALL:
            case SHUFFLE:
            case SEQUENTIAL:
            default:
                // per ora fallback: stop (finché non implementi skip/playlist index)
                currentState = PlaybackState.STOPPED;
                currentTrack = null;
                break;
        }
        return getSnapshot();
    }
}
