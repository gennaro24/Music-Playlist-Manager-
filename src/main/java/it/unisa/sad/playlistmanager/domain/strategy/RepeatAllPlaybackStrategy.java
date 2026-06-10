package it.unisa.sad.playlistmanager.domain.strategy;

import it.unisa.sad.playlistmanager.domain.model.PlaybackQueue;
import it.unisa.sad.playlistmanager.domain.model.Track;

/**
 * Strategia di riproduzione REPEAT_ALL.
 * Avanza alla traccia successiva e, raggiunta la fine della coda,
 * riparte dalla prima traccia.
 */
public class RepeatAllPlaybackStrategy implements PlaybackStrategy {
    @Override
    /**T-149
     * Calcola la traccia successiva in base alla coda corrente.
     * @param queue La coda di riproduzione attuale.
     * @return La prossima traccia da riprodurre, oppure null se la coda è terminata.
     */
    public Track getNextTrack(PlaybackQueue queue) {
        // Se non c'è una coda o la coda è vuota, non c'è una traccia successiva
        if (queue == null || queue.isEmpty()) {
            return null;
        }
        // Calcola l'indice successivo
        int nextIndex = queue.getCurrentIndex() + 1;
        // Se abbiamo superato l'ultima canzone della lista impostiamo l'indice a 0
        if (nextIndex >= queue.getTracks().size()) {
            //la coda riparte dalla prima traccia
            nextIndex = 0;
        }
        // Ritorna la traccia all'indice successivo
        return queue.getTracks().get(nextIndex);
    }
}
