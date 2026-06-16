package it.unisa.sad.playlistmanager.application.command.concreteCommands;

import it.unisa.sad.playlistmanager.application.exceptions.TrackNotFoundException;
import it.unisa.sad.playlistmanager.application.service.PlaylistService;
import it.unisa.sad.playlistmanager.application.service.TagService;
import it.unisa.sad.playlistmanager.application.service.TrackService;
import it.unisa.sad.playlistmanager.domain.model.Playlist;
import it.unisa.sad.playlistmanager.domain.model.Tag;
import it.unisa.sad.playlistmanager.domain.model.Track;
import it.unisa.sad.playlistmanager.persistence.repository.InMemoryPlaylistRepository;
import it.unisa.sad.playlistmanager.persistence.repository.InMemoryTagRepository;
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
 * cascata le associazioni nelle playlist e i tag) e che l'undo ripristini
 * entita', playlist e tag tramite un'unica operazione atomica
 * ({@code TrackRepository.restoreWithPlaylistPositions}).
 */
class DeleteTrackCommandTest {

    private TrackService trackService;
    private PlaylistService playlistService;
    private TagService tagService;
    private InMemoryTrackRepository trackRepository;
    private Track track;
    private Playlist playlist;
    private Tag tag;

    @BeforeEach
    void setUp() {
        trackRepository = new InMemoryTrackRepository();
        InMemoryPlaylistRepository playlistRepository = new InMemoryPlaylistRepository(trackRepository);
        InMemoryTagRepository tagRepository = new InMemoryTagRepository(trackRepository);
        trackRepository.linkPlaylistRepository(playlistRepository);
        trackRepository.linkTagRepository(tagRepository);
        trackService = new TrackService(trackRepository);
        playlistService = new PlaylistService(playlistRepository, trackRepository);
        tagService = new TagService(tagRepository, trackRepository);

        track = trackService.addTrack("Titolo", "Autore", 123, "Jazz", 2019);
        playlist = playlistService.createPlaylist("La mia playlist");
        playlistService.addTrackToPlaylist(playlist.getId(), track.getId());
        tag = tagService.addTag("Preferita");
        tagService.assignTagToTrack(track.getId(), tag.getId());
    }

    private DeleteTrackCommand newCommand() {
        return new DeleteTrackCommand(trackService, playlistService, tagService, track.getId());
    }

    private List<String> trackIds() {
        return playlistService.getTracksForPlaylist(playlist.getId())
                .stream().map(Track::getId).collect(Collectors.toList());
    }

    @Test
    void executeEliminaTracciaDalCatalogoEDallePlaylist() {
        DeleteTrackCommand command = newCommand();

        command.execute();

        assertThrows(TrackNotFoundException.class, () -> trackService.getTrackById(track.getId()));
        assertTrue(trackIds().isEmpty());
        assertThrows(TrackNotFoundException.class, () -> tagService.getTagsForTrack(track.getId()));
    }

    @Test
    void undoRipristinaTracciaConStessiCampi() {
        DeleteTrackCommand command = newCommand();
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
        DeleteTrackCommand command = newCommand();
        command.execute();

        command.undo();

        assertEquals(List.of(track.getId()), trackIds());
    }

    @Test
    void undoRipristinaITagAssegnati() {
        DeleteTrackCommand command = newCommand();
        command.execute();

        command.undo();

        List<Tag> restoredTags = tagService.getTagsForTrack(track.getId());
        assertEquals(1, restoredTags.size());
        assertEquals(tag.getId(), restoredTags.get(0).getId());
        assertEquals("Preferita", restoredTags.get(0).getName());
    }

    @Test
    void undoSenzaExecuteNonFaNulla() {
        DeleteTrackCommand command = newCommand();

        assertDoesNotThrow(command::undo);
        assertNotNull(trackService.getTrackById(track.getId()));
    }

    @Test
    void costruttoreRifiutaParametriNonValidi() {
        assertThrows(IllegalArgumentException.class,
                () -> new DeleteTrackCommand(null, playlistService, tagService, track.getId()));
        assertThrows(IllegalArgumentException.class,
                () -> new DeleteTrackCommand(trackService, null, tagService, track.getId()));
        assertThrows(IllegalArgumentException.class,
                () -> new DeleteTrackCommand(trackService, playlistService, null, track.getId()));
        assertThrows(IllegalArgumentException.class,
                () -> new DeleteTrackCommand(trackService, playlistService, tagService, "  "));
    }

    @Test
    void undoConRestoreFallitoPropagaEccezioneEMantieneLoSnapshot() {
        DeleteTrackCommand command = newCommand();
        command.execute();

        trackRepository.setRestoreFailure(() -> new RuntimeException("Fallimento simulato del restore"));

        assertThrows(RuntimeException.class, command::undo);

        assertNotNull(command.getDeletedTrack());
        assertEquals(track.getId(), command.getDeletedTrack().getId());

        assertThrows(TrackNotFoundException.class, () -> trackService.getTrackById(track.getId()));
        assertTrue(trackIds().isEmpty());
    }

    @Test
    void undoRitentatoDopoFallimentoRipristinaCorrettamente() {
        DeleteTrackCommand command = newCommand();
        command.execute();

        trackRepository.setRestoreFailure(() -> new RuntimeException("Fallimento simulato del restore"));
        assertThrows(RuntimeException.class, command::undo);

        trackRepository.setRestoreFailure(null);
        command.undo();

        Track restored = trackService.getTrackById(track.getId());
        assertEquals("Titolo", restored.getTitle());
        assertEquals(List.of(track.getId()), trackIds());
        assertEquals(tag.getId(), tagService.getTagsForTrack(track.getId()).get(0).getId());

        assertNull(command.getDeletedTrack());
    }
}
