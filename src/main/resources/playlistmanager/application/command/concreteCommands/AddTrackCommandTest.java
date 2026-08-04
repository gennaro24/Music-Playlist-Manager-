package it.unisa.sad.playlistmanager.application.command.concreteCommands;

import it.unisa.sad.playlistmanager.application.exceptions.TrackNotFoundException;
import it.unisa.sad.playlistmanager.application.service.TrackService;
import it.unisa.sad.playlistmanager.domain.model.Track;
import it.unisa.sad.playlistmanager.persistence.repository.InMemoryPlaylistRepository;
import it.unisa.sad.playlistmanager.persistence.repository.InMemoryTrackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test del command di aggiunta globale di una traccia ({@link AddTrackCommand}).
 *
 * Verifica che l'esecuzione crei e salvi la traccia conservandone l'entita',
 * che l'undo la rimuova dal catalogo e che l'undo ripetuto sia idempotente.
 */
class AddTrackCommandTest {

    private TrackService trackService;

    @BeforeEach
    void setUp() {
        InMemoryTrackRepository trackRepository = new InMemoryTrackRepository();
        InMemoryPlaylistRepository playlistRepository = new InMemoryPlaylistRepository(trackRepository);
        trackRepository.linkPlaylistRepository(playlistRepository);
        trackService = new TrackService(trackRepository);
    }

    @Test
    void executeCreaERegistraLaTraccia() {
        AddTrackCommand command = new AddTrackCommand(trackService, "Titolo", "Autore", 100, "Pop", 2020);

        command.execute();

        Track created = command.getCreatedTrack();
        assertNotNull(created);
        assertNotNull(created.getId());
        assertTrue(trackService.getAllTracks().contains(created));
    }

    @Test
    void undoRimuoveLaTracciaCreata() {
        AddTrackCommand command = new AddTrackCommand(trackService, "Titolo", "Autore", 100, "Pop", 2020);
        command.execute();
        String id = command.getCreatedTrack().getId();

        command.undo();

        assertTrue(trackService.getAllTracks().isEmpty());
        assertThrows(TrackNotFoundException.class, () -> trackService.getTrackById(id));
    }

    @Test
    void undoSenzaExecuteNonFaNulla() {
        AddTrackCommand command = new AddTrackCommand(trackService, "Titolo", "Autore", 100, "Pop", 2020);

        assertDoesNotThrow(command::undo);
        assertTrue(trackService.getAllTracks().isEmpty());
    }

    @Test
    void undoRipetutoRimuoveUnaSolaVolta() {
        AddTrackCommand command = new AddTrackCommand(trackService, "Titolo", "Autore", 100, "Pop", 2020);
        command.execute();

        command.undo();
        assertDoesNotThrow(command::undo);
        assertTrue(trackService.getAllTracks().isEmpty());
    }

    @Test
    void costruttoreRifiutaTrackServiceNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new AddTrackCommand(null, "Titolo", "Autore", 100, "Pop", 2020));
    }
}