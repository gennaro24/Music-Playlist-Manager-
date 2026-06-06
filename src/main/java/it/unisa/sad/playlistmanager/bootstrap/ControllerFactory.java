package it.unisa.sad.playlistmanager.bootstrap;

import it.unisa.sad.playlistmanager.application.facade.MusicPlaylistManagerFacade;
import javafx.util.Callback;
import java.lang.reflect.Constructor;

public class ControllerFactory implements Callback<Class<?>, Object>{
    
    //facade che verrà iniettato nel controller
    private MusicPlaylistManagerFacade facade;

    //costruttore che inietta la facade
    public ControllerFactory(MusicPlaylistManagerFacade facade) {
        this.facade = facade;
    }

    /**
     * Metodo di callback invocato da FXMLLoader per istanziare i controller. Verra richiamato automaticamente
     * da JavaFX per ogni Controller richiesto nell'FXML attraverso il metodo load() nel Main
     * @param param controllerClass, controller Class che verrà ri
     * @return istnazia del controller correttamente instanziata
     */
    @Override
    public Object call(Class<?> controllerClass) {
        try {
            //per ogni costruttore del controller, verifca se ne esiste uno che accetta un solo parametro e di tipo MusicPlaylistManagerFacade
            for (Constructor<?> constructor : controllerClass.getConstructors()) {
                if (constructor.getParameterCount() == 1 && 
                    constructor.getParameterTypes()[0] == MusicPlaylistManagerFacade.class) {
                    
                    //se viene trovato il costruttore, lo si usa per istanziare il controller inserendo il facade
                    return constructor.newInstance(this.facade);
                }
            }

            //se non viene trovato il costruttore, si usa il costruttore vuoto per istanziare il controller
            //non è necessario iniettare il facade, perché il controller non ha logica e non ha bisogno di accedere al facade
            return controllerClass.getDeclaredConstructor().newInstance();

        } catch (Exception e) {
            //TODO: gestire l'eccezione
            throw new RuntimeException("Errore nell'istanziazione del controller: " + controllerClass.getName(), e);
        }
    }
    
}

    
