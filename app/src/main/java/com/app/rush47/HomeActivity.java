package com.app.rush47;

import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.app.rush47.utils.UserLocalStore;

/**
 * Minimal placeholder Home screen so the auth flow has somewhere to land.
 * The real dashboard (tabs, Ludo, Lottery, Wallet etc.) is the next part
 * we build on top of this, following the module list already agreed on.
 */
public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        UserLocalStore userLocalStore = new UserLocalStore(this);
        TextView welcome = findViewById(R.id.home_welcome_text);
        String firstName = userLocalStore.getLoggedInUser().getFirstName();
        if (firstName != null && !firstName.isEmpty()) {
            welcome.setText(getString(R.string.home_welcome) + " " + firstName);
        }

        Button logoutButton = findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(v -> {
            userLocalStore.clearUserData();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        });
    }
}
