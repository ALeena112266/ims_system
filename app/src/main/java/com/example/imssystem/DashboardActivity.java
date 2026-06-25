package com.example.imssystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.imssystem.adapters.ComplaintAdapter;
import com.example.imssystem.helpers.DBHelper;
import com.example.imssystem.models.Complaint;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private int userId;
    private String userName;
    private RecyclerView recyclerView;
    private ComplaintAdapter adapter;
    private List<Complaint> complaintList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        dbHelper = new DBHelper(this);
        userId = getIntent().getIntExtra("userId", 0);
        userName = getIntent().getStringExtra("userName");

        // Update greeting
        TextView tvUserName = findViewById(R.id.tv_user_name);
        if (userName != null) {
            tvUserName.setText(userName + " 👋");
        }

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recycler_complaints);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        complaintList = new ArrayList<>();
        adapter = new ComplaintAdapter(this, complaintList,
                new ComplaintAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Complaint complaint) {
                        Intent intent = new Intent(DashboardActivity.this, ComplaintDetailsActivity.class);
                        intent.putExtra("complaintId", complaint.getId());
                        startActivity(intent);
                    }
                },
                null,
                "student");
        recyclerView.setAdapter(adapter);

        // New complaint button
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

        // Update stats
        updateStats();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadComplaints();
        updateStats();
    }

    private void loadComplaints() {
        complaintList.clear();
        complaintList.addAll(dbHelper.getAllComplaintsByStudentId(userId));
        adapter.notifyDataSetChanged();
    }

    private void updateStats() {
        // We'll just keep static numbers for now, but you could calculate from DB
        // For example:
        // int active = dbHelper.getActiveComplaintsCount();
        // But let's leave it as static for simplicity right now
    }
}
