# Installing & Updating Vyayah (Sideload / Obtainium)

Vyayah is built to be **100% offline and private**. It contains **zero `android.permission.INTERNET`** in its manifest, which means Google Play Store restrictions do not apply, and no code in the app can ever phone home.

---

## Method 1: Automatic Updates via Obtainium (Recommended)

[Obtainium](https://github.com/ImranR98/Obtainium) allows you to install and get seamless, automatic updates directly from GitHub Releases without an app store.

1. Download and install **Obtainium** on your Android phone.
2. In Obtainium, tap **Add App (+)**.
3. Paste the URL of your Vyayah GitHub repository:
   ```text
   https://github.com/<your-username>/vyayah
   ```
4. Enable **"Include Prereleases"** if testing release candidates.
5. Tap **Add**. Obtainium will download the latest APK, verify the release, and install it.
6. Whenever you push a tag (e.g. `v0.1.0`), GitHub Actions builds the APK and Obtainium notifies you to update with one tap.

---

## Method 2: Manual APK Sideload

1. Go to the **Releases** tab of your GitHub repository.
2. Download `app-release.apk` (or `app-debug.apk` from GitHub Actions artifacts).
3. Tap the downloaded APK in your Android file manager to install.

---

## First-Run Permissions & Reliability Setup

### 1. Android 13+ Sideload Restricted Settings
On Android 13, 14, and 15, sideloaded APKs have accessibility and sensitive permissions blocked by default until you permit them:
1. Long-press the **Vyayah** app icon on your home screen → tap **App info** (ℹ️).
2. Tap the **three dots (⋮)** in the top-right corner.
3. Tap **Allow restricted settings**.
4. Authenticate with your fingerprint or PIN.
5. Open Vyayah and grant **SMS permission**.

### 2. OEM Battery Optimization (Xiaomi, Oppo, Vivo, Realme, OnePlus)
Aggressive background task killers on Chinese OEM ROMs can terminate the SMS receiver. To ensure instant transaction capture:
* **Autostart / Background launch:** Enable in *App info → Permissions / Autostart*.
* **Battery saver:** Set to *No restrictions / Unrestricted*.
* **Lock in Recent Apps:** Open the app switcher, long press Vyayah (or tap its menu), and tap the **Lock** icon.

---

## Verifying Offline Privacy

You can verify that Vyayah is truly offline:
1. Open terminal on your computer with `aapt` or `apkanalyzer`:
   ```bash
   apkanalyzer manifest permissions app-release.apk
   ```
2. You will observe that `android.permission.INTERNET` is **completely absent**. The Android OS will physically prevent the app from opening sockets or making network connections.
