# CricScore - Cricket Scoring Application

CricScore is a modern Android application designed to manage and track local cricket matches with ease. It features a polished Material 3 design with full support for Light and Dark modes.

## Application Flow

The application follows a linear and intuitive flow to set up and score a cricket match:

1.  **Home Screen (`MainActivity`)**
    *   The entry point of the app.
    *   Allows users to start a new match, view match history, or read basic rules.
    *   Displays a summary of the latest match result.

2.  **Match Setup (`MatchSetupActivity`)**
    *   User enters team names (Team A vs Team B).
    *   Defines the number of players per team and total overs for the match.
    *   Once confirmed, the activity finishes to maintain a clean navigation stack.

3.  **Player Setup (`PlayersSetupActivity`)**
    *   This screen appears twice: once for Team A and once for Team B.
    *   Users enter the names of all players for each team.
    *   Features a "Fill Dummy Names" option for quick testing.
    *   The UI dynamically updates its title and hints based on the team being configured.

4.  **The Toss (`TossActivity`)**
    *   A virtual coin flip decides the toss winner.
    *   The winner chooses to either "Bat" or "Bowl" first.
    *   The activity re-orders the teams internally so that the batting team always starts as the active team in the scoring screen.

5.  **Scoring Screen (`ScoringActivity`)**
    *   The core of the application where real-time scoring happens.
    *   **Dynamic UI**: Shows live score, wickets, overs, and run rate.
    *   **Player Tracking**: Allows selection of Striker, Non-Striker, and Bowler.
    *   **Undo Functionality**: A robust 50-step undo system allows correcting scoring mistakes.
    *   **Innings Management**: Handles transitions between the first and second innings, setting targets automatically.
    *   **Tie-Breaker**: If scores are level at the end of the match, a **Super Over** (1 over, 2 wickets) can be initiated to decide the winner.

6.  **Match Result (`ResultActivity`)**
    *   Displays the final winner and a detailed scorecard for both teams.
    *   Detailed statistics include runs, balls faced, strike rate, and dismissal type for every player.

7.  **Match History (`MatchHistoryActivity`)**
    *   Uses a **Room Database** to store all completed matches.
    *   Matches are displayed in a clean list format with the ability to delete past records.

8.  **Rules (`RulesActivity`)**
    *   A quick reference guide for basic cricket rules.

## Technical Highlights

*   **Material 3 Design**: Leverages the latest Material design components for a modern look.
*   **Adaptive Theme**: Uses dynamic theme attributes (`?attr/...`) to ensure perfect compatibility with both Dark navy and Light off-white themes.
*   **Room Persistence**: Reliable local storage for match history.
*   **Gson Integration**: Used for serializing complex player statistics into the database.
*   **Clean Stack Navigation**: Every setup step finishes its activity upon completion, preventing confusing "back-loops" for the user.
