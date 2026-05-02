package com.hr.ui;

import com.hr.model.Department;
import com.hr.service.DepartmentService;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DepartmentController {

    @FXML private FlowPane  cardPane;
    @FXML private TextField tfSearch;

    private DepartmentService service;
    private List<Department>  allDepts = new ArrayList<>();

    private static final String[] DEPT_COLORS = {
        "#3b82f6","#8b5cf6","#10b981","#f59e0b","#ef4444","#ec4899","#06b6d4"
    };

    @FXML
    public void initialize() {
        try { service = new DepartmentService(); }
        catch (SQLException e) { showError("Database Error", e.getMessage()); return; }
        tfSearch.textProperty().addListener((o, old, val) -> filterCards(val));
        loadData();
    }

    private void loadData() {
        try { allDepts = service.getAllDepartments(); populateCards(allDepts); }
        catch (SQLException e) { showError("Load Error", e.getMessage()); }
    }

    private void filterCards(String q) {
        if (q == null || q.isBlank()) { populateCards(allDepts); return; }
        String lq = q.toLowerCase();
        populateCards(allDepts.stream().filter(d ->
            (d.getName()        != null && d.getName().toLowerCase().contains(lq)) ||
            (d.getDescription() != null && d.getDescription().toLowerCase().contains(lq))
        ).toList());
    }

    private void populateCards(List<Department> depts) {
        cardPane.getChildren().clear();
        if (depts.isEmpty()) {
            Label empty = new Label("No departments found.");
            empty.getStyleClass().add("label-muted"); empty.setPadding(new Insets(40));
            cardPane.getChildren().add(empty); return;
        }
        for (Department d : depts) cardPane.getChildren().add(buildCard(d));
    }

    private VBox buildCard(Department dept) {
        VBox card = new VBox(0);
        card.getStyleClass().add("data-card");
        card.setPrefWidth(260);

        // Header: colored icon + name
        HBox header = new HBox(12);
        header.getStyleClass().add("data-card-header");
        header.setAlignment(Pos.CENTER_LEFT);

        String color = DEPT_COLORS[Math.abs(dept.getName().hashCode()) % DEPT_COLORS.length];
        Label icon = new Label(dept.getName().substring(0, 1).toUpperCase());
        icon.setMinSize(44, 44); icon.setMaxSize(44, 44); icon.setAlignment(Pos.CENTER);
        icon.setStyle("-fx-background-color:" + color + ";-fx-background-radius:10;" +
            "-fx-text-fill:white;-fx-font-size:18px;-fx-font-weight:bold;");

        VBox titleBox = new VBox(3);
        Label nameLabel = new Label(dept.getName());
        nameLabel.getStyleClass().add("data-card-name");
        Label idLabel = new Label("ID #" + dept.getId());
        idLabel.getStyleClass().add("data-card-sub");
        titleBox.getChildren().addAll(nameLabel, idLabel);
        HBox.setHgrow(titleBox, Priority.ALWAYS);
        header.getChildren().addAll(icon, titleBox);

        // Body: description + headcount
        VBox body = new VBox(8);
        body.getStyleClass().add("data-card-body");

        String desc = dept.getDescription() != null && !dept.getDescription().isBlank()
            ? dept.getDescription() : "No description provided.";
        Label descLabel = new Label(desc);
        descLabel.getStyleClass().add("detail-value");
        descLabel.setWrapText(true);

        HBox hcRow = new HBox(8); hcRow.setAlignment(Pos.CENTER_LEFT);
        Label hcKey = new Label("Headcount:"); hcKey.getStyleClass().add("detail-label"); hcKey.setMinWidth(80);
        String hcText = dept.getMaxHeadcount() > 0
            ? dept.getCurrentHeadcount() + " / " + dept.getMaxHeadcount()
            : dept.getCurrentHeadcount() + " / (no limit set)";
        Label hcVal = new Label(hcText);
        hcVal.getStyleClass().add("detail-value");
        hcRow.getChildren().addAll(hcKey, hcVal);
        body.getChildren().addAll(descLabel, hcRow);

        // Footer
        HBox footer = new HBox(8);
        footer.getStyleClass().add("data-card-footer");
        footer.setAlignment(Pos.CENTER_RIGHT);
        Button editBtn = new Button("Edit");   editBtn.getStyleClass().add("btn-secondary");
        Button delBtn  = new Button("Delete"); delBtn.getStyleClass().add("btn-danger");
        editBtn.setOnAction(e -> showForm(dept));
        delBtn.setOnAction(e  -> handleDeleteCard(dept));
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        footer.getChildren().addAll(sp, editBtn, delBtn);

        card.getChildren().addAll(header, body, footer);
        return card;
    }

    @FXML private void handleAdd() { showForm(null); }

    private void handleDeleteCard(Department dept) {
        Alert c = new Alert(Alert.AlertType.CONFIRMATION,
            "Delete department \"" + dept.getName() + "\"?", ButtonType.YES, ButtonType.NO);
        c.setHeaderText(null);
        if (c.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try { service.deleteDepartment(dept.getId()); loadData(); }
            catch (IllegalStateException e) { showError("Cannot Delete", e.getMessage()); }
            catch (SQLException e)          { showError("Delete Error", e.getMessage()); }
        }
    }

    private void showForm(Department existing) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Department" : "Edit Department");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));

        TextField tfName     = new TextField();
        TextArea  taDesc     = new TextArea(); taDesc.setPrefRowCount(3);
        TextField tfMaxHead  = new TextField();
        tfMaxHead.setPromptText("e.g. 20");

        if (existing != null) {
            tfName.setText(existing.getName());
            taDesc.setText(existing.getDescription());
            tfMaxHead.setText(existing.getMaxHeadcount() > 0
                ? String.valueOf(existing.getMaxHeadcount()) : "");
        }

        grid.addRow(0, new Label("Name *:"),           tfName);
        grid.addRow(1, new Label("Description:"),      taDesc);
        grid.addRow(2, new Label("Max Headcount *:"),  tfMaxHead);
        grid.addRow(3, new Label(""),
            new Label("Maximum number of employees allowed in this department."));
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(420);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String maxStr = tfMaxHead.getText().trim();
            if (maxStr.isBlank() || !maxStr.matches("\\d+") || Integer.parseInt(maxStr) < 1) {
                showError("Validation Error", "Max Headcount must be a positive number (e.g. 10).");
                return;
            }
            try {
                Department dept = existing != null ? existing : new Department();
                dept.setName(tfName.getText().trim());
                dept.setDescription(taDesc.getText().trim());
                dept.setMaxHeadcount(Integer.parseInt(maxStr));
                if (existing == null) service.addDepartment(dept);
                else                  service.updateDepartment(dept);
                loadData();
            } catch (IllegalArgumentException | SQLException e) { showError("Save Error", e.getMessage()); }
        }
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setTitle(title); a.setHeaderText(null); a.showAndWait();
    }
}
