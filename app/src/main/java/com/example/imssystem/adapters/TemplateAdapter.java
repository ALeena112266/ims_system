package com.example.imssystem.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.imssystem.R;
import com.example.imssystem.models.Template;

import java.util.List;

public class TemplateAdapter extends RecyclerView.Adapter<TemplateAdapter.TemplateViewHolder> {
    private Context context;
    private List<Template> templateList;
    private OnTemplateClickListener listener;
    private int selectedPosition = -1;

    public interface OnTemplateClickListener {
        void onTemplateClick(Template template);
    }

    public TemplateAdapter(Context context, List<Template> templateList, OnTemplateClickListener listener) {
        this.context = context;
        this.templateList = templateList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TemplateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_template, parent, false);
        return new TemplateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TemplateViewHolder holder, int position) {
        Template template = templateList.get(position);
        holder.templateName.setText(template.getName());
        if (template.getId() == null || template.getId().isEmpty()) {
            holder.templateDepartment.setText("Fill in all fields manually");
        } else {
            holder.templateDepartment.setText("Routes to " + template.getDepartment());
        }

        // Set correct icon based on template
        if (template.getId() == null || template.getId().isEmpty()) {
            holder.templateIcon.setImageResource(R.drawable.ic_menu_edit);
        } else if (template.getName().toLowerCase().contains("wi-fi") || template.getName().toLowerCase().contains("network")) {
            holder.templateIcon.setImageResource(R.drawable.ic_menu_info_details);
        } else if (template.getName().toLowerCase().contains("hostel") || template.getName().toLowerCase().contains("cleanliness")) {
            holder.templateIcon.setImageResource(R.drawable.ic_menu_myplaces);
        } else if (template.getName().toLowerCase().contains("transport") || template.getName().toLowerCase().contains("delay")) {
            holder.templateIcon.setImageResource(R.drawable.ic_menu_directions);
        } else if (template.getName().toLowerCase().contains("fee") || template.getName().toLowerCase().contains("query")) {
            holder.templateIcon.setImageResource(R.drawable.ic_menu_my_calendar);
        } else {
            holder.templateIcon.setImageResource(R.drawable.ic_menu_agenda);
        }

        // Handle selected state
        if (selectedPosition == position) {
            holder.templateCard.setBackgroundResource(R.drawable.card_background_purple);
            holder.templateIcon.setColorFilter(ContextCompat.getColor(context, R.color.white));
            ((LinearLayout) holder.templateIcon.getParent()).setBackgroundResource(R.drawable.card_white_rounded);
            holder.templateName.setTextColor(ContextCompat.getColor(context, R.color.white));
            holder.templateDepartment.setTextColor(ContextCompat.getColor(context, R.color.white));
        } else {
            holder.templateCard.setBackgroundResource(R.drawable.card_white_rounded);
            holder.templateIcon.setColorFilter(ContextCompat.getColor(context, R.color.purple_start));
            ((LinearLayout) holder.templateIcon.getParent()).setBackgroundResource(R.drawable.card_background_purple);
            holder.templateName.setTextColor(ContextCompat.getColor(context, R.color.dark_blue));
            holder.templateDepartment.setTextColor(ContextCompat.getColor(context, R.color.gray_medium));
        }

        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
            listener.onTemplateClick(template);
        });
    }

    @Override
    public int getItemCount() {
        return templateList.size();
    }

    public Template getSelectedTemplate() {
        if (selectedPosition != -1) {
            return templateList.get(selectedPosition);
        }
        return null;
    }

    static class TemplateViewHolder extends RecyclerView.ViewHolder {
        LinearLayout templateCard;
        ImageView templateIcon;
        TextView templateName;
        TextView templateDepartment;

        public TemplateViewHolder(@NonNull View itemView) {
            super(itemView);
            templateCard = itemView.findViewById(R.id.template_card);
            templateIcon = itemView.findViewById(R.id.template_icon);
            templateName = itemView.findViewById(R.id.template_name);
            templateDepartment = itemView.findViewById(R.id.template_department);
        }
    }
}
