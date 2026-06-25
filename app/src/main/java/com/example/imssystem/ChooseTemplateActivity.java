package com.example.imssystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.imssystem.adapters.TemplateAdapter;
import com.example.imssystem.helpers.DBHelper;
import com.example.imssystem.models.Template;

import java.util.ArrayList;
import java.util.List;

public class ChooseTemplateActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private RecyclerView recyclerView;
    private TemplateAdapter adapter;
    private List<Template> templateList;
    private Template selectedTemplate;
    private int userId;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_template);

        dbHelper = new DBHelper(this);
        userId = getIntent().getIntExtra("userId", 0);
        userName = getIntent().getStringExtra("userName");

        ImageButton backArrow = findViewById(R.id.back_arrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recycler_templates);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        templateList = new ArrayList<>();
        adapter = new TemplateAdapter(this, templateList, new TemplateAdapter.OnTemplateClickListener() {
            @Override
            public void onTemplateClick(Template template) {
                selectedTemplate = template;
            }
        });
        recyclerView.setAdapter(adapter);

        loadTemplates();

        Button continueBtn = findViewById(R.id.continue_btn);
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChooseTemplateActivity.this, NewComplaintActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("userName", userName);
                if (selectedTemplate != null) {
                    intent.putExtra("templateId", selectedTemplate.getId());
                    intent.putExtra("templateName", selectedTemplate.getName());
                    intent.putExtra("templateCategory", selectedTemplate.getCategory());
                    intent.putExtra("templateDepartment", selectedTemplate.getDepartment());
                    intent.putExtra("templateTitle", selectedTemplate.getDefaultTitle());
                    intent.putExtra("templateDescription", selectedTemplate.getDefaultDescription());
                    intent.putExtra("templatePriority", selectedTemplate.getPriorityHint());
                }
                startActivity(intent);
            }
        });
    }

    private void loadTemplates() {
        templateList.clear();
        templateList.addAll(dbHelper.getAllTemplates());
        // Add custom template at the end
        Template customTemplate = new Template("Custom complaint", "", "", "", "Fill in all fields manually", "");
        customTemplate.setId(-1);
        templateList.add(customTemplate);
        adapter.notifyDataSetChanged();
    }
}
