package it.unisa.sad.playlistmanager.persistence.repository;

import it.unisa.sad.playlistmanager.domain.model.Tag;
import it.unisa.sad.playlistmanager.domain.model.Track;
import it.unisa.sad.playlistmanager.persistence.db.DatabaseConnectionManager;
import it.unisa.sad.playlistmanager.persistence.db.DatabaseInitializer;
import it.unisa.sad.playlistmanager.persistence.exceptions.RepositoryException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test di persistenza per {@link SqliteTagRepository}.
 *
 * Ogni test gira su un database SQLite reale isolato, creato su file
 * temporaneo e inizializzato con lo schema applicativo tramite
 * {@link DatabaseInitializer}. Si usa un file temporaneo (non un database
 * {@code :memory:}) perché il repository apre una nuova connessione a ogni
 * metodo: un {@code :memory:} verrebbe ricreato vuoto a ogni chiamata, mentre
 * il file mantiene i dati tra le connessioni successive.
 *
 * Le tracce necessarie ai test vengono inserite con SQL diretto, dato che il
 * repository dei tag non gestisce la tabella {@code tracks}.
 *
 * NOTA: la tabella {@code track_tags} dichiara ON DELETE CASCADE, ma il vincolo
 * non viene applicato durante le normali operazioni perché il PRAGMA
 * foreign_keys è abilitato solo sulla connessione dell'initializer e non su
 * quelle del repository. Eliminando un tag, le righe di associazione restano
 * quindi orfane in {@code track_tags}; non compaiono comunque nelle letture
 * perché {@code findByTrackId} fa la JOIN con {@code tags}. I test verificano
 * il comportamento osservabile dal contratto del repository, non la pulizia
 * fisica delle righe orfane.
 */
class SqliteTagRepositoryTest {

    /** Connection manager di test che punta a un database temporaneo isolato. */
    private static final class TestDatabaseConnectionManager extends DatabaseConnectionManager {
        private final String url;

        TestDatabaseConnectionManager(Path databaseFile) {
            this.url = "jdbc:sqlite:" + databaseFile.toAbsolutePath();
        }

        @Override
        public Connection getConnection() throws SQLException {
            return java.sql.DriverManager.getConnection(url);
        }
    }

    private Path databaseFile;
    private DatabaseConnectionManager connectionManager;
    private SqliteTagRepository repository;

    @BeforeEach
    void setUp() throws IOException, SQLException {
        databaseFile = Files.createTempFile("sqlite-tag-repo-test", ".db");
        Files.deleteIfExists(databaseFile); // SQLite crea il file vuoto all'apertura
        connectionManager = new TestDatabaseConnectionManager(databaseFile);
        new DatabaseInitializer(connectionManager).initializeDatabase();
        repository = new SqliteTagRepository(connectionManager);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(databaseFile);
        Files.deleteIfExists(Path.of(databaseFile.toAbsolutePath() + "-journal"));
        Files.deleteIfExists(Path.of(databaseFile.toAbsolutePath() + "-wal"));
        Files.deleteIfExists(Path.of(databaseFile.toAbsolutePath() + "-shm"));
    }

    private void insertTrack(Track track) throws SQLException {
        String sql = "INSERT INTO tracks(id, title, author, duration, genre, year) VALUES(?,?,?,?,?,?)";
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, track.getId());
            statement.setString(2, track.getTitle());
            statement.setString(3, track.getAuthor());
            statement.setInt(4, track.getDuration());
            statement.setString(5, track.getGenre());
            statement.setInt(6, track.getYear());
            statement.executeUpdate();
        }
    }

    private Set<String> tagIds(List<Tag> tags) {
        return tags.stream().map(Tag::getId).collect(Collectors.toSet());
    }

    private Set<String> trackIds(List<Track> tracks) {
        return tracks.stream().map(Track::getId).collect(Collectors.toSet());
    }

    // ===== save / findById / findAll / findByName =====

    @Test
    void saveEFindByIdRoundTrip() {
        repository.save(new Tag("tag-1", "favourite"));

        Optional<Tag> found = repository.findById("tag-1");
        assertTrue(found.isPresent());
        assertEquals("favourite", found.get().getName());
    }

    @Test
    void findByIdAssenteRitornaEmpty() {
        assertTrue(repository.findById("inesistente").isEmpty());
    }

    @Test
    void findAllRitornaTuttiITag() {
        repository.save(new Tag("tag-1", "favourite"));
        repository.save(new Tag("tag-2", "explicit"));

        assertEquals(Set.of("tag-1", "tag-2"), tagIds(repository.findAll()));
    }

    @Test
    void findByName() {
        repository.save(new Tag("tag-1", "favourite"));

        assertEquals("tag-1", repository.findByName("favourite").orElseThrow().getId());
        assertTrue(repository.findByName("inesistente").isEmpty());
    }

    @Test
    void saveConNomeDuplicatoLanciaRepositoryException() {
        repository.save(new Tag("tag-1", "favourite"));
        // name ha vincolo UNIQUE nello schema
        assertThrows(RepositoryException.class, () -> repository.save(new Tag("tag-2", "favourite")));
    }

    @Test
    void saveConIdDuplicatoLanciaRepositoryException() {
        repository.save(new Tag("tag-1", "favourite"));
        assertThrows(RepositoryException.class, () -> repository.save(new Tag("tag-1", "explicit")));
    }

    // ===== attach / isAttached / detach / findByTrackId =====

    @Test
    void attachIsAttachedEDetach() throws SQLException {
        insertTrack(new Track("track-1", "Titolo", "Autore", 100, "Pop", 2020));
        repository.save(new Tag("tag-1", "favourite"));

        assertFalse(repository.isAttached("track-1", "tag-1"));

        repository.attach("track-1", "tag-1");
        assertTrue(repository.isAttached("track-1", "tag-1"));

        repository.detach("track-1", "tag-1");
        assertFalse(repository.isAttached("track-1", "tag-1"));
    }

    @Test
    void findByTrackIdRitornaITagAssociati() throws SQLException {
        insertTrack(new Track("track-1", "Titolo", "Autore", 100, "Pop", 2020));
        repository.save(new Tag("tag-1", "favourite"));
        repository.save(new Tag("tag-2", "explicit"));
        repository.attach("track-1", "tag-1");
        repository.attach("track-1", "tag-2");

        assertEquals(Set.of("tag-1", "tag-2"), tagIds(repository.findByTrackId("track-1")));
    }

    @Test
    void findByTrackIdVuotoSeNessunaAssociazione() throws SQLException {
        insertTrack(new Track("track-1", "Titolo", "Autore", 100, "Pop", 2020));
        assertTrue(repository.findByTrackId("track-1").isEmpty());
    }

    // ===== findTracksByTagId =====

    @Test
    void findTracksByTagIdRitornaLeTracceConIlTag() throws SQLException {
        insertTrack(new Track("track-1", "Uno", "Autore", 100, "Pop", 2020));
        insertTrack(new Track("track-2", "Due", "Autore", 100, "Rock", 2021));
        insertTrack(new Track("track-3", "Tre", "Autore", 100, "Jazz", 2022));
        repository.save(new Tag("tag-1", "favourite"));
        repository.attach("track-1", "tag-1");
        repository.attach("track-2", "tag-1");

        Set<String> result = trackIds(repository.findTracksByTagId("tag-1"));

        assertEquals(Set.of("track-1", "track-2"), result);
        assertFalse(result.contains("track-3"));
    }

    @Test
    void findTracksByTagIdSenzaAssociazioniRitornaVuoto() {
        repository.save(new Tag("tag-1", "favourite"));
        assertTrue(repository.findTracksByTagId("tag-1").isEmpty());
    }

    // ===== deleteById =====

    @Test
    void deleteByIdRimuoveIlTagERitornaLEntita() {
        repository.save(new Tag("tag-1", "favourite"));

        Optional<Tag> deleted = repository.deleteById("tag-1");

        assertTrue(deleted.isPresent());
        assertEquals("tag-1", deleted.get().getId());
        assertTrue(repository.findById("tag-1").isEmpty());
    }

    @Test
    void deleteByIdAssenteRitornaEmpty() {
        assertTrue(repository.deleteById("inesistente").isEmpty());
    }

    @Test
    void deleteByIdTagAssociatoNonCompariPiuTraITagDellaTraccia() throws SQLException {
        insertTrack(new Track("track-1", "Titolo", "Autore", 100, "Pop", 2020));
        repository.save(new Tag("tag-1", "favourite"));
        repository.attach("track-1", "tag-1");

        repository.deleteById("tag-1");

        assertTrue(repository.findByTrackId("track-1").isEmpty());
    }
}