package com.example.imssystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.imssystem.adapters.TimelineAdapter;
import com.example.imssystem.helpers.FirebaseRepository;
import com.example.imssystem.models.StatusLog;
import java.util.ArrayList;
import java.util.List;

public class AlertsActivity extends AppCompatActivity {
    private FirebaseRepository repo;
    private String userId;
    private String userName;
    private RecyclerView recyclerView;
    private TimelineAdapter adapter;
    private List<StatusLog> alertList;
    private LinearLayout emptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_alerts);

            repo = FirebaseRepository.getInstance();
            userId = getIntent().getStringExtra("userId");
            userName = getIntent().getStringExtra("userName");

            emptyState = findViewById(R.id.empty_state);
            recyclerView = findViewById(R.id.recycler_alerts);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            alertList = new ArrayList<>();
            adapter = new TimelineAdapter(this, alertList);
            recyclerView.setAdapter(adapter);

            setupBottomNav();
        } catch (Exception e) {
            Toast.makeText(this, "Error loading alerts: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAlerts();
    }

    private void loadAlerts() {
        repo.getAlertsForStudent(userId, new FirebaseRepository.OnCompleteListener<List<StatusLog>>() {
            @Override
            public void onSuccess(List<StatusLog> result) {
                alertList.clear();
                if (result != null) {
                    alertList.addAll(result);
                }
                adapter.notifyDataSetChanged();
                if (alertList.isEmpty()) {
                    emptyState.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    emptyState.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Exception e) {
                alertList.clear();
                adapter.notifyDataSetChanged();
                emptyState.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                String msg = e != null ? e.getMessage() : "Unknown error";
                if (msg == null || msg.isEmpty()) msg = "Check connection or permissions.";
                Toast.makeText(AlertsActivity.this,
                        "Firebase read failed: " + msg, Toast.LENGTH_LONG).show();
            }
        });
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
