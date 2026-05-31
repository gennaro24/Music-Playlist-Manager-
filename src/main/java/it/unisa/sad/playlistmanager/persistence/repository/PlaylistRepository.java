package it.unisa.sad.playlistmanager.persistence.repository;

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
}