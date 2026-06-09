package it.unisa.sad.playlistmanager.application.facade;

import java.util.List;
import it.unisa.sad.playlistmanager.domain.model.PlaybackSnapshot;
import it.unisa.sad.playlistmanager.application.exceptions.TrackNotFoundException;
import it.unisa.sad.playlistmanager.application.service.PlaybackService;
import it.unisa.sad.playlistmanager.application.service.PlaylistService;
import it.unisa.sad.playlistmanager.application.service.TrackService;
import it.unisa.sad.playlistmanager.domain.model.Playlist;
import it.unisa.sad.playlistmanager.domain.model.Track;

/**
 * Facciata principale dell'applicazione (Facade Pattern).
 * Fornisce un'interfaccia unificata e semplificata per il Presentation Layer,
 * centralizzando l'accesso a tutti i servizi del modulo Application.
 * @version 1.0
 */
public class MusicPlaylistManagerFacade {

    /** Riferimento al servizio applicativo per la gestione delle tracce. */
    private final TrackService trackService;
    private final PlaylistService playlistService;
    private final PlaybackService playbackService;

    /**
     * Costruttore della Facade. Inietta le dipendenze dei servizi necessari.
     *
     * @param trackService Il servizio incaricato della logica di business delle tracce.
     * @param playlistService Il servizio incaricato della logica di business delle playlist.
     * @param playbackService Il servizio incaricato della logica di business del playback.
     */
    public MusicPlaylistManagerFacade(TrackService trackService, PlaylistService playlistService, PlaybackService playbackService) {
        this.playbackService = playbackService;
        this.trackService = trackService;
        this.playlistService = playlistService;
    }

    /**
     * Espone al Presentation Layer la funzionalità di aggiunta di una nuova traccia nel catalogo.
     * Svolge il ruolo di pass-through verso il servizio specializzato {@link TrackService}.
     *
     * @param title    Il titolo della canzone da aggiungere.
     * @param author   L'artista della canzone.
     * @param duration La durata complessiva in secondi.
     * @param genre    Il genere della canzone.
     * @param year     L'anno di pubblicazione.
     * @return L'oggetto {@link Track} creato, validato e salvato.
     * @throws IllegalArgumentException Se i parametri violano le regole di validazione del dominio.
     */
    public Track addTrack(String title, String author, int duration, String genre, int year) {
        // Il pattern Facade si limita a delegare l'operazione al servizio competente
        return this.trackService.addTrack(title, author, duration, genre, year);
    }

    /**
     * Centralizza l'accesso al caso d'uso di creazione di una playlist.
     * @param name Il nome della playlist.
     * @return La playlist creata.
     */
    public Playlist createPlaylist(String name) {
        return this.playlistService.createPlaylist(name);
    }

    /**
     * Espone al Presentation Layer la funzionalità di eliminazione di una playlist.
     * @param playlistId L'identificativo unico della playlist da eliminare.
     * @return La playlist eliminata.
     */
    public Playlist deletePlaylist(String playlistId) {
        playbackService.handleDeletedPlaylist(playlistId);
        return this.playlistService.deletePlaylist(playlistId);
    }

    /**
     * Espone al Presentation Layer l'elenco completo di tutte le tracce presenti nel catalogo.
     * Risolve il Task T-11 della prima sprint.
     *
     * @return Una lista contenente tutte le tracce musicali disponibili.
     */
    public List<Track> getAllTracks() {
        // Delega del pass-through verso il servizio di competenza
        return this.trackService.getAllTracks();
    }

    /**
     * Espone al Presentation Layer l'elenco completo di tutte le playlist configurate.
     * Risolve il Task T-25 della prima sprint.
     *
     * @return Una lista contenente tutte le playlist caricate dal modulo persistence.
     */
    public List<Playlist> getAllPlaylists() {
        // Delega del pass-through verso il servizio di competenza
        return this.playlistService.getAllPlaylists();
    }

    /**
     * Fornisce l'accesso al dettaglio di una specifica playlist identificata da ID.
     * Consente alla UI di verificare la presenza di elementi e l'ordine delle tracce.
     * Risolve il Task T-25 della prima sprint.
     *
     * @param id L'identificativo univoco della risorsa.
     * @return La playlist corrispondente, o null se non trovata.
     */
    public Playlist getPlaylistById(String id) {
        // Delega del pass-through verso il servizio di competenza
        return this.playlistService.getPlaylistById(id);
    }


    /**
     * Centralizza ed espone alla UI il caso d'uso di aggiunta traccia a una playlist.
     *
     * @param playlistId Identificativo della playlist di destinazione.
     * @param trackId    Identificativo della traccia da aggiungere.
     */
    public void addTrackToPlaylist(String playlistId, String trackId) {
        this.playlistService.addTrackToPlaylist(playlistId, trackId);
    }

    /**
     * Espone al Presentation Layer la funzionalità di rimozione di una traccia da una playlist.
     * Agisce da puro pass-through verso il servizio applicativo competente.
     *
     * @param playlistId L'identificativo unico della playlist di riferimento.
     * @param trackId    L'identificativo unico della traccia da cancellare dalla playlist.
     * @throws IllegalArgumentException Se i parametri o le regole di business vengono violate.
     */
    public void removeTrackFromPlaylist(String playlistId, String trackId) {
        this.playlistService.removeTrackFromPlaylist(playlistId, trackId);
    }


    /**
     * Espone al Presentation Layer la funzionalità di recupero delle tracce associate a una playlist.
     * @param playlistId L'identificativo unico della playlist di riferimento.
     * @return Una lista di tracce associate alla playlist.
     */
    public List<Track> getTracksForPlaylist(String playlistId) {
        return playlistService.getTracksForPlaylist(playlistId);
    }
    // =====================METODI PER IL PLAYBACK=====================:
    

    
     /**
     * Espone al Presentation Layer la funzionalità di avvio del playback di una traccia specifica.
     * Recupera la traccia tramite il servizio TrackService e delega l'operazione al PlaybackService.
     * @return Una fotografia dello stato corrente del playback dopo l'avvio (PlaybackSnapshot).
     * @param trackId L'identificativo della traccia da riprodurre.
     * @throws TrackNotFoundException Se la traccia non esiste (propagata dal Service).
     */
    public PlaybackSnapshot playTrack(String trackId) {
        Track track = trackService.getTrackById(trackId);
        playbackService.playTrack(track);
        return getPlaybackSnapshot();
    }

    /**
     * Espone al Presentation Layer la funzionalità di pausa del playback.
     * Delega l'operazione al PlaybackService.
     * @return Una fotografia dello stato corrente del playback dopo la pausa (PlaybackSnapshot).
     */
    public PlaybackSnapshot pausePlayback(){
        playbackService.pause();
        return getPlaybackSnapshot();
    }
    /**
     * Espone al Presentation Layer la funzionalità di skip alla traccia successiva.
     * @return Una fotografia dello stato corrente del playback dopo lo skip (PlaybackSnapshot).
     */
    public PlaybackSnapshot skipToNext() {
        playbackService.skipToNext();
        return getPlaybackSnapshot();
    }
    
    
    /**
     * Restituisce l'unico DTO letto dalla UI per conoscere lo stato del player.
     * * @return Una fotografia dello stato corrente del playback (PlaybackSnapshot).
     */
    public PlaybackSnapshot getPlaybackSnapshot() {
        return playbackService.getSnapshot();
    }
    /**
     * Espone al presentation layer la funzionalità di modifica di una traccia.
     * @return la traccia modificata da ritornare alla UI.
     */
    public Track updateTrack(String trackId, Track updatedTrack) {
        return trackService.updateTrack(trackId, updatedTrack);
    }
    /**
     * Espone al presentation layer la funzionalità di eliminazione di una traccia.
     * Notifica il PlayBackService se la traccia corrente è in playback.
     * @return la traccia eliminata da ritornare alla UI.
     */    
    public Track deleteTrack(String trackId) {
        playbackService.handleDeletedTrack(trackId);
        return trackService.deleteTrack(trackId);
    }

}