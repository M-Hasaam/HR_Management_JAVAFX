package com.hr.model;

public class Designation {
    private int id;
    private String title;
    private String description;
    private String salaryGrade;

    public Designation() {}

    public Designation(int id, String title, String description, String salaryGrade) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.salaryGrade = salaryGrade;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSalaryGrade() { return salaryGrade; }
    public void setSalaryGrade(String salaryGrade) { this.salaryGrade = salaryGrade; }

    @Override
    public String toString() { return title; }
}
