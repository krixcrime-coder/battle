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
import androidx.viewpager.widget.PagerAdapter;

import com.app.rush47.R;
import com.app.rush47.models.Banner;
import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Feeds banner slides into the {@code kk_pager} ViewPager on the Play tab.
 * Tapping a banner opens its redirect_url (from banners.php) in the
 * browser - banners with no redirect_url just aren't tappable.
 * Auto-scrolling itself is handled by PlayFragment with a Handler, since a
 * plain PagerAdapter has no scheduling of its own.
 */
public class BannerPagerAdapter extends PagerAdapter {

    private final List<Banner> banners;

    public BannerPagerAdapter(List<Banner> banners) {
        this.banners = banners;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = LayoutInflater.from(container.getContext())
                .inflate(R.layout.item_banner_slide, container, false);
        ImageView image = view.findViewById(R.id.bannerImage);
        Banner banner = banners.get(position);

        Glide.with(container.getContext())
                .load(banner.getImageUrl())
                .into(image);

        String redirectUrl = banner.getRedirectUrl();
        if (!TextUtils.isEmpty(redirectUrl)) {
            image.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(redirectUrl));
                    container.getContext().startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(container.getContext(), "Couldn't open link", Toast.LENGTH_SHORT).show();
                }
            });
        }

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return banners.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
}
