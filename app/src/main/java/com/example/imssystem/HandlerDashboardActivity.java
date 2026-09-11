package com.example.imssystem;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.imssystem.helpers.FirebaseRepository;
import com.example.imssystem.models.Complaint;

import java.util.ArrayList;
import java.util.List;

public class HandlerDashboardActivity extends AppCompatActivity {
    private FirebaseRepository repo;
    private String userId;
    private String userName;
    private String userDepartment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            // #region debug-point F:H5 : HandlerDashboard onCreate evidence
            android.util.Log.d("[ROLE-DEBUG] [DASHBOARD]",
                    "HandlerDashboardActivity.onCreate ENTER. Extras: userId=" + getIntent().getStringExtra("userId")
                            + " userName=" + getIntent().getStringExtra("userName")
                            + " userRole=" + getIntent().getStringExtra("userRole")
                            + " userDepartment=" + getIntent().getStringExtra("userDepartment"));
            // #endregion
            setContentView(R.layout.activity_handler_dashboard);

        repo = FirebaseRepository.getInstance();
        userId = getIntent().getStringExtra("userId");
        userName = getIntent().getStringExtra("userName");
        userDepartment = getIntent().getStringExtra("userDepartment");

        TextView tvUserName = findViewById(R.id.tv_user_name);
        if (userName != null) {
            tvUserName.setText(userName + " 👋");
        }

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
            // #region debug-point F:H5
            android.util.Log.d("[ROLE-DEBUG] [DASHBOARD]",
                    "HandlerDashboardActivity.onCreate SUCCESS — layout fully inflated, listeners set. department=" + userDepartment);
            // #endregion
        } catch (Exception e) {
            android.util.Log.e("[ROLE-DEBUG] [DASHBOARD]",
                    "HandlerDashboardActivity.onCreate CRASHED: " + e.getClass().getSimpleName() + " — " + e.getMessage(), e);
            Toast.makeText(this,
                    "HandlerDashboard failed to load: " + e.getClass().getSimpleName() + " — " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStats();
    }

    private void updateStats() {
        final String safeDept = (userDepartment == null || userDepartment.trim().isEmpty()) ? "General" : userDepartment.trim();
        android.util.Log.d("[HANDLER-DIAG]", "updateStats() — using safeDept=[" + safeDept + "] userDepartment raw=[" + userDepartment + "]");
        // DIAGNOSTIC BYPASS — temporarily use UNFILTERED query so we can see if filter itself is the cause.
        // If cards suddenly show real numbers, then it is 100% a handler.department vs complaint.department string mismatch.
        repo.getAllComplaints(new FirebaseRepository.OnCompleteListener<List<Complaint>>() {
            @Override
            public void onSuccess(List<Complaint> all) {
                android.util.Log.d("[HANDLER-DIAG]",
                        "getAllComplaints diag result: total=" + (all != null ? all.size() : 0) +
                        (all != null && !all.isEmpty() ? " — dept values in first 3: [" +
                                (all.size() > 0 ? safe(all.get(0).getDepartment()) : "") + "], [" +
                                (all.size() > 1 ? safe(all.get(1).getDepartment()) : "") + "], [" +
                                (all.size() > 2 ? safe(all.get(2).getDepartment()) : "") + "]" : ""));
                // Now filter manually using safeDept so we can compare apples-to-apples with the query version
                List<Complaint> filtered = new ArrayList<>();
                if (all != null) {
                    for (Complaint c : all) {
                        String d = c.getDepartment();
                        if (safeDept.equalsIgnoreCase(d == null ? "" : d.trim())) {
                            filtered.add(c);
                        }
                    }
                }
                android.util.Log.d("[HANDLER-DIAG]", "manual filter with safeDept=[" + safeDept + "] kept " + filtered.size() + " of " + (all != null ? all.size() : 0));

                int assigned = 0, inProgress = 0, resolved = 0, escalated = 0;
                for (Complaint complaint : filtered) {
                    String status = complaint.getStatus();
                    if (status == null) continue;
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
            }

            @Override
            public void onFailure(Exception e) {
                ((TextView) findViewById(R.id.tv_assigned)).setText("0");
                ((TextView) findViewById(R.id.tv_in_progress)).setText("0");
                ((TextView) findViewById(R.id.tv_resolved)).setText("0");
                ((TextView) findViewById(R.id.tv_escalated)).setText("0");
                String msg = e != null ? e.getMessage() : "Unknown error";
                if (msg == null || msg.isEmpty()) msg = "Check connection or permissions.";
                android.util.Log.e("[HANDLER-DIAG]", "getAllComplaints onFailure: " +
                        (e != null ? e.getClass().getSimpleName() : "null-ex") + " — " + msg, e);
                Toast.makeText(HandlerDashboardActivity.this,
                        "Firebase read failed: " + msg, Toast.LENGTH_LONG).show();
            }
        });

        final String safeDept2 = (userDepartment == null || userDepartment.trim().isEmpty()) ? "General" : userDepartment.trim();
        repo.getComplaintsResolvedInPastWeekByDepartment(safeDept2, new FirebaseRepository.OnCompleteListener<Integer>() {
            @Override
            public void onSuccess(Integer weekResolved) {
                ((TextView) findViewById(R.id.tv_week)).setText(String.valueOf(weekResolved != null ? weekResolved : 0));
            }

            @Override
            public void onFailure(Exception e) {
                ((TextView) findViewById(R.id.tv_week)).setText("0");
            }
        });

        final String safeDept3 = (userDepartment == null || userDepartment.trim().isEmpty()) ? "General" : userDepartment.trim();
        repo.getComplaintsResolvedInPastMonthByDepartment(safeDept3, new FirebaseRepository.OnCompleteListener<Integer>() {
            @Override
            public void onSuccess(Integer monthResolved) {
                ((TextView) findViewById(R.id.tv_month)).setText(String.valueOf(monthResolved != null ? monthResolved : 0));
            }

            @Override
            public void onFailure(Exception e) {
                ((TextView) findViewById(R.id.tv_month)).setText("0");
            }
        });
    }

    private void openComplaintList(String filterType) {
        Intent intent = new Intent(this, ComplaintListActivity.class);
        intent.putExtra("filter_type", filterType);
        String safeDept = (userDepartment == null || userDepartment.trim().isEmpty()) ? "General" : userDepartment.trim();
        intent.putExtra("department", safeDept);
        intent.putExtra("user_role", "handler");
        intent.putExtra("user_id", userId);
        intent.putExtra("user_name", userName);
        startActivity(intent);
    }

    public void onLogoutClick(View view) {
        repo.logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private static String safe(String s) { return s == null ? "null" : s; }
}
