package com.example.imssystem;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
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

        // Setup bottom nav (only for admin/handler)
        LinearLayout bottomNav = findViewById(R.id.bottom_nav);
        if ("admin".equals(userRole) || "handler".equals(userRole)) {
            bottomNav.setVisibility(View.VISIBLE);

            LinearLayout navHome = findViewById(R.id.nav_home);
            LinearLayout navAllComplaints = findViewById(R.id.nav_all_complaints);
            LinearLayout navManageUsers = findViewById(R.id.nav_manage_users);

            // Set current item
            if ("all".equals(filterType)) {
                ((ImageView) navAllComplaints.getChildAt(0)).setImageTintList(getColorStateList(R.color.purple_start));
                ((TextView) navAllComplaints.getChildAt(1)).setTextColor(getColor(R.color.purple_start));
            } else {
                ((ImageView) navHome.getChildAt(0)).setImageTintList(getColorStateList(R.color.purple_start));
                ((TextView) navHome.getChildAt(1)).setTextColor(getColor(R.color.purple_start));
            }

            // Home nav
            navHome.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent;
                    if ("admin".equals(userRole)) {
                        intent = new Intent(ComplaintListActivity.this, AdminDashboardActivity.class);
                    } else {
                        intent = new Intent(ComplaintListActivity.this, HandlerDashboardActivity.class);
                        intent.putExtra("userDepartment", department);
                    }
                    intent.putExtra("userId", userId);
                    intent.putExtra("userName", userName);
                    intent.putExtra("userRole", userRole);
                    startActivity(intent);
                    finish();
                }
            });

            // All complaints nav
            navAllComplaints.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    filterType = "all";
                    tvTitle.setText(getTitleForFilter(filterType));
                    ((ImageView) navAllComplaints.getChildAt(0)).setImageTintList(getColorStateList(R.color.purple_start));
                    ((TextView) navAllComplaints.getChildAt(1)).setTextColor(getColor(R.color.purple_start));
                    ((ImageView) navHome.getChildAt(0)).setImageTintList(getColorStateList(R.color.gray_medium));
                    ((TextView) navHome.getChildAt(1)).setTextColor(getColor(R.color.gray_medium));
                    loadComplaints();
                }
            });

            // Manage users nav (only for admin)
            if ("admin".equals(userRole)) {
                navManageUsers.setVisibility(View.VISIBLE);
                navManageUsers.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ComplaintListActivity.this, UserListActivity.class);
                        intent.putExtra("userId", userId);
                        intent.putExtra("userName", userName);
                        intent.putExtra("userRole", userRole);
                        startActivity(intent);
                    }
                });
            } else {
                navManageUsers.setVisibility(View.GONE);
            }
        }

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
                        intent.putExtra("userId", userId);
                        intent.putExtra("userName", userName);
                        intent.putExtra("userRole", userRole);
                        if (department != null) {
                            intent.putExtra("department", department);
                        }
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
