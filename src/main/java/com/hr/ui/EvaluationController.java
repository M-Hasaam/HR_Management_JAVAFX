package com.hr.ui;
// GRASP Pattern: Controller — UC-11 Initiate Evaluation Cycle & UC-12 Submit Performance Evaluation

import com.hr.controller.EvaluationCycleController;
import com.hr.controller.PerformanceEvalController;
import com.hr.dao.EvaluationCycleDAO;
import com.hr.dao.PerformanceEvaluationDAO;
import com.hr.model.Employee;
import com.hr.model.EvaluationCycle;
import com.hr.model.PerformanceEvaluation;
import com.hr.service.AuditLogService;
import com.hr.service.EmployeeService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EvaluationController {

    // ── UC-11: Cycle form ──────────────────────────────────────────────────────
    @FXML private TextField        tfCycleName;
    @FXML private DatePicker       dpStart;
    @FXML private DatePicker       dpEnd;
    @FXML private ComboBox<String> cbEvalType;
    @FXML private ComboBox<String> cbScope;
    @FXML private Button           btnActivate;
    @FXML private Button           btnSetGracePeriod;

    // ── UC-11: Cycle table ─────────────────────────────────────────────────────
    @FXML private TableView<EvaluationCycle>              tableCycles;
    @FXML private TableColumn<EvaluationCycle, Integer>   colCycleId;
    @FXML private TableColumn<EvaluationCycle, String>    colCycleName;
    @FXML private TableColumn<EvaluationCycle, LocalDate> colCycleStart;
    @FXML private TableColumn<EvaluationCycle, LocalDate> colCycleEnd;
    @FXML private TableColumn<EvaluationCycle, String>    colCycleType;
    @FXML private TableColumn<EvaluationCycle, String>    colCycleScope;
    @FXML private TableColumn<EvaluationCycle, String>    colCycleStatus;

    // ── UC-12: Evaluation form ─────────────────────────────────────────────────
    @FXML private ComboBox<Employee>        cbEmpEval;
    @FXML private ComboBox<EvaluationCycle> cbCycle;
    @FXML private TextField                 tfWorkQuality;
    @FXML private TextField                 tfTechnical;
    @FXML private TextField                 tfCommunication;
    @FXML private TextField                 tfTeamwork;
    @FXML private TextField                 tfLeadership;
    @FXML private Label                     lblAggregate;
    @FXML private TextArea                  taRemarks;

    // ── UC-12: Evaluation table ────────────────────────────────────────────────
    @FXML private TableView<PerformanceEvaluation>             tableEvals;
    @FXML private TableColumn<PerformanceEvaluation, Integer>  colEvalId;
    @FXML private TableColumn<PerformanceEvaluation, String>   colEvalEmployee;
    @FXML private TableColumn<PerformanceEvaluation, Integer>  colEvalCycle;
    @FXML private TableColumn<PerformanceEvaluation, Double>   colEvalScore;
    @FXML private TableColumn<PerformanceEvaluation, String>   colEvalRemarks;
    @FXML private TableColumn<PerformanceEvaluation, String>   colEvalStatus;

    private EvaluationCycleController  evalCycleController;
    private PerformanceEvalController  perfEvalController;
    private EvaluationCycleDAO         cycleDAO;
    private PerformanceEvaluationDAO   evalDAO;
    private EmployeeService            employeeService;

    private final ObservableList<EvaluationCycle>       cycleData = FXCollections.observableArrayList();
    private final ObservableList<PerformanceEvaluation> evalData  = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        try {
            evalCycleController = new EvaluationCycleController();
            perfEvalController  = new PerformanceEvalController();
            cycleDAO        = new EvaluationCycleDAO();
            evalDAO         = new PerformanceEvaluationDAO();
            employeeService = new EmployeeService();
            new AuditLogService(); // warm up
        } catch (SQLException e) {
            showError("Database Error", e.getMessage());
            return;
        }

        cbEvalType.setItems(FXCollections.observableArrayList(
                "ANNUAL", "MID_YEAR", "PROBATION", "PROJECT_BASED"));
        cbEvalType.setValue("ANNUAL");

        cbScope.setItems(FXCollections.observableArrayList(
                "ALL", "ENGINEERING", "HR", "FINANCE", "OPERATIONS", "MARKETING", "SALES"));
        cbScope.setValue("ALL");

        try {
            cbEmpEval.setItems(FXCollections.observableArrayList(
                    employeeService.getAllEmployees()));
        } catch (SQLException e) {
            showError("Load Error", "Could not load employees: " + e.getMessage());
        }

        // Cycle table
        colCycleId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCycleName.setCellValueFactory(new PropertyValueFactory<>("cycleName"));
        colCycleStart.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colCycleEnd.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        colCycleType.setCellValueFactory(new PropertyValueFactory<>("evaluationType"));
        colCycleScope.setCellValueFactory(new PropertyValueFactory<>("applicableScope"));
        colCycleStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        tableCycles.setItems(cycleData);

        // Evaluation table
        colEvalId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEvalEmployee.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        colEvalCycle.setCellValueFactory(new PropertyValueFactory<>("cycleId"));
        colEvalScore.setCellValueFactory(new PropertyValueFactory<>("aggregateScore"));
        colEvalRemarks.setCellValueFactory(new PropertyValueFactory<>("remarks"));
        colEvalStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        tableEvals.setItems(evalData);

        // UC-11: button states driven by table selection
        btnActivate.setDisable(true);
        btnSetGracePeriod.setDisable(true);
        tableCycles.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, sel) -> {
                    boolean isDraft  = sel != null && "DRAFT".equals(sel.getStatus());
                    boolean isActive = sel != null && "ACTIVE".equals(sel.getStatus());
                    btnActivate.setDisable(!isDraft);
                    btnSetGracePeriod.setDisable(!isActive);
                });

        lblAggregate.setText("—");
        loadCycles();
        loadActiveCyclesIntoCombo();
        loadEvaluations();
    }

    // ════════════════════════════════════════════════════════════════════════════
    // UC-11 handlers
    // ════════════════════════════════════════════════════════════════════════════

    @FXML
    private void handleSaveDraft() {
        String    name  = tfCycleName.getText().trim();
        LocalDate start = dpStart.getValue();
        LocalDate end   = dpEnd.getValue();
        String    type  = cbEvalType.getValue();
        String    scope = cbScope.getValue();

        if (name.isEmpty() || start == null || end == null || type == null || scope == null) {
            showError("Input Error", "All cycle fields are required.");
            return;
        }
        try {
            EvaluationCycle draft = evalCycleController.createDraftCycle(name, start, end, type, scope);
            showInfo("Draft Saved", "Cycle '" + draft.getCycleName()
                    + "' saved as DRAFT.\nSelect it in the table and click Activate when ready.");
            clearCycleForm();
            loadCycles();
        } catch (IllegalArgumentException e) {
            showError("Validation Error", e.getMessage());
        } catch (IllegalStateException e) {
            showError("Period Conflict", e.getMessage() + "\n\nModify the period to avoid overlap.");
        } catch (SQLException e) {
            showError("Save Error", e.getMessage());
        }
    }

    @FXML
    private void handleActivateCycle() {
        EvaluationCycle sel = tableCycles.getSelectionModel().getSelectedItem();
        if (sel == null || !"DRAFT".equals(sel.getStatus())) {
            showError("Selection Error", "Select a DRAFT cycle to activate.");
            return;
        }
        if (!showActivationChecklist(sel)) return;
        try {
            boolean remindersOk = evalCycleController.activateCycle(sel.getId());
            loadCycles();
            loadActiveCyclesIntoCombo();
            if (remindersOk) {
                showInfo("Cycle Activated", "'" + sel.getCycleName()
                        + "' is now ACTIVE.\nEvaluation tasks distributed to: "
                        + sel.getApplicableScope()
                        + "\nReminders scheduled 7 and 2 days before deadline.");
            } else {
                showInfo("Cycle Activated (Reminders Delayed)", "'" + sel.getCycleName()
                        + "' is now ACTIVE.\nNOTE: Reminder scheduling service was temporarily "
                        + "unavailable — reminders queued for retry.");
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            showError("Activation Blocked", e.getMessage());
        } catch (SQLException e) {
            showError("Activation Error", e.getMessage());
        }
    }

    /** Ext 4a confirmation checklist before activation. */
    private boolean showActivationChecklist(EvaluationCycle cycle) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Activate Evaluation Cycle");
        alert.setHeaderText("Review Configuration Before Activation");
        alert.setContentText(
                "Activation Checklist\n"
                + "─────────────────────────────\n"
                + "  Cycle Name  :  " + cycle.getCycleName() + "\n"
                + "  Period      :  " + cycle.getStartDate() + "  →  " + cycle.getEndDate() + "\n"
                + "  Type        :  " + cycle.getEvaluationType() + "\n"
                + "  Scope       :  " + cycle.getApplicableScope() + "\n"
                + "─────────────────────────────\n\n"
                + "Once activated this cycle is LOCKED for editing.\n"
                + "Confirm activation?"
        );
        return alert.showAndWait().filter(b -> b == ButtonType.OK).isPresent();
    }

    /** Ext 3b: Admin grants grace period for a selected ACTIVE cycle. */
    @FXML
    private void handleSetGracePeriod() {
        EvaluationCycle sel = tableCycles.getSelectionModel().getSelectedItem();
        if (sel == null || !"ACTIVE".equals(sel.getStatus())) {
            showError("Selection Error", "Select an ACTIVE cycle to extend its grace period.");
            return;
        }
        Dialog<LocalDate> dialog = new Dialog<>();
        dialog.setTitle("Set Grace Period");
        dialog.setHeaderText("Extend submission window for: " + sel.getCycleName());
        ButtonType okBtn = new ButtonType("Set Grace Period", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okBtn, ButtonType.CANCEL);

        DatePicker dp = new DatePicker(LocalDate.now().plusDays(7));
        VBox box = new VBox(6, new Label("Grace period end date:"), dp);
        box.setPadding(new Insets(12));
        dialog.getDialogPane().setContent(box);
        dialog.setResultConverter(btn -> btn == okBtn ? dp.getValue() : null);

        dialog.showAndWait().ifPresent(until -> {
            if (until == null) return;
            try {
                cycleDAO.setGracePeriod(sel.getId(), until);
                loadCycles();
                showInfo("Grace Period Set", "Evaluators for '" + sel.getCycleName()
                        + "' can now submit until " + until + ".");
            } catch (SQLException e) {
                showError("Save Error", e.getMessage());
            }
        });
    }

    // ════════════════════════════════════════════════════════════════════════════
    // UC-12 handlers
    // ════════════════════════════════════════════════════════════════════════════

    /** Updates the running weighted score label as the evaluator types (Step 2). */
    @FXML
    private void updateRunningScore() {
        try {
            double wq   = parseScore(tfWorkQuality.getText());
            double tech = parseScore(tfTechnical.getText());
            double comm = parseScore(tfCommunication.getText());
            double team = parseScore(tfTeamwork.getText());
            double lead = parseScore(tfLeadership.getText());
            double agg  = PerformanceEvalController.calculateWeightedScore(
                    wq, tech, comm, team, lead);
            lblAggregate.setText(String.format("%.1f / 100", agg));
        } catch (NumberFormatException e) {
            lblAggregate.setText("—");
        }
    }

    @FXML
    private void handleSubmitEvaluation() {
        Employee        emp    = cbEmpEval.getValue();
        EvaluationCycle cycle  = cbCycle.getValue();
        String wqText   = tfWorkQuality.getText().trim();
        String techText = tfTechnical.getText().trim();
        String commText = tfCommunication.getText().trim();
        String teamText = tfTeamwork.getText().trim();
        String leadText = tfLeadership.getText().trim();
        String remarks  = taRemarks.getText().trim();

        // Ext 2a: highlight all incomplete mandatory sections
        if (emp == null || cycle == null || wqText.isEmpty() || techText.isEmpty()
                || commText.isEmpty() || teamText.isEmpty() || leadText.isEmpty()
                || remarks.isEmpty()) {
            StringBuilder missing = new StringBuilder("The following sections are incomplete:\n");
            if (emp   == null)         missing.append("  • Employee\n");
            if (cycle == null)         missing.append("  • Evaluation Cycle\n");
            if (wqText.isEmpty())      missing.append("  • Work Quality score\n");
            if (techText.isEmpty())    missing.append("  • Technical Skills score\n");
            if (commText.isEmpty())    missing.append("  • Communication score\n");
            if (teamText.isEmpty())    missing.append("  • Teamwork score\n");
            if (leadText.isEmpty())    missing.append("  • Leadership score\n");
            if (remarks.isEmpty())     missing.append("  • Remarks / feedback\n");
            showError("Incomplete Form", missing.toString().trim());
            return;
        }

        double wq, tech, comm, team, lead;
        try {
            wq   = parseScore(wqText);
            tech = parseScore(techText);
            comm = parseScore(commText);
            team = parseScore(teamText);
            lead = parseScore(leadText);
        } catch (NumberFormatException e) {
            showError("Input Error", "All scores must be numbers between 0 and 100.");
            return;
        }

        double aggregate = PerformanceEvalController.calculateWeightedScore(
                wq, tech, comm, team, lead);

        // Step 3: evaluation summary review before final submit
        if (!showEvaluationSummary(emp, cycle, wq, tech, comm, team, lead, aggregate, remarks))
            return;

        try {
            boolean outlier = perfEvalController.submitEvaluation(
                    emp.getId(), 1, cycle, aggregate, remarks);

            // Ext 4a: soft outlier warning — does not block, just flags
            if (outlier) {
                Alert warn = new Alert(Alert.AlertType.WARNING);
                warn.setTitle("Calibration Flag");
                warn.setHeaderText("Statistical Outlier Detected");
                warn.setContentText(
                        "The score " + String.format("%.1f", aggregate)
                        + " deviates significantly from the cycle average.\n\n"
                        + "The evaluation has been saved. Consider reviewing your "
                        + "scores for calibration consistency before the cycle closes.");
                warn.showAndWait();
            }

            showInfo("Evaluation Submitted",
                    "Score: " + String.format("%.1f", aggregate) + " / 100 saved successfully.\n"
                    + emp.getFullName() + " has been notified.");
            clearEvalForm();
            loadEvaluations();
        } catch (IllegalStateException e) {
            showError("Submission Blocked", e.getMessage());
        } catch (IllegalArgumentException e) {
            showError("Validation Error", e.getMessage());
        } catch (SQLException e) {
            showError("Save Error", e.getMessage());
        }
    }

    /** Step 3: Shows evaluation summary and asks evaluator to confirm before submitting. */
    private boolean showEvaluationSummary(Employee emp, EvaluationCycle cycle,
                                           double wq, double tech, double comm,
                                           double team, double lead, double agg,
                                           String remarks) {
        String preview = remarks.length() > 80 ? remarks.substring(0, 80) + "…" : remarks;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Review Evaluation");
        alert.setHeaderText("Evaluation Summary — Review before submitting");
        alert.setContentText(
                "Employee  :  " + emp.getFullName() + "\n"
                + "Cycle     :  " + cycle.getCycleName() + "\n"
                + "─────────────────────────────────\n"
                + "Work Quality     (25%)  :  " + String.format("%.0f", wq) + "\n"
                + "Technical Skills (25%)  :  " + String.format("%.0f", tech) + "\n"
                + "Communication    (20%)  :  " + String.format("%.0f", comm) + "\n"
                + "Teamwork         (15%)  :  " + String.format("%.0f", team) + "\n"
                + "Leadership       (15%)  :  " + String.format("%.0f", lead) + "\n"
                + "─────────────────────────────────\n"
                + "Aggregate Score         :  " + String.format("%.1f", agg) + " / 100\n\n"
                + "Remarks: " + preview + "\n\n"
                + "Once submitted this evaluation cannot be modified.\nConfirm?"
        );
        return alert.showAndWait().filter(b -> b == ButtonType.OK).isPresent();
    }

    // ════════════════════════════════════════════════════════════════════════════
    // Data loading helpers
    // ════════════════════════════════════════════════════════════════════════════

    private void loadCycles() {
        try {
            cycleData.setAll(cycleDAO.getAll());
        } catch (SQLException e) {
            showError("Load Error", e.getMessage());
        }
    }

    private void loadActiveCyclesIntoCombo() {
        try {
            List<EvaluationCycle> active = cycleDAO.getAll().stream()
                    .filter(c -> "ACTIVE".equals(c.getStatus()))
                    .collect(Collectors.toList());
            cbCycle.setItems(FXCollections.observableArrayList(active));
        } catch (SQLException e) {
            showError("Load Error", e.getMessage());
        }
    }

    private void loadEvaluations() {
        try {
            evalData.setAll(evalDAO.getAll());
        } catch (SQLException e) {
            showError("Load Error", e.getMessage());
        }
    }

    private void clearCycleForm() {
        tfCycleName.clear();
        dpStart.setValue(null);
        dpEnd.setValue(null);
        cbEvalType.setValue("ANNUAL");
        cbScope.setValue("ALL");
    }

    private void clearEvalForm() {
        tfWorkQuality.clear();
        tfTechnical.clear();
        tfCommunication.clear();
        tfTeamwork.clear();
        tfLeadership.clear();
        taRemarks.clear();
        lblAggregate.setText("—");
    }

    private double parseScore(String text) {
        if (text == null || text.isBlank()) return 0;
        double v = Double.parseDouble(text.trim());
        if (v < 0 || v > 100) throw new NumberFormatException("Score out of range: " + v);
        return v;
    }

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
