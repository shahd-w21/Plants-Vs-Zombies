<div align="center">

![Typing SVG](https://readme-typing-svg.demolab.com/?lines=🌱+Plants+vs+Zombies;JavaFX+Tower+Defense+Game;5+Unique+Themed+Levels!;&center=true&width=700&height=80&size=28&weight=700&color=4CAF50&pause=1000)

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-007396?style=for-the-badge&logo=java&logoColor=white)
![Status](https://img.shields.io/badge/Status-Complete-4CAF50?style=for-the-badge)
![License](https://img.shields.io/badge/License-Academic-7F77DD?style=for-the-badge)

</div>

---

## 📌 Overview

A **JavaFX-based Plants vs Zombies** game that recreates the classic tower defense experience with a creative twist. Players strategically place plants to defend their home against waves of zombies across **5 unique themed levels** — each with its own visual style, plants, and zombie types, plus an exciting bonus bowling level.

> 🎓 Built as an academic project at **Misr International University (MIU)** — Computer Science Department

---

## 🎬 Gameplay Video

<div align="center">

[![Watch Gameplay](https://img.youtube.com/vi/6E5kbJZrKIE/maxresdefault.jpg)](https://www.youtube.com/watch?v=6E5kbJZrKIE)

> 👆 Click the thumbnail to watch the full gameplay on YouTube 🎮

</div>

---

## 🗺️ Game Levels

<table>
<tr>
<td align="center" width="16%">

🌿<br>**Level 1**<br>Original

</td>
<td align="center" width="16%">

🎄<br>**Level 2**<br>Christmas

</td>
<td align="center" width="16%">

🎃<br>**Level 3**<br>Halloween

</td>
<td align="center" width="16%">

🍬<br>**Level 4**<br>Candy

</td>
<td align="center" width="16%">

🟫<br>**Level 5**<br>Minecraft

</td>
<td align="center" width="16%">

🎳<br>**Bonus**<br>Bowling

</td>
</tr>
</table>

| Level | Theme | Description |
|-------|-------|-------------|
| 1 | 🌿 **Original** | The classic PvZ backyard experience with familiar plants & zombies |
| 2 | 🎄 **Christmas** | Festive winter theme with holiday-decorated plants and zombie elves |
| 3 | 🎃 **Halloween** | Spooky night setting with dark plants and terrifying zombie variants |
| 4 | 🍬 **Candy** | Sweet colorful candy world with sugar-themed characters |
| 5 | 🟫 **Pixeled (Minecraft)** | Blocky Minecraft-style world with pixelated plants and creeper zombies |
| 🎳 | **Bonus — Bowling** | Roll plants like bowling balls to knock out zombie waves |

---

## ✨ Key Features
- 🎮 **5 Unique Themed Levels** — each with its own visual style and characters
- 🎳 **Bonus Bowling Level** — a fun twist on the classic gameplay
- 🌱 **Multiple Plant Types** — each with unique abilities and attack styles
- 🧟 **Multiple Zombie Types** — each with different speeds and strengths
- 🖥️ **JavaFX GUI** — smooth animations and graphical interface
- 💥 **Collision Detection** — accurate hit detection between plants and zombies
- 🏆 **Score Tracking** — keep track of your performance across levels
- ☀️ **Resource Management** — collect sun to strategically place plants
---

## 🌱 Plants & Zombies

**Plants** — each with unique abilities:
- 🌻 Sun producers to generate resources
- 🟢 Shooters that attack incoming zombies
- 💣 Explosive plants for area damage
- 🧊 Freeze plants to slow zombies down
- 🛡️ Wall plants to block zombie paths

**Zombies** — each with different strengths:
- 🧟 Basic zombies — slow and standard
- 🪖 Armored zombies — harder to kill
- 🏃 Fast zombies — rush your defenses
- 👾 Boss zombies — appear at end of waves

---

## 🎮 How to Play

| Step | Action |
|------|--------|
| 1 | ☀️ **Collect Sun** — click falling sun to earn resources |
| 2 | 🌱 **Buy Plants** — spend sun to select plants from the top bar |
| 3 | 📍 **Place Plants** — click grid cells to place your plants |
| 4 | 🧟 **Defend** — stop zombies from reaching the left side |
| 5 | 🏆 **Clear Waves** — defeat all zombies to complete the level |
| 6 | 🔓 **Progress** — beat each level to unlock the next theme |

---

## 🛠️ Tech Stack

| Technology | Purpose |
|-----------|---------|
| ☕ Java | Core game logic and OOP structure |
| 🎨 JavaFX | GUI, animations, and rendering |
| 🖼️ FXML | UI layout and scene design |
| 🎵 JavaFX Media | Sound effects and background music |

---

## 🚀 How to Run

### Requirements
- Java JDK **11 or higher**
- JavaFX SDK

### Option 1 — IntelliJ IDEA (Easiest)
1. Clone the repo and open it in **IntelliJ IDEA**
2. Add JavaFX to your project libraries
3. Run `Main.java`

### Option 2 — Command Line
```bash
# Clone the repository
git clone https://github.com/shahd-w21/Plants-Vs-Zombies.git
cd Plants-Vs-Zombies

# Run with JavaFX
java --module-path /path/to/javafx-sdk/lib \
     --add-modules javafx.controls,javafx.fxml,javafx.media \
     -jar PlantsVsZombies.jar
```

---

## 📂 Project Structure

```
Plants-Vs-Zombies/
│
├── 📁 src/
│   ├── 📄 Main.java                        # Entry point of the application
│   │
│   ├── 📁 application/
│   │   ├── 📄 Game.java                    # Core game loop and logic
│   │   ├── 📄 GameController.java          # Handles user input and events
│   │   └── 📄 ScoreManager.java            # Score tracking system
│   │
│   ├── 📁 levels/
│   │   ├── 📄 Level.java                   # Base level class
│   │   ├── 📄 OriginalLevel.java           # 🌿 Classic backyard level
│   │   ├── 📄 ChristmasLevel.java          # 🎄 Christmas themed level
│   │   ├── 📄 HalloweenLevel.java          # 🎃 Halloween themed level
│   │   ├── 📄 CandyLevel.java              # 🍬 Candy themed level
│   │   ├── 📄 MinecraftLevel.java          # 🟫 Pixeled Minecraft level
│   │   └── 📄 BowlingLevel.java            # 🎳 Bonus bowling level
│   │
│   ├── 📁 plants/
│   │   ├── 📄 Plant.java                   # Base plant class
│   │   ├── 📄 Sunflower.java               # ☀️ Produces sun resources
│   │   ├── 📄 Peashooter.java              # 🟢 Shoots peas at zombies
│   │   ├── 📄 WallNut.java                 # 🛡️ Blocks zombie path
│   │   ├── 📄 CherryBomb.java              # 💣 Area explosion damage
│   │   └── 📄 Freezer.java                 # 🧊 Slows down zombies
│   │
│   ├── 📁 zombies/
│   │   ├── 📄 Zombie.java                  # Base zombie class
│   │   ├── 📄 BasicZombie.java             # 🧟 Standard slow zombie
│   │   ├── 📄 ArmoredZombie.java           # 🪖 High defense zombie
│   │   ├── 📄 FastZombie.java              # 🏃 Rushes your defenses
│   │   └── 📄 BossZombie.java              # 👾 End of wave boss
│   │
│   └── 📁 utils/
│       ├── 📄 CollisionDetector.java       # Handles plant-zombie collisions
│       ├── 📄 SunManager.java              # Sun collection and spending
│       └── 📄 WaveSpawner.java             # Controls zombie wave timing
│
├── 📁 resources/
│   ├── 📁 images/
│   │   ├── 📁 plants/                      # Plant sprites per theme
│   │   ├── 📁 zombies/                     # Zombie sprites per theme
│   │   ├── 📁 backgrounds/                 # Level background images
│   │   └── 📁 ui/                          # Buttons, icons, menus
│   │
│   ├── 📁 sounds/
│   │   ├── 📁 music/                       # Background music per level
│   │   └── 📁 effects/                     # Attack, death, win sounds
│   │
│   └── 📁 fxml/
│       ├── 📄 MainMenu.fxml                # Main menu layout
│       ├── 📄 LevelSelect.fxml             # Level selection screen
│       └── 📄 GameBoard.fxml               # Main game board layout
│
└── 📄 README.md
```
