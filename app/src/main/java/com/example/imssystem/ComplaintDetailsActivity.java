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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_details);

        dbHelper = new DBHelper(this);
        complaintId = getIntent().getIntExtra("complaintId", 0);

        // Back button
        ImageButton backArrow = findViewById(R.id.back_arrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button btnDelete = findViewById(R.id.btn_delete);
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

        // Setup Timeline RecyclerView
        recyclerView = findViewById(R.id.recycler_timeline);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        logList = new ArrayList<>();
        adapter = new TimelineAdapter(this, logList);
        recyclerView.setAdapter(adapter);

        // Load data
        loadComplaintDetails();
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
