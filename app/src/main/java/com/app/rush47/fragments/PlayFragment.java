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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.rush47.R;
import com.app.rush47.adapters.BannerPagerAdapter;
import com.app.rush47.adapters.CategoryAdapter;
import com.app.rush47.models.Banner;
import com.app.rush47.models.Category;
import com.app.rush47.utils.UserLocalStore;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Play tab (Home dashboard).
 *
 * Everything on this screen is fetched independently from its own
 * backend endpoint, so each piece can be managed from the database on
 * its own:
 *   POST {api}home.php        -> wallet_balance, ongoing/upcoming/completed counts (needs login)
 *   POST {api}banners.php     -> [{image_url, redirect_url}]   (top auto-scroll slider, tap-through)
 *   POST {api}notices.php     -> [message, message, ...]       (notice bar, rotates every few seconds)
 *   POST {api}categories.php  -> [{category_id, name, image_url, redirect_url}] (3-per-row grid)
 *
 * Banners / notices / categories are all "empty table -> nothing shown"
 * - there's no sample/placeholder fallback data for those. The
 * Tournament/Solo tab only changes which "type" is requested once
 * matchmaking is wired into categories.php later.
 */
public class PlayFragment extends Fragment {

    private static final long BANNER_AUTO_SCROLL_MS = 3000;
    private static final long NOTICE_ROTATE_MS = 4000;

    private ShimmerFrameLayout shimmer;
    private SwipeRefreshLayout pullToRefresh;
    private TextView announceText;
    private TextView balanceText;
    private TextView ongoingCount, upcomingCount, completedCount;
    private TextView noGameFound;
    private RecyclerView categoriesRecyclerView;
    private TabLayout tournamentSoloTabs;
    private ViewPager bannerPager;

    private RequestQueue requestQueue;
    private UserLocalStore userLocalStore;
    private String apiBase;
    private String selectedType = "tournament";

    private final Handler bannerHandler = new Handler(Looper.getMainLooper());
    private Runnable bannerRunnable;
    private int bannerCount = 0;

    private final Handler noticeHandler = new Handler(Looper.getMainLooper());
    private Runnable noticeRunnable;
    private List<String> notices = new ArrayList<>();
    private int noticeIndex = 0;

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
        categoriesRecyclerView = view.findViewById(R.id.allgamerv);
        tournamentSoloTabs = view.findViewById(R.id.tablayoutmycontest);
        bannerPager = view.findViewById(R.id.kk_pager);

        // 3 cards per row, matching the design.
        categoriesRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 3));

        pullToRefresh.setOnRefreshListener(this::fetchEverything);

        tournamentSoloTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedType = tab.getPosition() == 0 ? "tournament" : "solo";
                fetchCategories();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        shimmer.startShimmer();
        fetchEverything();
    }

    private void fetchEverything() {
        fetchWallet();
        fetchBanners();
        fetchNotices();
        fetchCategories();
    }

    // ---------------------------------------------------------------
    // Wallet balance + My Matches counts
    // ---------------------------------------------------------------
    private void fetchWallet() {
        String url = apiBase + "home.php";

        JSONObject params = new JSONObject();
        try {
            params.put("member_id", userLocalStore.getLoggedInUser().getMemberid());
            params.put("api_token", userLocalStore.getLoggedInUser().getToken());
        } catch (JSONException ignored) {
        }

        JsonObjectRequest request = new JsonObjectRequest(
                com.android.volley.Request.Method.POST, url, params,
                this::handleWalletResponse,
                error -> finishLoading());

        request.setShouldCache(false);
        requestQueue.add(request);
    }

    private void handleWalletResponse(JSONObject response) {
        try {
            String status = response.getString("status");
            if (TextUtils.equals(status, "true")) {
                JSONObject data = response.getJSONObject("message");
                balanceText.setText(data.optString("wallet_balance", "0"));
                ongoingCount.setText(getString(R.string.ongoing) + " (" + data.optString("ongoing_count", "0") + ")");
                upcomingCount.setText(getString(R.string.upcoming) + " (" + data.optString("upcoming_count", "0") + ")");
                completedCount.setText(getString(R.string.completed) + " (" + data.optString("completed_count", "0") + ")");
            }
        } catch (JSONException ignored) {
        } finally {
            finishLoading();
        }
    }

    // ---------------------------------------------------------------
    // Banners (tap-through, from banners.php)
    // ---------------------------------------------------------------
    private void fetchBanners() {
        String url = apiBase + "banners.php";
        JsonObjectRequest request = new JsonObjectRequest(
                com.android.volley.Request.Method.POST, url, new JSONObject(),
                response -> {
                    List<Banner> banners = new ArrayList<>();
                    try {
                        if (TextUtils.equals(response.getString("status"), "true")) {
                            JSONArray arr = response.getJSONObject("message").optJSONArray("banners");
                            if (arr != null) {
                                for (int i = 0; i < arr.length(); i++) {
                                    JSONObject b = arr.getJSONObject(i);
                                    banners.add(new Banner(
                                            b.optString("image_url", ""),
                                            b.optString("redirect_url", "")));
                                }
                            }
                        }
                    } catch (JSONException ignored) {
                    }
                    setupBanner(banners);
                },
                error -> setupBanner(new ArrayList<>()));

        request.setShouldCache(false);
        requestQueue.add(request);
    }

    private void setupBanner(List<Banner> banners) {
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

    // ---------------------------------------------------------------
    // Notices (rotating strip, from notices.php)
    // ---------------------------------------------------------------
    private void fetchNotices() {
        String url = apiBase + "notices.php";
        JsonObjectRequest request = new JsonObjectRequest(
                com.android.volley.Request.Method.POST, url, new JSONObject(),
                response -> {
                    List<String> list = new ArrayList<>();
                    try {
                        if (TextUtils.equals(response.getString("status"), "true")) {
                            JSONArray arr = response.getJSONObject("message").optJSONArray("notices");
                            if (arr != null) {
                                for (int i = 0; i < arr.length(); i++) {
                                    list.add(arr.getString(i));
                                }
                            }
                        }
                    } catch (JSONException ignored) {
                    }
                    setupNotices(list);
                },
                error -> setupNotices(new ArrayList<>()));

        request.setShouldCache(false);
        requestQueue.add(request);
    }

    private void setupNotices(List<String> list) {
        stopNoticeRotation();
        notices = list;
        noticeIndex = 0;
        if (notices.isEmpty()) {
            announceText.setText("");
        } else {
            announceText.setText(notices.get(0));
            if (notices.size() > 1) {
                startNoticeRotation();
            }
        }
    }

    private void startNoticeRotation() {
        noticeRunnable = () -> {
            if (notices.isEmpty()) return;
            noticeIndex = (noticeIndex + 1) % notices.size();
            announceText.setText(notices.get(noticeIndex));
            noticeHandler.postDelayed(noticeRunnable, NOTICE_ROTATE_MS);
        };
        noticeHandler.postDelayed(noticeRunnable, NOTICE_ROTATE_MS);
    }

    private void stopNoticeRotation() {
        if (noticeRunnable != null) {
            noticeHandler.removeCallbacks(noticeRunnable);
        }
    }

    // ---------------------------------------------------------------
    // Categories grid (3-per-row, from categories.php)
    // ---------------------------------------------------------------
    private void fetchCategories() {
        String url = apiBase + "categories.php";
        JsonObjectRequest request = new JsonObjectRequest(
                com.android.volley.Request.Method.POST, url, new JSONObject(),
                response -> {
                    List<Category> categories = new ArrayList<>();
                    try {
                        if (TextUtils.equals(response.getString("status"), "true")) {
                            JSONArray arr = response.getJSONObject("message").optJSONArray("categories");
                            if (arr != null) {
                                for (int i = 0; i < arr.length(); i++) {
                                    JSONObject c = arr.getJSONObject(i);
                                    categories.add(new Category(
                                            c.optString("category_id", ""),
                                            c.optString("name", ""),
                                            c.optString("image_url", ""),
                                            c.optString("redirect_url", "")));
                                }
                            }
                        }
                    } catch (JSONException ignored) {
                    }
                    renderCategories(categories);
                },
                error -> renderCategories(new ArrayList<>()));

        request.setShouldCache(false);
        requestQueue.add(request);
    }

    private void renderCategories(List<Category> categories) {
        // Database is the only source of truth here - empty table means
        // an empty grid, never a placeholder card.
        noGameFound.setVisibility(categories.isEmpty() ? View.VISIBLE : View.GONE);
        categoriesRecyclerView.setAdapter(new CategoryAdapter(categories, this::openCategory));
        finishLoading();
    }

    private void openCategory(Category category) {
        String redirectUrl = category.getRedirectUrl();
        if (!TextUtils.isEmpty(redirectUrl)) {
            try {
                android.content.Intent intent = new android.content.Intent(
                        android.content.Intent.ACTION_VIEW, android.net.Uri.parse(redirectUrl));
                startActivity(intent);
                return;
            } catch (Exception ignored) {
                // fall through to the toast below
            }
        }
        Toast.makeText(requireContext(), category.getName() + " - coming soon", Toast.LENGTH_SHORT).show();
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
        stopNoticeRotation();
    }
}
