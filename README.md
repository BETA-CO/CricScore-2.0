# CricScore - Professional Cricket Scoring & Analytics Engine

CricScore is a high-precision Android application designed for detailed tracking of cricket matches. It prioritizes data integrity, user experience, and professional reporting. This document provides an in-depth look at the system architecture and features for both users and developers.

---

## Detailed Feature Breakdown

### 1. Professional PDF Reporting System
Unlike standard apps that take simple screenshots, CricScore implements a low-level drawing engine using the `PdfDocument` and `Canvas` APIs.
*   **Vector-Quality Tables**: Data is rendered as selectable text within structured tables.
*   **Comprehensive Data**: Includes batter names, runs, balls, boundaries (4s/6s), strike rates, and dismissal types. Bowling sections include overs, maidens, runs conceded, wickets, and economy rates.
*   **Multi-Page Logic**: The engine calculates vertical offsets dynamically. if a match has many players, it automatically spawns new A4 pages to prevent data cutoff.
*   **Secure Distribution**: Uses `androidx.core.content.FileProvider` to generate temporary URI permissions, allowing users to share the report instantly via third-party apps without granting broad storage access.

### 2. Intelligent Scoring & Rules Engine
The core scoring logic is designed to handle the nuances of local ground rules and professional standards.
*   **Dynamic Extras Management**: 
    *   **Wide Ball Popup**: Handles "Wide + X" scenarios (e.g., Wide + 1, 2, 3, or 4 runs). The system correctly credits 1 penalty run to the team plus the extra runs taken, all against the bowler's stats.
    *   **No Ball Logic**: Similar popup for No Balls, ensuring the striker gets credit for runs scored off the bat while the bowler is penalized.
*   **Customizable "Ground Rules"**: Users can toggle **1D (1 Run Dot)** and **2D (2 Run Dot)** buttons. These allow adding runs to the total without rotating the strike, perfect for overthrows or specific local rules where running isn't required.
*   **Robust Undo (State Snapshotting)**: The engine uses a "Memento Pattern" variant. Every ball delivery triggers a `ScoreState` snapshot. This allows for a perfect 50-step undo, reverting scores, wickets, overs, and individual player stats to their exact previous state.

### 3. Real-Time Analytics & Match Context
*   **Chase Intelligence**: During the second innings, the app activates a "Target Monitor." It calculates `(First Innings Score + 1) - Current Score` and displays a live string like "15 runs needed from 12 balls."
*   **Automatic Maiden Detection**: The system tracks the start of every over. If an over completes with 6 legal deliveries and zero runs conceded (excluding specific leg-byes/byes if applicable), it automatically increments the bowler's "Maidens" count.
*   **Strike Rotation Logic**: Automates strike swaps after odd runs and at the end of each over, with a manual "Swap" override for edge cases.

### 4. Persistence & Data Management
*   **Room Database**: Uses a local SQLite database with Room for high-speed match logging.
*   **JSON Serialization**: Since cricket stats involve nested lists (PlayerStats), the app utilizes **Google Gson** to serialize/deserialize complex scorecard data into text blobs within the `Match` database entity.

---

## Detailed Application Flow (Technical Trace)

Understanding the flow is critical for developers looking to modify the scoring logic or UI.

### Phase 1: Configuration & Roster Input
1.  **`MatchSetupActivity`**: Captures global match parameters (Team Names, Overs, Player Count). It also handles the optional **Common Player** name and the **1D/2D toggle**. These are passed as `Intent` extras.
2.  **`PlayersSetupActivity` (Pass 1 - Team A)**: Generates a dynamic list of `EditText` fields based on the player count. Once submitted, it launches *itself* again but with a flag `setupForTeamA = false`.
3.  **`PlayersSetupActivity` (Pass 2 - Team B)**: Captures the second team. Before finishing, it checks if a **Common Player** was specified in Phase 1. If so, it appends that name to both team strings before moving forward.

### Phase 2: The Pre-Match Toss
1.  **`TossActivity`**: An interactive coin flip screen.
2.  **Mistake Correction**: If a user notices a typo in player names, the **"Edit Teams"** button triggers a cleanup. It finishes the current activity and returns to `PlayersSetupActivity` for Team A, carrying back all match configurations to preserve user progress.
3.  **Handoff**: Upon selecting "Bat" or "Bowl," the activity reorders the `playersA` and `playersB` data so that the batting team is always treated as `Innings 1` in the next stage.

### Phase 3: Live Scoring Engine
1.  **`ScoringActivity`**: Receives all rosters and rules. 
2.  **Initial Selection**: The user must pick a Striker, Non-Striker, and Bowler. These selections create **Player Keys** (`Team:Name`) used to index the `HashMap` collections that track individual performance.
3.  **Ball Lifecycle**: 
    *   User taps a run button.
    *   `saveState()` creates a snapshot of all maps/variables.
    *   Scores and player stats are updated.
    *   Strike rotation logic is evaluated.
    *   Over completion or Innings completion is checked.
4.  **Completion**: When the match ends, a final `Match` object is constructed, serialized using Gson, and saved to the Room DB.

### Phase 4: Analysis & Reporting
1.  **`ResultActivity`**: Fetches the `Match` data.
2.  **Scorecard Rendering**: Dynamically inflates `item_player_stat.xml` for every player in the roster, hiding sections (like bowling) for players who didn't participate in that specific role.
3.  **Export**: The `createAndSharePdf()` method iterates through the data structures to draw the professional PDF report described in Feature #1.

---

## Technical Architecture
*   **Language**: Java (JDK 11+)
*   **Database**: Room Persistence Library (SQLite wrapper)
*   **Serialization**: GSON 2.10.1
*   **UI Framework**: Material Components 3 (M3)
*   **PDF Engine**: Android Graphics (PdfDocument)
*   **Asset Management**: Vector Drawables for resolution-independent icons.
