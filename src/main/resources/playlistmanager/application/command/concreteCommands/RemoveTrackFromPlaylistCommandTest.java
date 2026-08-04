package it.unisa.sad.playlistmanager.application.command.concreteCommands;

import it.unisa.sad.playlistmanager.application.exceptions.ValidationException;
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
 * Test del command di rimozione traccia da playlist
 * ({@link RemoveTrackFromPlaylistCommand}).
 *
 * Verifica che l'esecuzione rimuova l'associazione e che l'undo reinserisca la
 * traccia esattamente nella posizione originale. Copre anche il caso di
 * esecuzione su una traccia non presente nella playlist.
 */
class RemoveTrackFromPlaylistCommandTest {

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

    private List<String> trackIds() {
        return playlistService.getTracksForPlaylist(playlist.getId())
                .stream().map(Track::getId).collect(Collectors.toList());
    }

    @Test
    void executeRimuoveLAssociazione() {
        RemoveTrackFromPlaylistCommand command =
                new RemoveTrackFromPlaylistCommand(playlistService, playlist.getId(), first.getId());

        command.execute();

        assertEquals(List.of(second.getId()), trackIds());
    }

    @Test
    void undoRipristinaLaTracciaNellaPosizioneOriginale() {
        RemoveTrackFromPlaylistCommand command =
                new RemoveTrackFromPlaylistCommand(playlistService, playlist.getId(), first.getId());
        command.execute();

        command.undo();

        assertEquals(List.of(first.getId(), second.getId()), trackIds());
    }

    @Test
    void undoRipetutoNonRipristinaDueVolte() {
        RemoveTrackFromPlaylistCommand command =
                new RemoveTrackFromPlaylistCommand(playlistService, playlist.getId(), first.getId());
        command.execute();

        command.undo();
        assertDoesNotThrow(command::undo);
        assertEquals(List.of(first.getId(), second.getId()), trackIds());
    }

    @Test
    void executeSuTracciaNonInPlaylistLanciaValidationException() {
        Track estranea = trackService.addTrack("Estranea", "Autore", 100, "Pop", 2022);
        RemoveTrackFromPlaylistCommand command =
                new RemoveTrackFromPlaylistCommand(playlistService, playlist.getId(), estranea.getId());

        assertThrows(ValidationException.class, command::execute);
    }

    @Test
    void costruttoreRifiutaIdNonValidi() {
        assertThrows(IllegalArgumentException.class,
                () -> new RemoveTrackFromPlaylistCommand(playlistService, "", first.getId()));
        assertThrows(IllegalArgumentException.class,
                () -> new RemoveTrackFromPlaylistCommand(null, playlist.getId(), first.getId()));
    }
}