package it.unisa.sad.playlistmanager.application.facade;

import it.unisa.sad.playlistmanager.application.service.TrackService;
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


    /**
     * Costruttore della Facade. Inietta le dipendenze dei servizi necessari.
     *
     * @param trackService Il servizio incaricato della logica di business delle tracce.
     */
    public MusicPlaylistManagerFacade(TrackService trackService) {
        this.trackService = trackService;
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
}