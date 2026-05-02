# Smart HR Operations & Workforce Management System
## Master Reference Document — Group 16, SE2002 SDA, Spring 2026

**Course:** SE2002 – Software Design and Architecture  
**Instructor:** Ms. Laiba Imran  
**Institution:** FAST NUCES, Department of Computer Science  
**Group 16 Members (original):**  
- Sohaib Akhlaq (24i-3108) — UC-01 to UC-05 Employee Management  
- M. Hasaam (24i-3107) — UC-11 to UC-15 Performance & Compliance  
- Shaiman Qasir (24i-3074) — UC-06 to UC-10 Leave & Attendance  

**Solo Implementation (this project):** All 4 modules implemented by one student.

---

## Rubric — 140 Marks Total

| # | Criteria | Marks |
|---|----------|-------|
| 1 | Documentation — complete sections, readable diagram images, Readme | 10 |
| 2 | Use Cases — all assigned UCs proper, functional, completed | 18 |
| 3 | User Interface — JavaFX, all screens & events working, mapped to SDs/SSDs | 10 |
| 4 | OOP Principles — encapsulation, inheritance, abstraction, polymorphism | 10 |
| 5 | Business Logic Layer — well designed, correct output, no runtime errors | 10 |
| 6 | Database — correct tables, exception handling, full CRUD, input validation | 10 |
| 7 | Architecture — 3 diagrams (Package + Deployment + Component), correct style | 5 + 15 |
| 8 | Integration — all 3 layers (UI, BL, DB) as separate packages, fully integrated | 12 |
| 9 | Consistency — 100% mapping of SDs to code | 10 |
| 10 | Design Patterns — ALL 6 GRASP + GoF applied correctly | 10 |
| 11 | Non-Functional Requirements — at least 2 implemented correctly | 10 |
| 12 | Code — fully functional, commented, indented | 5 + 5 |
| **Total** | | **140** |

**Critical items:** Architecture diagrams = 20 marks | SD-to-code mapping = 10 marks | ALL 6 GRASP patterns required explicitly.

---

## System Overview (from D1 Vision Document)

### System Name
**Smart HR Operations & Workforce Management System** — a role-based, centralized enterprise solution automating HR activities. Built in Java using a layered architectural pattern and UML-based design.

### Business Problem
Organizations use manual processes / spreadsheets for HR → delayed approvals, data discrepancies, policy violations, lack of transparency. This system provides structured workflows, policy enforcement, and centralized workforce visibility.

### 3 Roles (RBAC)
| Role | Responsibilities |
|------|-----------------|
| **Admin** | Define departments & org structure; configure system policies; control roles; generate compliance reports |
| **HR Officer** | Manage employee lifecycle (onboarding/offboarding); approve/reject leave; monitor attendance; conduct performance appraisals |
| **Employee** | Submit leave requests; submit attendance corrections; view own profile and performance history |

### 3 Modules
1. **Employee Management** — UC-01 to UC-05
2. **Leave and Attendance Management** — UC-06 to UC-10
3. **Performance and Compliance Management** — UC-11 to UC-15

### Architecture Style
3-Layer Layered Architecture:
```
Presentation Layer   → JavaFX + FXML (Controllers, Views)
Business Logic Layer → Service classes (validation, workflow, business rules)
Data Access Layer    → DAO classes + MySQL (JDBC)
```

### NFRs
- Form submissions < 3 seconds under normal load
- Profile page loads < 2 seconds
- Data encrypted in transit (TLS 1.2+) and at rest
- Access revocation < 1 hour for involuntary terminations
- Assignment access-rights sync < 5 minutes
- Compliance reports archived ≥ 5 years

---

## All 15 Use Cases (from D2)

### Module 1: Employee Management (UC-01 to UC-05)

#### UC-01 | Register New Employee
- **Actor:** HR Officer (Primary); Admin, IT (Supporting); New Employee, Payroll, Compliance (Offstage)
- **Preconditions:** HR authenticated; no duplicate National ID/email; department & designation exist; contract signed
- **Postconditions:** Unique Employee ID generated; record persisted; welcome email sent; user account provisioned
- **Main Flow:**
  1. Navigate to Register New Employee → form displayed with mandatory fields highlighted
  2. Enter personal details → real-time duplicate check on NID + email
  3. Select department, designation, employment type, joining date → probation period auto-populated
  4. Upload documents (PDF/JPEG, ≤5 MB) → format/size validated
  5. Submit → Employee ID generated (format: `EMP-YYYY-NNNN`), record saved, welcome email dispatched
- **Key Extensions:** 2a. Duplicate NID → block; 3a. Dept at capacity → HR override + Admin auth; 4a. Wrong format → re-upload prompt; 5a. SMTP down → queued retry

#### UC-02 | Update Employee Record
- **Actor:** HR (Primary); Admin (override), System audit logger (Supporting)
- **Preconditions:** HR authenticated; employee Active or On Leave; fields within HR role scope
- **Postconditions:** Record updated; timestamped audit log (field, old, new, updater ID); payroll/reporting notified; employee notified
- **Main Flow:**
  1. Search by ID/name/department → matching records shown
  2. Select employee → editable form pre-populated, restricted fields disabled
  3. Modify fields → real-time validation
  4. Enter mandatory reason → Save → audit log + downstream notifications
- **Key Extensions:** 3b. Salary-grade change without privileges → Access Restricted; 3c. National ID update → requires Admin secondary approval; 4a. Concurrent edit conflict → show both versions

#### UC-03 | Assign Employee to Department
- **Actor:** HR/Admin (Primary); System hierarchy updater, IT (Supporting)
- **Preconditions:** Employee Active; target dept exists; user has Org Management or HR Admin permissions
- **Postconditions:** Dept association updated; org history recorded with effective date; reporting line & access rights updated; both managers notified
- **Main Flow:**
  1. Navigate to Org Structure → select target dept (shows headcount, manager, slots)
  2. Search and select employee → current dept shown
  3. Set effective date + optional remark → validated (not past, dept has capacity)
  4. Confirm → hierarchy updated, change logged, notifications sent
- **Key Extensions:** 3a. Dept at capacity → justification required; 3b. Backdated → acknowledgment required; 3c. Active offboarding → block assignment

#### UC-04 | Initiate Employee Offboarding
- **Actor:** HR (Primary); Admin, Finance, IT Security, Direct Manager (Supporting)
- **Preconditions:** Employee active; HR has offboarding privileges; formal separation notice exists
- **Postconditions:** Offboarding workflow created; access revocation scheduled; final settlement initiated; status → Offboarding – Pending Clearance; record archived on full clearance
- **Main Flow:**
  1. Select employee → Initiate Offboarding → form with separation type + last working date
  2. Select type (resignation/retirement/contract expiry/termination) + enter last working date → notice period validation
  3. Assign tasks to IT, Finance, employee → checklist generated, notifications sent
  4. Submit → status changed, record editing locked, payroll settlement triggered
- **Key Extensions:** 2a. Notice period violation → exception + Admin approval; 2b. Termination for Cause → upload formal letter required; 3b. Outstanding financial obligations → Finance must confirm net settlement

#### UC-05 | View Employee Profile
- **Actor:** Employee/HR (Primary); Admin (Supporting)
- **Preconditions:** Authenticated user; Employees see own profile only; HR/Admin see all within scope; record exists and is Active/On Leave/Offboarding
- **Postconditions:** Profile data shown per role; read-access log entry created for sensitive field access
- **Main Flow:**
  1. Navigate to Employee Directory or global search → role-scoped results
  2. Select employee → profile rendered: personal info, employment details, dept, leave balance, attendance summary, performance history
  3. Apply section filters → dynamic rendering with field masking per role
- **Key Extensions:** 2a. Not found; 2b. Archived → read-only; 3a. Out-of-scope field → Access Restricted + unauthorized attempt log; 3b. Export to PDF → masked per role

---

### Module 2: Leave & Attendance Management (UC-06 to UC-10)

#### UC-06 | Submit Leave Request
- **Actor:** Employee (Primary); HR, System (Supporting)
- **Preconditions:** Employee authenticated + active; sufficient leave balance; dates in active policy calendar
- **Postconditions:** Request stored as Pending Approval; HR notified; dates blocked in attendance calendar
- **Main Flow:**
  1. Navigate to Leave Management → New Leave Request → form shows current balances by type
  2. Select leave type + dates + optional reason → working days calculated, balance validated, blackout check
  3. Submit → status: Pending Approval, dates blocked, notifications to HR
- **Key Extensions:** 2a. Insufficient balance → block + display remaining; 2b. Blackout period conflict → highlight + suggest adjusted dates; 3b. Document required (e.g. sick leave) → Pending Document status

#### UC-07 | Approve or Reject Leave Request
- **Actor:** HR (Primary); Dept Manager (Supporting)
- **Preconditions:** Pending Approval request exists; HR authorized; HR reviewed team coverage calendar
- **Postconditions:** Approved → balance deducted + calendar confirmed + employee notified; Rejected → reason recorded + employee notified; decision audit log maintained
- **Main Flow:**
  1. Open leave queue → select pending request → employee details + leave summary
  2. Review coverage calendar
  3. Approve or Reject with mandatory comment → system updates status, deducts balance (if approved), notifies employee
- **Key Extensions:** 3a. Approval would leave dept below minimum coverage → warning; 3b. Leave type requires medical certificate → validate before approving

#### UC-08 | Record Daily Attendance
- **Actor:** Employee (Primary)
- **Description:** Record check-in/out timestamps via biometric/web/mobile. Compute work hours, flag late arrivals/early departures. Link attendance to payroll.

#### UC-09 | Request Attendance Correction
- **Actor:** Employee (Primary); HR (Supporting)
- **Description:** Employee specifies affected date, incorrect value, corrected value, reason. HR reviews; approved corrections update record with audit annotation.

#### UC-10 | View Leave Balance and Attendance Summary
- **Actor:** Employee/HR (Primary)
- **Description:** Real-time dashboard of remaining leave balances by type and monthly attendance KPIs. Aggregated from leave + attendance modules. Export to PDF or Excel.

---

### Module 3: Performance & Compliance Management (UC-11 to UC-15)

#### UC-11 | Initiate Performance Evaluation Cycle
- **Actor:** Admin (Primary)
- **Description:** Configure and launch evaluation cycle — define period, scope, evaluators, deadlines. System distributes tasks, opens evaluation module, sends automated reminders.

#### UC-12 | Submit Employee Performance Evaluation
- **Actor:** HR/Evaluator (Primary)
- **Description:** Complete structured evaluation form (competencies + KPIs). System validates completeness, calculates aggregate score, records in performance history.

#### UC-13 | Monitor Employee Probation Status
- **Actor:** HR (Primary); Admin (Supporting)
- **Description:** Track probation evaluation milestones and decision dates. Automated deadline alerts. HR records confirmation, extension, or termination decisions.

#### UC-14 | Generate Compliance Report
- **Actor:** Admin (Primary)
- **Description:** Generate formal compliance report for specified period and domain. Aggregate policy adherence data, apply regulatory filters, time-stamp, archive ≥5 years.

#### UC-15 | Generate HR Analytics Report
- **Actor:** Admin (Primary)
- **Description:** Workforce KPIs — headcount, attrition, leave utilization, performance benchmarks. Query all modules, apply statistical aggregation, render interactive charts + data tables. Support export and scheduled delivery.

---

## Domain Model (from D2)

### Core Entities & Key Attributes

| Entity | Key Attributes |
|--------|---------------|
| **Employee** | employeeId (EMP-YYYY-NNNN), fullName, nationalId, dateOfBirth, gender, contact, address, joiningDate, employmentType, status (Active/On Leave/Offboarding/Archived) |
| **Department** | departmentId, name, maxHeadcount, reportingManager |
| **Designation** | designationId, title, salaryGrade, requiredClearanceLevel |
| **EmployeeRecord** | recordId, employeeId, fieldName, oldValue, newValue, updaterId, timestamp |
| **LeaveRequest** | requestId, employeeId, leaveType, startDate, endDate, workingDays, status (Pending/Approved/Rejected), reason, hrComment, appliedDate |
| **LeaveBalance** | balanceId, employeeId, annual (21d), sick (14d), personal (7d) |
| **AttendanceRecord** | recordId, employeeId, date, checkIn, checkOut, workHours, status (Present/Late/Early/Absent) |
| **AttendanceCorrectionRequest** | requestId, employeeId, affectedDate, incorrectValue, correctedValue, reason, status |
| **PerformanceEvaluation** | evalId, employeeId, cycleId, evaluatorId, scores (competencies + KPIs), aggregateScore, submittedDate |
| **EvaluationCycle** | cycleId, period, scope, evaluators, deadlines, status |
| **ProbationRecord** | probationId, employeeId, startDate, endDate, milestones, decision (Confirmed/Extended/Terminated) |
| **ComplianceReport** | reportId, period, domain, content, generatedDate, archivedDate |
| **HRAnalyticsReport** | reportId, period, kpis (headcount/attrition/leaveUtilization/performanceBenchmarks), exportFormat |
| **UserAccount** | accountId, employeeId, role (Admin/HR/Employee), passwordHash, lastLogin |
| **OffboardingWorkflow** | workflowId, employeeId, separationType, lastWorkingDate, tasks[], status |

### Key Relationships
- Employee *→* Department (many-to-one, with effective date history)
- Employee *→* Designation (many-to-one)
- Employee *→* LeaveBalance (one-to-one)
- Employee *→* LeaveRequest (one-to-many)
- Employee *→* AttendanceRecord (one-to-many)
- Employee *→* PerformanceEvaluation (one-to-many)
- Employee *→* ProbationRecord (one-to-one, during probation)
- Employee *→* UserAccount (one-to-one)
- Employee *→* OffboardingWorkflow (one-to-one, when offboarding)
- EvaluationCycle *→* PerformanceEvaluation (one-to-many)

---

## D3: System Sequence Diagrams (SSDs)

D3 contains SSDs for all 15 use cases. Each UC has:
1. **Main Success Scenario SSD** — shows the full happy path interaction between actors and the system boundary
2. **Major Alternative/Exception Flow SSDs** — 3+ alternative scenarios per UC (diagram images in PDF)

SSDs treat the system as a black box; the actor sends system events and receives system responses. The internal class structure is NOT shown in SSDs.

Key system events per module (for SD→code mapping):
- UC-01: `registerEmployee(data)`, `generateEmployeeId()`, `sendWelcomeEmail()`
- UC-02: `searchEmployee(query)`, `updateRecord(id, fields, reason)`, `writeAuditLog()`
- UC-06: `calculateWorkingDays(start, end)`, `validateBalance(type, days)`, `submitLeaveRequest()`
- UC-07: `approveLeaveRequest(id, comment)`, `rejectLeaveRequest(id, comment)`, `deductLeaveBalance()`

---

## D3: Sequence Diagrams (SDs) & GRASP Annotations

Each system event from the SSDs has a corresponding Sequence Diagram showing internal class interactions, annotated with GRASP patterns:

### GRASP Pattern Assignments (required in rubric — ALL 6)
| GRASP Pattern | Applied To | How |
|---------------|-----------|-----|
| **Controller** | XxxController classes | Handle UI events, delegate to Service layer — do not contain business logic |
| **Information Expert** | Service classes | Own the data they process (EmployeeService knows Employee validation rules) |
| **Creator** | Service classes | Create domain objects (PayrollService creates Payroll records) |
| **Low Coupling** | DAO ↔ Service ↔ Controller | Each layer only depends on the layer directly below |
| **High Cohesion** | Single-responsibility classes | EmployeeDAO only does employee DB operations; PayrollService only does payroll calculations |
| **Protected Variation** | DatabaseConnection (Singleton) | Shields all classes from connection management details; interface stable even if DB changes |

### GoF Pattern
| GoF Pattern | Applied To |
|-------------|-----------|
| **Singleton** | `DatabaseConnection.getInstance()` — ensures one connection pool to MySQL |

---

## Architecture — 3 Required Diagrams

### 1. Package Diagram
Shows the 3 packages and their dependencies:
```
com.hr (MainApp)
  └── depends on → com.hr.controller
com.hr.controller
  └── depends on → com.hr.service
com.hr.service
  └── depends on → com.hr.dao, com.hr.model
com.hr.dao
  └── depends on → com.hr.model
com.hr.model
  └── (no dependencies)
```

### 2. Component Diagram
Shows the system components and their interfaces:
```
[JavaFX UI] ──uses──► [Controller Layer]
[Controller Layer] ──uses──► [Service Layer]
[Service Layer] ──uses──► [DAO Layer]
[DAO Layer] ──uses──► [MySQL Database]
[DatabaseConnection] ──provides──► [JDBC Connection]
```

### 3. Deployment Diagram
```
Developer Workstation
  ├── JVM (Java 21+)
  │     ├── JavaFX Application (UI + Controllers + Services + DAOs)
  │     └── MySQL Connector/J (JDBC driver)
  └── MySQL Server 8.x (localhost:3306)
        └── hr_management database
```

---

## Implementation: Solo Student's 4 Use Cases

The student implements 4 complete functional modules in the JavaFX application:

### UC-A: Manage Employees (maps to UC-01, UC-02, UC-05)
- **Add Employee** → validates all fields, inserts into `employees` table, assigns dept
- **Edit Employee** → updates record in DB (all fields editable in form)
- **Delete Employee** → removes from DB
- **List/Search** → loads all employees with dept name via JOIN

### UC-B: Manage Departments (maps to UC-03)
- **Add Department** → validates name uniqueness, inserts into `departments` table
- **Edit Department** → updates name/description
- **Delete Department** → removes (if no employees assigned)
- **List** → loads all departments

### UC-C: Process Payroll (custom implementation)
- **Business Rules:** Tax = 15% of basic salary; Benefits deduction = 5%; Net Pay = Basic − Tax − Benefits
- **Process** → selects employee, sets pay period (start/end date), calculates and saves to `payroll` table
- **List** → shows all payroll records with employee name + all calculated fields
- **Delete** → removes payroll record

### UC-D: Manage Leave Requests (maps to UC-06, UC-07)
- **Apply** → employee submits leave request; validates balance from `leave_balances` table
- **Approve** → status → APPROVED; deducts from `leave_balances`; balance restored if later deleted
- **Reject** → status → REJECTED (balance NOT deducted)
- **Delete** → removes record + restores balance if it was APPROVED
- **Leave Types & Balances:** Annual (21 days), Sick (14 days), Personal (7 days)

---

## Database Schema

### Tables
```sql
-- departments
CREATE TABLE departments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- employees
CREATE TABLE employees (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    hire_date DATE NOT NULL,
    position VARCHAR(100),
    basic_salary DECIMAL(10,2) NOT NULL,
    department_id INT,
    status ENUM('Active','Inactive','On Leave') DEFAULT 'Active',
    FOREIGN KEY (department_id) REFERENCES departments(id)
);

-- payroll
CREATE TABLE payroll (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    basic_salary DECIMAL(10,2) NOT NULL,
    tax_deduction DECIMAL(10,2) NOT NULL,      -- 15%
    benefits_deduction DECIMAL(10,2) NOT NULL, -- 5%
    net_pay DECIMAL(10,2) NOT NULL,
    processed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);

-- leave_requests
CREATE TABLE leave_requests (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    leave_type ENUM('Annual','Sick','Personal') NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    days_requested INT NOT NULL,
    status ENUM('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING',
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);

-- leave_balances
CREATE TABLE leave_balances (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL UNIQUE,
    annual_balance INT DEFAULT 21,
    sick_balance INT DEFAULT 14,
    personal_balance INT DEFAULT 7,
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);
```

---

## Project File Structure

```
SDA/
├── pom.xml                          (Maven build, JavaFX 21, MySQL 8.0.33)
├── database/
│   └── schema.sql                   (CREATE TABLE + sample data)
├── src/main/
│   ├── java/com/hr/
│   │   ├── MainApp.java             (JavaFX Application entry point)
│   │   ├── model/
│   │   │   ├── Employee.java
│   │   │   ├── Department.java
│   │   │   ├── Payroll.java
│   │   │   └── LeaveRequest.java
│   │   ├── dao/
│   │   │   ├── DatabaseConnection.java  (Singleton)
│   │   │   ├── EmployeeDAO.java
│   │   │   ├── DepartmentDAO.java
│   │   │   ├── PayrollDAO.java
│   │   │   └── LeaveRequestDAO.java
│   │   ├── service/
│   │   │   ├── EmployeeService.java
│   │   │   ├── DepartmentService.java
│   │   │   ├── PayrollService.java      (TAX=15%, BENEFITS=5%)
│   │   │   └── LeaveRequestService.java
│   │   └── controller/
│   │       ├── MainController.java      (navigation between modules)
│   │       ├── EmployeeController.java
│   │       ├── DepartmentController.java
│   │       ├── PayrollController.java
│   │       └── LeaveRequestController.java
│   └── resources/
│       ├── database.properties          (db.url, db.user, db.password)
│       ├── styles.css                   (custom JavaFX CSS)
│       └── com/hr/
│           ├── main.fxml                (sidebar navigation)
│           ├── employee.fxml
│           ├── department.fxml
│           ├── payroll.fxml
│           └── leave_request.fxml
└── docs/
    ├── D1.pdf                           (Vision Document)
    ├── D2.pdf                           (Use Cases + Domain Model)
    ├── D3.pdf                           (SSDs + SDs + Class Diagram)
    ├── Rubric.pdf                       (Grading criteria, 140 marks)
    └── PROJECT_MASTER_REFERENCE.md      (this file)
```

---

## Running the Project

**Prerequisites:**
1. JDK 21+ (Temurin 25.0.2 installed)
2. MySQL 8.x running on localhost:3306
3. IntelliJ IDEA with Maven plugin

**Setup:**
```
1. Run database/schema.sql in MySQL Workbench
2. Verify src/main/resources/database.properties has correct password
3. Open IntelliJ → Load pom.xml as Maven project
4. Maven panel → Plugins → javafx → javafx:run
   OR: Run MainApp.java directly from IntelliJ (green play button)
```

**IntelliJ bundled Maven path:**
`C:\Program Files\JetBrains\IntelliJ IDEA 2026.1.1\plugins\maven\lib\maven3\bin\mvn.cmd`

---

## Key Design Decisions & Pattern Notes

1. **Singleton** (`DatabaseConnection`): Only one DB connection instance; lazy-initialized; checks if connection is closed before returning.

2. **DAO Pattern**: All SQL isolated in DAO classes. Service classes never write raw SQL. Enables swapping DB without touching business logic.

3. **Service Layer** (Information Expert + High Cohesion): All business rules live in Service classes:
   - `PayrollService`: calculates tax (15%), benefits (5%), net pay
   - `LeaveRequestService`: validates balance, deducts on approve, restores on delete-of-approved
   - `EmployeeService`: validates email uniqueness, required fields
   - `DepartmentService`: validates name uniqueness

4. **Controller** (GRASP Controller): JavaFX controllers handle UI events only — they call services, not DAOs directly. Example: `LeaveRequestController.handleApprove()` → calls `leaveRequestService.approveRequest()`.

5. **Low Coupling**: Controller → Service → DAO → Model. No layer skips. No circular dependencies.

6. **Protected Variation**: `DatabaseConnection` singleton shields all DAOs from the complexity of JDBC setup, connection properties, and reconnection logic.

7. **MVC**: FXML files = View, Controller classes = Controller, Model classes = Model. Service/DAO are behind the Controller.

---

## Color Coding in UI

- Status coloring in Leave Request table: APPROVED = green, REJECTED = red, PENDING = orange
- Approve/Reject buttons disabled unless a PENDING row is selected
- Sidebar: dark blue `#1a237e`; nav hover `#283593`; content area `#f5f5f5`
- Button classes: `btn-primary` (blue), `btn-danger` (red), `btn-success` (green), `btn-warning` (orange)
