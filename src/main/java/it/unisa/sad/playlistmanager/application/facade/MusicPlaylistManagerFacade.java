package it.unisa.sad.playlistmanager.application.facade;

import it.unisa.sad.playlistmanager.application.service.TrackService;
import it.unisa.sad.playlistmanager.domain.model.Track;
import java.util.List;
import java.util.ArrayList;
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
    

    private final List<Track> mockCatalog;
    private final List<Playlist> mockPlaylists;

    /**
     * Costruttore della Facade. Inietta le dipendenze dei servizi necessari.
     *
     * @param trackService Il servizio incaricato della logica di business delle tracce.
     */
    public MusicPlaylistManagerFacade(TrackService trackService) {
        this.trackService = trackService;
        this.mockCatalog = new ArrayList<>();
        this.mockPlaylists = new ArrayList<>();
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
        mockCatalog.add(new Track(null, title, author, duration, genre, year));
        return this.trackService.addTrack(title, author, duration, genre, year);
    }

    public Track addTrack2(String title, String author, int duration, String genre, int year) {        Track newTrack = new Track(null, title, author, duration, genre, year);
        mockCatalog.add(newTrack);
        System.out.println("[TEST UI] Traccia catturata dal Form e inserita nella lista: " + title);
        return newTrack;
    }

    public List<Track> getAllTracks2() {
        
        // Generazione di istanze di test conformi al costruttore del Domain Model
        mockCatalog.add(new Track(null, "Bohemian Rhapsody", "Queen", 355, "Rock", 1975));
        mockCatalog.add(new Track(null, "Starboy", "The Weeknd", 230, "Pop", 2016));
        mockCatalog.add(new Track(null, "Hotel California", "Eagles", 390, "Classic Rock", 1976));
        mockCatalog.add(new Track(null, "Shape of You", "Ed Sheeran", 233, "Pop", 2017));
        
        return mockCatalog;
    }


    public void addPlaylist(String name) {
        Playlist newPlaylist = new Playlist(name);
        mockPlaylists.add(newPlaylist);
    }

    public List<Playlist> getAllPlaylists2() {
        return mockPlaylists;
    }
}
