package com.app.rush47;

import android.content.Intent;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
 * Registration screen. Backend contract:
 *   POST {api}registrationAcc -> first_name, last_name, user_name,
 *                                 mobile_no, email_id, password,
 *                                 cpassword, promo_code, submit
 *
 * There's only a single password field on this screen now (no confirm
 * password), so we send cpassword = password to satisfy the backend's
 * existing check without needing a backend change.
 *
 * The country-code spinner only lists India (+91) for now. It's not
 * appended to mobile_no yet since the backend only supports 10-digit
 * Indian numbers today - it's there so more countries can be added
 * later without another UI rework.
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText firstNameEt, lastNameEt, usernameEt, mobileEt, emailEt,
            passwordEt, referCodeEt;
    private Spinner countryCodeSpinner;
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
        countryCodeSpinner = findViewById(R.id.reg_country_code);
        mobileEt = findViewById(R.id.reg_mobile);
        emailEt = findViewById(R.id.reg_email);
        passwordEt = findViewById(R.id.reg_password);
        referCodeEt = findViewById(R.id.reg_refer_code);

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
        String referCode = referCodeEt.getText().toString().trim();

        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(username)
                || TextUtils.isEmpty(mobile) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mobile.length() != 10) {
            Toast.makeText(this, "Enter a valid 10-digit mobile number", Toast.LENGTH_SHORT).show();
            return;
        }

        registerUser(firstName, lastName, username, mobile, email, password, referCode);
    }

    private void registerUser(String firstName, String lastName, String username, String mobile,
                               String email, String password, String referCode) {
        loadingDialog.show();
        String url = apiBase + "registrationAcc.php";

        Map<String, Object> params = new HashMap<>();
        params.put("first_name", firstName);
        params.put("last_name", lastName);
        params.put("user_name", username);
        params.put("mobile_no", mobile);
        params.put("email_id", email);
        params.put("password", password);
        params.put("cpassword", password); // no confirm-password field anymore
        params.put("promo_code", referCode);
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
