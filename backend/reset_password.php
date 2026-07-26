<?php
// reset_password.php
require_once "response.php";
require_once "db.php";

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    fail("Sirf POST method allowed hai", 405);
}

$input = json_decode(file_get_contents("php://input"), true);
if (!$input) {
    fail("Invalid request body", 400);
}

$identifier   = trim($input["identifier"] ?? "");
$otp          = trim($input["otp"] ?? "");
$new_password = trim($input["new_password"] ?? "");

if ($identifier === "" || $otp === "" || $new_password === "") {
    fail("Identifier, OTP aur new password bharo");
}

if (strlen($new_password) < 6) {
    fail("Password kam se kam 6 character ka hona chahiye");
}

// Latest, unused, non-expired OTP dhoondo
$stmt = $conn->prepare(
    "SELECT id FROM password_resets
     WHERE identifier = ? AND otp = ? AND used = 0 AND expires_at >= NOW()
     ORDER BY id DESC LIMIT 1"
);
$stmt->bind_param("ss", $identifier, $otp);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    $stmt->close();
    fail("OTP galat ya expire ho chuka hai", 400);
}
$resetRow = $result->fetch_assoc();
$stmt->close();

// Password update karo
$password_hash = password_hash($new_password, PASSWORD_BCRYPT);
$update = $conn->prepare("UPDATE users SET password_hash = ? WHERE email = ? OR mobile = ?");
$update->bind_param("sss", $password_hash, $identifier, $identifier);
$update->execute();

if ($update->affected_rows === 0) {
    $update->close();
    fail("User update nahi ho paya", 500);
}
$update->close();

// OTP ko used mark karo (dobara use na ho)
$markUsed = $conn->prepare("UPDATE password_resets SET used = 1 WHERE id = ?");
$markUsed->bind_param("i", $resetRow["id"]);
$markUsed->execute();
$markUsed->close();

ok("Password reset ho gaya! Ab naye password se login karo.");

$conn->close();
