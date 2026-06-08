package it.unisa.sad.playlistmanager.application.exceptions;

public class ValidationException extends RuntimeException {
    public ValidationException(String message){
        super(message);
    }
}