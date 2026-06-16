package it.unisa.sad.playlistmanager.domain.model;

import it.unisa.sad.playlistmanager.domain.exceptions.ValidationException;

import java.util.UUID;

/**
 * 
 * Rappresenta un'entità Tag visuale assegnabile alle tracce.
 * Questa classe è immutabile per garantire la consistenza dei dati nel dominio.
 * Sotto-modulo: Domain Module (High Cohesion).
 * * @author Fraws
 * * @version 1.0
 */
public class Tag {
    /** Identificativo unico del tag (UUID). */
    private String id;
    
    /** Nome del tag. Obbligatorio. */
    private String name;    


    public Tag(String id, String name) {
        validateName(name);
        this.id = (id == null || id.trim().isEmpty()) ? UUID.randomUUID().toString() : id;
        this.name = name.trim();
    }

    // --- METODI PRIVATI DI VALIDAZIONE ---
    /**
     * Valida che il nome del tag non sia nullo o composto solo da spazi vuoti.
     */
    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Errore di validazione: Il nome del tag è obbligatorio.");
        }
    }

    // --- GETTERS ---
    /** @return L'identificativo unico del tag. */
    public String getId() { return id; }
    /** @return Il nome del tag. */
    public String getName() { return name; }

    // --- SETTERS ---
    /**
     * Imposta il nome del tag.
     * @param name Il nuovo nome del tag.
     */
    public void setName(String name) {
        validateName(name);
        this.name = name.trim();
    }

    @Override
    public String toString() {
        return "Tag{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        return id != null ? id.equals(tag.id) : tag.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : name.hashCode();
    }
}

