# AtLas App

A native Android Java starter for an AI-assisted web-design workspace.

## Version information

| Item | Information |
|---|---|
| App version | **1.0** |
| Android version code | `1` |
| Version type | **Development version** |
| Release status | In development |
| Application ID | `com.atlas.app` |

**AtLas 1.0 is the current development version.** The app is still in development and this does not indicate that a signed release APK has been published. Update the version name and increment the version code in `app/build.gradle` when preparing a new build.

## Features

- Native dark-themed UI built in Java.
- Generate a complete HTML page using the OpenRouter Chat Completions API.
- User-configurable OpenRouter API key and model ID.
- API key encrypted with AES-GCM using a key held in Android Keystore; encrypted values are stored in private app preferences. Android backup is disabled to avoid restoring ciphertext without its Keystore key.
- Android Storage Access Framework folder picker (`ACTION_OPEN_DOCUMENT_TREE`) for a folder the user explicitly chooses.
- Browse selected folder's top-level files and open HTML, CSS, JS, JSON, TXT, and Markdown files for editing.
- Save edits to the opened document URI, or save a generated website as `atlas-website.html` in the selected folder.
- Ask AI to revise an HTML page; existing HTML is sent to OpenRouter only when you explicitly start an AI edit.
- HTML preview in an Android WebView.

## Versioning

AtLas follows a simple version format:

- **Version name** (for example, `1.0`): the version shown to users.
- **Version code** (for example, `1`): an integer used by Android to distinguish app builds. Increase it for every new Android release.

When changing the app version, update `versionName` and increase `versionCode` in `app/build.gradle`.

## Changelog

### 1.0 — Development Version

- Established the initial AtLas Android app baseline.
- Added the AI-assisted HTML generation and editing workflow using OpenRouter.
- Added API key settings and encrypted local key storage.
- Added workspace folder selection, text-file editing, saving, and HTML preview.

## Build

Open this folder in Android Studio and sync Gradle. The project uses Android Gradle Plugin 8.7.3, compile SDK 35, Java, and AndroidX DocumentFile 1.0.1. Android Studio must have a compatible JDK and Android SDK installed.

## First use

1. Install and launch AtLas.
2. Open Settings (gear icon).
3. Create an API key at https://openrouter.ai/keys and enter it in AtLas.
4. Choose an OpenRouter model ID, for example `openai/gpt-4o-mini` (availability and pricing depend on your OpenRouter account and selected model).
5. Choose a workspace folder using Android's folder picker and grant access.
6. Describe a website on the home screen to generate it, or open an HTML file from the workspace and tap **Ask AI to edit this HTML**.

## Security and privacy notes

- The app does not upload workspace files in the background. Opening a file reads it locally. AI editing sends the current HTML and instruction to OpenRouter when you tap the AI edit action.
- OpenRouter API requests require internet connectivity and may incur provider/model charges.
- The API key is encrypted at rest using Android Keystore. No client-side storage method can protect a key from a fully compromised device; users can remove the saved key from Settings.
- Generated HTML and JavaScript are previewed in a WebView. Only open or preview code you trust. The WebView has file and content access disabled, but generated pages can still run JavaScript and may load network resources.
- The workspace view lists files and directories at the selected folder's top level. Choose a subfolder separately through the Android folder picker if you want it to be the active workspace.
- This is a starter app, not a full autonomous agent: it currently edits HTML pages with AI, while other supported text files can be edited manually.
