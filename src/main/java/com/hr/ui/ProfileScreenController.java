package com.hr.ui;
// GRASP Pattern: Controller (UI Facade) — JavaFX controller for employee_profile.fxml
// Delegates business logic to ProfileController (UC-05).

import com.hr.controller.ProfileController;
import com.hr.model.Employee;
import com.hr.service.EmployeeService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.util.List;

public class ProfileScreenController {

    @FXML private ComboBox<Employee> cbEmployee;
    @FXML private ComboBox<String>   cbRole;
    @FXML private Label              lblPermissions;

    @FXML private Label lblEmpId;
    @FXML private Label lblName;
    @FXML private Label lblEmail;
    @FXML private Label lblPhone;
    @FXML private Label lblDept;
    @FXML private Label lblPosition;
    @FXML private Label lblStatus;
    @FXML private Label lblNationalId;
    @FXML private Label lblEmpType;
    @FXML private Label lblHireDate;

    // SD Controller — UC-05 business logic
    private ProfileController profileController;

    private EmployeeService employeeService;

    @FXML
    public void initialize() {
        try {
            profileController = new ProfileController();
            employeeService   = new EmployeeService();
        } catch (SQLException e) {
            showError("Database Error", e.getMessage());
            return;
        }

        cbRole.setItems(FXCollections.observableArrayList("ADMIN", "HR", "EMPLOYEE"));
        cbRole.setValue("HR");

        try {
            List<Employee> employees = employeeService.getAllEmployees();
            cbEmployee.setItems(FXCollections.observableArrayList(employees));
        } catch (SQLException e) {
            showError("Load Error", "Could not load employees: " + e.getMessage());
        }
    }

    @FXML
    private void handleViewProfile() {
        Employee sel  = cbEmployee.getValue();
        String   role = cbRole.getValue();

        if (sel == null) {
            showError("Input Error", "Please select an employee.");
            return;
        }
        if (role == null) {
            showError("Input Error", "Please select a role.");
            return;
        }

        try {
            Employee emp = profileController.viewEmployeeProfile(sel.getId(), role, 1);
            List<String> permitted = profileController.getPermittedFields(role, sel.getId());

            lblEmpId.setText(String.valueOf(emp.getId()));
            lblName.setText(emp.getFullName());
            lblEmail.setText(permitted.contains("email") ? emp.getEmail() : "***");
            lblPhone.setText(permitted.contains("phone") ? emp.getPhone() : "***");
            lblDept.setText(String.valueOf(emp.getDepartmentId()));
            lblPosition.setText(emp.getPosition());
            lblStatus.setText(emp.getStatus());
            lblNationalId.setText(permitted.contains("nationalId") ? emp.getNationalId() : "***");
            lblEmpType.setText(permitted.contains("employmentType") ? emp.getEmploymentType() : "***");
            lblHireDate.setText(emp.getHireDate() != null ? emp.getHireDate().toString() : "");

            lblPermissions.setText("Permitted fields: " + String.join(", ", permitted));
        } catch (SQLException e) {
            showError("Profile Error", e.getMessage());
        }
    }

    private void showError(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
