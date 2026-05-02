package com.hr.ui;

import com.hr.controller.AssignmentController;
import com.hr.controller.OffboardingController;
import com.hr.controller.RegisterController;
import com.hr.controller.UpdateController;
import com.hr.model.Department;
import com.hr.model.Employee;
import com.hr.service.DepartmentService;
import com.hr.service.EmployeeService;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EmployeeController {

    @FXML private FlowPane  cardPane;
    @FXML private TextField tfSearch;
    @FXML private Label     lblTotal;
    @FXML private Label     lblActive;
    @FXML private Label     lblInactive;

    private EmployeeService    employeeService;
    private DepartmentService  departmentService;
    private RegisterController   registerController;
    private UpdateController     updateController;
    private AssignmentController assignmentController;
    private OffboardingController offboardingController;

    private List<Employee> allEmployees = new ArrayList<>();

    private static final String[] AVATAR_COLORS = {
        "#3b82f6","#8b5cf6","#10b981","#f59e0b","#ef4444",
        "#ec4899","#06b6d4","#84cc16","#f97316","#6366f1"
    };

    @FXML
    public void initialize() {
        try {
            employeeService   = new EmployeeService();
            departmentService = new DepartmentService();
        } catch (SQLException e) { showError("Database Error", e.getMessage()); return; }

        try { registerController    = new RegisterController();   } catch (SQLException ignored) {}
        try { updateController      = new UpdateController();     } catch (SQLException ignored) {}
        try { assignmentController  = new AssignmentController(); } catch (SQLException ignored) {}
        try { offboardingController = new OffboardingController();} catch (SQLException ignored) {}

        tfSearch.textProperty().addListener((obs, old, val) -> filterCards(val));
        loadData();
    }

    private void loadData() {
        try {
            allEmployees = employeeService.getAllEmployees();
            updateStatsBar(allEmployees);
            populateCards(allEmployees);
        } catch (SQLException e) { showError("Load Error", e.getMessage()); }
    }

    private void filterCards(String query) {
        if (query == null || query.isBlank()) { populateCards(allEmployees); return; }
        String q = query.toLowerCase();
        List<Employee> filtered = allEmployees.stream().filter(emp ->
            safeContains(emp.getFullName(),       q) ||
            safeContains(emp.getEmail(),          q) ||
            safeContains(emp.getPosition(),       q) ||
            safeContains(emp.getDepartmentName(), q)
        ).toList();
        populateCards(filtered);
    }

    private boolean safeContains(String f, String q) { return f != null && f.toLowerCase().contains(q); }

    private void updateStatsBar(List<Employee> list) {
        long active = list.stream().filter(e -> "ACTIVE".equalsIgnoreCase(e.getStatus())).count();
        lblTotal.setText(list.size() + " Total");
        lblActive.setText(active + " Active");
        lblInactive.setText((list.size() - active) + " Inactive");
    }

    private void populateCards(List<Employee> employees) {
        cardPane.getChildren().clear();
        if (employees.isEmpty()) {
            Label empty = new Label("No employees match your search.");
            empty.getStyleClass().add("label-muted");
            empty.setPadding(new Insets(40));
            cardPane.getChildren().add(empty);
            return;
        }
        for (Employee emp : employees) cardPane.getChildren().add(buildCard(emp));
    }

    private VBox buildCard(Employee emp) {
        VBox card = new VBox(0);
        card.getStyleClass().add("data-card");
        card.setPrefWidth(280);

        // Header
        HBox header = new HBox(12);
        header.getStyleClass().add("data-card-header");
        header.setAlignment(Pos.CENTER_LEFT);
        Label avatar = buildAvatar(emp.getFullName());
        VBox nameBox = new VBox(2);
        Label nameLabel = new Label(emp.getFullName());
        nameLabel.getStyleClass().add("data-card-name");
        nameLabel.setWrapText(true);
        Label posLabel = new Label(nvl(emp.getPosition(), "No position"));
        posLabel.getStyleClass().add("data-card-sub");
        nameBox.getChildren().addAll(nameLabel, posLabel);
        HBox.setHgrow(nameBox, Priority.ALWAYS);
        boolean active = "ACTIVE".equalsIgnoreCase(emp.getStatus());
        Label badge = new Label(nvl(emp.getStatus(), "UNKNOWN"));
        badge.getStyleClass().add(active ? "badge-active" : "badge-inactive");
        header.getChildren().addAll(avatar, nameBox, badge);

        // Body
        VBox body = new VBox(8);
        body.getStyleClass().add("data-card-body");
        body.getChildren().addAll(
            detailRow("Dept",   nvl(emp.getDepartmentName(), "—")),
            detailRow("Email",  nvl(emp.getEmail(), "—")),
            detailRow("Phone",  nvl(emp.getPhone(), "—")),
            detailRow("Salary", emp.getBasicSalary() != null
                ? "PKR " + String.format("%,.0f", emp.getBasicSalary()) : "—"),
            detailRow("Hired",  emp.getHireDate() != null ? emp.getHireDate().toString() : "—")
        );

        // Footer
        HBox footer = new HBox(8);
        footer.getStyleClass().add("data-card-footer");
        footer.setAlignment(Pos.CENTER_RIGHT);
        Button assignBtn = new Button("Assign"); assignBtn.getStyleClass().add("btn-secondary");
        Button editBtn   = new Button("Edit");   editBtn.getStyleClass().add("btn-secondary");
        Button delBtn    = new Button("Delete"); delBtn.getStyleClass().add("btn-danger");
        assignBtn.setOnAction(e -> navigateToAssign(emp));
        editBtn.setOnAction(e   -> navigateToUpdate(emp));
        delBtn.setOnAction(e    -> handleDeleteCard(emp));
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        footer.getChildren().addAll(sp, assignBtn, editBtn, delBtn);

        card.getChildren().addAll(header, body, footer);
        return card;
    }

    private Label buildAvatar(String name) {
        String first  = name != null && !name.isEmpty() ? name.substring(0, 1).toUpperCase() : "?";
        int    space  = name != null ? name.indexOf(' ') : -1;
        String second = (space > 0 && space + 1 < name.length())
            ? String.valueOf(name.charAt(space + 1)).toUpperCase() : "";
        Label av = new Label(first + second);
        av.setMinSize(44, 44); av.setMaxSize(44, 44);
        av.setAlignment(Pos.CENTER); av.setTextAlignment(TextAlignment.CENTER);
        String color = AVATAR_COLORS[Math.abs(name != null ? name.hashCode() : 0) % AVATAR_COLORS.length];
        av.setStyle("-fx-background-color:" + color + ";-fx-background-radius:22;" +
            "-fx-text-fill:white;-fx-font-size:13px;-fx-font-weight:bold;");
        return av;
    }

    private HBox detailRow(String label, String value) {
        HBox row = new HBox(8); row.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(label + ":"); lbl.getStyleClass().add("detail-label"); lbl.setMinWidth(48);
        Label val = new Label(value);       val.getStyleClass().add("detail-value");
        val.setWrapText(true); HBox.setHgrow(val, Priority.ALWAYS);
        row.getChildren().addAll(lbl, val);
        return row;
    }

    @FXML private void handleAdd() {
        try {
            javafx.scene.Scene scene = cardPane.getScene();
            StackPane contentArea = (StackPane) scene.lookup("#contentArea");
            if (contentArea == null) return;

            java.net.URL fxmlUrl = getClass().getResource("/com/hr/register_employee.fxml");
            if (fxmlUrl == null) {
                showError("Navigation Error", "register_employee.fxml not found. Rebuild the project.");
                return;
            }

            Node content = new FXMLLoader(fxmlUrl).load();
            content.setOpacity(0);
            content.setTranslateY(10);
            contentArea.getChildren().setAll(content);
            new Timeline(new KeyFrame(Duration.millis(220),
                new KeyValue(content.opacityProperty(),    1, Interpolator.EASE_OUT),
                new KeyValue(content.translateYProperty(), 0, Interpolator.EASE_OUT)
            )).play();

            Button navEmployees = (Button) scene.lookup("#navEmployees");
            Button navRegister  = (Button) scene.lookup("#navRegister");
            if (navEmployees != null) navEmployees.getStyleClass().remove("nav-button-active");
            if (navRegister  != null) navRegister.getStyleClass().add("nav-button-active");

        } catch (Exception e) {
            showError("Navigation Error", e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleDeleteCard(Employee emp) {
        Alert c = new Alert(Alert.AlertType.CONFIRMATION,
            "Delete employee \"" + emp.getFullName() + "\"?", ButtonType.YES, ButtonType.NO);
        c.setTitle("Confirm Delete"); c.setHeaderText(null);
        if (c.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try { employeeService.deleteEmployee(emp.getId()); loadData(); }
            catch (SQLException e) { showError("Delete Error", e.getMessage()); }
        }
    }

    // ===== UC-02: Navigate to full Update Employee module =====
    private void navigateToUpdate(Employee emp) {
        try {
            javafx.scene.Scene scene = cardPane.getScene();
            StackPane contentArea = (StackPane) scene.lookup("#contentArea");
            if (contentArea == null) { showError("Navigation Error", "Content area not found."); return; }

            java.net.URL url = getClass().getResource("/com/hr/update_employee.fxml");
            if (url == null) { showError("Navigation Error", "update_employee.fxml not found. Rebuild the project."); return; }

            FXMLLoader loader = new FXMLLoader(url);
            Node content = loader.load();
            UpdateEmployeeController ctrl = loader.getController();
            ctrl.setEmployee(emp);

            content.setOpacity(0);
            content.setTranslateY(10);
            contentArea.getChildren().setAll(content);
            new Timeline(new KeyFrame(Duration.millis(220),
                new KeyValue(content.opacityProperty(),    1, Interpolator.EASE_OUT),
                new KeyValue(content.translateYProperty(), 0, Interpolator.EASE_OUT)
            )).play();

            Button navEmployees = (Button) scene.lookup("#navEmployees");
            if (navEmployees != null) navEmployees.getStyleClass().remove("nav-button-active");

        } catch (Exception e) {
            showError("Navigation Error", e.getMessage());
            e.printStackTrace();
        }
    }

    // ===== UC-03: Navigate to Assign Department module =====
    private void navigateToAssign(Employee emp) {
        try {
            javafx.scene.Scene scene = cardPane.getScene();
            StackPane contentArea = (StackPane) scene.lookup("#contentArea");
            if (contentArea == null) { showError("Navigation Error", "Content area not found."); return; }

            java.net.URL url = getClass().getResource("/com/hr/assign_department.fxml");
            if (url == null) { showError("Navigation Error", "assign_department.fxml not found. Rebuild the project."); return; }

            FXMLLoader loader = new FXMLLoader(url);
            Node content = loader.load();
            AssignDepartmentController ctrl = loader.getController();
            ctrl.setEmployee(emp);

            content.setOpacity(0);
            content.setTranslateY(10);
            contentArea.getChildren().setAll(content);
            new Timeline(new KeyFrame(Duration.millis(220),
                new KeyValue(content.opacityProperty(),    1, Interpolator.EASE_OUT),
                new KeyValue(content.translateYProperty(), 0, Interpolator.EASE_OUT)
            )).play();

            Button navEmployees = (Button) scene.lookup("#navEmployees");
            if (navEmployees != null) navEmployees.getStyleClass().remove("nav-button-active");

        } catch (Exception e) {
            showError("Navigation Error", e.getMessage());
            e.printStackTrace();
        }
    }

    // ===== PRESERVED FORM LOGIC (unchanged) =====
    private void showForm(Employee existing) {
        List<Department> departments;
        try { departments = departmentService.getAllDepartments(); }
        catch (SQLException e) { showError("Load Error", e.getMessage()); return; }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Employee" : "Edit Employee");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));

        TextField    tfFirst    = new TextField();
        TextField    tfLast     = new TextField();
        TextField    tfEmail    = new TextField();
        TextField    tfPhone    = new TextField();
        TextField    tfPosition = new TextField();
        TextField    tfSalary   = new TextField();
        DatePicker   dpHire     = new DatePicker(LocalDate.now());
        ComboBox<Department> cbDept   = new ComboBox<>(FXCollections.observableArrayList(departments));
        ComboBox<String>     cbStatus = new ComboBox<>(FXCollections.observableArrayList("ACTIVE","INACTIVE"));
        cbStatus.setValue("ACTIVE");

        if (existing != null) {
            tfFirst.setText(existing.getFirstName());
            tfLast.setText(existing.getLastName());
            tfEmail.setText(existing.getEmail());
            tfPhone.setText(existing.getPhone());
            tfPosition.setText(existing.getPosition());
            tfSalary.setText(existing.getBasicSalary() != null ? existing.getBasicSalary().toString() : "");
            dpHire.setValue(existing.getHireDate());
            cbStatus.setValue(existing.getStatus());
            departments.stream().filter(d -> d.getId() == existing.getDepartmentId())
                .findFirst().ifPresent(cbDept::setValue);
        }

        grid.addRow(0, new Label("First Name:*"), tfFirst);
        grid.addRow(1, new Label("Last Name:*"),  tfLast);
        grid.addRow(2, new Label("Email:*"),       tfEmail);
        grid.addRow(3, new Label("Phone:"),        tfPhone);
        grid.addRow(4, new Label("Department:"),   cbDept);
        grid.addRow(5, new Label("Position:"),     tfPosition);
        grid.addRow(6, new Label("Basic Salary:"), tfSalary);
        grid.addRow(7, new Label("Hire Date:"),    dpHire);
        grid.addRow(8, new Label("Status:"),       cbStatus);
        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Employee emp = existing != null ? existing : new Employee();
                emp.setFirstName(tfFirst.getText().trim());
                emp.setLastName(tfLast.getText().trim());
                emp.setEmail(tfEmail.getText().trim());
                emp.setPhone(tfPhone.getText().trim());
                emp.setPosition(tfPosition.getText().trim());
                emp.setHireDate(dpHire.getValue());
                emp.setStatus(cbStatus.getValue());
                if (cbDept.getValue() != null) emp.setDepartmentId(cbDept.getValue().getId());
                emp.setBasicSalary(tfSalary.getText().isBlank()
                    ? BigDecimal.ZERO : new BigDecimal(tfSalary.getText().trim()));

                if (existing == null) {
                    if (registerController != null) {
                        try { registerController.registerNewEmployee(emp); }
                        catch (IllegalArgumentException | IllegalStateException dupEx) {
                            showError("Registration Error", dupEx.getMessage()); return;
                        }
                    } else { employeeService.addEmployee(emp); }
                } else {
                    if (updateController != null) updateController.updateEmployeeRecord(emp.getId(), emp, "Updated via UI");
                    else employeeService.updateEmployee(emp);
                }
                loadData();
            } catch (NumberFormatException e) {
                showError("Input Error", "Salary must be a valid number.");
            } catch (IllegalArgumentException | IllegalStateException | SQLException e) {
                showError("Save Error", e.getMessage());
            }
        }
    }

    private String nvl(String s, String fb) { return (s != null && !s.isBlank()) ? s : fb; }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setTitle(title); a.setHeaderText(null); a.showAndWait();
    }
}
