package it.unisa.sad.playlistmanager.application.service;

import it.unisa.sad.playlistmanager.application.exceptions.ValidationException;
import it.unisa.sad.playlistmanager.domain.model.AutoPlaylistCriteria;
import it.unisa.sad.playlistmanager.domain.model.Tag;
import it.unisa.sad.playlistmanager.domain.model.Track;
import it.unisa.sad.playlistmanager.persistence.repository.InMemoryPlaylistRepository;
import it.unisa.sad.playlistmanager.persistence.repository.InMemoryTagRepository;
import it.unisa.sad.playlistmanager.persistence.repository.InMemoryTrackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test di {@link AutoPlaylistService#previewAutoPlaylist} (US-28).
 *
 * Copre i criteri singoli (genere, anno, tag), i criteri combinati interpretati
 * in AND e lo scenario "nessun risultato", che deve restituire una lista vuota
 * senza creare alcuna playlist. I test girano su service reali montati su
 * repository in memoria.
 */
class AutoPlaylistServiceTest {

    private PlaylistService playlistService;
    private AutoPlaylistService autoPlaylistService;

    private Track rock2020;
    private Track rock2021;
    private Track pop2020;
    private Track pop2022;
    private Track jazz2020;
    private Tag favourite;

    @BeforeEach
    void setUp() {
        InMemoryTrackRepository trackRepository = new InMemoryTrackRepository();
        InMemoryPlaylistRepository playlistRepository = new InMemoryPlaylistRepository(trackRepository);
        trackRepository.linkPlaylistRepository(playlistRepository);
        InMemoryTagRepository tagRepository = new InMemoryTagRepository(trackRepository);

        TrackService trackService = new TrackService(trackRepository);
        TagService tagService = new TagService(tagRepository, trackRepository);
        playlistService = new PlaylistService(playlistRepository, trackRepository);
        autoPlaylistService = new AutoPlaylistService(trackService, tagService, playlistService);

        rock2020 = trackService.addTrack("Rock 2020", "Autore", 100, "Rock", 2020);
        rock2021 = trackService.addTrack("Rock 2021", "Autore", 100, "Rock", 2021);
        pop2020 = trackService.addTrack("Pop 2020", "Autore", 100, "Pop", 2020);
        pop2022 = trackService.addTrack("Pop 2022", "Autore", 100, "Pop", 2022);
        jazz2020 = trackService.addTrack("Jazz 2020", "Autore", 100, "Jazz", 2020);

        favourite = tagService.addTag("favourite");
        tagService.assignTagToTrack(rock2020.getId(), favourite.getId());
        tagService.assignTagToTrack(pop2020.getId(), favourite.getId());
    }

    private Set<String> ids(List<Track> tracks) {
        return tracks.stream().map(Track::getId).collect(Collectors.toSet());
    }

    // ===== Scenario 1: per genere =====

    @Test
    void previewPerGenereRitornaSoloLeTracceDelGenere() {
        List<Track> result = autoPlaylistService.previewAutoPlaylist(AutoPlaylistCriteria.byGenre("Rock"));
        assertEquals(Set.of(rock2020.getId(), rock2021.getId()), ids(result));
    }

    @Test
    void previewPerGenereECaseInsensitive() {
        List<Track> result = autoPlaylistService.previewAutoPlaylist(AutoPlaylistCriteria.byGenre("rock"));
        assertEquals(Set.of(rock2020.getId(), rock2021.getId()), ids(result));
    }

    // ===== Scenario 2: per anno =====

    @Test
    void previewPerAnnoRitornaSoloLeTracceDellAnno() {
        List<Track> result = autoPlaylistService.previewAutoPlaylist(AutoPlaylistCriteria.byYear(2020));
        assertEquals(Set.of(rock2020.getId(), pop2020.getId(), jazz2020.getId()), ids(result));
    }

    // ===== per tag =====

    @Test
    void previewPerTagRitornaSoloLeTracceTaggate() {
        List<Track> result = autoPlaylistService.previewAutoPlaylist(AutoPlaylistCriteria.byTag(favourite.getId()));
        assertEquals(Set.of(rock2020.getId(), pop2020.getId()), ids(result));
    }

    // ===== criteri combinati (AND) =====

    @Test
    void previewCombinatoGenereEAnno() {
        List<Track> result = autoPlaylistService.previewAutoPlaylist(
                AutoPlaylistCriteria.combined("Rock", 2020, null));
        assertEquals(Set.of(rock2020.getId()), ids(result));
    }

    @Test
    void previewCombinatoGenereETag() {
        List<Track> result = autoPlaylistService.previewAutoPlaylist(
                AutoPlaylistCriteria.combined("Pop", null, favourite.getId()));
        assertEquals(Set.of(pop2020.getId()), ids(result));
    }

    @Test
    void previewCombinatoAnnoETag() {
        List<Track> result = autoPlaylistService.previewAutoPlaylist(
                AutoPlaylistCriteria.combined(null, 2020, favourite.getId()));
        assertEquals(Set.of(rock2020.getId(), pop2020.getId()), ids(result));
    }

    @Test
    void previewCombinatoGenereAnnoETag() {
        List<Track> result = autoPlaylistService.previewAutoPlaylist(
                AutoPlaylistCriteria.combined("Rock", 2020, favourite.getId()));
        assertEquals(Set.of(rock2020.getId()), ids(result));
    }

    // ===== Scenario 3: nessun risultato =====

    @Test
    void previewSenzaCorrispondenzeRitornaListaVuota() {
        assertTrue(autoPlaylistService.previewAutoPlaylist(AutoPlaylistCriteria.byGenre("Metal")).isEmpty());
        assertTrue(autoPlaylistService.previewAutoPlaylist(AutoPlaylistCriteria.byYear(1999)).isEmpty());
        assertTrue(autoPlaylistService.previewAutoPlaylist(
                AutoPlaylistCriteria.combined("Jazz", 2021, null)).isEmpty());
    }

    @Test
    void previewNonCreaAlcunaPlaylist() {
        autoPlaylistService.previewAutoPlaylist(AutoPlaylistCriteria.byGenre("Rock"));
        autoPlaylistService.previewAutoPlaylist(AutoPlaylistCriteria.byGenre("Metal"));
        assertTrue(playlistService.getAllPlaylists().isEmpty());
    }

    // ===== validazione =====

    @Test
    void previewConCriteriaNullLanciaValidationException() {
        assertThrows(ValidationException.class, () -> autoPlaylistService.previewAutoPlaylist(null));
    }
}