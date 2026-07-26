package com.app.rush47.adapters;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.rush47.R;
import com.app.rush47.ReferAndEarnActivity;
import com.app.rush47.models.Banner;
import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Feeds banner cards into the vertical RecyclerView on the Earn tab
 * (@id/earnBannerList in fragment_earn.xml). Same Banner model as the
 * Play tab's auto-scroll pager, but fed from earn_banners.php (its own
 * table) instead - these are stacked full-width instead of swiped.
 *
 * Tapping a banner: if redirect_url is the special "app://refer_and_earn"
 * value, opens the in-app Refer & Earn screen directly. Any other
 * non-empty redirect_url opens in the browser, same as Play.
 */
public class EarnBannerAdapter extends RecyclerView.Adapter<EarnBannerAdapter.ViewHolder> {

    private final List<Banner> banners;

    public EarnBannerAdapter(List<Banner> banners) {
        this.banners = banners;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_earn_banner, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Banner banner = banners.get(position);

        Glide.with(holder.image.getContext())
                .load(banner.getImageUrl())
                .into(holder.image);

        String redirectUrl = banner.getRedirectUrl();
        if (TextUtils.equals(redirectUrl, "app://refer_and_earn")) {
            holder.image.setOnClickListener(v ->
                    v.getContext().startActivity(new Intent(v.getContext(), ReferAndEarnActivity.class)));
        } else if (!TextUtils.isEmpty(redirectUrl)) {
            holder.image.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(redirectUrl));
                    v.getContext().startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(v.getContext(), "Couldn't open link", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            holder.image.setOnClickListener(null);
            holder.image.setClickable(false);
        }
    }

    @Override
    public int getItemCount() {
        return banners.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView image;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.earnBannerImage);
        }
    }
}
