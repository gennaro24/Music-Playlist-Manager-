# Product Backlog — Music Playlist Manager
**Progetto:** Software Architecture Design — A.A. 2025/2026  
**Formato stime:** Story Points (scala Fibonacci: 1, 2, 3, 5, 8, 13)  
**Formato AC:** GIVEN / WHEN / THEN

---

## Legenda colori 
| Colore | Tipo |
|---|---|
| 🟢 Verde | LOW PRIORITY  |
| 🟡 Giallo | MEDIUM PRIORITY  |
| 🔴 Rosso | HIGH PRIORITY |

---


### 🔴US-01 — Aggiungere una nuova traccia al catalogo

**Descrizione:**

Come utente, voglio aggiungere una nuova traccia inserendo titolo, autore, durata, genere, anno di pubblicazione e tag?, affinché io possa costruire un catalogo musicale completo e riutilizzabile nelle playlist.

**Acceptance criteria:**

**Scenario 1 - Aggiunta valida**  
**Given** il catalogo musicale è aperto  
**When** inserisco titolo, autore, durata, genere, anno ed eventuale tag validi  
**Then** la traccia viene aggiunta al catalogo  
**And** viene visualizzata nell’elenco con tutti i metadati inseriti  

**Scenario 2 - Campi obbligatori mancanti**  
**Given** il form di inserimento traccia è aperto  
**When** lascio vuoto titolo o autore  
**Then** il sistema mostra un errore di validazione  
**And** la traccia non viene salvata  

**Scenario 3 - Valori numerici non validi**  
**Given** il form di inserimento traccia è aperto  
**When** inserisco durata minore o uguale a zero oppure anno non valido  
**Then** il sistema mostra un errore  
**And** il catalogo rimane invariato  

---

### 🔴US-02 — Visualizzare tutte le tracce del catalogo

**Descrizione:**

Come utente, voglio visualizzare tutte le tracce presenti nel catalogo con i rispettivi dati principali, affinché io possa consultare rapidamente la mia libreria musicale prima di creare o modificare playlist.

**Acceptance criteria:**

**Scenario 1 - Catalogo con tracce presenti**  
**Given** nel catalogo sono presenti una o più tracce  
**When** apro la schermata del catalogo  
**Then** vedo l’elenco delle tracce  
**And** per ogni traccia vedo titolo, autore, durata, genere e anno  

**Scenario 2 - Catalogo vuoto**  
**Given** il catalogo non contiene tracce  
**When** apro la schermata del catalogo  
**Then** il sistema mostra un messaggio di catalogo vuoto  
**And** non mostra dati inconsistenti  

**Scenario 3 - Aggiornamento dopo inserimento**  
**Given** sto visualizzando il catalogo  
**When** aggiungo una nuova traccia valida  
**Then** la traccia compare nell’elenco senza dover riavviare l’applicazione  

---

### 🔴US-03 — Modificare gli attributi di una traccia

**Descrizione:**

Come utente, voglio modificare gli attributi di una traccia già presente nel catalogo, affinché io possa correggere errori o aggiornare informazioni musicali senza dover eliminare e reinserire la traccia.

**Acceptance criteria:**

**Scenario 1 - Modifica valida**  
**Given** una traccia esiste nel catalogo  
**When** modifico uno o più dati con valori validi  
**Then** il sistema aggiorna la traccia  
**And** mostra i nuovi valori nel catalogo  

**Scenario 2 - Modifica non valida**  
**Given** una traccia esiste nel catalogo  
**When** inserisco valori non validi, come titolo vuoto o durata non positiva  
**Then** il sistema mostra un errore  
**And** conserva i valori precedenti della traccia  

**Scenario 3 - Coerenza con playlist esistenti**  
**Given** una traccia è già contenuta in una playlist  
**When** modifico i suoi metadati dal catalogo  
**Then** la playlist mostra la traccia aggiornata  
**And** non crea duplicati della stessa traccia  

---

### 🔴US-04 — Eliminare una traccia dal catalogo

**Descrizione:**

Come utente, voglio eliminare una traccia dal catalogo musicale, affinché io possa rimuovere contenuti non più desiderati e mantenere ordinata la mia libreria.

**Acceptance criteria:**

**Scenario 1 - Eliminazione confermata**  
**Given** una traccia esiste nel catalogo  
**When** scelgo di eliminarla e confermo l’operazione  
**Then** la traccia viene rimossa dal catalogo  

**Scenario 2 - Eliminazione annullata**  
**Given** una traccia esiste nel catalogo  
**When** scelgo di eliminarla ma annullo la conferma  
**Then** la traccia resta nel catalogo  

**Scenario 3 - Rimozione dalle playlist**  
**Given** la traccia eliminata è presente in una o più playlist  
**When** confermo l’eliminazione dal catalogo  
**Then** la traccia viene rimossa anche dalle playlist che la contenevano  
**And** il sistema non lascia riferimenti invalidi  

---

### 🔴US-05 — Creare una nuova playlist

**Descrizione:**

Come utente, voglio creare una nuova playlist identificata da un nome, affinché io possa organizzare le tracce del catalogo in raccolte musicali personalizzate.

**Acceptance criteria:**

**Scenario 1 - Creazione valida**  
**Given** sono nella sezione playlist  
**When** inserisco un nome valido per una nuova playlist  
**Then** il sistema crea la playlist  
**And** la playlist compare nell’elenco delle playlist  

**Scenario 2 - Nome mancante**  
**Given** sono nel form di creazione playlist  
**When** provo a creare una playlist senza nome  
**Then** il sistema mostra un errore  
**And** non crea alcuna playlist  

**Scenario 3 - Nome duplicato**  
**Given** esiste già una playlist con lo stesso nome  
**When** provo a creare una nuova playlist con quel nome  
**Then** il sistema impedisce la duplicazione  
**And** mostra un messaggio di errore comprensibile  

---

### 🔴US-06 — Visualizzare il contenuto di una playlist

**Descrizione:**

Come utente, voglio visualizzare il contenuto di una playlist con l’elenco ordinato delle tracce, affinché io possa controllare la sequenza musicale prima di avviare il playback.

**Acceptance criteria:**

**Scenario 1 - Playlist con tracce**  
**Given** una playlist contiene una o più tracce  
**When** apro il dettaglio della playlist  
**Then** vedo tutte le tracce contenute  
**And** per ogni traccia vedo almeno titolo, autore e durata  

**Scenario 2 - Playlist vuota**  
**Given** una playlist non contiene tracce  
**When** apro il dettaglio della playlist  
**Then** il sistema mostra un messaggio di playlist vuota  

**Scenario 3 - Ordine delle tracce**  
**Given** una playlist contiene più tracce  
**When** visualizzo la playlist  
**Then** le tracce sono mostrate nell’ordine in cui sono state aggiunte  

---

### 🔴US-07 — Aggiungere tracce esistenti a una playlist

**Descrizione:**

Come utente, voglio aggiungere tracce esistenti dal catalogo a una playlist anche in esecuzione in quel momento, affinché io possa costruire una raccolta musicale ordinata secondo le mie preferenze.

**Acceptance criteria:**

**Scenario 1 - Aggiunta valida**  
**Given** esiste almeno una traccia nel catalogo  
**And** esiste una playlist  
**When** seleziono una traccia e la aggiungo alla playlist  
**Then** la traccia compare nella playlist selezionata  

**Scenario 2 - Traccia già presente**  
**Given** una traccia è già presente nella playlist  
**When** provo ad aggiungerla di nuovo  
**Then** il sistema impedisce il duplicato oppure segnala che la traccia è già presente  

**Scenario 3 - Playlist aggiornata**  
**Given** sto visualizzando il dettaglio della playlist  
**When** aggiungo una traccia valida  
**Then** l’elenco della playlist viene aggiornato correttamente  

---

### 🔴US-08 — Rimuovere una traccia da una playlist

**Descrizione:**

Come utente, voglio rimuovere una traccia da una playlist senza eliminarla dal catalogo anche durante la riproduzione, affinché io possa modificare il contenuto della playlist mantenendo disponibile la traccia per altri usi.

**Acceptance criteria:**

**Scenario 1 - Rimozione valida**  
**Given** una playlist contiene almeno una traccia  
**When** seleziono una traccia e la rimuovo dalla playlist  
**Then** la traccia non compare più nella playlist  

**Scenario 2 - Traccia mantenuta nel catalogo**  
**Given** rimuovo una traccia da una playlist  
**When** apro il catalogo musicale  
**Then** la traccia è ancora presente nel catalogo  

**Scenario 3 - Playlist vuota**  
**Given** rimuovo l’ultima traccia da una playlist  
**When** visualizzo la playlist  
**Then** il sistema mostra la playlist vuota senza errori  

**Scenario 4 - Rimozione durante playback**  
**Given** una playlist è in riproduzione  
**When** rimuovo una traccia dalla playlist  
**Then** il sistema aggiorna la playlist  
**And** il playback resta in uno stato coerente  

---

### 🔴US-09 — Riprendere il playback simulato di una singola traccia

**Descrizione:**

Come utente, voglio mettere in riproduzione il playback simulato di un singola traccia, affinché possa avviare l’ascolto senza perdere la traccia corrente.

**Acceptance criteria:**

**Scenario 1 - Ripresa del playback**  
**Given** una traccia è in stato Paused  
**When** premo Resume  
**Then** lo stato torna Playing  
**And** la traccia corrente resta invariata  

**Scenario 2 - Resume quando il player non è in Paused**  
**Given** il playback è in stato Stopped  
**When** premo Resume  
**Then** lo stato resta Stopped  
**And** non viene generato alcun errore  

---

### 🔴US-10 — Mettere in pausa una singola traccia

**Descrizione:**

Come utente del lettore musicale, voglio poter mettere in pausa la traccia attualmente in riproduzione, affinché io possa interrompere momentaneamente l'ascolto simulato senza perdere il minutaggio corrente del brano.

**Acceptance criteria:**

**Scenario 1 - Richiesta di Pausa su traccia in riproduzione**  
**Given** una traccia è in esecuzione attiva e il lettore si trova nello stato "Playing"  
**When** premo il pulsante "Pause"  
**Then** il sistema modifica lo stato interno del playback in "Paused"  
**And** la traccia corrente resta invariata nella UI  
**And** il timer di avanzamento della simulazione si blocca sul secondo esatto dell'interruzione.  

**Scenario 2 - Richiesta di Pausa su traccia già in pausa (Caso Limite)**  
**Given** il playback si trova già nello stato "Paused"  
**When** premo nuovamente il pulsante "Pause"  
**Then** il sistema ignora il comando e non effettua alcuna transizione di stato  
**And** non genera errori o comportamenti anomali nel flusso simulato  

---

### 🔴US-11 — Attivare il loop su singola traccia

**Descrizione:**

Come utente del lettore musicale, voglio attivare la modalità di loop continuo su una singola traccia isolata, affinché lo stesso brano venga ripetuto dall'inizio in modo automatico e infinito, fino alla disattivazione della funzionalità.

**Acceptance criteria:**

**Scenario 1 - Ripetizione continua della singola traccia**  
**Given** una traccia è in esecuzione dal catalogo o da una playlist  
**And** la modalità di riproduzione attiva è impostata su "Loop Traccia Singola"  
**When** la simulazione del playback raggiunge la fine della riproduzione della traccia (es. 354 secondi)  
**Then** il sistema mantiene la stessa identica traccia come traccia corrente senza passare ad altri brani  
**And** resetta il timer a 00:00 continuando il playback simulato in stato "Playing".  

**Scenario 2 - Disattivazione del Loop Traccia Singola**  
**Given** una traccia sta riproducendo in modalità "Loop Traccia Singola"  
**When** l'utente disattiva il loop cambiando la modalità in "Sequential"  
**Then** la traccia corrente finisce di riprodursi normalmente  
**And** al suo termine il sistema applicherà la normale coda sequenziale (passando al brano successivo della playlist o fermandosi in stato "Stopped").  

---

### 🔴US-12 — Avviare il playback sequenziale di una playlist

**Descrizione:**

Come utente, voglio avviare il playback simulato di una playlist in modalità sequenziale, affinché io possa simulare l’ascolto dei brani nell’ordine previsto.

**Acceptance criteria:**

**Scenario 1 - Play sequenziale di una playlist**  
**Given** una playlist contiene più tracce  
**When** avvio il playback sequenziale  
**Then** il sistema riproduce la prima traccia della playlist  
**And** mantiene l’ordine originale delle tracce  

**Scenario 2 - Tentativo di Play su una Playlist vuota**  
**Given** una playlist appena creata non contiene alcuna traccia è in playback sequenziale  
**When** tento di avviare il playback sequenziale su di essa  
**Then** il sistema impedisce l'avvio della simulazione  

---

### 🔴US-13 — Mettere in pausa una playlist

**Descrizione:**

Come utente del lettore musicale, voglio poter mettere in pausa la playlist attualmente in riproduzione, affinché io possa interrompere momentaneamente l'ascolto della sequenza di brani senza perdere la traccia corrente e la mia posizione all'interno della coda.

**Acceptance criteria:**

**Scenario 1 - Richiesta di Pausa su una playlist in riproduzione**  
**Given** il lettore si trova nello stato "Playing" e sta riproducendo una playlist (es. al brano #3 di 10)  
**When** premo il pulsante "Pause"  
**Then** il sistema modifica lo stato del lettore in "Paused"  
**And** mantiene in memoria l'indice della traccia attiva all'interno della playlist.  

---

### 🔴US-14 — Saltare alla traccia successiva durante il playback

**Descrizione:**

Come utente, voglio saltare alla traccia successiva durante il playback simulato, affinché possa passare rapidamente al brano seguente della playlist corrente.

**Acceptance criteria:**

**Scenario 1 - Skip con traccia successiva disponibile**  
**Given** una playlist è in stato Playing  
**And** esiste una traccia successiva  
**When** premo Skip  
**Then** la traccia corrente diventa la traccia successiva  
**And** lo stato resta Playing  

**Scenario 2 - Skip dall’ultima traccia**  
**Given** sto riproducendo l’ultima traccia della playlist in modalità sequenziale  
**When** premo Skip  
**Then** lo stato diventa Stopped  
**And** non viene generato alcun errore  

**Scenario 3 - Skip quando il player è in Paused**  
**Given** una playlist è in stato Paused  
**And** esiste una traccia successiva  
**When** premo Skip  
**Then** la traccia corrente diventa la traccia successiva  
**And** lo stato resta Paused  

---

### 🔴US-15 — Riprodurre una playlist in modalità casuale

**Descrizione:**

Come utente, voglio riprodurre una playlist in modalità casuale, affinché io possa ascoltare una sequenza di tracce in ordine non deterministico rispetto all’ordine della playlist o del catalogo.

**Acceptance criteria:**

**Scenario 1 - Avvio shuffle**  
**Given** una playlist contiene almeno due tracce  
**When** seleziono la modalità Shuffle e avvio il playback  
**Then** il sistema seleziona le tracce in un ordine non necessariamente uguale all’ordine della playlist  

**Scenario 2 - Playlist con una sola traccia**  
**Given** una playlist contiene una sola traccia  
**When** avvio il playback Shuffle  
**Then** il sistema riproduce quella traccia senza errori  

---

### 🔴US-16 — Attivare il loop su playlist

**Descrizione:**

Come utente del lettore musicale, voglio attivare la modalità di loop continuo su una playlist in riproduzione, affinché la playlist ricominci automaticamente dalla prima canzone subito dopo il termine dell'ultima, in un ciclo infinito.

**Acceptance criteria:**

**Scenario 1 - Loop su playlist**  
**Given** una playlist contiene più tracce  
**And** il playback è in modalità Loop  
**When** termina l’ultima traccia oppure viene eseguito skip dall’ultima traccia  
**Then** il sistema torna alla prima traccia della playlist  
**And** mantiene lo stato Playing  

**Scenario 2 - Cambio modalità con playlist attiva**  
**Given** il playback di una playlist è attivo in modalità "Loop Playlist"  
**When** cambio la modalità di riproduzione in "Sequential" o "Shuffle"  
**Then** il sistema applica la nuova strategia algoritmica solo ai comandi e agli eventi successivi  
**And** non interrompe né resetta il brano simulato attualmente in esecuzione.  

---

### 🔴US-17 — Visualizzare traccia corrente e stato del playback

**Descrizione:**

Come utente, voglio vedere nella UI la traccia corrente e lo stato del playback, affinché possa capire cosa sta riproducendo il Media Player in ogni momento.

**Acceptance criteria:**

**Scenario 1 - Stato Playing**  
**Given** avvio il playback di una traccia o playlist  
**When** il playback entra in stato Playing  
**Then** la UI mostra la traccia corrente  
**And** mostra lo stato Playing  

**Scenario 2 - Stato Paused**  
**Given** il playback è in stato Playing  
**When** premo Pause  
**Then** la UI mostra lo stato Paused  
**And** mantiene visibile la traccia corrente  

**Scenario 3 - Stato Stopped**  
**Given** il playback è attivo  
**When** il playback termina oppure premo Stop  
**Then** la UI mostra lo stato Stopped  
**And** non mostra una traccia in riproduzione oppure mantiene l’ultima traccia secondo la decisione di design  

---

### 🔴US-18 — Visualizzare l’elenco delle playlist create

**Descrizione:**

Come utente del lettore musicale, voglio visualizzare nel Media Player l'elenco completo di tutte le playlist che ho creato, affinché io possa scorrere le mie raccolte e selezionare rapidamente quale playlist riprodurre o modificare.

**Acceptance criteria:**

**Scenario 1 - Visualizzazione con più playlist presenti**  
**Given** l'utente ha creato precedentemente 3 playlist nel sistema (es. "Rock", "Pop", "Jazz")  
**When** accedo alla schermata principale del Media Player  
**Then** il sistema mostra l'elenco esatto di tutte e 3 le playlist con i rispettivi nomi aggiornati  
**And** la lista visiva si adegua dinamicamente senza richiedere il riavvio dell'applicazione  

**Scenario 2 - Visualizzazione iniziale senza playlist**  
**Given** un utente ha appena installato o avviato l'applicazione per la prima volta e non ha ancora creato alcuna playlist  
**When** visualizzo l’elenco delle playlist  
**Then** il sistema mostra l'elenco delle playlist vuoto o un testo segnaposto (es. "Nessuna playlist creata")  
**And** non genera errori grafici  

---

### 🟡US-19 — Annullare l’inserimento di una nuova canzone nel catalogo

**Descrizione:**

Come utente del lettore musicale, voglio poter annullare l'inserimento di una nuova canzone nel catalogo globale, affinché la traccia creata per errore venga rimossa istantaneamente dal sistema e da qualsiasi playlist in cui sia stata eventualmente inserita nel frattempo.

**Acceptance criteria:**

**Scenario 1 - Undo di una canzone creata globalmente**  
**Given** ho appena creato globalmente la traccia "Song Error" con i relativi metadati  
**And** l'ho aggiunta alla playlist "Preferiti"  
**When** eseguo il comando "Undo"  
**Then** la canzone "Song Error" viene interamente cancellata dal catalogo globale  
**And** viene rimossa automaticamente e in tempo reale dalla playlist "Preferiti" senza lasciare record.  

---

### 🟡US-20 — Annullare l’eliminazione globale di una canzone

**Descrizione:**

Come utente del lettore musicale, voglio poter annullare l'eliminazione globale di una canzone dal catalogo, affinché la traccia venga ripristinata nel catalogo generale e reinserita automaticamente all'interno di tutte le playlist in cui era presente prima della cancellazione, mantenendo la sua posizione originaria.

**Acceptance criteria:**

**Scenario 1 - Undo di una rimozione globale con traccia presente in più playlist**  
**Given** la canzone "Bohemian Rhapsody" è presente nel catalogo globale  
**And** è inserita nella playlist "Rock Classics" (in posizione #1) e nella playlist "Best of Queen" (in posizione #4)  
**And** ho eseguito la cancellazione globale della traccia (che l'ha rimossa dal catalogo e da tutte le liste)  
**When** eseguo il comando "Undo"  
**Then** "Bohemian Rhapsody" riappare nel catalogo globale con tutti i suoi metadati intatti  
**And** viene reinserita automaticamente nella playlist "Rock Classics" esattamente in posizione #1  
**And** viene reinserita automaticamente nella playlist "Best of Queen" esattamente in posizione #4  
**And** l'interfaccia multimediale aggiorna istantaneamente tutte le tabelle e le liste visive.  

---

### 🟡US-21 — Annullare la creazione di una nuova playlist

**Descrizione:**

Come utente del lettore musicale, voglio poter annullare la creazione di una nuova playlist, affinché la playlist vuota generata per errore venga eliminata dal sistema.

**Acceptance criteria:**

**Scenario 1 - Undo di una nuova playlist creata**  
**Given** ho appena creato una nuova playlist  
**When** eseguo il comando "Undo"  
**Then** la playlist viene interamente eliminata dal sistema  
**And** scompare dalla lista delle playlist visibili nella UI.  

---

### 🟡US-22 — Annullare la rimozione di una playlist

**Descrizione:**

Come utente del lettore musicale, voglio poter annullare la rimozione di una playlist precedentemente eliminata, affinchè io possa recuperare l'intera raccolta e tutte le canzoni in essa contenute.

**Acceptance criteria:**

**Scenario 1 - Undo di una rimozione di una playlist popolata**  
**Given** ho rimosso una playlist funzionante che conteneva delle canzoni  
**When** eseguo il comando "Undo"  
**Then** l'intera playlist viene ripristinata nel sistema con lo stesso nome  
**And** mantiene intatta al suo interno la lista originaria delle canzoni  
**And** riappare immediatamente nell'interfaccia utente.  

---

### 🟡US-23 — Annullare l’aggiunta di una traccia a una playlist

**Descrizione:**

Come utente del lettore musicale, voglio poter annullare l'aggiunta di una traccia a una playlist, affinché io possa rimuoverla immediatamente in caso di inserimento errato.

**Acceptance criteria:**

**Scenario 1 - Undo di un'aggiunta avvenuta con successo**  
**Given** ho appena aggiunto la traccia "Billie Jean" alla playlist "Pop"  
**When** eseguo il comando "Undo"  
**Then** la traccia viene rimossa dalla playlist  
**And** la schermata della playlist si aggiornano in tempo real-time  

**Scenario 2 - Tentativo di Undo senza storico**  
**Given** lo stack dei comandi di aggiunta tracce è vuoto  
**When** premo il pulsante "Undo"  
**Then** il sistema non modifica lo stato dei dati  
**And** il pulsante visivo nella UI viene disabilitato per prevenire input non validi.  

---

### 🟡US-24 — Annullare la rimozione di una traccia da una playlist

**Descrizione:**

Come utente del lettore musicale, voglio poter annullare la rimozione di una traccia da una playlist, affinché il brano venga reinserito nella raccolta senza perdere l'ordine originale.

**Acceptance criteria:**

**Scenario 1 - Undo di una rimozione avvenuta con successo**  
**Given** la playlist contiene tre tracce in ordine e ne rimuovo una posizionata al centro (indice #1)  
**When** eseguo il comando "Undo"  
**Then** la traccia viene reinserita nella playlist esattamente nella posizione corretta originaria (indice #1)  

---

### 🟡US-25 — Visualizzare le tracce più frequentemente riprodotte

**Descrizione:**

Come utente, voglio vedere nella home page le tracce più frequentemente riprodotte, affinché io possa accedere rapidamente ai contenuti che ascolto di più.

**Acceptance criteria:**

**Scenario 1 - Conteggio riproduzioni tracce**  
**Given** riproduco una traccia più volte  
**When** apro la home page  
**Then** la traccia compare tra quelle più riprodotte  

**Scenario 3 - Nessuna riproduzione**  
**Given** non ho ancora riprodotto tracce  
**When** apro la home page  
**Then** nessuna traccia compare tra quelle più riprodotte  

---

### 🟡US-26 — Visualizzare le playlist più frequentemente riprodotte

**Descrizione:**

Come utente, voglio vedere nella home page le playlist più frequentemente riprodotte, affinché io possa accedere rapidamente ai contenuti che ascolto di più.

**Acceptance criteria:**

**Scenario 1 - Conteggio riproduzioni playlist**  
**Given** riproduco una playlist più volte  
**When** apro la home page  
**Then** la playlist compare tra quelle più riprodotte  

**Scenario 2 - Nessuna riproduzione**  
**Given** non ho ancora riprodotto playlist  
**When** apro la home page  
**Then** il sistema mostra una home vuota o un messaggio coerente  

---

### 🟡US-27 — Aggiungere tag visuali alle tracce

**Descrizione:**

Come utente, voglio aggiungere tag visuali alle tracce, come favourite, explicit o new release, affinché le tracce siano visualizzate in modo più informativo e possano essere usate per creare playlist automatiche.

**Acceptance criteria:**

**Scenario 1 - Aggiunta tag**  
**Given** una traccia esiste nel catalogo  
**When** assegno un tag visuale alla traccia  
**Then** il tag viene associato alla traccia  
**And** viene mostrato nella UI  

**Scenario 2 - Rimozione tag**  
**Given** una traccia ha un tag visuale associato  
**When** rimuovo il tag  
**Then** il tag non viene più mostrato sulla traccia  

**Scenario 3 - Tag multipli**  
**Given** una traccia esiste nel catalogo  
**When** assegno più tag compatibili  
**Then** la UI mostra tutti i tag associati  

---

### 🟡US-28 — Creare playlist automatiche

**Descrizione:**

Come utente, voglio poter creare automaticamente playlist basate su genere, anno e tag, affinché io possa organizzare rapidamente il catalogo senza selezionare manualmente ogni traccia.

**Acceptance criteria:**

**Scenario 1 - Playlist automatica per genere**  
**Given** il catalogo contiene tracce di generi diversi  
**When** scelgo di creare una playlist per genere  
**Then** il sistema crea una playlist contenente solo le tracce del genere selezionato  

**Scenario 2 - Playlist automatica per anno**  
**Given** il catalogo contiene tracce con anni diversi  
**When** scelgo di creare una playlist per anno  
**Then** il sistema crea una playlist contenente solo le tracce dell’anno selezionato  

**Scenario 3 - Nessuna traccia corrispondente**  
**Given** nessuna traccia soddisfa il criterio scelto  
**When** provo a creare la playlist automatica  
**Then** il sistema non crea una playlist vuota non richiesta  
**And** mostra un messaggio di nessun risultato  

---

### 🟡US-29 — Aggiornare la UI della playlist corrente durante il playback

**Descrizione:**

Come utente, voglio vedere nella UI le modifiche alla playlist corrente anche durante il playback, affinché la lista visualizzata resti coerente con le operazioni effettuate.

**Acceptance criteria:**

**Scenario 1 - Aggiunta traccia durante playback**  
**Given** una playlist è in playback  
**When** aggiungo una traccia alla playlist  
**Then** la UI mostra la nuova traccia nella playlist  
**And** il player mantiene la traccia corrente  

**Scenario 2 - Rimozione di una traccia non corrente**  
**Given** una playlist è in playback  
**And** rimuovo una traccia diversa da quella corrente  
**When** la rimozione viene confermata  
**Then** la UI aggiorna la playlist visualizzata  
**And** il player mantiene la traccia corrente  

**Scenario 3 - Rimozione della traccia corrente**  
**Given** una playlist è in playback  
**And** rimuovo la traccia corrente  
**When** la rimozione viene confermata  
**Then** il sistema passa a una traccia coerente oppure ferma il playback  
**And** la UI mostra lo stato aggiornato  

---

### 🟡US-30 — Riordinare manualmente le tracce in una playlist

**Descrizione:**

Come utente, voglio riordinare manualmente le tracce presenti in una playlist in qualsiasi momento, anche durante la riproduzione, affinché io possa modificare l’ordine di ascolto secondo le mie preferenze.

**Acceptance criteria:**

**Scenario 1 - Riordino playlist ferma**  
**Given** una playlist contiene più tracce  
**And** il playback non è attivo  
**When** cambio l’ordine delle tracce  
**Then** il sistema salva il nuovo ordine della playlist  

**Scenario 2 - Riordino durante playback**  
**Given** una playlist è in playback  
**When** cambio l’ordine delle tracce  
**Then** il sistema aggiorna la playlist  
**And** mantiene coerente la traccia corrente  

**Scenario 3 - Effetto sullo skip**  
**Given** ho riordinato una playlist durante il playback  
**When** premo Skip in modalità Sequential  
**Then** il sistema passa alla traccia successiva secondo il nuovo ordine  

---

### Elementi da discutere

### 🟢US-31 — Rearrange manuale durante riproduzione di playlist
