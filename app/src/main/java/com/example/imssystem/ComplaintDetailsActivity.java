package com.example.imssystem;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.imssystem.adapters.TimelineAdapter;
import com.example.imssystem.helpers.DBHelper;
import com.example.imssystem.models.Complaint;
import com.example.imssystem.models.StatusLog;
import com.example.imssystem.models.User;

import java.util.ArrayList;
import java.util.List;

public class ComplaintDetailsActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private int complaintId;
    private Complaint complaint;
    private RecyclerView recyclerView;
    private TimelineAdapter adapter;
    private List<StatusLog> logList;
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_details);

        dbHelper = new DBHelper(this);
        complaintId = getIntent().getIntExtra("complaintId", 0);
        userRole = getIntent().getStringExtra("userRole");

        // Back button
        ImageButton backArrow = findViewById(R.id.back_arrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button btnDelete = findViewById(R.id.btn_delete);
        Button btnFeedback = findViewById(R.id.btn_feedback);

        // Show/hide buttons based on role
        if ("student".equals(userRole)) {
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            btnDelete.setVisibility(View.GONE);
        }
        // Feedback button is visible for all roles
        btnFeedback.setVisibility(View.VISIBLE);

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(ComplaintDetailsActivity.this)
                        .setTitle("Delete Complaint")
                        .setMessage("Are you sure you want to delete this complaint?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            int rowsDeleted = dbHelper.deleteComplaint(complaintId);
                            if (rowsDeleted > 0) {
                                Toast.makeText(ComplaintDetailsActivity.this, "Complaint deleted", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(ComplaintDetailsActivity.this, "Failed to delete complaint", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

        btnFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFeedbackDialog();
            }
        });

        // Setup Timeline RecyclerView
        recyclerView = findViewById(R.id.recycler_timeline);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        logList = new ArrayList<>();
        adapter = new TimelineAdapter(this, logList);
        recyclerView.setAdapter(adapter);

        // Load data
        loadComplaintDetails();
    }

    private void showFeedbackDialog() {
        final EditText feedbackInput = new EditText(this);
        feedbackInput.setHint("Enter your feedback here...");
        feedbackInput.setPadding(48, 24, 48, 24);

        new AlertDialog.Builder(this)
                .setTitle("Submit Feedback")
                .setView(feedbackInput)
                .setPositiveButton("Submit", (dialog, which) -> {
                    String feedback = feedbackInput.getText().toString().trim();
                    if (!feedback.isEmpty()) {
                        Toast.makeText(this, "Thank you for your feedback!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Please enter feedback", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadComplaintDetails() {
        complaint = dbHelper.getComplaintById(complaintId);
        if (complaint != null) {
            TextView tvTitle = findViewById(R.id.tv_title);
            TextView tvCategory = findViewById(R.id.tv_category);
            TextView tvStatus = findViewById(R.id.tv_status);
            TextView tvDescription = findViewById(R.id.tv_description);
            LinearLayout layoutAttachment = findViewById(R.id.layout_attachment);
            TextView tvAttachment = findViewById(R.id.tv_attachment);
            TextView tvStudentName = findViewById(R.id.tv_student_name);
            TextView tvStudentId = findViewById(R.id.tv_student_id);
            TextView tvStudentEmail = findViewById(R.id.tv_student_email);

            tvTitle.setText(complaint.getTitle());
            tvCategory.setText(complaint.getCategory());
            tvStatus.setText(complaint.getStatus());
            tvDescription.setText(complaint.getDescription());

            // Load student info
            User student = dbHelper.getUserByStudentId(complaint.getStudentId());
            if (student != null) {
                tvStudentName.setText(student.getName());
                tvStudentId.setText("Student ID: " + student.getStudentId());
                tvStudentEmail.setText("Email: " + student.getEmail());
            }

            // Handle attachment
            if (complaint.getAttachmentUri() != null && !complaint.getAttachmentUri().isEmpty()) {
                layoutAttachment.setVisibility(View.VISIBLE);
                tvAttachment.setText("Open Attachment");
                tvAttachment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.parse(complaint.getAttachmentUri()), "*/*");
                            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(intent);
                        } catch (Exception e) {
                            Toast.makeText(ComplaintDetailsActivity.this, "Unable to open attachment", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                layoutAttachment.setVisibility(View.GONE);
            }

            // Load timeline
            logList.clear();
            logList.addAll(dbHelper.getStatusLogsByComplaintId(complaintId));
            adapter.notifyDataSetChanged();
        }
    }
}
