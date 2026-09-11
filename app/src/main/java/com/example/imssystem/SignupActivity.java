package com.example.imssystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.imssystem.helpers.FirebaseRepository;
import com.example.imssystem.models.User;

public class SignupActivity extends AppCompatActivity {
    private EditText etFullName, etStudentId, etEmail, etPassword;
    private Spinner spinnerDepartment;
    private Button signupBtn;
    private TextView loginText;
    private ImageButton backArrow;
    private FirebaseRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        repo = FirebaseRepository.getInstance();

        etFullName = findViewById(R.id.et_full_name);
        etStudentId = findViewById(R.id.et_student_id);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        spinnerDepartment = findViewById(R.id.spinner_department);
        signupBtn = findViewById(R.id.signup_btn);
        loginText = findViewById(R.id.login_text);
        backArrow = findViewById(R.id.back_arrow);

        String[] departments = {
            "Computer Science",
            "Electrical Engineering",
            "Mechanical Engineering",
            "Civil Engineering",
            "Business Administration",
            "IT Support",
            "Hostel Admin",
            "Accounts",
            "Administration"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, departments);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDepartment.setAdapter(adapter);

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullName = etFullName.getText().toString().trim();
                String studentId = etStudentId.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String department = spinnerDepartment.getSelectedItem().toString();

                if (fullName.isEmpty() || studentId.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(SignupActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(SignupActivity.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                    return;
                }

                signupBtn.setEnabled(false);
                repo.isEmailExists(email, new FirebaseRepository.OnCompleteListener<Boolean>() {
                    @Override
                    public void onSuccess(Boolean exists) {
                        if (exists) {
                            signupBtn.setEnabled(true);
                            Toast.makeText(SignupActivity.this, "Email already exists", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        doSignup(fullName, studentId, email, password, department);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        doSignup(fullName, studentId, email, password, department);
                    }
                });
            }
        });

        loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void doSignup(String fullName, String studentId, String email,
                          String password, String department) {
        User user = new User(fullName, email, "student", department, studentId, password);
        repo.registerUser(user, new FirebaseRepository.OnCompleteListener<String>() {
            @Override
            public void onSuccess(String userId) {
                signupBtn.setEnabled(true);
                Toast.makeText(SignupActivity.this, "Signup successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                signupBtn.setEnabled(true);
                Toast.makeText(SignupActivity.this, "Signup failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
