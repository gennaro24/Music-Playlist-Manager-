package it.unisa.sad.playlistmanager.domain.model;

import org.junit.jupiter.api.Test;
import java.time.Year;
import static org.junit.jupiter.api.Assertions.*;

class TrackTest {

    @Test
    void testCreazioneTracciaValida() {
        String id = "123-abc";
        String title = "Bohemian Rhapsody";
        String author = "Queen";
        int duration = 354;
        String genre = "Rock";
        int year = 1975;

        Track track = new Track(id, title, author, duration, genre, year);

        assertEquals(id, track.getId());
        assertEquals(title, track.getTitle());
        assertEquals(author, track.getAuthor());
        assertEquals(duration, track.getDuration());
        assertEquals(genre, track.getGenre());
        assertEquals(year, track.getYear());
    }

    @Test
    void testCreazioneTracciaValidaSenzaIdGeneraUUID() {
        Track track = new Track(null, "Imagine", "John Lennon", 183, "Pop", 1971);

        assertNotNull(track.getId());
        assertFalse(track.getId().isEmpty());
        assertEquals("Imagine", track.getTitle());
    }

    @Test
    void testErroreTitoloMancante() {
        assertThrows(IllegalArgumentException.class, () -> new Track("1", null, "Autore", 200, "Pop", 2020));
        assertThrows(IllegalArgumentException.class, () -> new Track("1", "   ", "Autore", 200, "Pop", 2020));
    }

    @Test
    void testErroreAutoreMancante() {
        assertThrows(IllegalArgumentException.class, () -> new Track("1", "Titolo", null, 200, "Pop", 2020));
        assertThrows(IllegalArgumentException.class, () -> new Track("1", "Titolo", "   ", 200, "Pop", 2020));
    }

    @Test
    void testErroreDurataZeroONegativa() {
        assertThrows(IllegalArgumentException.class, () -> new Track("1", "Titolo", "Autore", 0, "Pop", 2020));
        assertThrows(IllegalArgumentException.class, () -> new Track("1", "Titolo", "Autore", -10, "Pop", 2020));
    }

    @Test
    void testErroreAnnoNelFuturo() {
        int nextYear = Year.now().getValue() + 1;
        assertThrows(IllegalArgumentException.class, () -> new Track("1", "Titolo", "Autore", 200, "Pop", nextYear));
    }

    @Test
    void testErroreAnnoZeroONegativo() {
        assertThrows(IllegalArgumentException.class, () -> new Track("1", "Titolo", "Autore", 200, "Pop", 0));
        assertThrows(IllegalArgumentException.class, () -> new Track("1", "Titolo", "Autore", 200, "Pop", -1990));
    }
}