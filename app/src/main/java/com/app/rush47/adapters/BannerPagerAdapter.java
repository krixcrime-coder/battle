package com.app.rush47.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.app.rush47.R;
import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Feeds banner image URLs into the {@code kk_pager} ViewPager on the Play tab.
 * Auto-scrolling itself is handled by PlayFragment with a Handler, since a
 * plain PagerAdapter has no scheduling of its own.
 */
public class BannerPagerAdapter extends PagerAdapter {

    private final List<String> bannerUrls;

    public BannerPagerAdapter(List<String> bannerUrls) {
        this.bannerUrls = bannerUrls;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = LayoutInflater.from(container.getContext())
                .inflate(R.layout.item_banner_slide, container, false);
        ImageView image = view.findViewById(R.id.bannerImage);
        Glide.with(container.getContext())
                .load(bannerUrls.get(position))
                .into(image);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return bannerUrls.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
}
