package com.example.imssystem;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.imssystem.helpers.FirebaseRepository;
import com.example.imssystem.models.Complaint;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {
    private FirebaseRepository repo;
    private String userId;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            // #region debug-point E:H5 : AdminDashboard onCreate evidence
            android.util.Log.d("[ROLE-DEBUG] [DASHBOARD]",
                    "AdminDashboardActivity.onCreate ENTER. Extras: userId=" + getIntent().getStringExtra("userId")
                            + " userName=" + getIntent().getStringExtra("userName")
                            + " userRole=" + getIntent().getStringExtra("userRole"));
            // #endregion
            setContentView(R.layout.activity_admin_dashboard);

        repo = FirebaseRepository.getInstance();
        userId = getIntent().getStringExtra("userId");
        userName = getIntent().getStringExtra("userName");

        TextView tvUserName = findViewById(R.id.tv_user_name);
        if (userName != null) {
            tvUserName.setText(userName + " 👋");
        }

        CardView cardTotal = findViewById(R.id.card_total);
        CardView cardActive = findViewById(R.id.card_active);
        CardView cardResolved = findViewById(R.id.card_resolved);
        CardView cardEscalated = findViewById(R.id.card_escalated);

        cardTotal.setOnClickListener(v -> openComplaintList("all"));
        cardActive.setOnClickListener(v -> openComplaintList("active"));
        cardResolved.setOnClickListener(v -> openComplaintList("resolved"));
        cardEscalated.setOnClickListener(v -> openComplaintList("escalated"));

        Button btnManageUsers = findViewById(R.id.btn_manage_users);
        btnManageUsers.setOnClickListener(v -> {
            Intent intent = new Intent(this, UserListActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("userName", userName);
            intent.putExtra("userRole", "admin");
            startActivity(intent);
        });

        LinearLayout navAllComplaints = findViewById(R.id.nav_all_complaints);
        navAllComplaints.setOnClickListener(v -> openComplaintList("all"));

        LinearLayout navManageUsers = findViewById(R.id.nav_manage_users);
        navManageUsers.setOnClickListener(v -> {
            Intent intent = new Intent(this, UserListActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("userName", userName);
            intent.putExtra("userRole", "admin");
            startActivity(intent);
        });
            // #region debug-point E:H5
            android.util.Log.d("[ROLE-DEBUG] [DASHBOARD]",
                    "AdminDashboardActivity.onCreate SUCCESS — layout fully inflated, listeners set.");
            // #endregion
        } catch (Exception e) {
            android.util.Log.e("[ROLE-DEBUG] [DASHBOARD]",
                    "AdminDashboardActivity.onCreate CRASHED: " + e.getClass().getSimpleName() + " — " + e.getMessage(), e);
            Toast.makeText(this,
                    "AdminDashboard failed to load: " + e.getClass().getSimpleName() + " — " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStats();
    }

    private void updateStats() {
        repo.getAllComplaints(new FirebaseRepository.OnCompleteListener<List<Complaint>>() {
            @Override
            public void onSuccess(List<Complaint> all) {
                int total = all != null ? all.size() : 0;
                int active = 0, resolved = 0, escalated = 0;

                if (all != null) {
                    for (Complaint complaint : all) {
                        String status = complaint.getStatus();
                        if (status == null) continue;
                        if (status.equals("Submitted") || status.equals("Assigned") || status.equals("Under Review") || status.equals("In Progress")) {
                            active++;
                        } else if (status.equals("Resolved") || status.equals("Closed")) {
                            resolved++;
                        } else if (status.equals("Escalated")) {
                            escalated++;
                        }
                    }
                }

                ((TextView) findViewById(R.id.tv_total)).setText(String.valueOf(total));
                ((TextView) findViewById(R.id.tv_active)).setText(String.valueOf(active));
                ((TextView) findViewById(R.id.tv_resolved)).setText(String.valueOf(resolved));
                ((TextView) findViewById(R.id.tv_escalated)).setText(String.valueOf(escalated));
            }

            @Override
            public void onFailure(Exception e) {
                ((TextView) findViewById(R.id.tv_total)).setText("0");
                ((TextView) findViewById(R.id.tv_active)).setText("0");
                ((TextView) findViewById(R.id.tv_resolved)).setText("0");
                ((TextView) findViewById(R.id.tv_escalated)).setText("0");
                String msg = e != null ? e.getMessage() : "Unknown error";
                if (msg == null || msg.isEmpty()) msg = "Check connection or permissions.";
                Toast.makeText(AdminDashboardActivity.this,
                        "Firebase read failed: " + msg, Toast.LENGTH_LONG).show();
            }
        });

        repo.getComplaintsResolvedInPastWeek(new FirebaseRepository.OnCompleteListener<Integer>() {
            @Override
            public void onSuccess(Integer weekResolved) {
                ((TextView) findViewById(R.id.tv_week)).setText(String.valueOf(weekResolved != null ? weekResolved : 0));
            }

            @Override
            public void onFailure(Exception e) {
                ((TextView) findViewById(R.id.tv_week)).setText("0");
            }
        });

        repo.getComplaintsResolvedInPastMonth(new FirebaseRepository.OnCompleteListener<Integer>() {
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
        intent.putExtra("user_role", "admin");
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
}
