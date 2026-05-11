# Smart HR Operations & Workforce Management System
## Demo Preparation — Deep-Dive Reference

> **Course:** SE2002 — Software Design & Architecture (SDA)
> **Institution:** FAST NUCES, Spring 2026 — Group 16
> **Instructor:** Ms. Laiba Imran
> **Tech Stack:** Java 21 · JavaFX 21 · MySQL 8.0.33 · Maven · IntelliJ IDEA
> **Architecture:** 3-Layer Layered (Presentation → Business Logic → Data Access) + Domain Model

This document is a single-source, demo-day reference covering **every architecturally significant corner** of the project: file layout, OOP concepts, SOLID, GoF design patterns, GRASP responsibilities, layered architecture, all 15 use cases mapped to code, NFRs, the database schema, ASCII UML, and a Q&A bank for the viva.

---

## Table of Contents
1. [Project Overview](#1-project-overview)
2. [Project Structure & Package Layout](#2-project-structure--package-layout)
3. [Layered Architecture](#3-layered-architecture)
4. [OOP Principles — Deep Evidence](#4-oop-principles--deep-evidence)
5. [SOLID Principles](#5-solid-principles)
6. [Gang of Four (GoF) Design Patterns](#6-gang-of-four-gof-design-patterns)
7. [GRASP Patterns](#7-grasp-patterns)
8. [Use Cases — All 15 Mapped to Code](#8-use-cases--all-15-mapped-to-code)
9. [Non-Functional Requirements (NFRs)](#9-non-functional-requirements-nfrs)
10. [Database Schema Overview](#10-database-schema-overview)
11. [ASCII Class & Sequence Diagrams](#11-ascii-class--sequence-diagrams)
12. [End-to-End Flow Walkthrough — UC-06](#12-end-to-end-flow-walkthrough--uc-06)
13. [Authentication, Sessions & Role-Based Access](#13-authentication-sessions--role-based-access)
14. [Demo-Day Q&A Bank](#14-demo-day-qa-bank)

---

## 1. Project Overview

The **Smart HR Operations & Workforce Management System** is a desktop application (JavaFX UI + MySQL backend) that automates the full HR lifecycle: hiring → leave management → attendance → performance evaluation → compliance reporting → offboarding.

| Aspect | Detail |
|---|---|
| Use cases | **15 fully implemented** (UC-01 … UC-15) |
| Modules | 3 (Employee Mgmt, Leave & Attendance, Performance & Compliance) |
| Roles | Admin / HR / Employee (role-based UI) |
| OOP principles | Encapsulation, Inheritance, Polymorphism, Abstraction (all four) |
| GoF patterns | Singleton, Strategy, Observer, Factory Method, Adapter (all five) |
| GRASP patterns | Controller, Information Expert, Creator, Low Coupling, High Cohesion, Pure Fabrication, Protected Variation, Indirection (8) |
| NFRs explicit in code | NFR-1 Performance (3 s SLA), NFR-2 Data Retention (5-year archive) |
| DB tables | 17 (departments, employees, leave_requests, payroll, audit_log, …) |

The system uses a **3-tier layered architecture** with a clear separation between JavaFX UI controllers (`com.hr.ui`) and **GRASP system-operation controllers** (`com.hr.controller`) — this distinction is one of the most-asked points in viva.

---

## 2. Project Structure & Package Layout

```
D:\CODE\Projects\University_Projects\SDA
├── pom.xml                              # Maven build (Java 21, JavaFX 21, MySQL 8.0.33)
├── database\
│   ├── schema.sql                       # Full DDL — 17 tables
│   └── migrate_uc06.sql                 # Incremental migration (leave fields)
├── docs\
│   ├── Project_Report_Final.md / .docx  # Final SDA report
│   ├── DEMO_PREP_DEEP_DIVE.md           # ← THIS FILE
│   └── HR_SYSTEM_IMPLEMENTATION_REFERENCE.md
└── src\main\
    ├── java\com\hr\
    │   ├── MainApp.java                 # JavaFX entry point — boots login.fxml first
    │   ├── ui\                          # Presentation layer — JavaFX @FXML controllers
    │   │     ├── LoginController.java
    │   │     ├── MainController.java          (sidebar, role-based nav, collapse)
    │   │     ├── EmployeeController.java
    │   │     ├── RegisterEmployeeController.java
    │   │     ├── UpdateEmployeeController.java
    │   │     ├── AssignDepartmentController.java
    │   │     ├── DepartmentController.java
    │   │     ├── ProfileScreenController.java
    │   │     ├── OffboardingScreenController.java
    │   │     ├── LeaveRequestController.java   (FlowPane card UI)
    │   │     ├── AttendanceController.java
    │   │     ├── CorrectionController.java
    │   │     ├── DashboardController.java
    │   │     ├── EvaluationController.java
    │   │     ├── PayrollController.java
    │   │     ├── ReportController.java         (compliance)
    │   │     └── AnalyticsController.java
    │   ├── controller\                  # Application layer — GRASP SD Controllers
    │   │     ├── RegisterController.java        (UC-01)
    │   │     ├── UpdateController.java           (UC-02 + Observer publisher)
    │   │     ├── AssignmentController.java       (UC-03)
    │   │     ├── OffboardingController.java      (UC-04)
    │   │     ├── ProfileController.java          (UC-05)
    │   │     ├── LeaveController.java            (UC-06)
    │   │     ├── LeaveApprovalController.java    (UC-07)
    │   │     ├── AttendanceRecordController.java (UC-08)
    │   │     ├── AttendanceCorrectionController.java (UC-09)
    │   │     ├── LeaveDashboardController.java   (UC-10)
    │   │     ├── EvaluationCycleController.java  (UC-11)
    │   │     ├── PerformanceEvalController.java  (UC-12)
    │   │     ├── ProbationMonitorController.java (UC-13)
    │   │     ├── ComplianceReportController.java (UC-14)
    │   │     └── HRAnalyticsController.java      (UC-15)
    │   ├── service\                     # Business layer — services + GoF classes
    │   │     ├── (core)            EmployeeService, EmployeeRepository, DepartmentService,
    │   │     │                     LeaveRequestService, PayrollService, AuthService,
    │   │     │                     SessionManager (Singleton), NotificationService,
    │   │     │                     AuditLogService, AccessControlService
    │   │     ├── (Strategy)        ReportFormatStrategy, PdfFormatStrategy,
    │   │     │                     ExcelFormatStrategy, CsvFormatStrategy, ReportGenerator
    │   │     ├── (Observer)        HREventObserver, HREventPublisher,
    │   │     │                     AuditLogObserver, NotificationObserver
    │   │     ├── (Factory Method)  ReportCreator, ComplianceReportCreator,
    │   │     │                     AnalyticsReportCreator
    │   │     ├── (Adapter)         PayrollIntegration, ExternalPayrollSystem,
    │   │     │                     PayrollSystemAdapter, PayrollNotifier
    │   │     ├── (NFR)             PerformanceMonitor (NFR-1), ReportArchive (NFR-2)
    │   │     └── (helpers)         ComplianceDataAgg, AnalyticsAggregator,
    │   │                           DataVisualization, SecureStorage, TaskScheduler
    │   ├── dao\                     # Persistence layer
    │   │     ├── DatabaseConnection.java         (Singleton)
    │   │     ├── EmployeeDAO, DepartmentDAO, DesignationDAO, UserAccountDAO,
    │   │     ├── LeaveRequestDAO, AttendanceRecordDAO, AttendanceCorrectionDAO,
    │   │     ├── PayrollDAO, OffboardingWorkflowDAO, EmployeeAssignmentDAO,
    │   │     ├── EvaluationCycleDAO, PerformanceEvaluationDAO, ProbationRecordDAO,
    │   │     ├── ComplianceReportDAO, HRAnalyticsReportDAO, AuditLogDAO
    │   ├── model\                   # Domain layer — entity classes
    │   │     ├── Employee, Department, Designation, UserAccount, Payroll,
    │   │     ├── LeaveRequest, AttendanceRecord, AttendanceCorrectionRequest,
    │   │     ├── OffboardingWorkflow, EmployeeAssignment, EvaluationCycle,
    │   │     ├── PerformanceEvaluation, ProbationRecord
    │   │     └── Report (abstract) ← ComplianceReport, HRAnalyticsReport
    │   └── util\
    │         └── DemoUserSetup.java   (one-time DB seeder for admin/hr/emp users)
    └── resources\
          ├── database.properties          (URL, user, password)
          ├── styles.css                   (modern dark UI styling)
          └── com\hr\*.fxml                (20 FXML view files)
```

---

## 3. Layered Architecture

```
┌──────────────────────────────────────────────────────────────────────────┐
│                       PRESENTATION LAYER                                 │
│   com.hr.ui  +  src/main/resources/com/hr/*.fxml                         │
│   JavaFX @FXML controllers — events, validation, FlowPane card UI        │
│   Examples:  LeaveRequestController, EmployeeController, MainController  │
└────────────────────────────┬─────────────────────────────────────────────┘
                             │ delegates user intent
                             ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                APPLICATION / SD CONTROLLER LAYER                         │
│   com.hr.controller  — *no* @FXML, plain-Java GRASP Controllers          │
│   One controller per use case (UC-01 … UC-15)                            │
│   Orchestrates: validation → service calls → publish events → return DTO │
└────────────────────────────┬─────────────────────────────────────────────┘
                             │ depends on services (interfaces / services)
                             ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                     BUSINESS / SERVICE LAYER                             │
│   com.hr.service  — domain rules, workflows, GoF pattern classes         │
│   Examples: LeaveRequestService, EmployeeService, ReportGenerator,       │
│             AuthService, HREventPublisher, PayrollSystemAdapter          │
└────────────────────────────┬─────────────────────────────────────────────┘
                             │ delegates persistence
                             ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                  PERSISTENCE / DAO LAYER                                 │
│   com.hr.dao   — JDBC PreparedStatements, no business rules              │
│   DatabaseConnection (Singleton) → MySQL `hr_management` (17 tables)     │
└────────────────────────────┬─────────────────────────────────────────────┘
                             ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                       DOMAIN / MODEL LAYER                               │
│   com.hr.model  — pure POJO entity classes (id, getters, setters)        │
│   Plus the abstract Report class — base for ComplianceReport / HRAnalyticsReport
└──────────────────────────────────────────────────────────────────────────┘
```

**Why two controller layers?** The MVC `ui/*Controller.java` classes deal with JavaFX widgets (tables, forms, animations). The GRASP system-operation controllers in `controller/*Controller.java` handle the **use-case logic** (validation, orchestration, transactions). This deliberate separation keeps UI code free of business rules and lets the use-case logic be unit-testable without a JavaFX runtime.

---

## 4. OOP Principles — Deep Evidence

All four pillars are implemented with file + line citations.

### 4.1 Encapsulation

Every entity class declares its state `private` and exposes accessor methods. Examples:

| Class | File | Private fields | Sample accessor |
|---|---|---|---|
| `Employee` | `model/Employee.java` | L7–26 (id, firstName, email, basicSalary, hireDate…) | L72–73 `getId()/setId()` |
| `Department` | `model/Department.java` | L4–12 (id, name, managerId, maxHeadcount, currentHeadcount) | L33–34, L44–47 |
| `LeaveRequest` | `model/LeaveRequest.java` | L6–20 (employeeId, leaveType, startDate, status…) | L60–61, L74–75 |
| `PerformanceEvaluation` | `model/PerformanceEvaluation.java` | L6–14 (id, evaluatorId, aggregateScore, status…) | L41–42, L59–60 |

```java
// model/Employee.java — L7–26
public class Employee {
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private BigDecimal basicSalary;
    private LocalDate hireDate;
    private String status;
    // ...
    public BigDecimal getBasicSalary() { return basicSalary; }                // L88
    public void setBasicSalary(BigDecimal s) { this.basicSalary = s; }        // L89
}
```

**Why this matters:** Validation and side-effects (e.g., audit-log triggers in `UpdateController`) hook into setters/services rather than touching raw fields. The DAO layer is the only writer to entity state from outside the domain.

### 4.2 Inheritance

Two genuine inheritance trees, both abstract-base + concrete subclasses:

#### Tree 1 — Report hierarchy (Domain)

```
              Report (abstract)              model/Report.java        L15
              ├─ ComplianceReport            model/ComplianceReport.java L9
              └─ HRAnalyticsReport           model/HRAnalyticsReport.java L9
```

```java
// model/Report.java  L15–49
public abstract class Report {
    protected int id;
    protected LocalDateTime generatedAt;
    protected int generatedBy;
    public abstract String getReportType();   // L43
    public abstract String getSummary();      // L46
    public abstract String getFormat();       // L49
}
```

#### Tree 2 — Factory creators (Service/Pattern)

```
              ReportCreator (abstract)         service/ReportCreator.java   L17
              ├─ ComplianceReportCreator       service/ComplianceReportCreator.java L16
              └─ AnalyticsReportCreator        service/AnalyticsReportCreator.java  L15
```

The abstract base owns the **template method** `generate()` (L36–40) which calls the abstract `createReport()` — a textbook *Factory Method* shape (covered in §6.4).

### 4.3 Polymorphism

Three flavours all present:

**(a) Method Overriding** — `@Override` in `ComplianceReport.java` (L31–41) and `HRAnalyticsReport.java` (L27–37) provides distinct behaviour for `getReportType()`, `getSummary()`, `getFormat()`.

**(b) Interface (Subtype) Polymorphism** — `service/ReportFormatStrategy.java` (interface L11) is implemented by three concrete classes (`Pdf`/`Excel`/`Csv`FormatStrategy). The context holds the **interface type** and the actual implementation is selected at runtime:

```java
// service/ReportGenerator.java
private final Map<String, ReportFormatStrategy> strategies = new HashMap<>();   // L19

private String applyStrategy(String raw, String format) {                       // L62
    ReportFormatStrategy strategy =
        strategies.get(format != null ? format.toUpperCase() : "PDF");
    if (strategy == null) return "[REPORT:" + format + "] " + raw;
    return strategy.format(raw);   // ← runtime polymorphic dispatch            // L68
}
```

**(c) Method Overloading** — `service/EmployeeService.java`:
```java
public void updateEmployee(Employee emp) throws SQLException {                  // L36
    updateEmployee(emp, false);
}
public void updateEmployee(Employee emp, boolean skipCapacityCheck) {           // L45
    /* … */
}
```

The same overloaded shape exists for `notifyEmployee` overloads in `NotificationService` and the two-argument vs single-argument constructors of `PayrollNotifier`.

### 4.4 Abstraction

| Type | File | Hides |
|---|---|---|
| Abstract class | `model/Report.java` | The concept of "a report" without committing to a particular kind |
| Abstract class | `service/ReportCreator.java` | Creation-time defaults & template flow |
| Interface | `service/ReportFormatStrategy.java` | Output-format encoding (PDF vs Excel vs CSV) |
| Interface | `service/HREventObserver.java` | Reaction logic (audit log vs employee notification) |
| Interface | `service/PayrollIntegration.java` | The legacy external payroll API |
| Service classes | e.g. `PayrollService.java` L17, L35 | All JDBC details (DAO is the only class that sees `Connection`) |

Abstraction is what lets `UpdateController` call `eventPublisher.publishEvent("EMPLOYEE_UPDATED", ...)` without knowing whether AuditLogObserver, NotificationObserver, or both react.

---

## 5. SOLID Principles

| Principle | Where it shows up | Evidence |
|---|---|---|
| **S — Single Responsibility** | `AuthService` only authenticates; `SessionManager` only holds session; `ReportArchive` only enforces retention. | `service/AuthService.java`, `service/SessionManager.java`, `service/ReportArchive.java` |
| **O — Open/Closed** | New report formats require *only a new `ReportFormatStrategy` implementation* — `ReportGenerator` does not change. New report types require a new `ReportCreator` subclass — `ComplianceReportController`/`HRAnalyticsController` are unaffected. | `service/ReportFormatStrategy.java`, `service/ReportCreator.java` |
| **L — Liskov Substitution** | Anywhere a `Report` reference is used (e.g., `ReportCreator.generate()` returns `Report`), either subtype slots in correctly because both honour the abstract contract. Same for any `ReportFormatStrategy` substitution. | `service/ReportCreator.java` L36–40 |
| **I — Interface Segregation** | Observer interface has *exactly one* method — `onEvent(...)`. Strategy interface has only `format()` and `getFormatName()`. No fat interfaces force unused methods. | `service/HREventObserver.java`, `service/ReportFormatStrategy.java` |
| **D — Dependency Inversion** | `PayrollNotifier` depends on the abstraction `PayrollIntegration`, not on the concrete `ExternalPayrollSystem`. `ReportGenerator` depends on `ReportFormatStrategy`, not on `PdfFormatStrategy`. | `service/PayrollNotifier.java` L15, `service/ReportGenerator.java` L19 |

---

## 6. Gang of Four (GoF) Design Patterns

Five GoF patterns are explicitly implemented and wired into live use cases.

### 6.1 Singleton — `DatabaseConnection`

**File:** `dao/DatabaseConnection.java`

```java
public class DatabaseConnection {
    private static DatabaseConnection instance;            // L14
    private Connection connection;

    private DatabaseConnection() throws SQLException {     // L17 (private ctor)
        Properties props = new Properties();
        InputStream input = getClass().getClassLoader()
                .getResourceAsStream("database.properties");
        if (input == null) throw new SQLException("database.properties not found");
        props.load(input);
        connection = DriverManager.getConnection(
                props.getProperty("db.url"),
                props.getProperty("db.user"),
                props.getProperty("db.password"));
    }

    public static DatabaseConnection getInstance() throws SQLException {  // L34
        if (instance == null || instance.connection.isClosed()) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
}
```

**Roles:**
- *Instance holder* — `private static DatabaseConnection instance;` (L14)
- *Hidden ctor* — `private DatabaseConnection()` (L17)
- *Global access point* — `getInstance()` with **lazy** init **and** auto-reconnect (L34–39)

Used by every DAO: `EmployeeDAO`, `LeaveRequestDAO`, `PayrollDAO`, etc.

A second Singleton is `service/SessionManager.java` (L7 instance, L10 private ctor, L12 `getInstance()`) — used by `MainController` for role checks.

### 6.2 Strategy — Report formatting

**Roles:** `ReportFormatStrategy` (interface) → `PdfFormatStrategy` / `ExcelFormatStrategy` / `CsvFormatStrategy` (concrete strategies) → `ReportGenerator` (context).

```java
// service/ReportFormatStrategy.java  L11
public interface ReportFormatStrategy {
    String format(String data);
    String getFormatName();
}

// service/PdfFormatStrategy.java  L8–18
public class PdfFormatStrategy implements ReportFormatStrategy {
    @Override public String format(String data) { return "[PDF REPORT]\n…" + data; }
    @Override public String getFormatName() { return "PDF"; }
}

// service/ReportGenerator.java
public ReportGenerator() {
    registerStrategy(new PdfFormatStrategy());
    registerStrategy(new ExcelFormatStrategy());
    registerStrategy(new CsvFormatStrategy());          // Defaults registered in ctor
}
public void registerStrategy(ReportFormatStrategy s) {  // ← Open/Closed: plug-in
    strategies.put(s.getFormatName().toUpperCase(), s);
}
```

**Wired in:** `controller/ComplianceReportController.java` and `controller/HRAnalyticsController.java` both call `reportGenerator.generateReport(..., format)` with a user-selected format string. **Adding a JsonFormatStrategy needs zero changes to existing classes** — just register it in the ctor or via a public registration method.

### 6.3 Observer — HR event broadcasting

**Roles:** `HREventObserver` (interface) — `HREventPublisher` (subject) — `AuditLogObserver` + `NotificationObserver` (concrete observers).

```java
// service/HREventObserver.java  L11
public interface HREventObserver {
    void onEvent(String eventType, int entityId, String payload);
}

// service/HREventPublisher.java  L14–41
public class HREventPublisher {
    private final List<HREventObserver> observers = new ArrayList<>();   // L16
    public void register(HREventObserver o)   { if (!observers.contains(o)) observers.add(o); }
    public void unregister(HREventObserver o) { observers.remove(o); }
    public void publishEvent(String eventType, int entityId, String payload) {
        for (HREventObserver o : observers) o.onEvent(eventType, entityId, payload);  // L37–41
    }
}

// controller/UpdateController.java  L19–34  (wiring + publishing)
public UpdateController() throws SQLException {
    this.eventPublisher = new HREventPublisher();
    this.eventPublisher.register(new AuditLogObserver(auditLogService));
    this.eventPublisher.register(new NotificationObserver(new NotificationService()));
}
// later, after employee update:
eventPublisher.publishEvent("EMPLOYEE_UPDATED", employeeID, payload);     // L34
```

Adding a 3rd observer (e.g. `SlackBroadcastObserver`) requires only registering one more line in the controller — UC-02's body never changes.

### 6.4 Factory Method — Report creation

**Roles:** `ReportCreator` (abstract creator with **template method** + **factory method**) → `ComplianceReportCreator` & `AnalyticsReportCreator` (concrete creators).

```java
// service/ReportCreator.java  L17–41
public abstract class ReportCreator {
    public abstract Report createReport(String period, int userId);   // ← FACTORY METHOD  L26
    public Report generate(String period, int userId) {                // ← TEMPLATE METHOD L36
        Report report = createReport(period, userId);                  // polymorphic call
        System.out.println("[FACTORY] Report created: " + report);
        return report;
    }
}

// service/ComplianceReportCreator.java  L16–30
public class ComplianceReportCreator extends ReportCreator {
    @Override public Report createReport(String period, int userId) {
        ComplianceReport r = new ComplianceReport();
        r.setGeneratedAt(LocalDateTime.now());
        r.setReportType("COMPLIANCE");
        r.setFormat("PDF");
        r.setStatus("GENERATED");
        return r;
    }
}
```

Used by:
- `controller/ComplianceReportController.java` L41 `new ComplianceReportCreator()` & L69 `creator.generate(period, userId)` — UC-14
- `controller/HRAnalyticsController.java` L39 `new AnalyticsReportCreator()` & L71 — UC-15

### 6.5 Adapter — External Payroll integration

**Roles:** `PayrollIntegration` (target) ← `PayrollSystemAdapter` (adapter) → `ExternalPayrollSystem` (adaptee, *legacy/incompatible API*) ← used by `PayrollNotifier` (client).

```java
// service/PayrollIntegration.java  L11   ← TARGET (what HR domain wants)
public interface PayrollIntegration {
    void queueSalaryUpdate(int employeeId, String designation, double salary);
    void recordHours(int attendanceId, double hoursWorked);
    void initiateSettlement(int employeeId);
}

// service/ExternalPayrollSystem.java  L10  ← ADAPTEE (legacy, uses String refs)
public class ExternalPayrollSystem {
    public void submitSalaryChange(String empRef, String role, double amountPKR) { … }
    public void logAttendanceHours(String sessionId, double hours) { … }
    public void processFinalPayout(String empRef) { … }
}

// service/PayrollSystemAdapter.java  L15   ← ADAPTER
public class PayrollSystemAdapter implements PayrollIntegration {
    private final ExternalPayrollSystem externalSystem;
    @Override public void queueSalaryUpdate(int employeeId, String designation, double salary) {
        externalSystem.submitSalaryChange("EMP-" + employeeId, designation, salary);  // ID translation
    }
    @Override public void recordHours(int attendanceId, double hoursWorked) {
        externalSystem.logAttendanceHours("ATT-" + attendanceId, hoursWorked);
    }
    @Override public void initiateSettlement(int employeeId) {
        externalSystem.processFinalPayout("EMP-" + employeeId);
    }
}

// service/PayrollNotifier.java  L12–24    ← CLIENT (uses target interface only)
public class PayrollNotifier {
    private final PayrollIntegration payrollIntegration;
    public PayrollNotifier() {
        this.payrollIntegration = new PayrollSystemAdapter(new ExternalPayrollSystem());
    }
    public String notifyPayroll(int employeeId, String designation, double salary) {
        payrollIntegration.queueSalaryUpdate(employeeId, designation, salary);
        return "notified";
    }
}
```

If the payroll vendor changes their API, **only the adapter** is rewritten — `PayrollNotifier`, `UpdateController` (UC-02) and `OffboardingController` (UC-04) remain untouched.

---

## 7. GRASP Patterns

Eight GRASP responsibilities, each with concrete code citations.

### 7.1 Controller — `LeaveController` (UC-06)

```java
// controller/LeaveController.java  L18–49
public class LeaveController {
    private final LeaveRequestService leaveService;
    private final NotificationService notificationService;

    public LeaveController() throws SQLException {
        this.leaveService = new LeaveRequestService();
        this.notificationService = new NotificationService();
    }
    public SubmitResult submitLeaveRequest(int employeeId, String leaveType,
                                           LocalDate start, LocalDate end,
                                           String reason, String docRef) {
        LeaveRequestService.SubmitResult result =
            leaveService.applyForLeave(employeeId, leaveType, start, end, reason, docRef);
        try { notificationService.notifyHR(result.request()); }
        catch (Exception e) { return new SubmitResult(result.request(), true); }
        return new SubmitResult(result.request(), false);
    }
}
```

This class is the **system-operation controller** — the receiver of the system event "Submit Leave Request" — distinct from the JavaFX UI controller.

### 7.2 Information Expert — `LeaveRequestService.calculateWorkingDays()`

The service **owns** the leave-balance, holiday, and probation data, so it's the right place to compute working days, validate balances, etc.

```java
// service/LeaveRequestService.java  L55–66
public static int calculateWorkingDays(LocalDate start, LocalDate end,
                                       Collection<LocalDate> excludedDates) {
    int count = 0;
    LocalDate d = start;
    while (!d.isAfter(end)) {
        DayOfWeek dow = d.getDayOfWeek();
        if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY
                && !excludedDates.contains(d)) count++;
        d = d.plusDays(1);
    }
    return count;
}
```

### 7.3 Creator

`LeaveRequestService.applyForLeave()` aggregates `LeaveRequest` data, contains it, has the initial values, so it creates it (B + D + E of GRASP Creator):

```java
// service/LeaveRequestService.java  L150–161
LeaveRequest lr = new LeaveRequest();
lr.setEmployeeId(employeeId);
lr.setLeaveType(leaveType);
lr.setStartDate(start);
lr.setEndDate(end);
lr.setDaysRequested(days);
lr.setStatus(status);
lr.setAppliedDate(LocalDate.now());
lr.setDocumentPath(docRef);
dao.insert(lr);
```

Same pattern: `PayrollService.processPayroll()` creates `Payroll` (`service/PayrollService.java` L45–57).

### 7.4 Low Coupling

UC-06 dependency chain:

```
LeaveRequestController(UI)  ─►  LeaveController  ─►  LeaveRequestService  ─►  LeaveRequestDAO  ─►  Connection
       (depends only on the next layer; no skipping)
```

`LeaveController` does **not** import `LeaveRequestDAO`; it depends on `LeaveRequestService`. If we swap JDBC for JPA, the controller is untouched.

### 7.5 High Cohesion

`AuthService` (`service/AuthService.java`) does *only* authentication. Two methods — `login()` and `hashPassword()` — both about credentials. `SessionManager` (`service/SessionManager.java`) does *only* session state. Both classes pass the "single elevator-pitch responsibility" test.

### 7.6 Pure Fabrication

| Class | Why it's "fabricated" |
|---|---|
| `service/ReportGenerator.java` | "Report Generator" is not a domain concept; exists to host the Strategy registry |
| `service/PerformanceMonitor.java` | Pure NFR plumbing — measures elapsed time per op |
| `service/ReportArchive.java` | Pure NFR plumbing — enforces 5-year retention |
| `service/AuditLogService.java` | Cross-cutting infrastructure for audit |
| `service/NotificationService.java` | Cross-cutting infrastructure for emails / queue |

These are not domain entities; they're **assigned responsibilities** to keep the model classes clean.

### 7.7 Protected Variation

`ReportFormatStrategy` is the protective interface — clients (`ReportGenerator`, controllers) hold a *stable* reference even though concrete formatters can come and go. Same for `PayrollIntegration` shielding HR code from `ExternalPayrollSystem` change.

### 7.8 Indirection

`PayrollSystemAdapter` mediates between HR domain calls (int IDs) and legacy API (`"EMP-123"` strings). `HREventPublisher` mediates between the publishing controller and the unknown set of observers. Both reduce direct coupling to *zero*.

---

## 8. Use Cases — All 15 Mapped to Code

The system implements **all 15 use cases** specified in the SRS. Each follows the consistent **UI → SD Controller → Service → DAO → DB** path.

### Module 1 — Employee Management

| UC | Title | UI Controller / FXML | SD Controller (entry method) | Service(s) | DAO |
|---|---|---|---|---|---|
| **UC-01** | Register Employee | `RegisterEmployeeController` / `register_employee.fxml` | `RegisterController.registerWithProvisioning()` L69 | `EmployeeRepository`, `NotificationService`, `DepartmentDAO` | `EmployeeDAO` |
| **UC-02** | Update Employee | `UpdateEmployeeController` / `update_employee.fxml` | `UpdateController.updateWithAuditTrail()` L46 + Observer | `EmployeeService`, `AuditLogService`, `PayrollNotifier` | `EmployeeDAO`, `AuditLogDAO` |
| **UC-03** | Assign Department | `AssignDepartmentController` / `assign_department.fxml` | `AssignmentController.assignWithHistory()` L47 | `DepartmentService`, `EmployeeService` | `EmployeeAssignmentDAO`, `DepartmentDAO` |
| **UC-04** | Initiate Offboarding | `OffboardingScreenController` / `offboarding.fxml` | `OffboardingController.initiateOffboarding()` L38 | `EmployeeService`, `PayrollNotifier` (Adapter) | `OffboardingWorkflowDAO` |
| **UC-05** | View Employee Profile | `ProfileScreenController` / `employee_profile.fxml` | `ProfileController.viewEmployeeProfile()` L36 | `EmployeeService`, `AccessControlService`, `AuditLogService` | `EmployeeDAO` |

### Module 2 — Leave & Attendance

| UC | Title | UI Controller / FXML | SD Controller | Service(s) | DAO |
|---|---|---|---|---|---|
| **UC-06** | Submit Leave Request | `LeaveRequestController` / `leave_request.fxml` | `LeaveController.submitLeaveRequest()` L31 | `LeaveRequestService`, `NotificationService` | `LeaveRequestDAO` |
| **UC-07** | Approve / Reject Leave | `LeaveRequestController` (PENDING card actions) | `LeaveApprovalController.processLeaveDecision()` L33 | `LeaveRequestService`, `NotificationService` | `LeaveRequestDAO` |
| **UC-08** | Record Attendance | `AttendanceController` / `attendance.fxml` | `AttendanceRecordController.recordCheckIn()` L39 / `recordCheckOut()` L62 | `PayrollNotifier` (Adapter) | `AttendanceRecordDAO` |
| **UC-09** | Request Attendance Correction | `CorrectionController` / `attendance_correction.fxml` | `AttendanceCorrectionController.submitCorrectionRequest()` L43 | `AuditLogService` | `AttendanceCorrectionDAO` |
| **UC-10** | View Dashboard | `DashboardController` / `dashboard.fxml` | `LeaveDashboardController.getLeaveBalance()` L38 / `getAttendanceSummary()` L51 | `LeaveRequestService`, `ReportGenerator` (Strategy) | `LeaveRequestDAO`, `AttendanceRecordDAO` |

### Module 3 — Performance & Compliance

| UC | Title | UI Controller / FXML | SD Controller | Service(s) | DAO |
|---|---|---|---|---|---|
| **UC-11** | Initiate Evaluation Cycle | `EvaluationController` / `performance.fxml` | `EvaluationCycleController.createDraftCycle()` L40 / `activateCycle()` L85 | `NotificationService`, `TaskScheduler` | `EvaluationCycleDAO` |
| **UC-12** | Submit Performance Evaluation | `EvaluationController` / `performance.fxml` | `PerformanceEvalController.submitEvaluation()` L51 | `AuditLogService`, `NotificationService` | `PerformanceEvaluationDAO` |
| **UC-13** | Monitor Probation Status | `EvaluationController` (probation pane) / `probation.fxml` | `ProbationMonitorController.recordDecision()` L83 | `EmployeeService`, `NotificationService`, `TaskScheduler` | `ProbationRecordDAO` |
| **UC-14** | Generate Compliance Report | `ReportController` / `compliance_report.fxml` | `ComplianceReportController.generateComplianceReport()` L62 | `ComplianceDataAgg`, `ReportGenerator` (Strategy), `ReportCreator` (Factory), `SecureStorage`, `ReportArchive` (NFR) | `ComplianceReportDAO` |
| **UC-15** | Generate HR Analytics Report | `AnalyticsController` / `analytics.fxml` | `HRAnalyticsController.generateAnalyticsReport()` L63 | `AnalyticsAggregator`, `DataVisualization`, `ReportArchive` (NFR), `ReportCreator` (Factory) | `HRAnalyticsReportDAO` |

### Notable alternative-flow coverage (UC-06)

| Flow | Trigger | Implementation |
|---|---|---|
| 2a | Insufficient balance | `LeaveRequestService.applyForLeave()` calls `dao.getBalance()` then compares to `calculateWorkingDays()` |
| 2b | Public holiday conflict | `LeaveRequestDAO.getHolidayMapInRange()` — fault-tolerant on missing `public_holidays` table |
| 2c | Date overlap | SQL `start_date <= end_param AND end_date >= start_param` against PENDING / APPROVED / PENDING_DOCUMENT |
| 3a | HR notification fallback | `LeaveController.submitLeaveRequest()` try/catch returns `SubmitResult(req, notifQueued=true)` |
| 3b | Document required (SICK > 2 days) | Status set to `PENDING_DOCUMENT`; UI exposes "Submit Doc" button only to the owning employee |
| 3c | Probation restriction | Reads `probation_end_date`; ANNUAL/PERSONAL blocked, SICK allowed |

---

## 9. Non-Functional Requirements (NFRs)

### NFR-1 Performance — `< 3 s` SLA per submission

`service/PerformanceMonitor.java`

```java
private static final long SLA_THRESHOLD_MS = 3_000;             // L15
public void recordOperation(String op, long elapsedMs) {        // L27
    if (elapsedMs > SLA_THRESHOLD_MS) {
        System.err.println("[PERFORMANCE WARNING] SLA BREACH: '"
            + op + "' took " + elapsedMs + " ms (limit: 3000 ms)");
    }
}
public void time(String op, RunnableWithException action) throws Exception {  // L48
    long start = System.currentTimeMillis();
    try { action.run(); }
    finally { recordOperation(op, System.currentTimeMillis() - start); }
}
```

Wraps any callable, surfaces SLA breaches to stderr, tracks running averages.

### NFR-2 Data Retention — compliance reports `≥ 5 years`

`service/ReportArchive.java`

```java
private static final int MIN_RETENTION_YEARS = 5;                       // L22
public String archiveReport(int reportId, int creatorId, String ts) {   // L35
    LocalDate retentionExpiry = LocalDate.now().plusYears(MIN_RETENTION_YEARS);
    retentionRegistry.put(reportId, retentionExpiry);
    return "archived — retained until " + retentionExpiry;
}
public boolean canDelete(int reportId) {                                // L55
    LocalDate expiry = retentionRegistry.get(reportId);
    if (expiry == null) return true;
    if (LocalDate.now().isBefore(expiry)) {
        System.err.println("[REPORT ARCHIVE] POLICY VIOLATION: …");
        return false;                       // ← blocks early deletion
    }
    return true;
}
```

Used by both `ComplianceReportController` (UC-14) and `HRAnalyticsController` (UC-15) at archival time.

---

## 10. Database Schema Overview

`hr_management` database — **17 tables**, defined in `database/schema.sql`. Tables grouped by module:

```
Identity & Org            departments · designations · employees · user_accounts

Compensation              payroll · public_holidays

Leave                     leave_requests · leave_balances

Attendance                attendance_records · attendance_corrections

Lifecycle                 offboarding_workflows · probation_records

Performance               evaluation_cycles · performance_evaluations

Reporting                 compliance_reports · hr_analytics_reports

Cross-cutting             audit_log · org_history
```

### Key Tables (truncated DDL excerpts)

```sql
CREATE TABLE departments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    manager_id INT, parent_dept_id INT,
    max_headcount INT DEFAULT 50,
    current_headcount INT DEFAULT 0
);

CREATE TABLE employees (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name, last_name, email UNIQUE, phone,
    national_id UNIQUE, date_of_birth, gender,
    department_id INT, designation_id INT,
    employment_type ENUM('FULL_TIME','PART_TIME','CONTRACT','PERMANENT'),
    basic_salary DECIMAL(10,2), hire_date, probation_end_date,
    status ENUM('ACTIVE','INACTIVE','ON_LEAVE','OFFBOARDING'),
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL
);

CREATE TABLE user_accounts (
    id INT PK,
    employee_id INT UNIQUE NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,        -- SHA-256 hex
    role ENUM('ADMIN','HR','EMPLOYEE'),
    account_status ENUM('ACTIVE','INACTIVE','LOCKED')
);
```

### Inter-Table Relationships (cardinalities)

```
departments  1 ──< many  employees
designations 1 ──< many  employees
employees    1 ──1       user_accounts            (login)
employees    1 ──< many  leave_requests
employees    1 ──< many  attendance_records
employees    1 ──< many  payroll
employees    1 ──< many  performance_evaluations
evaluation_cycles 1 ──< many performance_evaluations
employees    1 ──< many  probation_records
employees    1 ──< many  offboarding_workflows
employees    1 ──< many  audit_log               (who changed what)
```

---

## 11. ASCII Class & Sequence Diagrams

### 11.1 Class diagram — Reports + Patterns (compact)

```
                        ┌──────────────┐
                        │   <<abs>>    │
                        │    Report    │  model/Report.java
                        ├──────────────┤
                        │ # id         │
                        │ # generatedAt│
                        │ # generatedBy│
                        ├──────────────┤
                        │ +getReportType():String   {abstract}
                        │ +getSummary()  :String    {abstract}
                        │ +getFormat()   :String    {abstract}
                        └──────┬───────┘
                               △
              ┌────────────────┴────────────────┐
              │                                 │
   ┌──────────┴────────┐               ┌────────┴───────────┐
   │ ComplianceReport  │               │ HRAnalyticsReport  │
   ├───────────────────┤               ├────────────────────┤
   │ - reportType      │               │ - reportPeriod     │
   │ - parameters      │               │ - summaryMetrics   │
   │ - status          │               │                    │
   │ - archivePath     │               │                    │
   ├───────────────────┤               ├────────────────────┤
   │ +getReportType()  │ {override}    │ +getReportType()   │ {override}
   │ +getSummary()     │ {override}    │ +getSummary()      │ {override}
   │ +getFormat()      │ {override}    │ +getFormat()       │ {override}
   └───────────────────┘               └────────────────────┘

                        ┌─────────────────────────┐
                        │   <<abs>>               │
                        │   ReportCreator         │
                        ├─────────────────────────┤
                        │ +createReport(...) {abstract}  ← FACTORY METHOD
                        │ +generate(...)            ← TEMPLATE METHOD
                        └────────────┬────────────┘
                                     △
                  ┌──────────────────┴───────────────────┐
                  │                                      │
       ┌──────────┴──────────────┐         ┌─────────────┴──────────────┐
       │ ComplianceReportCreator │         │  AnalyticsReportCreator    │
       ├─────────────────────────┤         ├────────────────────────────┤
       │ +createReport() ► returns ComplianceReport   │ +createReport() ► returns HRAnalyticsReport
       └─────────────────────────┘         └────────────────────────────┘

       ┌─────────────────────────────────────────────────┐
       │ <<interface>> ReportFormatStrategy              │
       ├─────────────────────────────────────────────────┤
       │ +format(data:String):String                     │
       │ +getFormatName():String                         │
       └────────────────────┬────────────────────────────┘
                            △
   ┌────────────────────────┼─────────────────────────┐
   │                        │                         │
┌──┴──────────────────┐ ┌───┴────────────────┐  ┌─────┴───────────────┐
│ PdfFormatStrategy   │ │ ExcelFormatStrategy │  │ CsvFormatStrategy   │
└─────────────────────┘ └─────────────────────┘  └─────────────────────┘
                ▲
                │ uses (Strategy registry)
                │
       ┌────────┴────────┐
       │ ReportGenerator │  Map<String, ReportFormatStrategy>
       └─────────────────┘
```

### 11.2 Class diagram — Observer

```
   <<interface>> HREventObserver         ┌────────────────────┐
   +onEvent(type, id, payload)           │  HREventPublisher  │ ◄─ subject
                △                        ├────────────────────┤
                │ implements             │ - observers: List  │
   ┌────────────┴───────────┐            │ +register(o)       │
   │                        │            │ +unregister(o)     │
┌──┴─────────────────┐ ┌────┴───────────┐│ +publishEvent(...) │
│ AuditLogObserver   │ │ NotificationObs││                    │
│  → AuditLogService │ │ → NotificationS│└────────────────────┘
└────────────────────┘ └────────────────┘            ▲
                                                     │ uses
                                                     │
                                       ┌─────────────┴────────────┐
                                       │  UpdateController (UC-02)│
                                       └──────────────────────────┘
```

### 11.3 Class diagram — Adapter

```
   <<interface>> PayrollIntegration   ◄─── target  ────┐
   +queueSalaryUpdate(empId, designation, salary)      │ implements
   +recordHours(attId, hours)                          │
   +initiateSettlement(empId)                          │
                                                       │
                                          ┌────────────┴───────────┐
                                          │ PayrollSystemAdapter   │   ┌────────────────────┐
                                          ├────────────────────────┤── │ ExternalPayrollSys │ adaptee
                                          │ - externalSystem       │   │ +submitSalaryChange│
                                          │ +queueSalaryUpdate(...){override}  +logAttendanceHours│
                                          │ +recordHours(...)      │   │ +processFinalPayout│
                                          │ +initiateSettlement(...)│  └────────────────────┘
                                          └────────────────────────┘
                                                       ▲
                                                       │ uses
                                                       │
                                          ┌────────────┴────────────┐
                                          │   PayrollNotifier       │  client
                                          └─────────────────────────┘
```

### 11.4 Sequence diagram — UC-06 Submit Leave Request

```
 Employee   LeaveRequestController     LeaveController       LeaveRequestService     LeaveRequestDAO     NotificationService
    │           (UI / @FXML)          (SD/GRASP)            (Business)             (DAO)              (Service)
    │                │                    │                     │                      │                    │
    │ click Submit   │                    │                     │                      │                    │
    │──────────────► │                    │                     │                      │                    │
    │                │ submitLeaveRequest()│                    │                      │                    │
    │                │───────────────────►│                     │                      │                    │
    │                │                    │ applyForLeave()     │                      │                    │
    │                │                    │────────────────────►│                      │                    │
    │                │                    │                     │ getHolidayMapInRange()                    │
    │                │                    │                     │─────────────────────►│                    │
    │                │                    │                     │ getBalance()         │                    │
    │                │                    │                     │─────────────────────►│                    │
    │                │                    │                     │ findOverlapping()    │                    │
    │                │                    │                     │─────────────────────►│                    │
    │                │                    │                     │ NEW LeaveRequest{...}│                    │
    │                │                    │                     │ insert()             │                    │
    │                │                    │                     │─────────────────────►│                    │
    │                │                    │                     │ ◄────────────────────│ generated id       │
    │                │                    │ ◄───────────────────│ SubmitResult(req)    │                    │
    │                │                    │ notifyHR(req)       │                      │                    │
    │                │                    │─────────────────────────────────────────────────────────────────►
    │                │                    │ ◄───────────────────────────────────────────────────────────────│ ok | throw
    │                │ ◄──────────────────│ SubmitResult(req, notifQueued=t/f)                              │
    │ ◄──────────────│ refresh card list  │                     │                      │                    │
```

---

## 12. End-to-End Flow Walkthrough — UC-06

A typical demo question: *"Walk us through what happens from the moment you click 'Submit Leave Request'."*

1. **UI** — `ui/LeaveRequestController.java` `@FXML` handler captures form state (leave type, start, end, reason, optional doc).
2. **SD Controller** — `controller/LeaveController.submitLeaveRequest()` (L31–49) calls service. Wraps `notifyHR()` in try/catch — alt-flow 3a.
3. **Service** — `service/LeaveRequestService.applyForLeave()` (L87–165):
   - Loads holidays via `dao.getHolidayMapInRange()` (alt 2b)
   - Computes working days via `calculateWorkingDays()` (Mon–Fri, excluding holidays)
   - Validates balance via `dao.getBalance()` (alt 2a)
   - Detects overlap via `dao.findOverlapping()` (alt 2c)
   - Reads `probation_end_date` from `employees` (alt 3c)
   - Constructs `LeaveRequest` (Creator) and calls `dao.insert()`
4. **DAO** — `dao/LeaveRequestDAO.insert()` runs `INSERT INTO leave_requests …` via `PreparedStatement`.
5. **DB** — row written; `Statement.RETURN_GENERATED_KEYS` populates `lr.setId(...)`.
6. **Notification** — `service/NotificationService.notifyHR()` (a Pure Fabrication) emails HR; if it throws, controller flips `notifQueued=true` and the UI shows a "queued" toast instead of a failure.

This single use case demonstrates **all 4 OOP pillars + 3 of 5 GoF + 6 of 8 GRASP** in flight simultaneously.

---

## 13. Authentication, Sessions & Role-Based Access

| Concern | File | Notes |
|---|---|---|
| Login UI | `resources/com/hr/login.fxml` + `ui/LoginController.java` | Slide-in glassmorphic card, shake on error, fade-out on success |
| Hashing | `service/AuthService.hashPassword()` | SHA-256 hex |
| Credential check | `service/AuthService.login()` | Throws `IllegalArgumentException` on bad credentials |
| Session | `service/SessionManager.java` | Singleton; `isAdmin/isHR/isEmployee()` helpers |
| RBAC navigation | `ui/MainController.applyRoleAccess()` | Hides nav buttons by role |
| Demo seed | `util/DemoUserSetup.main()` | DELETE-then-INSERT admin/hr/emp users |
| DB note | — | `user_accounts.employee_id` had to be made `NULL`-able for the demo seeder |

The `MainController` also implements the **collapsible sidebar** with the BorderPane width animation gotcha (must animate `min + pref + max` simultaneously; animating `prefWidth` alone is silently ignored — see `MEMORY.md` entry on this).

---

## 14. Demo-Day Q&A Bank

> Use these as quick rehearsal — every answer is grounded in the code above.

**Q1. Why two controller layers?**
A. JavaFX controllers (`com.hr.ui`) handle widget events; GRASP system-operation controllers (`com.hr.controller`) handle use-case logic. Separation makes business rules unit-testable without a JavaFX runtime, and follows GRASP Controller responsibility cleanly.

**Q2. Show me Polymorphism, runtime-dispatch flavour.**
A. `service/ReportGenerator.java` L19 holds `Map<String, ReportFormatStrategy>`. Line 68 calls `strategy.format(raw)` — Java picks `Pdf`/`Excel`/`Csv` at runtime based on the registered concrete type.

**Q3. Where's the Singleton — and why must its constructor be private?**
A. `dao/DatabaseConnection.java` L17 — private ctor blocks `new DatabaseConnection()` from anywhere outside the class. `getInstance()` is the only doorway. Keeps the JDBC `Connection` shared and reused, plus the `instance.connection.isClosed()` check at L35 auto-reconnects on stale connections.

**Q4. Walk me through the Strategy pattern.**
A. *Interface* `ReportFormatStrategy` declares `format(...)` and `getFormatName()`. *Three concrete strategies* (`Pdf`/`Excel`/`Csv`FormatStrategy) implement it. *Context* `ReportGenerator` keeps a registry, looks up by format key, and delegates. Adding a 4th format requires writing one new class — zero changes to `ReportGenerator` (Open/Closed).

**Q5. Where is the Observer pattern used and why?**
A. `controller/UpdateController.java` (UC-02). After updating an employee, it calls `eventPublisher.publishEvent("EMPLOYEE_UPDATED", ...)`. Two observers — `AuditLogObserver` (writes to `audit_log`) and `NotificationObserver` (emails the employee) — react independently. The controller doesn't know which observers are present; cross-cutting concerns are decoupled.

**Q6. What's the Adapter solving?**
A. The HR domain wants `queueSalaryUpdate(int employeeId, ...)` but the legacy `ExternalPayrollSystem` exposes `submitSalaryChange(String empRef, ...)` with `"EMP-{id}"` reference strings. `PayrollSystemAdapter` implements `PayrollIntegration` and translates the call. If we later switch payroll vendors, only the Adapter changes.

**Q7. Factory Method vs Abstract Factory — which one is this?**
A. **Factory Method.** `ReportCreator` is an abstract creator with an abstract `createReport()` and a concrete template method `generate()` that calls it. Each subclass overrides the factory method to return its own `Report` subtype. (Abstract Factory would create *families* of related objects; we create one product per creator.)

**Q8. Information Expert — who has the "calculate working days" responsibility?**
A. `LeaveRequestService.calculateWorkingDays()` (L55) — it has the start/end dates, holiday set, and the rules. Putting this in the controller would be GRASP-bad (controllers shouldn't own business algorithms); putting it in the model would force `LeaveRequest` to know about holiday tables.

**Q9. What is "Pure Fabrication" in your code?**
A. Any class that isn't a domain entity but exists to *do a job*: `ReportGenerator`, `PerformanceMonitor`, `ReportArchive`, `AuditLogService`, `NotificationService`. They keep `Employee`/`LeaveRequest`/etc. uncluttered.

**Q10. How are the NFRs enforced in code, not just on paper?**
A. `service/PerformanceMonitor.java` wraps any `Runnable` and prints a stderr SLA-breach warning above 3000 ms. `service/ReportArchive.canDelete()` returns `false` and prints a "POLICY VIOLATION" log if a report is asked to be deleted before the 5-year mark.

**Q11. Show me where Inheritance is more than just `extends`.**
A. `Report` is abstract — you literally cannot do `new Report()`. Subclasses inherit `protected int id`, `LocalDateTime generatedAt`, `int generatedBy` and **must** implement `getReportType()`, `getSummary()`, `getFormat()`. The compiler enforces the contract — if `ComplianceReport` doesn't override all three, it won't compile.

**Q12. How do you avoid a `God Class`?**
A. Each service has one focused responsibility (High Cohesion). `AuthService` ≠ `SessionManager` ≠ `AccessControlService` even though they're all "auth-ish." `EmployeeService` does business rules; `EmployeeDAO` does SQL; `EmployeeRepository` is a higher-level orchestration with extra validation. No single class exceeds ~250 lines.

**Q13. How does an event in UC-02 reach both the audit log *and* the email queue?**
A. `UpdateController` ctor (L19–25) registers two observers with `HREventPublisher`. After persisting the update, it calls `publishEvent("EMPLOYEE_UPDATED", id, payload)`. The publisher loops `observers` and invokes `onEvent` polymorphically. Audit logging and email are independent reactions.

**Q14. What if MySQL goes down mid-session?**
A. `DatabaseConnection.getInstance()` checks `instance.connection.isClosed()` at L35 and re-runs the constructor (re-reads `database.properties`, re-issues `DriverManager.getConnection`). Subsequent DAO calls see a fresh connection. Without this re-init guard, all DAOs would return stale closed connections.

**Q15. Where in your code is the "Open/Closed Principle" most visible?**
A. `ReportGenerator.registerStrategy()` — closed for modification (no `if (format.equals("PDF"))` chains anywhere), open for extension (drop in any class implementing `ReportFormatStrategy` and call `register()`). Same shape for `HREventPublisher.register(observer)`.

**Q16. Why is `LeaveRequest.applyForLeave()` *not* in `LeaveRequest`?**
A. `LeaveRequest` is a POJO — pure state. The act of *applying for leave* needs cross-aggregate data (holidays, balances, probation date) plus persistence. That's a **service** responsibility, not a model responsibility, by Information Expert + Low Coupling. Keeping the model anemic-but-clean lets us serialize/transport it easily.

**Q17. How would you add a JSON report format in 5 minutes?**
A. Create `service/JsonFormatStrategy.java` implementing `ReportFormatStrategy`. Either register it in `ReportGenerator`'s ctor or call `reportGenerator.registerStrategy(new JsonFormatStrategy())` once at startup. **Zero existing code changes.** That's the Strategy + Open/Closed payoff.

**Q18. Where do you handle role-based access control?**
A. `service/SessionManager.isAdmin/isHR/isEmployee()` provides role checks. `ui/MainController.applyRoleAccess()` hides nav buttons. UC-06 leave card actions check role to show/hide "Approve / Reject" — only HR/Admin see those. UC-05 profile view filters PII fields based on `AccessControlService` rules.

**Q19. Which patterns appear in UC-14 (Compliance Report)?**
A. Five at once: **Factory Method** (`ComplianceReportCreator.generate()`), **Strategy** (`ReportGenerator.applyStrategy(...)`), **Singleton** (`DatabaseConnection`), **Pure Fabrication** (`ReportGenerator`, `SecureStorage`, `ReportArchive`), and **Inheritance/Polymorphism** (`Report` ← `ComplianceReport`).

**Q20. What's the upgrade path if the project moves from JavaFX desktop to a Spring Boot REST API?**
A. Replace the entire `ui/` package with `@RestController` classes that call the *same* `controller/*Controller.java` system-operation controllers. Replace `dao/DatabaseConnection` with a Spring-managed `DataSource` bean and rewrite each DAO as a `@Repository`. The *service* and *model* layers — where the business rules live — port over **unchanged**. That portability is the dividend of layered architecture and Low Coupling.

---

### Quick Reference Card (memorize these for viva)

```
SINGLETON   →  dao/DatabaseConnection.java                  (L14, L17, L34)
              service/SessionManager.java                    (L7, L10, L12)

STRATEGY    →  service/ReportFormatStrategy + 3 concretes
              service/ReportGenerator.java                   (L19, L62–68)

OBSERVER    →  service/HREventObserver / HREventPublisher
              controller/UpdateController.java               (L19–25 wire, L34 publish)

FACTORY M.  →  service/ReportCreator + 2 concrete creators
              controller/ComplianceReportController L41,L69
              controller/HRAnalyticsController     L39,L71

ADAPTER     →  service/PayrollIntegration  (target)
              service/PayrollSystemAdapter (adapter)
              service/ExternalPayrollSystem (adaptee)
              service/PayrollNotifier      (client)

NFR-1 perf  →  service/PerformanceMonitor.java               (3000 ms SLA)
NFR-2 ret.  →  service/ReportArchive.java                    (5-year archive)
```

---

**End of Demo-Prep Deep-Dive.** Read this once cold, then again clicking through each cited file in IntelliJ — you'll have a verbatim answer for every question on the rubric.
