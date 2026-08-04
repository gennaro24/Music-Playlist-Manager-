package it.unisa.sad.playlistmanager.persistence.repository;

import it.unisa.sad.playlistmanager.domain.model.Tag;
import it.unisa.sad.playlistmanager.domain.model.Track;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Repository di tag in memoria per i test del layer applicativo.
 *
 * Implementazione completa del contratto {@link TagRepository}: mantiene il
 * catalogo dei tag e le associazioni traccia-tag. Risolve gli id traccia in
 * {@link Track} tramite il {@link TrackRepository} collegato.
 */
public class InMemoryTagRepository implements TagRepository {

    private final Map<String, Tag> tags = new LinkedHashMap<>();
    private final Map<String, Set<String>> tagIdsByTrackId = new LinkedHashMap<>();
    private final TrackRepository trackRepository;

    public InMemoryTagRepository(TrackRepository trackRepository) {
        this.trackRepository = trackRepository;
    }

    @Override
    public void save(Tag tag) {
        tags.put(tag.getId(), tag);
    }

    @Override
    public Optional<Tag> findById(String id) {
        return Optional.ofNullable(tags.get(id));
    }

    @Override
    public List<Tag> findAll() {
        return new ArrayList<>(tags.values());
    }

    @Override
    public Optional<Tag> deleteById(String id) {
        Tag removed = tags.remove(id);
        if (removed != null) {
            removeTagFromAllTracks(id);
        }
        return Optional.ofNullable(removed);
    }

    @Override
    public List<Tag> findByTrackId(String trackId) {
        Set<String> tagIds = tagIdsByTrackId.getOrDefault(trackId, Set.of());
        List<Tag> result = new ArrayList<>();
        for (String tagId : tagIds) {
            findById(tagId).ifPresent(result::add);
        }
        return result;
    }

    @Override
    public Optional<Tag> findByName(String name) {
        return tags.values().stream()
                .filter(tag -> tag.getName().equals(name))
                .findFirst();
    }

    @Override
    public boolean isAttached(String trackId, String tagId) {
        Set<String> tagIds = tagIdsByTrackId.get(trackId);
        return tagIds != null && tagIds.contains(tagId);
    }

    @Override
    public void attach(String trackId, String tagId) {
        tagIdsByTrackId.computeIfAbsent(trackId, ignored -> new LinkedHashSet<>()).add(tagId);
    }

    @Override
    public void detach(String trackId, String tagId) {
        Set<String> tagIds = tagIdsByTrackId.get(trackId);
        if (tagIds != null) {
            tagIds.remove(tagId);
        }
    }

    @Override
    public List<Track> findTracksByTagId(String tagId) {
        List<Track> result = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : tagIdsByTrackId.entrySet()) {
            if (entry.getValue().contains(tagId)) {
                trackRepository.findById(entry.getKey()).ifPresent(result::add);
            }
        }
        return result;
    }

    /** Rimuove tutte le associazioni di una traccia (delete a cascata). */
    void removeTrackFromAllTags(String trackId) {
        tagIdsByTrackId.remove(trackId);
    }

    private void removeTagFromAllTracks(String tagId) {
        for (Set<String> tagIds : tagIdsByTrackId.values()) {
            tagIds.remove(tagId);
        }
    }
}
