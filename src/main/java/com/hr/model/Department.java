package com.hr.model;

public class Department {
    private int id;
    private String name;
    private String description;

    // New fields
    private int managerId;
    private int parentDeptId;
    private int maxHeadcount;
    private int currentHeadcount;

    public Department() {}

    public Department(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Department(int id, String name, String description,
                      int managerId, int parentDeptId, int maxHeadcount, int currentHeadcount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.managerId = managerId;
        this.parentDeptId = parentDeptId;
        this.maxHeadcount = maxHeadcount;
        this.currentHeadcount = currentHeadcount;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getManagerId() { return managerId; }
    public void setManagerId(int managerId) { this.managerId = managerId; }
    public int getParentDeptId() { return parentDeptId; }
    public void setParentDeptId(int parentDeptId) { this.parentDeptId = parentDeptId; }
    public int getMaxHeadcount() { return maxHeadcount; }
    public void setMaxHeadcount(int maxHeadcount) { this.maxHeadcount = maxHeadcount; }
    public int getCurrentHeadcount() { return currentHeadcount; }
    public void setCurrentHeadcount(int currentHeadcount) { this.currentHeadcount = currentHeadcount; }

    @Override
    public String toString() { return name; }
}
