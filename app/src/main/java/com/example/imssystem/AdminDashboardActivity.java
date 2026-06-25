package com.example.imssystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.imssystem.helpers.DBHelper;
import com.example.imssystem.models.Complaint;

import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private int userId;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        dbHelper = new DBHelper(this);
        userId = getIntent().getIntExtra("userId", 0);
        userName = getIntent().getStringExtra("userName");

        // Update greeting
        TextView tvUserName = findViewById(R.id.tv_user_name);
        if (userName != null) {
            tvUserName.setText(userName + " 👋");
        }

        // Set up click listeners for stats cards
        CardView cardTotal = findViewById(R.id.card_total);
        CardView cardActive = findViewById(R.id.card_active);
        CardView cardResolved = findViewById(R.id.card_resolved);
        CardView cardEscalated = findViewById(R.id.card_escalated);

        cardTotal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openComplaintList("all");
            }
        });

        cardActive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openComplaintList("active");
            }
        });

        cardResolved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openComplaintList("resolved");
            }
        });

        cardEscalated.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openComplaintList("escalated");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStats();
    }

    private void updateStats() {
        List<Complaint> all = dbHelper.getAllComplaints();
        int total = all.size();
        int active = 0, resolved = 0, escalated = 0;

        for (Complaint complaint : all) {
            String status = complaint.getStatus();
            if (status.equals("Submitted") || status.equals("Assigned") || status.equals("Under Review") || status.equals("In Progress")) {
                active++;
            } else if (status.equals("Resolved") || status.equals("Closed")) {
                resolved++;
            } else if (status.equals("Escalated")) {
                escalated++;
            }
        }

        ((TextView) findViewById(R.id.tv_total)).setText(String.valueOf(total));
        ((TextView) findViewById(R.id.tv_active)).setText(String.valueOf(active));
        ((TextView) findViewById(R.id.tv_resolved)).setText(String.valueOf(resolved));
        ((TextView) findViewById(R.id.tv_escalated)).setText(String.valueOf(escalated));
    }

    private void openComplaintList(String filterType) {
        Intent intent = new Intent(this, ComplaintListActivity.class);
        intent.putExtra("filter_type", filterType);
        intent.putExtra("user_role", "admin");
        intent.putExtra("user_id", userId);
        intent.putExtra("user_name", userName);
        startActivity(intent);
    }

    public void onLogoutClick(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
