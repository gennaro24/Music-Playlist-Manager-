package it.unisa.sad.playlistmanager.application.facade;

import it.unisa.sad.playlistmanager.application.command.CommandFactory;
import it.unisa.sad.playlistmanager.application.command.UndoManager;
import it.unisa.sad.playlistmanager.application.exceptions.PlaylistNotFoundException;
import it.unisa.sad.playlistmanager.application.exceptions.TrackNotFoundException;
import it.unisa.sad.playlistmanager.application.service.FakePlaybackService; // IMPORTATO DALLA NUOVA CARTELLA
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
 * Test di integrazione dell'undo esposto dalla facade
 * ({@code undoLastAction()} e {@code canUndo()}).
 */
class MusicPlaylistManagerFacadeUndoTest {

    private TrackService trackService;
    private PlaylistService playlistService;
    private FakePlaybackService fakePlaybackService; // Dichiarazione del nostro fake
    private MusicPlaylistManagerFacade facade;

    @BeforeEach
    void setUp() {
        InMemoryTrackRepository trackRepository = new InMemoryTrackRepository();
        InMemoryPlaylistRepository playlistRepository = new InMemoryPlaylistRepository(trackRepository);
        trackRepository.linkPlaylistRepository(playlistRepository);
        trackService = new TrackService(trackRepository);
        playlistService = new PlaylistService(playlistRepository, trackRepository);
        
        // Istanziamo il fake creato nella nuova cartella
        fakePlaybackService = new FakePlaybackService();

        CommandFactory commandFactory = new CommandFactory(trackService, playlistService);
        
        // Passiamo il fake al posto di null
        facade = new MusicPlaylistManagerFacade(
                trackService, playlistService, fakePlaybackService, commandFactory, new UndoManager());
    }

    @Test
    void canUndoFalseAllInizio() {
        assertFalse(facade.canUndo());
    }

    @Test
    void addTrackRegistraEUndoLaRimuove() {
        Track created = facade.addTrack("Titolo", "Autore", 100, "Pop", 2020);
        assertTrue(facade.canUndo());

        facade.undoLastAction();

        assertFalse(facade.canUndo());
        assertTrue(facade.getAllTracks().isEmpty());
        assertThrows(TrackNotFoundException.class, () -> trackService.getTrackById(created.getId()));
    }

    @Test
    void operazioniMultipleAnnullateInOrdineLifo() {
        Playlist playlist = facade.createPlaylist("La mia playlist");
        Track track = facade.addTrack("Titolo", "Autore", 100, "Pop", 2020);

        facade.undoLastAction();
        assertTrue(facade.getAllTracks().isEmpty());
        assertNotNull(playlistService.getPlaylistById(playlist.getId()));

        facade.undoLastAction();
        assertFalse(facade.canUndo());
        assertThrows(PlaylistNotFoundException.class,
                () -> playlistService.getPlaylistById(playlist.getId()));
    }

    @Test
    void undoDellAssociazionePlaylistTraccia() {
        Track track = facade.addTrack("Titolo", "Autore", 100, "Pop", 2020);
        Playlist playlist = facade.createPlaylist("La mia playlist");
        facade.addTrackToPlaylist(playlist.getId(), track.getId());

        assertEquals(List.of(track.getId()), trackIds(playlist.getId()));

        facade.undoLastAction(); 

        assertTrue(trackIds(playlist.getId()).isEmpty());
        assertNotNull(trackService.getTrackById(track.getId()));
    }

    @Test
    void undoLastActionSuCronologiaVuotaNonFaNulla() {
        assertDoesNotThrow(facade::undoLastAction);
        assertFalse(facade.canUndo());
    }

    // =========================================================================
    // NUOVI TEST PER VERIFICARE L'UNDO DI DELETE TRACK E PLAYLIST
    // =========================================================================

    @Test
    void deleteTrackNotificaPlaybackEUndoLaRipristina() {
        Track track = facade.addTrack("Traccia da eliminare", "Artista", 120, "Rock", 2022);
        String trackId = track.getId();

        // Eliminiamo la traccia
        facade.deleteTrack(trackId);

        // Verifichiamo che il fake abbia intercettato il passaggio
        assertTrue(fakePlaybackService.isHandleDeletedTrackCalled());
        assertEquals(trackId, fakePlaybackService.getLastDeletedTrackId());

        // Eseguiamo l'undo e verifichiamo il ripristino nel catalogo reale
        facade.undoLastAction();
        assertFalse(facade.getAllTracks().isEmpty());
        assertNotNull(trackService.getTrackById(trackId));
    }

    @Test
    void deletePlaylistNotificaPlaybackEUndoLaRipristina() {
        Playlist playlist = facade.createPlaylist("Playlist da eliminare");
        String playlistId = playlist.getId();

        // Eliminiamo la playlist
        facade.deletePlaylist(playlistId);

        // Verifichiamo che il fake abbia intercettato il passaggio
        assertTrue(fakePlaybackService.isHandleDeletedPlaylistCalled());
        assertEquals(playlistId, fakePlaybackService.getLastDeletedPlaylistId());

        // Eseguiamo l'undo e verifichiamo il ripristino
        facade.undoLastAction();
        assertFalse(facade.getAllPlaylists().isEmpty());
        assertNotNull(playlistService.getPlaylistById(playlistId));
    }

    private List<String> trackIds(String playlistId) {
        return facade.getTracksForPlaylist(playlistId)
                .stream().map(Track::getId).collect(Collectors.toList());
    }
}