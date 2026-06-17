package it.unisa.sad.playlistmanager.domain.model;

import java.time.Year;
import it.unisa.sad.playlistmanager.domain.exceptions.ValidationException;
    /**
     * Rappresenta i criteri scelti dall'utente per creare o visualizzare
     * l'anteprima di una playlist automatica.
     * Il suo unico compito e' conservare in modo valido
     * e immutabile i criteri selezionati: genere, anno e tag.
     *
     * I service useranno questa classe per sapere quali condizioni
     * applicare al catalogo. Se sono presenti piu criteri, devono essere
     * interpretati come condizioni combinate: una traccia deve soddisfarli tutti.
     * @author Foschillo G. 
     * @version 1.0
 */
    public class AutoPlaylistCriteria {

    private final String genre;
    private final Integer year;
    private final String tagId;

    public AutoPlaylistCriteria(String genre, Integer year, String tagId) {
        this.genre = normalizeText(genre);
        this.year = year;
        this.tagId = normalizeText(tagId);

        validateAtLeastOneCriteria();
        validateYear();
    }

    public static AutoPlaylistCriteria byGenre(String genre) {
        return new AutoPlaylistCriteria(genre, null, null);
    }

    public static AutoPlaylistCriteria byYear(Integer year) {
        return new AutoPlaylistCriteria(null, year, null);
    }

    public static AutoPlaylistCriteria byTag(String tagId) {
        return new AutoPlaylistCriteria(null, null, tagId);
    }

    public static AutoPlaylistCriteria combined(String genre, Integer year, String tagId) {
        return new AutoPlaylistCriteria(genre, year, tagId);
    }

    public String getGenre() {
        return genre;
    }

    public Integer getYear() {
        return year;
    }

    public String getTagId() {
        return tagId;
    }

    public boolean hasGenreCriteria() {
        return genre != null;
    }

    public boolean hasYearCriteria() {
        return year != null;
    }

    public boolean hasTagCriteria() {
        return tagId != null;
    }

    private String normalizeText(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }

    private void validateAtLeastOneCriteria() {
        if (!hasGenreCriteria() && !hasYearCriteria() && !hasTagCriteria()) {
            throw new ValidationException(
                    "Almeno un criterio deve essere specificato per creare una playlist automatica."
            );
        }
    }

    private void validateYear() {
        if (year == null) {
            return;
        }

        int currentYear = Year.now().getValue();
        if (year <= 0 || year > currentYear) {
            throw new ValidationException("Anno non valido per la playlist automatica: " + year + ".");
        }
    }
    }

