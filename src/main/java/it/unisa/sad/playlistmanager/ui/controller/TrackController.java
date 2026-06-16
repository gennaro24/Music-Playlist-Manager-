package it.unisa.sad.playlistmanager.ui.controller;

import it.unisa.sad.playlistmanager.application.exceptions.PlaylistNotFoundException;
import it.unisa.sad.playlistmanager.application.exceptions.TagNotFoundException;
import it.unisa.sad.playlistmanager.application.exceptions.TrackNotFoundException;
import it.unisa.sad.playlistmanager.application.exceptions.ValidationException;
import it.unisa.sad.playlistmanager.application.facade.MusicPlaylistManagerFacade;
import it.unisa.sad.playlistmanager.domain.model.Playlist;
import it.unisa.sad.playlistmanager.domain.model.Tag;
import it.unisa.sad.playlistmanager.domain.model.Track;
import it.unisa.sad.playlistmanager.ui.util.TrackFormValues;
import it.unisa.sad.playlistmanager.ui.util.TrackTagTextFormatter;
import it.unisa.sad.playlistmanager.ui.util.StyledAlertFactory;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.event.ActionEvent;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.beans.binding.Bindings;
import java.util.List;
import java.util.function.Consumer;

/**
 * Sotto-controllore delegato alla visualizzazione, manipolazione e immissione dati
 * concernenti il catalogo globale delle tracce e le canzoni interne a una specifica playlist.
 * <p><b>Revisione Sprint 2 (US-04):</b> Integra la funzionalità di eliminazione di una traccia
 * dal catalogo globale con annesso dialogo di conferma e sincronizzazione in tempo reale delle viste.</p>
 * <p>La modifica dei metadati avviene tramite dialogo dal menu contestuale, non inline nella tabella.</p>
 * @version 4.0
 */
public class TrackController {

    private static final String FEEDBACK_COLOR_ERROR = "red";
    private static final String FEEDBACK_COLOR_SUCCESS = "#1f7a1f";
    private static final String FEEDBACK_COLOR_SUCCESS_ALT = "green";
    private static final String FEEDBACK_COLOR_INFO = "#0066cc";
    private static final String FEEDBACK_COLOR_WARNING = "#b0413e";

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

    /** Colonna per la visualizzazione dei tag associati alla traccia. */
    @FXML private TableColumn<Track, String> colTags;
    
    /** Contenitore grafico del modulo di inserimento tracce. */
    @FXML private VBox formAddTrack;

    /** Riga superiore del catalogo con form traccia e gestione tag affiancati. */
    @FXML private HBox hboxCatalogForms;
    
    /** Menu a tendina per la scelta della playlist a cui associare il brano. */
    @FXML private ComboBox<Playlist> dropdownPlaylists;
    
    /** Contenitore dei comandi di aggiunta rapida a una playlist. */
    @FXML private HBox hboxAddtoPlaylist;
    
    /** Pulsante contestuale per escludere una traccia dalla playlist visualizzata. */
    @FXML private Button btnRemoveFromPlaylist;

    /** Pulsante contestuale per riprodurre tutto il catalogo. */
    @FXML private Button btnPlayCatalog;

    /** Pannello per la gestione dei tag nel catalogo. */
    @FXML private VBox formTagManagement;

    /** Campo per il nome di un nuovo tag. */
    @FXML private TextField txtNewTag;

    /** Elenco di tutti i tag disponibili nel sistema. */
    @FXML private ComboBox<Tag> dropdownAllTags;

    /** Flag discriminante per comprendere se la UI mostra il catalogo o una playlist. */
    private boolean playlistViewMode = false;
    
    /** Riferimento alla playlist attualmente visualizzata nella TableView. */
    private Playlist currentPlaylist;
    
    /** Lista interna contenente la copia speculare delle tracce della playlist attiva. */
    private java.util.List<Track> currentPlaylistTracks = new java.util.ArrayList<>();
    
    /** Routine di callback per notificare al coordinatore la creazione di una nuova playlist. */
    private Runnable onPlaylistCreatedHandler;

    /**
     * Costruttore per Constructor Injection (Task T-63).
     *
     * @param facade L'istanza centralizzata dell'Application Facade.
     */
    public TrackController(MusicPlaylistManagerFacade facade) {
        this.facade = facade;
    }

    /**
     * Inizializza i componenti grafici della TableView, effettua il data-binding
     * ed esegue il caricamento dei dati di catalogo a startup.
     */
    @FXML
    private void initialize() {
        // 1. CONFIGURAZIONE INIZIALE DEI COMPONENTI GRAFICI
        if (tableTracks != null) {
            // 2. CONFIGURAZIONE DELLA TABLEVIEW
            tableTracks.setEditable(false);
            tableTracks.setFixedCellSize(-1);
        }

        // 3. CONFIGURAZIONE DELLE COLONNE DELLA TABLEVIEW
        initializeTableColumns();

        // 4. CONFIGURAZIONE DELLE PLAYLIST
        configureDropdownPlaylistsRendering();
        configureTagComboRendering();
        if (tableTracks != null) {
            configureTableToggleDeselection();
            configureTableSelectionListener();
        }
        // 5. CARICAMENTO DEI DATI DI CATALOGO
        if (this.facade != null) {
            loadCatalog();
            refreshTagCombos();
            // 6. CONFIGURAZIONE DELLE PLAYLIST
            if (dropdownPlaylists != null) {
                //PATTERN OBSERVABLE ARRAY LIST, in questo caso viene passato l'elenco delle playlist alla dropdown
                dropdownPlaylists.setItems(FXCollections.observableArrayList(this.facade.getAllPlaylists()));
            }
        }
    }

    /**
     * Associa le colonne della TableView ai campi dati del Domain Model (Track).
     * La modifica dei metadati avviene solo tramite il menu contestuale.
     */
    private void initializeTableColumns() {
        if (colTitle != null) {
            colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
            colTitle.setEditable(false);
        }
        if (colAuthor != null) {
            colAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
            colAuthor.setEditable(false);
        }
        if (colDuration != null) {
            colDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
            colDuration.setEditable(false);
        }
        if (colGenre != null) {
            colGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
            colGenre.setEditable(false);
        }
        if (colYear != null) {
            colYear.setCellValueFactory(new PropertyValueFactory<>("year"));
            colYear.setEditable(false);
        }
        configureTagsColumn();
        configureResponsiveColumnWidths();
    }

    /**
     * Configura la colonna tag come testo semplice: un nome per riga.
     */
    private void configureTagsColumn() {
        if (colTags == null) {
            return;
        }

        // 7. CONFIGURAZIONE DELLA COLONNA TAG
        colTags.setEditable(false); //NON è editabile
        colTags.setCellValueFactory(cellData -> {
            Track track = cellData.getValue();
            if (track == null || facade == null) {
                return new ReadOnlyObjectWrapper<>("");
            }
            return new ReadOnlyObjectWrapper<>(TrackTagTextFormatter.formatForTable( //FORMATTA I TAG PER LA TABELLA
                    facade.getTagsForTrack(track.getId())));
        });
        colTags.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isBlank()) {
                    setText(null);
                    setGraphic(null);
                    setTooltip(null);
                    return;
                }
                // 8. CONFIGURAZIONE DELLA CELLA TAG
                setGraphic(null);
                setText(item); //SETTA IL TESTO DELLA CELLA
                setWrapText(false); //NON SI PUò INVIARE A CAPO
                setAlignment(Pos.CENTER_LEFT); //CENTRA IL TESTO A SINISTRA
                setTooltip(new Tooltip(item.replace("\n", ", "))); //TOOLTIP PER MOSTRARE I TAG SEPARATI DA VIRGOLA
            }
        });
    }

    /**
     * Apre il dialog dedicato per modificare metadati e tag della traccia selezionata.
     */
    private void showEditTrackDialog(Track track) {
        if (facade == null || track == null) {
            return;
        }
        // 9. APERTURA DEL DIALOGO DI MODIFICA DEI METADATI E DEI TAG
        TrackEditDialog dialog = new TrackEditDialog(
                facade,

                this::showErrorFeedback,
                this::refreshTagColumn,
                this::validateFormValues);
        //MOSTRA IL DIALOGO E SE IL VALORE DELLA TRACCIA È MODIFICATO, VENGONO AGGIORNATI I DATI DEL CATALOGO
        dialog.show(track).ifPresent(updatedTrack -> {
            selectedTrack = updatedTrack;
            showSuccessFeedback("Traccia modificata con successo.");
            loadCatalog();
            refreshTagCombos();
            if (playlistViewMode && currentPlaylist != null) {
                loadPlaylistTracks(currentPlaylist);
            }
            refreshTagColumn();
        });
    }

    /** Aggiorna il rendering della colonna tag dopo mutazioni sui tag. */
    private void refreshTagColumn() {
        if (tableTracks != null) {
            tableTracks.refresh();
        }
    }

    /** Valida i campi del form traccia prima dell'invocazione dei servizi. */
    private void validateFormValues(TrackFormValues values) {
        validateDataInput(
                values.title(),
                values.author(),
                values.genre(),
                values.duration(),
                values.year());
    }

    /**
     * Imposta il dimensionamento proporzionale e vincolato (Responsive) delle colonne.
     */
    private void configureResponsiveColumnWidths() {
        if (tableTracks == null || colTitle == null || colAuthor == null
                || colDuration == null || colGenre == null || colYear == null || colTags == null) {
            return;
        }
        //CONFIGURAZIONE DELLE COLONNE RESPONSIVE
        tableTracks.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        final double weightTitle = 0.23;
        final double weightAuthor = 0.23;
        final double weightDuration = 0.10;
        final double weightGenre = 0.14;
        final double weightYear = 0.08;
        final double weightTags = 0.22;
        double scrollbarOffset = 15.0;

        //BINDING DELLE COLONNE RESPONSIVE
        colTitle.prefWidthProperty().bind(tableTracks.widthProperty().subtract(scrollbarOffset).multiply(weightTitle));
        colAuthor.prefWidthProperty().bind(tableTracks.widthProperty().subtract(scrollbarOffset).multiply(weightAuthor));
        colDuration.prefWidthProperty().bind(tableTracks.widthProperty().subtract(scrollbarOffset).multiply(weightDuration));
        colGenre.prefWidthProperty().bind(tableTracks.widthProperty().subtract(scrollbarOffset).multiply(weightGenre));
        colYear.prefWidthProperty().bind(tableTracks.widthProperty().subtract(scrollbarOffset).multiply(weightYear));
        colTags.prefWidthProperty().bind(tableTracks.widthProperty().subtract(scrollbarOffset).multiply(weightTags));

        //SETTA LE MIN WIDTH DELLE COLONNE
        colTitle.setMinWidth(120);
        colAuthor.setMinWidth(110);
        colDuration.setMinWidth(70);
        colGenre.setMinWidth(90);
        colYear.setMinWidth(60);
        colTags.setMinWidth(120);
    }

    /**
     * Configura il rendering custom all'interno del menu a tendina delle playlist,
     * assicurando l'estrazione pulita della proprietà stringa del nome dell'oggetto.
     */
    private void configureDropdownPlaylistsRendering() {
        if (dropdownPlaylists == null) return;
        //CONFIGURAZIONE DELLE CELLE DELLA DROPDOWN PLAYLIST
        dropdownPlaylists.setCellFactory(lv -> new ListCell<>() {
            @Override
            //SETTA IL TESTO DELLA CELLA
            protected void updateItem(Playlist item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getName());
            }
        });
        //CONFIGURAZIONE DELLE CELLE DEL BOTTONE DELLA DROPDOWN PLAYLIST
        dropdownPlaylists.setButtonCell(new ListCell<>() {
            @Override
            //SETTA IL TESTO DEL BOTTONE
            protected void updateItem(Playlist item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? "Seleziona una playlist" : item.getName());
            }
        });
    }

    /**
     * Configura il rendering dei menu a tendina che mostrano oggetti {@link Tag}.
     */
    private void configureTagComboRendering() {
        //CONFIGURAZIONE DELLE CELLE DELLA DROPDOWN TAG
        configureTagCombo(dropdownAllTags, "Seleziona un tag");
    }

    private void configureTagCombo(ComboBox<Tag> comboBox, String emptyPrompt) {
        //CONFIGURAZIONE DELLE CELLE DELLA DROPDOWN TAG
        if (comboBox == null) {
            return;
        }
        comboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Tag item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getName());
            }
        });
        comboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Tag item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? emptyPrompt : item.getName());
            }
        });
    }

    /**
     * Aggiorna l'elenco dei tag globali nel pannello di gestione.
     */
    private void refreshTagCombos() {
        if (facade == null) {
            return;
        }

        List<Tag> allTags = facade.getAllTags();
        if (dropdownAllTags != null) {
            Tag selectedGlobalTag = dropdownAllTags.getSelectionModel().getSelectedItem();
            dropdownAllTags.setItems(FXCollections.observableArrayList(allTags));
            if (selectedGlobalTag != null) {
                allTags.stream()
                        .filter(tag -> tag.getId().equals(selectedGlobalTag.getId()))
                        .findFirst()
                        .ifPresentOrElse(
                                dropdownAllTags.getSelectionModel()::select,
                                () -> dropdownAllTags.getSelectionModel().clearSelection());
            }
        }
    }

    @FXML
    private void handleCreateTag(ActionEvent event) {
        if (facade == null || txtNewTag == null) {
            return;
        }

        try {
            String tagName = txtNewTag.getText() != null ? txtNewTag.getText().trim() : "";
            Tag createdTag = facade.addTag(tagName);
            txtNewTag.clear();
            refreshTagCombos();
            if (dropdownAllTags != null) {
                dropdownAllTags.getSelectionModel().select(createdTag);
            }
            if (tableTracks != null) {
                tableTracks.refresh();
            }
            showSuccessFeedback("Tag '" + createdTag.getName() + "' creato con successo.");
        } catch (RuntimeException e) {
            handleUIException(e, "Errore durante la creazione del tag.");
        }
    }

    @FXML
    private void handleDeleteTag(ActionEvent event) {
        if (facade == null || dropdownAllTags == null) {
            return;
        }

        Tag tagToDelete = dropdownAllTags.getSelectionModel().getSelectedItem();
        if (tagToDelete == null) {
            showErrorFeedback("Seleziona un tag da eliminare.");
            return;
        }

        Alert alert = StyledAlertFactory.create(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Conferma eliminazione");
        alert.setHeaderText("Eliminare il tag '" + tagToDelete.getName() + "'?");
        alert.setContentText("Il tag verrà rimosso da tutte le tracce associate.");

        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType != ButtonType.OK) {
                return;
            }
            try {
                facade.deleteTag(tagToDelete.getId());
                refreshTagCombos();
                if (tableTracks != null) {
                    tableTracks.refresh();
                }
                showSuccessFeedback("Tag '" + tagToDelete.getName() + "' eliminato.");
            } catch (RuntimeException e) {
                handleUIException(e, "Errore durante l'eliminazione del tag.");
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
                showEditTrackDialog(track);
            });

            // TASK T-89 e T-90: Finestra di dialogo di conferma ed eliminazione traccia
            deleteItem.setOnAction(event -> {
                Track track = row.getItem();
                if (track == null) return;

                // T-89: Configurazione e apertura del dialogo di conferma nativo
                Alert alert = StyledAlertFactory.create(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Conferma Eliminazione");
                alert.setHeaderText("Eliminare la traccia dal catalogo?");
                alert.setContentText("L'operazione rimuoverà definitivamente la traccia '" + track.getTitle() + "' anche da tutte le playlist.");

                java.util.Optional<ButtonType> confirmationResult = alert.showAndWait();
                if (confirmationResult.isPresent() && confirmationResult.get() == ButtonType.OK) {
                    try {
                        facade.deleteTrack(track.getId());
                        showSuccessFeedbackAlt("Traccia eliminata con successo.");

                        loadCatalog();
                        if (playlistViewMode && currentPlaylist != null) {
                            loadPlaylistTracks(currentPlaylist);
                        }
                        tableTracks.getSelectionModel().clearSelection();
                        selectedTrack = null;
                    } catch (Exception e) {
                        handleUIException(e, "Errore durante l'eliminazione della traccia.");
                    }
                } else {
                    showInfoFeedback("Eliminazione annullata.");
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
            refreshTagCombos();
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

    /**
     * Registra il gestore eventi da lanciare non appena viene creata una playlist (manuale o automatica).
     *
     * @param handler Routine di callback esposta dal coordinatore principale.
     */
    public void setOnPlaylistCreated(Runnable handler) {
        this.onPlaylistCreatedHandler = handler;
    }

    @FXML
    private void handlePlayCatalog(ActionEvent event) {
        if (facade == null) {
            showErrorFeedback("Errore: Facade non inizializzata.");
            return;
        }
        try {
            facade.playCatalog();
            showSuccessFeedbackAlt("Riproduzione del catalogo globale avviata.");
        } catch (Exception e) {
            handleUIException(e, "Errore durante l'avvio del catalogo.");
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
            showErrorFeedback("Errore interno: facade non inizializzata.");
            return;
        }
        try {
            TrackFormValues values = TrackFormValues.fromRaw(
                    txtTitle.getText(),
                    txtAuthor.getText(),
                    txtDuration.getText(),
                    txtGenre.getText(),
                    txtYear.getText());
            validateFormValues(values);

            Track newTrack = facade.addTrack(
                    values.title(),
                    values.author(),
                    values.duration(),
                    values.genre(),
                    values.year());
            if (newTrack != null) {
                tableTracks.getSelectionModel().clearSelection();
                clearForm();
                showSuccessFeedbackAlt("Traccia aggiunta con successo.");
                loadCatalog();
            }
        } catch (Exception e) {
            handleUIException(e, "Errore durante il salvataggio della traccia.");
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
            } catch (Exception e) {
                handleUIException(e, "Errore durante l'aggiunta alla playlist.");
                return;
            }

            showSuccessFeedbackAlt("Traccia '" + selectedTrack.getTitle()
                    + "' aggiunta alla playlist '" + targetPlaylist.getName() + "' con successo.");
            if (playlistViewMode && currentPlaylist != null && currentPlaylist.getId().equals(targetPlaylist.getId())) {
                loadPlaylistTracks(currentPlaylist);
            }
            dropdownPlaylists.setItems(FXCollections.observableArrayList(facade.getAllPlaylists()));
            if (hboxAddtoPlaylist != null) {
                hboxAddtoPlaylist.setVisible(false);
                hboxAddtoPlaylist.setManaged(false);
            }
        } else {
            showErrorFeedback("Seleziona una playlist valida dal menu a tendina.");
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
            showErrorFeedback("Seleziona una traccia della playlist da rimuovere.");
            return;
        }

        try {
            facade.removeTrackFromPlaylist(currentPlaylist.getId(), selectedTrack.getId());
        } catch (Exception e) {
            handleUIException(e, "Errore durante la rimozione dalla playlist.");
            return;
        }
        loadPlaylistTracks(currentPlaylist);
        if (btnRemoveFromPlaylist != null) {
            btnRemoveFromPlaylist.setVisible(false);
            btnRemoveFromPlaylist.setManaged(false);
        }

        if (lblFeedback != null) {
            if (currentPlaylistTracks.isEmpty()) {
                showSuccessFeedback("Playlist '" + currentPlaylist.getName() + "' vuota.");
            } else {
                showSuccessFeedback("Traccia rimossa da '" + currentPlaylist.getName() + "'.");
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
        tableTracks.refresh();
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
        if (hboxCatalogForms != null) {
            hboxCatalogForms.setVisible(true);
            hboxCatalogForms.setManaged(true);
        }
        if (btnRemoveFromPlaylist != null) {
            btnRemoveFromPlaylist.setVisible(false);
            btnRemoveFromPlaylist.setManaged(false);
        }
        loadCatalog();
        refreshTagCombos();
        if (tableTracks != null) tableTracks.refresh();
        if (lblFeedback != null) {
            if (tableTracks != null && tableTracks.getItems() != null && tableTracks.getItems().isEmpty()) {
                labelFeedback("Catalogo vuoto. Aggiungi una traccia.", FEEDBACK_COLOR_WARNING);
            } else {
                showSuccessFeedback("Visualizzazione catalogo completo.");
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
        if (hboxCatalogForms != null) {
            hboxCatalogForms.setVisible(false);
            hboxCatalogForms.setManaged(false);
        }
        showSuccessFeedback("Nessuna playlist selezionata.");
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
        if (hboxCatalogForms != null) {
            hboxCatalogForms.setVisible(false);
            hboxCatalogForms.setManaged(false);
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
        labelFeedback("Contenuto playlist: " + playlist.getName(), FEEDBACK_COLOR_INFO);
    }

    /**
     * Esegue il recupero sincrono dei brani di una playlist aggiornando gli elementi della TableView.
     * * @param playlist L'istanza di Playlist da aggiornare a livello grafico.
     */
    public void loadPlaylistTracks(Playlist playlist) {
        if (tableTracks == null || playlist == null || facade == null) return;
        try {
            currentPlaylistTracks = facade.getTracksForPlaylist(playlist.getId());
        } catch (Exception e) {
            handleUIException(e, "Errore durante il caricamento della playlist.");
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
     * Modifica lo stile cromatico e il testo dell'etichetta informativa inferiore.
     *
     * @param text  Il testo descrittivo da stampare.
     * @param color La specifica CSS per la colorazione del font.
     */
    public void labelFeedback(String text, String color) {
        if (lblFeedback != null) {
            lblFeedback.setStyle("-fx-text-fill: " + color + ";");
            lblFeedback.setText(text);
        }
    }

    /** Mostra un messaggio di errore con lo stile standardizzato. */
    private void showErrorFeedback(String message) {
        labelFeedback(message, FEEDBACK_COLOR_ERROR);
    }

    /** Mostra un messaggio di successo con lo stile primario. */
    private void showSuccessFeedback(String message) {
        labelFeedback(message, FEEDBACK_COLOR_SUCCESS);
    }

    /** Mostra un messaggio di successo con lo stile alternativo (verde pieno). */
    private void showSuccessFeedbackAlt(String message) {
        labelFeedback(message, FEEDBACK_COLOR_SUCCESS_ALT);
    }

    /** Mostra un messaggio informativo con lo stile standardizzato. */
    private void showInfoFeedback(String message) {
        labelFeedback(message, FEEDBACK_COLOR_INFO);
    }

    /**
     * Centralizza la gestione delle eccezioni UI estraendo il messaggio appropriato.
     */
    private void handleUIException(Exception exception, String defaultMessage) {
        if (exception instanceof NumberFormatException) {
            showErrorFeedback("Durata e anno devono essere numeri validi.");
            return;
        }
        if (exception instanceof ValidationException validationException) {
            showErrorFeedback(validationException.getMessage());
            return;
        }
        if (exception instanceof it.unisa.sad.playlistmanager.domain.exceptions.ValidationException validationException) {
            showErrorFeedback(validationException.getMessage());
            return;
        }
        if (exception instanceof TrackNotFoundException trackNotFoundException) {
            showErrorFeedback(trackNotFoundException.getMessage());
            return;
        }
        if (exception instanceof TagNotFoundException tagNotFoundException) {
            showErrorFeedback(tagNotFoundException.getMessage());
            return;
        }
        if (exception instanceof PlaylistNotFoundException playlistNotFoundException) {
            showErrorFeedback(playlistNotFoundException.getMessage());
            return;
        }
        if (exception instanceof IllegalArgumentException illegalArgumentException) {
            showErrorFeedback(illegalArgumentException.getMessage());
            return;
        }
        if (defaultMessage != null && !defaultMessage.isBlank() && exception.getMessage() != null) {
            showErrorFeedback(defaultMessage + ": " + exception.getMessage());
            return;
        }
        showErrorFeedback(defaultMessage != null && !defaultMessage.isBlank()
                ? defaultMessage
                : "Si è verificato un errore imprevisto.");
    }

    /**
     * Rinfresca in tempo reale lo stato dei dati visibili (Catalogo o Playlist)
     * per riallineare la Tabella a seguito di operazioni mutative o comandi di Undo.
     */
    public void refresh() {
        if (playlistViewMode && currentPlaylist != null) {
            loadPlaylistTracks(currentPlaylist);
        } else {
            loadCatalog();
        }
        // Sincronizza anche il dropdown di aggiunta rapida alla playlist
        if (facade != null && dropdownPlaylists != null) {
            dropdownPlaylists.setItems(FXCollections.observableArrayList(facade.getAllPlaylists()));
        }
    }

    @FXML
    private void handleCreateAutoPlaylist(ActionEvent event) {
        if (facade == null) {
            showErrorFeedback("Errore interno: facade non inizializzata.");
            return;
        }

        new AutoPlaylistDialog(facade).show();
        
        refresh();
        
        if (onPlaylistCreatedHandler != null) {
            onPlaylistCreatedHandler.run();
        }
    }
}