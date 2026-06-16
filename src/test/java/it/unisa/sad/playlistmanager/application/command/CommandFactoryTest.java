package it.unisa.sad.playlistmanager.application.command;

import it.unisa.sad.playlistmanager.application.command.concreteCommands.AddTrackCommand;
import it.unisa.sad.playlistmanager.application.command.concreteCommands.AddTrackToPlaylistCommand;
import it.unisa.sad.playlistmanager.application.command.concreteCommands.CreatePlaylistCommand;
import it.unisa.sad.playlistmanager.application.command.concreteCommands.DeletePlaylistCommand;
import it.unisa.sad.playlistmanager.application.command.concreteCommands.DeleteTrackCommand;
import it.unisa.sad.playlistmanager.application.command.concreteCommands.RemoveTrackFromPlaylistCommand;
import it.unisa.sad.playlistmanager.application.service.PlaylistService;
import it.unisa.sad.playlistmanager.application.service.TagService;
import it.unisa.sad.playlistmanager.application.service.TrackService;
import it.unisa.sad.playlistmanager.persistence.repository.InMemoryPlaylistRepository;
import it.unisa.sad.playlistmanager.persistence.repository.InMemoryTagRepository;
import it.unisa.sad.playlistmanager.persistence.repository.InMemoryTrackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test della factory dei command ({@link CommandFactory}).
 *
 * Verifica la validazione delle dipendenze nel costruttore e che ogni metodo di
 * creazione restituisca il tipo di command atteso. La factory costruisce e
 * configura i command ma non li esegue, quindi i test non controllano effetti
 * sul dominio.
 */
class CommandFactoryTest {

    private TrackService trackService;
    private PlaylistService playlistService;
    private TagService tagService;

    @BeforeEach
    void setUp() {
        InMemoryTrackRepository trackRepository = new InMemoryTrackRepository();
        InMemoryPlaylistRepository playlistRepository = new InMemoryPlaylistRepository(trackRepository);
        InMemoryTagRepository tagRepository = new InMemoryTagRepository(trackRepository);
        trackRepository.linkPlaylistRepository(playlistRepository);
        trackService = new TrackService(trackRepository);
        playlistService = new PlaylistService(playlistRepository, trackRepository);
        tagService = new TagService(tagRepository, trackRepository);
    }

    @Test
    void costruttoreRifiutaTrackServiceNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new CommandFactory(null, playlistService, null));
    }

    @Test
    void costruttoreRifiutaPlaylistServiceNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new CommandFactory(trackService, null, tagService));
    }

    @Test
    void costruttoreRifiutaTagServiceNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new CommandFactory(trackService, playlistService, null));
    }

    @Test
    void creaTuttiICommandDelTipoAtteso() {
        CommandFactory factory = new CommandFactory(trackService, playlistService, tagService);

        assertInstanceOf(AddTrackCommand.class,
                factory.createAddTrackCommand("Titolo", "Autore", 120, "Pop", 2020));
        assertInstanceOf(DeleteTrackCommand.class,
                factory.createDeleteTrackCommand("track-id"));
        assertInstanceOf(CreatePlaylistCommand.class,
                factory.createCreatePlaylistCommand("Nome"));
        assertInstanceOf(DeletePlaylistCommand.class,
                factory.createDeletePlaylistCommand("playlist-id"));
        assertInstanceOf(AddTrackToPlaylistCommand.class,
                factory.createAddTrackToPlaylistCommand("playlist-id", "track-id"));
        assertInstanceOf(RemoveTrackFromPlaylistCommand.class,
                factory.createRemoveTrackFromPlaylistCommand("playlist-id", "track-id"));
    }
}