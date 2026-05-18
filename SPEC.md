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
- **Image Loading:** Coil (Compose)
- **Encryption:** AndroidX Security Crypto + Android Keystore
- **Video:** Media3 ExoPlayer

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

### Phase 1 - COMPLETED
1. **2048 Game Core** - DONE
   - Classic 2048 puzzle mechanics
   - Swipe gestures for tile movement
   - Score tracking
   - Game state persistence

2. **Vault Disguise System** - DONE
   - Secret unlock: tap 4 corners (TL -> TR -> BL -> BR)
   - Fallback: numeric PIN
   - Pattern lock (3x3 grid draw)
   - Decoy PIN system
   - Biometric unlock (UI ready)

3. **File Hiding** - DONE
   - Hide photos (JPG, PNG, GIF, WEBP)
   - Hide videos (MP4, MKV, AVI, MOV, WEBM)
   - Hide documents (PDF, DOC, DOCX, XLS, XLSX, PPT, TXT)
   - Hide audio files
   - File picker integration

4. **Built-in Viewers** - DONE
   - Photo viewer with zoom
   - Video player with basic controls
   - PDF viewer

5. **App Hider + Launcher** - DONE
   - List installed apps
   - Hide apps from launcher
   - Launch hidden apps from within vault
   - App list with search functionality
   - Lock/unlock individual apps

### Phase 2 - COMPLETED
6. **Folder Organization** - DONE
   - Create folders
   - Delete folders (items move to root)
   - Rename folders
   - Move items to folders
   - Navigate into/out of folders

7. **Search** - DONE
   - Search files by name
   - Search apps by name
   - Flow-based reactive search

8. **Thumbnails** - DONE
   - Generate thumbnails for hidden photos/videos
   - Cached thumbnail storage
   - Bitmap loading utility

9. **Sort & Filter** - DONE
   - Sort by date, name, size, type
   - Ascending/descending order

### Phase 3 - COMPLETED
10. **AES-256 Encrypted Storage** - DONE
    - CryptoManager using Android Keystore
    - AES-256-GCM encryption for files
    - encryptFile(), decryptFile() operations
    - Encrypted vault directory

11. **Backup & Restore** - DONE
    - Local backup creation (encrypted files + JSON metadata)
    - Local backup restoration
    - Backup listing with metadata
    - Database export/import as JSON
    - Settings preservation in backup
    - Backup screen with create/restore/export/delete

12. **Cloud Backup Infrastructure** - DONE (Placeholder)
    - Google Drive setup placeholder in UI
    - Auto sync toggle (UI ready)
    - Export backup to external directory
    - Import backup from external directory

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

### Data Encryption
- **AES-256-GCM** encryption for all stored files
- Android Keystore for key management
- Keys never exposed to application layer
- GCM mode provides authentication + encryption

---

## 6. Implementation Status

### COMPLETED
- [x] 2048 game core with swipe controls
- [x] Corner tap vault unlock mechanism
- [x] Pattern lock setup and verification
- [x] PIN lock setup and verification
- [x] Biometric unlock (AndroidX Biometric) - UI ready
- [x] App hider (hide apps from launcher)
- [x] App launcher (launch hidden apps)
- [x] File viewer (photos, videos, documents)
- [x] Settings screen with security options
- [x] Decoy vault with fake content
- [x] Intruder capture (photo on wrong attempt)
- [x] 2048 puzzle app icon (PNG + adaptive)
- [x] App list with search functionality
- [x] Vault storage info display
- [x] Auto-lock functionality
- [x] Folder organization (create/delete/rename)
- [x] Sort by date/name/size/type
- [x] Thumbnails generation for photos/videos
- [x] Flow-based reactive search
- [x] AES-256-GCM encrypted file storage
- [x] Backup repository (local backup/restore)
- [x] Backup screen with full UI

### IN PROGRESS
- None - All pending items completed

### PENDING
- None - All features implemented

---

## 7. Repository Structure

```
gamevault/
├── SPEC.md
├── README.md
├── LICENSE
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── java/com/gamevault/
│       │   ├── GameVaultApp.kt
│       │   ├── MainActivity.kt
│       │   ├── di/
│       │   │   └── AppModule.kt
│       │   ├── data/
│       │   │   ├── local/
│       │   │   │   └── Database.kt
│       │   │   └── repository/
│       │   │       ├── VaultRepository.kt
│       │   │       ├── BackupRepository.kt
│       │   │       └── CryptoManager.kt
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

---

## 9. Cloud Backup Notes

### Google Drive Integration
Full Google Drive API integration requires:
1. Google Cloud Console project setup
2. OAuth 2.0 consent screen configuration
3. Drive API enabled in the project
4. API credentials (client_secret.json)

**Current Implementation:**
- Local backup/restore fully functional
- Export backup to external storage (manual cloud sync)
- Import backup from external storage
- UI placeholders for Google Drive (ready for API integration)

**To enable Google Drive:**
1. Go to https://console.cloud.google.com
2. Create project or select existing
3. Enable "Drive API" from Library
4. Configure OAuth consent screen
5. Create API credentials (OAuth 2.0 Client ID)
6. Download client_secret.json
7. Implement Google Sign-In flow

---

## 10. Build Info

**Last Updated:** May 2026
**GitHub:** https://github.com/powds/gamevault
**Author:** powds
**Build Status:** BUILD SUCCESSFUL
**APK Size:** ~20MB
**Kotlin:** 1.9.22
**Min SDK:** 26 (Android 8.0)
**Target SDK:** 34 (Android 14)