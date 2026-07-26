package com.app.rush47;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.app.rush47.fragments.PlayFragment;
import com.app.rush47.fragments.StubTabFragment;
import com.google.android.material.tabs.TabLayout;

/**
 * Main dashboard shell after login: bottom TabLayout + ViewPager, matched
 * against the original decompiled activity_home.xml.
 *
 * Only the Play tab is built for real right now. The other five keep the
 * tab bar complete (and matching the original app) but just show a
 * "coming soon" placeholder until each one is built.
 */
public class HomeActivity extends AppCompatActivity {

    private static final String[] TAB_TITLES = {"Play", "Upcoming", "Ongoing", "Result", "Earn", "Me"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ViewPager viewPager = findViewById(R.id.view_pager);
        TabLayout tabLayout = findViewById(R.id.tabs);

        viewPager.setAdapter(new HomePagerAdapter(getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(TAB_TITLES.length - 1);
        tabLayout.setupWithViewPager(viewPager);
    }

    private static class HomePagerAdapter extends FragmentPagerAdapter {

        HomePagerAdapter(@NonNull FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return new PlayFragment();
            }
            return StubTabFragment.newInstance(TAB_TITLES[position]);
        }

        @Override
        public int getCount() {
            return TAB_TITLES.length;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return TAB_TITLES[position];
        }
    }
}
