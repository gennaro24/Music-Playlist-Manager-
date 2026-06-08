package it.unisa.sad.playlistmanager.application.service;

import it.unisa.sad.playlistmanager.application.exceptions.TrackNotFoundException;
import it.unisa.sad.playlistmanager.application.exceptions.ValidationException;
import it.unisa.sad.playlistmanager.domain.model.Track;
import it.unisa.sad.playlistmanager.persistence.repository.TrackRepository;
import java.util.List;

/**
 * Servizio applicativo responsabile del coordinamento dei casi d'uso legati alle tracce.
 * Funge da intermediario tra il Presentation Layer e il Domain/Persistence Layer.
 * * @version 1.1
 */

public class TrackService {

    /** Riferimento all'interfaccia di persistenza per il disaccoppiamento (DIP). */
    private final TrackRepository trackRepository;

    /**
     * Costruttore del servizio. Inietta la dipendenza del repository.
     *
     * @param trackRepository L'astrazione del database da utilizzare per le operazioni CRUD.
     */
    public TrackService(TrackRepository trackRepository) {
        this.trackRepository = trackRepository;
    }

    /**
     * Coordina il caso d'uso di aggiunta di una nuova traccia nel catalogo.
     * Crea l'oggetto di dominio attivando la validazione e ne richiede il salvataggio persistente.
     *
     * @param title    Il titolo della canzone da aggiungere.
     * @param author   L'artista della canzone.
     * @param duration La durata complessiva in secondi.
     * @param genre    Il genere della canzone.
     * @param year     L'anno di pubblicazione.
     * @return L'oggetto {@link Track} correttamente istanziato e salvato.
     * @throws IllegalArgumentException Se i dati forniti violano le regole di validazione del dominio.
     */
    public Track addTrack(String title, String author, int duration, String genre, int year) {
        // 1. Istanziazione e auto-validazione nel modello di dominio
        Track newTrack = new Track(null, title, author, duration, genre, year);

        // 2. Persistenza tramite interfaccia astratta
        trackRepository.save(newTrack);

        // 3. Ritorno dell'oggetto creato per l'aggiornamento della UI
        return newTrack;
    }

    /**
     * Coordina il caso d'uso di recupero e visualizzazione dell'intero catalogo musicale.
     * Risolve il Task T-10 della prima sprint.
     *
     * @return Una lista di tutti gli oggetti {@link Track} registrati nel sistema.
     */
    public List<Track> getAllTracks() {
        // Delega l'estrazione totale al repository astratto
        return this.trackRepository.findAll();
    }
    /**
     * Ritorna la traccia cercata tramite il suo ID. 
     * Siccome il metodo findById del repository ritorna un Optional, è necessario gestire il caso in cui la traccia non esista.
     * @return La traccia cercata.
     * @throws TrackNotFoundException se la traccia non esiste.
     */
    public Track getTrackById(String trackId) {

        return trackRepository.findById(trackId).orElseThrow(() -> new TrackNotFoundException("Track non trovata."));
    }


    /**
     * Coordina il caso d'uso di modifica di una traccia.
     * Ritorna la traccia aggiornata se nel livello inferiore (Repository) viene effettivamente aggiornata.
     * @throws ValidationException nel caso in cui la traccia da modificare abbia id nullo, vuoto o se la traccia nuova modificata sia nulla.
     * @throws TrackNotFoundException nel caso in cui la traccia da modificare non esista all'interno del sistema di persistenza.
     */
    public Track updateTrack(String trackId, Track newTrack){
        if (null == trackId || trackId.trim().isEmpty()) throw new ValidationException("L'id della Track da modificare è nullo o vuoto.");
        if (null == newTrack ) throw new ValidationException("la Track modificata è nulla");
        
        Track existingTrack = getTrackById(trackId);

            Track updatedTrack = new Track(
                    existingTrack.getId(),
                    newTrack.getTitle(),
                    newTrack.getAuthor(),
                    newTrack.getDuration(),
                    newTrack.getGenre(),
                    newTrack.getYear()
            );

            return trackRepository.update(updatedTrack).orElseThrow(() -> new TrackNotFoundException("La traccia da modificare non è stata trovata."));

            
    }
}