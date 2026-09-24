# Accord migration plan

## Goal and fixed constraints

Bring Accord's non-UI behavior and local persistence toward the current Gramophone implementation while retaining Accord's Apple Music for Android-inspired interface.

- Keep Accord's layouts, themes, Activities, Fragments, navigation, and styling. Adapt functionality to Accord's UI rather than replacing its screens with Gramophone UI.
- Keep `minSdk = 31` and `applicationId = uk.akane.accord`.
- Match Gramophone's current local storage behavior for playlists, favorites, and related state. The user will uninstall old Accord and install on a device without Accord installed; do not implement Room-data migration or legacy-data compatibility.
- Prioritize everyday media-library and playback behavior. USB audio and detailed audio diagnostics are optional work after core parity.
- Treat the app as local/offline; streaming is not in scope unless the user later changes that requirement.

## How to execute this plan

Run one numbered phase per agent session, in order. The phases are incremental: later phases intentionally build on completed earlier work, but each phase is a bounded, coherent change with its own deliverable and acceptance criteria. Start each session by reading this file and both project repositories' current guidance and Git status. Implement only the requested phase; do not begin the next phase as extra work. If a prerequisite is unmet or the current source invalidates a planned assumption, document the finding and stop for a decision rather than silently widening scope.

For Accord Legacy migration work, use `feature/migration` as the integration branch. Create each phase branch from the latest `feature/migration`, implement and validate only that phase, then merge it back after review. Do not implement migration phases directly on `alpha` or `feature/migration`. See `AccordLegacy/docs/migration/WORKFLOW.md` for the branch procedure.

Each implementation phase must:

1. Recheck current Gramophone source, dependencies, manifest/resources, and relevant Accord call sites; the reference project changes over time.
2. Make a cohesive, reviewable change limited to that phase. Preserve unrelated work in either repository.
3. Run phase-appropriate builds/tests and the manual checks stated below. Playback and storage behavior require runtime verification, not compile-only evidence.
4. Update the phase checklist and record what changed, checks run/results, and any follow-up at the end of this file before handing off. Mark a phase complete only when its completion criteria and verification pass. Leave it in progress and record blockers otherwise.

Phases are sequential: Phase 1 defines the verified scope; Phase 2 establishes the build baseline; Phases 3–6 port core behavior; Phase 7 integrates and validates the core migration; Phase 8 is optional. Complete and verify each prerequisite phase before starting the next. Each phase should leave Accord buildable and its completed behavior working. Integration changes that naturally belong to the current phase are expected; if a phase reveals that an earlier decision must be redesigned, record the reason and proposed adjustment before proceeding.

### Phase tracker

- [x] Phase 1 — Feature inventory and implementation brief
- [x] Phase 2 — Build and dependency baseline
- [x] Phase 3 — Library reading and browse integration
- [ ] Phase 4 — Gramophone storage for playlists and favorites
- [ ] Phase 5 — Core playback behavior
- [ ] Phase 6 — Lyrics and remaining everyday fixes
- [ ] Phase 7 — Core parity regression and completion review
- [ ] Phase 8 — Optional audio capabilities (deferred)

### Execution notes

Append one short entry per completed or blocked phase, with date, commit(s) if applicable, checks and results, and unresolved follow-ups. Keep source-of-truth decisions in “Goal and fixed constraints”; update that section if the user changes scope.

| Phase | Status | Notes |
|---|---|---|
| 1 | Complete | 2026-09-23: Inspected clean Accord `alpha` and Gramophone `beta` checkouts, manifests/build files, current reader/storage/playback/lyrics/search sources, and history. Inventory committed as Accord `36896a3f` in `AccordLegacy/docs/migration/phase-1-feature-inventory.md`; matrix below is the planning summary. Research only; no app code changed. Follow-up: Phase 2 must establish Accord's build baseline before ports. |
| 2 | Complete | 2026-09-24: Implemented on `migration/phase-2-build-baseline`, commit `f4993a55`; fast-forwarded into `feature/migration`. Baseline before changes passed `assembleDebug` and `testDebugUnitTest` after creating the documented ignored local `package.properties` with `releaseType=SelfBuilt`; corrected the README example because its quoted value is rejected by the build. User selected compile/target SDK 37, so upgraded Gradle 9.4.1, AGP 9.2.1, built-in Kotlin/KGP 2.3.0, KSP 2.3.12, and published Media3 1.10.1; Accord remains Java 17, minSdk 31, applicationId `uk.akane.accord`. Added the required Android SDK components to CI, updated the Gradle distribution checksum, and migrated RecyclerView resource source-set DSL for AGP 9.2.1. Checks passed: `./gradlew help`; `./gradlew build --dry-run`; `./gradlew assembleDebug testDebugUnitTest assembleRelease`; all with `releaseType=SelfBuilt`. API 37 emulator smoke test also passed: debug APK launched, displayed the music/audio permission prompt, accepted the grant, rendered Accord Home, remained running, and showed no AndroidRuntime errors. Compiler deprecation/nullability warnings remain noted in the build output. |
| 3 | Complete | 2026-09-24: Completed on `migration/phase-3-library-browse`; commits `967a4856` and `119cf695`, pushed to origin. Adapted the current Accord MediaStore reader to Gramophone `MediaStore:<id>` identity/content-URI behavior, display-name title fallback, duration/type/author/rating metadata, ancestor-aware independent black/white lists, and automatic debounced MediaStore invalidation. Serialized/coalesced scans and made search observe library refreshes; blank queries show the sorted loaded library. Accord category fragments/adapters and sort controls remain the presentation path. Room-backed playlist/favorite state remains for Phase 4. `./gradlew :app:assembleDebug`, `./gradlew :app:testDebugUnitTest`, and `git diff --check` passed. Runtime verification: started `Pixel_10_Pro_XL` (API 37), uninstalled/reinstalled debug app, granted the fresh-install audio permission, and scanned emulator OGG media. Verified duration filtering (60 seconds yielded no short test tracks; setting 0 loaded them), blacklist exclusion (2 songs remained from the non-blacklisted folder), whitelist inclusion (1 song from the selected folder), songs/albums/artists/genres/dates browse categories, sorting-menu selection, title search, blank-query results, and live search/library update after adding media while the app was open. AndroidRuntime log was empty after the final changes. Image permission was not granted; normal library scan/browse worked without it. |
| 4 | Not started | |
| 5 | Not started | |
| 6 | Not started | |
| 7 | Not started | |
| 8 | Deferred | |

### Verified feature matrix (Phase 1 output)

Phase 1 fills this table with concise source locations, disposition, and verification notes. Use the detailed source-verified integration notes in `AccordLegacy/docs/migration/phase-1-feature-inventory.md` alongside this summary for later implementation phases.

| Area/capability | Accord status | Current Gramophone source | Disposition and Accord adaptation | Verification |
|---|---|---|---|---|
| Library scan, metadata, rescan | Present: `logic/utils/MediaStoreUtils.kt` queries MediaStore and builds `MediaStoreUtils` models; `GramophoneApplication`/`LibraryViewModel` expose results. | `uk/akane/libphonograph/reader/Reader.kt` (MediaStore query, metadata, IDs/maps, album/artist/genre/date/folder construction); `SimpleReader.kt`; `FlowReader.kt` (invalidation and refresh). `GramophoneApplication.kt` owns reader flows. | Port the current reader/model behavior in Phase 3; bridge results into existing `MediaStoreUtils`/`LibraryViewModel` or replace model usage coherently while retaining Accord fragments/adapters. Reader uses published `io.github.nift4.mediastorecompat:mediastorecompat`; no Room dependency. Honor audio permission and API guards, keep minSdk 31. | Build; API 31+ fresh-install scan and rescan with audio permission; compare songs and metadata across media changes. |
| Filters, blacklist/whitelist, enhanced cover reading | Present: `MediaStoreUtils.kt` scan logic and `ui/fragments/settings/BlacklistSettingsFragment.kt`, `ui/adapters/BlacklistFolderAdapter.kt`; permission declarations in Accord manifest include audio and optional images. | `Reader.kt` handles minimum duration, normalized blacklist/whitelist paths, metadata filtering and enhanced cover reading; `SimpleReader.kt` forwards configuration. `FlowReader.kt` observes updates. | Adopt reader filtering semantics and supported cover behavior; retain Accord blacklist settings UI, preferences, and permissions, only adding permission/manifest requirements proven necessary. | API 31+ and 33+ permission checks; verify blacklisted/whitelisted folders, minimum duration and optional cover access. |
| Browse categories and item sorting | Present: Accord fragments/adapters cover songs, albums, artists, genres, dates, folders, and playlists; `ui/adapters/Sorter.kt` and `LibraryCategoryAdapter.kt` provide sorting. | `logic/LibraryTreeLoader.kt` maps reader domain items to categories and applies existing adapter `Sorter`; `reader/Reader.kt` constructs the category models. Gramophone itself retains View/adapters for this path; Compose UI is not needed. | Keep Accord browse/navigation, category affordances, item cards, and sort menus. Port only library data/behavior and reconcile sorting choices via current Accord `Sorter` and adapters. | API 31+ category inventory and representative sort modes; compare category contents/counts before and after refresh. |
| Search and suggestions | Present: `ui/fragments/SearchFragment.kt` filters loaded songs by title, album, artist; `ui/MainActivity.kt`/menus are entry points. | `logic/LibraryTreeLoader.kt:getSearchResult/searchForMediaItem`; `logic/SearchSuggestionsProvider.kt`; manifest searchable provider/intent metadata. Tree search currently filters title/album/artist and applies saved search sorting. | Preserve Accord search screen and typography. Align filtering semantics; assess adopting platform suggestions/search intents only if their required contract is useful to Accord, with manifest changes isolated. | Search title/album/artist, empty query, case-insensitivity, sorting; intent/suggestion checks only if adopted. |
| Playlists and favorites | Present: public MediaStore playlists read in `MediaStoreUtils.kt`; private Room-backed list/favorite state in `logic/data/db/*`, `logic/utils/DatabaseUtils.kt`, and `LibraryViewModel.privatePlaylistList`; UI callers include playlist/favorite actions and adapters. | `uk/akane/libphonograph/reader/Reader.kt:fetchPlaylists/readPlaylist`; `reader/FlowReader.kt` observes playlists; models `items/Playlist.kt`, `dynamicitem/Favorite.kt`, `dynamicitem/RecentlyAdded.kt`; read/write and favorite mutation in `manipulator/ItemManipulator.kt` and serialization in `manipulator/PlaylistSerializer.kt`. Persisted playlists/favorites are MediaStore playlist/file-backed; `mediastorecompat` handles scoped-storage operations. | Phase 4: use Gramophone's current MediaStore/file-backed storage as authoritative, adapt Accord playlist and favorite UI/actions; remove Room only after all callers are redirected. No Room data migration or compatibility. Add dependency only after Phase 2 confirms compatible version. | Fresh install: create/rename/edit/delete playlist; add/remove/reorder entries and favorites; activity recreation, process restart, and library rescan. Check scoped-storage consent paths on API 31+ and newer API. |
| Queue, everyday playback, shuffle/repeat, timer, persistence | Present: `logic/GramophonePlaybackService.kt`, Media3 session/player, `CircularShuffleOrder.kt`, `LastPlayedManager.kt`, player bottom sheet and queue UI. | `logic/GramophonePlaybackService.kt` (Media3 service/session, audio attributes, commands, callbacks); `logic/QueueBoard.kt` (multiple/pinned queues); `logic/utils/LastPlayedManager.kt` (restore queue/repeat/shuffle); `logic/GramophoneExtensions.kt` timer and queue controller APIs; `logic/utils/GramophoneShuffleOrder.kt`. | Port behavior in Phase 5 in slices; maintain Accord's bottom sheet and queue screens, bridge service commands/state as needed. Keep local playback; do not add network functionality. Evaluate custom Media3 fork only if a selected feature demonstrably requires it; current app declares published Media3 1.10.1. | Runtime: play from browse/search/playlist; next/previous, seek, queue edits, shuffle/repeat, timer, service/background continuity, restore after process death. |
| Lyrics | Present: `logic/utils/LrcUtils.kt`, service lyric loading and bundle contract, `GramophoneExtensions.getLyrics`, lyric fragments/widget and Accord XML styling. | `logic/utils/SemanticLyrics.kt` (semantic synced/unsynced and word-level parsing); `logic/utils/LrcUtils.kt` (file/embedded lyric loading and parser integration); service lyric timing/delivery in `GramophonePlaybackService.kt`; `logic/ui/MeiZuLyricsMediaNotificationProvider.kt`. | Phase 6: adopt accepted parser/model and service-to-UI contract changes while adapting to Accord lyric views/widget and preserving their presentation. | Regression samples for timed/untimed LRC, supported embedded/subtitle formats, seek/synchronization, empty/malformed input, and existing lyric UI interactions. |
| Background playback, notification and audio interruptions | Present: `GramophonePlaybackService.kt` is declared as exported foreground media-playback service; Accord manifest declares foreground media playback and wake lock; Media3 `MediaButtonReceiver` present. | `logic/GramophonePlaybackService.kt` MediaLibraryService setup, notification provider, audio attributes/focus, callbacks and service lifecycle; `logic/ui/MeiZuLyricsMediaNotificationProvider.kt`; manifest service declaration. | Phase 5: port applicable service/session fixes and notification behavior; preserve Accord service identity/UI and offline permission policy. Review newer target-SDK foreground-service requirements against Accord manifest rather than wholesale copying. | Runtime foreground/background playback, notification controls, focus loss/gain, unplug/headset disconnect, process/service recreation on API 31+. |
| External controls, search/play intents, file/playlist intents | Present: manifest exposes launcher/MUSIC_PLAYER and Media3 media-button receiver; service is a MediaLibraryService. No evidence of Gramophone's full intent/provider surface in Accord manifest. | Gramophone `AndroidManifest.xml` declares SEARCH/MEDIA_SEARCH/MEDIA_PLAY_FROM_SEARCH/Google search and shuffle actions, browsable playlist VIEW, song/playlist pickers, audio preview, searchable provider, album-art provider and media-button receiver; implementations in `ui/MainActivity.kt`, `logic/SearchSuggestionsProvider.kt`, `logic/GramophoneAlbumArtProvider.kt`, picker activities and `LibraryTreeLoader.kt`. | Decide/port only intent contracts needed by Accord, preserving its Activity/navigation and app ID. Prioritize MediaSession/system controls; no Compose or Gramophone app identity/config. | Explicit intent-resolution and end-to-end launch/play checks for each adopted action; notification/system media-button checks. |
| Optional specialized audio (USB, ReplayGain, format/route diagnostics) | Some settings/audio UI and FFmpeg decoder exist; confirm individual Accord status if selected. Not required for everyday core parity. | `logic/utils/LibusbAudioOutputProvider.kt`, `PostAmpAudioOutputProvider.kt`, `ReplayGainAudioProcessor.kt`, `ReplayGainUtil.kt`, `AudioFormatDetector.kt`, `AfFormatTracker.kt`, `BtCodecInfo.kt`, `MediaRoutes.kt`; Gramophone `hificore` and `misc/*` modules/native code. | Defer all specialized output/diagnostics to optional Phase 8. Do not add native modules, hificore or customized Media3 as part of core phases. | Define dependencies, Accord controls and device/runtime checks only if a feature is selected after Phase 7. |

#### Phase 1 implementation brief and integration decisions

- Feature work should flow through Accord's existing View/Fragment/adapter contracts. `ui/LibraryViewModel.kt`, `logic/utils/MediaStoreUtils.kt`, `ui/fragments/SearchFragment.kt`, `ui/adapters/*`, `logic/GramophonePlaybackService.kt`, and lyric views/widget are the primary Accord seams; do not port Gramophone Compose screens/settings/themes.
- Library port target is current `uk.akane.libphonograph` reader (`Reader`, `SimpleReader`, `FlowReader`, item models), not a blind replacement of UI-bound classes. Its compatibility boundary includes Media3 `MediaItem`, MediaStore, coroutines/flows, and published MediaStoreCompat. Validate the resolved dependency and build baseline in Phase 2 before choosing versions.
- Playlist/favorite persistence target is the current Gramophone reader/manipulator model. It is file/MediaStore playlist-backed, not Room. Accord's Room entities/DAOs and `DatabaseUtils` remain in place until Phase 4 redirects every caller; no migration is planned.
- Playback parity is broader upstream than Accord's current branch (queue board and pinned/multiple queues, rating/favorite session buttons, improved timer/lyrics notification paths). Phase 5 should select everyday capabilities, adapt the existing player UI, and prove runtime behavior; specialized audio stays deferred.
- Manifest comparison shows both apps have media playback service and media-button support, while current Gramophone additionally declares search and media intents, picker activities/providers, optional permissions/features, and album-art support. Each addition must be justified by a selected Accord flow; retain `applicationId = uk.akane.accord`, `minSdk = 31`, and offline behavior.
- Phase 1 is complete as source inventory and scope definition only. No clarification is currently needed; revisit a decision if later implementation evidence invalidates this verified brief.

## Phase 1 — Feature inventory and implementation brief

**Prerequisite:** none. **Depends on:** current source in both projects.

1. Inspect current `AccordLegacy` and `Gramophone` branches, working-tree status, source, manifests, resources, build files, and recent history before implementation. Gramophone evolves; validate every reference against its current checkout.
2. Build a feature matrix covering library scan/filtering, browse categories, sorting, search, playlists, favorites, queue behavior, playback, lyrics, background playback, notifications, external media controls, intents, and relevant integrations.
3. For each gap, record the Gramophone implementation and dependencies, Accord entry points, required Accord-style UI affordance, platform constraints, and verification method.
4. Mark specialized audio features (including USB audio and detailed audio diagnostics) as deferred nice-to-haves.
5. Add a concise, source-verified feature matrix to the execution notes below, grouping capabilities into library/browse, playlists/favorites, playback, lyrics, and optional audio. For each item record Accord status, Gramophone source location, planned port/adaptation, and verification approach.

**Complete when:** the feature matrix is recorded in this file; every everyday library/playback capability has a disposition (already present, port, or explicitly out of scope); and each planned port has identified integration points, dependencies, and verification. This phase is research/planning only; it does not modify app code.

## Phase 2 — Establish a buildable dependency baseline

**Prerequisite:** Phase 1 is complete and the feature matrix is recorded. **Depends on:** Phase 1 scope decisions.

1. Capture the current Accord build/test baseline before changing build configuration.
2. Compare Accord and Gramophone Gradle wrapper, AGP, Kotlin, Java toolchain, compile/target SDK, and dependency versions.
3. Upgrade compatible libraries in cohesive groups. Handle Gradle/AGP/Kotlin/Java changes separately from feature ports so failures remain attributable.
4. Keep Accord's API 31 minimum. Decide compile/target SDK movement independently and validate resulting permissions, manifest requirements, and behavior.
5. Review Gramophone's Media3 composite build/submodule and native modules. Add only the customized Media3 or native components demonstrated to be necessary for a selected core capability; otherwise use compatible published dependencies.

**Complete when:** the agreed Accord build variants compile reproducibly, existing core flows have a baseline, and all adopted dependencies/modules have a documented purpose. Record exact commands and results in the phase notes. Do not start library, storage, or playback ports in this phase.

### Phase 2 completion record (2026-09-24)

- Branch: `migration/phase-2-build-baseline`, based on the latest `feature/migration`.
- Selected SDK level: `compileSdk = 37`, `targetSdk = 37`; retained `minSdk = 31` and `applicationId = uk.akane.accord`.
- Build toolchain: Gradle 9.4.1 (distribution SHA-256 pinned), AGP 9.2.1, AGP built-in Kotlin/KGP 2.3.0, KSP 2.3.12, Java/Kotlin toolchain 17.
- Updated published Media3 ExoPlayer, MIDI, and Session dependencies together to 1.10.1, matching current Gramophone's published Media3 version. No customized Media3 composite build or native module was added.
- CI installs Android platform 37, Build Tools 36.0.0, and NDK 28.0.13004108 before building. The local `package.properties` remains ignored; CI uses `releaseType=CI` and local validation used `releaseType=SelfBuilt`.
- Fixed the README's quoted `releaseType` example, which contradicted the build's validation, and migrated RecyclerView resource source-set declarations to AGP 9's supported directory collection DSL.
- Exact successful commands (run using the repository Gradle wrapper): `./gradlew help`; `./gradlew build --dry-run`; `./gradlew assembleDebug testDebugUnitTest assembleRelease`.
- API 37 target-SDK smoke verification: started `Pixel_10_Pro_XL` (API 37), installed/launched the x86_64 debug APK, granted the requested `READ_MEDIA_AUDIO` permission, confirmed Accord Home rendered and the process remained alive, and observed no `AndroidRuntime` errors. Full library/playback workflow coverage remains in later phases.

## Phase 3 — Library reading and browse integration

**Status: Complete (2026-09-24).** Implemented and verified on `migration/phase-3-library-browse`; see the Phase 3 execution entry above. Playlist/favorite persistence remains intentionally unchanged for Phase 4.

**Prerequisite:** Phase 2 is complete and the agreed Accord build baseline passes. **Depends on:** Phase 1 library scope and Phase 2 dependency decisions.

1. Trace Gramophone's current storage and data ownership for favorites, playlists, library metadata, and persisted state. Confirm details in current code rather than inferring from README.
2. Port Gramophone's current media reader and library data required for scanning, filtering, browsing categories, and search/sorting parity. Keep media-library behavior independent of Accord presentation where practical.
3. Adapt reader output to Accord's existing view-models/adapters and UI contracts. Keep current Accord screens and styling intact.
4. Port only the permissions, manifest entries, resources, and platform-version guards required by the adopted library behavior. Playlist/favorite persistence is Phase 4.

**Complete when:** Accord can scan and rescan local media, apply the adopted filters/blacklists, and display/search/sort the supported library categories through its existing UI. Verify on API 31+ and record test/build results. Playlist/favorite actions may still use the pre-existing implementation until Phase 4.

## Phase 4 — Gramophone storage for playlists and favorites

**Prerequisite:** Phase 3 is complete, so the adapted library model and item identity are established. **Depends on:** Phase 1 storage scope and Phase 3 library integration.

1. Trace Gramophone's current favorite and playlist storage/read-write paths and the Accord screens/actions that access Room through `DatabaseUtils` and related models.
2. Port Gramophone's storage model and operations for playlists and favorites, adapting interfaces for Accord's current UI. Keep the chosen Gramophone model authoritative; do not retain Room as a second source of truth.
3. Redirect every Accord read/write caller to the new model, including playlist creation/editing, add/remove songs, favorite indicators/actions, and refresh/reload behavior.
4. Once no callers use Room, remove `AppDatabase`, DAOs/entities, `DatabaseUtils`, schema-generation configuration, KSP Room compiler usage, and Room dependencies. Do not add a migration from `app.db` or a compatibility path.

**Complete when:** fresh-install runtime checks can create/edit/delete playlists, add/remove playlist items and favorites, and show correct state after activity recreation, process restart, and library rescan using Gramophone's current local-storage behavior; source/build inspection confirms Room is no longer used or included. Record the storage paths inspected and verification results.

## Phase 5 — Core playback behavior

**Prerequisite:** Phases 2–4 are complete. **Depends on:** the library and playlist/favorite models that playback consumes.

1. Compare current Gramophone and Accord playback-service behavior and port feature-matrix items for everyday playback, in cohesive slices.
2. Preserve correct media-session, audio-focus, headset-disconnect, queue, shuffle/repeat, sleep-timer, notification, and background-service behavior as applicable to the agreed scope.
3. Adapt new service commands, metadata models, or callbacks to Accord's existing UI/service boundary. Avoid Gramophone-specific UI state dependencies where a narrower interface serves Accord.
4. Include only matching manifest declarations, permissions, icons/resources, and API guards required by adopted behavior.

**Complete when:** playback works from Accord browse, search, and playlist flows and continues in the background; notification/system media controls, audio focus/interruption, headset disconnect, queue/shuffle/repeat, and timer behavior pass applicable runtime checks on representative API 31+ devices. Record commands, devices/API levels, and results.

## Phase 6 — Lyrics and remaining everyday fixes

**Prerequisite:** Phase 5 is complete and the Accord playback/service boundary is stable. **Depends on:** Phase 1 lyrics/fix scope and Phase 5 playback integration.

1. Compare Gramophone's current lyric parsers and service-to-UI contract with Accord. Port accepted parsing, synchronization, and reliability changes while retaining Accord's lyric UI.
2. Adapt lyric-model changes at the boundary to Accord's lyric views/adapters.
3. Port remaining everyday bug fixes only when listed in the Phase 1 feature matrix; add focused regression coverage where appropriate.

**Complete when:** accepted lyric formats render and synchronize correctly in Accord, existing lyric interactions remain intact, and each additional everyday fix has targeted verification. Record deferred items and results.

## Phase 7 — Core parity regression and completion review

**Prerequisite:** Phases 2–6 are complete. **Depends on:** all core implementation phases and the Phase 1 feature matrix.

1. Run applicable Gradle builds, unit tests, lint/checks, and inspect the diff for accidental UI/theme/app-identity changes.
2. Verify fresh-install permissions and library behavior on API 31+: scan/rescan, filters/blacklists, browse categories, search/sort, playlists/favorites, and persistence after process restart.
3. Verify playback end-to-end: foreground/background operation, notification/lock-screen controls, audio focus/interruption, media buttons, queue/shuffle/repeat, timer, and lyrics.
4. Confirm `minSdk = 31` and `applicationId = uk.akane.accord`. The user uninstalls the prior Accord app; no Room-data migration is expected.
5. Compare actual behavior to the Phase 1 matrix and record any remaining gaps or device-specific failures.

**Complete when:** every core matrix item is verified, explicitly deferred, or has a documented blocker; all available checks pass; and no unresolved regression prevents everyday local-library or playback use.

## Phase 8 — Optional audio capabilities

**Prerequisite:** Phase 7 is complete. **Depends on:** a deliberate decision to select one or more deferred optional features.

Only after core parity is complete, assess ReplayGain, USB audio, detailed audio-format/route diagnostics, and other Gramophone audio features individually. For each accepted feature, identify required native code, Media3 patches, settings, resources, and Accord-style UI before implementation.

**Complete when:** each selected optional feature is implemented and verified end-to-end, or left deferred with its dependency cost recorded. This phase is not a prerequisite for completing core parity.

## Ongoing upstream maintenance

For future Gramophone updates, review upstream changes against the feature inventory. Port functional fixes/features in small slices, adapt rather than copy UI-dependent code, and repeat the affected verification. Revisit the plan when upstream changes storage, Media3 integration, or core playback architecture.
