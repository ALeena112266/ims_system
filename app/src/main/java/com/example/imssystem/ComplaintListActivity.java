package com.example.imssystem;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.imssystem.adapters.ComplaintAdapter;
import com.example.imssystem.helpers.DBHelper;
import com.example.imssystem.models.Complaint;

import java.util.ArrayList;
import java.util.List;

public class ComplaintListActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private RecyclerView recyclerView;
    private ComplaintAdapter adapter;
    private List<Complaint> complaintList;
    private String filterType;
    private String department;
    private int userId;
    private String userRole;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_list);

        dbHelper = new DBHelper(this);
        filterType = getIntent().getStringExtra("filter_type");
        department = getIntent().getStringExtra("department");
        userId = getIntent().getIntExtra("user_id", -1);
        userRole = getIntent().getStringExtra("user_role");
        userName = getIntent().getStringExtra("user_name");

        // Set title
        TextView tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText(getTitleForFilter(filterType));

        // Back button
        ImageButton backArrow = findViewById(R.id.back_arrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recycler_complaints);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        complaintList = new ArrayList<>();
        adapter = new ComplaintAdapter(this, complaintList,
                new ComplaintAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Complaint complaint) {
                        Intent intent = new Intent(ComplaintListActivity.this, ComplaintDetailsActivity.class);
                        intent.putExtra("complaintId", complaint.getId());
                        startActivity(intent);
                    }
                },
                new ComplaintAdapter.OnStatusChangeListener() {
                    @Override
                    public void onStatusChange(Complaint complaint) {
                        showStatusChangeDialog(complaint);
                    }
                },
                userRole);
        recyclerView.setAdapter(adapter);
    }

    private void showStatusChangeDialog(final Complaint complaint) {
        final String[] statuses = {"Submitted", "Under Review", "Assigned", "In Progress", "Resolved", "Closed", "Escalated"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Complaint Status");
        builder.setItems(statuses, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newStatus = statuses[which];
                int rowsUpdated = dbHelper.updateComplaintStatus(complaint.getId(), newStatus, userName, "Status changed to " + newStatus);
                if (rowsUpdated > 0) {
                    Toast.makeText(ComplaintListActivity.this, "Status updated successfully!", Toast.LENGTH_SHORT).show();
                    loadComplaints();
                } else {
                    Toast.makeText(ComplaintListActivity.this, "Failed to update status!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadComplaints();
    }

    private void loadComplaints() {
        complaintList.clear();
        List<Complaint> allComplaints;

        if ("admin".equals(userRole)) {
            allComplaints = dbHelper.getAllComplaints();
        } else if ("handler".equals(userRole)) {
            allComplaints = dbHelper.getComplaintsByDepartment(department);
        } else {
            allComplaints = dbHelper.getAllComplaintsByStudentId(userId);
        }

        for (Complaint complaint : allComplaints) {
            if (matchesFilter(complaint, filterType)) {
                complaintList.add(complaint);
            }
        }

        adapter.notifyDataSetChanged();
    }

    private boolean matchesFilter(Complaint complaint, String filterType) {
        if (filterType == null) return true;

        String status = complaint.getStatus();
        switch (filterType) {
            case "active":
                return !status.equals("Resolved") && !status.equals("Closed") && !status.equals("Escalated");
            case "resolved":
                return status.equals("Resolved") || status.equals("Closed");
            case "escalated":
                return status.equals("Escalated");
            case "pending":
                return status.equals("Submitted");
            case "assigned":
                return status.equals("Assigned");
            case "in_progress":
                return status.equals("Under Review") || status.equals("In Progress");
            default:
                return true;
        }
    }

    private String getTitleForFilter(String filterType) {
        if (filterType == null) return "All Complaints";

        switch (filterType) {
            case "active":
                return "Active Complaints";
            case "resolved":
                return "Resolved Complaints";
            case "escalated":
                return "Escalated Complaints";
            case "pending":
                return "Pending Complaints";
            case "assigned":
                return "Assigned Complaints";
            case "in_progress":
                return "In Progress Complaints";
            default:
                return "All Complaints";
        }
    }
}
