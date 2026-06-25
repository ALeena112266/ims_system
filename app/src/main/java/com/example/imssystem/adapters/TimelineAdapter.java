package com.example.imssystem.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.imssystem.R;
import com.example.imssystem.models.StatusLog;

import java.util.List;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.ViewHolder> {
    private Context context;
    private List<StatusLog> logList;

    public TimelineAdapter(Context context, List<StatusLog> logList) {
        this.context = context;
        this.logList = logList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_timeline, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StatusLog log = logList.get(position);
        holder.tvStatus.setText(log.getNewStatus());
        holder.tvNote.setText(log.getNote() != null ? log.getNote() : "Status updated");
        holder.tvTime.setText(log.getTimestamp());

        // Hide the line on last item
        if (position == logList.size() - 1) {
            holder.itemView.findViewById(R.id.timeline_dot).setVisibility(View.VISIBLE);
            // Hide the line below last dot - we can adjust layout params if needed, but for simplicity, let's just leave it
        }
    }

    @Override
    public int getItemCount() {
        return logList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStatus, tvNote, tvTime;
        View timelineDot;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvNote = itemView.findViewById(R.id.tv_note);
            tvTime = itemView.findViewById(R.id.tv_time);
            timelineDot = itemView.findViewById(R.id.timeline_dot);
        }
    }
}
