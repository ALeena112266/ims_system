package com.example.imssystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.imssystem.adapters.ComplaintAdapter;
import com.example.imssystem.helpers.DBHelper;
import com.example.imssystem.models.Complaint;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private int userId;
    private String userName;
    private RecyclerView recyclerView;
    private ComplaintAdapter adapter;
    private List<Complaint> complaintList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        dbHelper = new DBHelper(this);
        userId = getIntent().getIntExtra("userId", 0);
        userName = getIntent().getStringExtra("userName");

        recyclerView = findViewById(R.id.recycler_complaints);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        complaintList = new ArrayList<>();
        adapter = new ComplaintAdapter(this, complaintList,
                new ComplaintAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Complaint complaint) {
                        Intent intent = new Intent(HistoryActivity.this, ComplaintDetailsActivity.class);
                        intent.putExtra("complaintId", complaint.getId());
                        intent.putExtra("userId", userId);
                        intent.putExtra("userName", userName);
                        intent.putExtra("userRole", "student");
                        startActivity(intent);
                    }
                },
                null,
                "student");
        recyclerView.setAdapter(adapter);

        // Bottom Navigation Setup
        setupBottomNav();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadComplaints();
    }

    private void loadComplaints() {
        complaintList.clear();
        complaintList.addAll(dbHelper.getAllComplaintsByStudentId(userId));
        adapter.notifyDataSetChanged();
    }

    private void setupBottomNav() {
        LinearLayout navHome = findViewById(R.id.nav_home);
        LinearLayout navHistory = findViewById(R.id.nav_history);
        LinearLayout navAlerts = findViewById(R.id.nav_alerts);
        LinearLayout navProfile = findViewById(R.id.nav_profile);

        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(HistoryActivity.this, DashboardActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("userName", userName);
            startActivity(intent);
            finish();
        });

        navHistory.setOnClickListener(v -> {
            // Already on history
        });

        navAlerts.setOnClickListener(v -> {
            Intent intent = new Intent(HistoryActivity.this, AlertsActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("userName", userName);
            startActivity(intent);
            finish();
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(HistoryActivity.this, ProfileActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("userName", userName);
            startActivity(intent);
            finish();
        });
    }
}
