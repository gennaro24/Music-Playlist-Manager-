package it.unisa.sad.playlistmanager.domain.model;

import java.time.Year;
import java.util.UUID;

/**
 * Rappresenta un'entità Traccia (canzone) all'interno del catalogo musicale.
 * Questa classe è immutabile per garantire la consistenza dei dati nel dominio.
 * Sotto-modulo: Domain Module (High Cohesion).
 * * @author Domenico Di Marino
 * @version 1.0
 */
public class Track {
    
    /** Identificativo unico della traccia (UUID). */
    private final String id;
    
    /** Titolo della traccia. Obbligatorio. */
    private final String title;
    
    /** Autore o artista della traccia. Obbligatorio. */
    private final String author;
    
    /** Durata complessiva del brano espressa in secondi. Deve essere maggiore di zero. */
    private final int duration;
    
    /** Genere musicale del brano (es. Rock, Pop, Jazz). */
    private final String genre;
    
    /** Anno di pubblicazione del brano. Non può essere nel futuro. */
    private final int year;

    /**
     * Costruttore completo della classe Track.
     * Incorpora la logica di business e validazione.
     *
     * @param id       L'ID univoco della traccia. Se nullo o vuoto, viene generato automaticamente un UUID.
     * @param title    Il titolo della traccia (non può essere nullo o vuoto).
     * @param author   L'autore/artista della traccia (non può essere nullo o vuoto).
     * @param duration La durata in secondi (deve essere maggiore di 0).
     * @param genre    Il genere musicale (se nullo viene convertito in stringa vuota).
     * @param year     L'anno di pubblicazione (maggiore di zero e non superiore all'anno corrente).
     * @throws IllegalArgumentException Se uno dei criteri di validazione fallisce.
     */
    public Track(String id, String title, String author, int duration, String genre, int year) {
        validateTitle(title);
        validateAuthor(author);
        validateDuration(duration);
        validateYear(year);

        this.id = (id == null || id.trim().isEmpty()) ? UUID.randomUUID().toString() : id;
        this.title = title.trim();
        this.author = author.trim();
        this.duration = duration;
        this.genre = (genre != null) ? genre.trim() : "";
        this.year = year;
    }

    // --- METODI PRIVATI DI VALIDAZIONE ---

    /**
     * Valida che il titolo non sia nullo o composto solo da spazi vuoti.
     */
    private void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Errore di validazione: Il titolo della traccia è obbligatorio.");
        }
    }

    /**
     * Valida che l'autore non sia nullo o composto solo da spazi vuoti.
     */
    private void validateAuthor(String author) {
        if (author == null || author.trim().isEmpty()) {
            throw new IllegalArgumentException("Errore di validazione: L'autore della traccia è obbligatorio.");
        }
    }

    /**
     * Valida che la durata sia strettamente positiva.
     */
    private void validateDuration(int duration) {
        if (duration <= 0) {
            throw new IllegalArgumentException("Errore di validazione: La durata deve essere maggiore di zero.");
        }
    }

    /**
     * Valida che l'anno sia coerente (maggiore di zero e non nel futuro).
     */
    private void validateYear(int year) {
        int currentYear = Year.now().getValue();
        if (year <= 0 || year > currentYear) {
            throw new IllegalArgumentException("Errore di validazione: Anno di pubblicazione non valido (" + year + ").");
        }
    }

    // --- GETTERS ---

    /** @return L'identificativo unico della traccia. */
    public String getId() { return id; }

    /** @return Il titolo della traccia. */
    public String getTitle() { return title; }

    /** @return L'autore o artista della traccia. */
    public String getAuthor() { return author; }

    /** @return La durata della traccia in secondi. */
    public int getDuration() { return duration; }

    /** @return Il genere musicale della traccia. */
    public String getGenre() { return genre; }

    /** @return L'anno di pubblicazione. */
    public int getYear() { return year; }
    
    @Override
    public String toString() {
        return "Track{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", duration=" + duration +
                "s, genre='" + genre + '\'' +
                ", year=" + year +
                '}';
    }
}