package it.unisa.sad.playlistmanager.persistence.repository;

import it.unisa.sad.playlistmanager.domain.model.Playlist;
import it.unisa.sad.playlistmanager.persistence.db.DatabaseConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    /**
     * Converte la riga corrente di un {@link ResultSet} in un oggetto Playlist
     * @param resultSet risultato della query posizionata sulla riga corrente
     * @return una playlist costruita usando i valori nella riga corrente
     * @throws SQLException se si verifica un errore durante la lettura dei campi
     */
    private Playlist mapResultSetToPlaylist(ResultSet resultSet)throws SQLException{
        return new Playlist(
                            resultSet.getString("id"),
                            resultSet.getString("name")
                            );
    }

    public SqlitePlaylistRepository(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }
    /**
     * Salva una playlist con un nome valido all'interno della tabella playlists.
     * @param playlist che vuole essere salvata, con nome e id.
     * Il controllo della validità del nome è delegato alla classe di dominio {@Link Playlist}
     */
    @Override
    public void save(Playlist playlist) {
        String sql = """
                INSERT INTO playlists(id, name)
                VALUES (?, ?)
                """;
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)){
                preparedStatement.setString(1, playlist.getId());
                preparedStatement.setString(2, playlist.getName());
                preparedStatement.executeUpdate();
                // I CATCH VANNO MODIFICATI CON UN EXCEPTION DEDICATA.
             }catch(SQLException exception){exception.getSQLState();}
    }
    /**
     * Cerca una playlist tramite il suo id.
     *
     * Se viene trovata una riga con l'id specificato, il risultato viene convertito
     * in un oggetto {@link Playlist}. Se non viene trovata nessuna playlist,
     * il metodo restituisce {@link Optional#empty()}.
     *
     * @param id identificativo della playlist da cercare
     * @return un Optional contenente la playlist se presente, altrimenti Optional.empty()
     */
    @Override
    public Optional<Playlist> findById(String id) {
        String sql = """
                    SELECT id,name 
                    FROM playlists
                    WHERE id=?
                """;
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)){
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()){
                    Playlist playlist = mapResultSetToPlaylist(resultSet);
                    return Optional.of(playlist);
                }
                return Optional.empty();
                // I CATCH VANNO MODIFICATI CON UN EXCEPTION DEDICATA.
             }catch (SQLException exception){exception.getSQLState(); return Optional.empty();}
    }
        /**
     * Restituisce tutte le playlist presenti nella tabella playlists.
     *
     * Il metodo esegue una query senza filtro e converte ogni riga del risultato
     * in un oggetto {@link Playlist}. Se la tabella è vuota, restituisce una lista
     * vuota.
     *
     * @return lista delle playlist presenti nel database
     */
    @Override
    public List<Playlist> findAll() {
        String sql = """
                SELECT id,name 
                FROM playlists
                """;
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)){
                ResultSet resultSet = preparedStatement.executeQuery();
                List<Playlist> playlists = new ArrayList<>();
                while (resultSet.next()){
                    Playlist playlist = mapResultSetToPlaylist(resultSet);
                    playlists.add(playlist);
                }
                return playlists;
                // I CATCH VANNO MODIFICATI CON UN EXCEPTION DEDICATA.
            }catch (SQLException exception){exception.getSQLState(); return null;}
    }
    // TODO:
    @Override
    public Optional<Playlist> findByName(String name){
        return Optional.empty();
    }
@Override
public boolean existsByName(String name) {
    String sql = """
            SELECT 1
            FROM playlists
            WHERE name = ?
            LIMIT 1
            """;

    try (Connection connection = connectionManager.getConnection();
         PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

        preparedStatement.setString(1, name);

        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            return resultSet.next();
        }

    } catch (SQLException exception) {
        exception.getSQLState();
        return false;
    }
}
    /**
     * TODO:
     * Nelle prossime iterazioni il repository potrà essere esteso
     * con operazioni relative alla gestione delle tracce all'interno
     * di una playlist.
     *
     * Possibili metodi futuri:
     * - aggiungere una traccia esistente a una playlist;
     * - rimuovere una traccia da una playlist;
     * - recuperare tutte le tracce associate a una playlist;
     * - mantenere e aggiornare la posizione delle tracce nella playlist.
     *
     * Queste operazioni saranno collegate alla tabella ponte playlist_tracks.
     */
}