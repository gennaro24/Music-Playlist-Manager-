package it.unisa.sad.playlistmanager.application.command;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Gestisce la cronologia globale dei command eseguiti durante la sessione.
 *
 * Viene chiamato dalla facade applicativa per eseguire e registrare i command
 * creati da {@link CommandFactory}. La cronologia e' LIFO: l'ultima operazione
 * completata e' la prima a essere annullata.
 *
 * @author Foschillo G.
 * @version 1.0
 */
public class UndoManager {
    private final Deque<Command> history = new ArrayDeque<>();

    /**
     * Esegue il command e lo registra soltanto se l'esecuzione termina con
     * successo.
     *
     * @param command command da eseguire
     */
    public void executeAndPush(Command command) {
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }
        command.execute();
        history.push(command);
    }
    /**
     * Annulla l'ultimo command. Il command viene rimosso dalla cronologia solo
     * dopo un undo completato con successo.
     */
    public void undoLast() {
        if (!canUndo()) {
            return;
        }
        Command command = history.peek();
        command.undo();
        history.pop();
    }

    /**
     * @return true se esiste almeno un command annullabile
     */
    public boolean canUndo() {
        return !history.isEmpty();
    }
}
