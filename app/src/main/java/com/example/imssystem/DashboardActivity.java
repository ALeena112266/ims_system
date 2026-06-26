package com.example.imssystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.imssystem.helpers.DBHelper;

public class DashboardActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private int userId;
    private String userName;
    private TextView tvTotalComplaints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        dbHelper = new DBHelper(this);
        userId = getIntent().getIntExtra("userId", 0);
        userName = getIntent().getStringExtra("userName");

        TextView tvUserName = findViewById(R.id.tv_user_name);
        if (userName != null) {
            tvUserName.setText(userName + " 👋");
        }

        tvTotalComplaints = findViewById(R.id.tv_total_complaints);

        Button newComplaintBtn = findViewById(R.id.new_complaint_btn);
        newComplaintBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, ChooseTemplateActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("userName", userName);
                startActivity(intent);
            }
        });

        LinearLayout navHistory = findViewById(R.id.nav_history);
        navHistory.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, HistoryActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        LinearLayout navAlerts = findViewById(R.id.nav_alerts);
        navAlerts.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, AlertsActivity.class);
            startActivity(intent);
        });

        LinearLayout navProfile = findViewById(R.id.nav_profile);
        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, ProfileActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStats();
    }

    private void updateStats() {
        int total = dbHelper.getAllComplaintsByStudentId(userId).size();
        tvTotalComplaints.setText(String.valueOf(total));
    }
}
