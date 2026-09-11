package com.example.imssystem;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.imssystem.helpers.FirebaseRepository;
import com.example.imssystem.models.User;

public class RegisterActivity extends AppCompatActivity {
    private EditText etName, etStudentId, etEmail, etPassword;
    private Button btnRegister;
    private ImageButton backArrow;
    private Spinner spinnerDepartment;
    private FirebaseRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        repo = FirebaseRepository.getInstance();

        etName = findViewById(R.id.et_name);
        etStudentId = findViewById(R.id.et_student_id);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnRegister = findViewById(R.id.btn_register);
        backArrow = findViewById(R.id.back_arrow);
        spinnerDepartment = findViewById(R.id.spinner_department);

        String[] departments = new String[]{
                "BSSE",
                "BBA",
                "BS Data Science",
                "BS Computer Science",
                "BS AI",
                "BS Cyber Security",
                "BS English",
                "BS Social Sciences",
                "MBA"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, departments);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDepartment.setAdapter(adapter);

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etName.getText().toString().trim();
                String studentId = etStudentId.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String department = spinnerDepartment.getSelectedItem().toString();

                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(RegisterActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                    return;
                }

                btnRegister.setEnabled(false);
                repo.isEmailExists(email, new FirebaseRepository.OnCompleteListener<Boolean>() {
                    @Override
                    public void onSuccess(Boolean exists) {
                        if (exists) {
                            btnRegister.setEnabled(true);
                            Toast.makeText(RegisterActivity.this, "Email already registered", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        doRegister(name, studentId, email, password, department);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        doRegister(name, studentId, email, password, department);
                    }
                });
            }
        });

        findViewById(R.id.tv_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void doRegister(String name, String studentId, String email,
                            String password, String department) {
        User user = new User(name, email, "student", department, studentId, password);
        repo.registerUser(user, new FirebaseRepository.OnCompleteListener<String>() {
            @Override
            public void onSuccess(String userId) {
                btnRegister.setEnabled(true);
                Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                btnRegister.setEnabled(true);
                Toast.makeText(RegisterActivity.this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
