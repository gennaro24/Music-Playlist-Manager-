package it.unisa.sad.playlistmanager.application.command.concreteCommands;

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
 * Test del command di aggiunta traccia a playlist ({@link AddTrackToPlaylistCommand}).
 *
 * Verifica che l'esecuzione crei la sola associazione playlist-traccia e che
 * l'undo la rimuova senza toccare il catalogo, in modo idempotente.
 */
class AddTrackToPlaylistCommandTest {

    private TrackService trackService;
    private PlaylistService playlistService;
    private Track track;
    private Playlist playlist;

    @BeforeEach
    void setUp() {
        InMemoryTrackRepository trackRepository = new InMemoryTrackRepository();
        InMemoryPlaylistRepository playlistRepository = new InMemoryPlaylistRepository(trackRepository);
        trackRepository.linkPlaylistRepository(playlistRepository);
        trackService = new TrackService(trackRepository);
        playlistService = new PlaylistService(playlistRepository, trackRepository);

        track = trackService.addTrack("Titolo", "Autore", 100, "Pop", 2020);
        playlist = playlistService.createPlaylist("La mia playlist");
    }

    private List<String> trackIds() {
        return playlistService.getTracksForPlaylist(playlist.getId())
                .stream().map(Track::getId).collect(Collectors.toList());
    }

    @Test
    void executeAggiungeLaTracciaAllaPlaylist() {
        AddTrackToPlaylistCommand command =
                new AddTrackToPlaylistCommand(playlistService, playlist.getId(), track.getId());

        command.execute();

        assertEquals(List.of(track.getId()), trackIds());
    }

    @Test
    void undoRimuoveSoloLAssociazione() {
        AddTrackToPlaylistCommand command =
                new AddTrackToPlaylistCommand(playlistService, playlist.getId(), track.getId());
        command.execute();

        command.undo();

        assertTrue(trackIds().isEmpty());
        // La traccia resta nel catalogo.
        assertNotNull(trackService.getTrackById(track.getId()));
    }

    @Test
    void undoRipetutoNonRimuoveDueVolte() {
        AddTrackToPlaylistCommand command =
                new AddTrackToPlaylistCommand(playlistService, playlist.getId(), track.getId());
        command.execute();

        command.undo();
        assertDoesNotThrow(command::undo);
        assertTrue(trackIds().isEmpty());
    }

    @Test
    void costruttoreRifiutaIdNonValidi() {
        assertThrows(IllegalArgumentException.class,
                () -> new AddTrackToPlaylistCommand(playlistService, "  ", track.getId()));
        assertThrows(IllegalArgumentException.class,
                () -> new AddTrackToPlaylistCommand(playlistService, playlist.getId(), null));
        assertThrows(IllegalArgumentException.class,
                () -> new AddTrackToPlaylistCommand(null, playlist.getId(), track.getId()));
    }
}