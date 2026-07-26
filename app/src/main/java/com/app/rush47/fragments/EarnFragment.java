package com.app.rush47.fragments;

import android.content.Intent;
import android.os.Bundle;
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

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.rush47.MyReferralsActivity;
import com.app.rush47.MyWalletActivity;
import com.app.rush47.R;
import com.app.rush47.ReferAndEarnActivity;
import com.app.rush47.adapters.EarnBannerAdapter;
import com.app.rush47.models.Banner;
import com.app.rush47.utils.UserLocalStore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Earn tab: wallet quick-glance strip (Deposited / Winning / Bonus) plus
 * four menu cards - Refer & Earn, My Wallet, My Referrals, Watch & Earn.
 * All data comes from earn.php, requires login.
 */
public class EarnFragment extends Fragment {

    private TextView balanceText, depositedText, winningText, bonusText;
    private SwipeRefreshLayout pullToRefresh;
    private RecyclerView earnBannerList;

    private RequestQueue requestQueue;
    private UserLocalStore userLocalStore;
    private String apiBase;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_earn, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requestQueue = Volley.newRequestQueue(requireContext().getApplicationContext());
        userLocalStore = new UserLocalStore(requireContext());
        apiBase = getString(R.string.api);

        balanceText = view.findViewById(R.id.balinearn);
        depositedText = view.findViewById(R.id.depositedText);
        winningText = view.findViewById(R.id.winningText);
        bonusText = view.findViewById(R.id.bonusText);
        pullToRefresh = view.findViewById(R.id.pullToRefreshEarn);

        earnBannerList = view.findViewById(R.id.earnBannerList);
        earnBannerList.setLayoutManager(new LinearLayoutManager(requireContext()));
        earnBannerList.setHasFixedSize(false);

        pullToRefresh.setOnRefreshListener(() -> {
            fetchEarnData();
            fetchEarnBanners();
        });

        view.findViewById(R.id.earnShare).setOnClickListener(v -> shareReferralCode());

        view.findViewById(R.id.menuReferAndEarn).setOnClickListener(v ->
                startActivity(new Intent(getContext(), ReferAndEarnActivity.class)));

        view.findViewById(R.id.menuMyWallet).setOnClickListener(v ->
                startActivity(new Intent(getContext(), MyWalletActivity.class)));

        view.findViewById(R.id.menuMyReferrals).setOnClickListener(v ->
                startActivity(new Intent(getContext(), MyReferralsActivity.class)));

        view.findViewById(R.id.menuWatchAndEarn).setOnClickListener(v ->
                Toast.makeText(requireContext(), R.string.coming_soon, Toast.LENGTH_SHORT).show());

        fetchEarnData();
        fetchEarnBanners();
    }

    // ---------------------------------------------------------------
    // Banners: earn_banners.php - a completely separate table from the
    // Play tab's banners.php, managed independently (Refer & Earn,
    // How To Add Coins, WhatsApp Channel, Customer Support, Add Coin
    // Bonus, Watch & Earn, etc). If the server has none yet (or the
    // request fails), the local "Wait For End" banner is shown so the
    // section is never empty.
    // ---------------------------------------------------------------
    private void fetchEarnBanners() {
        String url = apiBase + "earn_banners.php";

        JSONObject params = new JSONObject();

        JsonObjectRequest request = new JsonObjectRequest(
                com.android.volley.Request.Method.POST, url, params,
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
                    setupEarnBanners(banners);
                },
                error -> setupEarnBanners(new ArrayList<>()));

        request.setShouldCache(false);
        requestQueue.add(request);
    }

    private void setupEarnBanners(List<Banner> banners) {
        if (banners.isEmpty()) {
            // Local fallback drawable, shown until the server has real banners.
            banners.add(new Banner("android.resource://" + requireContext().getPackageName()
                    + "/" + R.drawable.banner_wait_for_end, ""));
        }
        earnBannerList.setAdapter(new EarnBannerAdapter(banners));
    }

    private void fetchEarnData() {
        String url = apiBase + "earn.php";

        JSONObject params = new JSONObject();
        try {
            params.put("member_id", userLocalStore.getLoggedInUser().getMemberid());
            params.put("api_token", userLocalStore.getLoggedInUser().getToken());
        } catch (JSONException ignored) {
        }

        JsonObjectRequest request = new JsonObjectRequest(
                com.android.volley.Request.Method.POST, url, params,
                this::handleEarnResponse,
                error -> stopRefreshing());

        request.setShouldCache(false);
        requestQueue.add(request);
    }

    private void handleEarnResponse(JSONObject response) {
        try {
            if (TextUtils.equals(response.getString("status"), "true")) {
                JSONObject data = response.getJSONObject("message");
                balanceText.setText(data.optString("wallet_balance", "0"));
                depositedText.setText(data.optString("deposited", "0"));
                winningText.setText(data.optString("winning", "0"));
                bonusText.setText(data.optString("bonus", "0"));
                referralCode = data.optString("referral_code", "");
            }
        } catch (JSONException ignored) {
        } finally {
            stopRefreshing();
        }
    }

    private String referralCode = "";

    private void shareReferralCode() {
        if (TextUtils.isEmpty(referralCode)) {
            Toast.makeText(requireContext(), R.string.coming_soon, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                "Join Rush47 and use my referral code " + referralCode + " to get a signup bonus!");
        startActivity(Intent.createChooser(shareIntent, getString(R.string.refer_and_earn)));
    }

    private void stopRefreshing() {
        if (pullToRefresh != null) {
            pullToRefresh.setRefreshing(false);
        }
    }
}
