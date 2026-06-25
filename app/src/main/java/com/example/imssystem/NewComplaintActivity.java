package com.example.imssystem;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.imssystem.helpers.DBHelper;
import com.example.imssystem.models.Complaint;

public class NewComplaintActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private EditText etTitle, etDescription;
    private TextView chipIt, chipHostel, chipAcademic, chipTransport, chipAccounts;
    private TextView priorityLow, priorityMedium, priorityHigh;
    private TextView tvAttachment;
    private String selectedCategory = "IT Support";
    private String selectedDepartment = "IT Support";
    private String selectedPriority = "High";
    private String attachmentUri = null;
    private int userId;
    private String userName;

    private ActivityResultLauncher<Intent> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_complaint);

        dbHelper = new DBHelper(this);
        userId = getIntent().getIntExtra("userId", 0);
        userName = getIntent().getStringExtra("userName");

        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);

        chipIt = findViewById(R.id.chip_it);
        chipHostel = findViewById(R.id.chip_hostel);
        chipAcademic = findViewById(R.id.chip_academic);
        chipTransport = findViewById(R.id.chip_transport);
        chipAccounts = findViewById(R.id.chip_accounts);

        priorityLow = findViewById(R.id.priority_low);
        priorityMedium = findViewById(R.id.priority_medium);
        priorityHigh = findViewById(R.id.priority_high);

        tvAttachment = findViewById(R.id.tv_attachment);

        // Initialize file picker
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            Uri uri = result.getData().getData();
                            if (uri != null) {
                                attachmentUri = uri.toString();
                                tvAttachment.setText("File selected: " + uri.getLastPathSegment());
                                tvAttachment.setTextColor(ContextCompat.getColor(NewComplaintActivity.this, R.color.purple_start));
                            }
                        }
                    }
                }
        );

        // Set attachment click listener
        View attachmentArea = findViewById(R.id.attachment_area);
        attachmentArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                        "image/*",
                        "application/pdf",
                        "video/*"
                });
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                filePickerLauncher.launch(Intent.createChooser(intent, "Select File"));
            }
        });

        // Populate fields if template is selected
        if (getIntent().hasExtra("templateTitle")) {
            etTitle.setText(getIntent().getStringExtra("templateTitle"));
        }
        if (getIntent().hasExtra("templateDescription")) {
            etDescription.setText(getIntent().getStringExtra("templateDescription"));
        }
        if (getIntent().hasExtra("templatePriority")) {
            setPriority(getIntent().getStringExtra("templatePriority"));
        }
        if (getIntent().hasExtra("templateDepartment")) {
            String dep = getIntent().getStringExtra("templateDepartment");
            if (dep != null) {
                if (dep.contains("IT")) {
                    selectCategory("IT Support", "IT Support", chipIt);
                } else if (dep.contains("Hostel")) {
                    selectCategory("Hostel", "Hostel Admin", chipHostel);
                } else if (dep.contains("Academic")) {
                    selectCategory("Academics", "Academics", chipAcademic);
                } else if (dep.contains("Transport")) {
                    selectCategory("Transport", "Transport", chipTransport);
                } else if (dep.contains("Accounts")) {
                    selectCategory("Accounts", "Accounts", chipAccounts);
                }
            }
        }

        // Back button
        ImageButton backArrow = findViewById(R.id.back_arrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button btnNext = findViewById(R.id.btn_next);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitComplaint();
            }
        });

        // Category chip listeners
        chipIt.setOnClickListener(v -> selectCategory("IT Support", "IT Support", chipIt));
        chipHostel.setOnClickListener(v -> selectCategory("Hostel", "Hostel Admin", chipHostel));
        chipAcademic.setOnClickListener(v -> selectCategory("Academics", "Academics", chipAcademic));
        chipTransport.setOnClickListener(v -> selectCategory("Transport", "Transport", chipTransport));
        chipAccounts.setOnClickListener(v -> selectCategory("Accounts", "Accounts", chipAccounts));

        // Priority listeners
        priorityLow.setOnClickListener(v -> setPriority("Low"));
        priorityMedium.setOnClickListener(v -> setPriority("Medium"));
        priorityHigh.setOnClickListener(v -> setPriority("High"));
    }

    private void selectCategory(String category, String department, TextView selectedChip) {
        selectedCategory = category;
        selectedDepartment = department;

        // Reset all chips
        resetChip(chipIt);
        resetChip(chipHostel);
        resetChip(chipAcademic);
        resetChip(chipTransport);
        resetChip(chipAccounts);

        // Highlight selected chip
        selectedChip.setBackgroundResource(R.drawable.chip_selected);
        selectedChip.setTextColor(ContextCompat.getColor(this, R.color.purple_start));
    }

    private void resetChip(TextView chip) {
        chip.setBackgroundResource(R.drawable.chip_unselected);
        chip.setTextColor(ContextCompat.getColor(this, R.color.gray_medium));
    }

    private void setPriority(String priority) {
        selectedPriority = priority;

        // Reset all priorities
        resetPriority(priorityLow, R.color.status_resolved, R.drawable.priority_low);
        resetPriority(priorityMedium, R.color.status_pending, R.drawable.priority_medium);
        resetPriority(priorityHigh, R.color.status_escalated, R.drawable.priority_high);

        // Highlight selected priority
        switch (priority) {
            case "Low":
                priorityLow.setBackgroundResource(R.drawable.priority_low);
                priorityLow.setTextColor(ContextCompat.getColor(this, R.color.white));
                priorityLow.setBackgroundColor(ContextCompat.getColor(this, R.color.status_resolved));
                break;
            case "Medium":
                priorityMedium.setBackgroundResource(R.drawable.priority_medium);
                priorityMedium.setTextColor(ContextCompat.getColor(this, R.color.white));
                priorityMedium.setBackgroundColor(ContextCompat.getColor(this, R.color.status_pending));
                break;
            case "High":
                priorityHigh.setBackgroundResource(R.drawable.priority_high);
                priorityHigh.setTextColor(ContextCompat.getColor(this, R.color.white));
                priorityHigh.setBackgroundColor(ContextCompat.getColor(this, R.color.status_escalated));
                break;
        }
    }

    private void resetPriority(TextView priority, int colorRes, int drawableRes) {
        priority.setBackgroundResource(drawableRes);
        priority.setTextColor(ContextCompat.getColor(this, colorRes));
    }

    private void submitComplaint() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Complaint complaint = new Complaint(userId, userName, selectedCategory, selectedDepartment, title, description, selectedPriority, "Submitted", "");
        complaint.setAttachmentUri(attachmentUri);
        long complaintId = dbHelper.addComplaint(complaint);

        if (complaintId != -1) {
            Toast.makeText(this, "Complaint submitted successfully!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Failed to submit complaint", Toast.LENGTH_SHORT).show();
        }
    }
}
