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

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.imssystem.adapters.TimelineAdapter;
import com.example.imssystem.helpers.DBHelper;
import com.example.imssystem.models.Complaint;
import com.example.imssystem.models.StatusLog;

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

            tvTitle.setText(complaint.getTitle());
            tvCategory.setText(complaint.getCategory());
            tvStatus.setText(complaint.getStatus());
            tvDescription.setText(complaint.getDescription());

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
