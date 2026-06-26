package com.example.imssystem;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        dbHelper = new DBHelper(this);
        ImageButton backBtn = findViewById(R.id.back_btn);
        backBtn.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recycler_users);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        adapter = new UserAdapter(this, userList, user -> showRoleChangeDialog(user));
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUsers();
    }

    private void loadUsers() {
        userList.clear();
        userList.addAll(dbHelper.getAllUsers());
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
