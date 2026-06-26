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

public class HandlerDashboardActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private int userId;
    private String userName;
    private String userDepartment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_handler_dashboard);

        dbHelper = new DBHelper(this);
        userId = getIntent().getIntExtra("userId", 0);
        userName = getIntent().getStringExtra("userName");
        userDepartment = getIntent().getStringExtra("userDepartment");

        // Update greeting
        TextView tvUserName = findViewById(R.id.tv_user_name);
        if (userName != null) {
            tvUserName.setText(userName + " 👋");
        }

        // Set up click listeners for stats cards
        CardView cardAssigned = findViewById(R.id.card_assigned);
        CardView cardInProgress = findViewById(R.id.card_in_progress);
        CardView cardResolved = findViewById(R.id.card_resolved);
        CardView cardEscalated = findViewById(R.id.card_escalated);

        cardAssigned.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openComplaintList("assigned");
            }
        });

        cardInProgress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openComplaintList("in_progress");
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
        List<Complaint> all = dbHelper.getComplaintsByDepartment(userDepartment);
        int assigned = 0, inProgress = 0, resolved = 0, escalated = 0;

        for (Complaint complaint : all) {
            String status = complaint.getStatus();
            if (status.equals("Submitted") || status.equals("Assigned")) {
                assigned++;
            } else if (status.equals("Under Review") || status.equals("In Progress")) {
                inProgress++;
            } else if (status.equals("Resolved") || status.equals("Closed")) {
                resolved++;
            } else if (status.equals("Escalated")) {
                escalated++;
            }
        }

        ((TextView) findViewById(R.id.tv_assigned)).setText(String.valueOf(assigned));
        ((TextView) findViewById(R.id.tv_in_progress)).setText(String.valueOf(inProgress));
        ((TextView) findViewById(R.id.tv_resolved)).setText(String.valueOf(resolved));
        ((TextView) findViewById(R.id.tv_escalated)).setText(String.valueOf(escalated));

        // Update metrics
        int weekResolved = dbHelper.getComplaintsResolvedInPastWeekByDepartment(userDepartment);
        int monthResolved = dbHelper.getComplaintsResolvedInPastMonthByDepartment(userDepartment);
        ((TextView) findViewById(R.id.tv_week)).setText(String.valueOf(weekResolved));
        ((TextView) findViewById(R.id.tv_month)).setText(String.valueOf(monthResolved));
    }

    private void openComplaintList(String filterType) {
        Intent intent = new Intent(this, ComplaintListActivity.class);
        intent.putExtra("filter_type", filterType);
        intent.putExtra("department", userDepartment);
        intent.putExtra("user_role", "handler");
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
