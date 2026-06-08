package it.unisa.sad.playlistmanager.application.service;

import java.util.List;

import it.unisa.sad.playlistmanager.application.exceptions.TrackNotFoundException;
import it.unisa.sad.playlistmanager.application.exceptions.ValidationException;
import it.unisa.sad.playlistmanager.domain.model.Track;
import it.unisa.sad.playlistmanager.persistence.repository.TrackRepository;

/**
 * Servizio applicativo responsabile del coordinamento dei casi d'uso legati alle tracce.
 *
 * Funge da intermediario tra il Presentation Layer e il Domain/Persistence Layer.
 * La validazione dei campi della traccia è delegata al modello di dominio {@link Track}.
 *
 * @version 1.2
 */
public class TrackService {

    private final TrackRepository trackRepository;

    /**
     * Costruttore del servizio con Dependency Injection del repository.
     *
     * @param trackRepository repository astratto per la persistenza delle tracce
     */
    public TrackService(TrackRepository trackRepository) {
        this.trackRepository = trackRepository;
    }

    /**
     * Crea e salva una nuova traccia nel catalogo.
     *
     * @param title titolo della traccia
     * @param author autore/artista della traccia
     * @param duration durata in secondi
     * @param genre genere musicale
     * @param year anno di pubblicazione
     * @return traccia creata e salvata
     */
    public Track addTrack(String title, String author, int duration, String genre, int year) {
        Track newTrack = new Track(null, title, author, duration, genre, year);

        trackRepository.save(newTrack);

        return newTrack;
    }

    /**
     * Recupera tutte le tracce presenti nel catalogo.
     *
     * @return lista completa delle tracce, eventualmente vuota
     */
    public List<Track> getAllTracks() {
        return trackRepository.findAll();
    }

    /**
     * Recupera una traccia tramite id.
     *
     * @param trackId id della traccia da cercare
     * @return traccia trovata
     * @throws ValidationException se l'id è nullo o vuoto
     * @throws TrackNotFoundException se la traccia non esiste
     */
    public Track getTrackById(String trackId) {
        return getExistingTrack(trackId);
    }

    /**
     * Modifica una traccia esistente mantenendo invariato il suo id.
     *
     * @param trackId id della traccia da modificare
     * @param newTrack traccia contenente i nuovi valori
     * @return traccia aggiornata
     * @throws ValidationException se l'id è nullo/vuoto o se la nuova traccia è nulla
     * @throws TrackNotFoundException se la traccia da modificare non esiste
     */
    public Track updateTrack(String trackId, Track newTrack) {
        validateTrackInput(newTrack);

        Track existingTrack = getExistingTrack(trackId);
        Track updatedTrack = buildUpdatedTrack(existingTrack, newTrack);

        return trackRepository.update(updatedTrack)
                .orElseThrow(() -> new TrackNotFoundException("La traccia da modificare non è stata trovata."));
    }

    /**
     * Elimina una traccia dal catalogo.
     *
     * @param trackId id della traccia da eliminare
     * @return traccia eliminata
     * @throws ValidationException se l'id è nullo o vuoto
     * @throws TrackNotFoundException se la traccia da eliminare non esiste
     */
    public Track deleteTrack(String trackId) {
        validateId(trackId, "L'id della Track da eliminare è nullo o vuoto.");

        return trackRepository.deleteById(trackId)
                .orElseThrow(() -> new TrackNotFoundException("La traccia da eliminare non è stata trovata."));
    }

    /**
     * Valida un id obbligatorio.
     */
    private void validateId(String id, String errorMessage) {
        if (id == null || id.trim().isEmpty()) {
            throw new ValidationException(errorMessage);
        }
    }

    /**
     * Valida che la traccia usata come input non sia nulla.
     *
     * I singoli campi della traccia sono già validati dal costruttore di {@link Track}.
     */
    private void validateTrackInput(Track track) {
        if (track == null) {
            throw new ValidationException("La Track modificata non può essere nulla.");
        }
    }

    /**
     * Recupera una traccia esistente o lancia un errore applicativo coerente.
     */
    private Track getExistingTrack(String trackId) {
        validateId(trackId, "L'id della Track è nullo o vuoto.");

        return trackRepository.findById(trackId)
                .orElseThrow(() -> new TrackNotFoundException("Track non trovata."));
    }

    /**
     * Costruisce una nuova istanza immutabile di Track mantenendo l'id originale.
     */
    private Track buildUpdatedTrack(Track existingTrack, Track newTrack) {
        return new Track(
                existingTrack.getId(),
                newTrack.getTitle(),
                newTrack.getAuthor(),
                newTrack.getDuration(),
                newTrack.getGenre(),
                newTrack.getYear()
        );
    }
}