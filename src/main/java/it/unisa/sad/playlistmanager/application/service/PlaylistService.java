package it.unisa.sad.playlistmanager.application.service;

import java.util.List;

import it.unisa.sad.playlistmanager.application.exceptions.PlaylistNotFoundException;
import it.unisa.sad.playlistmanager.application.exceptions.TrackNotFoundException;
import it.unisa.sad.playlistmanager.application.exceptions.ValidationException;
import it.unisa.sad.playlistmanager.domain.model.Playlist;
import it.unisa.sad.playlistmanager.domain.model.Track;
import it.unisa.sad.playlistmanager.persistence.repository.PlaylistRepository;
import it.unisa.sad.playlistmanager.persistence.repository.TrackRepository;

/**
 * Servizio applicativo responsabile del coordinamento dei casi d'uso legati alle playlist.
 *
 * Funge da intermediario tra il Presentation Layer e il livello di persistenza,
 * mantenendo la logica applicativa fuori dai controller JavaFX.
 *
 * @version 1.2
 */
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final TrackRepository trackRepository;

    /**
     * Costruttore con Dependency Injection delle astrazioni di persistenza.
     *
     * @param playlistRepository repository astratto per la persistenza delle playlist
     * @param trackRepository repository astratto per la persistenza delle tracce
     */
    public PlaylistService(PlaylistRepository playlistRepository, TrackRepository trackRepository) {
        this.playlistRepository = playlistRepository;
        this.trackRepository = trackRepository;
    }

    /**
     * Crea e salva una nuova playlist vuota.
     *
     * @param name nome della playlist da creare
     * @return playlist creata e salvata
     * @throws ValidationException se il nome è nullo o vuoto
     */
    public Playlist createPlaylist(String name) {
        validateText(name, "Il nome della playlist non può essere nullo o vuoto.");

        Playlist newPlaylist = new Playlist(null, name);
        playlistRepository.save(newPlaylist);

        return newPlaylist;
    }

    /**
     * Recupera tutte le playlist salvate.
     *
     * @return lista completa delle playlist, eventualmente vuota
     */
    public List<Playlist> getAllPlaylists() {
        return playlistRepository.findAll();
    }

    /**
     * Recupera una playlist tramite id.
     *
     * @param playlistId id della playlist da cercare
     * @return playlist trovata
     * @throws ValidationException se l'id è nullo o vuoto
     * @throws PlaylistNotFoundException se la playlist non esiste
     */
    public Playlist getPlaylistById(String playlistId) {
        return getExistingPlaylist(playlistId);
    }

    /**
     * Aggiunge una traccia esistente a una playlist esistente.
     *
     * @param playlistId id della playlist
     * @param trackId id della traccia da aggiungere
     * @throws ValidationException se uno degli id è nullo/vuoto o se la traccia è già presente
     * @throws PlaylistNotFoundException se la playlist non esiste
     * @throws TrackNotFoundException se la traccia non esiste
     */
    public void addTrackToPlaylist(String playlistId, String trackId) {
        getExistingPlaylist(playlistId);
        getExistingTrack(trackId);

        if (isTrackAlreadyInPlaylist(playlistId, trackId)) {
            throw new ValidationException("La traccia è già presente nella playlist.");
        }

        playlistRepository.addTrackToPlaylist(playlistId, trackId);
    }

    /**
     * Rimuove una traccia da una playlist senza eliminarla dal catalogo.
     *
     * @param playlistId id della playlist
     * @param trackId id della traccia da rimuovere
     * @throws ValidationException se uno degli id è nullo o vuoto
     * @throws PlaylistNotFoundException se la playlist non esiste
     * @throws TrackNotFoundException se la traccia non esiste nel catalogo
     */
    public void removeTrackFromPlaylist(String playlistId, String trackId) {
        getExistingPlaylist(playlistId);
        getExistingTrack(trackId);

        playlistRepository.removeTrackFromPlaylist(playlistId, trackId);
    }

    /**
     * Recupera le tracce contenute in una playlist.
     *
     * @param playlistId id della playlist
     * @return lista ordinata delle tracce della playlist
     * @throws ValidationException se l'id è nullo o vuoto
     * @throws PlaylistNotFoundException se la playlist non esiste
     */
    public List<Track> getTracksForPlaylist(String playlistId) {
        getExistingPlaylist(playlistId);

        return playlistRepository.findTracksByPlaylistId(playlistId);
    }

    /**
     * Elimina una playlist.
     *
     * L'eliminazione riguarda la playlist e le associazioni con le tracce.
     * Le tracce presenti nel catalogo non vengono eliminate.
     *
     * @param playlistId id della playlist da eliminare
     * @return playlist eliminata
     * @throws ValidationException se l'id è nullo o vuoto
     * @throws PlaylistNotFoundException se la playlist non esiste
     */
    public Playlist deletePlaylist(String playlistId) {
        Playlist playlistToDelete = getExistingPlaylist(playlistId);

        return playlistRepository.deleteById(playlistToDelete.getId())
                .orElseThrow(() -> new PlaylistNotFoundException("La playlist da eliminare non è stata trovata."));
    }

    /**
     * UTILITY: Valida una stringa obbligatoria.
     */
    private void validateText(String value, String errorMessage) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(errorMessage);
        }
    }

    /**
     * UTILITY: Recupera una playlist esistente o lancia un errore applicativo coerente.
     */
    private Playlist getExistingPlaylist(String playlistId) {
        validateText(playlistId, "L'id della Playlist è nullo o vuoto.");

        return playlistRepository.findById(playlistId)
                .orElseThrow(() -> new PlaylistNotFoundException("Playlist non trovata con l'ID specificato."));
    }

    /**
     * UTILITY: Recupera una traccia esistente o lancia un errore applicativo coerente.
     */
    private Track getExistingTrack(String trackId) {
        validateText(trackId, "L'id della Track è nullo o vuoto.");

        return trackRepository.findById(trackId)
                .orElseThrow(() -> new TrackNotFoundException("Traccia non trovata nel catalogo con l'ID specificato."));
    }

    /**
     * UTILITY: Verifica se una traccia è già presente nella playlist.
     */
    private boolean isTrackAlreadyInPlaylist(String playlistId, String trackId) {
        return playlistRepository.findTracksByPlaylistId(playlistId)
                .stream()
                .anyMatch(track -> track.getId().equals(trackId));
    }
}