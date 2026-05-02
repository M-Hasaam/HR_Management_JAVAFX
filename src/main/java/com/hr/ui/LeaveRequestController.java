package com.hr.ui;

import com.hr.controller.LeaveApprovalController;
import com.hr.controller.LeaveController;
import com.hr.model.Employee;
import com.hr.model.LeaveRequest;
import com.hr.service.EmployeeService;
import com.hr.service.LeaveRequestService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class LeaveRequestController {

    @FXML private FlowPane       cardPane;
    @FXML private ComboBox<String> cbFilter;
    @FXML private Label          lblPending;
    @FXML private Label          lblApproved;
    @FXML private Label          lblRejected;

    private LeaveRequestService    leaveService;
    private EmployeeService        employeeService;
    private LeaveController        leaveController;
    private LeaveApprovalController leaveApprovalController;

    private List<LeaveRequest> allRequests = new ArrayList<>();

    @FXML
    public void initialize() {
        try {
            leaveService    = new LeaveRequestService();
            employeeService = new EmployeeService();
        } catch (SQLException e) { showError("Database Error", e.getMessage()); return; }

        try { leaveController         = new LeaveController();         } catch (SQLException ignored) {}
        try { leaveApprovalController = new LeaveApprovalController(); } catch (SQLException ignored) {}

        cbFilter.setItems(FXCollections.observableArrayList(
            "All", "PENDING", "PENDING_DOCUMENT", "APPROVED", "REJECTED"));
        cbFilter.setValue("All");
        cbFilter.setOnAction(e -> filterCards(cbFilter.getValue()));

        loadData();
    }

    private void loadData() {
        try {
            allRequests = leaveService.getAllRequests();
            updateStatsBar(allRequests);
            populateCards(allRequests);
        } catch (SQLException e) { showError("Load Error", e.getMessage()); }
    }

    private void filterCards(String status) {
        if (status == null || "All".equals(status)) { populateCards(allRequests); return; }
        populateCards(allRequests.stream()
            .filter(r -> status.equalsIgnoreCase(r.getStatus())).toList());
    }

    private void updateStatsBar(List<LeaveRequest> list) {
        long pending  = list.stream().filter(r ->
            "PENDING".equalsIgnoreCase(r.getStatus()) ||
            "PENDING_DOCUMENT".equalsIgnoreCase(r.getStatus())).count();
        long approved = list.stream().filter(r -> "APPROVED".equalsIgnoreCase(r.getStatus())).count();
        long rejected = list.stream().filter(r -> "REJECTED".equalsIgnoreCase(r.getStatus())).count();
        lblPending.setText(pending  + " Pending");
        lblApproved.setText(approved + " Approved");
        lblRejected.setText(rejected + " Rejected");
    }

    private void populateCards(List<LeaveRequest> requests) {
        cardPane.getChildren().clear();
        if (requests.isEmpty()) {
            Label empty = new Label("No leave requests found.");
            empty.getStyleClass().add("label-muted"); empty.setPadding(new Insets(40));
            cardPane.getChildren().add(empty); return;
        }
        for (LeaveRequest r : requests) cardPane.getChildren().add(buildCard(r));
    }

    private VBox buildCard(LeaveRequest req) {
        VBox card = new VBox(0);
        card.getStyleClass().add("data-card");
        card.setPrefWidth(320);

        // ── Header ────────────────────────────────────────────────────────────
        HBox header = new HBox(10);
        header.getStyleClass().add("data-card-header");
        header.setAlignment(Pos.CENTER_LEFT);

        String typeColor = switch (nvl(req.getLeaveType(), "")) {
            case "ANNUAL"   -> "#2563eb";
            case "SICK"     -> "#10b981";
            case "PERSONAL" -> "#7c3aed";
            default         -> "#6b7280";
        };
        Label typeIcon = new Label(req.getLeaveType() != null ? req.getLeaveType().substring(0, 1) : "L");
        typeIcon.setMinSize(40, 40); typeIcon.setMaxSize(40, 40);
        typeIcon.setAlignment(Pos.CENTER);
        typeIcon.setStyle("-fx-background-color:" + typeColor + ";-fx-background-radius:10;" +
            "-fx-text-fill:white;-fx-font-size:16px;-fx-font-weight:bold;");

        VBox nameBox = new VBox(2);
        Label empName  = new Label(nvl(req.getEmployeeName(), "Unknown Employee"));
        empName.getStyleClass().add("data-card-name");
        Label typeLabel = new Label(nvl(req.getLeaveType(), "LEAVE"));
        typeLabel.getStyleClass().add("data-card-sub");
        nameBox.getChildren().addAll(empName, typeLabel);
        HBox.setHgrow(nameBox, Priority.ALWAYS);

        String status = nvl(req.getStatus(), "PENDING");
        Label statusBadge = new Label("PENDING_DOCUMENT".equals(status) ? "PENDING DOC" : status);
        statusBadge.getStyleClass().add(switch (status) {
            case "APPROVED"        -> "badge-active";
            case "REJECTED"        -> "badge-inactive";
            case "PENDING_DOCUMENT"-> "badge-pending";
            default                -> "badge-pending";
        });
        header.getChildren().addAll(typeIcon, nameBox, statusBadge);

        // ── Body ──────────────────────────────────────────────────────────────
        VBox body = new VBox(8);
        body.getStyleClass().add("data-card-body");
        String dateRange = (req.getStartDate() != null ? req.getStartDate().toString() : "?")
            + "  →  " + (req.getEndDate() != null ? req.getEndDate().toString() : "?");
        body.getChildren().addAll(
            detailRow("Dates",   dateRange),
            detailRow("Days",    req.getDaysRequested() + " working day(s)"),
            detailRow("Applied", req.getAppliedDate() != null ? req.getAppliedDate().toString() : "—"),
            detailRow("Reason",  nvl(req.getReason(), "—"))
        );
        if (req.getDocumentPath() != null && !req.getDocumentPath().isBlank())
            body.getChildren().add(detailRow("Doc Ref", req.getDocumentPath()));
        if (req.getApprovedBy() != null && !req.getApprovedBy().isBlank()) {
            body.getChildren().add(detailRow("Reviewed by", req.getApprovedBy()));
            if (req.getComments() != null && !req.getComments().isBlank())
                body.getChildren().add(detailRow("Comment", req.getComments()));
        }

        // ── Footer ────────────────────────────────────────────────────────────
        boolean canReview = com.hr.service.SessionManager.getInstance().isAdmin()
                         || com.hr.service.SessionManager.getInstance().isHR();

        HBox footer = new HBox(8);
        footer.getStyleClass().add("data-card-footer");
        footer.setAlignment(Pos.CENTER_RIGHT);
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        footer.getChildren().add(sp);

        if (canReview && "PENDING".equals(status)) {
            Button approveBtn = new Button("Approve"); approveBtn.getStyleClass().add("btn-success-sm");
            Button rejectBtn  = new Button("Reject");  rejectBtn.getStyleClass().add("btn-warning-sm");
            approveBtn.setOnAction(e -> handleApproveCard(req));
            rejectBtn.setOnAction(e  -> handleRejectCard(req));
            footer.getChildren().addAll(approveBtn, rejectBtn);
        } else if ("PENDING_DOCUMENT".equals(status)) {
            // Any user can submit their own document reference
            Button docBtn = new Button("Submit Doc"); docBtn.getStyleClass().add("btn-primary-sm");
            docBtn.setOnAction(e -> handleSubmitDocument(req));
            footer.getChildren().add(docBtn);
            if (canReview) {
                Button rejectBtn = new Button("Reject"); rejectBtn.getStyleClass().add("btn-warning-sm");
                rejectBtn.setOnAction(e -> handleRejectCard(req));
                footer.getChildren().add(rejectBtn);
            }
        }

        if (canReview) {
            Button delBtn = new Button("Delete"); delBtn.getStyleClass().add("btn-danger-sm");
            delBtn.setOnAction(e -> handleDeleteCard(req));
            footer.getChildren().add(delBtn);
        }

        card.getChildren().addAll(header, body, footer);
        return card;
    }

    // ── Approve / Reject ──────────────────────────────────────────────────────

    private void handleApproveCard(LeaveRequest req) {
        Optional<String> comment = promptComment("Approve Leave", "Enter approval comment (min 10 chars):");
        if (comment.isEmpty()) return;
        try {
            if (leaveApprovalController != null)
                leaveApprovalController.processLeaveDecision(req.getId(), "APPROVED", comment.get());
            else leaveService.approveRequest(req);
            loadData();
            showInfo("Leave approved successfully.");
        } catch (IllegalArgumentException e) { showError("Comment Error", e.getMessage()); }
        catch (IllegalStateException | SQLException e) { showError("Approve Error", e.getMessage()); }
    }

    private void handleRejectCard(LeaveRequest req) {
        Optional<String> comment = promptComment("Reject Leave", "Enter rejection reason (min 10 chars):");
        if (comment.isEmpty()) return;
        try {
            if (leaveApprovalController != null)
                leaveApprovalController.processLeaveDecision(req.getId(), "REJECTED", comment.get());
            else leaveService.rejectRequest(req);
            loadData();
        } catch (IllegalArgumentException e) { showError("Comment Error", e.getMessage()); }
        catch (IllegalStateException | SQLException e) { showError("Reject Error", e.getMessage()); }
    }

    /** 3b: Collect document reference and move PENDING_DOCUMENT → PENDING. */
    private void handleSubmitDocument(LeaveRequest req) {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("Submit Document Reference");
        dlg.setHeaderText("Sick leave > 2 days requires a medical certificate.");
        dlg.setContentText("Enter certificate number or document reference:");
        dlg.showAndWait().ifPresent(ref -> {
            if (ref.isBlank()) { showError("Input Error", "Document reference cannot be empty."); return; }
            try {
                leaveService.submitDocument(req.getId(), ref.trim());
                loadData();
                showInfo("Document reference submitted. Request is now PENDING HR approval.");
            } catch (IllegalArgumentException | IllegalStateException ex) {
                showError("Document Error", ex.getMessage());
            } catch (SQLException ex) {
                showError("Database Error", ex.getMessage());
            }
        });
    }

    private void handleDeleteCard(LeaveRequest req) {
        Alert c = new Alert(Alert.AlertType.CONFIRMATION,
            "Delete this leave request?", ButtonType.YES, ButtonType.NO);
        c.setHeaderText(null);
        if (c.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try { leaveService.deleteRequest(req.getId()); loadData(); }
            catch (SQLException e) { showError("Delete Error", e.getMessage()); }
        }
    }

    // ── Apply for Leave dialog ────────────────────────────────────────────────

    @FXML
    private void handleApply() {
        List<Employee> employees;
        try { employees = employeeService.getAllEmployees(); }
        catch (SQLException e) { showError("Load Error", e.getMessage()); return; }

        // ── Dialog ────────────────────────────────────────────────────────────
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Apply for Leave");
        dialog.setResizable(true);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.OK)).setText("Submit Request");

        // ── Controls ──────────────────────────────────────────────────────────
        ComboBox<Employee> cbEmp = new ComboBox<>(FXCollections.observableArrayList(employees));
        cbEmp.setPromptText("Select employee…");
        cbEmp.setPrefWidth(270);
        cbEmp.setConverter(new StringConverter<>() {
            @Override public String toString(Employee e) { return e != null ? e.getFullName() + " (ID:" + e.getId() + ")" : ""; }
            @Override public Employee fromString(String s) { return null; }
        });

        ComboBox<String> cbType = new ComboBox<>(
            FXCollections.observableArrayList("ANNUAL", "SICK", "PERSONAL"));
        cbType.setPromptText("Select leave type…");
        cbType.setPrefWidth(270);

        DatePicker dpStart = new DatePicker(LocalDate.now());
        DatePicker dpEnd   = new DatePicker(LocalDate.now());

        TextArea taReason = new TextArea();
        taReason.setPrefRowCount(2);
        taReason.setPromptText("Optional reason…");
        taReason.setPrefWidth(270);

        TextField tfDocRef = new TextField();
        tfDocRef.setPromptText("Certificate No. or reference (required for SICK > 2 days)…");
        tfDocRef.setPrefWidth(270);

        // ── Live-feedback info panel ──────────────────────────────────────────
        Label lblDays      = new Label("Select dates to see working day count.");
        Label lblBalance   = new Label("Select an employee to view leave balance.");
        Label lblHoliday   = new Label();  // 2b
        Label lblOverlap   = new Label();  // 2c
        Label lblProbation = new Label();  // 3c
        Label lblDocNote   = new Label();  // 3b

        lblDays.setStyle("-fx-text-fill: #374151;");
        lblBalance.setStyle("-fx-text-fill: #1d4ed8;");
        lblHoliday.setStyle("-fx-text-fill: #b45309; -fx-font-weight: bold;");
        lblOverlap.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
        lblProbation.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
        lblDocNote.setStyle("-fx-text-fill: #7c3aed;");

        for (Label l : new Label[]{lblHoliday, lblOverlap, lblProbation, lblDocNote})
            l.setWrapText(true);

        VBox infoPanel = new VBox(6, lblDays, lblBalance, lblHoliday, lblOverlap, lblProbation, lblDocNote);
        infoPanel.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#e2e8f0; " +
                           "-fx-border-radius:6; -fx-background-radius:6; -fx-padding:10;");

        // Doc-ref row visibility is toggled in recalc
        Label lblDocLabel = new Label("Doc. Reference:");

        // ── Grid layout ───────────────────────────────────────────────────────
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.setMinWidth(520);

        ColumnConstraints c0 = new ColumnConstraints(110);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c0, c1);

        grid.addRow(0, new Label("Employee:*"),   cbEmp);
        grid.addRow(1, new Label("Leave Type:*"), cbType);
        grid.addRow(2, new Label("Start Date:*"), dpStart);
        grid.addRow(3, new Label("End Date:*"),   dpEnd);

        GridPane.setColumnSpan(infoPanel, 2);
        grid.add(infoPanel, 0, 4);

        // Doc-ref row (shown only for SICK)
        grid.addRow(5, lblDocLabel, tfDocRef);
        lblDocLabel.setVisible(false); lblDocLabel.setManaged(false);
        tfDocRef.setVisible(false);    tfDocRef.setManaged(false);

        grid.addRow(6, new Label("Reason:"), taReason);

        dialog.getDialogPane().setContent(grid);

        // ── Live recalculate ──────────────────────────────────────────────────
        Runnable recalc = () -> {
            Employee emp  = cbEmp.getValue();
            String   type = cbType.getValue();
            LocalDate s   = dpStart.getValue();
            LocalDate e   = dpEnd.getValue();

            lblHoliday.setText(""); lblOverlap.setText("");
            lblProbation.setText(""); lblDocNote.setText("");

            // Doc-ref visibility (always show for SICK regardless of dates)
            boolean isSick = "SICK".equals(type);
            lblDocLabel.setVisible(isSick); lblDocLabel.setManaged(isSick);
            tfDocRef.setVisible(isSick);    tfDocRef.setManaged(isSick);

            if (s == null || e == null) {
                lblDays.setText("Select start and end dates.");
                return;
            }
            if (e.isBefore(s)) {
                lblDays.setStyle("-fx-text-fill: #dc2626;");
                lblDays.setText("End date must be on or after start date.");
                return;
            }
            if (s.isBefore(LocalDate.now())) {
                lblDays.setStyle("-fx-text-fill: #dc2626;");
                lblDays.setText("⚠ Start date is in the past.");
                return;
            }

            lblDays.setStyle("-fx-text-fill: #374151;");

            try {
                // 2b: holiday check
                Map<LocalDate, String> holidays = leaveService.getHolidaysInRange(s, e);
                int workingDays = LeaveRequestService.calculateWorkingDays(s, e, holidays.keySet());

                if (!holidays.isEmpty()) {
                    lblHoliday.setText("⚠ Public holiday conflict: "
                        + String.join(", ", holidays.values())
                        + "\nAdjust your dates — submission will be blocked.");
                    lblDays.setText("Working days (excl. holidays): " + workingDays);
                } else {
                    lblDays.setText("Working days: " + workingDays);
                }

                // Balance + 2c + 3c checks (need employee selected)
                if (emp != null) {
                    int[] bal = leaveService.getLeaveBalance(emp.getId());
                    int avail = (type == null) ? -1 : switch (type) {
                        case "ANNUAL" -> bal[0]; case "SICK" -> bal[1]; case "PERSONAL" -> bal[2];
                        default -> 0;
                    };
                    String balText = String.format(
                        "Balance — Annual: %d  |  Sick: %d  |  Personal: %d", bal[0], bal[1], bal[2]);
                    if (type != null && workingDays > avail && avail >= 0) {
                        lblBalance.setStyle("-fx-text-fill: #dc2626;");
                        lblBalance.setText(balText +
                            "  ⚠ Insufficient (need " + workingDays + ", have " + avail + ")");
                    } else {
                        lblBalance.setStyle("-fx-text-fill: #1d4ed8;");
                        lblBalance.setText(balText);
                    }

                    // 2c: overlap
                    List<LeaveRequest> overlaps = leaveService.getOverlappingRequests(emp.getId(), s, e);
                    if (!overlaps.isEmpty()) {
                        LeaveRequest o = overlaps.get(0);
                        lblOverlap.setText("⚠ Overlaps with existing " + o.getStatus() + " leave ("
                            + o.getStartDate() + " → " + o.getEndDate()
                            + ") — submission will be blocked.");
                    }

                    // 3c: probation
                    LocalDate probEnd = leaveService.getEmployeeProbationEndDate(emp.getId());
                    if (probEnd != null && !LocalDate.now().isAfter(probEnd) && type != null) {
                        if ("ANNUAL".equals(type) || "PERSONAL".equals(type)) {
                            lblProbation.setText("⚠ " + type.charAt(0)
                                + type.substring(1).toLowerCase()
                                + " leave is not permitted during probation (ends: " + probEnd
                                + ") — submission will be blocked.");
                        }
                    }
                }

                // 3b: document note
                if (isSick && workingDays > 2) {
                    lblDocNote.setText("ℹ Medical certificate required (> 2 days sick leave). "
                        + "Provide the document reference below, or the request will be held "
                        + "as PENDING_DOCUMENT until you submit it.");
                }

            } catch (SQLException ex) {
                lblDays.setText("Cannot calculate: " + ex.getMessage());
            }
        };

        // ── Wire listeners ────────────────────────────────────────────────────
        cbEmp.setOnAction(ev  -> recalc.run());
        cbType.setOnAction(ev -> recalc.run());
        dpStart.valueProperty().addListener((obs, old, val) -> recalc.run());
        dpEnd.valueProperty().addListener((obs, old, val)   -> recalc.run());
        recalc.run(); // initial state

        // ── Handle submission ─────────────────────────────────────────────────
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        Employee emp = cbEmp.getValue();
        if (emp == null || cbType.getValue() == null) {
            showError("Input Error", "Employee and leave type are required.");
            return;
        }
        if (dpStart.getValue() == null || dpEnd.getValue() == null) {
            showError("Input Error", "Start and end dates are required.");
            return;
        }

        try {
            LeaveRequestService.SubmitResult sr;
            if (leaveController != null) {
                sr = leaveController.submitLeaveRequest(
                    emp.getId(), cbType.getValue(),
                    dpStart.getValue(), dpEnd.getValue(),
                    taReason.getText().trim(), tfDocRef.getText().trim());
            } else {
                sr = leaveService.applyForLeave(
                    emp.getId(), cbType.getValue(),
                    dpStart.getValue(), dpEnd.getValue(),
                    taReason.getText().trim(), tfDocRef.getText().trim());
            }
            loadData();

            // Post-submit feedback per alternative flow
            if ("PENDING_DOCUMENT".equals(sr.request().getStatus())) {
                showInfo("Request Held — Pending Document\n\n"
                    + "Your sick leave (" + sr.request().getDaysRequested() + " days) requires "
                    + "a medical certificate.\n"
                    + "The request is on hold. Click \"Submit Doc\" on the card once you have "
                    + "your certificate reference to move it to pending approval.");
            } else if (sr.notificationQueued()) {
                showInfo("Leave request submitted.\n\n"
                    + "Note: HR notification is queued (SMTP temporarily unavailable) "
                    + "and will be delivered automatically.");  // 3a
            } else {
                showInfo("Leave request submitted successfully.\n"
                    + sr.request().getDaysRequested() + " working day(s) reserved.\n"
                    + "HR has been notified.");
            }

        } catch (IllegalArgumentException | IllegalStateException ex) {
            showError("Leave Request Blocked", ex.getMessage());
        } catch (SQLException ex) {
            showError("Database Error", ex.getMessage());
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private HBox detailRow(String label, String value) {
        HBox row = new HBox(8); row.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(label + ":"); lbl.getStyleClass().add("detail-label"); lbl.setMinWidth(64);
        Label val = new Label(value); val.getStyleClass().add("detail-value");
        val.setWrapText(true); HBox.setHgrow(val, Priority.ALWAYS);
        row.getChildren().addAll(lbl, val); return row;
    }

    private Optional<String> promptComment(String title, String prompt) {
        TextInputDialog d = new TextInputDialog();
        d.setTitle(title); d.setHeaderText(null); d.setContentText(prompt);
        return d.showAndWait();
    }

    private String nvl(String s, String fb) { return (s != null && !s.isBlank()) ? s : fb; }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setTitle(title); a.setHeaderText(null); a.showAndWait();
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setTitle("Leave Request"); a.setHeaderText(null); a.showAndWait();
    }
}
