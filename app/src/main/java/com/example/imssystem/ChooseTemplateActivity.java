package com.example.imssystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.imssystem.adapters.TemplateAdapter;
import com.example.imssystem.helpers.FirebaseRepository;
import com.example.imssystem.models.Template;
import java.util.ArrayList;
import java.util.List;

public class ChooseTemplateActivity extends AppCompatActivity {
    private FirebaseRepository repo;
    private RecyclerView recyclerView;
    private TemplateAdapter adapter;
    private List<Template> templateList;
    private String userId;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_choose_template);

            repo = FirebaseRepository.getInstance();
            userId = getIntent().getStringExtra("userId");
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
                    try {
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
                    } catch (Exception e) {
                        Toast.makeText(ChooseTemplateActivity.this,
                                "Failed to open form: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
            recyclerView.setAdapter(adapter);

            loadTemplates();
        } catch (Exception e) {
            Toast.makeText(this, "Error loading templates: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void loadTemplates() {
        repo.getAllTemplates(new FirebaseRepository.OnCompleteListener<List<Template>>() {
            @Override
            public void onSuccess(List<Template> templates) {
                templateList.clear();
                if (templates != null) {
                    templateList.addAll(templates);
                }
                Template customTemplate = new Template("Custom complaint", "", "", "", "Fill in all fields manually", "");
                customTemplate.setId("custom");
                templateList.add(customTemplate);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                templateList.clear();
                Template customTemplate = new Template("Custom complaint", "", "", "", "Fill in all fields manually", "");
                customTemplate.setId("custom");
                templateList.add(customTemplate);
                adapter.notifyDataSetChanged();
            }
        });
    }
}
