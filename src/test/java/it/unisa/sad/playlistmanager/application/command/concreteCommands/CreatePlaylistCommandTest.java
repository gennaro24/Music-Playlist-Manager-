package it.unisa.sad.playlistmanager.application.command.concreteCommands;

import it.unisa.sad.playlistmanager.application.exceptions.PlaylistNotFoundException;
import it.unisa.sad.playlistmanager.application.service.PlaylistService;
import it.unisa.sad.playlistmanager.domain.model.Playlist;
import it.unisa.sad.playlistmanager.persistence.repository.InMemoryPlaylistRepository;
import it.unisa.sad.playlistmanager.persistence.repository.InMemoryTrackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test del command di creazione playlist ({@link CreatePlaylistCommand}).
 *
 * Verifica che l'esecuzione crei la playlist conservandone l'entita' con l'id
 * effettivo e che l'undo la elimini usando quell'id.
 */
class CreatePlaylistCommandTest {

    private PlaylistService playlistService;

    @BeforeEach
    void setUp() {
        InMemoryTrackRepository trackRepository = new InMemoryTrackRepository();
        InMemoryPlaylistRepository playlistRepository = new InMemoryPlaylistRepository(trackRepository);
        trackRepository.linkPlaylistRepository(playlistRepository);
        playlistService = new PlaylistService(playlistRepository, trackRepository);
    }

    @Test
    void executeCreaLaPlaylist() {
        CreatePlaylistCommand command = new CreatePlaylistCommand("La mia playlist", playlistService);

        command.execute();

        Playlist created = command.getCreatedPlaylist();
        assertNotNull(created);
        assertEquals(created.getId(), playlistService.getPlaylistById(created.getId()).getId());
    }

    @Test
    void undoEliminaLaPlaylistCreata() {
        CreatePlaylistCommand command = new CreatePlaylistCommand("La mia playlist", playlistService);
        command.execute();
        String id = command.getCreatedPlaylist().getId();

        command.undo();

        assertThrows(PlaylistNotFoundException.class, () -> playlistService.getPlaylistById(id));
    }

    @Test
    void undoSenzaExecuteNonFaNulla() {
        CreatePlaylistCommand command = new CreatePlaylistCommand("La mia playlist", playlistService);

        assertDoesNotThrow(command::undo);
        assertTrue(playlistService.getAllPlaylists().isEmpty());
    }

    @Test
    void costruttoreRifiutaNomeVuoto() {
        assertThrows(IllegalArgumentException.class,
                () -> new CreatePlaylistCommand("   ", playlistService));
    }

    @Test
    void costruttoreRifiutaPlaylistServiceNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new CreatePlaylistCommand("Nome", null));
    }
}