package com.example.imssystem.models;

public class Template {
    private int id;
    private String name;
    private String category;
    private String department;
    private String defaultTitle;
    private String defaultDescription;
    private String priorityHint;

    public Template() {
    }

    public Template(String name, String category, String department, String defaultTitle, String defaultDescription, String priorityHint) {
        this.name = name;
        this.category = category;
        this.department = department;
        this.defaultTitle = defaultTitle;
        this.defaultDescription = defaultDescription;
        this.priorityHint = priorityHint;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDefaultTitle() {
        return defaultTitle;
    }

    public void setDefaultTitle(String defaultTitle) {
        this.defaultTitle = defaultTitle;
    }

    public String getDefaultDescription() {
        return defaultDescription;
    }

    public void setDefaultDescription(String defaultDescription) {
        this.defaultDescription = defaultDescription;
    }

    public String getPriorityHint() {
        return priorityHint;
    }

    public void setPriorityHint(String priorityHint) {
        this.priorityHint = priorityHint;
    }
}
