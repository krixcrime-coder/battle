package com.app.rush47;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.rush47.adapters.TournamentAdapter;
import com.app.rush47.models.Tournament;
import com.app.rush47.utils.NetworkErrorHelper;
import com.app.rush47.utils.UserLocalStore;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Match list for one category/game (e.g. tapping "FULL MAP" on the
 * Play page). Tournament/Solo tab re-fetches with a different "type"
 * filter. Tapping a card opens MatchDetailActivity; tapping JOIN opens
 * SelectPositionActivity so the player can pick a position before
 * confirming (see JoiningMatchActivity), then this list refreshes once
 * that flow reports back a successful join.
 *
 * Launch with EXTRA_GAME_ID (required), EXTRA_TITLE (category name,
 * shown in the toolbar) and optionally EXTRA_TYPE ("tournament" or
 * "solo") to pre-select a tab.
 */
public class MatchListActivity extends AppCompatActivity {

    public static final String EXTRA_GAME_ID = "GAME_ID";
    public static final String EXTRA_CATEGORY_ID = "CATEGORY_ID";
    public static final String EXTRA_TITLE = "TITLE";
    public static final String EXTRA_TYPE = "TYPE";

    private static final int REQUEST_JOIN = 201;


    private RecyclerView recyclerView;
    private SwipeRefreshLayout pullToRefresh;
    private TextView noMatchesFound;
    private TabLayout tabs;

    private RequestQueue requestQueue;
    private UserLocalStore userLocalStore;
    private String apiBase;

    private String gameId;
    private String categoryId;
    private String selectedType = "tournament";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_list);

        gameId = getIntent().getStringExtra(EXTRA_GAME_ID);
        categoryId = getIntent().getStringExtra(EXTRA_CATEGORY_ID);
        String title = getIntent().getStringExtra(EXTRA_TITLE);
        String initialType = getIntent().getStringExtra(EXTRA_TYPE);
        if (!TextUtils.isEmpty(initialType)) {
            selectedType = initialType;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setTitle(TextUtils.isEmpty(title) ? getString(R.string.esports_games) : title);

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        userLocalStore = new UserLocalStore(this);
        apiBase = getString(R.string.api);

        recyclerView = findViewById(R.id.matchListRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        pullToRefresh = findViewById(R.id.pullToRefreshMatchList);
        noMatchesFound = findViewById(R.id.noMatchesFound);
        tabs = findViewById(R.id.matchListTabs);

        pullToRefresh.setOnRefreshListener(this::fetchMatches);

        if ("solo".equals(selectedType) && tabs.getTabAt(1) != null) {
            tabs.getTabAt(1).select();
        }

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedType = tab.getPosition() == 0 ? "tournament" : "solo";
                fetchMatches();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        fetchMatches();
    }

    private void fetchMatches() {
        String url = apiBase + "tournaments.php";

        JSONObject params = new JSONObject();
        try {
            params.put("category_id", categoryId);
            params.put("game_id", gameId);
            params.put("type", selectedType);
            params.put("member_id", userLocalStore.getLoggedInUser().getMemberid());
            params.put("api_token", userLocalStore.getLoggedInUser().getToken());
        } catch (JSONException ignored) {
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, url, params,
                this::handleMatchesResponse,
                error -> {
                    pullToRefresh.setRefreshing(false);
                    Toast.makeText(this, NetworkErrorHelper.describe(error), Toast.LENGTH_SHORT).show();
                });

        request.setShouldCache(false);
        requestQueue.add(request);
    }

    private void handleMatchesResponse(JSONObject response) {
        List<Tournament> tournaments = new ArrayList<>();
        try {
            if (TextUtils.equals(response.getString("status"), "true")) {
                JSONArray arr = response.getJSONObject("message").optJSONArray("tournaments");
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) {
                        tournaments.add(Tournament.fromJson(arr.getJSONObject(i)));
                    }
                }
            }
        } catch (JSONException ignored) {
        }
        renderMatches(tournaments);
    }

    private void renderMatches(List<Tournament> tournaments) {
        pullToRefresh.setRefreshing(false);
        noMatchesFound.setVisibility(tournaments.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setAdapter(new TournamentAdapter(tournaments, true, new TournamentAdapter.OnTournamentClickListener() {
            @Override
            public void onCardClick(Tournament tournament) {
                Intent intent = new Intent(MatchListActivity.this, MatchDetailActivity.class);
                intent.putExtra(MatchDetailActivity.EXTRA_TOURNAMENT_ID, tournament.getTournamentId());
                startActivity(intent);
            }

            @Override
            public void onJoinClick(Tournament tournament) {
                openSelectPosition(tournament);
            }
        }));
    }

    private void openSelectPosition(Tournament tournament) {
        Intent intent = new Intent(this, SelectPositionActivity.class);
        intent.putExtra(SelectPositionActivity.EXTRA_TOURNAMENT_ID, tournament.getTournamentId());
        intent.putExtra(SelectPositionActivity.EXTRA_TITLE, tournament.getTitle());
        intent.putExtra(SelectPositionActivity.EXTRA_ENTRY_FEE, tournament.getEntryFee());
        startActivityForResult(intent, REQUEST_JOIN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_JOIN && resultCode == RESULT_OK) {
            fetchMatches();
        }
    }
}
