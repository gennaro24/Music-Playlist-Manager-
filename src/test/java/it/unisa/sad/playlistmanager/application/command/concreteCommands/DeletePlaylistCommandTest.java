package it.unisa.sad.playlistmanager.application.command.concreteCommands;

import it.unisa.sad.playlistmanager.application.exceptions.PlaylistNotFoundException;
import it.unisa.sad.playlistmanager.application.service.PlaylistService;
import it.unisa.sad.playlistmanager.application.service.TrackService;
import it.unisa.sad.playlistmanager.domain.model.Playlist;
import it.unisa.sad.playlistmanager.domain.model.Track;
import it.unisa.sad.playlistmanager.persistence.repository.InMemoryPlaylistRepository;
import it.unisa.sad.playlistmanager.persistence.repository.InMemoryTrackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test del command di eliminazione playlist ({@link DeletePlaylistCommand}).
 *
 * Verifica che l'esecuzione elimini la playlist e che l'undo la ricrei con lo
 * stesso id ripristinando le tracce nello stesso ordine. Le tracce restano nel
 * catalogo durante tutta l'operazione.
 */
class DeletePlaylistCommandTest {

    private TrackService trackService;
    private PlaylistService playlistService;
    private Track first;
    private Track second;
    private Playlist playlist;

    @BeforeEach
    void setUp() {
        InMemoryTrackRepository trackRepository = new InMemoryTrackRepository();
        InMemoryPlaylistRepository playlistRepository = new InMemoryPlaylistRepository(trackRepository);
        trackRepository.linkPlaylistRepository(playlistRepository);
        trackService = new TrackService(trackRepository);
        playlistService = new PlaylistService(playlistRepository, trackRepository);

        first = trackService.addTrack("Prima", "Autore", 100, "Pop", 2020);
        second = trackService.addTrack("Seconda", "Autore", 100, "Pop", 2021);
        playlist = playlistService.createPlaylist("La mia playlist");
        playlistService.addTrackToPlaylist(playlist.getId(), first.getId());
        playlistService.addTrackToPlaylist(playlist.getId(), second.getId());
    }

    private List<String> trackIds(String playlistId) {
        return playlistService.getTracksForPlaylist(playlistId)
                .stream().map(Track::getId).collect(Collectors.toList());
    }

    @Test
    void executeEliminaLaPlaylist() {
        DeletePlaylistCommand command = new DeletePlaylistCommand(playlist.getId(), playlistService);

        command.execute();

        assertThrows(PlaylistNotFoundException.class,
                () -> playlistService.getPlaylistById(playlist.getId()));
    }

    @Test
    void undoRipristinaPlaylistETracceNelloStessoOrdine() {
        DeletePlaylistCommand command = new DeletePlaylistCommand(playlist.getId(), playlistService);
        command.execute();

        command.undo();

        Playlist restored = playlistService.getPlaylistById(playlist.getId());
        assertEquals(playlist.getId(), restored.getId());
        assertEquals(playlist.getName(), restored.getName());
        assertEquals(List.of(first.getId(), second.getId()), trackIds(playlist.getId()));
    }

    @Test
    void undoSenzaExecuteNonFaNulla() {
        DeletePlaylistCommand command = new DeletePlaylistCommand(playlist.getId(), playlistService);

        assertDoesNotThrow(command::undo);
        // La playlist originale e' ancora presente.
        assertNotNull(playlistService.getPlaylistById(playlist.getId()));
    }

    @Test
    void costruttoreRifiutaIdNonValidi() {
        assertThrows(IllegalArgumentException.class,
                () -> new DeletePlaylistCommand("  ", playlistService));
        assertThrows(IllegalArgumentException.class,
                () -> new DeletePlaylistCommand(playlist.getId(), null));
    }
}