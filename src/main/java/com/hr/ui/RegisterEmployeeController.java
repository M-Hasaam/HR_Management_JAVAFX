package com.hr.ui;

import com.hr.controller.RegisterController;
import com.hr.model.Department;
import com.hr.model.Employee;
import com.hr.service.DepartmentService;
import com.hr.service.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class RegisterEmployeeController {

    // ── Banner ───────────────────────────────────────────────────────────────
    @FXML private VBox   bannerArea;
    @FXML private Label  lblBanner;
    @FXML private HBox   capacityWarningBox;
    @FXML private Label  lblCapacityWarning;
    @FXML private Button btnOverride;

    // ── Section 1 — Personal ─────────────────────────────────────────────────
    @FXML private TextField        tfFirstName;
    @FXML private TextField        tfLastName;
    @FXML private TextField        tfNationalId;
    @FXML private DatePicker       dpDateOfBirth;
    @FXML private ComboBox<String> cbGender;
    @FXML private TextField        tfPhone;
    @FXML private TextField        tfEmail;
    @FXML private TextArea         taAddress;

    @FXML private Label errFirstName;
    @FXML private Label errLastName;
    @FXML private Label errNationalId;
    @FXML private Label errDob;
    @FXML private Label errGender;
    @FXML private Label errPhone;
    @FXML private Label errEmail;
    @FXML private Label errAddress;

    // ── Section 2 — Organizational ───────────────────────────────────────────
    @FXML private ComboBox<Department> cbDepartment;
    @FXML private TextField            tfDesignation;
    @FXML private ComboBox<String>     cbEmploymentType;
    @FXML private DatePicker           dpJoiningDate;
    @FXML private DatePicker           dpProbationEnd;
    @FXML private TextField            tfSalary;

    @FXML private Label errDepartment;
    @FXML private Label errDesignation;
    @FXML private Label errEmploymentType;
    @FXML private Label errJoiningDate;
    @FXML private Label errSalary;

    @FXML private Button btnSubmit;

    // ── State ────────────────────────────────────────────────────────────────
    private RegisterController registerController;
    private DepartmentService  departmentService;
    private boolean capacityOverridden    = false;
    private String  overrideJustification = null;

    @FXML
    public void initialize() {
        try {
            registerController = new RegisterController();
            departmentService  = new DepartmentService();
        } catch (SQLException e) {
            showBanner("Database connection error: " + e.getMessage(), true);
            btnSubmit.setDisable(true);
            return;
        }

        cbGender.setItems(FXCollections.observableArrayList("Male", "Female", "Other"));
        cbEmploymentType.setItems(FXCollections.observableArrayList(
            "Full-Time", "Part-Time", "Contract", "Intern"));

        try {
            List<Department> depts = departmentService.getAllDepartments();
            cbDepartment.setItems(FXCollections.observableArrayList(depts));
        } catch (SQLException e) {
            showBanner("Failed to load departments: " + e.getMessage(), true);
        }

        // Real-time duplicate checks on focus-lost (Extension 2a / 2b)
        tfNationalId.focusedProperty().addListener((obs, had, has) -> {
            if (!has && !tfNationalId.getText().isBlank()) checkNationalIdDuplicate();
        });
        tfEmail.focusedProperty().addListener((obs, had, has) -> {
            if (!has && !tfEmail.getText().isBlank()) checkEmailDuplicate();
        });

        // Department capacity check on selection (Extension 3a)
        cbDepartment.valueProperty().addListener((obs, old, val) -> {
            if (val != null) checkDepartmentCapacity(val);
        });

        // Probation auto-calculation
        cbEmploymentType.valueProperty().addListener((obs, old, val) -> recalcProbation());
        dpJoiningDate.valueProperty().addListener((obs, old, val) -> recalcProbation());

        dpProbationEnd.setEditable(false);
    }

    // ── Probation auto-calculation ────────────────────────────────────────────
    private void recalcProbation() {
        String    type    = cbEmploymentType.getValue();
        LocalDate joining = dpJoiningDate.getValue();
        if (type == null || joining == null) return;
        int months = switch (type) {
            case "Full-Time" -> 3;
            case "Contract"  -> 2;
            case "Part-Time", "Intern" -> 1;
            default -> 0;
        };
        dpProbationEnd.setValue(months == 0 ? null : joining.plusMonths(months));
    }

    // ── Real-time duplicate checks ────────────────────────────────────────────
    private void checkNationalIdDuplicate() {
        try {
            if (registerController.checkNationalIdDuplicate(tfNationalId.getText().trim()))
                showFieldError(errNationalId, "This National ID is already registered.");
            else
                clearFieldError(errNationalId);
        } catch (SQLException ignored) {}
    }

    private void checkEmailDuplicate() {
        try {
            if (registerController.checkEmailDuplicate(tfEmail.getText().trim()))
                showFieldError(errEmail, "This email is already registered to another employee.");
            else
                clearFieldError(errEmail);
        } catch (SQLException ignored) {}
    }

    // ── Department capacity check (Extension 3a) ──────────────────────────────
    private void checkDepartmentCapacity(Department dept) {
        try {
            if (registerController.isDepartmentAtCapacity(dept.getId())) {
                lblCapacityWarning.setText(
                    "Warning: \"" + dept.getName() + "\" has reached its maximum headcount ("
                    + dept.getCurrentHeadcount() + "/" + dept.getMaxHeadcount() + "). "
                    + "Admin authorization is required to proceed.");
                setVisible(capacityWarningBox, true);
                setVisible(bannerArea, true);
                capacityOverridden = false;
            } else {
                setVisible(capacityWarningBox, false);
                refreshBannerVisibility();
                capacityOverridden = false;
            }
        } catch (SQLException ignored) {}
    }

    @FXML
    private void handleCapacityOverride() {
        if (!SessionManager.getInstance().isAdmin()) {
            new Alert(Alert.AlertType.WARNING,
                "Only an Admin can authorize a headcount override. Contact your administrator.",
                ButtonType.OK).showAndWait();
            return;
        }
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("Admin Override");
        dlg.setHeaderText("Department is at maximum headcount.");
        dlg.setContentText("Enter justification (will be logged):");
        dlg.showAndWait().ifPresent(text -> {
            if (!text.isBlank()) {
                overrideJustification = text.trim();
                capacityOverridden    = true;
                lblCapacityWarning.setText(lblCapacityWarning.getText()
                    + "\n✔ Override authorized. Justification: " + overrideJustification);
                btnOverride.setDisable(true);
            }
        });
    }

    // ── Submit ────────────────────────────────────────────────────────────────
    @FXML
    private void handleSubmit() {
        clearAllErrors();
        hideBanner();

        if (!validateForm()) return;

        if (capacityWarningBox.isVisible() && !capacityOverridden) {
            showBanner("Department is at maximum headcount. Admin must authorize the override first.", true);
            return;
        }

        try {
            Employee emp = buildEmployee();
            RegisterController.RegistrationResult result =
                registerController.registerWithProvisioning(emp, capacityOverridden, overrideJustification);
            showSuccessDialog(result);
            handleClear();

        } catch (IllegalArgumentException ex) {
            String msg = ex.getMessage() != null ? ex.getMessage() : "Duplicate record.";
            if (msg.toLowerCase().contains("national id")) showFieldError(errNationalId, msg);
            else if (msg.toLowerCase().contains("email"))  showFieldError(errEmail, msg);
            showBanner("Registration blocked: " + msg, true);
        } catch (IllegalStateException ex) {
            showBanner(ex.getMessage(), true);
            Alert alert = new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK);
            alert.setTitle("Cannot Register Employee");
            alert.setHeaderText("Department Capacity Exceeded");
            alert.showAndWait();
        } catch (SQLException ex) {
            showBanner("Database error: " + ex.getMessage(), true);
        }
    }

    // ── Validation ────────────────────────────────────────────────────────────
    private boolean validateForm() {
        boolean ok = true;
        if (tfFirstName.getText().isBlank())     { showFieldError(errFirstName, "First name is required.");       ok = false; }
        if (tfLastName.getText().isBlank())       { showFieldError(errLastName, "Last name is required.");         ok = false; }
        if (tfNationalId.getText().isBlank())     { showFieldError(errNationalId, "National ID is required.");     ok = false; }
        if (dpDateOfBirth.getValue() == null)     { showFieldError(errDob, "Date of birth is required.");          ok = false; }
        if (cbGender.getValue() == null)          { showFieldError(errGender, "Gender is required.");              ok = false; }
        if (tfPhone.getText().isBlank())          { showFieldError(errPhone, "Contact number is required.");       ok = false; }
        if (tfEmail.getText().isBlank())          { showFieldError(errEmail, "Email address is required.");        ok = false; }
        else if (!tfEmail.getText().contains("@")){ showFieldError(errEmail, "Enter a valid email address.");      ok = false; }
        if (taAddress.getText().isBlank())        { showFieldError(errAddress, "Residential address is required."); ok = false; }
        if (cbDepartment.getValue() == null)      { showFieldError(errDepartment, "Department is required.");      ok = false; }
        if (tfDesignation.getText().isBlank())    { showFieldError(errDesignation, "Designation is required.");    ok = false; }
        if (cbEmploymentType.getValue() == null)  { showFieldError(errEmploymentType, "Employment type is required."); ok = false; }
        if (dpJoiningDate.getValue() == null)     { showFieldError(errJoiningDate, "Joining date is required.");   ok = false; }
        if (!tfSalary.getText().isBlank()) {
            try { new BigDecimal(tfSalary.getText().trim()); }
            catch (NumberFormatException e) { showFieldError(errSalary, "Salary must be a valid number."); ok = false; }
        }
        if (!ok) showBanner("Please correct the highlighted fields before submitting.", true);
        return ok;
    }

    // ── Build Employee ────────────────────────────────────────────────────────
    private Employee buildEmployee() {
        Employee emp = new Employee();
        emp.setFirstName(tfFirstName.getText().trim());
        emp.setLastName(tfLastName.getText().trim());
        emp.setNationalId(tfNationalId.getText().trim());
        emp.setDateOfBirth(dpDateOfBirth.getValue());
        emp.setGender(cbGender.getValue());
        emp.setPhone(tfPhone.getText().trim());
        emp.setEmail(tfEmail.getText().trim());
        emp.setAddress(taAddress.getText().trim());
        emp.setDepartmentId(cbDepartment.getValue().getId());
        emp.setPosition(tfDesignation.getText().trim());
        emp.setEmploymentType(cbEmploymentType.getValue());
        emp.setHireDate(dpJoiningDate.getValue());
        emp.setProbationEndDate(dpProbationEnd.getValue());
        emp.setStatus("ACTIVE");
        if (!tfSalary.getText().isBlank())
            emp.setBasicSalary(new BigDecimal(tfSalary.getText().trim()));
        return emp;
    }

    // ── Clear ─────────────────────────────────────────────────────────────────
    @FXML
    private void handleClear() {
        tfFirstName.clear(); tfLastName.clear(); tfNationalId.clear();
        dpDateOfBirth.setValue(null); cbGender.setValue(null);
        tfPhone.clear(); tfEmail.clear(); taAddress.clear();
        cbDepartment.setValue(null); tfDesignation.clear();
        cbEmploymentType.setValue(null); dpJoiningDate.setValue(null);
        dpProbationEnd.setValue(null); tfSalary.clear();
        clearAllErrors();
        hideBanner();
        setVisible(capacityWarningBox, false);
        capacityOverridden    = false;
        overrideJustification = null;
        btnOverride.setDisable(false);
    }

    // ── Success dialog ────────────────────────────────────────────────────────
    private void showSuccessDialog(RegisterController.RegistrationResult result) {
        String emailLine = result.emailQueued()
            ? "Welcome email queued (SMTP retry scheduled)."
            : "Welcome email sent.";
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Registration Complete");
        a.setHeaderText("Employee Registered Successfully");
        a.setContentText(
            "Employee ID:  " + result.employeeCode() + "\n"
            + emailLine + "\n"
            + "Username: " + result.username() + "\n"
            + "Temp password: " + result.tempPassword() + "\n\n"
            + "Please share credentials securely with the employee.");
        a.showAndWait();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void showFieldError(Label lbl, String msg) { lbl.setText(msg); setVisible(lbl, true); }
    private void clearFieldError(Label lbl)            { lbl.setText("");  setVisible(lbl, false); }

    private void showBanner(String msg, boolean error) {
        lblBanner.setText(msg);
        lblBanner.getStyleClass().removeAll("form-banner-error", "form-banner-warning");
        lblBanner.getStyleClass().add(error ? "form-banner-error" : "form-banner-warning");
        setVisible(lblBanner, true);
        setVisible(bannerArea, true);
    }

    private void hideBanner() {
        setVisible(lblBanner, false);
        refreshBannerVisibility();
    }

    private void refreshBannerVisibility() {
        boolean show = lblBanner.isVisible() || capacityWarningBox.isVisible();
        setVisible(bannerArea, show);
    }

    private void clearAllErrors() {
        for (Label l : new Label[]{errFirstName, errLastName, errNationalId, errDob,
                errGender, errPhone, errEmail, errAddress,
                errDepartment, errDesignation, errEmploymentType, errJoiningDate, errSalary})
            clearFieldError(l);
    }

    private void setVisible(javafx.scene.Node n, boolean v) {
        n.setVisible(v);
        n.setManaged(v);
    }
}
