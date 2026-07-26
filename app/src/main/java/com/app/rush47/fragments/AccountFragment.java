package com.app.rush47.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.app.rush47.R;
import com.app.rush47.utils.UserLocalStore;

/**
 * Account tab. This is intentionally just the page shell for now: every
 * row (My Profile, My Wallet, My Referrals, Change Password, Customer
 * Support, About Us, Logout) shows a "coming soon" toast instead of
 * opening a real screen. Wire each row to its real Activity later, one
 * at a time, as each screen gets built.
 */
public class AccountFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView usernameText = view.findViewById(R.id.accountUsername);
        String username = new UserLocalStore(requireContext()).getLoggedInUser().getUsername();
        if (username != null && !username.isEmpty()) {
            usernameText.setText(username);
        }

        setupRow(view, R.id.rowMyProfile, R.drawable.myprofile, R.string.my_profile);
        setupRow(view, R.id.rowMyWallet, R.drawable.mywallet, R.string.my_wallet);
        setupRow(view, R.id.rowMyReferrals, R.drawable.myreferrals, R.string.my_referrals);
        setupRow(view, R.id.rowChangePassword, R.drawable.copy_icon, R.string.change_password);
        setupRow(view, R.id.rowCustomerSupport, R.drawable.customersupport, R.string.customer_support);
        setupRow(view, R.id.rowAboutUs, R.drawable.aboutus, R.string.about_us);
        setupRow(view, R.id.rowLogout, R.drawable.logout, R.string.logout);
    }

    /**
     * Fills in one included menu row's icon/title and makes tapping it
     * show "coming soon" - every row behaves the same way for now.
     */
    private void setupRow(View parent, int rowId, int iconRes, int titleRes) {
        View row = parent.findViewById(rowId);
        if (row == null) return;

        ImageView icon = row.findViewById(R.id.menuIcon);
        TextView title = row.findViewById(R.id.menuTitle);
        icon.setImageResource(iconRes);
        title.setText(titleRes);

        row.setOnClickListener(v ->
                Toast.makeText(requireContext(), R.string.coming_soon, Toast.LENGTH_SHORT).show());
    }
}
