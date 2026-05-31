package it.unisa.sad.playlistmanager.persistence.repository;

import it.unisa.sad.playlistmanager.domain.model.Playlist;

/**
 * Interfaccia contrattuale per le operazioni di persistenza dell'entità Playlist (DIP).
 */
public interface PlaylistRepository {
    /**
     * Salva una nuova playlist nel sistema di persistenza.
     * * @param playlist L'oggetto {@link Playlist} da salvare.
     */
    void save(Playlist playlist);
}
