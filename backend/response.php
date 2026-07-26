<?php
// response.php - Har API isi format me reply karega taaki Android ka
// ApiResponse(success, message, data) model sahi se parse ho.

header("Content-Type: application/json");

function send_response($success, $message, $data = null, $http_code = 200) {
    http_response_code($http_code);
    echo json_encode([
        "success" => $success,
        "message" => $message,
        "data" => $data
    ]);
    exit;
}

// Convenience wrappers
function ok($message, $data = null) {
    send_response(true, $message, $data, 200);
}

function fail($message, $http_code = 400) {
    // 4xx/5xx status par Android side errorBody() se parse hota hai
    // (ApiUtils.extractApiResponse already ye handle karta hai)
    send_response(false, $message, null, $http_code);
}
