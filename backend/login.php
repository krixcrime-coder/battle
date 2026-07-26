<?php
// login.php
require_once "response.php";
require_once "db.php";

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    fail("Sirf POST method allowed hai", 405);
}

$input = json_decode(file_get_contents("php://input"), true);
if (!$input) {
    fail("Invalid request body", 400);
}

$username = trim($input["username"] ?? "");
$password = trim($input["password"] ?? "");

if ($username === "" || $password === "") {
    fail("Username aur Password bharo");
}

// Username, email ya mobile - teeno se login allow karte hain
$stmt = $conn->prepare("SELECT id, first_name, last_name, username, email, mobile, password_hash, wallet_balance, status FROM users WHERE username = ? OR email = ? OR mobile = ? LIMIT 1");
$stmt->bind_param("sss", $username, $username, $username);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    $stmt->close();
    fail("Account nahi mila, pehle Sign Up karo", 404);
}

$user = $result->fetch_assoc();
$stmt->close();

if ((int)$user["status"] === 0) {
    fail("Ye account block/disable hai. Support se contact karo.", 403);
}

if (!password_verify($password, $user["password_hash"])) {
    fail("Username ya Password galat hai", 401);
}

// Simple session token (proper JWT baad me laga sakte hain)
$token = bin2hex(random_bytes(32));

// Login successful
ok("Login successful", [
    "token" => $token,
    "user_id" => (int)$user["id"],
    "first_name" => $user["first_name"],
    "last_name" => $user["last_name"],
    "username" => $user["username"],
    "email" => $user["email"],
    "mobile" => $user["mobile"],
    "wallet_balance" => (float)$user["wallet_balance"]
]);

$conn->close();
