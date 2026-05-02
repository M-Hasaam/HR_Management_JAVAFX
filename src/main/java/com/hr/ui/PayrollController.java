package com.hr.ui;

import com.hr.model.Employee;
import com.hr.model.Payroll;
import com.hr.service.EmployeeService;
import com.hr.service.PayrollService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PayrollController {

    @FXML private FlowPane  cardPane;
    @FXML private TextField tfSearch;
    @FXML private Label     lblCount;
    @FXML private Label     lblTotalNet;

    private PayrollService  payrollService;
    private EmployeeService employeeService;
    private List<Payroll>   allPayrolls = new ArrayList<>();

    @FXML
    public void initialize() {
        try {
            payrollService  = new PayrollService();
            employeeService = new EmployeeService();
        } catch (SQLException e) { showError("Database Error", e.getMessage()); return; }
        tfSearch.textProperty().addListener((o, old, val) -> filterCards(val));
        loadData();
    }

    private void loadData() {
        try {
            allPayrolls = payrollService.getAllPayrolls();
            updateStatsBar(allPayrolls);
            populateCards(allPayrolls);
        } catch (SQLException e) { showError("Load Error", e.getMessage()); }
    }

    private void filterCards(String q) {
        if (q == null || q.isBlank()) { populateCards(allPayrolls); return; }
        String lq = q.toLowerCase();
        populateCards(allPayrolls.stream()
            .filter(p -> p.getEmployeeName() != null && p.getEmployeeName().toLowerCase().contains(lq))
            .toList());
    }

    private void updateStatsBar(List<Payroll> list) {
        BigDecimal total = list.stream()
            .map(p -> p.getNetPay() != null ? p.getNetPay() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        lblCount.setText(list.size() + " Records");
        lblTotalNet.setText("PKR " + String.format("%,.0f", total) + " Total Net");
    }

    private void populateCards(List<Payroll> payrolls) {
        cardPane.getChildren().clear();
        if (payrolls.isEmpty()) {
            Label empty = new Label("No payroll records found.");
            empty.getStyleClass().add("label-muted"); empty.setPadding(new Insets(40));
            cardPane.getChildren().add(empty); return;
        }
        for (Payroll p : payrolls) cardPane.getChildren().add(buildCard(p));
    }

    private VBox buildCard(Payroll p) {
        VBox card = new VBox(0);
        card.getStyleClass().add("data-card");
        card.setPrefWidth(300);

        // Header
        HBox header = new HBox(10);
        header.getStyleClass().add("data-card-header");
        header.setAlignment(Pos.CENTER_LEFT);

        Label empLabel = new Label(nvl(p.getEmployeeName(), "Unknown"));
        empLabel.getStyleClass().add("data-card-name");
        HBox.setHgrow(empLabel, Priority.ALWAYS);

        Label netLabel = new Label(p.getNetPay() != null
            ? "PKR " + String.format("%,.0f", p.getNetPay()) : "—");
        netLabel.getStyleClass().add("payroll-net-badge");
        header.getChildren().addAll(empLabel, netLabel);

        // Body: period + breakdown
        VBox body = new VBox(8);
        body.getStyleClass().add("data-card-body");

        String period = (p.getPayPeriodStart() != null ? p.getPayPeriodStart().toString() : "?")
            + " → " + (p.getPayPeriodEnd() != null ? p.getPayPeriodEnd().toString() : "?");
        body.getChildren().add(detailRow("Period", period));

        Separator sep = new Separator(); sep.setStyle("-fx-padding: 4 0 4 0;");
        body.getChildren().add(sep);

        body.getChildren().addAll(
            detailRow("Basic",    fmt(p.getBasicSalary())),
            detailRow("Tax 15%", "− " + fmt(p.getTaxDeduction())),
            detailRow("Benefit", "− " + fmt(p.getBenefitsDeduction()))
        );

        Separator sep2 = new Separator();
        body.getChildren().add(sep2);

        HBox netRow = new HBox(8); netRow.setAlignment(Pos.CENTER_LEFT);
        Label netKey = new Label("Net Pay:"); netKey.getStyleClass().add("payroll-net-key");
        Label netVal = new Label(fmt(p.getNetPay())); netVal.getStyleClass().add("payroll-net-value");
        netRow.getChildren().addAll(netKey, netVal);
        body.getChildren().add(netRow);

        if (p.getProcessedDate() != null) {
            body.getChildren().add(detailRow("Processed", p.getProcessedDate().toString()));
        }

        // Footer
        HBox footer = new HBox(8);
        footer.getStyleClass().add("data-card-footer");
        footer.setAlignment(Pos.CENTER_RIGHT);
        Button delBtn = new Button("Delete"); delBtn.getStyleClass().add("btn-danger-sm");
        delBtn.setOnAction(e -> handleDeleteCard(p));
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        footer.getChildren().addAll(sp, delBtn);

        card.getChildren().addAll(header, body, footer);
        return card;
    }

    private HBox detailRow(String label, String value) {
        HBox row = new HBox(8); row.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(label + ":"); lbl.getStyleClass().add("detail-label"); lbl.setMinWidth(60);
        Label val = new Label(value); val.getStyleClass().add("detail-value");
        HBox.setHgrow(val, Priority.ALWAYS); row.getChildren().addAll(lbl, val); return row;
    }

    private void handleDeleteCard(Payroll p) {
        Alert c = new Alert(Alert.AlertType.CONFIRMATION, "Delete this payroll record?", ButtonType.YES, ButtonType.NO);
        c.setHeaderText(null);
        if (c.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try { payrollService.deletePayroll(p.getId()); loadData(); }
            catch (SQLException e) { showError("Delete Error", e.getMessage()); }
        }
    }

    // ===== PRESERVED PROCESS LOGIC (unchanged) =====
    @FXML
    private void handleProcess() {
        List<Employee> employees;
        try { employees = employeeService.getAllEmployees(); }
        catch (SQLException e) { showError("Load Error", e.getMessage()); return; }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Process Payroll");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));

        ComboBox<Employee> cbEmployee = new ComboBox<>(FXCollections.observableArrayList(employees));
        DatePicker dpStart = new DatePicker(LocalDate.now().withDayOfMonth(1));
        DatePicker dpEnd   = new DatePicker(LocalDate.now());
        Label lblPreview   = new Label("Select employee and period, then click OK.");
        lblPreview.setStyle("-fx-text-fill: #555; -fx-font-style: italic;");

        cbEmployee.setOnAction(e -> updatePreview(cbEmployee.getValue(), dpStart, dpEnd, lblPreview));
        dpStart.setOnAction(e    -> updatePreview(cbEmployee.getValue(), dpStart, dpEnd, lblPreview));
        dpEnd.setOnAction(e      -> updatePreview(cbEmployee.getValue(), dpStart, dpEnd, lblPreview));

        grid.addRow(0, new Label("Employee:*"),     cbEmployee);
        grid.addRow(1, new Label("Period Start:*"), dpStart);
        grid.addRow(2, new Label("Period End:*"),   dpEnd);
        grid.addRow(3, new Label("Preview:"),       lblPreview);
        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Employee emp = cbEmployee.getValue();
            if (emp == null) { showError("Input Error", "Please select an employee."); return; }
            try {
                payrollService.processPayroll(emp, dpStart.getValue(), dpEnd.getValue());
                loadData();
                showInfo("Payroll processed for " + emp.getFullName() + ".");
            } catch (IllegalArgumentException | SQLException e) { showError("Processing Error", e.getMessage()); }
        }
    }

    private void updatePreview(Employee emp, DatePicker dpStart, DatePicker dpEnd, Label lbl) {
        if (emp == null || dpStart.getValue() == null || dpEnd.getValue() == null) return;
        BigDecimal[] breakdown = payrollService.calculateBreakdown(emp.getBasicSalary());
        lbl.setText(String.format("Basic: %.2f  |  Tax: %.2f  |  Benefits: %.2f  |  Net: %.2f",
            emp.getBasicSalary(), breakdown[0], breakdown[1], breakdown[2]));
    }

    private String fmt(BigDecimal v) { return v != null ? "PKR " + String.format("%,.2f", v) : "—"; }
    private String nvl(String s, String fb) { return (s != null && !s.isBlank()) ? s : fb; }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setTitle(title); a.setHeaderText(null); a.showAndWait();
    }
    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setTitle("Success"); a.setHeaderText(null); a.showAndWait();
    }
}
