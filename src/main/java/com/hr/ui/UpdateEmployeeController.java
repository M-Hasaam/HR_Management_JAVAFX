package com.hr.ui;

import com.hr.controller.UpdateController;
import com.hr.model.Department;
import com.hr.model.Employee;
import com.hr.service.DepartmentService;
import com.hr.service.EmployeeService;
import com.hr.service.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class UpdateEmployeeController {

    // ── Banner ───────────────────────────────────────────────────────────────
    @FXML private VBox   bannerArea;
    @FXML private Label  lblBanner;
    @FXML private VBox   conflictBox;
    @FXML private Label  lblConflict;
    @FXML private HBox   retryBox;
    @FXML private Button btnRetry;

    // ── Search ───────────────────────────────────────────────────────────────
    @FXML private TextField               tfSearch;
    @FXML private ComboBox<Department>    cbDeptFilter;
    @FXML private TableView<Employee>     resultsTable;

    // ── Edit section wrapper ──────────────────────────────────────────────────
    @FXML private VBox  editSection;
    @FXML private Label lblEditingName;

    // ── Personal fields ──────────────────────────────────────────────────────
    @FXML private TextField        tfFirstName;
    @FXML private TextField        tfLastName;
    @FXML private TextField        tfNationalId;
    @FXML private DatePicker       dpDob;
    @FXML private ComboBox<String> cbGender;
    @FXML private TextField        tfPhone;
    @FXML private TextField        tfEmail;
    @FXML private TextArea         taAddress;
    @FXML private Button           btnRequestNidChange;
    @FXML private Label            lblNidStatus;

    // ── Org fields ───────────────────────────────────────────────────────────
    @FXML private ComboBox<Department> cbDepartment;
    @FXML private TextField            tfDesignation;
    @FXML private ComboBox<String>     cbEmploymentType;
    @FXML private ComboBox<String>     cbStatus;
    @FXML private TextField            tfSalary;
    @FXML private Label                lblSalaryRestricted;
    @FXML private Label                lblEmpTypeRestricted;

    // ── Reason ───────────────────────────────────────────────────────────────
    @FXML private TextArea tfReason;
    @FXML private Label    errReason;

    // ── Error labels ─────────────────────────────────────────────────────────
    @FXML private Label errFirstName, errLastName, errPhone, errEmail;
    @FXML private Label errDesignation, errSalary;
    @FXML private Button btnSave;

    // ── State ────────────────────────────────────────────────────────────────
    private UpdateController  updateController;
    private EmployeeService   employeeService;
    private DepartmentService departmentService;

    private Employee loadedEmployee;    // snapshot when loaded — used for diff + conflict check
    private String   pendingNationalId; // 3c: pending NID change awaiting Admin approval
    private List<Employee> allEmployees;

    // ── Initialize ───────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        try {
            updateController  = new UpdateController();
            employeeService   = new EmployeeService();
            departmentService = new DepartmentService();
        } catch (SQLException e) {
            showBanner("Database error: " + e.getMessage(), true);
            return;
        }

        cbGender.setItems(FXCollections.observableArrayList("Male", "Female", "Other"));
        cbEmploymentType.setItems(FXCollections.observableArrayList(
            "Full-Time", "Part-Time", "Contract", "Intern"));
        cbStatus.setItems(FXCollections.observableArrayList("ACTIVE", "ON_LEAVE", "INACTIVE"));

        try {
            List<Department> depts = departmentService.getAllDepartments();
            cbDepartment.setItems(FXCollections.observableArrayList(depts));
            cbDeptFilter.setItems(FXCollections.observableArrayList(depts));
        } catch (SQLException e) {
            showBanner("Failed to load departments: " + e.getMessage(), true);
        }

        buildResultsTable();
        handleShowAll(); // pre-load all employees

        // Real-time format validation (3a)
        tfPhone.focusedProperty().addListener((o, had, has) -> {
            if (!has) validatePhoneFormat();
        });
        tfEmail.focusedProperty().addListener((o, had, has) -> {
            if (!has) validateEmailFormat();
        });
        tfSalary.focusedProperty().addListener((o, had, has) -> {
            if (!has) validateSalaryFormat();
        });

        // 3b: salary focus attempt logging for HR
        tfSalary.focusedProperty().addListener((o, had, has) -> {
            if (has && !SessionManager.getInstance().isAdmin() && loadedEmployee != null) {
                updateController.logRestrictedFieldAccess(
                    loadedEmployee.getId(), "basic_salary",
                    SessionManager.getInstance().getCurrentUser().getId());
            }
        });
    }

    // ── Build search results TableView ────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private void buildResultsTable() {
        TableColumn<Employee, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(50);
        colId.setStyle("-fx-alignment: CENTER;");

        TableColumn<Employee, String> colName = new TableColumn<>("Full Name");
        colName.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
            d.getValue().getFullName()));
        colName.setPrefWidth(160);

        TableColumn<Employee, String> colDept = new TableColumn<>("Department");
        colDept.setCellValueFactory(new PropertyValueFactory<>("departmentName"));
        colDept.setPrefWidth(120);

        TableColumn<Employee, String> colPos = new TableColumn<>("Position");
        colPos.setCellValueFactory(new PropertyValueFactory<>("position"));
        colPos.setPrefWidth(130);

        TableColumn<Employee, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setPrefWidth(90);
        colStatus.setStyle("-fx-alignment: CENTER;");

        TableColumn<Employee, Void> colAction = new TableColumn<>("");
        colAction.setPrefWidth(110);
        colAction.setCellFactory(buildEditButtonFactory());

        resultsTable.getColumns().setAll(colId, colName, colDept, colPos, colStatus, colAction);
        resultsTable.setPlaceholder(new Label("No employees found."));
        resultsTable.getStyleClass().add("table-view");
    }

    private Callback<TableColumn<Employee, Void>, TableCell<Employee, Void>> buildEditButtonFactory() {
        return col -> new TableCell<>() {
            private final Button btn = new Button("Edit Record");
            {
                btn.getStyleClass().add("btn-primary");
                btn.setStyle("-fx-font-size:11px; -fx-padding: 5 12 5 12;");
                btn.setOnAction(e -> {
                    Employee emp = getTableView().getItems().get(getIndex());
                    selectEmployee(emp);
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : btn);
            }
        };
    }

    // ── Search handlers ───────────────────────────────────────────────────────
    @FXML
    private void handleShowAll() {
        try {
            allEmployees = employeeService.getAllEmployees();
            resultsTable.setItems(FXCollections.observableArrayList(allEmployees));
        } catch (SQLException e) {
            showBanner("Failed to load employees: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleSearch() {
        if (allEmployees == null) return;
        String query = tfSearch.getText().trim().toLowerCase();
        Department deptFilter = cbDeptFilter.getValue();

        List<Employee> filtered = allEmployees.stream()
            .filter(e -> {
                boolean matchQuery = query.isBlank()
                    || e.getFullName().toLowerCase().contains(query)
                    || String.valueOf(e.getId()).equals(query)
                    || (e.getEmail() != null && e.getEmail().toLowerCase().contains(query));
                boolean matchDept = deptFilter == null || e.getDepartmentId() == deptFilter.getId();
                return matchQuery && matchDept;
            })
            .collect(Collectors.toList());

        resultsTable.setItems(FXCollections.observableArrayList(filtered));
        if (filtered.isEmpty())
            showBanner("No employees match the search criteria.", false);
        else
            hideBanner();
    }

    // ── Select employee → populate edit form ──────────────────────────────────
    public void setEmployee(Employee emp) {
        // Called externally (from EmployeeController Edit button)
        selectEmployee(emp);
    }

    private void selectEmployee(Employee emp) {
        loadedEmployee     = emp;
        pendingNationalId  = null;

        lblEditingName.setText(emp.getFullName() + "  (ID: " + emp.getId() + ")");

        // Populate personal fields
        tfFirstName.setText(nvl(emp.getFirstName()));
        tfLastName.setText(nvl(emp.getLastName()));
        tfNationalId.setText(nvl(emp.getNationalId()));
        dpDob.setValue(emp.getDateOfBirth());
        cbGender.setValue(emp.getGender());
        tfPhone.setText(nvl(emp.getPhone()));
        tfEmail.setText(nvl(emp.getEmail()));
        taAddress.setText(nvl(emp.getAddress()));

        // Populate org fields
        cbDepartment.getItems().stream()
            .filter(d -> d.getId() == emp.getDepartmentId()).findFirst()
            .ifPresent(cbDepartment::setValue);
        tfDesignation.setText(nvl(emp.getPosition()));
        cbEmploymentType.setValue(emp.getEmploymentType());
        cbStatus.setValue(nvl(emp.getStatus(), "ACTIVE"));
        tfSalary.setText(emp.getBasicSalary() != null ? emp.getBasicSalary().toPlainString() : "");

        // Apply role-based restrictions (3b + 3c)
        applyRoleRestrictions();

        // Show edit section with animation
        setNodeVisible(editSection, true);
        clearAllErrors();
        hideBanner();

        // Scroll to edit section — done by JavaFX layout naturally since it's in ScrollPane
    }

    // ── Role restrictions ─────────────────────────────────────────────────────
    private void applyRoleRestrictions() {
        boolean isAdmin = SessionManager.getInstance().isAdmin();

        // 3b: Basic Salary — Admin only
        tfSalary.setEditable(isAdmin);
        tfSalary.setStyle(isAdmin ? "" : "-fx-opacity: 0.65;");
        setNodeVisible(lblSalaryRestricted, !isAdmin);

        // 3b: Employment Type — Admin only
        cbEmploymentType.setDisable(!isAdmin);
        setNodeVisible(lblEmpTypeRestricted, !isAdmin);

        // 3c: National ID always read-only in field; change via request button
        tfNationalId.setEditable(false);
        tfNationalId.setStyle("-fx-opacity: 0.7;");
        // Admin sees "Change Directly", HR sees "Request Change"
        btnRequestNidChange.setText(isAdmin ? "Change" : "Request Change");
    }

    // ── 3c: National ID change workflow ──────────────────────────────────────
    @FXML
    private void handleRequestNidChange() {
        boolean isAdmin = SessionManager.getInstance().isAdmin();

        if (isAdmin) {
            // Admin: direct edit with confirmation step
            TextInputDialog dlg = new TextInputDialog(tfNationalId.getText());
            dlg.setTitle("Edit National ID");
            dlg.setHeaderText("Sensitive field — this change will be logged.");
            dlg.setContentText("New National ID:");
            dlg.showAndWait().ifPresent(newId -> {
                if (!newId.isBlank()) {
                    tfNationalId.setText(newId);
                    setNodeVisible(lblNidStatus, true);
                    lblNidStatus.setText("National ID updated. Will be saved with your other changes.");
                }
            });
        } else {
            // HR: pending-approval workflow
            TextInputDialog dlg = new TextInputDialog(tfNationalId.getText());
            dlg.setTitle("Request National ID Change");
            dlg.setHeaderText("National ID changes require Admin secondary approval.");
            dlg.setContentText("Proposed new National ID:");
            dlg.showAndWait().ifPresent(newId -> {
                if (!newId.isBlank()) {
                    pendingNationalId = newId;
                    setNodeVisible(lblNidStatus, true);
                    lblNidStatus.setText(
                        "Change request submitted (pending Admin approval): " + newId
                        + " — other fields can still be saved normally.");
                    showBanner("National ID change is pending Admin approval. "
                        + "Your other updates will be saved immediately.", false);
                }
            });
        }
    }

    // ── Save handler ──────────────────────────────────────────────────────────
    @FXML
    private void handleSave() {
        clearAllErrors();
        hideRetryBox();

        if (!validateForm()) return;

        try {
            // 4a: Concurrent edit detection
            Employee current = updateController.getEmployee(loadedEmployee.getId());
            if (current != null && updateController.hasBeenModified(loadedEmployee, current)) {
                showConflictWarning(current);
                return;
            }

            // Build updated employee from form
            Employee updated = buildUpdatedEmployee();

            var currentUser = SessionManager.getInstance().getCurrentUser();
            int updaterId = currentUser != null ? currentUser.getId() : 0;
            String reason = tfReason.getText().trim();

            // Persist with full audit trail
            boolean payrollOk = updateController.updateWithAuditTrail(
                loadedEmployee, updated, reason, updaterId);

            // 4c: payroll notification failure
            if (!payrollOk) {
                showBanner("Record saved. Payroll notification failed and has been queued for retry. "
                    + "Please verify payroll sync manually.", false);
            } else {
                hideBanner();
            }

            // Refresh snapshot
            loadedEmployee = updateController.getEmployee(updated.getId());
            lblEditingName.setText(loadedEmployee.getFullName() + "  (ID: " + loadedEmployee.getId() + ")");

            showSuccess("Employee record updated successfully.");
            handleShowAll(); // refresh table

        } catch (SQLException ex) {
            // 4b: DB write failure — show retry strip, preserve form data
            showRetryBox("Database error: " + ex.getMessage());
            ex.printStackTrace();
        } catch (IllegalStateException ex) {
            String msg = ex.getMessage() != null ? ex.getMessage() : "Department is at maximum capacity.";
            showBanner(msg, true);
            Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
            alert.setTitle("Cannot Save Changes");
            alert.setHeaderText("Department Capacity Exceeded");
            alert.showAndWait();
        } catch (IllegalArgumentException ex) {
            showBanner("Validation error: " + ex.getMessage(), true);
        } catch (Exception ex) {
            showBanner("Unexpected error: " + ex.getClass().getSimpleName() + ": " + ex.getMessage(), true);
            ex.printStackTrace();
        }
    }

    // ── 4a: Conflict handling ─────────────────────────────────────────────────
    private Employee conflictingVersion; // holds DB version during conflict

    private void showConflictWarning(Employee dbVersion) {
        conflictingVersion = dbVersion;
        lblConflict.setText(
            "The record was modified by another user since you opened it.\n"
            + "DB version:  " + dbVersion.getFullName()
            + ", Dept: " + nvl(dbVersion.getDepartmentName())
            + ", Position: " + nvl(dbVersion.getPosition()) + "\n"
            + "Your version: " + tfFirstName.getText() + " " + tfLastName.getText()
            + ", Position: " + tfDesignation.getText());
        setNodeVisible(conflictBox, true);
        setNodeVisible(bannerArea, true);
    }

    @FXML
    private void handleKeepMine() {
        // Force save with the user's version — update the snapshot so conflict check passes
        loadedEmployee = conflictingVersion;
        setNodeVisible(conflictBox, false);
        refreshBannerVisibility();
        handleSave();
    }

    @FXML
    private void handleReloadData() {
        // Discard form and reload current DB state
        setNodeVisible(conflictBox, false);
        refreshBannerVisibility();
        selectEmployee(conflictingVersion);
        showBanner("Form reloaded with the current database values.", false);
    }

    // ── 4b: Retry ─────────────────────────────────────────────────────────────
    private void showRetryBox(String msg) {
        setNodeVisible(retryBox, true);
        setNodeVisible(bannerArea, true);
        showBanner(msg, true);
    }

    private void hideRetryBox() {
        setNodeVisible(retryBox, false);
        refreshBannerVisibility();
    }

    // ── Cancel ────────────────────────────────────────────────────────────────
    @FXML
    private void handleCancel() {
        setNodeVisible(editSection, false);
        loadedEmployee    = null;
        pendingNationalId = null;
        clearAllErrors();
        hideBanner();
        setNodeVisible(retryBox, false);
        setNodeVisible(conflictBox, false);
    }

    // ── Form validation (3a) ──────────────────────────────────────────────────
    private boolean validateForm() {
        boolean ok = true;
        if (tfFirstName.getText().isBlank())   { showFieldError(errFirstName,   "First name is required.");       ok = false; }
        if (tfLastName.getText().isBlank())     { showFieldError(errLastName,    "Last name is required.");        ok = false; }
        if (tfPhone.getText().isBlank())        { showFieldError(errPhone,       "Contact number is required.");   ok = false; }
        if (!validatePhoneFormat())             { ok = false; }
        if (tfEmail.getText().isBlank())        { showFieldError(errEmail,       "Email is required.");            ok = false; }
        else if (!validateEmailFormat())        { ok = false; }
        if (tfDesignation.getText().isBlank())  { showFieldError(errDesignation, "Designation is required.");      ok = false; }
        if (!tfSalary.getText().isBlank() && !validateSalaryFormat()) { ok = false; }
        if (tfReason.getText().isBlank())       { showFieldError(errReason,      "A reason is required for audit compliance."); ok = false; }

        if (!ok) showBanner("Please correct the highlighted fields.", true);
        return ok;
    }

    private boolean validatePhoneFormat() {
        String p = tfPhone.getText().trim();
        if (!p.isBlank() && !p.matches("[0-9\\-+() ]{7,20}")) {
            showFieldError(errPhone, "Invalid phone format (e.g. 0300-1234567).");
            return false;
        }
        clearFieldError(errPhone);
        return true;
    }

    private boolean validateEmailFormat() {
        String e = tfEmail.getText().trim();
        if (!e.isBlank() && (!e.contains("@") || !e.contains("."))) {
            showFieldError(errEmail, "Enter a valid email address.");
            return false;
        }
        clearFieldError(errEmail);
        return true;
    }

    private boolean validateSalaryFormat() {
        try {
            new BigDecimal(tfSalary.getText().trim());
            clearFieldError(errSalary);
            return true;
        } catch (NumberFormatException e) {
            showFieldError(errSalary, "Salary must be a valid number.");
            return false;
        }
    }

    // ── Build updated Employee from form ──────────────────────────────────────
    private Employee buildUpdatedEmployee() {
        Employee e = new Employee();
        e.setId(loadedEmployee.getId());
        e.setFirstName(tfFirstName.getText().trim());
        e.setLastName(tfLastName.getText().trim());
        // 3c: apply National ID (admin direct or keep original)
        e.setNationalId(tfNationalId.getText().trim());
        e.setDateOfBirth(dpDob.getValue());
        e.setGender(cbGender.getValue());
        e.setPhone(tfPhone.getText().trim());
        e.setEmail(tfEmail.getText().trim());
        e.setAddress(taAddress.getText().trim());
        e.setDepartmentId(cbDepartment.getValue() != null
            ? cbDepartment.getValue().getId()
            : loadedEmployee.getDepartmentId());
        e.setPosition(tfDesignation.getText().trim());
        // 3b: salary and employment type — only apply if Admin (non-Admin sees disabled fields)
        boolean isAdmin = SessionManager.getInstance().isAdmin();
        e.setEmploymentType(isAdmin ? cbEmploymentType.getValue() : loadedEmployee.getEmploymentType());
        if (!tfSalary.getText().isBlank() && isAdmin)
            e.setBasicSalary(new BigDecimal(tfSalary.getText().trim()));
        else
            e.setBasicSalary(loadedEmployee.getBasicSalary());
        e.setStatus(cbStatus.getValue() != null ? cbStatus.getValue() : loadedEmployee.getStatus());
        e.setHireDate(loadedEmployee.getHireDate());
        e.setProbationEndDate(loadedEmployee.getProbationEndDate());
        return e;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void showFieldError(Label lbl, String msg) { lbl.setText(msg); setNodeVisible(lbl, true); }
    private void clearFieldError(Label lbl)            { lbl.setText("");  setNodeVisible(lbl, false); }

    private void showBanner(String msg, boolean error) {
        lblBanner.setText(msg);
        lblBanner.getStyleClass().removeAll("form-banner-error", "form-banner-warning");
        lblBanner.getStyleClass().add(error ? "form-banner-error" : "form-banner-warning");
        setNodeVisible(lblBanner, true);
        setNodeVisible(bannerArea, true);
    }

    private void hideBanner() {
        setNodeVisible(lblBanner, false);
        refreshBannerVisibility();
    }

    private void refreshBannerVisibility() {
        boolean show = lblBanner.isVisible() || conflictBox.isVisible() || retryBox.isVisible();
        setNodeVisible(bannerArea, show);
    }

    private void clearAllErrors() {
        for (Label l : new Label[]{errFirstName, errLastName, errPhone, errEmail,
                errDesignation, errSalary, errReason})
            clearFieldError(l);
    }

    private void showSuccess(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setTitle("Success"); a.setHeaderText(null); a.showAndWait();
    }

    private void setNodeVisible(javafx.scene.Node n, boolean v) {
        n.setVisible(v); n.setManaged(v);
    }

    private String nvl(String s) { return s != null ? s : ""; }
    private String nvl(String s, String fallback) { return (s != null && !s.isBlank()) ? s : fallback; }
}
