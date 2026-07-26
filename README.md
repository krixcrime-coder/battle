# Rush47 — Part 1: Auth Module (Splash, Login, Register, Forgot Password, Home)

Ye 5 screens ready hain, original decompiled app (`com.app.rush47`) ke backend
(`https://api.rush47.in/api/`) ke saath hi kaam karne ke liye banaye gaye hain:

1. **Splash** – session check karke Login ya Home pe bhejta hai
2. **Login** – username/password, backend: `POST {api}login`
3. **Register** – signup form, backend: `POST {api}registrationAcc`
4. **Forgot Password popup** – Login screen ke andar hi dialog, backend: `POST {api}sendOTP`
5. **Forgot Password OTP + New Password screen** – backend: `POST {api}forgotpassword`
6. **Home** – abhi sirf placeholder hai, agla part yahi se banega

## ⚠️ Is part mein jaanbujh kar chhoda gaya hai
- **Firebase (Google sign-in / phone OTP verify)** — original app Firebase use
  karta tha, par uska `google-services.json` decompiled APK mein nahi milta
  (ye file build-time secret hoti hai, APK ke andar store nahi hoti).
  Jab client ye file de dega (Firebase console → Project Settings → apna
  Android app → `google-services.json` download), tab wapas add kar denge.
- Home screen abhi khaali hai — agla part isi ke upar banega (jaisa list
  humne pehle discuss ki thi: Home/Dashboard → Ludo → Lottery → Wallet…)

## GitHub pe push karke APK banana (mobile se)
1. Is poore folder ko ek naye GitHub repo mein push karo (GitHub app / Termux
   / koi bhi Git client se — `git init`, `git add .`, `git commit`, `git push`).
2. Repo mein `.github/workflows/build-apk.yml` already hai — jaise hi tum
   `main` branch pe push karoge, GitHub Actions khud APK build kar dega.
3. GitHub repo → **Actions** tab → latest run open karo → neeche
   **Artifacts** section mein `rush47-debug-apk` milega → download kar lo
   (ZIP ke andar `app-debug.apk` hoga).
4. Ye **debug APK** hai (bina signing) — testing ke liye theek hai. Play
   Store pe daalne ke liye baad mein release keystore se sign karna hoga.

## Local testing (agar kabhi Android Studio access mile)
`gradle assembleDebug` chala do project root se — same workflow jo CI
use karta hai.

---
Agla part ready karne ke liye bolo: **Home/Dashboard** (bottom tabs, Play/Earn/Me
fragments) is foundation ke upar banega.
