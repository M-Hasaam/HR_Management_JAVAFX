package com.hr.ui;
// GRASP Pattern: Controller — handles UC-09 Request Attendance Correction
// Participants: AttendanceCorrectionRequest (Creator/Information Expert),
//              AuditLogService (Pure Fabrication)

import com.hr.controller.AttendanceCorrectionController;
import com.hr.dao.AttendanceCorrectionDAO;
import com.hr.dao.AttendanceRecordDAO;
import com.hr.model.AttendanceCorrectionRequest;
import com.hr.model.AttendanceRecord;
import com.hr.model.Employee;
import com.hr.service.AuditLogService;
import com.hr.service.EmployeeService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.util.List;

public class CorrectionController {

    @FXML private ComboBox<Employee>         cbEmployee;
    @FXML private ComboBox<AttendanceRecord> cbAttendanceRecord;
    @FXML private TextField                  tfOriginalValue;
    @FXML private TextField                  tfCorrectedValue;
    @FXML private TextArea                   taJustification;

    @FXML private TableView<AttendanceCorrectionRequest>                table;
    @FXML private TableColumn<AttendanceCorrectionRequest, Integer>     colId;
    @FXML private TableColumn<AttendanceCorrectionRequest, String>      colEmployee;
    @FXML private TableColumn<AttendanceCorrectionRequest, String>      colDate;
    @FXML private TableColumn<AttendanceCorrectionRequest, String>      colOriginal;
    @FXML private TableColumn<AttendanceCorrectionRequest, String>      colCorrected;
    @FXML private TableColumn<AttendanceCorrectionRequest, String>      colStatus;

    private AttendanceCorrectionController correctionController; // SD Controller (UC-09)
    private AttendanceCorrectionDAO        correctionDAO;
    private AttendanceRecordDAO            attendanceDAO;
    private AuditLogService                auditLogService;
    private EmployeeService                employeeService;

    private final ObservableList<AttendanceCorrectionRequest> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        try {
            correctionController = new AttendanceCorrectionController(); // SD Controller
            correctionDAO   = new AttendanceCorrectionDAO();
            attendanceDAO   = new AttendanceRecordDAO();
            auditLogService = new AuditLogService();
            employeeService = new EmployeeService();
        } catch (SQLException e) {
            showError("Database Error", "Could not connect to database: " + e.getMessage());
            return;
        }

        try {
            List<Employee> employees = employeeService.getAllEmployees();
            cbEmployee.setItems(FXCollections.observableArrayList(employees));
        } catch (SQLException e) {
            showError("Load Error", "Could not load employees: " + e.getMessage());
        }

        cbAttendanceRecord.setOnAction(e -> {
            AttendanceRecord rec = cbAttendanceRecord.getValue();
            if (rec != null) {
                String original = rec.getCheckInTime() != null
                        ? rec.getCheckInTime().toString() : "N/A";
                tfOriginalValue.setText(original);
            }
        });

        tfOriginalValue.setEditable(false);

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEmployee.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        colOriginal.setCellValueFactory(new PropertyValueFactory<>("originalValue"));
        colCorrected.setCellValueFactory(new PropertyValueFactory<>("correctedValue"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.setItems(data);
        loadAllCorrections();
    }

    public void submitCorrectionRequest(int attendanceID, String field,
                                        String originalVal, String correctedVal,
                                        String reason) throws SQLException {
        // Delegate to SD Controller (UC-09) — business logic lives there, not in the UI layer
        correctionController.submitCorrectionRequest(
                attendanceID, field, originalVal, correctedVal, reason);
    }

    @FXML
    private void handleLoadRecords() {
        Employee sel = cbEmployee.getValue();
        if (sel == null) { showError("Input Error", "Please select an employee."); return; }
        try {
            List<AttendanceRecord> records = attendanceDAO.getByEmployee(sel.getId());
            cbAttendanceRecord.setItems(FXCollections.observableArrayList(records));
            if (records.isEmpty()) {
                showInfo("No attendance records found for the selected employee.");
            }
        } catch (SQLException e) {
            showError("Load Error", e.getMessage());
        }
    }

    @FXML
    private void handleSubmitCorrection() {
        AttendanceRecord selRec = cbAttendanceRecord.getValue();
        if (selRec == null) {
            showError("Input Error", "Please select an attendance record to correct.");
            return;
        }
        String corrected     = tfCorrectedValue.getText().trim();
        String justification = taJustification.getText().trim();
        if (corrected.isEmpty()) {
            showError("Input Error", "Please enter the corrected value.");
            return;
        }
        if (justification.isEmpty()) {
            showError("Input Error", "Please enter a justification.");
            return;
        }
        try {
            submitCorrectionRequest(
                    selRec.getId(),
                    "check_in_time",
                    tfOriginalValue.getText(),
                    corrected,
                    justification
            );
            showInfo("Correction request submitted successfully.");
            tfCorrectedValue.clear();
            taJustification.clear();
            loadAllCorrections();
        } catch (IllegalArgumentException e) {
            showError("Validation Error", e.getMessage());
        } catch (SQLException e) {
            showError("Save Error", e.getMessage());
        }
    }

    private void loadAllCorrections() {
        try {
            data.setAll(correctionDAO.getAll());
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
