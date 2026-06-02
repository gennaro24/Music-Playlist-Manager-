package it.unisa.sad.playlistmanager.ui.controller;

import it.unisa.sad.playlistmanager.application.facade.MusicPlaylistManagerFacade;
import it.unisa.sad.playlistmanager.domain.model.Playlist;
import it.unisa.sad.playlistmanager.domain.model.Track;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.collections.FXCollections;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.TableRow;
import javafx.scene.layout.VBox;
import java.util.function.Consumer;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
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
        configureResponsiveColumnWidths();
    }

    /**
     * Configura il dimensionamento responsive delle colonne in base alle
     * proporzioni ideali,
     * definendo limiti di usabilità (minWidth) per evitare il collasso visivo dei
     * dati.
     */
    private void configureResponsiveColumnWidths() {
        if (tableTracks == null || colTitle == null || colAuthor == null
                || colDuration == null || colGenre == null || colYear == null) {
            return;
        }

        //Abilitazione della politica di ridimensionamento vincolata nativa
        tableTracks.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // 2. Definizione dei pesi proporzionali (Percentuali ideali espresse come
        // double)
        final double weightTitle = 0.30;
        final double weightAuthor = 0.30;
        final double weightDuration = 0.12;
        final double weightGenre = 0.18;
        final double weightYear = 0.10;

        // Binding dinamico normalizzato sul contenitore
        // Sottraiamo un offset fisso empirico per prevenire l'attivazione della
        // scrollbar orizzontale
        double scrollbarOffset = 15.0;

        colTitle.prefWidthProperty().bind(tableTracks.widthProperty().subtract(scrollbarOffset).multiply(weightTitle));
        colAuthor.prefWidthProperty()
                .bind(tableTracks.widthProperty().subtract(scrollbarOffset).multiply(weightAuthor));
        colDuration.prefWidthProperty()
                .bind(tableTracks.widthProperty().subtract(scrollbarOffset).multiply(weightDuration));
        colGenre.prefWidthProperty().bind(tableTracks.widthProperty().subtract(scrollbarOffset).multiply(weightGenre));
        colYear.prefWidthProperty().bind(tableTracks.widthProperty().subtract(scrollbarOffset).multiply(weightYear));

        // Width)
        // Impedisce che il ridimensionamento della finestra renda il testo illeggibile
        colTitle.setMinWidth(150);
        colAuthor.setMinWidth(130);
        colDuration.setMinWidth(70);
        colGenre.setMinWidth(100);
        colYear.setMinWidth(65);
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
            final TableRow<Track> row = new TableRow<>();

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
                    dropdownPlaylists.setItems(FXCollections.observableArrayList(facade.getAllPlaylists()));
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

    /**
     * metodo per aggiungere una traccia al catalogo
     * controlla che il facade sia inizializzato e che i dati inseriti siano validi
     * 
     * @param event
     */
    @FXML
    private void handleTrackAddition(ActionEvent event) {
        lblFeedback.setText("");
        if (facade == null) {
            lblFeedback.setText("Errore interno: facade non inizializzata.");
            return;
        }
        // controlla che i campi siano compilati
        try {
            String title = toSentenceCase(txtTitle.getText());
            String author = toSentenceCase(txtAuthor.getText());
            String genre = toSentenceCase(txtGenre.getText());
            int duration = Integer.parseInt(txtDuration.getText().trim());
            int year = Integer.parseInt(txtYear.getText().trim());
            validateDataInput(title, author, genre, duration, year);
            // se la funzione validateDataInput non genera eccezioni, aggiunge la traccia al
            // catalogo

            Track newTrack = facade.addTrack(title, author, duration, genre, year);
            if (newTrack != null) {
                // aggiunge la traccia alla tabella
                tableTracks.getItems().add(newTrack);

                tableTracks.setItems(FXCollections.observableArrayList(currentPlaylistTracks));
                // pulisce la selezione della tabella
                tableTracks.getSelectionModel().clearSelection();
                tableTracks.refresh();
                // pulisce il form
                clearForm();
                // modifica il feedback in verde
                labelFeedback("Traccia aggiunta con successo.", "green");
                loadCatalog();
            }
        } catch (NumberFormatException e) {
            labelFeedback("Durata e anno devono essere numeri validi.", "red");
        } catch (IllegalArgumentException e) {
            labelFeedback(e.getMessage(), "red");
        } catch (Exception e) {
            labelFeedback("Errore durante il salvataggio della traccia.", "red");
            e.printStackTrace();
        }
    }

    private void validateDataInput(String title, String author, String genre, int duration, int year) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Errore di inserimento titolo: il titolo della traccia è obbligatorio.");
        }
        if (author == null || author.trim().isEmpty()) {
            throw new IllegalArgumentException("Errore di inserimento autore: l'autore della traccia è obbligatorio.");
        }
        if (genre == null || genre.trim().isEmpty()) {
            throw new IllegalArgumentException("Errore di inserimento genere: il genere della traccia è obbligatorio.");
        }
        if (duration <= 0) {
            throw new IllegalArgumentException("Errore di inserimento durata: la durata della traccia è obbligatorio.");
        }
        if (year <= 0 || year > java.time.Year.now().getValue()) {
            throw new IllegalArgumentException("Errore di inserimento anno: l'anno della traccia è obbligatorio.");
        }
    }

    /**
     * metodo per aggiungere una traccia a una playlist, tramite la combo box delle
     * playlist
     * e il bottone di save
     * 
     * @param event
     */
    @FXML
    private void handleSaveAddPlaylist(ActionEvent event) {
        // controlla che la traccia selezionata e la playlist selezionata siano non
        // nulle
        if (selectedTrack != null && dropdownPlaylists != null
                && dropdownPlaylists.getSelectionModel().getSelectedItem() != null) {
            Playlist targetPlaylist = dropdownPlaylists.getSelectionModel().getSelectedItem();
            String selectedTrackId = selectedTrack.getId();
            String targetPlaylistId = targetPlaylist.getId();
            System.out.println("selectedTrackId: " + selectedTrackId);
            System.out.println("targetPlaylistId: " + targetPlaylistId);
            facade.addTrackToPlaylist(targetPlaylistId, selectedTrackId);

            labelFeedback("Traccia '" + selectedTrack.getTitle() + "' aggiunta alla playlist '"
                    + targetPlaylist.getName() + "' con successo.", "green");
            refreshCurrentPlaylistTable(targetPlaylistId);
            //aggiorna la lista delle playlist
            dropdownPlaylists.setItems(FXCollections.observableArrayList(facade.getAllPlaylists()));
            // Opzionale: Nascondiamo l'HBox dopo il salvataggio per pulizia di interfaccia
            if (hboxAddtoPlaylist != null) {
                hboxAddtoPlaylist.setVisible(false);
                hboxAddtoPlaylist.setManaged(false);
            }
        } else {
            labelFeedback("Seleziona una playlist valida dal menu a tendina.", "red");
        }
    }

    @FXML
    private void handleRemoveFromPlaylist(ActionEvent event) {
        removeSelectedTrackFromCurrentPlaylist();
    }

    private void removeSelectedTrackFromCurrentPlaylist() {
        if (!playlistViewMode || currentPlaylist == null || selectedTrack == null) {
            if (lblFeedback != null) {
                labelFeedback("Seleziona una traccia della playlist da rimuovere.", "red");
            }
            return;
        }

        facade.removeTrackFromPlaylist(currentPlaylist.getId(), selectedTrack.getId());
        System.out.println("currentPlaylistTracks: " + currentPlaylistTracks);
        tableTracks.setItems(FXCollections.observableArrayList(currentPlaylistTracks));
        tableTracks.getSelectionModel().clearSelection();
        if (btnRemoveFromPlaylist != null) {
            btnRemoveFromPlaylist.setVisible(false);
            btnRemoveFromPlaylist.setManaged(false);
        }
        tableTracks.refresh();

        if (lblFeedback != null) {
            if (currentPlaylistTracks.isEmpty()) {
                labelFeedback("Playlist '" + currentPlaylist.getName() + "' vuota.", "#1f7a1f");
            } else {
                labelFeedback("Traccia rimossa da '" + currentPlaylist.getName() + "'.", "#1f7a1f");
            }
        }
    }

    private void loadCatalog() {
        if (facade == null || tableTracks == null) {
            return;
        } else {
            tableTracks.setItems(FXCollections.observableArrayList(facade.getAllTracks()));
            tableTracks.setPlaceholder(new Label("Catalogo vuoto. Aggiungi una traccia."));
            if (tableTracks.getItems().isEmpty() && lblFeedback != null) {
                lblFeedback.setStyle("-fx-text-fill: #b0413e;");
                lblFeedback.setText("Catalogo vuoto. Aggiungi una traccia.");
            }
        }
    }

    private void refreshCurrentPlaylistTable(String playlistId) {
        Playlist refreshed = facade.getPlaylistById(playlistId);
        if (refreshed == null || tableTracks == null) return;
    
        currentPlaylist = refreshed;
        currentPlaylistTracks = new java.util.ArrayList<>(refreshed.getTracks());
        System.out.println("currentPlaylistTracks: " + currentPlaylistTracks);
    
        tableTracks.setItems(FXCollections.observableArrayList(currentPlaylistTracks));
        tableTracks.getSelectionModel().clearSelection();
        tableTracks.refresh();
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
            if (tableTracks != null && tableTracks.getItems() != null && tableTracks.getItems().isEmpty()) {
                labelFeedback("Catalogo vuoto. Aggiungi una traccia.", "#b0413e");
            } else {
                labelFeedback("Visualizzazione catalogo completo.", "#1f7a1f");
            }
        }
    }

    /**
     * Pulisce la vista playlist quando la selezione viene rimossa o la playlist
     * viene eliminata.
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
            labelFeedback("Nessuna playlist selezionata.", "#1f7a1f");
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
        tableTracks.setPlaceholder(new Label("Questa playlist non contiene tracce."));
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
        // Primo accesso: forza il layout dopo che il nodo è realmente visibile nel
        // scene graph.
        Platform.runLater(() -> {
            tableTracks.applyCss();
            tableTracks.layout();
            tableTracks.refresh();
        });
        System.out.println("Contenuto playlist: " + currentPlaylistTracks);
        if (lblFeedback != null) {
            labelFeedback("Contenuto playlist: " + playlist.getName(), "#0066cc");
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

    public void labelFeedback(String text, String color) {
        if (lblFeedback != null) {
            lblFeedback.setStyle("-fx-text-fill: " + color + ";");
            lblFeedback.setText(text);
        }
    }
}