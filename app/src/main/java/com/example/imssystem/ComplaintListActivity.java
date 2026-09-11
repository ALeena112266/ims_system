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
import com.example.imssystem.helpers.FirebaseRepository;
import com.example.imssystem.models.Complaint;

import java.util.ArrayList;
import java.util.List;

public class ComplaintListActivity extends AppCompatActivity {
    private FirebaseRepository repo;
    private RecyclerView recyclerView;
    private ComplaintAdapter adapter;
    private List<Complaint> complaintList;
    private String filterType;
    private String department;
    private String userId;
    private String userRole;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_complaint_list);

            repo = FirebaseRepository.getInstance();
            filterType = getIntent().getStringExtra("filter_type");
            department = getIntent().getStringExtra("department");
            userId = getIntent().getStringExtra("user_id");
            userRole = getIntent().getStringExtra("user_role");
            userName = getIntent().getStringExtra("user_name");

            TextView tvTitle = findViewById(R.id.tv_title);
            tvTitle.setText(getTitleForFilter(filterType));

            ImageButton backArrow = findViewById(R.id.back_arrow);
            backArrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

            LinearLayout bottomNav = findViewById(R.id.bottom_nav);
            if ("admin".equals(userRole) || "handler".equals(userRole)) {
                bottomNav.setVisibility(View.VISIBLE);

                final LinearLayout navHome = findViewById(R.id.nav_home);
                final LinearLayout navAllComplaints = findViewById(R.id.nav_all_complaints);
                final LinearLayout navManageUsers = findViewById(R.id.nav_manage_users);

                if ("all".equals(filterType)) {
                    applyNavItemTint(navAllComplaints, R.color.purple_start);
                } else {
                    applyNavItemTint(navHome, R.color.purple_start);
                }

                navHome.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
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
                        } catch (Exception e) {
                            Toast.makeText(ComplaintListActivity.this,
                                    "Navigation error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                navAllComplaints.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        filterType = "all";
                        tvTitle.setText(getTitleForFilter(filterType));
                        applyNavItemTint(navAllComplaints, R.color.purple_start);
                        applyNavItemTint(navHome, R.color.gray_medium);
                        loadComplaints();
                    }
                });

                if ("admin".equals(userRole)) {
                    navManageUsers.setVisibility(View.VISIBLE);
                    navManageUsers.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                Intent intent = new Intent(ComplaintListActivity.this, UserListActivity.class);
                                intent.putExtra("userId", userId);
                                intent.putExtra("userName", userName);
                                intent.putExtra("userRole", userRole);
                                startActivity(intent);
                            } catch (Exception e) {
                                Toast.makeText(ComplaintListActivity.this,
                                        "Failed to open Users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    navManageUsers.setVisibility(View.GONE);
                }
            }

            recyclerView = findViewById(R.id.recycler_complaints);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            complaintList = new ArrayList<>();
            adapter = new ComplaintAdapter(this, complaintList,
                    new ComplaintAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(Complaint complaint) {
                            try {
                                Intent intent = new Intent(ComplaintListActivity.this, ComplaintDetailsActivity.class);
                                intent.putExtra("complaintId", complaint.getId());
                                intent.putExtra("userId", userId);
                                intent.putExtra("userName", userName);
                                intent.putExtra("userRole", userRole);
                                if (department != null) {
                                    intent.putExtra("department", department);
                                }
                                startActivity(intent);
                            } catch (Exception e) {
                                Toast.makeText(ComplaintListActivity.this,
                                        "Failed to open complaint details: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
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
        } catch (Exception e) {
            Toast.makeText(this, "Error loading complaints: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void applyNavItemTint(LinearLayout navItem, int colorRes) {
        try {
            if (navItem == null || navItem.getChildCount() < 2) return;
            View icon = navItem.getChildAt(0);
            View label = navItem.getChildAt(1);
            if (icon instanceof ImageView) {
                ((ImageView) icon).setImageTintList(getColorStateList(colorRes));
            }
            if (label instanceof TextView) {
                ((TextView) label).setTextColor(getColor(colorRes));
            }
        } catch (Exception e) {
            // Silent — cosmetic only, should not break navigation
        }
    }

    private void showStatusChangeDialog(final Complaint complaint) {
        final String[] statuses = {"Submitted", "Under Review", "Assigned", "In Progress", "Resolved", "Closed", "Escalated"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Complaint Status");
        builder.setItems(statuses, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newStatus = statuses[which];
                repo.updateComplaintStatus(complaint.getId(), newStatus, userName, "Status changed to " + newStatus,
                        new FirebaseRepository.OnCompleteListener<Integer>() {
                            @Override
                            public void onSuccess(Integer rowsUpdated) {
                                if (rowsUpdated != null && rowsUpdated > 0) {
                                    Toast.makeText(ComplaintListActivity.this, "Status updated successfully!", Toast.LENGTH_SHORT).show();
                                    loadComplaints();
                                } else {
                                    Toast.makeText(ComplaintListActivity.this, "Failed to update status!", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(ComplaintListActivity.this, "Failed to update status!", Toast.LENGTH_SHORT).show();
                            }
                        });
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
        FirebaseRepository.OnCompleteListener<List<Complaint>> listener = new FirebaseRepository.OnCompleteListener<List<Complaint>>() {
            @Override
            public void onSuccess(List<Complaint> allComplaints) {
                complaintList.clear();
                if (allComplaints != null) {
                    for (Complaint complaint : allComplaints) {
                        if (matchesFilter(complaint, filterType)) {
                            complaintList.add(complaint);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                complaintList.clear();
                adapter.notifyDataSetChanged();
                String msg = e != null ? e.getMessage() : "Unknown error";
                if (msg == null || msg.isEmpty()) msg = "Firebase read failed. Check connection or permissions.";
                Toast.makeText(ComplaintListActivity.this,
                        "Firebase read failed: " + msg, Toast.LENGTH_LONG).show();
            }
        };

        if ("admin".equals(userRole)) {
            repo.getAllComplaints(listener);
        } else if ("handler".equals(userRole)) {
            final String safeDept = (department == null || department.trim().isEmpty()) ? "General" : department.trim();
            android.util.Log.d("[HANDLER-DIAG]", "ComplaintListActivity loadComplaints — using safeDept=[" + safeDept + "] raw department=[" + department + "]");
            // DIAGNOSTIC BYPASS: pull all and filter manually to remove Firestore query-mismatch variable
            repo.getAllComplaints(new FirebaseRepository.OnCompleteListener<List<Complaint>>() {
                @Override
                public void onSuccess(List<Complaint> allComplaints) {
                    android.util.Log.d("[HANDLER-DIAG]",
                            "ComplaintListActivity getAllComplaints total=" + (allComplaints != null ? allComplaints.size() : 0));
                    List<Complaint> filtered = new ArrayList<>();
                    if (allComplaints != null) {
                        for (Complaint c : allComplaints) {
                            String d = c.getDepartment();
                            if (safeDept.equalsIgnoreCase(d == null ? "" : d.trim())) {
                                filtered.add(c);
                            }
                        }
                    }
                    android.util.Log.d("[HANDLER-DIAG]",
                            "after manual dept filter=[" + safeDept + "] -> kept " + filtered.size());
                    // Now pass to the regular listener that also applies filterType/status filter
                    listener.onSuccess(filtered);
                }

                @Override
                public void onFailure(Exception e) {
                    listener.onFailure(e);
                }
            });
        } else {
            repo.getAllComplaintsByStudentId(userId, listener);
        }
    }

    private boolean matchesFilter(Complaint complaint, String filterType) {
        if (filterType == null) return true;

        String status = complaint.getStatus();
        if (status == null) return false;
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
                return status.equals("Submitted") || status.equals("Assigned");
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
