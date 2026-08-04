package it.unisa.sad.playlistmanager.application.command;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test della cronologia globale dei command ({@link UndoManager}).
 *
 * Verifica i due contratti chiave: l'esecuzione registra il command solo se
 * l'execute riesce, e l'annullamento segue l'ordine LIFO rimuovendo il command
 * solo dopo un undo completato con successo. Usa un command di prova che
 * registra le proprie chiamate, così da osservare ordine ed effetti senza
 * dipendere dai service applicativi.
 */
class UndoManagerTest {

    /** Command di prova che registra le chiamate execute/undo in un log condiviso. */
    private static class RecordingCommand implements Command {
        private final String name;
        private final List<String> log;
        private final boolean failOnExecute;
        private final boolean failOnUndo;

        RecordingCommand(String name, List<String> log) {
            this(name, log, false, false);
        }

        RecordingCommand(String name, List<String> log, boolean failOnExecute, boolean failOnUndo) {
            this.name = name;
            this.log = log;
            this.failOnExecute = failOnExecute;
            this.failOnUndo = failOnUndo;
        }

        @Override
        public void execute() {
            if (failOnExecute) {
                throw new IllegalStateException("execute fallita");
            }
            log.add("execute:" + name);
        }

        @Override
        public void undo() {
            if (failOnUndo) {
                throw new IllegalStateException("undo fallita");
            }
            log.add("undo:" + name);
        }
    }

    @Test
    void canUndoFalseSuCronologiaVuota() {
        UndoManager undoManager = new UndoManager();
        assertFalse(undoManager.canUndo());
    }

    @Test
    void executeAndPushRifiutaCommandNull() {
        UndoManager undoManager = new UndoManager();
        assertThrows(IllegalArgumentException.class, () -> undoManager.executeAndPush(null));
    }

    @Test
    void executeAndPushEsegueERegistraIlCommand() {
        UndoManager undoManager = new UndoManager();
        List<String> log = new ArrayList<>();

        undoManager.executeAndPush(new RecordingCommand("A", log));

        assertEquals(List.of("execute:A"), log);
        assertTrue(undoManager.canUndo());
    }

    @Test
    void undoLastAnnullaInOrdineLifo() {
        UndoManager undoManager = new UndoManager();
        List<String> log = new ArrayList<>();
        undoManager.executeAndPush(new RecordingCommand("1", log));
        undoManager.executeAndPush(new RecordingCommand("2", log));
        undoManager.executeAndPush(new RecordingCommand("3", log));

        undoManager.undoLast();
        undoManager.undoLast();
        undoManager.undoLast();

        assertEquals(
                List.of("execute:1", "execute:2", "execute:3", "undo:3", "undo:2", "undo:1"),
                log);
        assertFalse(undoManager.canUndo());
    }

    @Test
    void undoLastSuCronologiaVuotaNonFaNulla() {
        UndoManager undoManager = new UndoManager();
        assertDoesNotThrow(undoManager::undoLast);
        assertFalse(undoManager.canUndo());
    }

    @Test
    void executeFallitaNonRegistraIlCommand() {
        UndoManager undoManager = new UndoManager();
        List<String> log = new ArrayList<>();

        assertThrows(IllegalStateException.class,
                () -> undoManager.executeAndPush(new RecordingCommand("F", log, true, false)));

        assertFalse(undoManager.canUndo());
        assertTrue(log.isEmpty());
    }

    @Test
    void undoFallitaNonRimuoveIlCommandDallaCronologia() {
        UndoManager undoManager = new UndoManager();
        List<String> log = new ArrayList<>();
        undoManager.executeAndPush(new RecordingCommand("G", log, false, true));

        assertThrows(IllegalStateException.class, undoManager::undoLast);

        // Il command resta annullabile: la pop avviene solo dopo un undo riuscito.
        assertTrue(undoManager.canUndo());
    }
}