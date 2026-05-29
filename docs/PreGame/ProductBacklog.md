# Product Backlog — Music Playlist Manager
**Project:** Software Architecture Design — A.Y. 2025/2026  
**Estimation format:** Story Points (Fibonacci scale: 1, 2, 3, 5, 8, 13)  On Trello
**AC format:** GIVEN / WHEN / THEN

---

## Color legend
| Color | Type |
|---|---|
| 🟢 Green | LOW PRIORITY |
| 🟡 Yellow | MEDIUM PRIORITY |
| 🔴 Red | HIGH PRIORITY |

---

### 🔴US-01 — Add a new track to the catalog

**Description:**

As a user, I want to add a new track by entering title, author, duration, genre, publication year and tag?, so that I can build a complete music catalog reusable in playlists.

**Acceptance criteria:**

**Scenario 1 - Valid addition**  
**Given** the music catalog is open  
**When** I enter valid title, author, duration, genre, year and optional tag  
**Then** the track is added to the catalog  
**And** it is displayed in the list with all the entered metadata  

**Scenario 2 - Missing required fields**  
**Given** the track insertion form is open  
**When** I leave title or author empty  
**Then** the system shows a validation error  
**And** the track is not saved  

**Scenario 3 - Invalid numeric values**  
**Given** the track insertion form is open  
**When** I enter a duration less than or equal to zero or an invalid year  
**Then** the system shows an error  
**And** the catalog remains unchanged  

---

### 🔴US-02 — View all tracks in the catalog

**Description:**

As a user, I want to view all tracks in the catalog with their main data, so that I can quickly consult my music library before creating or modifying playlists.

**Acceptance criteria:**

**Scenario 1 - Catalog with tracks present**  
**Given** one or more tracks are present in the catalog  
**When** I open the catalog screen  
**Then** I see the list of tracks  
**And** for each track I see title, author, duration, genre and year  

**Scenario 2 - Empty catalog**  
**Given** the catalog contains no tracks  
**When** I open the catalog screen  
**Then** the system shows an empty catalog message  
**And** it does not show inconsistent data  

**Scenario 3 - Update after insertion**  
**Given** I am viewing the catalog  
**When** I add a valid new track  
**Then** the track appears in the list without restarting the application  

---

### 🔴US-03 — Modify the attributes of a track

**Description:**

As a user, I want to modify the attributes of a track already present in the catalog, so that I can correct errors or update music information without having to delete and reinsert the track.

**Acceptance criteria:**

**Scenario 1 - Valid modification**  
**Given** a track exists in the catalog  
**When** I modify one or more data fields with valid values  
**Then** the system updates the track  
**And** shows the new values in the catalog  

**Scenario 2 - Invalid modification**  
**Given** a track exists in the catalog  
**When** I enter invalid values, such as an empty title or non-positive duration  
**Then** the system shows an error  
**And** keeps the previous values of the track  

**Scenario 3 - Consistency with existing playlists**  
**Given** a track is already contained in a playlist  
**When** I modify its metadata from the catalog  
**Then** the playlist shows the updated track  
**And** does not create duplicates of the same track  

---

### 🔴US-04 — Delete a track from the catalog

**Description:**

As a user, I want to delete a track from the music catalog, so that I can remove unwanted content and keep my library organized.

**Acceptance criteria:**

**Scenario 1 - Confirmed deletion**  
**Given** a track exists in the catalog  
**When** I choose to delete it and confirm the operation  
**Then** the track is removed from the catalog  

**Scenario 2 - Cancelled deletion**  
**Given** a track exists in the catalog  
**When** I choose to delete it but cancel the confirmation  
**Then** the track remains in the catalog  

**Scenario 3 - Removal from playlists**  
**Given** the deleted track is present in one or more playlists  
**When** I confirm deletion from the catalog  
**Then** the track is also removed from the playlists that contained it  
**And** the system leaves no invalid references  

---

### 🔴US-05 — Create a new playlist

**Description:**

As a user, I want to create a new playlist identified by a name, so that I can organize the catalog tracks into personalized music collections.

**Acceptance criteria:**

**Scenario 1 - Valid creation**  
**Given** I am in the playlist section  
**When** I enter a valid name for a new playlist  
**Then** the system creates the playlist  
**And** the playlist appears in the playlist list  

**Scenario 2 - Missing name**  
**Given** I am in the playlist creation form  
**When** I try to create a playlist without a name  
**Then** the system shows an error  
**And** does not create any playlist  

**Scenario 3 - Duplicate name**  
**Given** a playlist with the same name already exists  
**When** I try to create a new playlist with that name  
**Then** the system prevents duplication  
**And** shows an understandable error message  

---

### 🔴US-06 — View the content of a playlist

**Description:**

As a user, I want to view the content of a playlist with the ordered list of tracks, so that I can check the music sequence before starting playback.

**Acceptance criteria:**

**Scenario 1 - Playlist with tracks**  
**Given** a playlist contains one or more tracks  
**When** I open the playlist details  
**Then** I see all contained tracks  
**And** for each track I see at least title, author and duration  

**Scenario 2 - Empty playlist**  
**Given** a playlist contains no tracks  
**When** I open the playlist details  
**Then** the system shows an empty playlist message  

**Scenario 3 - Track order**  
**Given** a playlist contains multiple tracks  
**When** I view the playlist  
**Then** the tracks are shown in the order in which they were added  

---

### 🔴US-07 — Add existing tracks to a playlist

**Description:**

As a user, I want to add existing tracks from the catalog to a playlist even while it is currently running, so that I can build an ordered music collection according to my preferences.

**Acceptance criteria:**

**Scenario 1 - Valid addition**  
**Given** at least one track exists in the catalog  
**And** a playlist exists  
**When** I select a track and add it to the playlist  
**Then** the track appears in the selected playlist  

**Scenario 2 - Track already present**  
**Given** a track is already present in the playlist  
**When** I try to add it again  
**Then** the system prevents the duplicate or reports that the track is already present  

**Scenario 3 - Updated playlist**  
**Given** I am viewing the playlist details  
**When** I add a valid track  
**Then** the playlist list is updated correctly  

---

### 🔴US-08 — Remove a track from a playlist

**Description:**

As a user, I want to remove a track from a playlist without deleting it from the catalog even during playback, so that I can modify the playlist content while keeping the track available for other uses.

**Acceptance criteria:**

**Scenario 1 - Valid removal**  
**Given** a playlist contains at least one track  
**When** I select a track and remove it from the playlist  
**Then** the track no longer appears in the playlist  

**Scenario 2 - Track kept in the catalog**  
**Given** I remove a track from a playlist  
**When** I open the music catalog  
**Then** the track is still present in the catalog  

**Scenario 3 - Empty playlist**  
**Given** I remove the last track from a playlist  
**When** I view the playlist  
**Then** the system shows the empty playlist without errors  

**Scenario 4 - Removal during playback**  
**Given** a playlist is playing  
**When** I remove a track from the playlist  
**Then** the system updates the playlist  
**And** playback remains in a consistent state  

---

### 🔴US-09 — Play a Single Track

**Description:**

As a music player user, I want to start the simulated playback of a single track, so that I can start listening without losing the current track.

**Acceptance criteria:**

**Scenario 1 — Normal playback**

**Given** a track has been selected  
**When** I press Play  
**Then** the playback state becomes Playing  
**And** the selected track becomes the current track  

**Scenario 2 — Resume playback**

**Given** a track is in Paused state  
**When** I press Play  
**Then** the playback state becomes Playing  
**And** the current track remains unchanged  

---

### 🔴US-10 — Pause a single track

**Description:**

As a music player user, I want to be able to pause the currently playing track, so that I can momentarily interrupt simulated listening without losing the current timestamp of the song.

**Acceptance criteria:**

**Scenario 1 - Pause request on a playing track**  
**Given** a track is actively running and the player is in the "Playing" state  
**When** I press the "Pause" button  
**Then** the system changes the internal playback state to "Paused"  
**And** the current track remains unchanged in the UI  
**And** the simulation progress timer stops at the exact second of the interruption.  

**Scenario 2 - Pause request on an already paused track (Edge Case)**  
**Given** playback is already in the "Paused" state  
**When** I press the "Pause" button again  
**Then** the system ignores the command and performs no state transition  
**And** it does not generate errors or anomalous behavior in the simulated flow  

---

### 🔴US-11 — Enable loop on a single track

**Description:**

As a music player user, I want to enable continuous loop mode on a single isolated track, so that the same song is automatically and infinitely repeated from the beginning until the feature is disabled.

**Acceptance criteria:**

**Scenario 1 - Continuous repetition of the single track**  
**Given** a track is running from the catalog or from a playlist  
**And** the active playback mode is set to "Single Track Loop"  
**When** the playback simulation reaches the end of the track playback (e.g. 354 seconds)  
**Then** the system keeps the exact same track as the current track without moving to other songs  
**And** resets the timer to 00:00 while continuing simulated playback in "Playing" state.  

**Scenario 2 - Disabling Single Track Loop**  
**Given** a track is playing in "Single Track Loop" mode  
**When** the user disables loop by changing the mode to "Sequential"  
**Then** the current track finishes playing normally  
**And** at its end the system will apply the normal sequential queue (moving to the next playlist song or stopping in "Stopped" state).  

---

### 🔴US-12 — Start sequential playback of a playlist

**Description:**

As a user, I want to start simulated playback of a playlist in sequential mode, so that I can simulate listening to the songs in the expected order.

**Acceptance criteria:**

**Scenario 1 - Sequential play of a playlist**  
**Given** a playlist contains multiple tracks  
**When** I start sequential playback  
**Then** the system plays the first track of the playlist  
**And** keeps the original order of the tracks  

**Scenario 2 - Attempt to Play an empty Playlist**  
**Given** a newly created playlist contains no tracks and is in sequential playback  
**When** I try to start sequential playback on it  
**Then** the system prevents the simulation from starting  

---

### 🔴US-13 — Pause a playlist

**Description:**

As a music player user, I want to be able to pause the playlist currently being played, so that I can momentarily interrupt listening to the song sequence without losing the current track and my position inside the queue.

**Acceptance criteria:**

**Scenario 1 - Pause request on a playlist in playback**  
**Given** the player is in the "Playing" state and is playing a playlist (e.g. at song #3 of 10)  
**When** I press the "Pause" button  
**Then** the system changes the player state to "Paused"  
**And** keeps the index of the active track inside the playlist in memory.  

---

### 🔴US-14 — Skip to the next track during playback

**Description:**

As a user, I want to skip to the next track during simulated playback, so that I can quickly move to the following song of the current playlist.

**Acceptance criteria:**

**Scenario 1 - Skip with next track available**  
**Given** a playlist is in Playing state  
**And** a next track exists  
**When** I press Skip  
**Then** the current track becomes the next track  
**And** the state remains Playing  

**Scenario 2 - Skip from the last track**  
**Given** I am playing the last track of the playlist in sequential mode  
**When** I press Skip  
**Then** the state becomes Stopped  
**And** no error is generated  

**Scenario 3 - Skip when the player is Paused**  
**Given** a playlist is in Paused state  
**And** a next track exists  
**When** I press Skip  
**Then** the current track becomes the next track  
**And** the state remains Paused  

---

### 🔴US-15 — Play a playlist in shuffle mode

**Description:**

As a user, I want to play a playlist in shuffle mode, so that I can listen to a sequence of tracks in a non-deterministic order compared to the order of the playlist or catalog.

**Acceptance criteria:**

**Scenario 1 - Shuffle start**  
**Given** a playlist contains at least two tracks  
**When** I select Shuffle mode and start playback  
**Then** the system selects tracks in an order not necessarily equal to the playlist order  

**Scenario 2 - Playlist with a single track**  
**Given** a playlist contains a single track  
**When** I start Shuffle playback  
**Then** the system plays that track without errors  

---

### 🔴US-16 — Enable loop on playlist

**Description:**

As a music player user, I want to enable continuous loop mode on a playlist in playback, so that the playlist automatically restarts from the first song immediately after the end of the last one, in an infinite cycle.

**Acceptance criteria:**

**Scenario 1 - Loop on playlist**  
**Given** a playlist contains multiple tracks  
**And** playback is in Loop mode  
**When** the last track ends or skip is executed from the last track  
**Then** the system returns to the first track of the playlist  
**And** keeps the Playing state  

**Scenario 2 - Mode change with active playlist**  
**Given** playlist playback is active in "Loop Playlist" mode  
**When** I change the playback mode to "Sequential" or "Shuffle"  
**Then** the system applies the new algorithmic strategy only to subsequent commands and events  
**And** it does not interrupt or reset the currently running simulated song.  

---

### 🔴US-17 — View current track and playback state

**Description:**

As a user, I want to see the current track and the playback state in the UI, so that I can understand what the Media Player is playing at any moment.

**Acceptance criteria:**

**Scenario 1 - Playing state**  
**Given** I start playback of a track or playlist  
**When** playback enters Playing state  
**Then** the UI shows the current track  
**And** shows the Playing state  

**Scenario 2 - Paused state**  
**Given** playback is in Playing state  
**When** I press Pause  
**Then** the UI shows the Paused state  
**And** keeps the current track visible  

**Scenario 3 - Stopped state**  
**Given** playback is active  
**When** playback ends or I press Stop  
**Then** the UI shows the Stopped state  
**And** does not show a playing track or keeps the last track according to the design decision  

---

### 🔴US-18 — View the list of created playlists

**Description:**

As a music player user, I want to view the complete list of all playlists I have created in the Media Player, so that I can browse my collections and quickly select which playlist to play or modify.

**Acceptance criteria:**

**Scenario 1 - View with multiple playlists present**  
**Given** the user has previously created 3 playlists in the system (e.g. "Rock", "Pop", "Jazz")  
**When** I access the main Media Player screen  
**Then** the system shows the exact list of all 3 playlists with their updated names  
**And** the visual list adapts dynamically without requiring an application restart  

**Scenario 2 - Initial view without playlists**  
**Given** a user has just installed or started the application for the first time and has not created any playlist yet  
**When** I view the playlist list  
**Then** the system shows the empty playlist list or placeholder text (e.g. "No playlist created")  
**And** it does not generate graphical errors  

---

### 🟡US-19 — Undo the insertion of a new song into the catalog

**Description:**

As a music player user, I want to be able to undo the insertion of a new song into the global catalog, so that the track created by mistake is instantly removed from the system and from any playlist in which it may have been inserted in the meantime.

**Acceptance criteria:**

**Scenario 1 - Undo of a globally created song**  
**Given** I have just globally created the track "Song Error" with its metadata  
**And** I added it to the playlist "Favorites"  
**When** I execute the "Undo" command  
**Then** the song "Song Error" is entirely deleted from the global catalog  
**And** it is automatically and in real time removed from the playlist "Favorites" without leaving records.  

---

### 🟡US-20 — Undo global deletion of a song

**Description:**

As a music player user, I want to be able to undo the global deletion of a song from the catalog, so that the track is restored in the general catalog and automatically reinserted into all playlists where it was present before deletion, maintaining its original position.

**Acceptance criteria:**

**Scenario 1 - Undo of a global removal with track present in multiple playlists**  
**Given** the song "Bohemian Rhapsody" is present in the global catalog  
**And** it is inserted in the playlist "Rock Classics" (at position #1) and in the playlist "Best of Queen" (at position #4)  
**And** I performed the global deletion of the track (which removed it from the catalog and from all lists)  
**When** I execute the "Undo" command  
**Then** "Bohemian Rhapsody" reappears in the global catalog with all its metadata intact  
**And** it is automatically reinserted into the playlist "Rock Classics" exactly at position #1  
**And** it is automatically reinserted into the playlist "Best of Queen" exactly at position #4  
**And** the multimedia interface instantly updates all tables and visual lists.  

---

### 🟡US-21 — Undo creation of a new playlist

**Description:**

As a music player user, I want to be able to undo the creation of a new playlist, so that the empty playlist generated by mistake is deleted from the system.

**Acceptance criteria:**

**Scenario 1 - Undo of a newly created playlist**  
**Given** I have just created a new playlist  
**When** I execute the "Undo" command  
**Then** the playlist is entirely deleted from the system  
**And** it disappears from the list of playlists visible in the UI.  

---

### 🟡US-22 — Undo removal of a playlist

**Description:**

As a music player user, I want to be able to undo the removal of a previously deleted playlist, so that I can recover the entire collection and all the songs contained in it.

**Acceptance criteria:**

**Scenario 1 - Undo of removal of a populated playlist**  
**Given** I removed a working playlist that contained songs  
**When** I execute the "Undo" command  
**Then** the entire playlist is restored in the system with the same name  
**And** it keeps the original list of songs intact inside it  
**And** it immediately reappears in the user interface.  

---

### 🟡US-23 — Undo adding a track to a playlist

**Description:**

As a music player user, I want to be able to undo adding a track to a playlist, so that I can immediately remove it in case of wrong insertion.

**Acceptance criteria:**

**Scenario 1 - Undo of a successful addition**  
**Given** I have just added the track "Billie Jean" to the playlist "Pop"  
**When** I execute the "Undo" command  
**Then** the track is removed from the playlist  
**And** the playlist screen updates in real-time  

**Scenario 2 - Attempt to Undo without history**  
**Given** the track addition command stack is empty  
**When** I press the "Undo" button  
**Then** the system does not modify the data state  
**And** the visual button in the UI is disabled to prevent invalid input.  

---

### 🟡US-24 — Undo removal of a track from a playlist

**Description:**

As a music player user, I want to be able to undo the removal of a track from a playlist, so that the song is reinserted into the collection without losing the original order.

**Acceptance criteria:**

**Scenario 1 - Undo of a successful removal**  
**Given** the playlist contains three tracks in order and I remove one positioned in the middle (index #1)  
**When** I execute the "Undo" command  
**Then** the track is reinserted into the playlist exactly in the correct original position (index #1)  

---

### 🟡US-25 — View the most frequently played tracks

**Description:**

As a user, I want to see the most frequently played tracks on the home page, so that I can quickly access the content I listen to the most.

**Acceptance criteria:**

**Scenario 1 - Track play count**  
**Given** I play a track multiple times  
**When** I open the home page  
**Then** the track appears among the most played ones  

**Scenario 3 - No playback**  
**Given** I have not played any tracks yet  
**When** I open the home page  
**Then** no track appears among the most played ones  

---

### 🟡US-26 — View the most frequently played playlists

**Description:**

As a user, I want to see the most frequently played playlists on the home page, so that I can quickly access the content I listen to the most.

**Acceptance criteria:**

**Scenario 1 - Playlist play count**  
**Given** I play a playlist multiple times  
**When** I open the home page  
**Then** the playlist appears among the most played ones  

**Scenario 2 - No playback**  
**Given** I have not played any playlists yet  
**When** I open the home page  
**Then** the system shows an empty home or a coherent message  

---

### 🟡US-27 — Add visual tags to tracks

**Description:**

As a user, I want to add visual tags to tracks, such as favourite, explicit or new release, so that tracks are displayed in a more informative way and can be used to create automatic playlists.

**Acceptance criteria:**

**Scenario 1 - Tag addition**  
**Given** a track exists in the catalog  
**When** I assign a visual tag to the track  
**Then** the tag is associated with the track  
**And** it is shown in the UI  

**Scenario 2 - Tag removal**  
**Given** a track has an associated visual tag  
**When** I remove the tag  
**Then** the tag is no longer shown on the track  

**Scenario 3 - Multiple tags**  
**Given** a track exists in the catalog  
**When** I assign multiple compatible tags  
**Then** the UI shows all associated tags  

---

### 🟡US-28 — Create automatic playlists

**Description:**

As a user, I want to be able to automatically create playlists based on genre, year and tag, so that I can quickly organize the catalog without manually selecting each track.

**Acceptance criteria:**

**Scenario 1 - Automatic playlist by genre**  
**Given** the catalog contains tracks of different genres  
**When** I choose to create a playlist by genre  
**Then** the system creates a playlist containing only the tracks of the selected genre  

**Scenario 2 - Automatic playlist by year**  
**Given** the catalog contains tracks with different years  
**When** I choose to create a playlist by year  
**Then** the system creates a playlist containing only the tracks of the selected year  

**Scenario 3 - No matching track**  
**Given** no track satisfies the chosen criterion  
**When** I try to create the automatic playlist  
**Then** the system does not create an unwanted empty playlist  
**And** shows a no results message  

---

### 🟡US-29 — Update the UI of the current playlist during playback

**Description:**

As a user, I want to see changes to the current playlist in the UI even during playback, so that the displayed list remains consistent with the performed operations.

**Acceptance criteria:**

**Scenario 1 - Adding a track during playback**  
**Given** a playlist is in playback  
**When** I add a track to the playlist  
**Then** the UI shows the new track in the playlist  
**And** the player keeps the current track  

**Scenario 2 - Removing a non-current track**  
**Given** a playlist is in playback  
**And** I remove a track different from the current one  
**When** the removal is confirmed  
**Then** the UI updates the displayed playlist  
**And** the player keeps the current track  

**Scenario 3 - Removing the current track**  
**Given** a playlist is in playback  
**And** I remove the current track  
**When** the removal is confirmed  
**Then** the system switches to a consistent track or stops playback  
**And** the UI shows the updated state  

---

### 🟢US-30 — Manually reorder tracks in a playlist

**Description:**

As a user, I want to manually reorder the tracks present in a playlist at any time, even during playback, so that I can modify the listening order according to my preferences.

**Acceptance criteria:**

**Scenario 1 - Reordering a stopped playlist**  
**Given** a playlist contains multiple tracks  
**And** playback is not active  
**When** I change the order of the tracks  
**Then** the system saves the new playlist order  

**Scenario 2 - Reordering during playback**  
**Given** a playlist is in playback  
**When** I change the order of the tracks  
**Then** the system updates the playlist  
**And** keeps the current track consistent  

**Scenario 3 - Effect on skip**  
**Given** I reordered a playlist during playback  
**When** I press Skip in Sequential mode  
**Then** the system moves to the next track according to the new order  
