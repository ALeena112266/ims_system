package com.example.imssystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.imssystem.helpers.FirebaseRepository;
import com.example.imssystem.models.Complaint;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {
    private FirebaseRepository repo;
    private String userId;
    private String userName;
    private TextView tvTotalComplaints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        repo = FirebaseRepository.getInstance();
        userId = getIntent().getStringExtra("userId");
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
            intent.putExtra("userName", userName);
            startActivity(intent);
        });

        LinearLayout navAlerts = findViewById(R.id.nav_alerts);
        navAlerts.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, AlertsActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("userName", userName);
            startActivity(intent);
        });

        LinearLayout navProfile = findViewById(R.id.nav_profile);
        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, ProfileActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("userName", userName);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStats();
    }

    private void updateStats() {
        repo.getAllComplaintsByStudentId(userId, new FirebaseRepository.OnCompleteListener<List<Complaint>>() {
            @Override
            public void onSuccess(List<Complaint> result) {
                tvTotalComplaints.setText(String.valueOf(result != null ? result.size() : 0));
            }

            @Override
            public void onFailure(Exception e) {
                tvTotalComplaints.setText("0");
            }
        });
    }
}
