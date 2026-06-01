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

    /**
     * Utility method per costruire l'oggetto Track a partire dal ResultSet.
     * @param ResultSet
     */

    private Track mapResultSetToTrack(ResultSet resultSet) throws SQLException{

        return new Track(
            resultSet.getString("id"),
            resultSet.getString("title"),
            resultSet.getString("author"),
            resultSet.getInt("duration"),
            resultSet.getString("genre"),
            resultSet.getInt("year")
        );
    }

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
                // I CATCH VANNO MODIFICATI CON UN EXCEPTION DEDICATA.
             }catch(SQLException exception){
                // da rendere più robusto, viene inserito ora come placeholder.
                exception.getSQLState();
             }
    }
    /**
     * Trova una Track tramite il suo id.
     * Se la query viene eseguita correttamente, potrà dare due esiti:
     *  - la Track è stata trovata, dunque vengono estratti i campi e @return Optional.of(Track)
     *  - La Track con quello specifico id non esiste, @return Optional.empty()     
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

                        Track track = mapResultSetToTrack(resultSet);
                        return Optional.of(track);

                    }
                    return Optional.empty();
                }

                // I CATCH VANNO MODIFICATI CON UN EXCEPTION DEDICATA.
             }catch(SQLException exception){
                    exception.getSQLState();
                    return Optional.empty();
            }
        
    }
    /**
     * Trova tutte le Track presenti nella tabella tracks.
     * @return un'ArrayList<Track> contenente oggetti di tipo Track. 
     * Se la tabella non contiene nessuna traccia, ritorna una lista vuota.
     */
    @Override
    public List<Track> findAll() {
                String sql = """
                SELECT id, title, author, duration, genre, year
                FROM tracks
                """;
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)){
                try (ResultSet resultSet = preparedStatement.executeQuery()){
                    List<Track> allTracks = new ArrayList<>();
                    while (resultSet.next()){

                        Track currentTrack = mapResultSetToTrack(resultSet);
                        allTracks.add(currentTrack);

                    }
                    return allTracks;
                }
                // I CATCH VANNO MODIFICATI CON UN EXCEPTION DEDICATA.
             }catch(SQLException exception){
                exception.getSQLState();
                return null;
             }

    }
}