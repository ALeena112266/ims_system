package com.example.imssystem.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.imssystem.R;
import com.example.imssystem.models.User;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private Context context;
    private List<User> userList;
    private OnRoleChangeListener onRoleChangeListener;

    public interface OnRoleChangeListener {
        void onRoleChange(User user);
    }

    public UserAdapter(Context context, List<User> userList, OnRoleChangeListener onRoleChangeListener) {
        this.context = context;
        this.userList = userList;
        this.onRoleChangeListener = onRoleChangeListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.tvUserName.setText(user.getName());
        holder.tvUserEmail.setText(user.getEmail());
        holder.tvUserRole.setText(user.getRole());
        String initial = user.getName().substring(0, 1).toUpperCase();
        holder.tvUserInitial.setText(initial);

        holder.btnChangeRole.setOnClickListener(v -> {
            if (onRoleChangeListener != null) {
                onRoleChangeListener.onRoleChange(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvUserEmail, tvUserRole, tvUserInitial;
        Button btnChangeRole;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvUserEmail = itemView.findViewById(R.id.tv_user_email);
            tvUserRole = itemView.findViewById(R.id.tv_user_role);
            tvUserInitial = itemView.findViewById(R.id.tv_user_initial);
            btnChangeRole = itemView.findViewById(R.id.btn_change_role);
        }
    }
}
