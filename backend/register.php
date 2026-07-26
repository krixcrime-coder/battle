<?php
// register.php
require_once "response.php";
require_once "db.php";

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    fail("Sirf POST method allowed hai", 405);
}

// Android Retrofit body ko raw JSON ke roop me bhejta hai (GsonConverterFactory)
$input = json_decode(file_get_contents("php://input"), true);
if (!$input) {
    fail("Invalid request body", 400);
}

$first_name   = trim($input["first_name"] ?? "");
$last_name    = trim($input["last_name"] ?? "");
$username     = trim($input["username"] ?? "");
$country_code = trim($input["country_code"] ?? "+91");
$mobile       = trim($input["mobile"] ?? "");
$email        = trim($input["email"] ?? "");
$password     = trim($input["password"] ?? "");
$referral     = trim($input["referral_code"] ?? "");

// ---- Validation ----
if ($first_name === "" || $last_name === "" || $username === "" ||
    $mobile === "" || $email === "" || $password === "") {
    fail("Saari required fields bharo");
}

if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    fail("Email sahi format me nahi hai");
}

if (!preg_match('/^[0-9]{10}$/', $mobile)) {
    fail("Mobile number 10 digit ka hona chahiye");
}

if (strlen($password) < 6) {
    fail("Password kam se kam 6 character ka hona chahiye");
}

if (!preg_match('/^[a-zA-Z0-9_]{3,20}$/', $username)) {
    fail("Username sirf letters, numbers, underscore (3-20 chars) allowed hai");
}

// ---- Duplicate check (username / email / mobile) ----
$stmt = $conn->prepare("SELECT id FROM users WHERE username = ? OR email = ? OR mobile = ? LIMIT 1");
$stmt->bind_param("sss", $username, $email, $mobile);
$stmt->execute();
$stmt->store_result();
if ($stmt->num_rows > 0) {
    $stmt->close();
    fail("Username, email ya mobile pehle se registered hai", 409);
}
$stmt->close();

// ---- Referral code validate (agar diya hai to) ----
if ($referral !== "") {
    $refCheck = $conn->prepare("SELECT id FROM users WHERE my_referral_code = ? LIMIT 1");
    $refCheck->bind_param("s", $referral);
    $refCheck->execute();
    $refCheck->store_result();
    if ($refCheck->num_rows === 0) {
        $refCheck->close();
        fail("Referral code invalid hai");
    }
    $refCheck->close();
}

// ---- Insert ----
$password_hash = password_hash($password, PASSWORD_BCRYPT);
$my_referral_code = strtoupper(substr($username, 0, 4)) . rand(1000, 9999);

$insert = $conn->prepare(
    "INSERT INTO users (first_name, last_name, username, country_code, mobile, email, password_hash, referral_code, my_referral_code)
     VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
);
$insert->bind_param(
    "sssssssss",
    $first_name, $last_name, $username, $country_code, $mobile, $email, $password_hash, $referral, $my_referral_code
);

if ($insert->execute()) {
    ok("Signup successful! Ab login karo.", [
        "user_id" => $insert->insert_id,
        "my_referral_code" => $my_referral_code
    ]);
} else {
    fail("Signup fail ho gaya, dobara try karo. (" . $conn->error . ")", 500);
}

$insert->close();
$conn->close();
