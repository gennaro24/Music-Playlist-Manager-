package it.unisa.sad.playlistmanager.persistence.repository;

import it.unisa.sad.playlistmanager.domain.model.Track;
import it.unisa.sad.playlistmanager.persistence.db.DatabaseConnectionManager;
import it.unisa.sad.playlistmanager.persistence.exceptions.RepositoryException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
     * 
     * @param ResultSet
     */

    private Track mapResultSetToTrack(ResultSet resultSet) throws SQLException {

        return new Track(
                resultSet.getString("id"),
                resultSet.getString("title"),
                resultSet.getString("author"),
                resultSet.getInt("duration"),
                resultSet.getString("genre"),
                resultSet.getInt("year"));
    }

    public SqliteTrackRepository(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    /**
     * Salva una nuova Track all'interno della tabella tracks in SQlite.
     * lo statement viene scritto indicando per ogni "?" la posizione rispettiva e
     * il tipo da salvare.
     * Infine, viene eseguito l'executeUpdate
     */
    @Override
    public void save(Track track) {
        String sql = """
                INSERT INTO tracks(id, title, author, duration, genre, year)
                VALUES(?,?,?,?,?,?)
                """;
        try (Connection connection = connectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, track.getId());
            statement.setString(2, track.getTitle());
            statement.setString(3, track.getAuthor());
            statement.setInt(4, track.getDuration());
            statement.setString(5, track.getGenre());
            statement.setInt(6, track.getYear());

            statement.executeUpdate();

        } catch (SQLException exception) {
            throw new RepositoryException("Errore nel salvataggio della Track con id: [" + track.getId() + "]",
                    exception);
        }
    }

    /**
     * Trova una Track tramite il suo id.
     * Se la query viene eseguita correttamente, potrà dare due esiti:
     * - la Track è stata trovata, dunque vengono estratti i campi e @return
     * Optional.of(Track)
     * - La Track con quello specifico id non esiste, @return Optional.empty()
     */
    @Override
    public Optional<Track> findById(String id) {
        String sql = """
                SELECT id, title, author, duration, genre, year
                FROM tracks
                WHERE id = ?
                """;
        try (Connection connection = connectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {

                    Track track = mapResultSetToTrack(resultSet);
                    return Optional.of(track);

                }
                return Optional.empty();
            }

        } catch (SQLException exception) {
            throw new RepositoryException("Errore nel trovare la Track con id: [" + id + "]", exception);
        }

    }

    /**
     * Trova tutte le Track presenti nella tabella tracks.
     * 
     * @return un'ArrayList<Track> contenente oggetti di tipo Track.
     *         Se la tabella non contiene nessuna traccia, ritorna una lista vuota.
     */
    @Override
    public List<Track> findAll() {
        String sql = """
                SELECT id, title, author, duration, genre, year
                FROM tracks
                """;
        try (Connection connection = connectionManager.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<Track> allTracks = new ArrayList<>();
                while (resultSet.next()) {

                    Track currentTrack = mapResultSetToTrack(resultSet);
                    allTracks.add(currentTrack);

                }
                return allTracks;
            }

        } catch (SQLException exception) {
            throw new RepositoryException("Errore nel trovare tutte le Track del catalogo.", exception);
        }

    }

    /**
     * Elimina una track nel sistema di persistenza in base al suo id.
     * 
     * @param id dell'oggetto Track da eliminare
     * @return un Optional contenente la traccia eliminata se presente, altrimenti Optional.empty()
     */
    @Override
    public Optional<Track> deleteById(String id) {
        Optional<Track> trackOpt = findById(id);
        String sql = """
                DELETE FROM tracks
                WHERE id = ?
                """;
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            int affected = statement.executeUpdate();
            if (affected == 0) {
                return Optional.empty();
            }
            return trackOpt;
        } catch (SQLException exception) {
            throw new RepositoryException("Errore nell'eliminazione della Track");
        }
    }

    /**
     * Aggiorna una track nel sistema di persistenza in base al suo id.
     * @param track l'oggetto Track da aggiornare
     */
    @Override
    public Optional<Track> update(Track track) {
        String sql = """
                UPDATE tracks
                SET title = ?, author = ?, duration = ?, genre = ?, year = ?
                WHERE id = ?
                """;

        try (Connection connection = connectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, track.getTitle());
            statement.setString(2, track.getAuthor());
            statement.setInt(3, track.getDuration());
            statement.setString(4, track.getGenre());
            statement.setInt(5, track.getYear());
            statement.setString(6, track.getId());

            int rows = statement.executeUpdate();
            if (rows == 0) {
                Optional.empty();
            }
            return Optional.of(track);

        } catch (SQLException exception) {
            throw new RepositoryException("Errore nell'aggiornamento della Track con id");
        }
    }

    /**
     * Ripristina una Track e tutte le associazioni con le playlist usando
     * un'unica transazione SQLite.
     *
     * Se anche una sola associazione non puo' essere inserita, viene eseguito
     * il rollback: la traccia e le associazioni gia' elaborate non restano nel
     * database. In questo modo l'undo non puo' terminare a meta'.
     *
     * @param track traccia eliminata da ricreare con lo stesso ID
     * @param playlistPositions ID delle playlist e relative posizioni originali
     */
    @Override
    public void restoreWithPlaylistPositions(
            Track track,
            Map<String, Integer> playlistPositions) {
        String insertTrackSql = """
                INSERT INTO tracks(id, title, author, duration, genre, year)
                VALUES(?,?,?,?,?,?)
                """;
        String insertAssociationSql = """
                INSERT INTO playlist_tracks(playlist_id, track_id, position)
                VALUES(?,?,?)
                """;

        try (Connection connection = connectionManager.getConnection()) {
            connection.setAutoCommit(false);

            try {
                insertTrack(connection, insertTrackSql, track);
                insertPlaylistPositions(
                        connection,
                        insertAssociationSql,
                        track.getId(),
                        playlistPositions);
                connection.commit();
            } catch (SQLException exception) {
                rollbackRestore(connection, exception);
                throw new RepositoryException(
                        "Errore nel ripristino transazionale della Track con id: ["
                                + track.getId() + "]",
                        exception);
            }
        } catch (SQLException exception) {
            throw new RepositoryException(
                    "Errore nell'apertura della transazione di ripristino della Track con id: ["
                            + track.getId() + "]",
                    exception);
        }
    }

    /**
     * Inserisce la traccia usando la connessione della transazione corrente.
     */
    private void insertTrack(Connection connection, String sql, Track track)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, track.getId());
            statement.setString(2, track.getTitle());
            statement.setString(3, track.getAuthor());
            statement.setInt(4, track.getDuration());
            statement.setString(5, track.getGenre());
            statement.setInt(6, track.getYear());
            statement.executeUpdate();
        }
    }

    /**
     * Ripristina in batch tutte le associazioni playlist-track.
     */
    private void insertPlaylistPositions(
            Connection connection,
            String sql,
            String trackId,
            Map<String, Integer> playlistPositions) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Map.Entry<String, Integer> entry : playlistPositions.entrySet()) {
                statement.setString(1, entry.getKey());
                statement.setString(2, trackId);
                statement.setInt(3, entry.getValue());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    /**
     * Esegue il rollback conservando anche un eventuale errore del rollback
     * come eccezione soppressa della causa originale.
     */
    private void rollbackRestore(Connection connection, SQLException cause) {
        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            cause.addSuppressed(rollbackException);
        }
    }
}
