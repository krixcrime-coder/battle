<?php
// db.php - Database connection (mysqli)
// Sabhi register.php, login.php, forgot_password.php, reset_password.php
// isi file ko include karte hain.

// ⚠️ SECURITY NOTE: Ye file me real DB password hai. Isko kabhi bhi public
// GitHub repo me push mat karna (sirf backend server par upload karna, FTP/cPanel se).
// Android app ka GitHub Actions build sirf app/ folder build karta hai, backend/
// alag rakhna aur .gitignore me dalna best hai.

$DB_HOST = "localhost";           // Zyada tar shared hosting (cPanel) par ye hi hota hai
$DB_NAME = "battleroy_battle";
$DB_USER = "battleroy_battle";
$DB_PASS = "battleroy_battle";

$conn = @new mysqli($DB_HOST, $DB_USER, $DB_PASS, $DB_NAME);

if ($conn->connect_error) {
    http_response_code(500);
    header("Content-Type: application/json");
    echo json_encode([
        "success" => false,
        "message" => "Database connection failed: " . $conn->connect_error,
        "data" => null
    ]);
    exit;
}

$conn->set_charset("utf8mb4");
