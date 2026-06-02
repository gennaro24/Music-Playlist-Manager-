package it.unisa.sad.playlistmanager.application.facade;

import java.util.List;

import it.unisa.sad.playlistmanager.application.service.PlaylistService;
import it.unisa.sad.playlistmanager.application.service.TrackService;
import it.unisa.sad.playlistmanager.domain.model.Playlist;
import it.unisa.sad.playlistmanager.domain.model.Track;
import it.unisa.sad.playlistmanager.domain.model.Playlist;

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

    /**
     * Costruttore della Facade. Inietta le dipendenze dei servizi necessari.
     *
     * @param trackService Il servizio incaricato della logica di business delle tracce.
     * @param playlistService Il servizio incaricato della logica di business delle playlist.
     */
    public MusicPlaylistManagerFacade(TrackService trackService, PlaylistService playlistService) {
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

}