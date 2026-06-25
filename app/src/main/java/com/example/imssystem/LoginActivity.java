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

import com.example.imssystem.helpers.DBHelper;
import com.example.imssystem.models.User;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button loginBtn;
    private TextView signupText;
    private ImageButton backArrow;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DBHelper(this);

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        loginBtn = findViewById(R.id.login_btn);
        signupText = findViewById(R.id.signup_text);
        backArrow = findViewById(R.id.back_arrow);

        // Add hints to login layout (we forgot to add EditText ids in login.xml earlier! Oops!)
        // Let's first update login.xml to add EditText with proper ids!

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

                User user = dbHelper.checkLogin(email, password);
        if (user != null) {
            Intent intent;
            if (user.getRole().equals("student")) {
                intent = new Intent(LoginActivity.this, DashboardActivity.class);
            } else if (user.getRole().equals("handler")) {
                intent = new Intent(LoginActivity.this, HandlerDashboardActivity.class);
                intent.putExtra("userDepartment", user.getDepartment());
            } else {
                intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
            }
            intent.putExtra("userId", user.getId());
            intent.putExtra("userName", user.getName());
            intent.putExtra("userRole", user.getRole());
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
        }
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