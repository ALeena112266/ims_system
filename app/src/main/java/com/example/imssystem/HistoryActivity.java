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
import com.example.imssystem.helpers.FirebaseRepository;
import com.example.imssystem.models.Complaint;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {
    private FirebaseRepository repo;
    private String userId;
    private String userName;
    private RecyclerView recyclerView;
    private ComplaintAdapter adapter;
    private List<Complaint> complaintList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        repo = FirebaseRepository.getInstance();
        userId = getIntent().getStringExtra("userId");
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

        setupBottomNav();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadComplaints();
    }

    private void loadComplaints() {
        repo.getAllComplaintsByStudentId(userId, new FirebaseRepository.OnCompleteListener<List<Complaint>>() {
            @Override
            public void onSuccess(List<Complaint> result) {
                complaintList.clear();
                if (result != null) {
                    complaintList.addAll(result);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                complaintList.clear();
                adapter.notifyDataSetChanged();
            }
        });
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
