# 🧠 BrainZone – Mind Games App

**Mobile Application Development – Group Project**  
Platform: Android (API 24+) | Language: Java

---

## 📁 Project Structure

```
BrainZone/
├── app/
│   ├── build.gradle                          ← App dependencies
│   └── src/main/
│       ├── AndroidManifest.xml               ← Components + permissions
│       ├── java/com/example/brainzone/
│       │   ├── Constants.java                ← All SharedPreferences keys & extras
│       │   ├── SplashActivity.java           ← Launcher screen
│       │   ├── MainActivity.java             ← Main menu
│       │   ├── SettingsActivity.java         ← Music, SFX, difficulty, reset
│       │   ├── LeaderboardActivity.java      ← Top-5 scores per game
│       │   ├── Game1Activity.java            ← Multiplication Puzzle
│       │   ├── Game2Activity.java            ← Memory Match
│       │   ├── Game3Activity.java            ← Word Scramble
│       │   ├── GameResultActivity.java       ← Shared results screen
│       │   ├── BackgroundMusicService.java   ← Foreground music service
│       │   ├── ScreenOffReceiver.java        ← Dynamic BroadcastReceiver
│       │   ├── CardAdapter.java              ← RecyclerView adapter (Game 2)
│       │   ├── LeaderboardAdapter.java       ← RecyclerView adapter (Leaderboard)
│       │   ├── Card.java                     ← Model: memory card
│       │   └── ScoreEntry.java               ← Model: leaderboard row
│       └── res/
│           ├── layout/                       ← All XML screen layouts
│           ├── values/                       ← strings, colors, themes, dimens
│           └── drawable/                     ← Gradient backgrounds, buttons
├── build.gradle                              ← Project-level Gradle
├── settings.gradle
└── README.md
```

---

## ⚙️ Android Components Checklist

| Component | Where |
|---|---|
| **Activities (8)** | Splash, Main, Settings, Leaderboard, Game1, Game2, Game3, GameResult |
| **Service** | `BackgroundMusicService` – foreground service with MediaPlayer |
| **BroadcastReceiver** | `ScreenOffReceiver` – dynamic, pauses active game on screen off |
| **SharedPreferences** | Player name, scores, settings – read/write across all activities |
| **Explicit Intents** | All screen navigation + `putExtra` / `getStringExtra` |
| **Implicit Intent** | `ACTION_SEND` – share score from GameResultActivity |
| **RecyclerView** | Card grid (Game2), Leaderboard rows |
| **CountDownTimer** | Game1 (per question), Game3 (per word) |
| **Runnable + Handler** | Game2 elapsed-time timer |
| **onSaveInstanceState** | Game1 – preserves score across screen rotation |
| **Custom Broadcast** | `com.example.brainzone.GAME_OVER` – sent after every game |

---

## 🎵 Adding Background Music

1. Create the folder: `app/src/main/res/raw/`
2. Add your MP3 file named exactly: `background_music.mp3`
3. Re-build. The `BackgroundMusicService` auto-detects it via `getResources().getIdentifier()`.

> Without the file, the service still starts and shows its foreground notification — it just won't play audio.

---

## 🚀 How to Open in Android Studio

1. Clone / unzip this project
2. **File → Open** → select the `BrainZone/` folder
3. Let Gradle sync
4. Run on an emulator (API 24+) or a real device

---

## 🎮 Games Overview

### 1. Multiplication Puzzle
- 10 questions per round with a hidden value (e.g., `6 × ? = 48`)
- Countdown timer: Easy=20s, Medium=12s, Hard=7s
- Scoring: **+10** correct | **0** wrong | **-5** timeout

### 2. Memory Match (Flip Cards)
- Grid: Easy=4×3, Medium=4×4, Hard=4×5
- Tap two cards to flip; matched pairs stay green
- Score = `1000 - (extraMoves × 10) - (elapsedSeconds × 2)`

### 3. Word Scramble
- 10 words per round from 4 categories (Animals, Countries, Food, Tech)
- Scoring: **+15** (1st attempt) | **+8** (2nd) | **+3** (3rd)
- Hint button reveals one letter (costs 5 pts, once per word)

---

## 📚 Lecture Concepts Used

| Lecture | Concept Applied |
|---|---|
| 4 | Activity lifecycle, onSaveInstanceState |
| 5 | XML files (strings, colors, drawables) |
| 6 | LinearLayout, button listeners, Toast |
| 7 | EditText, ToggleButton, RadioButton |
| 8 | Explicit Intents + putExtra/getStringExtra |
| 9 | Implicit Intent (ACTION_SEND share) |
| 11 | RecyclerView, Adapter, ViewHolder, GridLayoutManager |
| 13–14 | Runnable, Handler, background threading |
| 15–16 | Background/Foreground Service, MediaPlayer, Notification |
| 17 | SharedPreferences read/write/delete |
| 18 | Dynamic BroadcastReceiver, register/unregister |

---

*BrainZone – Train Your Brain, Beat Your Best!* 🧠
