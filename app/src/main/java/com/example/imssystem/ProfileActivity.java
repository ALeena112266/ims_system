package com.example.imssystem;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.imssystem.helpers.DBHelper;
import com.example.imssystem.models.User;

public class ProfileActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private int userId;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dbHelper = new DBHelper(this);
        userId = getIntent().getIntExtra("userId", 0);
        userName = getIntent().getStringExtra("userName");

        Button btnLogout = findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        loadUserInfo();
        setupBottomNav();
    }

    private void loadUserInfo() {
        User user = dbHelper.getUserById(userId);
        if (user != null) {
            TextView tvName = findViewById(R.id.tv_name);
            TextView tvEmail = findViewById(R.id.tv_email);
            TextView tvStudentId = findViewById(R.id.tv_student_id);
            TextView tvAvatar = findViewById(R.id.tv_avatar);

            tvName.setText(user.getName());
            tvEmail.setText(user.getEmail());
            tvStudentId.setText(user.getStudentId() != null ? user.getStudentId() : "N/A");

            if (user.getName() != null && !user.getName().isEmpty()) {
                tvAvatar.setText(user.getName().substring(0, 1).toUpperCase());
            }
        }
    }

    private void setupBottomNav() {
        LinearLayout navHome = findViewById(R.id.nav_home);
        LinearLayout navHistory = findViewById(R.id.nav_history);
        LinearLayout navAlerts = findViewById(R.id.nav_alerts);
        LinearLayout navProfile = findViewById(R.id.nav_profile);

        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, DashboardActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("userName", userName);
            startActivity(intent);
            finish();
        });

        navHistory.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, HistoryActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("userName", userName);
            startActivity(intent);
            finish();
        });

        navAlerts.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, AlertsActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("userName", userName);
            startActivity(intent);
            finish();
        });

        navProfile.setOnClickListener(v -> {
            // Already on profile
        });
    }
}
