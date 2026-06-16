package it.unisa.sad.playlistmanager.application.service;

import java.util.List;
import java.util.Optional;

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
     * Crea e salva una nuova playlist vuota con un id specifico.
     * @param id
     * @param name
     * @return
     */
    public Playlist createPlaylist(String id,String name) {
        validateText(name, "Il nome della playlist non può essere nullo o vuoto.");

        Playlist newPlaylist = new Playlist(id, name);
        playlistRepository.save(newPlaylist);

        return newPlaylist;
    }

    public Playlist createPlaylistWithTracks(String name, List<Track> tracks) {
        return createPlaylistWithTracks(null, name, tracks);
    }

    public Playlist createPlaylistWithTracks(String id, String name, List<Track> tracks) {
        validateText(name, "Il nome della playlist non può essere nullo o vuoto.");
        validateTracksInput(tracks);

        Playlist newPlaylist = new Playlist(id, name);
        playlistRepository.saveWithTracks(newPlaylist, tracks);

        return newPlaylist;
    }

    /**
     * Popola una playlist esistente con tracce.
     */

    public void populatePlaylist(String playlistId, List<Track> tracks){
        getExistingPlaylist(playlistId); // Verifica che la playlist esista
        for (Track track : tracks) {
            addTrackToPlaylist(playlistId, track.getId());
        }
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
     * Verifica se una traccia è già presente in una playlist.
     * @param playlistId
     * @param trackId
     * @return
     */
    public boolean isTrackInPlaylist(String playlistId, String trackId) {
        getExistingPlaylist(playlistId);
        getExistingTrack(trackId);

        return isTrackAlreadyInPlaylist(playlistId, trackId);
    }

    /**
     * Restituisce la posizione di una traccia all'interno di una playlist.
     * @param playlistId
     * @param trackId
     * @return Optional<Integer> contenente la posizione della traccia se presente, o vuoto se la traccia non è presente.
     */
    public Optional<Integer> getTrackPosition(String playlistId, String trackId) {
        getExistingPlaylist(playlistId);
        getExistingTrack(trackId);

        return playlistRepository.getTrackPosition(playlistId, trackId);
    }

    /**
     * Ripristina una traccia in una playlist alla posizione originale.
     * @param playlistId
     * @param trackId
     * @param position
     */
    public void restoreTrackToPlaylist(String playlistId,String trackId,int position) {
        getExistingPlaylist(playlistId);
        getExistingTrack(trackId);

        if (position < 1) {
            throw new ValidationException(
                    "La posizione deve essere maggiore di zero."
            );
        }

        if (isTrackAlreadyInPlaylist(playlistId, trackId)) {
            throw new ValidationException(
                    "La traccia è già presente nella playlist."
            );
        }

        playlistRepository.addTrackToPlaylistAtPosition(
                playlistId,
                trackId,
                position
        );
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

    private void validateTracksInput(List<Track> tracks) {
        if (tracks == null) {
            throw new ValidationException("La lista di tracce non può essere nulla.");
        }
        for (Track track : tracks) {
            if (track == null) {
                throw new ValidationException("La playlist non può contenere tracce nulle.");
            }
            getExistingTrack(track.getId());
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
