# Photo Importer

An Android app that imports and organizes photos from external sources (SD cards, USB drives, etc.) into a structured folder hierarchy based on EXIF capture dates.

## Features

- **Media Scanning** — Recursively scan source folders for JPEG, RAW, and sidecar (XMP) files
- **EXIF Metadata Extraction** — Parse capture timestamps using [metadata-extractor](https://github.com/drewnoakes/metadata-extractor)
- **Duplicate Detection** — Detect and skip duplicate files by comparing source and target
- **Flexible Directory Rules** — Organize photos into `YYYY-MM-DD`, `YYYY-MM`, or `YYYY-MM-DD` folder structures
- **Copy or Move** — Choose between copying files (safe) or moving them (space-saving)
- **Import Progress** — Real-time progress with speed and ETA during import
- **Material 3 UI** — Modern Jetpack Compose interface with edge-to-edge support

## Tech Stack

| Layer      | Technology                                          |
| ---------- | --------------------------------------------------- |
| UI         | Jetpack Compose + Material 3 + Navigation Compose   |
| State      | ViewModel + StateFlow + Coroutines                  |
| Domain     | Pure Kotlin models & logic                          |
| Data       | `DocumentFile` SAF + `metadata-extractor`           |
| Build      | Gradle KTS + Version Catalog                         |
| Min SDK    | 29 (Android 10)                                     |

## Project Structure

```
app/src/main/java/com/captraw/photoimpoter/
├── MainActivity.kt
├── data/
│   ├── FileImportRepository.kt      # Import orchestration
│   ├── FileScanner.kt               # Scanner interface
│   ├── PlatformFileScanner.kt       # SAF-based file scanner
│   ├── ExifReader.kt                # EXIF reader interface
│   └── MetadataExtractorExifReader.kt
├── domain/
│   ├── Models.kt                    # MediaFile, ImportPlan, ImportResult, enums
│   ├── PathGenerator.kt             # Target path generation
│   ├── ExifTimestampParser.kt       # Timestamp extraction logic
│   └── DuplicateDetector.kt         # Duplicate check logic
├── ui/
│   ├── app/PhotoImporterApp.kt      # Root composable + navigation
│   ├── screens/
│   │   ├── HomeScreen.kt            # Source/target folder selection
│   │   ├── ScanResultScreen.kt      # Scanned files preview
│   │   ├── ImportProgressScreen.kt  # Import progress & ETA
│   │   └── ImportReportScreen.kt    # Import summary report
│   └── theme/                       # Color, Type, Theme
└── viewmodel/
    └── PhotoImporterViewModel.kt
```

## Build & Run

```bash
# Build the project
./gradlew build

# Install on connected device / emulator
./gradlew installDebug

# Run unit tests
./gradlew test
```

Requires **JDK 25** and **Android SDK 36**.

## License

AGPL-3.0
