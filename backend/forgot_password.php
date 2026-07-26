<?php
// forgot_password.php
require_once "response.php";
require_once "db.php";

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    fail("Sirf POST method allowed hai", 405);
}

$input = json_decode(file_get_contents("php://input"), true);
if (!$input) {
    fail("Invalid request body", 400);
}

$identifier = trim($input["identifier"] ?? "");
if ($identifier === "") {
    fail("Email ya Mobile No. daalo");
}

// Check user exists (email ya mobile se)
$stmt = $conn->prepare("SELECT id, email, mobile FROM users WHERE email = ? OR mobile = ? LIMIT 1");
$stmt->bind_param("ss", $identifier, $identifier);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    $stmt->close();
    // Security best practice: attacker ko batao mat ki account exist karta hai ya nahi.
    // Lekin tumhare UX ke hisaab se clear error chahiye to ye line use karo:
    fail("Is email/mobile se koi account nahi mila", 404);
}
$user = $result->fetch_assoc();
$stmt->close();

// Generate 6-digit OTP, 10 min expiry
$otp = strval(random_int(100000, 999999));
$expires_at = date("Y-m-d H:i:s", strtotime("+10 minutes"));

$insert = $conn->prepare("INSERT INTO password_resets (identifier, otp, expires_at) VALUES (?, ?, ?)");
$insert->bind_param("sss", $identifier, $otp, $expires_at);
$insert->execute();
$insert->close();

$sent = false;

// Agar identifier email hai to PHP mail() se bhejne ki koshish karo
// ⚠️ Shared hosting par mail() kabhi kabhi spam me jaata hai ya block ho sakta hai.
// Better reliability ke liye PHPMailer + SMTP (Gmail/SendGrid) use karna recommended hai.
if (filter_var($identifier, FILTER_VALIDATE_EMAIL)) {
    $subject = "RUSH 47 - Password Reset OTP";
    $message = "Your OTP is: $otp\nThis OTP is valid for 10 minutes.";
    $headers = "From: no-reply@royalflood.site";
    $sent = @mail($identifier, $subject, $message, $headers);
} else {
    // Mobile number hai -> SMS gateway (jaise Fast2SMS, MSG91, Twilio) yahan integrate karna hoga.
    // TODO: apna SMS gateway API key lagakar yahan curl request bhejo.
    // Filhaal SMS gateway connect nahi hai, isliye ye 'sent = false' rahega.
    $sent = false;
}

if ($sent) {
    ok("OTP bhej diya gaya hai, apna email check karo.");
} else {
    // SMS gateway/mail() setup hone tak, testing ke liye OTP response me bhi bhej rahe hai.
    // ⚠️ PRODUCTION ME YE LINE HATA DENA (security risk) jab SMS/Email gateway live ho jaye.
    ok("OTP generate ho gaya (abhi SMS/Email gateway connected nahi hai, isliye OTP yahin dikha rahe hai).", [
        "otp_debug_only" => $otp
    ]);
}

$conn->close();
