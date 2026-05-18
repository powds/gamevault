# GameVault - Private Vault Hidden in 2048 Puzzle

A privacy vault app disguised as a 2048 puzzle game. Hide your private photos, videos, documents, and apps behind an addictive game interface.

![GameVault](https://img.shields.io/badge/Platform-Android-green) ![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-purple) ![License](https://img.shields.io/badge/License-MIT-blue)

## Features

### Game Layer (Disguise)
- Fully functional 2048 puzzle game
- Smooth animations and swipe controls
- Score tracking with local persistence
- Best score display
- **Secret unlock**: Tap 4 corners (TL -> TR -> BL -> BR) to access vault

### Vault Layer (Hidden)
- **File Hiding**: Hide photos, videos, documents from gallery/file manager
- **App Hiding**: Hide apps from app drawer, launch from within vault
- **App Locking**: Lock individual apps requiring vault password to open
- **Built-in Viewers**: View photos, play videos, read PDFs without leaving vault
- **Search**: Quick search across all hidden items
- **Storage Info**: See how much space your vault is using

### Security
- **Pattern Lock**: Draw secret pattern to unlock vault
- **PIN Lock**: 4-digit PIN fallback option
- **Decoy PIN**: Fake vault with different PIN (shows fake content)
- **Biometric**: Fingerprint unlock option
- **Auto-lock**: Automatically lock when leaving app
- **Intruder Capture**: Take photo on wrong password attempts
- **Hidden from Recent Apps**: Vault content won't show in recent apps list

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Kotlin 1.9.22 |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Database | Room |
| Navigation | Navigation Compose |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 34 (Android 14) |

## Screenshots

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│                 │     │                 │     │                 │
│     2    4      │     │   Vault 🔒      │     │  Photos (12)    │
│                 │     │                 │     │  Videos (5)     │
│   8   16   32   │     │  [Photos]       │     │  Files (8)      │
│                 │     │  [Videos]       │     │  Apps (3)       │
│      64  128    │     │  [Files]        │     │  Settings       │
│                 │     │  [Apps]         │     │                 │
│   Score: 12580  │     │  [Settings]     │     │                 │
└─────────────────┘     └─────────────────┘     └─────────────────┘
     Game Screen           Vault Gate           Vault Home
```

## Project Structure

```
com.gamevault/
├── GameVaultApp.kt              # Application class
├── MainActivity.kt              # Entry point
├── di/
│   └── AppModule.kt            # Hilt DI module
├── data/
│   ├── local/
│   │   └── Database.kt         # Room database
│   └── repository/
│       └── VaultRepository.kt   # Repository implementation
├── domain/
│   ├── model/
│   │   ├── GameState.kt        # 2048 game state
│   │   ├── VaultItem.kt        # Hidden item model
│   │   └── HiddenApp.kt        # Hidden app model
│   └── usecase/
│       └── GameEngine.kt       # 2048 game logic
└── presentation/
    ├── common/theme/           # Material 3 theme
    ├── navigation/             # Navigation setup
    ├── game/
    │   ├── GameScreen.kt       # 2048 game UI
    │   └── GameViewModel.kt    # Game state management
    └── vault/
        ├── VaultScreen.kt      # Main vault UI
        ├── VaultViewModel.kt   # Vault logic
        ├── PatternSetupScreen.kt
        ├── PinSetupScreen.kt
        ├── FileViewerScreen.kt
        ├── AppListScreen.kt
        └── SettingsScreen.kt
```

## Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 21
- Android SDK with API 34

### Build
```bash
./gradlew assembleDebug
```

### Install
```bash
./gradlew installDebug
```

Or copy `app/build/outputs/apk/debug/app-debug.apk` to your device.

## How It Works

### Hiding Apps
1. Open vault via secret pattern
2. Go to "Apps" tab
3. Tap the eye icon to hide any app
4. Hidden apps disappear from your launcher
5. Launch them anytime from within the vault

### Hiding Files
1. Grant storage permissions when prompted
2. Go to Photos/Videos/Files tab
3. Tap the + button
4. Select files to hide
5. Files are moved to secure vault storage

### Secret Unlock Pattern
The vault is unlocked by tapping 4 corners of the game board in sequence:
- Top-Left (TL)
- Top-Right (TR)
- Bottom-Left (BL)
- Bottom-Right (BR)

You can also set a custom pattern in Settings.

### Decoy PIN
Set up a decoy PIN in Settings. When entered, it shows a fake vault with fake content while the real vault remains protected.

## Privacy

- All data stored locally on device
- No cloud sync (by design)
- No analytics or tracking
- No internet permission required
- Vault content hidden from recent apps
- Encrypted file storage (planned)

## Roadmap

### Completed
- [x] 2048 game core
- [x] Pattern/PIN unlock
- [x] File hiding (photos, videos, docs)
- [x] App hider
- [x] App launcher
- [x] Biometric unlock (UI ready)
- [x] Decoy vault (fake content)
- [x] Intruder photo capture
- [x] Settings screen
- [x] App list with search
- [x] Vault storage info

### In Progress
- [ ] Folder organization for files
- [ ] Thumbnails display
- [ ] Encrypted file storage

### Planned
- [ ] Cloud backup to Google Drive
- [ ] App lock (lock individual apps with vault password)

## Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- 2048 game concept by [Gabriele Cirulli](https://github.com/gabrielecirulli/2048)
- Built with [Jetpack Compose](https://developer.android.com/compose)
- Icons from [Material Design](https://material.io/design)

---

**Made with privacy in mind**
**Author:** powds
**Repository:** https://github.com/powds/gamevault-vault