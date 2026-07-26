package com.app.rush47.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.rush47.R;
import com.app.rush47.adapters.BannerPagerAdapter;
import com.app.rush47.adapters.GameAdapter;
import com.app.rush47.models.Game;
import com.app.rush47.utils.UserLocalStore;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Play tab (Home dashboard). Matched pixel-for-pixel against the original
 * decompiled play_home.xml.
 *
 * Backend contract (adjust once the real endpoint is confirmed):
 *   POST {api}home.php  ->  member_id, api_token, type (tournament|solo)
 *   {
 *     "status": "true",
 *     "message": {
 *       "wallet_balance": "500",
 *       "announcement": "Welcome to Rush47!",
 *       "banners": ["https://.../b1.jpg", "https://.../b2.jpg"],
 *       "ongoing_count": "2",
 *       "upcoming_count": "5",
 *       "completed_count": "10",
 *       "games": [
 *         {"game_id":"1","name":"FREE FIRE","banner":"https://...","matches_available":"12"}
 *       ]
 *     }
 *   }
 *
 * Until that endpoint is ready, sample data is shown so the screen is fully
 * demoable - swap loadSampleData() out once fetchHomeData() is wired to the
 * real backend.
 */
public class PlayFragment extends Fragment {

    private static final long BANNER_AUTO_SCROLL_MS = 3000;

    private ShimmerFrameLayout shimmer;
    private SwipeRefreshLayout pullToRefresh;
    private TextView announceText;
    private TextView balanceText;
    private TextView ongoingCount, upcomingCount, completedCount;
    private TextView noGameFound;
    private RecyclerView gamesRecyclerView;
    private TabLayout tournamentSoloTabs;
    private ViewPager bannerPager;

    private RequestQueue requestQueue;
    private UserLocalStore userLocalStore;
    private String apiBase;
    private String selectedType = "tournament";

    private final Handler bannerHandler = new Handler(Looper.getMainLooper());
    private Runnable bannerRunnable;
    private int bannerCount = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_play, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requestQueue = Volley.newRequestQueue(requireContext().getApplicationContext());
        userLocalStore = new UserLocalStore(requireContext());
        apiBase = getString(R.string.api);

        shimmer = view.findViewById(R.id.shimmerplay);
        pullToRefresh = view.findViewById(R.id.pullToRefreshplay);
        announceText = view.findViewById(R.id.announce);
        balanceText = view.findViewById(R.id.balinplay);
        ongoingCount = view.findViewById(R.id.ongoingCount);
        upcomingCount = view.findViewById(R.id.upcomingCount);
        completedCount = view.findViewById(R.id.completedCount);
        noGameFound = view.findViewById(R.id.noupcominginplay1);
        gamesRecyclerView = view.findViewById(R.id.allgamerv);
        tournamentSoloTabs = view.findViewById(R.id.tablayoutmycontest);
        bannerPager = view.findViewById(R.id.kk_pager);

        gamesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        pullToRefresh.setOnRefreshListener(this::fetchHomeData);

        tournamentSoloTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedType = tab.getPosition() == 0 ? "tournament" : "solo";
                fetchHomeData();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        shimmer.startShimmer();
        fetchHomeData();
    }

    private void fetchHomeData() {
        String url = apiBase + "home.php";

        JSONObject params = new JSONObject();
        try {
            params.put("member_id", userLocalStore.getLoggedInUser().getMemberid());
            params.put("api_token", userLocalStore.getLoggedInUser().getToken());
            params.put("type", selectedType);
        } catch (JSONException ignored) {
        }

        JsonObjectRequest request = new JsonObjectRequest(
                com.android.volley.Request.Method.POST, url, params,
                this::handleHomeResponse,
                error -> {
                    // Backend not wired up yet (or unreachable) - fall back to
                    // sample data so the screen still demos correctly.
                    loadSampleData();
                });

        request.setShouldCache(false);
        requestQueue.add(request);
    }

    private void handleHomeResponse(JSONObject response) {
        try {
            String status = response.getString("status");
            if (!TextUtils.equals(status, "true")) {
                loadSampleData();
                return;
            }
            JSONObject data = response.getJSONObject("message");

            balanceText.setText(data.optString("wallet_balance", "0"));
            announceText.setText(data.optString("announcement", ""));
            ongoingCount.setText(getString(R.string.ongoing) + " (" + data.optString("ongoing_count", "0") + ")");
            upcomingCount.setText(getString(R.string.upcoming) + " (" + data.optString("upcoming_count", "0") + ")");
            completedCount.setText(getString(R.string.completed) + " (" + data.optString("completed_count", "0") + ")");

            List<String> banners = new ArrayList<>();
            JSONArray bannerArray = data.optJSONArray("banners");
            if (bannerArray != null) {
                for (int i = 0; i < bannerArray.length(); i++) {
                    banners.add(bannerArray.getString(i));
                }
            }
            setupBanner(banners);

            List<Game> games = new ArrayList<>();
            JSONArray gameArray = data.optJSONArray("games");
            if (gameArray != null) {
                for (int i = 0; i < gameArray.length(); i++) {
                    JSONObject g = gameArray.getJSONObject(i);
                    games.add(new Game(
                            g.optString("game_id", ""),
                            g.optString("name", ""),
                            g.optString("banner", ""),
                            g.optInt("matches_available", 0)
                    ));
                }
            }
            renderGames(games);

        } catch (JSONException e) {
            loadSampleData();
        } finally {
            finishLoading();
        }
    }

    /** Shown when the backend isn't reachable yet, so the page still looks right. */
    private void loadSampleData() {
        balanceText.setText("0");
        announceText.setText(getString(R.string.app_name) + " - tournaments coming soon!");
        ongoingCount.setText(getString(R.string.ongoing) + " (0)");
        upcomingCount.setText(getString(R.string.upcoming) + " (0)");
        completedCount.setText(getString(R.string.completed) + " (0)");

        setupBanner(new ArrayList<>());

        List<Game> games = new ArrayList<>();
        games.add(new Game("1", "FREE FIRE", "", 0));
        games.add(new Game("2", "BGMI", "", 0));
        renderGames(games);

        finishLoading();
    }

    private void renderGames(List<Game> games) {
        noGameFound.setVisibility(games.isEmpty() ? View.VISIBLE : View.GONE);
        gamesRecyclerView.setAdapter(new GameAdapter(games, game ->
                Toast.makeText(requireContext(),
                        game.getName() + " tournaments - coming soon", Toast.LENGTH_SHORT).show()));
    }

    private void setupBanner(List<String> banners) {
        stopBannerAutoScroll();
        bannerCount = banners.size();
        bannerPager.setAdapter(new BannerPagerAdapter(banners));
        if (bannerCount > 1) {
            startBannerAutoScroll();
        }
    }

    private void startBannerAutoScroll() {
        bannerRunnable = () -> {
            if (bannerPager == null || bannerCount == 0) return;
            int next = (bannerPager.getCurrentItem() + 1) % bannerCount;
            bannerPager.setCurrentItem(next, true);
            bannerHandler.postDelayed(bannerRunnable, BANNER_AUTO_SCROLL_MS);
        };
        bannerHandler.postDelayed(bannerRunnable, BANNER_AUTO_SCROLL_MS);
    }

    private void stopBannerAutoScroll() {
        if (bannerRunnable != null) {
            bannerHandler.removeCallbacks(bannerRunnable);
        }
    }

    private void finishLoading() {
        shimmer.stopShimmer();
        shimmer.setVisibility(View.GONE);
        pullToRefresh.setRefreshing(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopBannerAutoScroll();
    }
}
