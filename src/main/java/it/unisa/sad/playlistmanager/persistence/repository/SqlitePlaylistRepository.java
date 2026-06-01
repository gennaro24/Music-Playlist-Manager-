package it.unisa.sad.playlistmanager.persistence.repository;

import it.unisa.sad.playlistmanager.domain.model.Playlist;
import it.unisa.sad.playlistmanager.persistence.db.DatabaseConnectionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementazione SQLite del repository delle playlist.
 *
 * Questa classe traduce le operazioni definite da PlaylistRepository
 * in query SQL eseguite sul database SQLite.
 */
public class SqlitePlaylistRepository implements PlaylistRepository {

    private final DatabaseConnectionManager connectionManager;

    public SqlitePlaylistRepository(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public void save(Playlist playlist) {
        // Da implementare 
    }

    @Override
    public Optional<Playlist> findById(String id) {
        // Da implementare 
        return Optional.empty();
    }

    @Override
    public List<Playlist> findAll() {
        // Da implementare 
        return new ArrayList<>();
    }
}