package it.unisa.sad.playlistmanager.persistence.exceptions;

/**
 * Eccezione unchecked usata per segnalare errori nello strato di persistenza.
 */
public class RepositoryException extends RuntimeException {

    public RepositoryException(String message) {
        super(message);
    }

    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}