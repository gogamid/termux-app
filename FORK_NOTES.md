# Termux fork — three-finger swipe to toggle the soft keyboard

A minimal fork of [termux/termux-app](https://github.com/termux/termux-app) that adds
one feature:

> **Swipe up with three fingers anywhere on the terminal to toggle the on-screen
> keyboard on/off.**

(Swipe-*down* is avoided because it is commonly the system "take screenshot" gesture.)

Because the toggle happens inside Termux itself at the app layer, it works in every
TUI running inside the terminal (vim, tmux, pi, htop, ...) — there is no dependency
on any particular TUI.

## Changes

| File | Change |
| --- | --- |
| `app/src/main/java/com/termux/app/TermuxThreeFingerSwipeDetector.java` | New. Detects a three-finger upward swipe (threshold ~120 dp of average pointer travel) and reports it via a listener. Attached with `View.OnTouchListener`, it always returns `false`, so normal terminal touch handling (scroll, text selection, pinch-zoom, mouse reporting, extra keys) is completely unaffected. |
| `app/src/main/java/com/termux/app/TermuxActivity.java` | Wires the detector to the terminal view in `setTermuxTerminalViewAndClients()`. The gesture calls the existing `TermuxTerminalViewClient.onToggleSoftKeyboardRequest()`, which honours `soft-keyboard-toggle-behaviour` from `~/.termux/termux.properties` (default: show/hide; set it to `enable/disable` for that mode). |

## Building

Debug APKs are built automatically by the GitHub Actions workflow
`.github/workflows/build-debug-apk.yml` on every push (and via manual
`workflow_dispatch`).

1. Push to your fork.
2. Open **Actions** → **Build debug APK** → pick the latest run.
3. Download the **termux-fork-debug-apk** artifact.

Or build locally:

```bash
./gradlew assembleDebug
# APKs land in app/build/outputs/apk/debug/
```

## Installing

- The debug APK is signed with the committed `testkey_untrusted.jks` debug key.
  It has a **different signature** from official Termux builds, so you must
  **uninstall any existing Termux first** (this deletes its data) or the install
  will fail with `INSTALL_FAILED_UPDATE_INCOMPATIBLE`.
- Install with e.g. `adb install app/build/outputs/apk/debug/termux-app_*.apk`.