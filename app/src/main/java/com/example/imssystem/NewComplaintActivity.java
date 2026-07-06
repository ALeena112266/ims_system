package com.example.imssystem;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.imssystem.helpers.DBHelper;
import com.example.imssystem.models.Complaint;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NewComplaintActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private EditText etTitle, etDescription;
    private TextView chipIt, chipHostel, chipAcademic, chipTransport, chipAccounts, chipLibrary, chipOthers;
    private TextView priorityLow, priorityMedium, priorityHigh;
    private TextView tvAttachment;
    private String selectedCategory = "IT Support";
    private String selectedDepartment = "IT Support";
    private String selectedPriority = "High";
    private String attachmentUri = null;
    private int userId;
    private String userName;
    private Uri cameraImageUri = null;

    private ActivityResultLauncher<Intent> filePickerLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;

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
        chipLibrary = findViewById(R.id.chip_library);
        chipOthers = findViewById(R.id.chip_others);

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

        // Initialize camera launcher
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            if (cameraImageUri != null) {
                                attachmentUri = cameraImageUri.toString();
                                tvAttachment.setText("Photo captured!");
                                tvAttachment.setTextColor(ContextCompat.getColor(NewComplaintActivity.this, R.color.purple_start));
                            }
                        }
                    }
                }
        );

        // Set attachment click listener
        View attachmentArea = findViewById(R.id.layout_attachment);
        attachmentArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAttachmentOptions();
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
                } else if (dep.contains("Library")) {
                    selectCategory("Library", "Library", chipLibrary);
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
        chipLibrary.setOnClickListener(v -> selectCategory("Library", "Library", chipLibrary));
        chipOthers.setOnClickListener(v -> selectCategory("Others", "General", chipOthers));

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
        resetChip(chipLibrary);
        resetChip(chipOthers);

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

    private void showAttachmentOptions() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Select Attachment")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openCamera();
                    } else {
                        openGallery();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (photoFile != null) {
                cameraImageUri = FileProvider.getUriForFile(this,
                        getPackageName() + ".fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
                cameraLauncher.launch(takePictureIntent);
            }
        }
    }

    private void openGallery() {
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

    private File createImageFile() throws Exception {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        return image;
    }
}
