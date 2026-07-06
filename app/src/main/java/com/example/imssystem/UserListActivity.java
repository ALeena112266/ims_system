package com.example.imssystem;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.imssystem.adapters.UserAdapter;
import com.example.imssystem.helpers.DBHelper;
import com.example.imssystem.models.User;
import java.util.ArrayList;
import java.util.List;

public class UserListActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<User> userList;
    private List<User> allUsersList;
    private int userId;
    private String userName;
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        dbHelper = new DBHelper(this);
        userId = getIntent().getIntExtra("userId", -1);
        userName = getIntent().getStringExtra("userName");
        userRole = getIntent().getStringExtra("userRole");

        ImageButton backBtn = findViewById(R.id.back_btn);
        backBtn.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recycler_users);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        allUsersList = new ArrayList<>();
        adapter = new UserAdapter(this, userList, user -> showRoleChangeDialog(user));
        recyclerView.setAdapter(adapter);

        // Setup search
        EditText searchInput = findViewById(R.id.search_input);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        // Setup bottom nav
        LinearLayout navHome = findViewById(R.id.nav_home);
        LinearLayout navAllComplaints = findViewById(R.id.nav_all_complaints);
        LinearLayout navManageUsers = findViewById(R.id.nav_manage_users);

        // Set current item
        ((ImageView) navManageUsers.getChildAt(0)).setImageTintList(getColorStateList(R.color.purple_start));
        ((TextView) navManageUsers.getChildAt(1)).setTextColor(getColor(R.color.purple_start));

        // Home nav
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(UserListActivity.this, AdminDashboardActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("userName", userName);
            intent.putExtra("userRole", userRole);
            startActivity(intent);
            finish();
        });

        // All complaints nav
        navAllComplaints.setOnClickListener(v -> {
            Intent intent = new Intent(UserListActivity.this, ComplaintListActivity.class);
            intent.putExtra("filter_type", "all");
            intent.putExtra("user_id", userId);
            intent.putExtra("userRole", userRole);
            intent.putExtra("userName", userName);
            startActivity(intent);
        });

        // Manage users nav
        navManageUsers.setOnClickListener(v -> {
            // Do nothing, we're already here
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUsers();
    }

    private void loadUsers() {
        allUsersList.clear();
        allUsersList.addAll(dbHelper.getAllUsers());
        userList.clear();
        userList.addAll(allUsersList);
        adapter.notifyDataSetChanged();
    }

    private void filterUsers(String query) {
        userList.clear();
        if (query.isEmpty()) {
            userList.addAll(allUsersList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (User user : allUsersList) {
                if (user.getName().toLowerCase().contains(lowerQuery) ||
                        user.getEmail().toLowerCase().contains(lowerQuery)) {
                    userList.add(user);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void showRoleChangeDialog(User user) {
        String[] roles = {"student", "handler", "admin"};
        new AlertDialog.Builder(this)
                .setTitle("Change User Role")
                .setSingleChoiceItems(roles, getRoleIndex(user.getRole()), (dialog, which) -> {
                    String newRole = roles[which];
                    int rowsUpdated = dbHelper.updateUserRole(user.getId(), newRole);
                    if (rowsUpdated > 0) {
                        Toast.makeText(this, "Role updated successfully", Toast.LENGTH_SHORT).show();
                        loadUsers();
                    } else {
                        Toast.makeText(this, "Failed to update role", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private int getRoleIndex(String role) {
        switch (role) {
            case "student":
                return 0;
            case "handler":
                return 1;
            case "admin":
                return 2;
            default:
                return 0;
        }
    }
}
