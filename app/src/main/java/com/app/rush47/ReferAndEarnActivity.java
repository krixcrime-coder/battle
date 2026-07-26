package com.app.rush47;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
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
 * Refer & Earn screen. Fetches this member's own auto-generated referral
 * code from earn.php and lets them copy or share it. Anyone who signs up
 * using this code gets an instant +2 coin bonus (handled server-side in
 * registrationAcc.php) - this screen just displays/shares the code.
 */
public class ReferAndEarnActivity extends AppCompatActivity {

    private TextView referralCodeText;
    private TextView referralCountText;
    private String referralCode = "";

    private RequestQueue requestQueue;
    private UserLocalStore userLocalStore;
    private String apiBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refer_and_earn);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        userLocalStore = new UserLocalStore(this);
        apiBase = getString(R.string.api);

        referralCodeText = findViewById(R.id.referralCodeText);
        referralCountText = findViewById(R.id.referralCountText);

        referralCodeText.setOnClickListener(v -> copyReferralCode());
        findViewById(R.id.referNowButton).setOnClickListener(v -> shareReferralCode());

        fetchReferralData();
    }

    private void fetchReferralData() {
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
                referralCode = data.optString("referral_code", "");
                referralCodeText.setText(TextUtils.isEmpty(referralCode) ? "------" : referralCode);
                referralCountText.setText(data.optString("referral_count", "0"));
            }
        } catch (JSONException ignored) {
        }
    }

    private void copyReferralCode() {
        if (TextUtils.isEmpty(referralCode)) return;
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("referral_code", referralCode));
        Toast.makeText(this, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
    }

    private void shareReferralCode() {
        if (TextUtils.isEmpty(referralCode)) {
            Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                "Join Rush47 and use my referral code " + referralCode + " to get a signup bonus!");
        startActivity(Intent.createChooser(shareIntent, getString(R.string.refer_and_earn)));
    }
}
