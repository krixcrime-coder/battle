package com.app.rush47;

import android.content.Intent;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.rush47.models.CurrentUser;
import com.app.rush47.utils.LoadingDialog;
import com.app.rush47.utils.NetworkErrorHelper;
import com.app.rush47.utils.UserLocalStore;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Registration screen. Recreated (cleaned up) from the original
 * decompiled CreateNewAccount activity. Backend contract kept the same:
 *   POST {api}registrationAcc -> first_name, last_name, user_name,
 *                                 mobile_no, email_id, password,
 *                                 cpassword, promo_code, submit
 *
 * Note: the original also verified the mobile number via Firebase Phone
 * Auth before registering. That step needs the client's Firebase
 * google-services.json, which wasn't present in the decompiled APK, so
 * it's left out of this first pass — register directly hits the backend.
 * We can wire Firebase phone verification back in once that file is provided.
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText firstNameEt, lastNameEt, usernameEt, mobileEt, emailEt,
            passwordEt, confirmPasswordEt, promoCodeEt;
    private LoadingDialog loadingDialog;
    private RequestQueue requestQueue;
    private String apiBase;

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        apiBase = getString(R.string.api);
        loadingDialog = new LoadingDialog(this);
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        firstNameEt = findViewById(R.id.reg_first_name);
        lastNameEt = findViewById(R.id.reg_last_name);
        usernameEt = findViewById(R.id.reg_username);
        mobileEt = findViewById(R.id.reg_mobile);
        emailEt = findViewById(R.id.reg_email);
        passwordEt = findViewById(R.id.reg_password);
        confirmPasswordEt = findViewById(R.id.reg_confirm_password);
        promoCodeEt = findViewById(R.id.reg_promo_code);

        Button registerButton = findViewById(R.id.register_button);
        TextView alreadyHaveAccount = findViewById(R.id.already_have_account);

        registerButton.setOnClickListener(v -> attemptRegister());
        alreadyHaveAccount.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        });
    }

    private void attemptRegister() {
        String firstName = firstNameEt.getText().toString().trim();
        String lastName = lastNameEt.getText().toString().trim();
        String username = usernameEt.getText().toString().trim();
        String mobile = mobileEt.getText().toString().trim();
        String email = emailEt.getText().toString().trim();
        String password = passwordEt.getText().toString();
        String confirmPassword = confirmPasswordEt.getText().toString();
        String promoCode = promoCodeEt.getText().toString().trim();

        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(username)
                || TextUtils.isEmpty(mobile) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!TextUtils.equals(password, confirmPassword)) {
            Toast.makeText(this, R.string.passwords_not_match, Toast.LENGTH_SHORT).show();
            return;
        }

        registerUser(firstName, lastName, username, mobile, email, password, confirmPassword, promoCode);
    }

    private void registerUser(String firstName, String lastName, String username, String mobile,
                               String email, String password, String confirmPassword, String promoCode) {
        loadingDialog.show();
        String url = apiBase + "registrationAcc";

        Map<String, Object> params = new HashMap<>();
        params.put("first_name", firstName);
        params.put("last_name", lastName);
        params.put("user_name", username);
        params.put("mobile_no", mobile);
        params.put("email_id", email);
        params.put("password", password);
        params.put("cpassword", confirmPassword);
        params.put("promo_code", promoCode);
        params.put("submit", "register");

        JsonObjectRequest request = new JsonObjectRequest(url, new JSONObject(params),
                response -> handleRegisterResponse(response, username, password, firstName, lastName, email, mobile),
                this::handleNetworkError);

        request.setShouldCache(false);
        requestQueue.add(request);
    }

    private void handleRegisterResponse(JSONObject response, String username, String password,
                                         String firstName, String lastName, String email, String mobile) {
        loadingDialog.dismiss();
        try {
            String status = response.getString("status");
            String message = response.getString("message");
            if (TextUtils.equals(status, "true")) {
                CurrentUser user = new CurrentUser(
                        response.optString("member_id", ""),
                        username,
                        password,
                        email,
                        mobile,
                        response.optString("api_token", ""),
                        firstName,
                        lastName
                );
                new UserLocalStore(getApplicationContext()).storeUserData(user);

                Toast.makeText(this, R.string.registration_successfully, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                finish();
            } else {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            Toast.makeText(this, "Something went wrong. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleNetworkError(VolleyError error) {
        loadingDialog.dismiss();
        Toast.makeText(this, NetworkErrorHelper.describe(error), Toast.LENGTH_LONG).show();
    }
}
