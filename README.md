# RUSH 47 - Android App (Kotlin)

Free Fire tournament app ka frontend. Screens: Splash → Choose Language →
Login / Sign Up (Forgot Password dialog included), backend PHP APIs se connect
karta hai (register.php, login.php, forgot_password.php, reset_password.php).

## Setup (Android Studio)
1. Android Studio me **Open** karo is `rush47-android` folder ko.
2. Pehli baar open karne par Android Studio khud `gradlew` wrapper files
   (gradle-wrapper.jar) generate/download kar dega — usko "Sync Now" karne dena.
3. `app/src/main/java/com/rush47/tournament/api/ApiClient.kt` me `BASE_URL`
   check kar lena — abhi `https://battle.royalflood.site/backend/` set hai.
   Agar backend kisi doosre path/folder me upload karega to yahi update karna.
4. Run karke emulator/device par test kar sakta hai.

## GitHub + Auto APK Build
1. Is poore folder ko GitHub repo me push kar (`.github/workflows/build.yml`
   already included hai).
2. Jaise hi `main` branch par push hoga, GitHub Actions khud `assembleDebug`
   run karke APK bana dega.
3. Repo ke **Actions** tab me jaake latest run open kar, "Artifacts" section
   me se `rush47-debug-apk` download kar lena.

## Ab tak ke screens
- Splash (logo dikhata hai, phir language ya login pe le jaata hai)
- Choose Language (English / Hindi, SharedPreferences me save hota hai)
- Login (Forgot Password dialog ke saath — backend se OTP request bhejta hai)
- Sign Up (poora form — first/last name, username, mobile+code, email, password, referral)
- Home — abhi sirf placeholder hai, tournaments list/wallet/profile screens
  aage banayenge jab tu bata dega.

## Aage kya banana hai (tu bata de)
- Tournament list + tournament detail + join/slot booking screen
- Wallet (add money, withdraw, transaction history)
- Room ID/Password reveal before match
- Leaderboard / results after match
- Admin panel (web) tournaments create/manage karne ke liye
