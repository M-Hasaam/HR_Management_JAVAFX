package com.hr.ui;

import com.hr.controller.AssignmentController;
import com.hr.model.Department;
import com.hr.model.Employee;
import com.hr.model.EmployeeAssignment;
import com.hr.service.DepartmentService;
import com.hr.service.EmployeeService;
import com.hr.service.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AssignDepartmentController {

    // ─── Banner area ─────────────────────────────────────────────────────────────
    @FXML private VBox     bannerArea;
    @FXML private Label    lblBanner;
    @FXML private VBox     offboardingBlockBox;   // 3c
    @FXML private VBox     capacityWarningBox;    // 3a
    @FXML private TextArea taCapacityJustification;
    @FXML private VBox     backdateWarningBox;    // 3b
    @FXML private Label    lblBackdateWarning;
    @FXML private CheckBox chkBackdateAck;
    @FXML private VBox     clearanceMismatchBox;  // 4c
    @FXML private Label    lblClearanceMismatch;
    @FXML private VBox     dualReportingBox;      // 4b
    @FXML private Label    lblDualReporting;
    @FXML private HBox     adSyncFailBox;         // 4a

    // ─── Search section ───────────────────────────────────────────────────────────
    @FXML private TextField            tfSearch;
    @FXML private ComboBox<String>     cbStatusFilter;
    @FXML private TableView<Employee>  resultsTable;

    // ─── Assignment section ───────────────────────────────────────────────────────
    @FXML private VBox     assignSection;
    @FXML private Label    lblEditingName;
    @FXML private Label    lblCurrentDept;
    @FXML private Label    lblEmpStatus;

    @FXML private ComboBox<Department> cbDepartment;
    @FXML private Label    lblDeptInfo;
    @FXML private Label    errDept;

    @FXML private DatePicker dpEffectiveDate;
    @FXML private Label      errDate;
    @FXML private TextArea   taRemark;

    @FXML private TableView<EmployeeAssignment> historyTable;
    @FXML private Button btnAssign;

    // ─── State ───────────────────────────────────────────────────────────────────
    private Employee   selectedEmployee;
    private boolean    capacityAtMax     = false;
    private boolean    clearanceMismatch = false;
    private boolean    dualReporting     = false;

    private AssignmentController assignmentController;
    private EmployeeService      employeeService;
    private DepartmentService    departmentService;
    private List<Employee>       allEmployees;
    private List<Department>     allDepartments;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ─── Initialize ──────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        try {
            assignmentController = new AssignmentController();
            employeeService      = new EmployeeService();
            departmentService    = new DepartmentService();
        } catch (SQLException e) {
            showBanner("Database initialisation error: " + e.getMessage());
            return;
        }

        buildResultsTable();
        buildHistoryTable();
        loadDepartments();
        loadAllEmployees();

        dpEffectiveDate.setValue(LocalDate.now());

        cbStatusFilter.setItems(FXCollections.observableArrayList(
            "All Statuses", "ACTIVE", "INACTIVE"));
        cbStatusFilter.setValue("ACTIVE");

        // Update dept info card whenever the selection changes
        cbDepartment.valueProperty().addListener((obs, old, dept) -> {
            if (dept == null) return;
            updateDeptInfo(dept);
            if (selectedEmployee != null) checkDeptConstraints(dept);
        });

        // 3b: Trigger backdating warning whenever the date changes
        dpEffectiveDate.valueProperty().addListener((obs, old, date) -> {
            if (date != null) onDateChanged(date);
        });
    }

    // ─── Public entry point (called from EmployeeController card) ─────────────────
    public void setEmployee(Employee emp) {
        // Try to find the already-loaded row (so the table selection stays consistent)
        if (allEmployees != null) {
            allEmployees.stream()
                .filter(e -> e.getId() == emp.getId())
                .findFirst()
                .ifPresent(found -> {
                    resultsTable.getSelectionModel().select(found);
                    selectEmployee(found);
                });
        }
        // Fallback: use the passed-in object directly
        if (selectedEmployee == null) selectEmployee(emp);
    }

    // ─── Employee selection ───────────────────────────────────────────────────────
    private void selectEmployee(Employee emp) {
        selectedEmployee = emp;
        lblEditingName.setText(emp.getFullName());
        lblCurrentDept.setText(emp.getDepartmentName() != null ? emp.getDepartmentName() : "None");
        lblEmpStatus.setText(emp.getStatus() != null ? emp.getStatus() : "Unknown");

        showWidget(assignSection, true);
        clearAllBanners();
        btnAssign.setDisable(false);

        // 3c: Check active offboarding — hard block
        try {
            if (assignmentController.isActiveOffboarding(emp.getId())) {
                showWidget(offboardingBlockBox, true);
                showWidget(bannerArea, true);
                btnAssign.setDisable(true);
                loadAssignmentHistory(emp.getId());
                return;
            }
        } catch (SQLException e) {
            showBanner("Could not verify offboarding status: " + e.getMessage());
        }

        // 4b: Dual-reporting warning (non-blocking, confirmed in handleAssign)
        try {
            dualReporting = assignmentController.isDualReportingConflict(emp);
            if (dualReporting) {
                lblDualReporting.setText(
                    emp.getFullName() + " is currently the named manager of another department. " +
                    "Proceeding will create a dual-reporting structure. You must resolve the " +
                    "primary reporting line before or immediately after confirming this assignment.");
                showWidget(dualReportingBox, true);
                showWidget(bannerArea, true);
            }
        } catch (SQLException e) {
            System.err.println("[UC-03] Dual-reporting check error: " + e.getMessage());
        }

        // If a dept is already picked, re-run its checks against the new employee
        Department dept = cbDepartment.getValue();
        if (dept != null) checkDeptConstraints(dept);

        loadAssignmentHistory(emp.getId());
    }

    // ─── Department constraints ───────────────────────────────────────────────────
    private void checkDeptConstraints(Department dept) {
        showWidget(capacityWarningBox, false);
        showWidget(clearanceMismatchBox, false);
        capacityAtMax     = false;
        clearanceMismatch = false;

        // 3a: Capacity
        try {
            capacityAtMax = assignmentController.isDepartmentAtCapacity(dept.getId());
            if (capacityAtMax) {
                showWidget(capacityWarningBox, true);
                showWidget(bannerArea, true);
            }
        } catch (SQLException e) {
            showBanner("Capacity check error: " + e.getMessage());
        }

        // 4c: Security clearance — hard block
        try {
            clearanceMismatch = assignmentController.hasSecurityClearanceMismatch(
                selectedEmployee, dept);
            if (clearanceMismatch) {
                lblClearanceMismatch.setText(
                    "Department \"" + dept.getName() + "\" requires a security clearance that " +
                    "\"" + selectedEmployee.getFullName() + "\" does not currently hold. " +
                    "The assignment is blocked until compliance is verified.");
                showWidget(clearanceMismatchBox, true);
                showWidget(bannerArea, true);
                btnAssign.setDisable(true);
            }
        } catch (SQLException e) {
            System.err.println("[UC-03] Clearance check error: " + e.getMessage());
        }

        if (!clearanceMismatch) btnAssign.setDisable(false);
    }

    private void updateDeptInfo(Department dept) {
        String managerStr = dept.getManagerId() > 0
            ? "Manager ID " + dept.getManagerId() : "No manager assigned";
        lblDeptInfo.setText(String.format(
            "Current headcount: %d / %d   |   %s",
            dept.getCurrentHeadcount(), dept.getMaxHeadcount(), managerStr));
    }

    // ─── Date changed (3b) ───────────────────────────────────────────────────────
    private void onDateChanged(LocalDate date) {
        if (date.isBefore(LocalDate.now())) {
            lblBackdateWarning.setText(
                "Effective date " + date.format(FMT) + " is in the past. " +
                "A backdated assignment may affect historical headcount, payroll, and reporting records.");
            showWidget(backdateWarningBox, true);
            showWidget(bannerArea, true);
        } else {
            showWidget(backdateWarningBox, false);
            refreshBannerVisibility();
        }
    }

    // ─── Search handlers ──────────────────────────────────────────────────────────
    @FXML private void handleSearch() {
        String query  = tfSearch.getText().trim().toLowerCase();
        String status = cbStatusFilter.getValue();
        if (allEmployees == null) return;
        List<Employee> filtered = allEmployees.stream()
            .filter(e ->
                (query.isBlank()
                    || e.getFullName().toLowerCase().contains(query)
                    || String.valueOf(e.getId()).contains(query)
                    || (e.getEmail() != null && e.getEmail().toLowerCase().contains(query)))
                && ("All Statuses".equals(status)
                    || status.equalsIgnoreCase(e.getStatus())))
            .toList();
        resultsTable.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML private void handleShowAll() {
        tfSearch.clear();
        cbStatusFilter.setValue("All Statuses");
        if (allEmployees != null)
            resultsTable.setItems(FXCollections.observableArrayList(allEmployees));
    }

    // ─── Cancel / clear ───────────────────────────────────────────────────────────
    @FXML private void handleCancel() {
        selectedEmployee  = null;
        capacityAtMax     = false;
        clearanceMismatch = false;
        dualReporting     = false;

        showWidget(assignSection, false);
        clearAllBanners();
        resultsTable.getSelectionModel().clearSelection();
        cbDepartment.setValue(null);
        dpEffectiveDate.setValue(LocalDate.now());
        taRemark.clear();
        taCapacityJustification.clear();
        chkBackdateAck.setSelected(false);
        lblDeptInfo.setText("Select a department to see headcount and manager details.");
        btnAssign.setDisable(false);
        historyTable.setItems(FXCollections.emptyObservableList());
    }

    // ─── Main assign action ───────────────────────────────────────────────────────
    @FXML private void handleAssign() {
        if (!validateForm()) return;

        Department targetDept  = cbDepartment.getValue();
        LocalDate  effDate     = dpEffectiveDate.getValue();
        boolean    isBackdated = effDate.isBefore(LocalDate.now());

        // 4c: Hard block
        if (clearanceMismatch) {
            showBanner("Assignment blocked — security clearance mismatch. " +
                "Resolve clearance compliance before proceeding.");
            return;
        }

        // 3b: Backdating requires acknowledgment checkbox
        if (isBackdated && !chkBackdateAck.isSelected()) {
            showBanner("You must check the backdating acknowledgment before confirming.");
            chkBackdateAck.requestFocus();
            return;
        }

        // 3a: Capacity override requires a justification text
        String overrideJust = "";
        if (capacityAtMax) {
            overrideJust = taCapacityJustification.getText().trim();
            if (overrideJust.isBlank()) {
                showBanner("Please enter the override justification for the capacity exception.");
                taCapacityJustification.requestFocus();
                return;
            }
        }

        // 4b: Dual-reporting — warn and require explicit confirmation
        if (dualReporting) {
            Alert warn = new Alert(Alert.AlertType.CONFIRMATION,
                selectedEmployee.getFullName() + " is currently a department manager.\n\n" +
                "Completing this transfer will create a dual-reporting structure. " +
                "You must manually update the reporting line after confirming.\n\nProceed?",
                ButtonType.YES, ButtonType.NO);
            warn.setTitle("Dual Reporting Conflict");
            warn.setHeaderText("Resolve Reporting Line");
            if (warn.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) return;
        }

        // Final confirmation dialog
        String summary = String.format(
            "Assign \"%s\" to \"%s\"?\n\nEffective: %s%s%s",
            selectedEmployee.getFullName(), targetDept.getName(),
            effDate.format(FMT),
            isBackdated ? "\n⚠ Backdated — historical records may be affected." : "",
            capacityAtMax ? "\n⚠ Capacity override applied." : "");

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            summary, ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Department Assignment");
        confirm.setHeaderText("Please review and confirm");
        if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) return;

        // Execute
        try {
            int userId = getUserId();

            AssignmentController.AssignmentResult result =
                assignmentController.assignWithHistory(
                    selectedEmployee, targetDept, effDate,
                    taRemark.getText().trim(),
                    capacityAtMax, overrideJust,
                    isBackdated, userId);

            // 4a: AD sync failure banner (assignment already saved)
            if (!result.adSyncSucceeded()) {
                showWidget(adSyncFailBox, true);
                showWidget(bannerArea, true);
            }

            // Refresh dept dropdown so headcount is current
            Department savedDept = targetDept;
            loadDepartments();

            showSuccessDialog(selectedEmployee, savedDept, effDate, result);
            handleCancel();

        } catch (SQLException e) {
            showBanner("Assignment failed — " + e.getMessage());
        }
    }

    // ─── Table builders ───────────────────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private void buildResultsTable() {
        TableColumn<Employee, String> colId   = new TableColumn<>("ID");
        TableColumn<Employee, String> colName = new TableColumn<>("Name");
        TableColumn<Employee, String> colDept = new TableColumn<>("Current Dept");
        TableColumn<Employee, String> colPos  = new TableColumn<>("Position");
        TableColumn<Employee, String> colStat = new TableColumn<>("Status");
        TableColumn<Employee, Void>   colSel  = new TableColumn<>("Action");

        colId.setCellValueFactory(c ->
            new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        colId.setMaxWidth(60);
        colName.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getFullName()));
        colName.setMinWidth(150);
        colDept.setCellValueFactory(c ->
            new SimpleStringProperty(nvl(c.getValue().getDepartmentName())));
        colPos.setCellValueFactory(c ->
            new SimpleStringProperty(nvl(c.getValue().getPosition())));
        colStat.setCellValueFactory(c ->
            new SimpleStringProperty(nvl(c.getValue().getStatus())));

        colSel.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Select");
            {
                btn.getStyleClass().add("btn-primary");
                btn.setStyle("-fx-font-size:11px;");
                btn.setOnAction(e ->
                    selectEmployee(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : btn);
            }
        });

        resultsTable.getColumns().setAll(colId, colName, colDept, colPos, colStat, colSel);
        resultsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        resultsTable.setPlaceholder(new Label("No employees found."));
    }

    @SuppressWarnings("unchecked")
    private void buildHistoryTable() {
        TableColumn<EmployeeAssignment, String> colDate = new TableColumn<>("Effective");
        TableColumn<EmployeeAssignment, String> colFrom = new TableColumn<>("From Dept");
        TableColumn<EmployeeAssignment, String> colTo   = new TableColumn<>("To Dept");
        TableColumn<EmployeeAssignment, String> colRmk  = new TableColumn<>("Remark");
        TableColumn<EmployeeAssignment, String> colBd   = new TableColumn<>("Backdated");

        colDate.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getEffectiveDate() != null
                ? c.getValue().getEffectiveDate().format(FMT) : "—"));
        colFrom.setCellValueFactory(c -> new SimpleStringProperty(
            nvl(c.getValue().getFromDeptName())));
        colTo.setCellValueFactory(c -> new SimpleStringProperty(
            nvl(c.getValue().getToDeptName())));
        colRmk.setCellValueFactory(c -> new SimpleStringProperty(
            nvl(c.getValue().getRemark())));
        colBd.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().isBackdated() ? "Yes" : "No"));

        colDate.setMinWidth(100); colFrom.setMinWidth(110); colTo.setMinWidth(110);

        historyTable.getColumns().setAll(colDate, colFrom, colTo, colRmk, colBd);
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        historyTable.setPlaceholder(new Label("No prior assignments recorded."));
    }

    // ─── Data loaders ─────────────────────────────────────────────────────────────
    private void loadAllEmployees() {
        try {
            allEmployees = employeeService.getAllEmployees();
            // Default to ACTIVE only in the search table
            resultsTable.setItems(FXCollections.observableArrayList(
                allEmployees.stream()
                    .filter(e -> "ACTIVE".equalsIgnoreCase(e.getStatus()))
                    .toList()));
        } catch (SQLException e) {
            showBanner("Could not load employees: " + e.getMessage());
        }
    }

    private void loadDepartments() {
        try {
            allDepartments = departmentService.getAllDepartments();
            Department current = cbDepartment.getValue();
            cbDepartment.setItems(FXCollections.observableArrayList(allDepartments));
            if (current != null) {
                allDepartments.stream()
                    .filter(d -> d.getId() == current.getId())
                    .findFirst()
                    .ifPresent(cbDepartment::setValue);
            }
        } catch (SQLException e) {
            showBanner("Could not load departments: " + e.getMessage());
        }
    }

    private void loadAssignmentHistory(int empId) {
        try {
            List<EmployeeAssignment> history = assignmentController.getAssignmentHistory(empId);
            historyTable.setItems(FXCollections.observableArrayList(history));
        } catch (SQLException e) {
            System.err.println("[UC-03] History load failed: " + e.getMessage());
            historyTable.setPlaceholder(new Label("Could not load history: " + e.getMessage()));
        }
    }

    // ─── Validation ───────────────────────────────────────────────────────────────
    private boolean validateForm() {
        boolean ok = true;
        showWidget(errDept, false);
        showWidget(errDate, false);

        if (selectedEmployee == null) {
            showBanner("Please select an employee from the table first.");
            ok = false;
        }

        Department dept = cbDepartment.getValue();
        if (dept == null) {
            errDept.setText("Please select a target department.");
            showWidget(errDept, true);
            ok = false;
        } else if (selectedEmployee != null
                   && dept.getId() == selectedEmployee.getDepartmentId()) {
            errDept.setText("Employee is already assigned to this department.");
            showWidget(errDept, true);
            ok = false;
        }

        if (dpEffectiveDate.getValue() == null) {
            errDate.setText("Effective date is required.");
            showWidget(errDate, true);
            ok = false;
        }

        return ok;
    }

    // ─── Success dialog ───────────────────────────────────────────────────────────
    private void showSuccessDialog(Employee emp, Department dept, LocalDate date,
                                   AssignmentController.AssignmentResult result) {
        Alert ok = new Alert(Alert.AlertType.INFORMATION);
        ok.setTitle("Assignment Confirmed");
        ok.setHeaderText("Department assignment saved successfully");
        ok.setContentText(String.format(
            "Employee:    %s%nDepartment:  %s%nEffective:   %s%nHistory ID:  %d%n%s",
            emp.getFullName(), dept.getName(), date.format(FMT), result.historyId(),
            result.adSyncSucceeded()
                ? "\nAccess rights synchronised successfully."
                : "\n⚠ Access-rights sync is queued — IT has been alerted (4a)."));
        ok.showAndWait();
    }

    // ─── UI helpers ───────────────────────────────────────────────────────────────
    private void showWidget(Region r, boolean show) {
        r.setVisible(show);
        r.setManaged(show);
    }

    private void showBanner(String msg) {
        lblBanner.setText(msg);
        showWidget(lblBanner, true);
        showWidget(bannerArea, true);
    }

    private void clearAllBanners() {
        showWidget(bannerArea,          false);
        showWidget(lblBanner,           false);
        showWidget(offboardingBlockBox, false);
        showWidget(capacityWarningBox,  false);
        showWidget(backdateWarningBox,  false);
        showWidget(clearanceMismatchBox, false);
        showWidget(dualReportingBox,    false);
        showWidget(adSyncFailBox,       false);
        showWidget(errDept, false);
        showWidget(errDate, false);
    }

    private void refreshBannerVisibility() {
        boolean anyVisible =
            offboardingBlockBox.isVisible() || capacityWarningBox.isVisible()
            || backdateWarningBox.isVisible() || clearanceMismatchBox.isVisible()
            || dualReportingBox.isVisible()   || adSyncFailBox.isVisible()
            || lblBanner.isVisible();
        showWidget(bannerArea, anyVisible);
    }

    private int getUserId() {
        try {
            var user = SessionManager.getInstance().getCurrentUser();
            if (user != null) return user.getId();
        } catch (Exception ignored) {}
        return 0;
    }

    private String nvl(String s) { return (s != null && !s.isBlank()) ? s : "—"; }
}
