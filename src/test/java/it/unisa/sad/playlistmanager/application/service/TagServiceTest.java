package it.unisa.sad.playlistmanager.application.service;

import it.unisa.sad.playlistmanager.application.exceptions.TagNotFoundException;
import it.unisa.sad.playlistmanager.application.exceptions.TrackNotFoundException;
import it.unisa.sad.playlistmanager.application.exceptions.ValidationException;
import it.unisa.sad.playlistmanager.domain.model.Tag;
import it.unisa.sad.playlistmanager.domain.model.Track;
import it.unisa.sad.playlistmanager.persistence.repository.InMemoryTagRepository;
import it.unisa.sad.playlistmanager.persistence.repository.InMemoryTrackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test del servizio applicativo dei tag ({@link TagService}).
 *
 * Copre i tre scenari della US-27 (assegnazione, rimozione e tag multipli) e i
 * relativi casi limite: tag o tracce inesistenti, assegnazioni duplicate,
 * rimozione di un tag non assegnato e validazione degli id. I test girano sui
 * service reali montati su repository in memoria, senza mock.
 */
class TagServiceTest {

    private TrackService trackService;
    private TagService tagService;

    @BeforeEach
    void setUp() {
        InMemoryTrackRepository trackRepository = new InMemoryTrackRepository();
        InMemoryTagRepository tagRepository = new InMemoryTagRepository(trackRepository);
        trackService = new TrackService(trackRepository);
        tagService = new TagService(tagRepository, trackRepository);
    }

    private Track newTrack() {
        return trackService.addTrack("Titolo", "Autore", 100, "Pop", 2020);
    }

    private Set<String> tagIds(List<Tag> tags) {
        return tags.stream().map(Tag::getId).collect(Collectors.toSet());
    }

    private Set<String> trackIds(List<Track> tracks) {
        return tracks.stream().map(Track::getId).collect(Collectors.toSet());
    }

    // ===== addTag =====

    @Test
    void addTagCreaERegistraIlTag() {
        Tag tag = tagService.addTag("favourite");

        assertNotNull(tag.getId());
        assertEquals("favourite", tag.getName());
        assertTrue(tagService.getAllTags().contains(tag));
    }

    @Test
    void addTagConNomeDuplicatoLanciaValidationException() {
        tagService.addTag("favourite");
        assertThrows(ValidationException.class, () -> tagService.addTag("favourite"));
    }

    @Test
    void addTagConNomeVuotoLanciaValidationException() {
        assertThrows(ValidationException.class, () -> tagService.addTag("   "));
    }

    // ===== Scenario 1: assegnazione tag =====

    @Test
    void assignTagToTrackAssociaIlTagAllaTraccia() {
        Track track = newTrack();
        Tag tag = tagService.addTag("explicit");

        tagService.assignTagToTrack(track.getId(), tag.getId());

        assertEquals(Set.of(tag.getId()), tagIds(tagService.getTagsForTrack(track.getId())));
    }

    @Test
    void assignTagGiaAssegnatoLanciaValidationException() {
        Track track = newTrack();
        Tag tag = tagService.addTag("explicit");
        tagService.assignTagToTrack(track.getId(), tag.getId());

        assertThrows(ValidationException.class,
                () -> tagService.assignTagToTrack(track.getId(), tag.getId()));
    }

    @Test
    void assignTagSuTracciaInesistenteLanciaTrackNotFoundException() {
        Tag tag = tagService.addTag("explicit");
        assertThrows(TrackNotFoundException.class,
                () -> tagService.assignTagToTrack("track-inesistente", tag.getId()));
    }

    @Test
    void assignTagInesistenteLanciaTagNotFoundException() {
        Track track = newTrack();
        assertThrows(TagNotFoundException.class,
                () -> tagService.assignTagToTrack(track.getId(), "tag-inesistente"));
    }

    @Test
    void assignTagConIdNulloLanciaValidationException() {
        Track track = newTrack();
        Tag tag = tagService.addTag("explicit");
        assertThrows(ValidationException.class,
                () -> tagService.assignTagToTrack(null, tag.getId()));
        assertThrows(ValidationException.class,
                () -> tagService.assignTagToTrack(track.getId(), "  "));
    }

    // ===== Scenario 3: tag multipli =====

    @Test
    void getTagsForTrackRitornaTuttiITagAssegnati() {
        Track track = newTrack();
        Tag favourite = tagService.addTag("favourite");
        Tag explicit = tagService.addTag("explicit");
        Tag newRelease = tagService.addTag("new release");
        tagService.assignTagToTrack(track.getId(), favourite.getId());
        tagService.assignTagToTrack(track.getId(), explicit.getId());
        tagService.assignTagToTrack(track.getId(), newRelease.getId());

        assertEquals(
                Set.of(favourite.getId(), explicit.getId(), newRelease.getId()),
                tagIds(tagService.getTagsForTrack(track.getId())));
    }

    @Test
    void getTagsForTrackVuotoSeNessunTag() {
        Track track = newTrack();
        assertTrue(tagService.getTagsForTrack(track.getId()).isEmpty());
    }

    @Test
    void getTagsForTrackSuTracciaInesistenteLanciaTrackNotFoundException() {
        assertThrows(TrackNotFoundException.class,
                () -> tagService.getTagsForTrack("track-inesistente"));
    }

    // ===== Scenario 2: rimozione tag =====

    @Test
    void removeTagFromTrackRimuoveSoloIlTagIndicato() {
        Track track = newTrack();
        Tag favourite = tagService.addTag("favourite");
        Tag explicit = tagService.addTag("explicit");
        tagService.assignTagToTrack(track.getId(), favourite.getId());
        tagService.assignTagToTrack(track.getId(), explicit.getId());

        tagService.removeTagFromTrack(track.getId(), favourite.getId());

        assertEquals(Set.of(explicit.getId()), tagIds(tagService.getTagsForTrack(track.getId())));
    }

    @Test
    void removeTagNonAssegnatoLanciaValidationException() {
        Track track = newTrack();
        Tag tag = tagService.addTag("favourite");

        assertThrows(ValidationException.class,
                () -> tagService.removeTagFromTrack(track.getId(), tag.getId()));
    }

    // ===== getTracksByTag =====

    @Test
    void getTracksByTagRitornaSoloLeTracceConQuelTag() {
        Track withTag1 = newTrack();
        Track withTag2 = newTrack();
        Track withoutTag = newTrack();
        Tag tag = tagService.addTag("favourite");
        tagService.assignTagToTrack(withTag1.getId(), tag.getId());
        tagService.assignTagToTrack(withTag2.getId(), tag.getId());

        Set<String> result = trackIds(tagService.getTracksByTag(tag.getId()));

        assertEquals(Set.of(withTag1.getId(), withTag2.getId()), result);
        assertFalse(result.contains(withoutTag.getId()));
    }

    @Test
    void getTracksByTagInesistenteLanciaTagNotFoundException() {
        assertThrows(TagNotFoundException.class,
                () -> tagService.getTracksByTag("tag-inesistente"));
    }

    // ===== deleteTag =====

    @Test
    void deleteTagRimuoveIlTagELeSueAssociazioni() {
        Track track = newTrack();
        Tag tag = tagService.addTag("favourite");
        tagService.assignTagToTrack(track.getId(), tag.getId());

        Tag deleted = tagService.deleteTag(tag.getId());

        assertEquals(tag.getId(), deleted.getId());
        assertFalse(tagService.getAllTags().contains(tag));
        assertTrue(tagService.getTagsForTrack(track.getId()).isEmpty());
    }

    @Test
    void deleteTagInesistenteLanciaTagNotFoundException() {
        assertThrows(TagNotFoundException.class, () -> tagService.deleteTag("tag-inesistente"));
    }

    @Test
    void deleteTagConIdNulloLanciaValidationException() {
        assertThrows(ValidationException.class, () -> tagService.deleteTag(null));
    }

    // ===== getAllTags =====

    @Test
    void getAllTagsRitornaTuttiITagCreati() {
        Tag a = tagService.addTag("favourite");
        Tag b = tagService.addTag("explicit");

        assertEquals(Set.of(a.getId(), b.getId()), tagIds(tagService.getAllTags()));
    }
}