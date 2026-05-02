package com.hr.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Employee {
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private int departmentId;
    private String departmentName;
    private String position;
    private BigDecimal basicSalary;
    private LocalDate hireDate;
    private String status;

    // New fields
    private String nationalId;
    private LocalDate dateOfBirth;
    private String gender;
    private String employmentType;
    private LocalDate probationEndDate;
    private int designationId;
    private String address;

    public Employee() {}

    public Employee(int id, String firstName, String lastName, String email, String phone,
                    int departmentId, String departmentName, String position,
                    BigDecimal basicSalary, LocalDate hireDate, String status) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.position = position;
        this.basicSalary = basicSalary;
        this.hireDate = hireDate;
        this.status = status;
    }

    public Employee(int id, String firstName, String lastName, String email, String phone,
                    int departmentId, String departmentName, String position,
                    BigDecimal basicSalary, LocalDate hireDate, String status,
                    String nationalId, LocalDate dateOfBirth, String gender,
                    String employmentType, LocalDate probationEndDate, int designationId) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.position = position;
        this.basicSalary = basicSalary;
        this.hireDate = hireDate;
        this.status = status;
        this.nationalId = nationalId;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.employmentType = employmentType;
        this.probationEndDate = probationEndDate;
        this.designationId = designationId;
    }

    public String getFullName() { return firstName + " " + lastName; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public int getDepartmentId() { return departmentId; }
    public void setDepartmentId(int departmentId) { this.departmentId = departmentId; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public BigDecimal getBasicSalary() { return basicSalary; }
    public void setBasicSalary(BigDecimal basicSalary) { this.basicSalary = basicSalary; }
    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNationalId() { return nationalId; }
    public void setNationalId(String nationalId) { this.nationalId = nationalId; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getEmploymentType() { return employmentType; }
    public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }
    public LocalDate getProbationEndDate() { return probationEndDate; }
    public void setProbationEndDate(LocalDate probationEndDate) { this.probationEndDate = probationEndDate; }
    public int getDesignationId() { return designationId; }
    public void setDesignationId(int designationId) { this.designationId = designationId; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    @Override
    public String toString() { return getFullName(); }
}
