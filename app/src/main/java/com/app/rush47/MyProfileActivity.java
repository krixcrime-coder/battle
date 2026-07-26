package com.app.rush47;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.app.rush47.models.CurrentUser;
import com.app.rush47.utils.UserLocalStore;

/**
 * Read-only profile screen: shows the info already saved on this device
 * from login/signup (name, username, email, mobile). There's no
 * profile-edit backend endpoint yet, so this is view-only for now -
 * wire it up to a real update-profile API once one exists.
 */
public class MyProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        CurrentUser user = new UserLocalStore(this).getLoggedInUser();

        TextView fullName = findViewById(R.id.profileFullName);
        TextView username = findViewById(R.id.profileUsername);
        TextView email = findViewById(R.id.profileEmail);
        TextView mobile = findViewById(R.id.profileMobile);

        String name = (user.getFirstName() + " " + user.getLastName()).trim();
        fullName.setText(TextUtils.isEmpty(name) ? "-" : name);
        username.setText(emptyToDash(user.getUsername()));
        email.setText(emptyToDash(user.getEmail()));
        mobile.setText(emptyToDash(user.getMobile()));
    }

    private String emptyToDash(String value) {
        return TextUtils.isEmpty(value) ? "-" : value;
    }
}
