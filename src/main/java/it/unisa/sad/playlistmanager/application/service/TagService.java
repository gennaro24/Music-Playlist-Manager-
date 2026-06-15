package it.unisa.sad.playlistmanager.application.service;

import it.unisa.sad.playlistmanager.persistence.repository.TagRepository;
import it.unisa.sad.playlistmanager.domain.model.Tag;
import it.unisa.sad.playlistmanager.application.exceptions.TagNotFoundException;
import it.unisa.sad.playlistmanager.application.exceptions.ValidationException;
import java.util.List;
import it.unisa.sad.playlistmanager.domain.model.Track;
import it.unisa.sad.playlistmanager.persistence.repository.TrackRepository;
import it.unisa.sad.playlistmanager.application.exceptions.TrackNotFoundException;

public class TagService {
    private final TagRepository tagRepository;
    private final TrackRepository trackRepository;

    public TagService(TagRepository tagRepository, TrackRepository trackRepository) {
        this.tagRepository = tagRepository;
        this.trackRepository = trackRepository;
    }

    public List<Tag> getTagsForTrack(String trackId) {
        validateId(trackId, "L'id della track è nullo o vuoto.");
        getExistingTrack(trackId);
        return tagRepository.findByTrackId(trackId);
    }

    public Tag addTag(String name) {
        validateText(name, "Il nome della tag non può essere nullo o vuoto.");
        if (tagRepository.findByName(name).isPresent()) {
            throw new ValidationException("La tag esiste già.");
        }
        Tag newTag = new Tag(null, name);
        tagRepository.save(newTag);
        return newTag;
    }

    /**
     * Elimina una tag.
     *
     * @param tagId id della tag da eliminare
     * @return tag eliminata
     * @throws ValidationException se l'id è nullo o vuoto
     * @throws TagNotFoundException se la tag non esiste
     */
    public Tag deleteTag(String tagId) {
        validateId(tagId, "L'id della tag da eliminare è nullo o vuoto.");
        getExistingTag(tagId);

        return tagRepository.deleteById(tagId)
                .orElseThrow(() -> new TagNotFoundException("La tag da eliminare non è stata trovata."));
    }

    public void assignTagToTrack(String trackId, String tagId){
        validateId(trackId, "L'id della track è nullo o vuoto.");
        validateId(tagId, "L'id della tag è nullo o vuoto.");
        getExistingTrack(trackId);
        getExistingTag(tagId);
        if (tagRepository.isAttached(trackId, tagId)) {
            throw new ValidationException("La tag è già assegnata alla track.");
        }
        tagRepository.attach(trackId, tagId);
    }

    public void removeTagFromTrack(String trackId, String tagId){
        validateId(trackId, "L'id della track è nullo o vuoto.");
        validateId(tagId, "L'id della tag è nullo o vuoto.");
        getExistingTrack(trackId);
        getExistingTag(tagId);
        if (!tagRepository.isAttached(trackId, tagId)) {
            throw new ValidationException("La tag non è assegnata alla track.");
        }
        tagRepository.detach(trackId, tagId);
    }

    public List<Track> getTracksByTag(String tagId){
        validateId(tagId, "L'id della tag è nullo o vuoto.");
        getExistingTag(tagId);
        return tagRepository.findTracksByTagId(tagId);
    }

    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    
    /**
     * UTILITY: Valida una stringa obbligatoria.
     */

    private void validateId(String id, String errorMessage) {
        if (id == null || id.trim().isEmpty()) {
            throw new ValidationException(errorMessage);
        }
    }

    private void validateText(String value, String errorMessage) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(errorMessage);
        }
    }

    /**
     * UTILITY: Recupera una traccia esistente o lancia un errore applicativo coerente.
     */
    private Track getExistingTrack(String trackId) {
        validateText(trackId, "L'id della Track è nullo o vuoto.");

        return trackRepository.findById(trackId)
                .orElseThrow(() -> new TrackNotFoundException("Traccia non trovata nel catalogo con l'ID specificato."));
    }

    /**
     * UTILITY: Recupera una tag esistente o lancia un errore applicativo coerente.
     */
    private Tag getExistingTag(String tagId) {
        validateText(tagId, "L'id della Tag è nullo o vuoto.");
        return tagRepository.findById(tagId).orElseThrow(() -> new TagNotFoundException("Tag non trovata con l'ID specificato."));
    }

}

