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
import com.example.imssystem.helpers.FirebaseRepository;
import com.example.imssystem.models.User;
import java.util.ArrayList;
import java.util.List;

public class UserListActivity extends AppCompatActivity {
    private FirebaseRepository repo;
    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<User> userList;
    private List<User> allUsersList;
    private String userId;
    private String userName;
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_user_list);

            repo = FirebaseRepository.getInstance();
            userId = getIntent().getStringExtra("userId");
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

            LinearLayout navHome = findViewById(R.id.nav_home);
            LinearLayout navAllComplaints = findViewById(R.id.nav_all_complaints);
            LinearLayout navManageUsers = findViewById(R.id.nav_manage_users);

            applyNavItemTint(navManageUsers, R.color.purple_start);

            navHome.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(UserListActivity.this, AdminDashboardActivity.class);
                    intent.putExtra("userId", userId);
                    intent.putExtra("userName", userName);
                    intent.putExtra("userRole", userRole);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    Toast.makeText(this, "Navigation error: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });

            navAllComplaints.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(UserListActivity.this, ComplaintListActivity.class);
                    intent.putExtra("filter_type", "all");
                    intent.putExtra("user_id", userId);
                    intent.putExtra("userRole", userRole);
                    intent.putExtra("userName", userName);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "Failed to open complaints: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });

            navManageUsers.setOnClickListener(v -> {
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error loading users: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void applyNavItemTint(LinearLayout navItem, int colorRes) {
        try {
            if (navItem == null || navItem.getChildCount() < 2) return;
            View icon = navItem.getChildAt(0);
            View label = navItem.getChildAt(1);
            if (icon instanceof ImageView) {
                ((ImageView) icon).setImageTintList(getColorStateList(colorRes));
            }
            if (label instanceof TextView) {
                ((TextView) label).setTextColor(getColor(colorRes));
            }
        } catch (Exception e) {
            // Silent — cosmetic only
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUsers();
    }

    private void loadUsers() {
        repo.getAllUsers(new FirebaseRepository.OnCompleteListener<List<User>>() {
            @Override
            public void onSuccess(List<User> result) {
                allUsersList.clear();
                if (result != null) {
                    allUsersList.addAll(result);
                }
                userList.clear();
                userList.addAll(allUsersList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                allUsersList.clear();
                userList.clear();
                adapter.notifyDataSetChanged();
                String msg = e != null ? e.getMessage() : "Unknown error";
                if (msg == null || msg.isEmpty()) msg = "Firebase read failed. Check connection or permissions.";
                Toast.makeText(UserListActivity.this,
                        "Firebase read failed: " + msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void filterUsers(String query) {
        userList.clear();
        if (query.isEmpty()) {
            userList.addAll(allUsersList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (User user : allUsersList) {
                String name = user.getName() != null ? user.getName().toLowerCase() : "";
                String email = user.getEmail() != null ? user.getEmail().toLowerCase() : "";
                if (name.contains(lowerQuery) || email.contains(lowerQuery)) {
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
                    repo.updateUserRole(user.getId(), newRole, new FirebaseRepository.OnCompleteListener<Integer>() {
                        @Override
                        public void onSuccess(Integer rowsUpdated) {
                            if (rowsUpdated != null && rowsUpdated > 0) {
                                Toast.makeText(UserListActivity.this, "Role updated successfully", Toast.LENGTH_SHORT).show();
                                loadUsers();
                            } else {
                                Toast.makeText(UserListActivity.this, "Failed to update role", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(UserListActivity.this, "Failed to update role", Toast.LENGTH_SHORT).show();
                        }
                    });
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private int getRoleIndex(String role) {
        if (role == null) return 0;
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
