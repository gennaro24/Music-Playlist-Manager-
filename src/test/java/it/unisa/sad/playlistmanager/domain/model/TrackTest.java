package it.unisa.sad.playlistmanager.domain.model;

import org.junit.jupiter.api.Test;
import java.time.Year;
import static org.junit.jupiter.api.Assertions.*;

class TrackTest {

    // --- TEST TRACCIA VALIDA ---

    @Test
    void testCreazioneTracciaValida() {
        // Dati validi
        String id = "123-abc";
        String title = "Bohemian Rhapsody";
        String author = "Queen";
        int duration = 354;
        String genre = "Rock";
        int year = 1975;

        Track track = new Track(id, title, author, duration, genre, year);

        // Verifico che i campi siano stati assegnati correttamente
        assertEquals(id, track.getId());
        assertEquals(title, track.getTitle());
        assertEquals(author, track.getAuthor());
        assertEquals(duration, track.getDuration());
        assertEquals(genre, track.getGenre());
        assertEquals(year, track.getYear());
    }

    @Test
    void testCreazioneTracciaValidaSenzaIdGeneraUUID() {
        // Passo null all'ID per testare la generazione automatica dell'UUID
        Track track = new Track(null, "Imagine", "John Lennon", 183, "Pop", 1971);

        assertNotNull(track.getId());
        assertFalse(track.getId().isEmpty());
        assertEquals("Imagine", track.getTitle());
    }

    // --- TEST CAMPI MANCANTI ---

    @Test
    void testErroreTitoloMancante() {
        // Titolo null
        assertThrows(IllegalArgumentException.class, () -> {
            new Track("1", null, "Autore", 200, "Pop", 2020);
        });

        // Titolo vuoto
        assertThrows(IllegalArgumentException.class, () -> {
            new Track("1", "   ", "Autore", 200, "Pop", 2020);
        });
    }

    @Test
    void testErroreAutoreMancante() {
        // Autore null
        assertThrows(IllegalArgumentException.class, () -> {
            new Track("1", "Titolo", null, 200, "Pop", 2020);
        });

        // Autore vuoto
        assertThrows(IllegalArgumentException.class, () -> {
            new Track("1", "Titolo", "   ", 200, "Pop", 2020);
        });
    }

    // --- TEST DURATA NON VALIDA ---

    @Test
    void testErroreDurataZeroONegativa() {
        // Durata = 0
        assertThrows(IllegalArgumentException.class, () -> {
            new Track("1", "Titolo", "Autore", 0, "Pop", 2020);
        });

        // Durata negativa
        assertThrows(IllegalArgumentException.class, () -> {
            new Track("1", "Titolo", "Autore", -10, "Pop", 2020);
        });
    }

    // --- TEST ANNO NON VALIDO ---

    @Test
    void testErroreAnnoNelFuturo() {
        int nextYear = Year.now().getValue() + 1;
        
        assertThrows(IllegalArgumentException.class, () -> {
            new Track("1", "Titolo", "Autore", 200, "Pop", nextYear);
        });
    }

    @Test
    void testErroreAnnoZeroONegativo() {
        // Anno = 0
        assertThrows(IllegalArgumentException.class, () -> {
            new Track("1", "Titolo", "Autore", 200, "Pop", 0);
        });

        // Anno negativo
        assertThrows(IllegalArgumentException.class, () -> {
            new Track("1", "Titolo", "Autore", 200, "Pop", -1990);
        });
    }
}