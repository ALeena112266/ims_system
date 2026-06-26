package com.example.imssystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

        recyclerView = findViewById(R.id.recycler_templates);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        templateList = new ArrayList<>();
        adapter = new TemplateAdapter(this, templateList, new TemplateAdapter.OnTemplateClickListener() {
            @Override
            public void onTemplateClick(Template template) {
                Intent intent = new Intent(ChooseTemplateActivity.this, NewComplaintActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("userName", userName);
                intent.putExtra("templateId", template.getId());
                intent.putExtra("templateName", template.getName());
                intent.putExtra("templateCategory", template.getCategory());
                intent.putExtra("templateDepartment", template.getDepartment());
                intent.putExtra("templateTitle", template.getDefaultTitle());
                intent.putExtra("templateDescription", template.getDefaultDescription());
                intent.putExtra("templatePriority", template.getPriorityHint());
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);

        loadTemplates();
    }

    private void loadTemplates() {
        templateList.clear();
        templateList.addAll(dbHelper.getAllTemplates());
        Template customTemplate = new Template("Custom complaint", "", "", "", "Fill in all fields manually", "");
        customTemplate.setId(-1);
        templateList.add(customTemplate);
        adapter.notifyDataSetChanged();
    }
}
