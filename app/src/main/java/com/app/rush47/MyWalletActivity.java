package com.app.rush47;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.rush47.utils.UserLocalStore;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * My Wallet screen: shows the wallet balance breakdown (deposited /
 * winning / bonus) from earn.php. Deposit and Withdraw are placeholders
 * until a real payment gateway is wired into the backend - wiring one
 * up needs your merchant/payment-provider credentials, so those buttons
 * show "coming soon" for now instead of guessing at a flow.
 */
public class MyWalletActivity extends AppCompatActivity {

    private TextView totalBalanceText, depositedText, winningText, bonusText;

    private RequestQueue requestQueue;
    private UserLocalStore userLocalStore;
    private String apiBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_wallet);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        userLocalStore = new UserLocalStore(this);
        apiBase = getString(R.string.api);

        totalBalanceText = findViewById(R.id.totalBalanceText);
        depositedText = findViewById(R.id.depositedText);
        winningText = findViewById(R.id.winningText);
        bonusText = findViewById(R.id.bonusText);

        findViewById(R.id.depositButton).setOnClickListener(v ->
                Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show());
        findViewById(R.id.withdrawButton).setOnClickListener(v ->
                Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show());

        fetchWalletData();
    }

    private void fetchWalletData() {
        String url = apiBase + "earn.php";

        JSONObject params = new JSONObject();
        try {
            params.put("member_id", userLocalStore.getLoggedInUser().getMemberid());
            params.put("api_token", userLocalStore.getLoggedInUser().getToken());
        } catch (JSONException ignored) {
        }

        JsonObjectRequest request = new JsonObjectRequest(
                com.android.volley.Request.Method.POST, url, params,
                this::handleResponse,
                error -> { });

        request.setShouldCache(false);
        requestQueue.add(request);
    }

    private void handleResponse(JSONObject response) {
        try {
            if (TextUtils.equals(response.getString("status"), "true")) {
                JSONObject data = response.getJSONObject("message");
                totalBalanceText.setText(data.optString("wallet_balance", "0"));
                depositedText.setText(data.optString("deposited", "0"));
                winningText.setText(data.optString("winning", "0"));
                bonusText.setText(data.optString("bonus", "0"));
            }
        } catch (JSONException ignored) {
        }
    }
}
