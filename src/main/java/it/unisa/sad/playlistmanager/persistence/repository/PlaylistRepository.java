package it.unisa.sad.playlistmanager.persistence.repository;

import java.util.List;
import java.util.Optional;

import it.unisa.sad.playlistmanager.domain.model.Playlist;

/**
 * Interfaccia contrattuale per le operazioni di persistenza della Playlist (DIP).
 */
public interface PlaylistRepository {
    
    /**
     * Salva una nuova playlist nel sistema di persistenza.
     * @param playlist L'oggetto Playlist da salvare.
     */
    void save(Playlist playlist);

    /**
     * Cerca una playlist nel sistema di persistenza dal suo id.
     * @return un Optional contenente la playlist se presente, altrimenti Optional.empty()
     */
    Optional<Playlist> findById (String id);

    /**
     * Ritorna tutte le playlist nel sistema di persistenza
     * @return una List di Playlist.
     */

    List<Playlist> findAll();
}