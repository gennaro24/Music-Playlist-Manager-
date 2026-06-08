package it.unisa.sad.playlistmanager.domain.strategy;

import it.unisa.sad.playlistmanager.domain.model.PlaybackQueue;
import it.unisa.sad.playlistmanager.domain.model.Track;

/**
 * Interfaccia per il pattern Strategy.
 * Definisce l'algoritmo per calcolare la traccia successiva da riprodurre.
 */
public interface PlaybackStrategy {
    
    /**
     * Calcola la traccia successiva in base alla coda corrente.
     * * @param queue La coda di riproduzione attuale.
     * @return La prossima traccia da riprodurre, oppure null se la coda è terminata.
     */
    Track getNextTrack(PlaybackQueue queue);
}