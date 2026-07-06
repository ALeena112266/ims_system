package com.example.imssystem;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

public class AlertsActivity extends AppCompatActivity {
    private int userId;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alerts);

        userId = getIntent().getIntExtra("userId", 0);
        userName = getIntent().getStringExtra("userName");

        setupBottomNav();
    }

    private void setupBottomNav() {
        LinearLayout navHome = findViewById(R.id.nav_home);
        LinearLayout navHistory = findViewById(R.id.nav_history);
        LinearLayout navAlerts = findViewById(R.id.nav_alerts);
        LinearLayout navProfile = findViewById(R.id.nav_profile);

        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(AlertsActivity.this, DashboardActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("userName", userName);
            startActivity(intent);
            finish();
        });

        navHistory.setOnClickListener(v -> {
            Intent intent = new Intent(AlertsActivity.this, HistoryActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("userName", userName);
            startActivity(intent);
            finish();
        });

        navAlerts.setOnClickListener(v -> {
            // Already on alerts
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(AlertsActivity.this, ProfileActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("userName", userName);
            startActivity(intent);
            finish();
        });
    }
}
