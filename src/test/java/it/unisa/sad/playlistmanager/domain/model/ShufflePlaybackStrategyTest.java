package it.unisa.sad.playlistmanager.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class ShufflePlaybackStrategyTest {

    private List<Track> standardQueue;

    @BeforeEach
    void setUp() {
        standardQueue = new ArrayList<>();
        standardQueue.add(new Track("t1", "Song One", "Artist One", 180, "Rock", 2020));
        standardQueue.add(new Track("t2", "Song Two", "Artist Two", 200, "Pop", 2021));
        standardQueue.add(new Track("t3", "Song Three", "Artist Three", 220, "Jazz", 2022));
    }

    /**
     * T-146: Verifica che lo shuffle produca un ordine valido contenente tutte le tracce una sola volta.
     * Sfrutta il seed deterministico (T-141) per rendere il test ripetibile.
     */
    @Test
    void testT146_ShuffleProduceOrdineValidoEContieneTutteLeTracceUnaVolta() {
        // Usiamo un seed fisso (es. 42) così l'ordine rimescolato sarà SEMPRE lo stesso ad ogni run del test
        Random seededRandom = new Random(42);
        ShufflePlaybackStrategy strategy = new ShufflePlaybackStrategy(standardQueue, seededRandom);

        List<Track> shuffled = strategy.getShuffledQueue();

        // 1. La dimensione deve essere identica
        assertEquals(standardQueue.size(), shuffled.size());

        // 2. Devono esserci tutti gli elementi originali (nessun duplicato inserito per errore)
        assertTrue(shuffled.containsAll(standardQueue));

        // 3. Verifichiamo l'avanzamento sequenziale della coda mescolata
        assertEquals(shuffled.get(0), strategy.getNextTrack(null));
        assertEquals(shuffled.get(1), strategy.getNextTrack(null));
        assertEquals(shuffled.get(2), strategy.getNextTrack(null));
        assertNull(strategy.getNextTrack(null), "La coda dovrebbe essere terminata");
    }

    /**
     * T-142 e T-147: Gestisce una playlist con una sola traccia senza errori e verifica che resti stabile.
     */
    @Test
    void testT142_T147_ShuffleMonotracciaRestaStabileSenzaErrori() {
        List<Track> singleTrackQueue = new ArrayList<>();
        Track loneTrack = new Track("t1", "Lone Song", "Artist", 180, "Rock", 2020);
        singleTrackQueue.add(loneTrack);

        // Inizializzazione della strategia con una sola traccia
        ShufflePlaybackStrategy strategy = assertDoesNotThrow(() -> new ShufflePlaybackStrategy(singleTrackQueue));

        List<Track> shuffled = strategy.getShuffledQueue();

        // Verifica stabilità: la coda deve contenere solo quella traccia
        assertEquals(1, shuffled.size());
        assertEquals(loneTrack, shuffled.get(0));

        // Verifica estrazione
        assertEquals(loneTrack, strategy.getNextTrack(null));
        assertNull(strategy.getNextTrack(null), "Dopo la prima traccia, una monotraccia deve restituire null");
    }
}