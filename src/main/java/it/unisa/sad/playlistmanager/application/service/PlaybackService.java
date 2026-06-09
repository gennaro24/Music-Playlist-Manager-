package it.unisa.sad.playlistmanager.application.service;

import it.unisa.sad.playlistmanager.application.exceptions.TrackNotFoundException;
import it.unisa.sad.playlistmanager.application.exceptions.ValidationException;
import it.unisa.sad.playlistmanager.domain.model.PlaybackMode;
import it.unisa.sad.playlistmanager.domain.model.PlaybackQueue;
import it.unisa.sad.playlistmanager.domain.model.PlaybackSource;
import it.unisa.sad.playlistmanager.domain.model.PlaybackSnapshot;
import it.unisa.sad.playlistmanager.domain.model.PlaybackState;
import it.unisa.sad.playlistmanager.domain.model.Track;
import it.unisa.sad.playlistmanager.domain.model.Playlist;
import it.unisa.sad.playlistmanager.domain.strategy.PlaybackStrategy;
import it.unisa.sad.playlistmanager.domain.strategy.SequentialPlaybackStrategy;

import java.util.ArrayList;
import java.util.List;

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
        if (mode == null) {
            throw new ValidationException("La modalità di playback non può essere nulla.");
        }
        this.currentMode = mode;
    }

    /**
     * Imposta la modalità di playback corrente e aggiorna implicitamente lo snapshot.
     * Risolve parte del Task T-157 (aggiornamento dopo cambio modalità).
     *
     * @param mode la nuova modalità di playback da impostare
     */
    public void setCurrentMode(PlaybackMode mode) {
        this.currentMode = mode;
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

        List<Track> singleTrackList = new ArrayList<>();
        singleTrackList.add(track);
        this.currentQueue = new PlaybackQueue(singleTrackList, PlaybackSource.SINGLE);
        
        this.currentTrack = track;
        this.currentState = PlaybackState.PLAYING;
    }

    /**
     * Avvia il playback di una playlist completa, inizializzando la coda di riproduzione.
     * Metodo essenziale per supportare lo skip multi-traccia e i relativi test.
     *
     * @param playlist La playlist da riprodurre
     * @param tracks I brani ordinati che compongono la playlist al momento dell'avvio
     * @throws IllegalArgumentException se la playlist o la lista tracce sono nulle/vuote
     */
    public void playPlaylist(Playlist playlist, List<Track> tracks) {
        if (playlist == null || tracks == null || tracks.isEmpty()) {
            throw new IllegalArgumentException("Playlist o tracce non valide.");
        }
        this.currentPlaylist = playlist;
        this.currentQueue = new PlaybackQueue(tracks, PlaybackSource.PLAYLIST);
        this.currentTrack = this.currentQueue.getCurrentTrack();
        this.currentState = PlaybackState.PLAYING;
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
            currentState = PlaybackState.STOPPED;
        }
    }

    /**
     * Gestisce il caso in cui la playlist eliminata sia attualmente in riproduzione.
     * * @param playlistId l'identificativo della playlist eliminata
     */
    public void handleDeletedPlaylist(String playlistId) {
        if (currentPlaylist != null && currentPlaylist.getId().equals(playlistId)) {
            currentPlaylist = null;
            currentQueue = null;
            currentTrack = null;
            currentState = PlaybackState.STOPPED;
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
                // stessa traccia, riparte da capo
                currentState = PlaybackState.PLAYING;
                //TODO: Se hai elapsedSeconds/lastTickMillis, qui fai reset a 0
                break;
            case REPEAT_ALL:
            case SHUFFLE:
            case SEQUENTIAL:
            default:
                // per ora fallback: stop (finché non implementi skip/playlist index)
                currentState = PlaybackState.STOPPED;
                break;
        }
        return getSnapshot();
    }
}
