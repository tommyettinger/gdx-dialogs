# gdx-dialogs
libGDX extension providing cross-platform support for native dialogs.

[![](https://jitpack.io/v/MRZ07/gdx-dialogs.svg)](https://jitpack.io/#MRZ07/gdx-dialogs)

> **This is a maintained fork of [TomGrill/gdx-dialogs](https://github.com/TomGrill/gdx-dialogs)** — actively maintained by [@MRZ07](https://github.com/MRZ07) with full modernization for current libGDX, Android, and Gradle versions.

![Alt text](/assets/dialogs.jpg?raw=true "Examples")

---

## What changed in this fork

### Build system
- Gradle wrapper upgraded from **4.2.1 → 9.6.1**
- Android Gradle Plugin upgraded from **2.3.0 → 8.9.3**
- libGDX upgraded from **1.9.6 → 1.14.0**
- RoboVM upgraded from **2.3.1 → 2.3.23**
- Removed dead `jcenter()` and `mavenLocal()` references; replaced with `google()` + `gradlePluginPortal()` + `mavenCentral()`
- Removed obsolete GWT/MOE Gradle plugins from the root buildscript (they are no longer published); each module now brings its own plugin via its own `buildscript` block
- Replaced deprecated `compile` configuration with `implementation` / `api` everywhere
- Replaced deprecated `maven` publish plugin with `maven-publish` in both `publish.gradle` and `androidpublish.gradle`
- Versions centralised in `gradle.properties` (`gdxVersion`, `roboVMVersion`)
- `ios-moe` module removed (Multi-OS Engine is abandoned)
- `configure(subprojects - project(':android'))` pattern adopted from the official libGDX template
- Java source/target compatibility raised from **1.7 → 11**

### Android
- `compileSdkVersion 23` / `targetSdkVersion 23` → **compileSdk 35 / targetSdk 35**
- `minSdkVersion 9` → **minSdk 21**
- `buildToolsVersion` removed (managed automatically by AGP 8+)
- `namespace` added (required by AGP 8)
- `android.app.ProgressDialog` (deprecated / removed in Android 13) replaced with an `AlertDialog` + `ProgressBar` implementation
- Added `!activity.isDestroyed()` guard in all `show()` calls for lifecycle safety
- `androidpublish.gradle` fully rewritten for `maven-publish` + `singleVariant('release')` (required by AGP 8)

### Desktop (Swing/LWJGL-less fallback)
- All Swing calls moved to `SwingUtilities.invokeLater` for proper Event Dispatch Thread safety on modern JVMs
- Test launcher added (`DesktopDialogTest`) to exercise all three dialog types without a full libGDX window

---

## Supported Platforms

| Platform | Status |
|---|---|
| Android | ✅ |
| Desktop (Swing fallback) | ✅ |
| iOS (RoboVM) | ✅ |
| HTML / GWT | ✅ |
| iOS-MOE | ❌ Removed |

---

## Currently available dialog types

1. **ButtonDialog** — 1–3 button dialog.
2. **ProgressDialog** — Indeterminate progress spinner. Optionally dismissable by the user via `setCancelable(true)`.
3. **TextPrompt** — On-screen text input (plain text or password).

---

## Installation via JitPack

Add JitPack to your root `build.gradle` repositories block:

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}
```

Then add the dependencies you need:

**Core** *(required by all platforms)*
```gradle
implementation 'com.github.MRZ07.gdx-dialogs:gdx-dialogs-core:1.7.1'
```

**Android**
```gradle
implementation 'com.github.MRZ07.gdx-dialogs:gdx-dialogs-android:1.7.1'
```

Copy the [`android/res`](android/res) folder from this project into your Android module and keep the directory structure.  
You may edit [`android/res/values-v11/styles.xml`](android/res/values-v11/styles.xml) to choose a different theme. If you already have a `styles.xml`, merge the relevant entries.

**Desktop** *(Swing-based fallback, works on macOS + LWJGL3)*
```gradle
implementation 'com.github.MRZ07.gdx-dialogs:gdx-dialogs-desktop:1.7.1'
```

**iOS (RoboVM)**

Add to your `robovm.xml`:
```xml
<forceLinkClasses>
    <pattern>com.mrz07.gdxdialogs.ios.IOSGDXDialogs</pattern>
</forceLinkClasses>
```

```gradle
implementation 'com.github.MRZ07.gdx-dialogs:gdx-dialogs-ios:1.7.1'
```

**HTML / GWT**
```gradle
implementation 'com.github.MRZ07.gdx-dialogs:gdx-dialogs-html:1.7.1'
implementation 'com.github.MRZ07.gdx-dialogs:gdx-dialogs-core:1.7.1:sources'
implementation 'com.github.MRZ07.gdx-dialogs:gdx-dialogs-html:1.7.1:sources'
```

Add to your `GdxDefinition.gwt.xml`:
```xml
<inherits name='com.mrz07.gdxdialogs.html.gdx_dialogs_html' />
<inherits name='com.mrz07.gdxdialogs.core.gdx_dialogs_core' />
```

---

## Usage

**Enable**

```java
GDXDialogs dialogs = GDXDialogsSystem.install();
```

**ButtonDialog**

```java
GDXButtonDialog bDialog = dialogs.newDialog(GDXButtonDialog.class);
bDialog.setTitle("Buy an item");
bDialog.setMessage("Do you want to buy the mozzarella?");

bDialog.setClickListener(new ButtonClickListener() {
    @Override
    public void click(int button) {
        // 0 = first added button, 1 = second, 2 = third, -1 = cancelled
    }
});

bDialog.addButton("No");
bDialog.addButton("Never");
bDialog.addButton("Yes, nomnom!");

bDialog.build().show();
```

**ProgressDialog**

```java
GDXProgressDialog progressDialog = dialogs.newDialog(GDXProgressDialog.class);
progressDialog.setTitle("Download");
progressDialog.setMessage("Loading new level from server...");
progressDialog.setCancelable(true); // optional — lets user dismiss by tapping outside
progressDialog.build().show();

// Later:
progressDialog.dismiss();
```

**TextPrompt**

```java
GDXTextPrompt textPrompt = dialogs.newDialog(GDXTextPrompt.class);
textPrompt.setTitle("Your name");
textPrompt.setMessage("Please tell me your name.");
textPrompt.setCancelButtonLabel("Cancel");
textPrompt.setConfirmButtonLabel("Save name");

textPrompt.setTextPromptListener(new TextPromptListener() {
    @Override
    public void confirm(String text) {
        // use the user input
    }

    @Override
    public void cancel() {
        // handle cancel
    }
});

textPrompt.build().show();
```

**OpenGL context in listener callbacks**

Dialog callbacks run on a background thread. If you need to call code that requires the GL thread, wrap it:

```java
dialog.setClickListener(new ButtonClickListener() {
    @Override
    public void click(int button) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                // GL-thread code here
            }
        });
    }
});
```

---

## Creating custom dialogs

1. Define an interface in `:core` — see [`GDXButtonDialog`](core/src/com/mrz07/gdxdialogs/core/dialogs/GDXButtonDialog.java)
2. Implement for Android in `:android` — see [`AndroidGDXButtonDialog`](android/src/com/mrz07/gdxdialogs/android/dialogs/AndroidGDXButtonDialog.java)
3. Implement for Desktop in `:desktop` — see [`DesktopGDXButtonDialog`](desktop/src/com/mrz07/gdxdialogs/desktop/dialogs/DesktopGDXButtonDialog.java)
4. Implement for iOS in `:ios` — see [`IOSGDXButtonDialog`](ios/src/com/mrz07/gdxdialogs/ios/dialogs/IOSGDXButtonDialog.java)
5. Add a fallback in `:core` — see [`FallbackGDXButtonDialog`](core/src/com/mrz07/gdxdialogs/core/dialogs/FallbackGDXButtonDialog.java)

Register your dialog at runtime:

```java
if (Gdx.app.getType() == ApplicationType.Android) {
    dialogs.registerDialog("com.example.MyDialog", "com.example.android.AndroidMyDialog");
} else if (Gdx.app.getType() == ApplicationType.Desktop) {
    dialogs.registerDialog("com.example.MyDialog", "com.example.desktop.DesktopMyDialog");
} else if (Gdx.app.getType() == ApplicationType.iOS) {
    dialogs.registerDialog("com.example.MyDialog", "com.example.ios.IOSMyDialog");
} else {
    dialogs.registerDialog("com.example.MyDialog", "com.example.FallbackMyDialog");
}
```

> **Note:** Every platform-specific implementation must have a no-arg constructor. Android implementations must also have a constructor taking an `Activity` parameter.

---

## Release History

| Version | Notes |
|---|---|
| 1.7.1 | Desktop ProGuard/R8 consumer rules; Gradle 9.4 · JitPack compatibility |
| 1.5.0 | `ProgressDialog.setCancelable(true)` — user-dismissable progress dialogs on Android, Desktop, iOS |
| 1.4.0 | Gradle 9.4 · AGP 8.9.3 · libGDX 1.14.0 · RoboVM 2.3.23 · Android 13+ compat · Swing EDT fix · maven-publish migration · ios-moe removed |
| 1.2.2 | TextPrompt password field support via `setInputType(...)` |
| 1.2.1 | TextPrompt `setMaxLength()` method |
| 1.2.0 | GWT/HTML support |
| 1.1.0 | iOS support, Android ProGuard out of the box |
| 1.0.0 | First stable release |

---

## Reporting Issues

Check the [issue tracker](https://github.com/MRZ07/gdx-dialogs/issues) and open a new issue if your problem is not already listed. Please include steps to reproduce and platform details.

## Contributing

Fork, branch, and submit a pull request to [MRZ07/gdx-dialogs](https://github.com/MRZ07/gdx-dialogs). All contributions welcome.

## License

Apache 2.0 — free to use in commercial and non-commercial projects.
