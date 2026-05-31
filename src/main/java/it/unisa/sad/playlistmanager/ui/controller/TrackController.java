package it.unisa.sad.playlistmanager.ui.controller;

import it.unisa.sad.playlistmanager.application.facade.MusicPlaylistManagerFacade;
import it.unisa.sad.playlistmanager.domain.model.Playlist;
import it.unisa.sad.playlistmanager.domain.model.Track;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.collections.FXCollections;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import java.util.function.Consumer;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell; // Importato per la CellFactory della ComboBox
import javafx.scene.layout.HBox;
import javafx.scene.input.MouseEvent;


public class TrackController {

    private MusicPlaylistManagerFacade facade;
    private Track selectedTrack;
    private Consumer<Track> onTrackSelectedHandler;

    @FXML private TextField txtTitle;
    @FXML private TextField txtAuthor;
    @FXML private TextField txtDuration;
    @FXML private TextField txtGenre;
    @FXML private TextField txtYear;
    @FXML private Label lblFeedback;

    @FXML private TableView<Track> tableTracks;
    @FXML private TableColumn<Track, String> colTitle;
    @FXML private TableColumn<Track, String> colAuthor;
    @FXML private TableColumn<Track, Integer> colDuration;
    @FXML private TableColumn<Track, String> colGenre;
    @FXML private TableColumn<Track, Integer> colYear;
    @FXML private VBox formAddTrack;
    @FXML private ComboBox<Playlist> dropdownPlaylists;
    @FXML private HBox hboxAddtoPlaylist;

    @FXML
    private void initialize() {
        // 1. Inizializza le cell value factory per le colonne della tabella
        if (colTitle != null)  colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        if (colAuthor != null) colAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
        if (colDuration != null) colDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        if (colGenre != null) colGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        if (colYear != null) colYear.setCellValueFactory(new PropertyValueFactory<>("year"));

        // 2. Configura la ComboBox per le playlist in modo null-safe
        if (dropdownPlaylists != null) {
            dropdownPlaylists.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Playlist item, boolean empty) {
                    super.updateItem(item, empty);
                    setText((empty || item == null) ? null : item.getName());
                }
            });
            dropdownPlaylists.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Playlist item, boolean empty) {
                    super.updateItem(item, empty);
                    setText((empty || item == null) ? null : item.getName());
                }
            });
        }

        // 3. Configura la TableView per selezione/deselezione delle righe e sincronizzazione dell'HBox
        if (tableTracks != null) {
            tableTracks.setRowFactory(tv -> {
                final javafx.scene.control.TableRow<Track> row = new javafx.scene.control.TableRow<>();
                row.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                    if (!row.isEmpty() && row.isSelected()) {
                        // Qui siamo PRIMA che JavaFX ricalcoli la selezione.
                        // Se clicco una riga già selezionata, la deseleziono (toggle).
                        tableTracks.getSelectionModel().clearSelection();
                        selectedTrack = null;
                        if (hboxAddtoPlaylist != null) {
                            hboxAddtoPlaylist.setVisible(false);
                            hboxAddtoPlaylist.setManaged(false);
                        }
                        System.out.println("[UI TOGGLE] Track deselezionata.");
                        event.consume();
                    }
                });
                return row;
            });

            tableTracks.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
                if (newSel != null) {
                    selectedTrack = newSel;
                    System.out.println("Track selected: " + selectedTrack.getTitle());

                    // Notifica l'handler (callback) se presente
                    if (onTrackSelectedHandler != null) {
                        onTrackSelectedHandler.accept(newSel);
                    }

                    // Aggiorna la ComboBox con le playlist disponibili
                    if (facade != null && dropdownPlaylists != null) {
                        dropdownPlaylists.setItems(FXCollections.observableArrayList(facade.getAllPlaylists2()));
                        System.out.println("Playlists caricate nel dropdown: " + facade.getAllPlaylists2());
                    }

                    // Mostra l'HBox per aggiungere alla playlist
                    if (hboxAddtoPlaylist != null) {
                        hboxAddtoPlaylist.setVisible(true);
                        hboxAddtoPlaylist.setManaged(true);
                    }
                } else {
                    // Se la selezione viene cancellata, nasconde anche l'HBox
                    selectedTrack = null;
                    if (hboxAddtoPlaylist != null) {
                        hboxAddtoPlaylist.setVisible(false);
                        hboxAddtoPlaylist.setManaged(false);
                    }
                }
            });
        }
    }

    public void setOnTrackSelected(Consumer<Track> handler) {
        this.onTrackSelectedHandler = handler;
    }

    public void setFacade(MusicPlaylistManagerFacade facade) {
        this.facade = facade;
        loadCatalog();
    }

    @FXML
    private void handleTrackAddition(ActionEvent event) {
        lblFeedback.setText(""); 
        if (facade == null) {
            lblFeedback.setText("Errore interno: facade non inizializzata.");
            return;
        }
        try {
            String title = toSentenceCase(txtTitle.getText());
            String author = toSentenceCase(txtAuthor.getText());
            String genre = toSentenceCase(txtGenre.getText());
            int duration = Integer.parseInt(txtDuration.getText().trim());
            int year = Integer.parseInt(txtYear.getText().trim());

            facade.addTrack2(title, author, duration, genre, year);
            clearForm();
            lblFeedback.setStyle("-fx-text-fill: green;");
            lblFeedback.setText("Traccia aggiunta con successo.");
            loadCatalog();

        } catch (NumberFormatException e) {
            lblFeedback.setStyle("-fx-text-fill: red;");
            lblFeedback.setText("Durata e anno devono essere numeri validi.");
        } catch (IllegalArgumentException e) {
            lblFeedback.setStyle("-fx-text-fill: red;");
            lblFeedback.setText(e.getMessage()); 
        } catch (Exception e) {
            lblFeedback.setStyle("-fx-text-fill: red;");
            lblFeedback.setText("Errore durante il salvataggio della traccia.");
            e.printStackTrace(); 
        }
    }

    @FXML
    private void handleSaveAddPlaylist(ActionEvent event) {
        if (selectedTrack != null && dropdownPlaylists != null && dropdownPlaylists.getSelectionModel().getSelectedItem() != null) {
            Playlist targetPlaylist = dropdownPlaylists.getSelectionModel().getSelectedItem();
            
            // Qui invocherai il metodo della Facade per associare la traccia (es. facade.addTrackToPlaylist(selectedTrack, targetPlaylist);)
            
            lblFeedback.setStyle("-fx-text-fill: green;");
            lblFeedback.setText("Traccia '" + selectedTrack.getTitle() + "' aggiunta alla playlist '" + targetPlaylist.getName() + "' con successo.");
            
            // Opzionale: Nascondiamo l'HBox dopo il salvataggio per pulizia di interfaccia
            if (hboxAddtoPlaylist != null) {
                hboxAddtoPlaylist.setVisible(false);
                hboxAddtoPlaylist.setManaged(false);
            }
        } else {
            lblFeedback.setStyle("-fx-text-fill: red;");
            lblFeedback.setText("Seleziona una playlist valida dal menu a tendina.");
        }
    }

    private void loadCatalog() {
        if (facade == null || tableTracks == null) {
            return;
        }
        tableTracks.setItems(FXCollections.observableArrayList(facade.getAllTracks2()));
    }

    public void showCatalogView() {
        if (formAddTrack != null) {
            formAddTrack.setVisible(true);
            formAddTrack.setManaged(true);
        }
        loadCatalog();
        if (lblFeedback != null) {
            lblFeedback.setStyle("-fx-text-fill: #1f7a1f;");
            lblFeedback.setText("Visualizzazione catalogo completo.");
        }
    }

    public void displayPlaylistTracks(Playlist playlist) {
        if (tableTracks == null || playlist == null) {
            return;
        }
        if (formAddTrack != null) {
            formAddTrack.setVisible(false);
            formAddTrack.setManaged(false);
        }
        // Quando visualizziamo una playlist specifica, nascondiamo la barra di aggiunta rapida
        if (hboxAddtoPlaylist != null) {
            hboxAddtoPlaylist.setVisible(false);
            hboxAddtoPlaylist.setManaged(false);
        }
        tableTracks.setItems(FXCollections.observableArrayList(playlist.getTracks()));
        if (lblFeedback != null) {
            lblFeedback.setStyle("-fx-text-fill: #0066cc;");
            lblFeedback.setText("Contenuto playlist: " + playlist.getName());
        }
    }

    public void clearForm() {
        txtTitle.clear();
        txtAuthor.clear();
        txtDuration.clear();
        txtGenre.clear();
        txtYear.clear();
    }

    private String toSentenceCase(String value) {
        if (value == null) return "";
        String trimmed = value.trim().replaceAll("\\s+", " ");
        if (trimmed.isEmpty()) return "";
        return trimmed.substring(0, 1).toUpperCase() + trimmed.substring(1).toLowerCase();
    }
}