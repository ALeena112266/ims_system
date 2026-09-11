package com.example.imssystem;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.imssystem.helpers.FirebaseRepository;
import com.example.imssystem.models.User;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button loginBtn;
    private TextView signupText;
    private ImageButton backArrow;
    private FirebaseRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        repo = FirebaseRepository.getInstance();

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        loginBtn = findViewById(R.id.login_btn);
        signupText = findViewById(R.id.signup_text);
        backArrow = findViewById(R.id.back_arrow);

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(LoginActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                loginBtn.setEnabled(false);

                repo.checkLogin(email, password, new FirebaseRepository.OnCompleteListener<User>() {
                    @Override
                    public void onSuccess(User user) {
                        loginBtn.setEnabled(true);
                        if (user != null) {
                            String role = user.getRole() != null ? user.getRole().trim().toLowerCase() : "";
                            Intent intent;
                            String roleDisplay;
                            if ("admin".equals(role)) {
                                intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                                roleDisplay = "Admin";
                            } else if ("handler".equals(role)) {
                                intent = new Intent(LoginActivity.this, HandlerDashboardActivity.class);
                                String dep = user.getDepartment();
                                if (dep == null || dep.trim().isEmpty()) dep = "General";
                                intent.putExtra("userDepartment", dep);
                                roleDisplay = "Handler (" + dep + ")";
                            } else if ("student".equals(role)) {
                                intent = new Intent(LoginActivity.this, DashboardActivity.class);
                                roleDisplay = "Student";
                            } else {
                                roleDisplay = role.isEmpty() ? "not set" : role;
                                Toast.makeText(LoginActivity.this,
                                        "Role not recognized (" + roleDisplay + ") — logged in as Student. Ask admin to set your role.",
                                        Toast.LENGTH_LONG).show();
                                intent = new Intent(LoginActivity.this, DashboardActivity.class);
                            }
                            Toast.makeText(LoginActivity.this,
                                    "Welcome " + user.getName() + "! Logged in as: " + roleDisplay,
                                    Toast.LENGTH_SHORT).show();

                            try {
                                intent.putExtra("userId", user.getId());
                                intent.putExtra("userName", user.getName());
                                intent.putExtra("userRole", user.getRole());
                                startActivity(intent);
                                finish();
                            } catch (Exception e) {
                                Toast.makeText(LoginActivity.this,
                                        "ERROR launching dashboard: " + e.getClass().getSimpleName() + " — " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();

                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        loginBtn.setEnabled(true);
                        Toast.makeText(LoginActivity.this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        signupText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}
