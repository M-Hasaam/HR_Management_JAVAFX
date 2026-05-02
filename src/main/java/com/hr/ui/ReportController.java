package com.hr.ui;
// GRASP Pattern: Controller — handles UC-14 Generate Compliance Report
// Participants: ComplianceReport (Creator/Information Expert),
//              ComplianceDataAgg (Pure Fabrication), ReportGenerator (Pure Fabrication),
//              SecureStorage (Pure Fabrication)

import com.hr.controller.ComplianceReportController;
import com.hr.dao.ComplianceReportDAO;
import com.hr.model.ComplianceReport;
import com.hr.service.ComplianceDataAgg;
import com.hr.service.ReportGenerator;
import com.hr.service.SecureStorage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.sql.SQLException;

public class ReportController {

    @FXML private ComboBox<String> cbDomain;
    @FXML private TextField        tfPeriod;
    @FXML private ComboBox<String> cbFormat;
    @FXML private Label            lblResult;

    @FXML private TableView<ComplianceReport>               table;
    @FXML private TableColumn<ComplianceReport, Integer>    colId;
    @FXML private TableColumn<ComplianceReport, String>     colType;
    @FXML private TableColumn<ComplianceReport, String>     colGeneratedAt;
    @FXML private TableColumn<ComplianceReport, String>     colFormat;
    @FXML private TableColumn<ComplianceReport, String>     colStatus;
    @FXML private TableColumn<ComplianceReport, String>     colArchivePath;

    private ComplianceReportController complianceReportController; // SD Controller (UC-14)
    private ComplianceDataAgg          complianceDataAgg;
    private ReportGenerator            reportGenerator;
    private SecureStorage              secureStorage;
    private ComplianceReportDAO        reportDAO;

    private final ObservableList<ComplianceReport> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        reportGenerator = new ReportGenerator();
        secureStorage   = new SecureStorage();
        try {
            complianceReportController = new ComplianceReportController(); // SD Controller
            complianceDataAgg = new ComplianceDataAgg();
            reportDAO         = new ComplianceReportDAO();
        } catch (SQLException e) {
            showError("Database Error", e.getMessage());
            return;
        }

        cbDomain.setItems(FXCollections.observableArrayList("LEAVE", "ATTENDANCE", "ALL"));
        cbDomain.setValue("ALL");

        cbFormat.setItems(FXCollections.observableArrayList("PDF", "EXCEL", "CSV"));
        cbFormat.setValue("PDF");

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colType.setCellValueFactory(new PropertyValueFactory<>("reportType"));
        colGeneratedAt.setCellValueFactory(new PropertyValueFactory<>("generatedAt"));
        colFormat.setCellValueFactory(new PropertyValueFactory<>("format"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colArchivePath.setCellValueFactory(new PropertyValueFactory<>("archivePath"));

        table.setItems(data);
        loadReports();
    }

    public void generateComplianceReport(String domain, String period, String format)
            throws SQLException {
        // Delegate to SD Controller (UC-14) — full 3-tier workflow with Factory + Strategy + NFR
        ComplianceReport report = complianceReportController.generateComplianceReport(
                domain, period, format, 1);
        loadReports();
        lblResult.setText("Report generated: " + report.getReportType()
                + " | Format: " + format + " | Archive: " + report.getArchivePath());
    }

    @FXML
    private void handleGenerate() {
        String domain = cbDomain.getValue();
        String period = tfPeriod.getText().trim();
        String format = cbFormat.getValue();

        if (domain == null || period.isEmpty() || format == null) {
            showError("Input Error", "Please fill in all fields (domain, period, format).");
            return;
        }

        try {
            generateComplianceReport(domain, period, format);
            showInfo("Compliance report generated successfully.");
        } catch (SQLException e) {
            showError("Report Error", e.getMessage());
        }
    }

    private void loadReports() {
        try {
            data.setAll(reportDAO.getAll());
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
