# 📜 Release History & Build Trials Log — Vyay (व्यय)

> *"Building a 100% offline, privacy-first personal finance tracker is not a one-shot process. It is a journey of relentless iteration against real-world bank SMS strings, OS sandboxing rules, and live transactions. Every single trial, bug, and improvement is preserved here to celebrate every step of the journey."*

---

## 🧭 The Iteration Index (How Many Times We Tried)

| Trial # | Version Tag | Release Date | Focus Area | Key Problem Encountered in Real Testing | Solution / Outcome | Artifact / APK |
| :---: | :---: | :---: | :--- | :--- | :--- | :---: |
| **8** | [`v0.1.2-alpha`](https://github.com/dot-wasim/Vyay/releases/tag/v0.1.2-alpha) | **Oct 5, 2026** | **Real-World Live Field Testing Fixes** | ₹1336.05 detected as 136; two ₹1000 debits missed 1; SMS character truncation (`Indian Rail W`). | Rebuilt Amount regex with decimal lookahead; added UPI Ref / RRN / UTR deduplication; normalized `Indian Railway 🚂`; added Fun Mode (`🍕🍔`, `🍿🎬`). | [⬇️ APK](https://github.com/dot-wasim/Vyay/releases/download/v0.1.2-alpha/vyay-app.apk) |
| **7** | [`v0.1.1-alpha`](https://github.com/dot-wasim/Vyay/releases/tag/v0.1.1-alpha) | **Oct 5, 2026** | **Build & Room KSP Stabilization** | Room KSP annotation processor conflicting with entity naming; SQLCipher helper functions missing. | Disambiguated `@androidx.room.Transaction`; aligned SQLCipher room dependencies; decoupled CI test & build steps. | [⬇️ Tag](https://github.com/dot-wasim/Vyay/tree/v0.1.1-alpha) |
| **6** | [`v0.1.0-alpha`](https://github.com/dot-wasim/Vyay/releases/tag/v0.1.0-alpha) | **Oct 4, 2026** | **First Public Alpha & Obtainium** | Sideloaded APKs lacked automatic updates and direct download channels. | Configured GitHub Actions CI release pipeline + Obtainium repository integration for direct zero-store auto-updates. | [⬇️ APK](https://github.com/dot-wasim/Vyay/releases/download/v0.1.0-alpha/vyay-app.apk) |
| **5** | [`v0.0.5-alpha`](https://github.com/dot-wasim/Vyay/tree/v0.0.5-alpha) | **Oct 4, 2026** | **Visual Identity & Editorial Branding** | Generic UI aesthetic and lack of cultural identity for the Devanagari roots of Vyay. | Introduced Devanagari **व्यय** adaptive icon crests, Parchment & Ink color palette, and bespoke editorial Material 3 components. | [🏷️ Tag](https://github.com/dot-wasim/Vyay/tree/v0.0.5-alpha) |
| **4** | [`v0.0.4-alpha`](https://github.com/dot-wasim/Vyay/tree/v0.0.4-alpha) | **Oct 4, 2026** | **Trips Mode & Privacy Shield** | Vacation expenses distorted monthly grocery/dining budget velocity. Risk of sensitive data exposure in recent apps switcher. | Created ring-fenced `Trip` database tables; integrated Android `FLAG_SECURE` app-switcher obscuring + Biometric hardware lock. | [🏷️ Tag](https://github.com/dot-wasim/Vyay/tree/v0.0.4-alpha) |
| **3** | [`v0.0.3-alpha`](https://github.com/dot-wasim/Vyay/tree/v0.0.3-alpha) | **Oct 4, 2026** | **Paced Budgets & Spend Velocity** | Users only realized they overspent at month-end when bank balances were depleted. | Implemented daily burn curve velocity algorithms and local background `WorkManager` alerts at 80% and 100% pacing thresholds. | [🏷️ Tag](https://github.com/dot-wasim/Vyay/tree/v0.0.3-alpha) |
| **2** | [`v0.0.2-alpha`](https://github.com/dot-wasim/Vyay/tree/v0.0.2-alpha) | **Oct 4, 2026** | **Bank SMS Parser Engine & OTP Gate** | High risk of storing confidential bank 2FA OTP passwords; non-standard SMS formats across Indian banks. | Built regex template engine for HDFC, SBI, ICICI, Axis, Kotak, PNB + hardcoded `SmsFilter` gatekeeper that drops OTPs before disk write. | [🏷️ Tag](https://github.com/dot-wasim/Vyay/tree/v0.0.2-alpha) |
| **1** | [`v0.0.1-alpha`](https://github.com/dot-wasim/Vyay/tree/v0.0.1-alpha) | **Oct 4, 2026** | **Core Offline Architecture** | Traditional fintech apps upload financial history to third-party ad/lending servers. | Completely stripped `android.permission.INTERNET` from manifest; configured SQLCipher AES-256 hardware Keystore database. | [🏷️ Tag](https://github.com/dot-wasim/Vyay/tree/v0.0.1-alpha) |

---

## 🔬 Deep-Dive Into Field Trials & Bug Fixes

### 🌟 Trial 8 — v0.1.2-alpha (Real-World Live Field Testing)
*Date: October 5, 2026*  
**Commit:** `ea7a4f3` & `d24c4f6`

#### What was tested in real life:
After deploying `v0.1.0-alpha` onto a personal daily driver Android device, real-world transactions were executed and tested against Indian banks and UPI rails.

#### Bugs discovered in the field:
1. **The ₹1336.05 Amount Truncation Bug:**
   - *Problem:* A real ticket payment of ₹1336.05 was detected as only `₹136` (and duplicated).
   - *Root Cause:* The legacy regex used non-greedy matching `[0-9]{1,3}(?:,[0-9]{2,3})*`. For unformatted 4-digit numbers (`1336`), it eagerly grabbed the first 3 digits (`133`) or matched partial sequences. Additionally, the paise formatter was dropping floating points.
   - *Resolution:* Completely re-engineered `AmountParser.kt` with currency-prefix, currency-suffix, and verb-based regex suites that capture complete numerical strings including decimals. `formatPaiseToInr()` now dynamically retains decimal paise when non-zero (`₹1,336.05`).
2. **The "Two Consecutive ₹1,000 Debits" Miss Bug:**
   - *Problem:* On October 1st, two legitimate separate payments of ₹1,000 were made back-to-back. Vyay captured only one.
   - *Root Cause:* 
     1) Bank SMS verbs like `Sent Rs 1000` or `Transferred Rs 1000` lacked `sent`/`transferred` intent flags in `BankSmsParser.kt`.
     2) Deduplication relied on `smsHash` with an overly strict timestamp inequality (`DATE > ?`) in `CatchUpSyncWorker.kt`, dropping messages with identical millisecond timestamps.
   - *Resolution:* Made **Reference Numbers (UPI Ref / RRN / UTR / Txn Id)** the definitive ground truth for transaction uniqueness via `transactionDao.getByUpiRef()`. Two payments of identical amounts with distinct reference numbers are recognized as legitimate and both preserved.
3. **The "Indian Rail W" Bank SMS Truncation Bug:**
   - *Problem:* SMS character limits imposed by banks cut off "Indian Railway" as "Indian Rail W".
   - *Resolution:* Added normalization mapping in `MerchantNormalizer.kt` resolving `Indian Rail W`, `Indian Rail`, and `IRCTC` to clean **`Indian Railway 🚂`**, tagged under Transport.
4. **"Fun Mode" Category Experience:**
   - Replaced boring category names with lively emojis and nuances:
     - `Food & Treats 🍕🍔` (with merchant icons for Swiggy 🍕, Zomato 🍔, McDonald's 🍟, Starbucks ☕)
     - `Entertainment & Fun 🍿🎬` (with BookMyShow 🎟️, PVR 🍿, INOX 🎬, Netflix 🍿, Spotify 🎧)
   - Built a database auto-migration hook in `VyayahDatabase.onOpen` that gracefully upgrades existing user databases without data loss.

---

### ⚙️ Trial 7 — v0.1.1-alpha (Build & Room KSP Stabilization)
*Date: October 5, 2026*  
**Commit:** `324eb0e` & `e09fd44`

#### What was tested:
Setting up continuous integration pipelines on Linux runners using headless Gradle daemon.

#### Bugs discovered:
1. **Room Compiler KSP Ambiguity:**
   - *Problem:* In `Daos.kt`, `@androidx.room.Transaction` collided with the domain model `Transaction` entity, causing Kotlin Symbol Processing (KSP) compilation failures.
   - *Resolution:* Explicitly qualified `@androidx.room.Transaction` and made all DAO queries fully KSP-compliant.
2. **SQLCipher Native Dependency Linkage:**
   - *Problem:* Missing helper methods for SQLite open helper factory.
   - *Resolution:* Corrected Room SQLite OpenHelper bindings and bundled `libc++_shared.so` picks in `build.gradle.kts`.

---

### 📦 Trial 6 — v0.1.0-alpha (First Public Alpha & Obtainium)
*Date: October 4, 2026*  
**Commit:** `09b83cd` & `ab534b9`

#### What was tested:
Releasing the first installable APK without Google Play Store dependency.

#### Highlights:
- Created the GitHub Actions CI workflow to build release artifacts on every push.
- Integrated Obtainium feed compatibility for background auto-updates directly from GitHub Releases.
- Verified zero-network sandboxing on Android 10, 11, 12, 13, and 14 test devices.

---

### 🎨 Trial 5 — v0.0.5-alpha (Visual Identity & Branding)
*Date: October 4, 2026*  
**Commit:** `09b83cd`

#### What was tested:
Designing an app icon and typography that honors the cultural origin of the word **व्यय** (Vyay).

#### Highlights:
- Designed adaptive launcher icons featuring the Devanagari **व्यय** letterform on warm parchment ivory (`#FBFBF9`) with deep ink typography (`#141413`).
- Created high-resolution crest vectors for the splash screen and app headers.

---

### 🏖️ Trial 4 — v0.0.4-alpha (Trips Ring-Fencing & Security)
*Date: October 4, 2026*  
**Commit:** `0ad69e2`

#### What was tested:
Separating temporary vacation spending from long-term home living budgets.

#### Highlights:
- Built the `Trip` entity and Room DAO with active trip toggle.
- Added `FLAG_SECURE` to prevent Android OS task switcher from exposing bank balances to screenshot grabbers.
- Implemented Hardware Biometric unlock via `androidx.biometric.BiometricPrompt`.

---

### 📊 Trial 3 — v0.0.3-alpha (Paced Budgets & Spend Velocity)
*Date: October 4, 2026*  
**Commit:** `c091b8b`

#### What was tested:
Predicting end-of-month financial health before the month finishes.

#### Highlights:
- Formulated the spend velocity equation:
  $$\text{Spend Velocity} = \frac{\sum \text{Expenses Month-to-Date}}{\text{Days Elapsed}}$$
- Configured local periodic WorkManager jobs to notify users at 80% and 100% budget marks without external servers.

---

### 🏦 Trial 2 — v0.0.2-alpha (Bank SMS Parser & OTP Shield)
*Date: October 4, 2026*  
**Commit:** `2dd06dc`

#### What was tested:
Parsing arbitrary SMS strings from diverse Indian banking formats while ensuring OTP safety.

#### Highlights:
- Added regex templates for HDFC, SBI, ICICI, Axis, Kotak, PNB, Canara, IndusInd, and UPI handles.
- Built `SmsFilter` gatekeeper: any SMS containing OTP patterns is dropped instantly before reaching memory or database.

---

### 🔒 Trial 1 — v0.0.1-alpha (Core Offline Architecture)
*Date: October 4, 2026*  
**Commit:** `2dd06dc`

#### What was tested:
Proving that an expense tracker can function 100% without the internet permission.

#### Highlights:
- Removed `<uses-permission android:name="android.permission.INTERNET" />` from `AndroidManifest.xml`.
- Configured 256-bit AES SQLCipher encryption with keys backed by Android Hardware Keystore.
- Established the foundational Room database tables (`transactions`, `categories`, `accounts`).

---

## 📥 How to Access Any Previous Version

All versions and their respective APK builds are permanently archived in the [GitHub Releases](https://github.com/dot-wasim/Vyay/releases) tab.

You can install or compare any historical build:
* **Latest Release:** [v0.1.2-alpha](https://github.com/dot-wasim/Vyay/releases/tag/v0.1.2-alpha)
* **First Alpha:** [v0.1.0-alpha](https://github.com/dot-wasim/Vyay/releases/tag/v0.1.0-alpha)
* **Source Archive:** [Git Commit Log](https://github.com/dot-wasim/Vyay/commits/main)
