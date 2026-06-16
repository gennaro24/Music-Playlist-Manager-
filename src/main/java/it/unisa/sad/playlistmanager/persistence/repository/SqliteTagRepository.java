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
import java.util.Optional;

import it.unisa.sad.playlistmanager.domain.model.Tag;

public class SqliteTagRepository implements TagRepository {
    private final DatabaseConnectionManager connectionManager;

    /**
     * Utility method per costruire l'oggetto Track a partire dal ResultSet.
     * 
     * @param ResultSet
     */

    private Tag mapResultSetToTag(ResultSet resultSet) throws SQLException {
        return new Tag(
                resultSet.getString("id"),
                resultSet.getString("name"));
    }
    private Track mapResultSetToTrack(ResultSet resultSet) throws SQLException {
        return new Track(
                resultSet.getString("id"),
                resultSet.getString("title"),
                resultSet.getString("author"),
                resultSet.getInt("duration"),
                resultSet.getString("genre"),
                resultSet.getInt("year"));
    }

    public SqliteTagRepository(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    /**
     * Salva una nuovo Tag all'interno della tabella tags in SQlite.
     * lo statement viene scritto indicando per ogni "?" la posizione rispettiva e
     * il tipo da salvare.
     * Infine, viene eseguito l'executeUpdate
     */
    @Override
    public void save(Tag tag) {
        String sql = """
                INSERT INTO tags(id, name)
                VALUES(?,?)
                """;
        try (Connection connection = connectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tag.getId());
            statement.setString(2, tag.getName());

            statement.executeUpdate();

        } catch (SQLException exception) {
            throw new RepositoryException("Errore nel salvataggio della Tag con id: [" + tag.getId() + "]",exception);
        }
    }

    /**
     * Trova una Tag tramite il suo id.
     * Se la query viene eseguita correttamente, potrà dare due esiti:
     * - la Tag è stata trovata, dunque vengono estratti i campi e @return
     * Optional.of(Tag)
     * - La Tag con quello specifico id non esiste, @return Optional.empty()
     */
    @Override
    public Optional<Tag> findById(String id) {
        String sql = """
                SELECT id, name
                FROM tags
                WHERE id = ?
                """;
        try (Connection connection = connectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {

                    Tag tag = mapResultSetToTag(resultSet);
                    return Optional.of(tag);

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
    public List<Tag> findAll() {
        String sql = """
                SELECT id, name
                FROM tags
                """;
        try (Connection connection = connectionManager.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<Tag> allTags = new ArrayList<>();
                while (resultSet.next()) {

                    Tag currentTag = mapResultSetToTag(resultSet);
                    allTags.add(currentTag);

                }
                return allTags;
            }

        } catch (SQLException exception) {
            throw new RepositoryException("Errore nel trovare tutte le Track del catalogo.", exception);
        }
    }

    /**
     * Elimina un tag nel sistema di persistenza in base al suo id.
     * @param id dell'oggetto Tag da eliminare
     * @return un Optional contenente il tag eliminato se presente, altrimenti Optional.empty()
     */
    @Override
    public Optional<Tag> deleteById(String id) {
        Optional<Tag> tagOpt = findById(id);
        String sql = """
                DELETE FROM tags
                WHERE id = ?
                """;
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            int affected = statement.executeUpdate();
            if (affected == 0) {
                return Optional.empty();
            }
            return tagOpt;
        } catch (SQLException exception) {
            throw new RepositoryException("Errore nell'eliminazione della Tag");
        }
    }

    /**
     * Trova tutti i tag nel sistema di persistenza assegnati a una traccia.
     * @param trackId id della traccia
     * @return una List di tag trovati. La lista può essere vuota.
     */
    @Override
    public List<Tag> findByTrackId(String trackId) {
            String sql = """
                    SELECT t.id, t.name
                    FROM tags t
                    JOIN track_tags tt ON t.id = tt.tag_id
                    WHERE tt.track_id = ?
                    """;
        try (Connection connection = connectionManager.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, trackId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Tag> tags = new ArrayList<>();
                while (resultSet.next()) {
                    Tag currentTag = mapResultSetToTag(resultSet);
                    tags.add(currentTag);
                }
                return tags;
            }
        } catch (SQLException exception) {
            throw new RepositoryException("Errore nel trovare i tag assegnati alla Track con id: [" + trackId + "]", exception);
        }
    }

    /**
     * Trova un tag nel sistema di persistenza in base al suo nome.
     * @param name nome del tag da trovare
     * @return un Optional contenente il tag se presente, altrimenti Optional.empty()
     */
    @Override
    public Optional<Tag> findByName(String name) {
        String sql = """
                SELECT id, name
                FROM tags
                WHERE name = ?
                """;
        try (Connection connection = connectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Tag tag = mapResultSetToTag(resultSet);
                    return Optional.of(tag);
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            throw new RepositoryException("Errore nel trovare la Tag con nome: [" + name + "]", exception);
        }
    }

    

    @Override
    public boolean isAttached(String trackId, String tagId) {
        String sql = """
                SELECT 1
                FROM track_tags
                WHERE track_id = ? AND tag_id = ?
                """;
        try (Connection connection = connectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, trackId);
            statement.setString(2, tagId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException exception) {
            throw new RepositoryException("Errore nel verificare se il tag è assegnato alla Track con id: [" + trackId + "]", exception);
        }
    }

    @Override
    public void attach(String trackId, String tagId) {
        String sql = """
                INSERT INTO track_tags (track_id, tag_id)
                VALUES (?, ?)
                """;
        try (Connection connection = connectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, trackId);
            statement.setString(2, tagId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RepositoryException("Errore nell'assegnazione del tag alla Track con id: [" + trackId + "]", exception);
        }
    }

    @Override
    public void detach(String trackId, String tagId) {
        String sql = """
                DELETE FROM track_tags
                WHERE track_id = ? AND tag_id = ?
                """;
        try (Connection connection = connectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, trackId);
            statement.setString(2, tagId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RepositoryException("Errore nella rimozione del tag dalla Track con id: [" + trackId + "]", exception);
        }
    }

    @Override
    public List<Track> findTracksByTagId(String tagId) {
        String sql = """
                SELECT t.id, t.title, t.author, t.duration, t.genre, t.year
                FROM tracks t
                JOIN track_tags tt ON t.id = tt.track_id
                WHERE tt.tag_id = ?
                """;
        try (Connection connection = connectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tagId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Track> tracks = new ArrayList<>();
                while (resultSet.next()) {
                    Track currentTrack = mapResultSetToTrack(resultSet);
                    tracks.add(currentTrack);
                }
                return tracks;
            }
        } catch (SQLException exception) {
            throw new RepositoryException("Errore nel trovare le Track con tag id: [" + tagId + "]", exception);
        }
    }

}
