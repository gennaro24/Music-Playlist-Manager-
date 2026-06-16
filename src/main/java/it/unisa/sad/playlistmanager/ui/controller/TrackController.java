package it.unisa.sad.playlistmanager.ui.controller;

import it.unisa.sad.playlistmanager.application.exceptions.PlaylistNotFoundException;
import it.unisa.sad.playlistmanager.application.exceptions.TrackNotFoundException;
import it.unisa.sad.playlistmanager.application.exceptions.ValidationException;
import it.unisa.sad.playlistmanager.application.facade.MusicPlaylistManagerFacade;
import it.unisa.sad.playlistmanager.domain.model.Playlist;
import it.unisa.sad.playlistmanager.domain.model.Track;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.beans.binding.Bindings;
import java.util.function.Consumer;

/**
 * Sotto-controllore delegato alla visualizzazione, manipolazione e immissione dati
 * concernenti il catalogo globale delle tracce e le canzoni interne a una specifica playlist.
 * <p><b>Revisione Sprint 2 (US-04):</b> Integra la funzionalità di eliminazione di una traccia
 * dal catalogo globale con annesso dialogo di conferma e sincronizzazione in tempo reale delle viste.</p>
 * @version 4.0
 */
public class TrackController {

    /** Istanza centralizzata dell'Application Facade per il pass-through dei casi d'uso. */
    private final MusicPlaylistManagerFacade facade;
    
    /** Riferimento alla traccia attualmente selezionata nella TableView. */
    private Track selectedTrack;
    
    /** Routine di callback per notificare la selezione di una traccia al coordinatore. */
    private Consumer<Track> onTrackSelectedHandler;
    
    /** Routine di callback per intercettare le richieste di riproduzione forzata. */
    private Consumer<Track> onTrackPlayRequestedHandler;

    /** Campo di testo per l'immissione del titolo della traccia. */
    @FXML private TextField txtTitle;
    
    /** Campo di testo per l'immissione dell'autore della traccia. */
    @FXML private TextField txtAuthor;
    
    /** Campo di testo per l'immissione della durata in secondi. */
    @FXML private TextField txtDuration;
    
    /** Campo di testo per l'immissione del genere musicale. */
    @FXML private TextField txtGenre;
    
    /** Campo di testo per l'immissione dell'anno di pubblicazione. */
    @FXML private TextField txtYear;
    
    /** Etichetta informativa inferiore destinata ai feedback operativi per l'utente. */
    @FXML private Label lblFeedback;

    /** Tabella principale per la renderizzazione delle tracce musicali. */
    @FXML private TableView<Track> tableTracks;
    
    /** Colonna per la visualizzazione e l'editing del titolo. */
    @FXML private TableColumn<Track, String> colTitle;
    
    /** Colonna per la visualizzazione e l'editing dell'autore. */
    @FXML private TableColumn<Track, String> colAuthor;
    
    /** Colonna per la visualizzazione e l'editing della durata. */
    @FXML private TableColumn<Track, Integer> colDuration;
    
    /** Colonna per la visualizzazione e l'editing del genere. */
    @FXML private TableColumn<Track, String> colGenre;
    
    /** Colonna per la visualizzazione e l'editing dell'anno. */
    @FXML private TableColumn<Track, Integer> colYear;
    
    /** Contenitore grafico del modulo di inserimento tracce. */
    @FXML private VBox formAddTrack;
    
    /** Menu a tendina per la scelta della playlist a cui associare il brano. */
    @FXML private ComboBox<Playlist> dropdownPlaylists;
    
    /** Contenitore dei comandi di aggiunta rapida a una playlist. */
    @FXML private HBox hboxAddtoPlaylist;
    
    /** Pulsante contestuale per escludere una traccia dalla playlist visualizzata. */
    @FXML private Button btnRemoveFromPlaylist;

    /** Pulsante contestuale per riprodurre tutto il catalogo. */
    @FXML private Button btnPlayCatalog;

    /** Flag discriminante per comprendere se la UI mostra il catalogo o una playlist. */
    private boolean playlistViewMode = false;
    
    /** Riferimento alla playlist attualmente visualizzata nella TableView. */
    private Playlist currentPlaylist;
    
    /** Lista interna contenente la copia speculare delle tracce della playlist attiva. */
    private java.util.List<Track> currentPlaylistTracks = new java.util.ArrayList<>();

    /**
     * Costruttore per Constructor Injection (Task T-63).
     *
     * @param facade L'istanza centralizzata dell'Application Facade.
     */
    public TrackController(MusicPlaylistManagerFacade facade) {
        this.facade = facade;
    }

    /**
     * Inizializza i componenti grafici della TableView, abilita l'editing in linea,
     * effettua il data-binding ed esegue il caricamento dei dati di catalogo a startup.
     */
    @FXML
    private void initialize() {
        if (tableTracks != null) {
            tableTracks.setEditable(true);
        }

        initializeTableColumns();
        configureDropdownPlaylistsRendering();
        if (tableTracks != null) {
            configureTableToggleDeselection();
            configureTableSelectionListener();
        }

        if (this.facade != null) {
            loadCatalog();
            if (dropdownPlaylists != null) {
                dropdownPlaylists.setItems(FXCollections.observableArrayList(this.facade.getAllPlaylists()));
            }
        }
    }

    /**
     * Associa le colonne della TableView ai campi dati del Domain Model (Track)
     * e configura i cell factory custom per l'inline editing automatico al focus lost.
     */
    private void initializeTableColumns() {
        if (colTitle != null) {
            colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
            colTitle.setCellFactory(col -> new EditableTableCell<>(val -> val));
            colTitle.setOnEditCommit(event -> handleInlineEdit(event.getRowValue(), 
                val -> new Track(event.getRowValue().getId(), val, event.getRowValue().getAuthor(), event.getRowValue().getDuration(), event.getRowValue().getGenre(), event.getRowValue().getYear()), event.getNewValue()));
        }
        if (colAuthor != null) {
            colAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
            colAuthor.setCellFactory(col -> new EditableTableCell<>(val -> val));
            colAuthor.setOnEditCommit(event -> handleInlineEdit(event.getRowValue(), 
                val -> new Track(event.getRowValue().getId(), event.getRowValue().getTitle(), val, event.getRowValue().getDuration(), event.getRowValue().getGenre(), event.getRowValue().getYear()), event.getNewValue()));
        }
        if (colDuration != null) {
            colDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
            colDuration.setCellFactory(col -> new EditableTableCell<>(Integer::parseInt));
            colDuration.setOnEditCommit(event -> handleInlineEdit(event.getRowValue(), 
                val -> new Track(event.getRowValue().getId(), event.getRowValue().getTitle(), event.getRowValue().getAuthor(), val, event.getRowValue().getGenre(), event.getRowValue().getYear()), event.getNewValue()));
        }
        if (colGenre != null) {
            colGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
            colGenre.setCellFactory(col -> new EditableTableCell<>(val -> val));
            colGenre.setOnEditCommit(event -> handleInlineEdit(event.getRowValue(), 
                val -> new Track(event.getRowValue().getId(), event.getRowValue().getTitle(), event.getRowValue().getAuthor(), event.getRowValue().getDuration(), val, event.getRowValue().getYear()), event.getNewValue()));
        }
        if (colYear != null) {
            colYear.setCellValueFactory(new PropertyValueFactory<>("year"));
            colYear.setCellFactory(col -> new EditableTableCell<>(Integer::parseInt));
            colYear.setOnEditCommit(event -> handleInlineEdit(event.getRowValue(), 
                val -> new Track(event.getRowValue().getId(), event.getRowValue().getTitle(), event.getRowValue().getAuthor(), event.getRowValue().getDuration(), event.getRowValue().getGenre(), val), event.getNewValue()));
        }
        configureResponsiveColumnWidths();
    }

    /**
     * Coordina ed esegue in differita l'aggiornamento dei dati tramite Facade, intercettando
     * le eccezioni di validazione per stampare a schermo l'errore standardizzato (Task T-79).
     *
     * @param <T>          Il tipo di dato generico gestito dalla colonna.
     * @param oldTrack     L'istanza originale della traccia prima della modifica.
     * @param trackCreator Funzione lambda funzionale atta a istanziare la nuova traccia immutabile.
     * @param newValue     Il valore testuale o numerico appena inserito dall'utente.
     */
    private <T> void handleInlineEdit(Track oldTrack, java.util.function.Function<T, Track> trackCreator, T newValue) {
        Platform.runLater(() -> {
            try {
                if (newValue == null) throw new IllegalArgumentException();
                Track updatedTrack = trackCreator.apply(newValue);
                facade.updateTrack(oldTrack.getId(), updatedTrack);
                labelFeedback("Traccia modificata con successo.", "green");
            } catch (Exception e) {
                labelFeedback("Errore nella modifica", "red");
            }
            loadCatalog();
            if (playlistViewMode && currentPlaylist != null) {
                loadPlaylistTracks(currentPlaylist);
            }
        });
    }

    /**
     * Classe interna di supporto per incorporare un TextField reattivo all'interno delle celle.
     * Consolida le modifiche in modo sincrono non appena viene perso il focus (Blur).
     *
     * @param <R> Tipo di riga del modello (Track).
     * @param <T> Tipo di cella specifico.
     */
    private class EditableTableCell<R, T> extends TableCell<R, T> {
        /** Componente di input testuale inserito dinamicamente nella cella in stato di editing. */
        private TextField textField;
        /** Funzione di conversione per mappare la stringa digitata nel tipo T appropriato. */
        private final java.util.function.Function<String, T> converter;

        /**
         * Costruttore della cella editabile inline.
         *
         * @param converter Convertitore funzionale da String a T.
         */
        public EditableTableCell(java.util.function.Function<String, T> converter) {
            this.converter = converter;
        }

        @Override
        public void startEdit() {
            if (!isEmpty()) {
                super.startEdit();
                createTextField();
                setText(null);
                setGraphic(textField);
                textField.requestFocus();
                textField.selectAll();
            }
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            setText(getItem() != null ? getItem().toString() : null);
            setGraphic(null);
        }

        @Override
        public void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (textField != null) {
                        textField.setText(item != null ? item.toString() : "");
                    }
                    setText(null);
                    setGraphic(textField);
                } else {
                    setText(item != null ? item.toString() : null);
                    setGraphic(null);
                }
            }
        }

        /**
         * Istanzia il TextField e aggancia i relativi listener per intercettare l'Invio
         * o la perdita del focus da parte dell'utente (Blur).
         */
        private void createTextField() {
            textField = new TextField(getItem() != null ? getItem().toString() : "");
            textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
            textField.setOnAction(e -> triggerCommit());
            textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal && isEditing()) {
                    triggerCommit();
                }
            });
        }

        /**
         * Tenta il commit del valore modificato catturando le eccezioni di parsing sintattico.
         */
        private void triggerCommit() {
            try {
                commitEdit(converter.apply(textField.getText().trim()));
            } catch (Exception ex) {
                cancelEdit();
                labelFeedback("Errore nella modifica", "red");
            }
        }
    }

    /**
     * Imposta il dimensionamento proporzionale e vincolato (Responsive) delle colonne.
     */
    private void configureResponsiveColumnWidths() {
        if (tableTracks == null || colTitle == null || colAuthor == null
                || colDuration == null || colGenre == null || colYear == null) {
            return;
        }
        tableTracks.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        final double weightTitle = 0.30;
        final double weightAuthor = 0.30;
        final double weightDuration = 0.12;
        final double weightGenre = 0.18;
        final double weightYear = 0.10;
        double scrollbarOffset = 15.0;

        colTitle.prefWidthProperty().bind(tableTracks.widthProperty().subtract(scrollbarOffset).multiply(weightTitle));
        colAuthor.prefWidthProperty().bind(tableTracks.widthProperty().subtract(scrollbarOffset).multiply(weightAuthor));
        colDuration.prefWidthProperty().bind(tableTracks.widthProperty().subtract(scrollbarOffset).multiply(weightDuration));
        colGenre.prefWidthProperty().bind(tableTracks.widthProperty().subtract(scrollbarOffset).multiply(weightGenre));
        colYear.prefWidthProperty().bind(tableTracks.widthProperty().subtract(scrollbarOffset).multiply(weightYear));

        colTitle.setMinWidth(150);
        colAuthor.setMinWidth(130);
        colDuration.setMinWidth(70);
        colGenre.setMinWidth(100);
        colYear.setMinWidth(65);
    }

    /**
     * Configura il rendering custom all'interno del menu a tendina delle playlist,
     * assicurando l'estrazione pulita della proprietà stringa del nome dell'oggetto.
     */
    private void configureDropdownPlaylistsRendering() {
        if (dropdownPlaylists == null) return;
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
                setText((empty || item == null) ? "Seleziona una playlist" : item.getName());
            }
        });
    }

    /**
     * Configura la riga della tabella iniettando un menu contestuale per il comando "Play",
     * "Modifica" e il nuovo comando di eliminazione dal catalogo globale (Task T-89).
     */
    private void configureTableToggleDeselection() {
        tableTracks.setRowFactory(tv -> {
            final TableRow<Track> row = new TableRow<>();
            MenuItem playItem = new MenuItem("Play");
            MenuItem editItem = new MenuItem("Modifica"); 
            MenuItem deleteItem = new MenuItem("Elimina dal catalogo"); // Iniezione Task T-89
            ContextMenu contextMenu = new ContextMenu(playItem, editItem, deleteItem);
            
            playItem.setOnAction(event -> {
                Track track = row.getItem();
                if (track == null) return;
                tableTracks.getSelectionModel().select(track);
                selectedTrack = track;
                if (onTrackPlayRequestedHandler != null)
                    onTrackPlayRequestedHandler.accept(track);
            });

            editItem.setOnAction(event -> {
                Track track = row.getItem();
                if (track == null) return;
                tableTracks.getSelectionModel().select(track);
                selectedTrack = track;
                
                TableColumn<Track, ?> focusedColumn = tableTracks.getFocusModel().getFocusedCell().getTableColumn();
                if (focusedColumn != null && focusedColumn.isEditable()) {
                    tableTracks.edit(row.getIndex(), focusedColumn);
                } else {
                    tableTracks.edit(row.getIndex(), colTitle);
                }
            });

            // TASK T-89 e T-90: Finestra di dialogo di conferma ed eliminazione traccia
            deleteItem.setOnAction(event -> {
                Track track = row.getItem();
                if (track == null) return;

                // T-89: Configurazione e apertura del dialogo di conferma nativo
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Conferma Eliminazione");
                alert.setHeaderText("Eliminare la traccia dal catalogo?");
                alert.setContentText("L'operazione rimuoverà definitivamente la traccia '" + track.getTitle() + "' anche da tutte le playlist.");

                java.util.Optional<ButtonType> confirmationResult = alert.showAndWait();
                if (confirmationResult.isPresent() && confirmationResult.get() == ButtonType.OK) {
                    try {
                        facade.deleteTrack(track.getId());
                        labelFeedback("Traccia eliminata con successo.", "green");
                        
                        // T-90: Aggiornamento reattivo in tempo reale del catalogo e della playlist corrente
                        loadCatalog();
                        if (playlistViewMode && currentPlaylist != null) {
                            loadPlaylistTracks(currentPlaylist);
                        }
                        tableTracks.getSelectionModel().clearSelection();
                        selectedTrack = null;
                    } catch (Exception e) {
                        labelFeedback("Errore durante l'eliminazione della traccia.", "red");
                    }
                } else {
                    labelFeedback("Eliminazione annullata.", "#0066cc"); // Scenario 2
                }
            });

            row.contextMenuProperty().bind(
                Bindings.when(row.emptyProperty())
                        .then((ContextMenu) null)
                        .otherwise(contextMenu)
            );

            row.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1 && row.isEmpty()) {
                    tableTracks.getSelectionModel().clearSelection();
                    selectedTrack = null;
                    if (hboxAddtoPlaylist != null) {
                        hboxAddtoPlaylist.setVisible(false);
                        hboxAddtoPlaylist.setManaged(false);
                    }
                }
            });
            return row;
        });
    }

    /**
     * Gestisce i mutamenti di stato della riga selezionata, allineando dinamicamente
     * la visibilità dei pannelli d'azione contestuali (Barra d'aggiunta vs Rimozione).
     */
    private void configureTableSelectionListener() {
        tableTracks.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                selectedTrack = newSel;
                if (onTrackSelectedHandler != null) {
                    onTrackSelectedHandler.accept(newSel);
                }
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
                    if (hboxAddtoPlaylist != null) {
                        hboxAddtoPlaylist.setVisible(true);
                        hboxAddtoPlaylist.setManaged(true);
                    }
                    if (dropdownPlaylists != null) {
                        dropdownPlaylists.getSelectionModel().clearSelection();
                        dropdownPlaylists.setValue(null);
                        dropdownPlaylists.setPromptText("Seleziona una playlist");
                    }
                }
            } else {
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

    /**
     * Registra il consumatore delegato a catturare gli eventi di selezione traccia.
     * * @param handler Routine di callback esposta dal coordinatore principale.
     */
    public void setOnTrackSelected(Consumer<Track> handler) {
        this.onTrackSelectedHandler = handler;
    }

    /**
     * Registra il consumatore delegato a catturare gli eventi di riproduzione forzata.
     *
     * @param handler Routine di callback esposta dal coordinatore principale.
     */
    public void setOnTrackPlayRequested(Consumer<Track> handler){
        this.onTrackPlayRequestedHandler = handler;
    }

    @FXML
    private void handlePlayCatalog(ActionEvent event) {
        if (facade == null) {
            labelFeedback("Errore: Facade non inizializzata.", "red");
            return;
        }
        try {
            facade.playCatalog();
            labelFeedback("Riproduzione del catalogo globale avviata.", "green");
        } catch (Exception e) {
            labelFeedback("Errore durante l'avvio del catalogo: " + e.getMessage(), "red");
        }
    }

    /**
     * Intercetta la richiesta di inserimento di una nuova traccia nel sistema.
     * Valida sintatticamente i campi e delega la persistenza all'Application Facade.
     *
     * @param event Evento di click associato al pulsante d'aggiunta.
     */
    @FXML
    private void handleTrackAddition(ActionEvent event) {
        lblFeedback.setText("");
        if (facade == null) {
            labelFeedback("Errore interno: facade non inizializzata.", "red");
            return;
        }
        try {
            String title = toSentenceCase(txtTitle.getText());
            String author = toSentenceCase(txtAuthor.getText());
            String genre = toSentenceCase(txtGenre.getText());
            int duration = Integer.parseInt(txtDuration.getText().trim());
            int year = Integer.parseInt(txtYear.getText().trim());
            validateDataInput(title, author, genre, duration, year);

            Track newTrack = facade.addTrack(title, author, duration, genre, year);
            if (newTrack != null) {
                tableTracks.getSelectionModel().clearSelection();
                clearForm();
                labelFeedback("Traccia aggiunta con successo.", "green");
                loadCatalog();
            }
        } catch (NumberFormatException e) {
            labelFeedback("Durata e anno devono essere numeri validi.", "red");
        } catch (ValidationException | it.unisa.sad.playlistmanager.domain.exceptions.ValidationException e) {
            labelFeedback(e.getMessage(), "red");
        } catch (IllegalArgumentException e) {
            labelFeedback(e.getMessage(), "red");
        } catch (Exception e) {
            labelFeedback("Errore durante il salvataggio della traccia.", "red");
            e.printStackTrace();
        }
    }

    /**
     * Valida la correttezza dei dati di input della traccia prima dell'invio ai servizi.
     */
    private void validateDataInput(String title, String author, String genre, int duration, int year) {
        if (title == null || title.trim().isEmpty()) throw new IllegalArgumentException("Errore di inserimento titolo: il titolo della traccia è obbligatorio.");
        if (author == null || author.trim().isEmpty()) throw new IllegalArgumentException("Errore di inserimento autore: l'autore della traccia è obbligatorio.");
        if (genre == null || genre.trim().isEmpty()) throw new IllegalArgumentException("Errore di inserimento genere: il genere della traccia è obbligatorio.");
        if (duration <= 0) throw new IllegalArgumentException("Errore di inserimento durata: la durata della traccia deve essere maggiore di zero.");
        if (year <= 0 || year > java.time.Year.now().getValue()) throw new IllegalArgumentException("Errore di inserimento anno: l'anno della traccia non è valido.");
    }

    /**
     * Lega la traccia selezionata alla playlist bersaglio scelta all'interno del menu a tendina.
     *
     * @param event Evento di click del bottone di salvataggio.
     */
    @FXML
    private void handleSaveAddPlaylist(ActionEvent event) {
        if (selectedTrack != null && dropdownPlaylists != null && dropdownPlaylists.getSelectionModel().getSelectedItem() != null) {
            Playlist targetPlaylist = dropdownPlaylists.getSelectionModel().getSelectedItem();
            try {
                facade.addTrackToPlaylist(targetPlaylist.getId(), selectedTrack.getId());
            } catch (ValidationException | PlaylistNotFoundException | TrackNotFoundException | IllegalArgumentException e) {
                labelFeedback(e.getMessage(), "red");
                return;
            }

            labelFeedback("Traccia '" + selectedTrack.getTitle() + "' aggiunta alla playlist '" + targetPlaylist.getName() + "' con successo.", "green");
            if (playlistViewMode && currentPlaylist != null && currentPlaylist.getId().equals(targetPlaylist.getId())) {
                loadPlaylistTracks(currentPlaylist);
            }
            dropdownPlaylists.setItems(FXCollections.observableArrayList(facade.getAllPlaylists()));
            if (hboxAddtoPlaylist != null) {
                hboxAddtoPlaylist.setVisible(false);
                hboxAddtoPlaylist.setManaged(false);
            }
        } else {
            labelFeedback("Seleziona una playlist valida dal menu a tendina.", "red");
        }
    }

    /**
     * Intercetta la sottomissione del pulsante grafico per rimuovere il brano dalla playlist corrente.
     * * @param event Evento di click associato al pulsante.
     */
    @FXML
    private void handleRemoveFromPlaylist(ActionEvent event) {
        removeSelectedTrackFromCurrentPlaylist();
    }

    /**
     * Rimuove la traccia selezionata dalla playlist corrente interrogando la Facade.
     */
    private void removeSelectedTrackFromCurrentPlaylist() {
        if (!playlistViewMode || currentPlaylist == null || selectedTrack == null) {
            labelFeedback("Seleziona una traccia della playlist da rimuovere.", "red");
            return;
        }

        try {
            facade.removeTrackFromPlaylist(currentPlaylist.getId(), selectedTrack.getId());
        } catch (ValidationException | PlaylistNotFoundException | TrackNotFoundException | IllegalArgumentException e) {
            labelFeedback(e.getMessage(), "red");
            return;
        }
        loadPlaylistTracks(currentPlaylist);
        if (btnRemoveFromPlaylist != null) {
            btnRemoveFromPlaylist.setVisible(false);
            btnRemoveFromPlaylist.setManaged(false);
        }

        if (lblFeedback != null) {
            if (currentPlaylistTracks.isEmpty()) {
                labelFeedback("Playlist '" + currentPlaylist.getName() + "' vuota.", "#1f7a1f");
            } else {
                labelFeedback("Traccia rimossa da '" + currentPlaylist.getName() + "'.", "#1f7a1f");
            }
        }
    }

    /**
     * Interroga la Facade per caricare tutte le tracce presenti nel sistema all'interno della TableView.
     */
    private void loadCatalog() {
        if (facade == null || tableTracks == null) return;
        tableTracks.setItems(FXCollections.observableArrayList(facade.getAllTracks()));
        tableTracks.setPlaceholder(new Label("Catalogo vuoto. Aggiungi una traccia."));
    }

    /**
     * Commuta lo stato visivo abilitando il form di immissione dati per la visualizzazione catalogo.
     */
    public void showCatalogView() {
        playlistViewMode = false;
        currentPlaylist = null;
        currentPlaylistTracks.clear();
        
        if (btnPlayCatalog != null) {
            btnPlayCatalog.setVisible(true);
            btnPlayCatalog.setManaged(true);
        }
        if (formAddTrack != null) {
            formAddTrack.setVisible(true);
            formAddTrack.setManaged(true);
        }
        if (btnRemoveFromPlaylist != null) {
            btnRemoveFromPlaylist.setVisible(false);
            btnRemoveFromPlaylist.setManaged(false);
        }
        loadCatalog();
        if (tableTracks != null) tableTracks.refresh();
        if (lblFeedback != null) {
            if (tableTracks != null && tableTracks.getItems() != null && tableTracks.getItems().isEmpty()) {
                labelFeedback("Catalogo vuoto. Aggiungi una traccia.", "#b0413e");
            } else {
                labelFeedback("Visualizzazione catalogo completo.", "#1f7a1f");
            }
        }
    }

    /**
     * Annulla lo stato dei dati e svuota la tabella quando non vi è selezione attiva.
     */
    public void clearPlaylistView() {
        playlistViewMode = false;
        currentPlaylist = null;
        currentPlaylistTracks.clear();

        if (btnPlayCatalog != null) {
            btnPlayCatalog.setVisible(false);
            btnPlayCatalog.setManaged(false);
        }
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
        labelFeedback("Nessuna playlist selezionata.", "#1f7a1f");
    }

    /**
     * Predispone la tabella per accogliere esclusivamente i brani della playlist sotto analisi.
     *
     * @param playlist L'istanza di Playlist da ispezionare.
     */
    public void displayPlaylistTracks(Playlist playlist) {
        if (tableTracks == null || playlist == null) return;
        playlistViewMode = true;
        currentPlaylist = playlist;
        currentPlaylistTracks = new java.util.ArrayList<>(facade != null ? facade.getTracksForPlaylist(playlist.getId()) : playlist.getTracks());
        tableTracks.setPlaceholder(new Label("Questa playlist non contiene tracce."));
        
        
        if (btnPlayCatalog != null) {
            btnPlayCatalog.setVisible(false);
            btnPlayCatalog.setManaged(false);
        }
        if (formAddTrack != null) {
            formAddTrack.setVisible(false);
            formAddTrack.setManaged(false);
        }
        if (hboxAddtoPlaylist != null) {
            hboxAddtoPlaylist.setVisible(false);
            hboxAddtoPlaylist.setManaged(false);
        }
        if (btnRemoveFromPlaylist != null) {
            btnRemoveFromPlaylist.setVisible(false);
            btnRemoveFromPlaylist.setManaged(false);
        }
        loadPlaylistTracks(playlist);
        
        Platform.runLater(() -> {
            tableTracks.applyCss();
            tableTracks.layout();
            tableTracks.refresh();
        });
        labelFeedback("Contenuto playlist: " + playlist.getName(), "#0066cc");
    }

    /**
     * Esegue il recupero sincrono dei brani di una playlist aggiornando gli elementi della TableView.
     * * @param playlist L'istanza di Playlist da aggiornare a livello grafico.
     */
    public void loadPlaylistTracks(Playlist playlist) {
        if (tableTracks == null || playlist == null || facade == null) return;
        try {
            currentPlaylistTracks = facade.getTracksForPlaylist(playlist.getId());
        } catch (ValidationException | PlaylistNotFoundException | IllegalArgumentException e) {
            labelFeedback(e.getMessage(), "red");
            currentPlaylistTracks = new java.util.ArrayList<>();
        }
        tableTracks.setItems(FXCollections.observableArrayList(currentPlaylistTracks));
        tableTracks.getSelectionModel().clearSelection();
        tableTracks.refresh();
    }

    /**
     * Svuota i campi testuali del modulo di inserimento.
     */
    public void clearForm() {
        txtTitle.clear();
        txtAuthor.clear();
        txtDuration.clear();
        txtGenre.clear();
        txtYear.clear();
    }

    /**
     * Uniforma la formattazione di una stringa impostando in maiuscolo il primo carattere.
     */
    private String toSentenceCase(String value) {
        if (value == null) return "";
        String trimmed = value.trim().replaceAll("\\s+", " ");
        if (trimmed.isEmpty()) return "";
        return trimmed.substring(0, 1).toUpperCase() + trimmed.substring(1).toLowerCase();
    }

    /**
     * Modifica lo stile cromatico e il testo della etichetta informativa inferiore.
     * * @param text  Il testo descrittivo da stampare.
     * @param color La specifica CSS per la colorazione del font.
     */
    public void labelFeedback(String text, String color) {
        if (lblFeedback != null) {
            lblFeedback.setStyle("-fx-text-fill: " + color + ";");
            lblFeedback.setText(text);
        }
    }

    /**
     * TASK T3-12: Rinfresca in tempo reale lo stato dei dati visibili (Catalogo o Playlist)
     * per riallineare la Tabella a seguito di operazioni mutative o comandi di Undo.
     */
    public void refresh() {
        if (playlistViewMode && currentPlaylist != null) {
            loadPlaylistTracks(currentPlaylist);
        } else {
            loadCatalog();
        }
    }
}