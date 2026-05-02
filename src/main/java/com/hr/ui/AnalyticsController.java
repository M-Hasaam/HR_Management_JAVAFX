package com.hr.ui;
// GRASP Pattern: Controller — handles UC-15 Generate HR Analytics Report
// Participants: HRAnalyticsReport (Creator/Information Expert),
//              AnalyticsAggregator (Pure Fabrication), DataVisualization (Pure Fabrication),
//              ReportArchive (Pure Fabrication)

import com.hr.controller.HRAnalyticsController;
import com.hr.dao.HRAnalyticsReportDAO;
import com.hr.model.HRAnalyticsReport;
import com.hr.service.AnalyticsAggregator;
import com.hr.service.DataVisualization;
import com.hr.service.ReportArchive;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AnalyticsController {

    @FXML private CheckBox chkHeadcount;
    @FXML private CheckBox chkAttrition;
    @FXML private CheckBox chkLeave;
    @FXML private CheckBox chkAttendance;
    @FXML private TextField tfPeriod;
    @FXML private TextField tfScope;
    @FXML private TextArea  taResult;

    private HRAnalyticsController hrAnalyticsController; // SD Controller (UC-15)
    private AnalyticsAggregator   analyticsAggregator;
    private DataVisualization     dataVisualization;
    private ReportArchive         reportArchive;
    private HRAnalyticsReportDAO  reportDAO;

    @FXML
    public void initialize() {
        dataVisualization = new DataVisualization();
        reportArchive     = new ReportArchive();
        try {
            hrAnalyticsController = new HRAnalyticsController(); // SD Controller
            analyticsAggregator = new AnalyticsAggregator();
            reportDAO           = new HRAnalyticsReportDAO();
        } catch (SQLException e) {
            showError("Database Error", e.getMessage());
            return;
        }

        taResult.setEditable(false);
        taResult.setPromptText("Analytics results will appear here after generation...");
    }

    public void generateHRAnalyticsReport(List<String> metrics, String dateRange, String scope)
            throws SQLException {
        List<String> unavailable = hrAnalyticsController.validateMetricsAvailability(metrics);
        if (!unavailable.isEmpty()) {
            throw new IllegalArgumentException(
                    "The following metrics are unavailable: " + String.join(", ", unavailable));
        }

        boolean headcount  = metrics.contains("headcount");
        boolean attrition  = metrics.contains("attrition");
        boolean leave      = metrics.contains("leave");
        boolean attendance = metrics.contains("attendance");

        // Delegate to SD Controller (UC-15) — full 3-tier workflow with Factory + Strategy + NFR
        String result = hrAnalyticsController.generateAnalyticsReport(
                headcount, attrition, leave, attendance, dateRange, 1);
        taResult.setText(result);
    }

    public List<String> validateMetricsAvailability(List<String> metrics) throws SQLException {
        return hrAnalyticsController.validateMetricsAvailability(metrics);
    }

    @FXML
    private void handleGenerate() {
        List<String> metrics = new ArrayList<>();
        if (chkHeadcount.isSelected())  metrics.add("headcount");
        if (chkAttrition.isSelected())  metrics.add("attrition");
        if (chkLeave.isSelected())      metrics.add("leave");
        if (chkAttendance.isSelected()) metrics.add("attendance");

        if (metrics.isEmpty()) {
            showError("Input Error", "Please select at least one metric.");
            return;
        }

        String period = tfPeriod.getText().trim();
        String scope  = tfScope.getText().trim();

        if (period.isEmpty()) {
            showError("Input Error", "Please enter a report period (e.g. 2025-Q1).");
            return;
        }
        if (scope.isEmpty()) scope = "ALL";

        try {
            generateHRAnalyticsReport(metrics, period, scope);
        } catch (IllegalArgumentException e) {
            showError("Metrics Error", e.getMessage());
        } catch (SQLException e) {
            showError("Analytics Error", e.getMessage());
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
