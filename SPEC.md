# GameVault - Specification Document

## 1. Project Overview

**Project Name:** GameVault
**Type:** Android Vault App disguised as 2048 Puzzle Game
**Core Functionality:** A privacy vault app that disguises as a 2048 puzzle game, allowing users to hide files (photos, videos, documents), hide apps, and store private content behind a game interface.

**Target Users:**
- Privacy-conscious Android users
- Users who want to hide private content from phone viewers
- Users who want a secure, disguised vault application

**Android Version Support:** API 26+ (Android 8.0+)

---

## 2. Technology Stack & Choices

### Framework & Language
- **Language:** Kotlin 1.9.22
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 34 (Android 14)
- **Compile SDK:** 34

### Key Libraries/Dependencies
- **UI:** Jetpack Compose (Material 3)
- **Architecture:** MVVM + Clean Architecture
- **DI:** Hilt
- **Database:** Room (for vault metadata)
- **Coroutines:** Kotlin Coroutines + Flow
- **File Access:** Android SAF (Storage Access Framework) + MediaStore
- **App Access:** PackageManager for hiding apps
- **Encryption:** Android Keystore + AES-256 for file encryption
- **Image Loading:** Coil (Compose)
- **Video Player:** ExoPlayer / Media3
- **Biometric:** AndroidX Biometric
- **Camera:** CameraX (for intruder capture)

### State Management
- ViewModel + StateFlow
- Repository pattern for data layer

### Architecture Pattern
- **Clean Architecture (3 layers):**
  - Presentation (Compose UI + ViewModels)
  - Domain (Use Cases + Repository Interfaces)
  - Data (Repository Implementations + Room + File System)

---

## 3. Feature List

### MVP Features (Phase 1) - COMPLETED
1. **2048 Game Core** - DONE
   - Classic 2048 puzzle mechanics
   - Swipe gestures for tile movement
   - Score tracking
   - Game state persistence

2. **Vault Disguise System** - DONE
   - Secret unlock: tap 4 corners (TL -> TR -> BL -> BR)
   - Fallback: numeric PIN
   - Pattern lock (3x3 grid draw)

3. **File Hiding** - PARTIAL
   - Hide photos (JPG, PNG, GIF, WEBP)
   - Hide videos (MP4, MKV, AVI, MOV, WEBM)
   - Hide documents (PDF, DOC, DOCX, XLS, XLSX, PPT, TXT)
   - Basic file management

4. **Built-in Viewers** - PARTIAL
   - Photo viewer with zoom
   - Video player with basic controls
   - PDF viewer

5. **App Hider + Launcher** - DONE
   - List installed apps
   - Hide apps from launcher
   - Launch hidden apps from within vault

6. **File Browser** - PARTIAL
   - Browse device storage
   - Import files to vault

### Phase 2 Features - IN PROGRESS
7. **Categories/Folders** - PENDING
   - Organize hidden content into folders
   - Sort by date, type, size

8. **Search** - PENDING
   - Search files by name
   - Search apps by name

9. **Thumbnails** - PENDING
   - Generate thumbnails for hidden photos/videos

### Phase 3 Features - COMPLETED
10. **Cloud Backup** (Google Drive) - PENDING
11. **Decoy Profile** (fake vault with fake content) - DONE
12. **Break-in Alert** (capture photo on wrong password) - DONE
13. **Biometric Authentication** - DONE

---

## 4. UI/UX Design Direction

### Overall Visual Style
- Material Design 3
- Clean, modern gaming aesthetic
- 2048 game as the primary visible interface

### Color Scheme
- Primary: Orange (#EDC22E) - classic 2048 tile color
- Secondary: Board gray (#BBADA0) - 2048 board
- Background: Light cream (#FAF8EF) - classic 2048 background
- Vault UI: Dark theme (#1A1A2E) for privacy feel
- Accent: Red (#E94560) for vault accent

### Layout Approach
- **Surface 1 (Game):** Full 2048 game experience - normal gameplay
- **Surface 2 (Vault):** Hidden behind game unlock, dark themed file/app manager
- Navigation: Bottom tab navigation within vault (Files, Apps, Settings)

### App Icon
- 2048 puzzle game icon (NOT a vault/lock icon - for stealth)
- PNG icons generated via rsvg from SVG source
- Adaptive icon support for Android 8+

---

## 5. Security Architecture

### Authentication Methods
- **Primary:** Tap 4 corners sequence (TL -> TR -> BL -> BR)
- **Secondary:** Pattern lock (draw on 3x3 grid)
- **Fallback:** 4-6 digit PIN
- **Optional:** Biometric (fingerprint)

### Break-in Detection
- Capture photo on wrong PIN/pattern attempts
- Store intruder photos for review
- Configurable attempt limit before capture

### Decoy Vault
- Fake vault accessible with decoy PIN
- Shows fake content (e.g., a few innocent photos)
- Real vault protected by real PIN

### Encryption
- AES-256-GCM for file encryption (planned)
- Android Keystore for key management (planned)
- Each vault item encrypted with unique key derived from master password (planned)

### Data Storage
- Files stored in app-private directory
- Metadata stored in Room database
- SharedPreferences for security settings

---

## 6. Implementation Status

### COMPLETED
- [x] 2048 game core with swipe controls
- [x] Corner tap vault unlock mechanism
- [x] Pattern lock setup and verification
- [x] PIN lock setup and verification
- [x] Biometric unlock (AndroidX Biometric)
- [x] App hider (hide apps from launcher)
- [x] App launcher (launch hidden apps)
- [x] File viewer (photos, videos, documents)
- [x] Settings screen
- [x] Decoy vault with fake content
- [x] Intruder capture (photo on wrong attempt)
- [x] 2048 puzzle app icon (PNG + adaptive)

### IN PROGRESS
- [ ] File import/export
- [ ] Encrypted file storage
- [ ] Folder organization

### PENDING
- [ ] Cloud backup to Google Drive
- [ ] Thumbnails for media
- [ ] Search functionality
- [ ] App lock (lock individual apps)

---

## 7. Repository Structure

```
gamevault/
├── SPEC.md
├── README.md
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── java/com/gamevault/
│       │   ├── GameVaultApp.kt
│       │   ├── MainActivity.kt
│       │   ├── di/
│       │   ├── data/
│       │   │   ├── local/
│       │   │   │   └── Database.kt
│       │   │   └── repository/
│       │   │       └── VaultRepository.kt
│       │   ├── domain/
│       │   │   ├── model/
│       │   │   └── usecase/
│       │   │       └── GameEngine.kt
│       │   └── presentation/
│       │       ├── common/theme/
│       │       ├── game/
│       │       ├── navigation/
│       │       └── vault/
│       └── res/
│           ├── values/
│           └── mipmap-*/
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
└── local.properties
```

---

## 8. Build & Distribution

### Build Commands
```bash
./gradlew assembleDebug     # Debug APK
./gradlew assembleRelease   # Release APK
./gradlew installDebug      # Install on connected device
```

### APK Location
`app/build/outputs/apk/debug/app-debug.apk`

### Google Play Store
- Package name: com.gamevault
- Category: Tools > Privacy
- Content rating: Everyone
- Price: Free (with optional premium features)
- Requires: $25 one-time developer registration