package it.unisa.sad.playlistmanager.persistence.db;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Gestisce la creazione delle connessioni verso il database SQLite
 * dell'applicazione.
 *
 * Questa classe centralizza l'URL del database e fornisce un unico punto
 * di accesso per ottenere connessioni JDBC. In questo modo il resto del
 * livello persistence non dipende direttamente da DriverManager.
 */

public class DatabaseConnectionManager {

    /**
     * URL JDBC del database SQLite locale.
     *
     * Il file del database viene creato nella cartella data/ del progetto
     * se non esiste già.
     */
    private static final String DATABASE_DIRECTORY = "data";
    private static final String DATABASE_URL = "jdbc:sqlite:data/music_playlist_manager.db";

    /**
     * Restituisce una nuova connessione JDBC al database SQLite con i vincoli
     * di chiave esterna attivi. In SQLite {@code PRAGMA foreign_keys = ON} vale
     * per connessione: attivarlo qui garantisce CASCADE e integrita' referenziale
     * su tutte le operazioni dei repository.
     * Crea la directory in cui salvare il database se non esiste.
     * @return una connessione attiva verso il database
     * @throws SQLException se la connessione al database non può essere creata
     */
    
    public Connection getConnection() throws SQLException{
        ensureDatabaseDirectoryExists();
        Connection connection = DriverManager.getConnection(DATABASE_URL);
        enableForeignKeys(connection);
        return connection;
    }

    private void enableForeignKeys(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
    }

     private void ensureDatabaseDirectoryExists() throws SQLException {
        try {
            Files.createDirectories(Path.of(DATABASE_DIRECTORY));
        } catch (IOException exception) {
            throw new SQLException("Impossibile creare la cartella del database.", exception);
        }
    }
}
