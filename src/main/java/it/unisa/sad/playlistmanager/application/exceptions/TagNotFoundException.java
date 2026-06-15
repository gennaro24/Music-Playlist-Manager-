package it.unisa.sad.playlistmanager.application.exceptions;


public class TagNotFoundException extends RuntimeException {
    public TagNotFoundException(String message){
        super(message);
    }
}
