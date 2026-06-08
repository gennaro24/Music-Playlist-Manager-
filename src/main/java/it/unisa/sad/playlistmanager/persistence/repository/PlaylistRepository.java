package it.unisa.sad.playlistmanager.persistence.repository;

import java.util.List;
import java.util.Optional;

import it.unisa.sad.playlistmanager.domain.model.Playlist;
import it.unisa.sad.playlistmanager.domain.model.Track;

/**
 * Interfaccia contrattuale per le operazioni di persistenza dell'entità Playlist.
 *
 * Definisce le operazioni che il livello applicativo può usare senza dipendere
 * da una specifica tecnologia di persistenza.
 */
public interface PlaylistRepository {

    /**
     * Salva una nuova playlist nel sistema di persistenza.
     *
     * @param playlist l'oggetto Playlist da salvare
     */
    void save(Playlist playlist);

    /**
     * Cerca una playlist nel sistema di persistenza tramite il suo id.
     *
     * @param id identificativo della playlist da cercare
     * @return un Optional contenente la playlist se presente, altrimenti Optional.empty()
     */
    Optional<Playlist> findById(String id);

    /**
     * Cerca una playlist nel sistema di persistenza tramite il suo nome.
     *
     * @param name nome della playlist da cercare
     * @return un Optional contenente la playlist se presente, altrimenti Optional.empty()
     */
    Optional<Playlist> findByName(String name);

    /**
     * Restituisce tutte le playlist presenti nel sistema di persistenza.
     *
     * @return lista delle playlist salvate. La lista può essere vuota
     */
    List<Playlist> findAll();

    /**
     * Verifica se esiste già una playlist con il nome specificato.
     *
     * @param name nome della playlist da verificare
     * @return true se esiste già una playlist con quel nome, false altrimenti
     */
    boolean existsByName(String name);

    void addTrackToPlaylist(String playlistId, String trackId );
    
    void removeTrackFromPlaylist(String playlistId, String trackId);

    List<Track> findTracksByPlaylistId(String playlistId);
    
    void deleteById(String playlistId);
    
}