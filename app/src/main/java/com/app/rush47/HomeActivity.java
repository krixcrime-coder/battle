package com.app.rush47;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.app.rush47.fragments.AccountFragment;
import com.app.rush47.fragments.EarnFragment;
import com.app.rush47.fragments.PlayFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Main dashboard shell after login: bottom navigation with exactly 3
 * tabs - Earn, Play, Account - each swapping the fragment shown in
 * fragment_container.
 */
public class HomeActivity extends AppCompatActivity {

    private static final String TAG_EARN = "earn";
    private static final String TAG_PLAY = "play";
    private static final String TAG_ACCOUNT = "account";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_earn) {
                showFragment(new EarnFragment(), TAG_EARN);
                return true;
            } else if (id == R.id.nav_play) {
                showFragment(new PlayFragment(), TAG_PLAY);
                return true;
            } else if (id == R.id.nav_account) {
                showFragment(new AccountFragment(), TAG_ACCOUNT);
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            // Play is the default landing tab (matches the original app).
            bottomNav.setSelectedItemId(R.id.nav_play);
        }
    }

    private void showFragment(Fragment fragment, String tag) {
        FragmentManager fm = getSupportFragmentManager();
        Fragment existing = fm.findFragmentByTag(tag);

        FragmentTransaction transaction = fm.beginTransaction();
        for (Fragment f : fm.getFragments()) {
            transaction.hide(f);
        }
        if (existing != null) {
            transaction.show(existing);
        } else {
            transaction.add(R.id.fragment_container, fragment, tag);
        }
        transaction.commit();
    }
}
