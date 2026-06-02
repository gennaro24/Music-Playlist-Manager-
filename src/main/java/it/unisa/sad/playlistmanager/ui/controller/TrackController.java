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
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import java.util.function.Consumer;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell; // Importato per la CellFactory della ComboBox
import javafx.scene.layout.HBox;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class TrackController {

    private MusicPlaylistManagerFacade facade;
    private Track selectedTrack;
    private Consumer<Track> onTrackSelectedHandler;

    @FXML
    private TextField txtTitle;
    @FXML
    private TextField txtAuthor;
    @FXML
    private TextField txtDuration;
    @FXML
    private TextField txtGenre;
    @FXML
    private TextField txtYear;
    @FXML
    private Label lblFeedback;

    @FXML
    private TableView<Track> tableTracks;
    @FXML
    private TableColumn<Track, String> colTitle;
    @FXML
    private TableColumn<Track, String> colAuthor;
    @FXML
    private TableColumn<Track, Integer> colDuration;
    @FXML
    private TableColumn<Track, String> colGenre;
    @FXML
    private TableColumn<Track, Integer> colYear;
    @FXML
    private VBox formAddTrack;
    @FXML
    private ComboBox<Playlist> dropdownPlaylists;
    @FXML
    private HBox hboxAddtoPlaylist;
    @FXML
    private Button btnRemoveFromPlaylist;
    private boolean playlistViewMode = false;
    private Playlist currentPlaylist;
    private java.util.List<Track> currentPlaylistTracks = new java.util.ArrayList<>();


    public void setFacade(MusicPlaylistManagerFacade facade) {
        this.facade = facade;
        loadCatalog();
    }


    @FXML
    private void initialize() {
        // 1. INIZIALIZZAZIONE VALUE FACTORIES (PRESENTATION LAYER BINDING)
        initializeTableColumns();
        // 2. CONFIGURAZIONE RENDERING GRAFICO (CUSTOM CELL FACTORIES)
        configureDropdownPlaylistsRendering();
        // 3. COMPORTAMENTO REATTIVO E LISTENER (EVENT-DRIVEN LOGIC)
        if (tableTracks != null) {
            configureTableToggleDeselection();
            configureTableSelectionListener();
        }
    }

    /**
     * Inizializza le CellValueFactory per mappare le proprietà del Domain Model
     * (Track) sulle colonne della TableView mediante Reflection.
     */
    private void initializeTableColumns() {
        if (colTitle != null)
            colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        if (colAuthor != null)
            colAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
        if (colDuration != null)
            colDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        if (colGenre != null)
            colGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        if (colYear != null)
            colYear.setCellValueFactory(new PropertyValueFactory<>("year"));
    }

    /**
     * Configura il rendering custom per la ComboBox delle Playlist, garantendo
     * che sia nella tendina che nella cella di bottone venga mostrato solo il nome.
     */
    private void configureDropdownPlaylistsRendering() {
        if (dropdownPlaylists == null)
            return;

        // Configurazione delle celle all'interno del menu a tendina srotolato
        dropdownPlaylists.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Playlist item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getName());
            }
        });

        // Configurazione della cella visibile quando il menu è chiuso
        dropdownPlaylists.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Playlist item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getName());
            }
        });
    }

    /**
     * Applica una RowFactory personalizzata alla TableView intercettando l'evento
     * MOUSE_PRESSED per implementare il comportamento di "Toggle Deselection".
     */
    private void configureTableToggleDeselection() {
        tableTracks.setRowFactory(tv -> {
            final javafx.scene.control.TableRow<Track> row = new javafx.scene.control.TableRow<>();

            row.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                if (event.getButton() != MouseButton.PRIMARY) {
                    return;
                }
                // Se l'utente clicca su una riga che risulta già graficamente selezionata
                if (!row.isEmpty() && row.isSelected()) {
                    // Interrompiamo la selezione forzando lo stato asettico
                    tableTracks.getSelectionModel().clearSelection();
                    selectedTrack = null;

                    if (hboxAddtoPlaylist != null) {
                        hboxAddtoPlaylist.setVisible(false);
                        hboxAddtoPlaylist.setManaged(false);
                    }

                    System.out.println("[UI TOGGLE] Track deselezionata correttamente.");
                    // Consumiamo l'evento per impedire che JavaFX riattivi la selezione standard
                    event.consume();
                }
            });
            return row;
        });
    }

    /**
     * Configura il listener sulla proprietà di selezione della TableView per
     * sincronizzare la barra di aggiunta rapida e popolare la ComboBox.
     */
    private void configureTableSelectionListener() {
        tableTracks.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                // SCENARIO: Riga selezionata con successo
                selectedTrack = newSel;
                System.out.println("Track selected: " + selectedTrack.getTitle());

                // Propaga l'evento al modulo padre (MainViewController) tramite callback
                if (onTrackSelectedHandler != null) {
                    onTrackSelectedHandler.accept(newSel);
                }

                // Interrogazione dinamica della Facade per aggiornare il dropdown delle
                // playlist
                if (facade != null && dropdownPlaylists != null) {
                    //dropdownPlaylists.setItems(FXCollections.observableArrayList(facade.getAllPlaylists()));
                }
                if (playlistViewMode) {
                    if (btnRemoveFromPlaylist != null) {
                        btnRemoveFromPlaylist.setVisible(true);
                        btnRemoveFromPlaylist.setManaged(true);
                    }
                    if (hboxAddtoPlaylist != null) {
                        hboxAddtoPlaylist.setVisible(false);
                        hboxAddtoPlaylist.setManaged(false);
                    }
                } else {
                    if (btnRemoveFromPlaylist != null) {
                        btnRemoveFromPlaylist.setVisible(false);
                        btnRemoveFromPlaylist.setManaged(false);
                    }
                    // Spostamento reattivo del layout: mostra la barra inferiore
                    if (hboxAddtoPlaylist != null) {
                        hboxAddtoPlaylist.setVisible(true);
                        hboxAddtoPlaylist.setManaged(true);
                    }
                }
            } else {
                // SCENARIO: Selezione svuotata (attivata da clearSelection() o filtro)
                selectedTrack = null;
                if (hboxAddtoPlaylist != null) {
                    hboxAddtoPlaylist.setVisible(false);
                    hboxAddtoPlaylist.setManaged(false);
                }
                if (btnRemoveFromPlaylist != null) {
                    btnRemoveFromPlaylist.setVisible(false);
                    btnRemoveFromPlaylist.setManaged(false);
                }
            }
            tableTracks.refresh();
        });
    }

    public void setOnTrackSelected(Consumer<Track> handler) {
        this.onTrackSelectedHandler = handler;
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

            Track newTrack = facade.addTrack(title, author, duration, genre, year);
            if (newTrack != null) {
                tableTracks.getItems().add(newTrack);
                tableTracks.setItems(FXCollections.observableArrayList(currentPlaylistTracks));
                tableTracks.getSelectionModel().clearSelection();
                tableTracks.refresh();
                clearForm();
                lblFeedback.setStyle("-fx-text-fill: green;");
                lblFeedback.setText("Traccia aggiunta con successo.");
                loadCatalog();
            }
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
        if (selectedTrack != null && dropdownPlaylists != null
                && dropdownPlaylists.getSelectionModel().getSelectedItem() != null) {
            Playlist targetPlaylist = dropdownPlaylists.getSelectionModel().getSelectedItem();

            // Qui invocherai il metodo della Facade per associare la traccia (es.
            // facade.addTrackToPlaylist(selectedTrack, targetPlaylist);)

            lblFeedback.setStyle("-fx-text-fill: green;");
            lblFeedback.setText("Traccia '" + selectedTrack.getTitle() + "' aggiunta alla playlist '"
                    + targetPlaylist.getName() + "' con successo.");

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

    @FXML
    private void handleRemoveFromPlaylist(ActionEvent event) {
        removeSelectedTrackFromCurrentPlaylist();
    }

    private void removeSelectedTrackFromCurrentPlaylist() {
        if (!playlistViewMode || currentPlaylist == null || selectedTrack == null) {
            if (lblFeedback != null) {
                lblFeedback.setStyle("-fx-text-fill: #b0413e;");
                lblFeedback.setText("Seleziona una traccia della playlist da rimuovere.");
            }
            return;
        }

        currentPlaylistTracks.remove(selectedTrack);
        tableTracks.setItems(FXCollections.observableArrayList(currentPlaylistTracks));
        tableTracks.getSelectionModel().clearSelection();
        if (btnRemoveFromPlaylist != null) {
            btnRemoveFromPlaylist.setVisible(false);
            btnRemoveFromPlaylist.setManaged(false);
        }
        tableTracks.refresh();

        if (lblFeedback != null) {
            lblFeedback.setStyle("-fx-text-fill: #1f7a1f;");
            if (currentPlaylistTracks.isEmpty()) {
                lblFeedback.setText("Playlist '" + currentPlaylist.getName() + "' vuota.");
            } else {
                lblFeedback.setText("Traccia rimossa da '" + currentPlaylist.getName() + "'.");
            }
        }
    }

    private void loadCatalog() {
        if (facade == null || tableTracks == null) {
            return;
        } else {
            tableTracks.setItems(FXCollections.observableArrayList(facade.getAllTracks()));
        }
    }

    public void showCatalogView() {
        playlistViewMode = false;
        currentPlaylist = null;
        currentPlaylistTracks.clear();
        if (formAddTrack != null) {
            formAddTrack.setVisible(true);
            formAddTrack.setManaged(true);
        }
        if (btnRemoveFromPlaylist != null) {
            btnRemoveFromPlaylist.setVisible(false);
            btnRemoveFromPlaylist.setManaged(false);
        }
        loadCatalog();
        if (tableTracks != null) {
            tableTracks.refresh();
        }
        if (lblFeedback != null) {
            lblFeedback.setStyle("-fx-text-fill: #1f7a1f;");
            lblFeedback.setText("Visualizzazione catalogo completo.");
        }
    }

    /**
     * Pulisce la vista playlist quando la selezione viene rimossa o la playlist viene eliminata.
     * Evita la persistenza di dati "fantasma" nella tabella.
     */
    public void clearPlaylistView() {
        playlistViewMode = false;
        currentPlaylist = null;
        currentPlaylistTracks.clear();

        if (tableTracks != null) {
            tableTracks.getSelectionModel().clearSelection();
            tableTracks.setItems(FXCollections.observableArrayList());
            tableTracks.refresh();
        }
        if (hboxAddtoPlaylist != null) {
            hboxAddtoPlaylist.setVisible(false);
            hboxAddtoPlaylist.setManaged(false);
        }
        if (btnRemoveFromPlaylist != null) {
            btnRemoveFromPlaylist.setVisible(false);
            btnRemoveFromPlaylist.setManaged(false);
        }
        if (lblFeedback != null) {
            lblFeedback.setStyle("-fx-text-fill: #1f7a1f;");
            lblFeedback.setText("Nessuna playlist selezionata.");
        }
        
    }

    public void displayPlaylistTracks(Playlist playlist) {
        if (tableTracks == null || playlist == null) {
            return;
        }
        playlistViewMode = true;
        currentPlaylist = playlist;
        java.util.List<Track> tracksForPlaylist = facade != null
                ? playlist.getTracks()
                : playlist.getTracks();
        currentPlaylistTracks = new java.util.ArrayList<>(tracksForPlaylist);
        if (formAddTrack != null) {
            formAddTrack.setVisible(false);
            formAddTrack.setManaged(false);
        }
        // Quando visualizziamo una playlist specifica, nascondiamo la barra di aggiunta
        // rapida
        if (hboxAddtoPlaylist != null) {
            hboxAddtoPlaylist.setVisible(false);
            hboxAddtoPlaylist.setManaged(false);
        }
        if (btnRemoveFromPlaylist != null) {
            btnRemoveFromPlaylist.setVisible(false);
            btnRemoveFromPlaylist.setManaged(false);
        }
        tableTracks.setItems(FXCollections.observableArrayList(currentPlaylistTracks));
        tableTracks.getSelectionModel().clearSelection();
        tableTracks.refresh();
        System.out.println("Contenuto playlist: " + currentPlaylistTracks);
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
        if (value == null)
            return "";
        String trimmed = value.trim().replaceAll("\\s+", " ");
        if (trimmed.isEmpty())
            return "";
        return trimmed.substring(0, 1).toUpperCase() + trimmed.substring(1).toLowerCase();
    }
}