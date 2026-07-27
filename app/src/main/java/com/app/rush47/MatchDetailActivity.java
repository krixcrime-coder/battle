package com.app.rush47;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.rush47.models.Tournament;
import com.app.rush47.utils.LoadingDialog;
import com.app.rush47.utils.NetworkErrorHelper;
import com.app.rush47.utils.UserLocalStore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

/**
 * Full detail for one match: fetches tournament_detail.php (which also
 * tells us whether we've joined and, if so, whether the room ID/
 * password are revealed yet), and lets you join right from here.
 */
public class MatchDetailActivity extends AppCompatActivity {

    public static final String EXTRA_TOURNAMENT_ID = "TOURNAMENT_ID";

    private TextView title, statusChip, map, matchTime, entryFee, prize, perKill,
            slotsText, roomId, roomPassword, roomRevealsSoon, joinedPlayers, joinButton;
    private ProgressBar slotsProgress;
    private View roomInfoCard;

    private RequestQueue requestQueue;
    private UserLocalStore userLocalStore;
    private LoadingDialog loadingDialog;
    private String apiBase;
    private String tournamentId;

    private Tournament current;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_detail);

        tournamentId = getIntent().getStringExtra(EXTRA_TOURNAMENT_ID);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        userLocalStore = new UserLocalStore(this);
        loadingDialog = new LoadingDialog(this);
        apiBase = getString(R.string.api);

        title = findViewById(R.id.detailTitle);
        statusChip = findViewById(R.id.detailStatusChip);
        map = findViewById(R.id.detailMap);
        matchTime = findViewById(R.id.detailMatchTime);
        entryFee = findViewById(R.id.detailEntryFee);
        prize = findViewById(R.id.detailPrize);
        perKill = findViewById(R.id.detailPerKill);
        slotsProgress = findViewById(R.id.detailSlotsProgress);
        slotsText = findViewById(R.id.detailSlotsText);
        roomInfoCard = findViewById(R.id.roomInfoCard);
        roomId = findViewById(R.id.detailRoomId);
        roomPassword = findViewById(R.id.detailRoomPassword);
        roomRevealsSoon = findViewById(R.id.roomRevealsSoonText);
        joinedPlayers = findViewById(R.id.detailJoinedPlayers);
        joinButton = findViewById(R.id.detailJoinButton);

        fetchDetail();
    }

    private void fetchDetail() {
        loadingDialog.show();
        String url = apiBase + "tournament_detail.php";

        JSONObject params = new JSONObject();
        try {
            params.put("member_id", userLocalStore.getLoggedInUser().getMemberid());
            params.put("api_token", userLocalStore.getLoggedInUser().getToken());
            params.put("tournament_id", tournamentId);
        } catch (JSONException ignored) {
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, url, params,
                this::handleDetailResponse,
                error -> {
                    loadingDialog.dismiss();
                    Toast.makeText(this, NetworkErrorHelper.describe(error), Toast.LENGTH_SHORT).show();
                });

        request.setShouldCache(false);
        requestQueue.add(request);
    }

    private void handleDetailResponse(JSONObject response) {
        loadingDialog.dismiss();
        try {
            if (!TextUtils.equals(response.getString("status"), "true")) {
                Toast.makeText(this, response.optString("message", ""), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            JSONObject data = response.getJSONObject("message");
            current = Tournament.fromJson(data);
            render(current, data.optJSONArray("joined_usernames"));
        } catch (JSONException e) {
            Toast.makeText(this, "Something went wrong. Please try again.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void render(Tournament t, JSONArray joinedUsernames) {
        title.setText(t.getTitle());

        int color;
        String label;
        switch (t.getStatus()) {
            case "ongoing":
                color = getResources().getColor(R.color.newgreen);
                label = "LIVE";
                break;
            case "completed":
                color = getResources().getColor(R.color.cb_dark_grey);
                label = "COMPLETED";
                break;
            default:
                color = getResources().getColor(R.color.purple);
                label = "UPCOMING";
        }
        statusChip.setText(label);
        statusChip.getBackground().mutate().setTint(color);

        map.setText(getString(R.string.map) + ": " + t.getMapName());
        matchTime.setText(getString(R.string.match_time_label) + ": " + formatMatchTime(t.getMatchTime()));

        entryFee.setText(t.getEntryFee());
        prize.setText(t.getPrize());
        perKill.setText(t.getPerKill());

        slotsProgress.setMax(Math.max(t.getSlotTotal(), 1));
        slotsProgress.setProgress(t.getSlotsFilled());
        slotsText.setText(getString(R.string.slots_filled_format, t.getSlotsFilled(), t.getSlotTotal()));

        boolean hasRoom = !TextUtils.isEmpty(t.getRoomId());
        roomId.setText(hasRoom ? t.getRoomId() : "-");
        roomPassword.setText(hasRoom ? t.getRoomPassword() : "-");
        roomRevealsSoon.setVisibility(t.isJoined() && !hasRoom && !"completed".equals(t.getStatus()) ? View.VISIBLE : View.GONE);
        roomInfoCard.setVisibility(t.isJoined() ? View.VISIBLE : View.GONE);

        StringBuilder names = new StringBuilder();
        if (joinedUsernames != null) {
            for (int i = 0; i < joinedUsernames.length(); i++) {
                if (names.length() > 0) names.append(", ");
                names.append(joinedUsernames.optString(i, ""));
            }
        }
        joinedPlayers.setText(names.length() > 0 ? names.toString() : getString(R.string.no_matches_found));

        bindJoinButton(t);
    }

    private void bindJoinButton(Tournament t) {
        if (t.isJoined()) {
            joinButton.setText(R.string.joined);
            joinButton.setAlpha(0.6f);
            joinButton.setOnClickListener(null);
        } else if (t.isFull() || "completed".equals(t.getStatus()) || "ongoing".equals(t.getStatus())) {
            joinButton.setText(t.isFull() ? R.string.match_full : R.string.completed);
            joinButton.setAlpha(0.6f);
            joinButton.setOnClickListener(null);
        } else {
            joinButton.setText(R.string.join);
            joinButton.setAlpha(1f);
            joinButton.setOnClickListener(v -> confirmAndJoin(t));
        }
    }

    private void confirmAndJoin(Tournament t) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_join_title)
                .setMessage(getString(R.string.confirm_join_message_format, t.getEntryFee()))
                .setPositiveButton(R.string.yes_join, (dialog, which) -> joinMatch())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void joinMatch() {
        loadingDialog.show();
        String url = apiBase + "join_tournament.php";

        JSONObject params = new JSONObject();
        try {
            params.put("member_id", userLocalStore.getLoggedInUser().getMemberid());
            params.put("api_token", userLocalStore.getLoggedInUser().getToken());
            params.put("tournament_id", tournamentId);
        } catch (JSONException ignored) {
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, url, params,
                response -> {
                    loadingDialog.dismiss();
                    try {
                        Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show();
                        if (TextUtils.equals(response.getString("status"), "true")) {
                            fetchDetail();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Something went wrong. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    loadingDialog.dismiss();
                    Toast.makeText(this, NetworkErrorHelper.describe(error), Toast.LENGTH_SHORT).show();
                });

        request.setShouldCache(false);
        requestQueue.add(request);
    }

    private String formatMatchTime(String raw) {
        if (TextUtils.isEmpty(raw)) return "";
        try {
            java.text.SimpleDateFormat in = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            java.text.SimpleDateFormat out = new java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
            return out.format(in.parse(raw));
        } catch (Exception e) {
            return raw;
        }
    }
}
