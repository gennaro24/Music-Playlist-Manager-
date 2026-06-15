package it.unisa.sad.playlistmanager.persistence.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Inizializza lo schema del database SQLite dell'applicazione.
 *
 * Questa classe ha la responsabilità di creare le tabelle necessarie
 * al funzionamento base del Music Playlist Manager, se non sono già
 * presenti nel database.
 *
 * Le tabelle create sono:
 * - tracks: contiene le tracce musicali del catalogo;
 * - playlists: contiene le playlist create dall'utente;
 * - playlist_tracks: rappresenta l'associazione molti-a-molti tra
 *   playlist e tracce, mantenendo anche l'ordine delle tracce nella playlist;
 * - tags: contiene i tag visuali assegnabili alle tracce;
 * - track_tags: rappresenta l'associazione molti-a-molti tra tracce e tag.
 */

public class DatabaseInitializer {
    
    private final DatabaseConnectionManager connectionManager;

     /**
     * Crea un nuovo initializer usando il connection manager specificato.
     *
     * @param connectionManager componente usato per ottenere connessioni al database
     */
    public DatabaseInitializer(DatabaseConnectionManager connectionManager){
        this.connectionManager = connectionManager;
    }

     /**
     * Crea lo schema iniziale del database, se non esiste già.
     *
     * Il metodo è idempotente: può essere eseguito più volte senza
     * ricreare o sovrascrivere tabelle già esistenti, grazie all'uso
     * di CREATE TABLE IF NOT EXISTS.
     *
     * @throws SQLException se si verifica un errore durante la creazione
     *                      delle tabelle o l'accesso al database
     */
    
    public void initializeDatabase() throws SQLException {
        try (Connection connection = connectionManager.getConnection();
             Statement statement = connection.createStatement()) {
                // Attiva i vincoli di chiave esterna esplicitamente
                statement.execute("PRAGMA foreign_keys = ON;");
                statement.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS tracks (
                            id TEXT PRIMARY KEY,
                            title TEXT NOT NULL,
                            author TEXT NOT NULL,
                            duration INTEGER NOT NULL,
                            genre TEXT,
                            year INTEGER,
                            UNIQUE (title, author, duration, genre, year)
                        ) 
                        """);
                statement.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS playlists (
                            id TEXT PRIMARY KEY,
                            name TEXT NOT NULL UNIQUE
                        )
                        """);
                statement.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS playlist_tracks (
                            playlist_id TEXT NOT NULL,
                            track_id TEXT NOT NULL,
                            position INTEGER NOT NULL,
                            PRIMARY KEY (playlist_id, track_id),
                            FOREIGN KEY (playlist_id) REFERENCES playlists(id) ON DELETE CASCADE ON UPDATE CASCADE,
                            FOREIGN KEY (track_id) REFERENCES tracks(id) ON DELETE CASCADE ON UPDATE CASCADE
                       
                        )
                        """);
                //aggiunta della tabella tags, contiene i tag visuali assegnabili alle tracce
                //id univoco del tag
                //name nome del tag
                //unique per evitare duplicati
                statement.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS tags (
                            id TEXT PRIMARY KEY,
                            name TEXT NOT NULL UNIQUE
                        )
                        """);
                //aggiunta della tabella track_tags, rappresenta l'associazione molti-a-molti tra tracce e tag
                //track_id id della traccia
                //tag_id id del tag
                //primary key per evitare duplicati
                //foreign key per evitare cancellazioni incrociate
                //on delete cascade per cancellare anche le associazioni se la traccia o il tag vengono eliminati
                //on update cascade per aggiornare anche le associazioni se l'id della traccia o del tag viene aggiornato
                statement.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS track_tags (
                            track_id TEXT NOT NULL,
                            tag_id TEXT NOT NULL,
                            PRIMARY KEY (track_id, tag_id),
                            FOREIGN KEY (track_id) REFERENCES tracks(id) ON DELETE CASCADE ON UPDATE CASCADE,
                            FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE ON UPDATE CASCADE
                        )
                        """);
             }
    }
}
