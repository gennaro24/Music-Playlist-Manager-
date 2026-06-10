package it.unisa.sad.playlistmanager.domain.strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import it.unisa.sad.playlistmanager.domain.model.PlaybackQueue;
import it.unisa.sad.playlistmanager.domain.model.Track;

/**
 * Gestisce la riproduzione in modalità Shuffle.
 */
public class ShufflePlaybackStrategy implements PlaybackStrategy {

    private final List<Track> shuffledQueue;
    private int currentIndex = 0;

    /**
     * T-141: Costruttore principale con dipendenza Random iniettabile.
     */
    public ShufflePlaybackStrategy(List<Track> originalQueue, Random random) {
        this.shuffledQueue = new ArrayList<>(originalQueue);
        if (random != null) {
            Collections.shuffle(this.shuffledQueue, random);
        } else {
            Collections.shuffle(this.shuffledQueue);
        }
    }

    /**
     * Costruttore di default.
     */
    public ShufflePlaybackStrategy(List<Track> originalQueue) {
        this(originalQueue, new Random());
    }

    public List<Track> getShuffledQueue() {
        return Collections.unmodifiableList(shuffledQueue);
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    /**
     * Calcola e restituisce la traccia successiva nella coda mescolata.
     * Implementa il contratto definito da PlaybackStrategy.
     *
     * @param queue La coda di riproduzione attuale (ignorata, usiamo quella mescolata interna).
     * @return La prossima Track, oppure null se la coda è terminata.
     */
    @Override
    public Track getNextTrack(PlaybackQueue queue) {
        if (currentIndex < shuffledQueue.size()) {
            return shuffledQueue.get(currentIndex++);
        }
        return null;
    }
}