package com.hr.ui;
// GRASP Pattern: Controller — handles UC-10 View Leave Balance and Attendance Summary
// Participants: LeaveBalance (Information Expert), AttendanceRecord (Information Expert),
//              ReportGenerator (Pure Fabrication)

import com.hr.controller.LeaveDashboardController;
import com.hr.dao.AttendanceRecordDAO;
import com.hr.model.AttendanceRecord;
import com.hr.model.Employee;
import com.hr.service.EmployeeService;
import com.hr.service.LeaveRequestService;
import com.hr.service.ReportGenerator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class DashboardController {

    @FXML private ComboBox<Employee> cbEmployee;
    @FXML private Label              lblAnnual;
    @FXML private Label              lblSick;
    @FXML private Label              lblPersonal;
    @FXML private Label              lblAttendanceCount;

    @FXML private TableView<AttendanceRecord>            table;
    @FXML private TableColumn<AttendanceRecord, String>  colDate;
    @FXML private TableColumn<AttendanceRecord, String>  colCheckIn;
    @FXML private TableColumn<AttendanceRecord, String>  colCheckOut;
    @FXML private TableColumn<AttendanceRecord, Double>  colHours;
    @FXML private TableColumn<AttendanceRecord, String>  colStatus;

    private LeaveDashboardController leaveDashboardController; // SD Controller (UC-10)
    private LeaveRequestService      leaveService;
    private AttendanceRecordDAO      attendanceDAO;
    private ReportGenerator          reportGenerator;
    private EmployeeService          employeeService;

    private final ObservableList<AttendanceRecord> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        reportGenerator = new ReportGenerator();
        try {
            leaveDashboardController = new LeaveDashboardController(); // SD Controller
            leaveService    = new LeaveRequestService();
            attendanceDAO   = new AttendanceRecordDAO();
            employeeService = new EmployeeService();
        } catch (SQLException e) {
            showError("Database Error", e.getMessage());
            return;
        }

        try {
            List<Employee> employees = employeeService.getAllEmployees();
            cbEmployee.setItems(FXCollections.observableArrayList(employees));
        } catch (SQLException e) {
            showError("Load Error", "Could not load employees: " + e.getMessage());
        }

        colDate.setCellValueFactory(new PropertyValueFactory<>("attendanceDate"));
        colCheckIn.setCellValueFactory(new PropertyValueFactory<>("checkInTime"));
        colCheckOut.setCellValueFactory(new PropertyValueFactory<>("checkOutTime"));
        colHours.setCellValueFactory(new PropertyValueFactory<>("totalHours"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("attendanceStatus"));

        table.setItems(data);
    }

    public void viewLeaveAndAttendanceDashboard(int userID) throws SQLException {
        // Delegate to SD Controller (UC-10) — keeps business logic out of UI layer
        int[] balance = leaveDashboardController.getLeaveBalance(userID);
        lblAnnual.setText(String.valueOf(balance[0]));
        lblSick.setText(String.valueOf(balance[1]));
        lblPersonal.setText(String.valueOf(balance[2]));

        List<AttendanceRecord> records = leaveDashboardController.getAttendanceSummary(userID, null);
        data.setAll(records);
        lblAttendanceCount.setText("Total Records: " + records.size());

        leaveDashboardController.generateDashboardReport(userID, "PDF");
    }

    public int[] getLeaveBalance(int userID, int year) throws SQLException {
        return leaveDashboardController.getLeaveBalance(userID);
    }

    public List<AttendanceRecord> getAttendanceSummary(int userID, LocalDate[] dateRange)
            throws SQLException {
        return leaveDashboardController.getAttendanceSummary(userID, dateRange);
    }

    @FXML
    private void handleViewDashboard() {
        Employee sel = cbEmployee.getValue();
        if (sel == null) { showError("Input Error", "Please select an employee."); return; }
        try {
            viewLeaveAndAttendanceDashboard(sel.getId());
        } catch (SQLException e) {
            showError("Dashboard Error", e.getMessage());
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
