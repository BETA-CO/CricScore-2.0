# CricScore - Cricket Scoring Application

CricScore is a modern, professional Android application designed to manage and track local cricket matches with high precision. It features a polished Material 3 design with full support for Light and Dark modes.

## Application Modules & Functionality

### 1. Animated Splash Screen (`SplashActivity`)
*   **Dynamic Intro**: Features a spinning cricket ball that drops and bounces into the center of the field.
*   **Branding**: The "CricScore" title appears with a smooth zoom-in and overshoot animation for a premium feel.

### 2. Home Screen (`MainActivity`)
*   **Central Hub**: Start a new match, view historical records, or check match rules.
*   **Latest Result**: Automatically displays a summary card of the most recently played match for quick reference.

### 3. Match Setup (`MatchSetupActivity`)
*   **Configuration**: Define team names, match length (overs), and player count.
*   **Navigation**: Efficiently transitions to player setup while clearing the navigation stack.

### 4. Player Setup (`PlayersSetupActivity`)
*   **Mandatory Entry**: Ensures all player names are filled before proceeding to prevent incomplete scorecards.
*   **Validation**: Real-time error feedback if any field is left blank.
*   **Quick Start**: Includes a "Star Players" option to rapidly populate teams for testing or casual games.

### 5. Interactive Toss (`TossActivity`)
*   **Fair Play**: Uses a cryptographically secure random number generator (`SecureRandom`) and shuffled list strategy for an absolutely unbiased 50/50 toss.
*   **Team Selection**: Before the flip, users assign which team is **HEADS** and which is **TAILS**.
*   **Suspense Animation**: A high-speed 5-second coin spin animation precedes the result.
*   **Outcome Decision**: The winner chooses to either Bat or Bowl, and the app automatically re-orders the batting line-up for the scoring screen.

### 6. Professional Scoring Engine (`ScoringActivity`)
*   **Split UI**: Separate sections for Batters and Bowlers for better readability.
*   **Advanced Scoring**:
    *   Standard runs (0, 1, 2, 3, 4, 6).
    *   **1D & 2D Buttons**: Add runs without rotating strike (ideal for overthrows or specific ground rules).
    *   **Extras**: Comprehensive handling of Wides and No Balls.
*   **Precision Tracking**:
    *   **Maiden Overs**: Automatically detects and credits maidens to the bowler.
    *   **Boundaries**: Tracks individual counts of 4s and 6s.
    *   **Fielding**: Prompts for fielder/keeper selection on Catches, Stumpings, and Run Outs.
*   **Strike Management**: Manual "Swap" button to adjust the striker at any time.
*   **Undo System**: A robust 50-step undo functionality allows for instant correction of mistakes.

### 7. Tie-Breaker (`Super Over`)
*   **Automatic Detection**: Triggered if scores are level after full overs/wickets.
*   **Fresh Start**: Completely separates statistics for the Super Over from the main match. Main match data is preserved, and players start the Super Over with clean stats.

### 8. Comprehensive Scorecard (`ResultActivity`)
*   **Detailed Analytics**:
    *   **Batting**: Runs, Balls, Fours, Sixes, Strike Rate, and Dismissal details.
    *   **Bowling**: Overs, Maidens, Runs conceded, Wickets, and Economy Rate.
    *   **Fielding**: Catches, Stumpings, and Run Outs.
*   **Dynamic Layout**: Only shows relevant sections (e.g., hiding the bowling section for players who didn't bowl).
*   **Dual Scorecards**: Displays separate full scorecards for the Main Match and the Super Over if applicable.

### 9. Match History (`MatchHistoryActivity`)
*   **Local Storage**: Uses **Room Database** for persistent storage of every game.
*   **Review Mode**: Click any past match to view its full detailed scorecard at any time.
*   **Data Management**: Easy deletion of old match records.

## Technical Highlights
*   **Material 3**: Modern, accessible UI components.
*   **Secure Randomness**: Ensures fair play in digital coin tosses.
*   **State Persistence**: Sophisticated undo and Super Over state management.
*   **Adaptive Theming**: Native look and feel in both Light and Dark modes.
