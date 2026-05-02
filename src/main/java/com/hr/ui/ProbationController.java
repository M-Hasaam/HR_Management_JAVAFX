package com.hr.ui;
// GRASP Pattern: Controller — handles UC-13 Monitor Probation Status

import com.hr.controller.ProbationMonitorController;
import com.hr.dao.ProbationRecordDAO;
import com.hr.model.ProbationRecord;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.DateCell;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ProbationController {

    // ── Table ──────────────────────────────────────────────────────────────────
    @FXML private TableView<ProbationRecord>              table;
    @FXML private TableColumn<ProbationRecord, Integer>   colId;
    @FXML private TableColumn<ProbationRecord, String>    colEmployee;
    @FXML private TableColumn<ProbationRecord, LocalDate> colStart;
    @FXML private TableColumn<ProbationRecord, LocalDate> colEnd;
    @FXML private TableColumn<ProbationRecord, String>    colDaysLeft;
    @FXML private TableColumn<ProbationRecord, Integer>   colExtensions;
    @FXML private TableColumn<ProbationRecord, String>    colDecision;
    @FXML private TableColumn<ProbationRecord, String>    colStatus;

    // ── Decision form ──────────────────────────────────────────────────────────
    @FXML private ComboBox<String> cbDecision;
    @FXML private DatePicker       dpDecisionDate;
    @FXML private TextArea         taNotes;
    @FXML private Button           btnDecide;

    // ── Conditional rows (shown/hidden by decision type) ──────────────────────
    @FXML private HBox      hboxNewEndDate;   // visible only for EXTENDED
    @FXML private DatePicker dpNewEndDate;
    @FXML private HBox      hboxReason;       // visible only for TERMINATED
    @FXML private TextField tfReason;

    private ProbationMonitorController probationMonitorController;
    private ProbationRecordDAO         probationDAO;

    private final ObservableList<ProbationRecord> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        try {
            probationMonitorController = new ProbationMonitorController();
            probationDAO = new ProbationRecordDAO();
        } catch (SQLException e) {
            showError("Database Error", e.getMessage());
            return;
        }

        cbDecision.setItems(FXCollections.observableArrayList("CONFIRMED", "EXTENDED", "TERMINATED"));
        dpDecisionDate.setValue(LocalDate.now());

        // Standard columns
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEmployee.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        colStart.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colEnd.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        colExtensions.setCellValueFactory(new PropertyValueFactory<>("extensions"));
        colDecision.setCellValueFactory(new PropertyValueFactory<>("decision"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Step 1: "Days Remaining" computed column
        colDaysLeft.setCellValueFactory(cellData -> {
            ProbationRecord rec = cellData.getValue();
            if ("CLOSED".equals(rec.getStatus())) return new SimpleStringProperty("Closed");
            if (rec.getEndDate() == null)          return new SimpleStringProperty("—");
            long days = ChronoUnit.DAYS.between(LocalDate.now(), rec.getEndDate());
            if (days < 0)  return new SimpleStringProperty("Overdue " + Math.abs(days) + "d");
            if (days == 0) return new SimpleStringProperty("Due today");
            return new SimpleStringProperty(days + " days");
        });

        table.setItems(data);

        // Enable decision button only when an ACTIVE record is selected
        btnDecide.setDisable(true);
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) ->
                btnDecide.setDisable(sel == null || !"ACTIVE".equals(sel.getStatus())));

        // Show/hide conditional form rows based on decision type (Ext 3a, reason for TERMINATED)
        hboxNewEndDate.setVisible(false);
        hboxNewEndDate.setManaged(false);
        hboxReason.setVisible(false);
        hboxReason.setManaged(false);

        cbDecision.setOnAction(e -> {
            String d = cbDecision.getValue();
            boolean isExtend    = "EXTENDED".equals(d);
            boolean isTerminate = "TERMINATED".equals(d);
            hboxNewEndDate.setVisible(isExtend);
            hboxNewEndDate.setManaged(isExtend);
            hboxReason.setVisible(isTerminate);
            hboxReason.setManaged(isTerminate);

            // Pre-fill new end date to currentEnd + 30 days and block earlier dates
            if (isExtend) {
                ProbationRecord sel = table.getSelectionModel().getSelectedItem();
                if (sel != null && sel.getEndDate() != null) {
                    LocalDate minDate = sel.getEndDate().plusDays(1);
                    dpNewEndDate.setValue(sel.getEndDate().plusDays(30));
                    dpNewEndDate.setDayCellFactory(picker -> new DateCell() {
                        @Override
                        public void updateItem(LocalDate date, boolean empty) {
                            super.updateItem(date, empty);
                            setDisabled(empty || date.isBefore(minDate));
                        }
                    });
                }
            }
        });

        // Step 1: auto-load ACTIVE records on open
        loadActive();
    }

    // ── Load handlers ──────────────────────────────────────────────────────────

    /** Step 1: Load ACTIVE probation cases; Ext 1a: show recently closed if none active. */
    private void loadActive() {
        try {
            List<ProbationRecord> active = probationMonitorController.getActiveRecords();
            if (active.isEmpty()) {
                // Ext 1a: no active cases — show recently closed records as context
                List<ProbationRecord> closed = probationMonitorController.getRecentlyClosed();
                data.setAll(closed);
                if (!closed.isEmpty())
                    showInfo("No Active Cases",
                            "No active probation cases found.\n"
                            + "Showing " + closed.size() + " recently closed record(s) for reference.");
            } else {
                data.setAll(active);
            }
        } catch (SQLException e) {
            showError("Load Error", e.getMessage());
        }
    }

    @FXML
    private void handleLoad() {
        loadActive();
    }

    // ── Decision handler ───────────────────────────────────────────────────────

    @FXML
    private void handleDecision() {
        ProbationRecord sel = table.getSelectionModel().getSelectedItem();
        if (sel == null || !"ACTIVE".equals(sel.getStatus())) {
            showError("Selection Error", "Select an ACTIVE probation record to record a decision.");
            return;
        }

        String    decision     = cbDecision.getValue();
        LocalDate decisionDate = dpDecisionDate.getValue();
        String    notes        = taNotes.getText().trim();
        LocalDate newEndDate   = dpNewEndDate.getValue();
        String    reason       = tfReason != null ? tfReason.getText().trim() : "";

        if (decision == null) {
            showError("Input Error", "Please select a decision.");
            return;
        }
        if (decisionDate == null) {
            showError("Input Error", "Please select a decision date.");
            return;
        }

        // Ext 2a: warn if no interim evaluation exists — advisory, not a hard block
        try {
            if (!probationMonitorController.hasInterimEvaluation(sel.getEmployeeId())) {
                Alert warn = new Alert(Alert.AlertType.WARNING);
                warn.setTitle("Missing Evaluation");
                warn.setHeaderText("Interim Evaluation Not Found");
                warn.setContentText(
                        "No submitted performance evaluation found for "
                        + sel.getEmployeeName() + ".\n\n"
                        + "It is recommended to complete an interim evaluation "
                        + "before recording a probation decision.\n\n"
                        + "Do you want to proceed anyway?");
                warn.getButtonTypes().setAll(ButtonType.YES, ButtonType.CANCEL);
                if (warn.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.CANCEL)
                    return;
            }
        } catch (SQLException e) {
            showError("Check Error", e.getMessage());
            return;
        }

        try {
            probationMonitorController.recordDecision(
                    sel.getId(), decision, decisionDate,
                    notes.isEmpty() ? null : notes,
                    newEndDate,
                    reason.isEmpty() ? null : reason);

            String msg = switch (decision) {
                case "CONFIRMED"  -> sel.getEmployeeName()
                        + " confirmed as permanent employee.\nPayroll module notified.";
                case "EXTENDED"   -> "Probation extended to " + newEndDate + ".";
                case "TERMINATED" -> sel.getEmployeeName() + "'s employment terminated.";
                default           -> "Decision recorded: " + decision;
            };
            showInfo("Decision Recorded", msg);
            taNotes.clear();
            if (tfReason != null) tfReason.clear();
            dpNewEndDate.setValue(null);
            loadActive();
        } catch (IllegalArgumentException | IllegalStateException e) {
            showError("Decision Blocked", e.getMessage());
        } catch (SQLException e) {
            showError("Save Error", e.getMessage());
        }
    }

    // ── Dialogs ────────────────────────────────────────────────────────────────

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
