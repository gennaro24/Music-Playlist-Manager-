package it.unisa.sad.playlistmanager.application.command.concreteCommands;

import it.unisa.sad.playlistmanager.application.exceptions.TrackNotFoundException;
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
 * Test del command di eliminazione globale di una traccia
 * ({@link DeleteTrackCommand}).
 *
 * Verifica che l'esecuzione elimini la traccia dal catalogo (rimuovendone a
 * cascata le associazioni nelle playlist) e che l'undo ripristini sia l'entita'
 * sia le associazioni nelle posizioni originali tramite un'unica operazione
 * atomica ({@code TrackRepository.restoreWithPlaylistPositions}).
 *
 * Vengono inoltre verificati il rollback e il comportamento di retry quando
 * il ripristino fallisce: lo snapshot interno del command (traccia eliminata
 * e posizioni nelle playlist) deve restare popolato in modo che un successivo
 * {@code undo()} possa ritentare l'operazione.
 */
class DeleteTrackCommandTest {

    private TrackService trackService;
    private PlaylistService playlistService;
    private InMemoryTrackRepository trackRepository;
    private Track track;
    private Playlist playlist;

    @BeforeEach
    void setUp() {
        trackRepository = new InMemoryTrackRepository();
        InMemoryPlaylistRepository playlistRepository = new InMemoryPlaylistRepository(trackRepository);
        trackRepository.linkPlaylistRepository(playlistRepository);
        trackService = new TrackService(trackRepository);
        playlistService = new PlaylistService(playlistRepository, trackRepository);

        track = trackService.addTrack("Titolo", "Autore", 123, "Jazz", 2019);
        playlist = playlistService.createPlaylist("La mia playlist");
        playlistService.addTrackToPlaylist(playlist.getId(), track.getId());
    }

    private List<String> trackIds() {
        return playlistService.getTracksForPlaylist(playlist.getId())
                .stream().map(Track::getId).collect(Collectors.toList());
    }

    @Test
    void executeEliminaTracciaDalCatalogoEDallePlaylist() {
        DeleteTrackCommand command = new DeleteTrackCommand(trackService, playlistService, track.getId());

        command.execute();

        assertThrows(TrackNotFoundException.class, () -> trackService.getTrackById(track.getId()));
        assertTrue(trackIds().isEmpty());
    }

    @Test
    void undoRipristinaTracciaConStessiCampi() {
        DeleteTrackCommand command = new DeleteTrackCommand(trackService, playlistService, track.getId());
        command.execute();

        command.undo();

        Track restored = trackService.getTrackById(track.getId());
        assertEquals("Titolo", restored.getTitle());
        assertEquals("Autore", restored.getAuthor());
        assertEquals(123, restored.getDuration());
        assertEquals("Jazz", restored.getGenre());
        assertEquals(2019, restored.getYear());
    }

    @Test
    void undoRipristinaLAssociazioneNellaPosizioneOriginale() {
        DeleteTrackCommand command = new DeleteTrackCommand(trackService, playlistService, track.getId());
        command.execute();

        command.undo();

        assertEquals(List.of(track.getId()), trackIds());
    }

    @Test
    void undoSenzaExecuteNonFaNulla() {
        DeleteTrackCommand command = new DeleteTrackCommand(trackService, playlistService, track.getId());

        assertDoesNotThrow(command::undo);
        // La traccia originale e' ancora nel catalogo.
        assertNotNull(trackService.getTrackById(track.getId()));
    }

    @Test
    void costruttoreRifiutaParametriNonValidi() {
        assertThrows(IllegalArgumentException.class,
                () -> new DeleteTrackCommand(null, playlistService, track.getId()));
        assertThrows(IllegalArgumentException.class,
                () -> new DeleteTrackCommand(trackService, null, track.getId()));
        assertThrows(IllegalArgumentException.class,
                () -> new DeleteTrackCommand(trackService, playlistService, "  "));
    }

    /**
     * Se {@code restoreWithPlaylistPositions} fallisce, l'eccezione deve
     * propagare e lo snapshot interno (traccia eliminata e posizioni nelle
     * playlist) deve restare popolato, in modo che UndoManager possa
     * conservare il command e ritentare l'undo in seguito.
     */
    @Test
    void undoConRestoreFallitoPropagaEccezioneEMantieneLoSnapshot() {
        DeleteTrackCommand command = new DeleteTrackCommand(trackService, playlistService, track.getId());
        command.execute();

        trackRepository.setRestoreFailure(() -> new RuntimeException("Fallimento simulato del restore"));

        assertThrows(RuntimeException.class, command::undo);

        // Lo snapshot non deve essere stato azzerato: serve per il retry.
        assertNotNull(command.getDeletedTrack());
        assertEquals(track.getId(), command.getDeletedTrack().getId());

        // Lo stato non deve restare a meta': il rollback simulato riporta lo
        // storage com'era prima del tentativo (traccia ancora assente).
        assertThrows(TrackNotFoundException.class, () -> trackService.getTrackById(track.getId()));
        assertTrue(trackIds().isEmpty());
    }

    /**
     * Dopo un primo fallimento, un secondo {@code undo()} con il repository
     * "guarito" deve ripristinare correttamente sia la traccia sia le
     * associazioni, usando lo snapshot conservato dal primo tentativo.
     */
    @Test
    void undoRitentatoDopoFallimentoRipristinaCorrettamente() {
        DeleteTrackCommand command = new DeleteTrackCommand(trackService, playlistService, track.getId());
        command.execute();

        trackRepository.setRestoreFailure(() -> new RuntimeException("Fallimento simulato del restore"));
        assertThrows(RuntimeException.class, command::undo);

        // Il repository torna a funzionare normalmente.
        trackRepository.setRestoreFailure(null);
        command.undo();

        Track restored = trackService.getTrackById(track.getId());
        assertEquals("Titolo", restored.getTitle());
        assertEquals(List.of(track.getId()), trackIds());

        // Dopo il retry riuscito, lo snapshot viene azzerato.
        assertNull(command.getDeletedTrack());
    }
}