package com.example.imssystem.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.imssystem.R;
import com.example.imssystem.models.Complaint;

import java.util.List;

public class ComplaintAdapter extends RecyclerView.Adapter<ComplaintAdapter.ViewHolder> {
    private Context context;
    private List<Complaint> complaintList;
    private OnItemClickListener listener;
    private OnStatusChangeListener statusChangeListener;
    private String userRole;

    public interface OnItemClickListener {
        void onItemClick(Complaint complaint);
    }

    public interface OnStatusChangeListener {
        void onStatusChange(Complaint complaint);
    }

    public ComplaintAdapter(Context context, List<Complaint> complaintList,
                            OnItemClickListener listener,
                            OnStatusChangeListener statusChangeListener,
                            String userRole) {
        this.context = context;
        this.complaintList = complaintList;
        this.listener = listener;
        this.statusChangeListener = statusChangeListener;
        this.userRole = userRole;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_complaint, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Complaint complaint = complaintList.get(position);
        String title = complaint.getTitle() != null ? complaint.getTitle() : "";
        String category = complaint.getCategory() != null ? complaint.getCategory() : "";
        String status = complaint.getStatus() != null ? complaint.getStatus() : "";
        String createdAt = complaint.getCreatedAt() != null ? complaint.getCreatedAt() : "";
        holder.tvTitle.setText(title);
        holder.tvCategory.setText(category + " • " + status);
        holder.tvDate.setText(createdAt.contains(" ") ? createdAt.split(" ")[0] : createdAt);

        // Set status dot color
        int colorRes = getStatusColor(complaint.getStatus());
        holder.statusDot.setBackgroundColor(ContextCompat.getColor(context, colorRes));

        // Show status change button only for admin or handler
        if ("admin".equals(userRole) || "handler".equals(userRole)) {
            holder.btnChangeStatus.setVisibility(View.VISIBLE);
            holder.btnChangeStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (statusChangeListener != null) {
                        statusChangeListener.onStatusChange(complaint);
                    }
                }
            });
        } else {
            holder.btnChangeStatus.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(complaint);
            }
        });
    }

    private int getStatusColor(String status) {
        switch (status) {
            case "Submitted":
            case "Pending":
                return R.color.status_pending;
            case "Under Review":
            case "Assigned":
            case "In Progress":
            case "Active":
                return R.color.status_active;
            case "Resolved":
            case "Closed":
                return R.color.status_resolved;
            case "Escalated":
            case "Urgent":
                return R.color.status_escalated;
            default:
                return R.color.status_active;
        }
    }

    @Override
    public int getItemCount() {
        return complaintList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCategory, tvDate;
        View statusDot;
        Button btnChangeStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvDate = itemView.findViewById(R.id.tv_date);
            statusDot = itemView.findViewById(R.id.status_dot);
            btnChangeStatus = itemView.findViewById(R.id.btn_change_status);
        }
    }
}
