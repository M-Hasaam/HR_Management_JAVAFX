package com.hr.ui;

import com.hr.MainApp;
import com.hr.model.UserAccount;
import com.hr.service.SessionManager;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MainController {

    @FXML private StackPane contentArea;
    @FXML private VBox      sidebar;
    @FXML private HBox      brandBar;
    @FXML private VBox      brandText;
    @FXML private Button    btnCollapse;

    // User pill
    @FXML private HBox  userPill;
    @FXML private VBox  pillText;
    @FXML private Label lblUserAvatar;
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;

    // Section labels
    @FXML private Label lblSection1;
    @FXML private Label lblSection2;
    @FXML private Label lblSection3;
    @FXML private Label lblSection4;

    // Nav buttons
    @FXML private Button navEmployees;
    @FXML private Button navRegister;
    @FXML private Button navDepts;
    @FXML private Button navProfile;
    @FXML private Button navAttendance;
    @FXML private Button navLeave;
    @FXML private Button navDashboard;
    @FXML private Button navPerformance;
    @FXML private Button navProbation;
    @FXML private Button navOffboarding;
    @FXML private Button navPayroll;
    @FXML private Button navCompliance;
    @FXML private Button navAnalytics;

    private boolean sidebarCollapsed = false;
    private Button  activeNavBtn     = null;

    private static final double EXPANDED_WIDTH  = 232;
    private static final double COLLAPSED_WIDTH = 64;

    private static final String[] EXPANDED_TEXTS = {
        "  Employees", "  Register Employee", "  Departments", "  Employee Profile",
        "  Attendance", "  Leave Requests", "  Dashboard",
        "  Performance", "  Probation", "  Offboarding",
        "  Payroll", "  Compliance Reports", "  HR Analytics"
    };
    private static final String[] COLLAPSED_TEXTS = {
        "Emp", "Reg", "Dep", "Pfl", "Att", "Lve", "Dsh",
        "Prf", "Prb", "Off", "Pay", "Cmp", "Ana"
    };

    @FXML
    public void initialize() {
        applyRoleAccess();
        populateUserPill();

        // Default landing page by role
        SessionManager sm = SessionManager.getInstance();
        if (sm.isEmployee()) {
            loadModule("/com/hr/dashboard.fxml");
            setActiveNav(navDashboard);
        } else {
            loadModule("/com/hr/employee.fxml");
            setActiveNav(navEmployees);
        }
    }

    // ===== NAVIGATION =====
    @FXML private void showEmployees()        { loadModule("/com/hr/employee.fxml");          setActiveNav(navEmployees); }
    @FXML private void showRegisterEmployee() { loadModule("/com/hr/register_employee.fxml"); setActiveNav(navRegister);  }
    @FXML private void showDepartments()      { loadModule("/com/hr/department.fxml");         setActiveNav(navDepts);    }
    @FXML private void showPayroll()       { loadModule("/com/hr/payroll.fxml");            setActiveNav(navPayroll);     }
    @FXML private void showLeaveRequests() { loadModule("/com/hr/leave_request.fxml");      setActiveNav(navLeave);       }
    @FXML void showAttendance()            { loadModule("/com/hr/attendance.fxml");          setActiveNav(navAttendance);  }
    @FXML void showDashboard()             { loadModule("/com/hr/dashboard.fxml");           setActiveNav(navDashboard);   }
    @FXML void showPerformance()           { loadModule("/com/hr/performance.fxml");         setActiveNav(navPerformance); }
    @FXML void showProbation()             { loadModule("/com/hr/probation.fxml");           setActiveNav(navProbation);   }
    @FXML void showComplianceReports()     { loadModule("/com/hr/compliance_report.fxml");   setActiveNav(navCompliance);  }
    @FXML void showAnalytics()             { loadModule("/com/hr/analytics.fxml");           setActiveNav(navAnalytics);   }
    @FXML void showOffboarding()           { loadModule("/com/hr/offboarding.fxml");          setActiveNav(navOffboarding); }
    @FXML void showEmployeeProfile()       { loadModule("/com/hr/employee_profile.fxml");     setActiveNav(navProfile);     }

    // ===== SIDEBAR COLLAPSE =====
    @FXML
    private void toggleSidebar() {
        sidebarCollapsed = !sidebarCollapsed;
        double targetW  = sidebarCollapsed ? COLLAPSED_WIDTH : EXPANDED_WIDTH;
        double currentW = sidebar.getWidth() > 0 ? sidebar.getWidth() : EXPANDED_WIDTH;

        // Animate min + pref + max so BorderPane respects the change
        new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(sidebar.minWidthProperty(),  currentW),
                new KeyValue(sidebar.prefWidthProperty(), currentW),
                new KeyValue(sidebar.maxWidthProperty(),  currentW)
            ),
            new KeyFrame(Duration.millis(260),
                new KeyValue(sidebar.minWidthProperty(),  targetW, Interpolator.EASE_BOTH),
                new KeyValue(sidebar.prefWidthProperty(), targetW, Interpolator.EASE_BOTH),
                new KeyValue(sidebar.maxWidthProperty(),  targetW, Interpolator.EASE_BOTH)
            )
        ).play();

        btnCollapse.setText(sidebarCollapsed ? "►" : "◄");

        // Compact brand bar padding when collapsed so content fits in 64 px
        if (sidebarCollapsed) {
            brandBar.setStyle("-fx-padding: 14 6 14 6;");
        } else {
            brandBar.setStyle("-fx-padding: 14 10 14 14;");
        }

        for (Node n : Arrays.asList(brandText, pillText, lblSection1,
                lblSection2, lblSection3, lblSection4)) {
            n.setVisible(!sidebarCollapsed);
            n.setManaged(!sidebarCollapsed);
        }

        List<Button> btns = navButtons();
        for (int i = 0; i < btns.size(); i++) {
            btns.get(i).setText(sidebarCollapsed ? COLLAPSED_TEXTS[i] : EXPANDED_TEXTS[i]);
            btns.get(i).setAlignment(sidebarCollapsed ? Pos.CENTER : Pos.CENTER_LEFT);
        }
    }

    // ===== LOGOUT =====
    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        try {
            Stage stage = (Stage) contentArea.getScene().getWindow();
            MainApp.showLogin(stage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ===== ROLE-BASED SIDEBAR =====
    private void applyRoleAccess() {
        SessionManager sm = SessionManager.getInstance();
        if (sm.isAdmin()) return; // sees everything

        if (sm.isHR()) {
            // HR does not get Employee Profile (self-service only)
            setVisible(navProfile, false);
            return;
        }

        // Employee: very limited access
        setVisible(navEmployees,   false);
        setVisible(navRegister,    false);
        setVisible(navDepts,       false);
        setVisible(navAttendance,  false);
        setVisible(navPerformance, false);
        setVisible(navProbation,   false);
        setVisible(navOffboarding, false);
        setVisible(navPayroll,     false);
        setVisible(navCompliance,  false);
        setVisible(navAnalytics,   false);
        setVisible(lblSection1,    false);
        setVisible(lblSection3,    false);
        setVisible(lblSection4,    false);
    }

    private void populateUserPill() {
        UserAccount user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;
        String name = user.getUsername();
        lblUserAvatar.setText(name.substring(0, 1).toUpperCase());
        lblUserName.setText(name);
        lblUserRole.setText(user.getRole());

        // Color the avatar and role badge by role
        String role = user.getRole();
        String avatarColor = switch (role) {
            case "Admin"    -> "#7c3aed";
            case "HR"       -> "#2563eb";
            default         -> "#10b981";
        };
        lblUserAvatar.setStyle("-fx-background-color: " + avatarColor + ";");

        String badgeStyle = switch (role) {
            case "Admin"    -> "-fx-background-color: #ede9fe; -fx-text-fill: #6d28d9;";
            case "HR"       -> "-fx-background-color: #dbeafe; -fx-text-fill: #1d4ed8;";
            default         -> "-fx-background-color: #d1fae5; -fx-text-fill: #065f46;";
        };
        lblUserRole.setStyle(lblUserRole.getStyle() + badgeStyle);
    }

    private void setVisible(Node n, boolean v) {
        n.setVisible(v);
        n.setManaged(v);
    }

    private void loadModule(String fxmlPath) {
        try {
            java.net.URL url = getClass().getResource(fxmlPath);
            if (url == null) throw new IllegalArgumentException("FXML not found: " + fxmlPath);
            FXMLLoader loader = new FXMLLoader(url);
            Node content = loader.load();
            content.setOpacity(0);
            content.setTranslateY(10);
            contentArea.getChildren().setAll(content);
            new Timeline(new KeyFrame(Duration.millis(220),
                new KeyValue(content.opacityProperty(),    1, Interpolator.EASE_OUT),
                new KeyValue(content.translateYProperty(), 0, Interpolator.EASE_OUT)
            )).play();
        } catch (Exception e) {
            e.printStackTrace();
            javafx.scene.control.Alert a = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR, e.getMessage(),
                javafx.scene.control.ButtonType.OK);
            a.setTitle("Module Load Error");
            a.setHeaderText(null);
            a.showAndWait();
        }
    }

    private void setActiveNav(Button btn) {
        if (activeNavBtn != null) activeNavBtn.getStyleClass().remove("nav-button-active");
        activeNavBtn = btn;
        if (btn != null) btn.getStyleClass().add("nav-button-active");
    }

    private List<Button> navButtons() {
        return Arrays.asList(navEmployees, navRegister, navDepts, navProfile,
            navAttendance, navLeave, navDashboard,
            navPerformance, navProbation, navOffboarding,
            navPayroll, navCompliance, navAnalytics);
    }
}
