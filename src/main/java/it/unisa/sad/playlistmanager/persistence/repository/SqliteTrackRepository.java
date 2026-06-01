package it.unisa.sad.playlistmanager.persistence.repository;

import it.unisa.sad.playlistmanager.domain.model.Track;
import it.unisa.sad.playlistmanager.persistence.db.DatabaseConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementazione SQLite del repository delle tracce.
 *
 * Questa classe traduce le operazioni definite da TrackRepository
 * in query SQL eseguite sul database SQLite.
 */
public class SqliteTrackRepository implements TrackRepository {

    private final DatabaseConnectionManager connectionManager;

    public SqliteTrackRepository(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }
    /**
     * Salva una nuova Track all'interno della tabella tracks in SQlite.
     * lo statement viene scritto indicando per ogni "?" la posizione rispettiva e il tipo da salvare.
     * Infine, viene eseguito l'executeUpdate
     */
    @Override
    public void save(Track track) {
        String sql = """
                INSERT INTO tracks(id, title, author, duration, genre, year)
                VALUES(?,?,?,?,?,?)
                """;
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)){
                statement.setString(1, track.getId());
                statement.setString(2, track.getTitle());
                statement.setString(3, track.getAuthor());
                statement.setInt(4, track.getDuration());
                statement.setString(5, track.getGenre());
                statement.setInt(6, track.getYear());

                statement.executeUpdate();

             }catch(SQLException exception){
                // da rendere più robusto, viene inserito ora come placeholder.
                exception.getSQLState();
             }
    }
    /**
     * 
     */
    @Override
    public Optional<Track> findById(String id) {
        String sql = """
                SELECT id, title, author, duration, genre, year
                FROM tracks
                WHERE id = ?
                """;
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)){
                statement.setString(1, id);

                try (ResultSet resultSet = statement.executeQuery()){
                    if (resultSet.next()){
                        String trackId = resultSet.getString("id");
                        String title = resultSet.getString("title");
                        String author = resultSet.getString("author");
                        int duration = resultSet.getInt("duration");
                        String genre = resultSet.getString("genre");
                        int year = resultSet.getInt("year");

                        Track track = new Track(trackId, title, author, duration, genre, year);

                        return Optional.of(track);

                    }
                    return Optional.empty();
                }


             }catch(SQLException exception){
               throw new RuntimeException("errore durante la ricerca della traccia.");
             }  
        
    }

    @Override
    public List<Track> findAll() {
                String sql = """
                SELECT id, title, author, duration, genre, year
                FROM tracks
                """;
        return new ArrayList<>();
    }
}