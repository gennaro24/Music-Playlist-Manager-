package it.unisa.sad.playlistmanager.domain.strategy;

import it.unisa.sad.playlistmanager.domain.model.PlaybackQueue;
import it.unisa.sad.playlistmanager.domain.model.Track;

/**
 * Implementazione della strategia di riproduzione sequenziale (normale).
 * Passa semplicemente alla traccia successiva nell'elenco.
 */
public class SequentialPlaybackStrategy implements PlaybackStrategy {

    @Override
    public Track getNextTrack(PlaybackQueue queue) {
        // Se non c'è una coda o la coda è vuota, non c'è una traccia successiva
        if (queue == null || queue.isEmpty()) {
            return null;
        }
        
        // Calcola l'indice successivo
        int nextIndex = queue.getCurrentIndex() + 1;
        
        // Se abbiamo superato l'ultima canzone della lista, la riproduzione si ferma
        if (nextIndex >= queue.getTracks().size()) {
            return null; 
        }
        
        // Ritorna la traccia all'indice successivo
        return queue.getTracks().get(nextIndex);
    }
}