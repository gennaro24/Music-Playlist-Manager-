package it.unisa.sad.playlistmanager.application.command;

/**
 * Contratto comune delle operazioni applicative annullabili.
 *
 * I command concreti sono creati da {@link CommandFactory} e invocati da
 * {@link UndoManager}. Ogni implementazione esegue un caso d'uso tramite i
 * service applicativi e conserva lo stato minimo necessario per ripristinarlo.
 *
 * @author Foschillo G.
 * @version 1.0
 */
public interface Command {

    /**
     * Esegue l'operazione rappresentata dal command.
     */
    void execute();

    /**
     * Ripristina lo stato precedente all'esecuzione del command.
     */
    void undo();
}
