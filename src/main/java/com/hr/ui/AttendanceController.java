package com.hr.ui;
// GRASP Pattern: Controller — handles UC-08 Record Daily Attendance
// Participants: AttendanceRecord (Creator/Information Expert), PayrollNotifier (Pure Fabrication)

import com.hr.controller.AttendanceRecordController;
import com.hr.dao.AttendanceRecordDAO;
import com.hr.model.AttendanceRecord;
import com.hr.model.Employee;
import com.hr.service.EmployeeService;
import com.hr.service.PayrollNotifier;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class AttendanceController {

    @FXML private ComboBox<Employee> cbEmployee;
    @FXML private ComboBox<String>   cbMethod;
    @FXML private Button             btnCheckIn;
    @FXML private Button             btnCheckOut;
    @FXML private Label              lblStatus;

    @FXML private TableView<AttendanceRecord>            table;
    @FXML private TableColumn<AttendanceRecord, Integer> colId;
    @FXML private TableColumn<AttendanceRecord, String>  colEmployee;
    @FXML private TableColumn<AttendanceRecord, String>  colDate;
    @FXML private TableColumn<AttendanceRecord, String>  colCheckIn;
    @FXML private TableColumn<AttendanceRecord, String>  colCheckOut;
    @FXML private TableColumn<AttendanceRecord, Double>  colHours;
    @FXML private TableColumn<AttendanceRecord, String>  colStatus;

    private AttendanceRecordController attendanceRecordController; // SD Controller (UC-08)
    private AttendanceRecordDAO        attendanceDAO;
    private PayrollNotifier            payrollNotifier;
    private EmployeeService            employeeService;

    private final ObservableList<AttendanceRecord> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        payrollNotifier = new PayrollNotifier();
        try {
            attendanceRecordController = new AttendanceRecordController(); // SD Controller
            attendanceDAO   = new AttendanceRecordDAO();
            employeeService = new EmployeeService();
        } catch (SQLException e) {
            showError("Database Error", e.getMessage());
            return;
        }

        cbMethod.setItems(FXCollections.observableArrayList("BIOMETRIC", "MANUAL", "CARD", "MOBILE"));
        cbMethod.setValue("BIOMETRIC");

        try {
            List<Employee> employees = employeeService.getAllEmployees();
            cbEmployee.setItems(FXCollections.observableArrayList(employees));
        } catch (SQLException e) {
            showError("Load Error", "Could not load employees: " + e.getMessage());
        }

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEmployee.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("attendanceDate"));
        colCheckIn.setCellValueFactory(new PropertyValueFactory<>("checkInTime"));
        colCheckOut.setCellValueFactory(new PropertyValueFactory<>("checkOutTime"));
        colHours.setCellValueFactory(new PropertyValueFactory<>("totalHours"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("attendanceStatus"));

        table.setItems(data);

        cbEmployee.setOnAction(e -> {
            Employee sel = cbEmployee.getValue();
            if (sel != null) {
                loadRecords(sel.getId());
                updateButtonStates(sel.getId());
            }
        });
    }

    public String checkIn(int employeeID, LocalDateTime timestamp, String method) throws SQLException {
        // Delegate to SD Controller (UC-08) — keeps business logic out of UI layer
        return attendanceRecordController.recordCheckIn(employeeID, timestamp, method);
    }

    public void checkOut(int employeeID, LocalDateTime timestamp) throws SQLException {
        // Delegate to SD Controller (UC-08)
        attendanceRecordController.recordCheckOut(employeeID, timestamp);
    }

    @FXML
    private void handleCheckIn() {
        Employee sel = cbEmployee.getValue();
        if (sel == null) { showError("Input Error", "Please select an employee."); return; }
        String method = cbMethod.getValue() != null ? cbMethod.getValue() : "MANUAL";
        try {
            checkIn(sel.getId(), LocalDateTime.now(), method);
            lblStatus.setText("Checked in at " + LocalDateTime.now().toLocalTime().toString().substring(0, 8));
            btnCheckOut.setDisable(false);
            btnCheckIn.setDisable(true);
            loadRecords(sel.getId());
        } catch (SQLException | IllegalStateException e) {
            showError("Check-In Error", e.getMessage());
        }
    }

    @FXML
    private void handleCheckOut() {
        Employee sel = cbEmployee.getValue();
        if (sel == null) { showError("Input Error", "Please select an employee."); return; }
        try {
            checkOut(sel.getId(), LocalDateTime.now());
            lblStatus.setText("Checked out at " + LocalDateTime.now().toLocalTime().toString().substring(0, 8));
            btnCheckOut.setDisable(true);
            btnCheckIn.setDisable(false);
            loadRecords(sel.getId());
        } catch (SQLException | IllegalStateException e) {
            showError("Check-Out Error", e.getMessage());
        }
    }

    private void loadRecords(int employeeId) {
        try {
            data.setAll(attendanceDAO.getByEmployee(employeeId));
        } catch (SQLException e) {
            showError("Load Error", e.getMessage());
        }
    }

    private void updateButtonStates(int employeeId) {
        try {
            AttendanceRecord today = attendanceDAO.getTodayRecord(employeeId);
            boolean checkedIn  = today != null && today.getCheckInTime() != null;
            boolean checkedOut = today != null && today.getCheckOutTime() != null;
            btnCheckIn.setDisable(checkedIn);
            btnCheckOut.setDisable(!checkedIn || checkedOut);
        } catch (SQLException e) {
            btnCheckIn.setDisable(false);
            btnCheckOut.setDisable(true);
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
