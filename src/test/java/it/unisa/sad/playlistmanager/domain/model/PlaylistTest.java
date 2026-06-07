package it.unisa.sad.playlistmanager.domain.model;

import it.unisa.sad.playlistmanager.domain.exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlaylistTest {

    @Test
    void testCreazionePlaylistValida() {
        Playlist playlist = new Playlist("p-1", "Rock Classics");
        
        assertEquals("p-1", playlist.getId());
        assertEquals("Rock Classics", playlist.getName());
        assertTrue(playlist.getTracks().isEmpty(), "La playlist appena creata dovrebbe essere vuota");
    }

    @Test
    void testErroreNomeMancanteCostruttore() {
        assertThrows(ValidationException.class, () -> new Playlist("p-1", null));
        assertThrows(ValidationException.class, () -> new Playlist("p-1", "   "));
    }

    @Test
    void testModificaNomeValida() {
        Playlist playlist = new Playlist("p-1", "Vecchi Successi");
        playlist.setName("Nuovi Successi");
        assertEquals("Nuovi Successi", playlist.getName());
    }

    @Test
    void testErroreNomeMancanteSetter() {
        Playlist playlist = new Playlist("p-1", "Playlist Valida");
        assertThrows(ValidationException.class, () -> playlist.setName(null));
    }

    // --- TEST AGGIUNTA E ORDINE TRACCE ---

    @Test
    void testAggiuntaTracciaValida() {
        Playlist playlist = new Playlist("p-1", "Rock Classics");
        Track track = new Track("t-1", "Bohemian Rhapsody", "Queen", 354, "Rock", 1975);
        
        playlist.addTrack(track);
        
        assertEquals(1, playlist.getTracks().size());
        assertEquals(track, playlist.getTracks().get(0));
    }

    @Test
    void testErroreAggiuntaTracciaNulla() {
        Playlist playlist = new Playlist("p-1", "Rock Classics");
        assertThrows(ValidationException.class, () -> playlist.addTrack(null));
    }

    @Test
    void testOrdineTracceMantenuto() {
        Playlist playlist = new Playlist("p-1", "My Mix");
        
        // Creiamo tre tracce distinte
        Track track1 = new Track("t-1", "Song A", "Author 1", 100, "Pop", 2020);
        Track track2 = new Track("t-2", "Song B", "Author 2", 200, "Rock", 2021);
        Track track3 = new Track("t-3", "Song C", "Author 3", 150, "Jazz", 2022);

        // Le aggiungiamo in un ordine specifico (1 -> 2 -> 3)
        playlist.addTrack(track1);
        playlist.addTrack(track2);
        playlist.addTrack(track3);

        // Estraiamo la lista
        List<Track> tracks = playlist.getTracks();

        // Verifichiamo che la dimensione e l'ordine siano stati preservati esattamente
        assertEquals(3, tracks.size(), "La playlist dovrebbe contenere 3 tracce");
        assertEquals(track1, tracks.get(0), "La prima traccia dovrebbe essere Song A");
        assertEquals(track2, tracks.get(1), "La seconda traccia dovrebbe essere Song B");
        assertEquals(track3, tracks.get(2), "La terza traccia dovrebbe essere Song C");
    }

    // --- TEST INCAPSULAMENTO ---

    @Test
    void testListaTracceImmodificabile() {
        Playlist playlist = new Playlist("p-1", "Rock Classics");
        Track track = new Track("t-1", "Bohemian Rhapsody", "Queen", 354, "Rock", 1975);
        
        assertThrows(UnsupportedOperationException.class, () -> playlist.getTracks().add(track));
    }
}