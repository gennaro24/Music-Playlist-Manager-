package it.unisa.sad.playlistmanager.ui.controller;

import it.unisa.sad.playlistmanager.application.facade.MusicPlaylistManagerFacade;
import it.unisa.sad.playlistmanager.domain.model.Track;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import it.unisa.sad.playlistmanager.domain.model.Playlist;
import javafx.event.ActionEvent;
import java.util.function.Consumer;
import javafx.collections.FXCollections;
import javafx.scene.control.ListCell;

public class PlaylistController {

    private MusicPlaylistManagerFacade facade;
    private Consumer<Playlist> onPlaylistSelectedHandler;
    public Consumer<String> onShowTracksTextChangeHandler;

    @FXML
    private ListView<Playlist> listPlaylists;
    @FXML
    private Button btnCreatePlaylist;
    @FXML
    private Button btnRemovePlaylist;
    @FXML
    private TextField txtPlaylistName;
    @FXML
    private Label lblPlaylistFeedback;

    @FXML
    private void initialize() {
        if (txtPlaylistName != null) {
            txtPlaylistName.setVisible(false);
            txtPlaylistName.setManaged(false);
        }

        if (listPlaylists != null) {
            loadPlaylists();

            // CONFIGURAZIONE DELLA CELL FACTORY CON TOGGLE DI DESELEZIONE
            listPlaylists.setCellFactory(lv -> {
                ListCell<Playlist> cell = new ListCell<>() {
                    @Override
                    protected void updateItem(Playlist item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? null : item.getName());
                    }
                };

                // Intercettiamo il click sulla cella PRIMA che scatti la selezione nativa
                cell.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, event -> {
                    if (!cell.isEmpty() && cell.isSelected()) {
                        // DESELEZIONE FORZATA: L'utente ha cliccato sulla playlist già attiva
                        listPlaylists.getSelectionModel().clearSelection();

                        System.out.println("[UI TOGGLE PLAYLIST] Playlist deselezionata correttamente.");

                        if (lblPlaylistFeedback != null) {
                            lblPlaylistFeedback.setText("");
                        }

                        // Propaghiamo un segnale di 'null' al MainViewController per notificare la
                        // deselezione
                        if (onPlaylistSelectedHandler != null) {
                            onPlaylistSelectedHandler.accept(null);
                        }

                        // Consumiamo l'evento per bloccare il comportamento di selezione standard
                        event.consume();
                    }
                });

                return cell;
            });

            // LISTENER STANDARD DI SELEZIONE (Rimane invariato, gestisce solo le nuove
            // selezioni non nulle)
            listPlaylists.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null && onPlaylistSelectedHandler != null) {
                    if (facade != null) {
                        System.out.println("Recupero tracce della playlist: " + newSelection.getName());
                        java.util.List<Track> tracks = facade.getTracksForPlaylist2(newSelection);

                        if (tracks == null || tracks.isEmpty()) {
                            if (lblPlaylistFeedback != null) {
                                lblPlaylistFeedback.setStyle("-fx-text-fill: #b0413e;");
                                lblPlaylistFeedback.setText("La playlist selezionata non contiene tracce.");
                            }
                        } else {
                            if (lblPlaylistFeedback != null) {
                                lblPlaylistFeedback.setText("");
                                

                            }
                        }
                        onPlaylistSelectedHandler.accept(newSelection);
                    } else {
                        onPlaylistSelectedHandler.accept(newSelection);
                    }
                }
            });

            // Evitiamo doppia propagazione eventi: selectedItemProperty e' l'unica fonte.
        }
    }

    public void setOnShowTracksTextChange(Consumer<String> handler) {
        this.onShowTracksTextChangeHandler = handler;
    }

    public void setOnPlaylistSelected(Consumer<Playlist> handler) {
        this.onPlaylistSelectedHandler = handler;
    }

    public void setFacade(MusicPlaylistManagerFacade facade) {
        this.facade = facade;
        loadPlaylists();
    }

    @FXML

    private void handleCreatePlaylist(ActionEvent event) {
        if (listPlaylists == null || txtPlaylistName == null || lblPlaylistFeedback == null) {
            return;
        }

        // WORKFLOW A COMPARSA: Se il campo di testo è nascosto, lo mostriamo al primo
        // click
        if (!txtPlaylistName.isVisible()) {
            txtPlaylistName.setVisible(true);
            txtPlaylistName.setManaged(true);
            txtPlaylistName.requestFocus(); // Richiede il focus per facilitare l'immissione di testo
            lblPlaylistFeedback.setStyle("-fx-text-fill: #0066cc;");
            lblPlaylistFeedback.setText("Digita il nome e salva la playlist");
            btnCreatePlaylist.setText("Salva la playlist");
            return;
        }
        String rawName = txtPlaylistName.getText();
        String name = rawName != null ? rawName.trim() : "";
        if (name.isEmpty()) {
            lblPlaylistFeedback.setStyle("-fx-text-fill: red;");
            lblPlaylistFeedback.setText("Inserisci un nome playlist.");
            return;
        } else {
            System.out.println("\n[TEST UI] Nome playlist: " + name);
            lblPlaylistFeedback.setStyle("-fx-text-fill: green;");
            lblPlaylistFeedback.setText("Playlist " + name + " creata con successo.");
            btnCreatePlaylist.setText("Nuova Playlist");
            txtPlaylistName.setVisible(false);
            txtPlaylistName.setManaged(false);
            txtPlaylistName.clear();
        }
        if (facade != null) {
            /*
             * boolean duplicate = facade.existsPlaylistWithName2(name);
             * if (duplicate) {
             * lblPlaylistFeedback.setStyle("-fx-text-fill: red;");
             * lblPlaylistFeedback.setText("Esiste già una playlist con questo nome.");
             * return;
             * }
             */
            try {
                // Playlist newPlaylist = facade.addPlaylist2(name);
                // DECOMMENTA la riga precedente e implementa il metodo nella facade per la gestione reale delle playlist!
                // Questo è un esempio di possibile flusso di controllo:
                // Se l'aggiunta della nuova playlist va a buon fine...
                // Sostituisci le seguenti righe con la logica corretta non appena disponibile.
                // Playlist newPlaylist = new Playlist(name); // Simulazione temporanea
                // Simulazione base:
                Playlist newPlaylist = new Playlist(name); // Rimuovi con la chiamata reale a facade.addPlaylist2(name)
                if (newPlaylist != null) {
                    listPlaylists.getItems().add(newPlaylist);
                    listPlaylists.getSelectionModel().select(newPlaylist);
                    txtPlaylistName.clear();
                    lblPlaylistFeedback.setStyle("-fx-text-fill: green;");
                    lblPlaylistFeedback.setText("Playlist creata con successo.");
                }
            } catch (Exception e) {
                lblPlaylistFeedback.setStyle("-fx-text-fill: red;");
                lblPlaylistFeedback.setText("Errore durante la creazione della playlist.");
                e.printStackTrace();
            }
        }

        if (facade != null) {
            // facade.addPlaylist(name);
            // Sincronizzazione atomica dei nodi della ListView
            // listPlaylists.setItems(FXCollections.observableArrayList(facade.getAllPlaylists2()));

            lblPlaylistFeedback.setStyle("-fx-text-fill: green;");
            lblPlaylistFeedback.setText("Playlist '" + name + "' creata con successo.");
        } else {
            lblPlaylistFeedback.setStyle("-fx-text-fill: red;");
            lblPlaylistFeedback.setText("Errore di sistema: Facciata non disponibile.");
        }
    }

    @FXML
    private void handleRemovePlaylist(ActionEvent event) {
        if (listPlaylists == null || lblPlaylistFeedback == null) {
            return;
        }
        Playlist selected = listPlaylists.getSelectionModel().getSelectedItem();
        if (selected != null) {
            listPlaylists.getSelectionModel().clearSelection();
            listPlaylists.getItems().remove(selected);

            // Deseleziona la playlist selezionata e notifica l'handler per nascondere la tabella canzoni
            listPlaylists.getSelectionModel().clearSelection();

            // Segnala la deselezione al MainViewController (o chi ascolta) per far nascondere la tabella dei brani
            if (onPlaylistSelectedHandler != null) {
                onPlaylistSelectedHandler.accept(null);
            }

            if (listPlaylists.getItems().isEmpty()) {
                // Messaggio placeholder quando non ci sono più playlist
                listPlaylists.setPlaceholder(new Label("Nessuna playlist disponibile."));
            }
            lblPlaylistFeedback.setStyle("-fx-text-fill: green;");
            lblPlaylistFeedback.setText("Playlist rimossa.");
        } else {
            lblPlaylistFeedback.setStyle("-fx-text-fill: red;");
            lblPlaylistFeedback.setText("Seleziona una playlist da rimuovere.");
        }
    }

    private void loadPlaylists() {
        // se il facade o la tabella non sono inizializzati, non faccio nulla
        if (facade == null || listPlaylists == null) {
            return;
        }
        // interrogo il facade per ottenere tutte le tracce e le setto nella tabella
        // quanto invoco il setItems, la tabella si aggiorna con i dati della facade
        // i dati vengono inseriti nella colonna corretta tramite PropertyValueFactory
        // definito in initialize
        listPlaylists.setItems(FXCollections.observableArrayList(facade.getAllPlaylists2()));

    }
}
