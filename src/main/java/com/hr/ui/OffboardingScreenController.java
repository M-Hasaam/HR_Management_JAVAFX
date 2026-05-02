package com.hr.ui;
// GRASP Pattern: Controller (UI Facade) — JavaFX controller for offboarding.fxml
// Delegates business logic to OffboardingController (UC-04).

import com.hr.controller.OffboardingController;
import com.hr.dao.OffboardingWorkflowDAO;
import com.hr.model.Employee;
import com.hr.model.OffboardingWorkflow;
import com.hr.service.EmployeeService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class OffboardingScreenController {

    @FXML private ComboBox<Employee>  cbEmployee;
    @FXML private ComboBox<String>    cbSeparationType;
    @FXML private DatePicker          dpLastWorking;
    @FXML private TextArea            taExitReason;

    @FXML private TableView<OffboardingWorkflow>                    table;
    @FXML private TableColumn<OffboardingWorkflow, Integer>         colId;
    @FXML private TableColumn<OffboardingWorkflow, String>          colEmployee;
    @FXML private TableColumn<OffboardingWorkflow, String>          colSeparationType;
    @FXML private TableColumn<OffboardingWorkflow, LocalDate>       colLastWorking;
    @FXML private TableColumn<OffboardingWorkflow, String>          colStatus;
    @FXML private TableColumn<OffboardingWorkflow, String>          colSettlement;

    // SD Controller — UC-04 business logic
    private OffboardingController offboardingController;

    private EmployeeService        employeeService;
    private OffboardingWorkflowDAO workflowDAO;

    private final ObservableList<OffboardingWorkflow> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        try {
            offboardingController = new OffboardingController();
            employeeService       = new EmployeeService();
            workflowDAO           = new OffboardingWorkflowDAO();
        } catch (SQLException e) {
            showError("Database Error", e.getMessage());
            return;
        }

        cbSeparationType.setItems(FXCollections.observableArrayList(
                "RESIGNATION", "RETIREMENT", "CONTRACT_EXPIRY", "TERMINATION"));
        cbSeparationType.setValue("RESIGNATION");

        try {
            List<Employee> employees = employeeService.getAllEmployees();
            cbEmployee.setItems(FXCollections.observableArrayList(employees));
        } catch (SQLException e) {
            showError("Load Error", "Could not load employees: " + e.getMessage());
        }

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEmployee.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        colSeparationType.setCellValueFactory(new PropertyValueFactory<>("separationType"));
        colLastWorking.setCellValueFactory(new PropertyValueFactory<>("lastWorkingDate"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colSettlement.setCellValueFactory(new PropertyValueFactory<>("finalSettlementStatus"));

        table.setItems(data);
        loadWorkflows();
    }

    @FXML
    private void handleInitiate() {
        Employee sel = cbEmployee.getValue();
        if (sel == null) {
            showError("Input Error", "Please select an employee.");
            return;
        }
        String separationType = cbSeparationType.getValue();
        LocalDate lastWorking = dpLastWorking.getValue();

        if (separationType == null) {
            showError("Input Error", "Please select a separation type.");
            return;
        }
        if (lastWorking == null) {
            showError("Input Error", "Please select the last working date.");
            return;
        }

        try {
            offboardingController.initiateOffboarding(sel.getId(), separationType, lastWorking);
            showInfo("Offboarding initiated for " + sel.getFullName());
            taExitReason.clear();
            dpLastWorking.setValue(null);
            loadWorkflows();
        } catch (IllegalArgumentException e) {
            showError("Policy Violation", e.getMessage());
        } catch (SQLException e) {
            showError("Save Error", e.getMessage());
        }
    }

    private void loadWorkflows() {
        try {
            data.setAll(workflowDAO.getAll());
        } catch (SQLException e) {
            showError("Load Error", e.getMessage());
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
