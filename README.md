# 🐍 Classic Snake Game (Java Swing)

A feature-rich, beginner-friendly desktop Snake game built with pure Java and Swing (`JFrame` & `JPanel`).

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)

---

## ✨ Features

- **Custom Snake Graphics**: Realistic snake visuals with animated eyes, forked tongue facing the movement direction, belly scale patterns, and a tapered tail.
- **Custom Apple Icon**: Apple sprite instead of plain dots.
- **Background Graphic**: Custom background graphic integrated into the playfield.
- **Background Music**: Looping 44.1 kHz soundtrack that syncs with gameplay and pauses/resumes cleanly.
- **Pause & Resume**: Press `ESC` or `P` at any time to pause the game and music.
- **Score Tracking & Collision Detection**: Tracks score (+10 per food) and detects self-collision and wall crashes.
- **Restart Mechanism**: Hit `SPACE` on the Game Over screen to instantly start a new match.

---

## 🎮 Controls

| Key | Action |
|:---:|:---|
| `↑` `↓` `←` `→` | Steer the Snake (Up, Down, Left, Right) |
| `ESC` or `P` | Pause / Resume Game & Music |
| `SPACE` | Restart after Game Over |

---

## 🚀 How to Run

### Prerequisites
- Java Development Kit (JDK 11 or newer) installed.

### 1. Clone the repository
```bash
git clone https://github.com/<YOUR-USERNAME>/<YOUR-REPO-NAME>.git
cd <YOUR-REPO-NAME>
```

### 2. Compile
```bash
javac SnakeGame.java
```

### 3. Run
```bash
java SnakeGame
```

---

## 📁 Project Structure

```
├── SnakeGame.java       # Main game code (Game loop, graphics, sound, input)
├── apple.png            # Apple food sprite
├── background.png       # Game arena background
├── music.wav            # Looping background soundtrack
├── .gitignore           # Git ignore rules
└── README.md            # Project documentation
```

