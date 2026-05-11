# HR Management System — Requirements Traceability Document

> **Project:** Software Design & Architecture (SDA) — HR Management System  
> **Stack:** Java 17 · JavaFX · MySQL  
> **Date:** 2026-05-09

---

## Table of Contents

1. [Functional Requirements](#1-functional-requirements)
   - [FR-01 Employee Registration](#fr-01-employee-registration)
   - [FR-02 Employee Update](#fr-02-employee-update)
   - [FR-03 Department Assignment / Transfer](#fr-03-department-assignment--transfer)
   - [FR-04 Employee Offboarding](#fr-04-employee-offboarding)
   - [FR-05 Authentication & Session Management](#fr-05-authentication--session-management)
   - [FR-06 Leave Request Submission](#fr-06-leave-request-submission)
   - [FR-07 Leave Approval / Rejection](#fr-07-leave-approval--rejection)
   - [FR-08 Attendance Correction Request](#fr-08-attendance-correction-request)
   - [FR-09 Employee Profile View (Role-Based)](#fr-09-employee-profile-view-role-based)
   - [FR-10 Leave & Attendance Dashboard](#fr-10-leave--attendance-dashboard)
   - [FR-11 Probation Period Monitoring](#fr-11-probation-period-monitoring)
   - [FR-12 Performance Evaluation](#fr-12-performance-evaluation)
   - [FR-13 Payroll Processing](#fr-13-payroll-processing)
   - [FR-14 Audit Logging](#fr-14-audit-logging)
   - [FR-15 Report Generation](#fr-15-report-generation)
2. [Non-Functional Requirements](#2-non-functional-requirements)
   - [NFR-01 Security](#nfr-01-security)
   - [NFR-02 Role-Based Access Control](#nfr-02-role-based-access-control)
   - [NFR-03 Data Integrity & Validation](#nfr-03-data-integrity--validation)
   - [NFR-04 Performance & Scalability](#nfr-04-performance--scalability)
   - [NFR-05 Maintainability & Design Patterns](#nfr-05-maintainability--design-patterns)
   - [NFR-06 Usability](#nfr-06-usability)
   - [NFR-07 Reliability & Error Handling](#nfr-07-reliability--error-handling)
   - [NFR-08 Extensibility](#nfr-08-extensibility)

---

## 1. Functional Requirements

---

### FR-01 Employee Registration

**Use Case:** UC-01  
**Description:** An HR admin can register a new employee, auto-provision a user account, validate uniqueness, enforce department headcount, auto-calculate probation, and send a welcome notification.

#### Implemented Sub-Requirements

| Sub-Req | Description | File | Class · Method |
|---------|-------------|------|----------------|
| FR-01-1 | Collect and validate personal details (name, DOB, gender, NID, email, phone, address) | `src/main/java/com/hr/ui/RegisterEmployeeController.java` | `RegisterEmployeeController.validateForm()` |
| FR-01-2 | Detect duplicate National ID before insert | `src/main/java/com/hr/controller/RegisterController.java` | `RegisterController.checkNationalIdDuplicate()` |
| FR-01-3 | Detect duplicate Email before insert | `src/main/java/com/hr/controller/RegisterController.java` | `RegisterController.checkEmailDuplicate()` |
| FR-01-4 | Check department headcount capacity | `src/main/java/com/hr/controller/RegisterController.java` | `RegisterController.isDepartmentAtCapacity()` |
| FR-01-5 | Allow admin to override capacity limit | `src/main/java/com/hr/ui/RegisterEmployeeController.java` | `RegisterEmployeeController.checkDepartmentCapacity()` |
| FR-01-6 | Auto-calculate probation end date from hire date | `src/main/java/com/hr/ui/RegisterEmployeeController.java` | `RegisterEmployeeController.recalcProbation()` |
| FR-01-7 | Persist employee to DB | `src/main/java/com/hr/dao/EmployeeDAO.java` | `EmployeeDAO.insert(Employee)` |
| FR-01-8 | Auto-create user account with hashed password | `src/main/java/com/hr/controller/RegisterController.java` | `RegisterController.createUserAccount()` |
| FR-01-9 | Send welcome email with fallback to in-app notification | `src/main/java/com/hr/controller/RegisterController.java` | `RegisterController.sendWelcomeEmail()` → `NotificationService.sendWelcomeEmailWithFallback()` |
| FR-01-10 | Clear form fields after successful registration | `src/main/java/com/hr/ui/RegisterEmployeeController.java` | `RegisterEmployeeController.handleClear()` |
| FR-01-11 | Display validation errors inline | `src/main/java/com/hr/ui/RegisterEmployeeController.java` | `RegisterEmployeeController.handleSubmit()` |

**Supporting Files:**
- `src/main/java/com/hr/model/Employee.java` — Employee entity with all fields
- `src/main/java/com/hr/service/NotificationService.java` — `sendWelcomeEmail()`, `sendWelcomeEmailWithFallback()`
- `src/main/java/com/hr/dao/UserAccountDAO.java` — `insert(UserAccount)`
- `src/main/resources/com/hr/register_employee.fxml` — Registration UI layout

---

### FR-02 Employee Update

**Use Case:** UC-02  
**Description:** HR can update any editable field of an existing employee record; every changed field is recorded in the audit log.

| Sub-Req | Description | File | Class · Method |
|---------|-------------|------|----------------|
| FR-02-1 | Load existing employee data into form | `src/main/java/com/hr/ui/UpdateEmployeeController.java` | `UpdateEmployeeController.setEmployee(Employee)` |
| FR-02-2 | Validate all modified fields | `src/main/java/com/hr/controller/UpdateController.java` | `UpdateController.validateChanges()` |
| FR-02-3 | Persist updated employee to DB | `src/main/java/com/hr/dao/EmployeeDAO.java` | `EmployeeDAO.update(Employee)` |
| FR-02-4 | Log every changed field (old value → new value) | `src/main/java/com/hr/controller/UpdateController.java` | `UpdateController.logChangedFields()` → `AuditLogService.writeAuditLog()` |
| FR-02-5 | Reject update if duplicate email/NID detected | `src/main/java/com/hr/dao/EmployeeDAO.java` | `EmployeeDAO.existsByEmail()`, `existsByNationalId()` |

**Supporting Files:**
- `src/main/java/com/hr/service/AuditLogService.java` — `writeAuditLog()`
- `src/main/java/com/hr/dao/AuditLogDAO.java` — `log(userId, action, entityType, entityId, field, oldVal, newVal)`
- `src/main/resources/com/hr/update_employee.fxml` — Update UI layout

---

### FR-03 Department Assignment / Transfer

**Use Case:** UC-03  
**Description:** HR transfers an employee to another department, enforcing headcount limits, supporting backdated transfers, and notifying managers.

| Sub-Req | Description | File | Class · Method |
|---------|-------------|------|----------------|
| FR-03-1 | Validate target department capacity | `src/main/java/com/hr/controller/AssignmentController.java` | `AssignmentController.enforceDepartmentCapacity()` |
| FR-03-2 | Allow admin override with written justification | `src/main/java/com/hr/controller/AssignmentController.java` | `AssignmentController.overrideCapacity()` |
| FR-03-3 | Allow backdated effective date | `src/main/java/com/hr/model/EmployeeAssignment.java` | `EmployeeAssignment.backdated` field |
| FR-03-4 | Persist assignment record | `src/main/java/com/hr/dao/EmployeeAssignmentDAO.java` | `EmployeeAssignmentDAO.insert(EmployeeAssignment)` |
| FR-03-5 | Notify source & target managers | `src/main/java/com/hr/controller/AssignmentController.java` | `AssignmentController.notifyManagers()` → `NotificationService.notifyManagers()` |
| FR-03-6 | UI override dialog when capacity exceeded | `src/main/java/com/hr/ui/AssignDepartmentController.java` | `AssignDepartmentController.requestCapacityOverride()` |

**Supporting Files:**
- `src/main/java/com/hr/model/EmployeeAssignment.java` — Assignment entity
- `src/main/java/com/hr/dao/DepartmentDAO.java` — `hasEmployees()`, department capacity fields
- `src/main/resources/com/hr/assign_department.fxml` — Transfer UI layout

---

### FR-04 Employee Offboarding

**Use Case:** UC-04  
**Description:** HR initiates an exit workflow for separating employees, tracks checklist completion, validates notice period, and finalizes settlement.

| Sub-Req | Description | File | Class · Method |
|---------|-------------|------|----------------|
| FR-04-1 | Initiate offboarding workflow (separation type, reason, last working day) | `src/main/java/com/hr/controller/OffboardingController.java` | `OffboardingController.initiateOffboarding()` |
| FR-04-2 | Validate minimum notice period | `src/main/java/com/hr/model/OffboardingWorkflow.java` | `OffboardingWorkflow.validateNoticePeriod()` |
| FR-04-3 | Track checklist item completion | `src/main/java/com/hr/controller/OffboardingController.java` | `OffboardingController.updateChecklistStatus()` |
| FR-04-4 | Finalize settlement and mark employee inactive | `src/main/java/com/hr/controller/OffboardingController.java` | `OffboardingController.finalizeSettlement()` |
| FR-04-5 | Persist offboarding record to DB | `src/main/java/com/hr/dao/OffboardingWorkflowDAO.java` | `OffboardingWorkflowDAO.insert()`, `update()` |

**Supporting Files:**
- `src/main/java/com/hr/model/OffboardingWorkflow.java` — Offboarding entity with checklist, settlement status
- `src/main/resources/com/hr/offboarding.fxml` — Offboarding UI layout

---

### FR-05 Authentication & Session Management

**Description:** Users log in with username + password; the system authenticates via SHA-256 hash comparison and establishes a role-aware session.

| Sub-Req | Description | File | Class · Method |
|---------|-------------|------|----------------|
| FR-05-1 | Hash password with SHA-256 on login attempt | `src/main/java/com/hr/service/AuthService.java` | `AuthService.hashPassword(String)` |
| FR-05-2 | Validate credentials against DB | `src/main/java/com/hr/service/AuthService.java` | `AuthService.login(username, password)` |
| FR-05-3 | Create session with logged-in user & role | `src/main/java/com/hr/service/SessionManager.java` | `SessionManager.setCurrentUser(UserAccount)` |
| FR-05-4 | Expose role checks throughout app | `src/main/java/com/hr/service/SessionManager.java` | `SessionManager.isAdmin()`, `isHR()`, `isEmployee()` |
| FR-05-5 | Animate shake on invalid login | `src/main/java/com/hr/ui/LoginController.java` | `LoginController.shakeCard()` |
| FR-05-6 | Show inline error message on failure | `src/main/java/com/hr/ui/LoginController.java` | `LoginController.showError(String)` |
| FR-05-7 | Logout and clear session | `src/main/java/com/hr/ui/MainController.java` | `MainController.handleLogout()` → `SessionManager.logout()` |

**Supporting Files:**
- `src/main/java/com/hr/dao/UserAccountDAO.java` — `getByUsername()`
- `src/main/resources/com/hr/login.fxml` — Login UI layout

---

### FR-06 Leave Request Submission

**Use Case:** UC-06  
**Description:** An employee applies for leave with full validation across 6 alternative flows: balance check, holiday conflict detection, overlap check, probation restriction, document upload requirement, and notification fallback.

| Sub-Req | Description | File | Class · Method |
|---------|-------------|------|----------------|
| FR-06-1 | Check remaining leave balance before submission | `src/main/java/com/hr/service/LeaveRequestService.java` | `LeaveRequestService.getLeaveBalance()` |
| FR-06-2 | Detect public holidays within requested range | `src/main/java/com/hr/service/LeaveRequestService.java` | `LeaveRequestService.getHolidaysInRange()` → `LeaveRequestDAO.getHolidayMapInRange()` |
| FR-06-3 | Detect overlapping pending/approved requests | `src/main/java/com/hr/service/LeaveRequestService.java` | `LeaveRequestService.getOverlappingRequests()` |
| FR-06-4 | Block ANNUAL/PERSONAL leave during probation; always allow SICK | `src/main/java/com/hr/service/LeaveRequestService.java` | `LeaveRequestService.applyForLeave()` — probation check block |
| FR-06-5 | Require document upload for SICK leave > 3 days | `src/main/java/com/hr/service/LeaveRequestService.java` | `LeaveRequestService.submitDocument()` |
| FR-06-6 | Calculate working days (excluding weekends + holidays) | `src/main/java/com/hr/service/LeaveRequestService.java` | `LeaveRequestService.calculateWorkingDays()` |
| FR-06-7 | Persist leave request with PENDING status | `src/main/java/com/hr/dao/LeaveRequestDAO.java` | `LeaveRequestDAO.insert(LeaveRequest)` |
| FR-06-8 | Notify HR of new leave request; fallback on email failure | `src/main/java/com/hr/service/NotificationService.java` | `NotificationService.notifyHR()` |
| FR-06-9 | UI: apply button, document upload, card listing | `src/main/java/com/hr/ui/LeaveRequestController.java` | `LeaveRequestController.handleApply()`, `handleSubmitDocument()` |

**Supporting Files:**
- `src/main/java/com/hr/model/LeaveRequest.java` — LeaveRequest entity
- `src/main/java/com/hr/controller/LeaveController.java` — `submitLeaveRequest()`
- `src/main/resources/com/hr/leave_request.fxml` — Leave request UI

---

### FR-07 Leave Approval / Rejection

**Use Case:** UC-07  
**Description:** HR or a manager reviews pending leave requests, approves or rejects them with optional comments, and deducts balance on approval.

| Sub-Req | Description | File | Class · Method |
|---------|-------------|------|----------------|
| FR-07-1 | List all pending leave requests | `src/main/java/com/hr/ui/LeaveRequestController.java` | `LeaveRequestController.populateCards()` |
| FR-07-2 | Approve request and update status | `src/main/java/com/hr/controller/LeaveApprovalController.java` | `LeaveApprovalController.processLeaveDecision()` → `LeaveRequestDAO.updateApproval()` |
| FR-07-3 | Deduct approved days from leave balance | `src/main/java/com/hr/dao/LeaveRequestDAO.java` | `LeaveRequestDAO.deductBalance()` |
| FR-07-4 | Reject request with reason | `src/main/java/com/hr/ui/LeaveRequestController.java` | `LeaveRequestController.handleRejectCard()` |
| FR-07-5 | Notify employee of decision | `src/main/java/com/hr/controller/LeaveApprovalController.java` | `LeaveApprovalController.notifyDecision()` → `NotificationService.notifyEmployee()` |

**Supporting Files:**
- `src/main/java/com/hr/dao/LeaveRequestDAO.java` — `updateStatus()`, `updateApproval()`, `deductBalance()`

---

### FR-08 Attendance Correction Request

**Use Case:** UC-08  
**Description:** An employee can request a correction for an erroneous attendance record within a 7-day window, providing justification. HR reviews and approves/rejects.

| Sub-Req | Description | File | Class · Method |
|---------|-------------|------|----------------|
| FR-08-1 | Validate that correction is within 7-day window | `src/main/java/com/hr/model/AttendanceRecord.java` | `AttendanceRecord.validateCorrectionWindow()` |
| FR-08-2 | Validate logical consistency (check-in before check-out) | `src/main/java/com/hr/model/AttendanceCorrectionRequest.java` | `AttendanceCorrectionRequest.validateLogicalConsistency()` |
| FR-08-3 | Submit correction request with justification | `src/main/java/com/hr/controller/AttendanceCorrectionController.java` | `AttendanceCorrectionController.submitCorrectionRequest()` |
| FR-08-4 | HR approves correction and updates original record | `src/main/java/com/hr/controller/AttendanceCorrectionController.java` | `AttendanceCorrectionController.approveCorrectionRequest()` |
| FR-08-5 | HR rejects correction with reason | `src/main/java/com/hr/controller/AttendanceCorrectionController.java` | `AttendanceCorrectionController.rejectCorrectionRequest()` |
| FR-08-6 | Persist correction request to DB | `src/main/java/com/hr/dao/AttendanceCorrectionDAO.java` | `AttendanceCorrectionDAO.insert()`, `updateStatus()` |
| FR-08-7 | Log correction attempt in audit trail | `src/main/java/com/hr/service/AuditLogService.java` | `AuditLogService.recordCorrectionAttempt()` |

**Supporting Files:**
- `src/main/java/com/hr/model/AttendanceCorrectionRequest.java` — Correction entity
- `src/main/resources/com/hr/attendance_correction.fxml` — Correction UI

---

### FR-09 Employee Profile View (Role-Based)

**Use Case:** UC-09  
**Description:** Employees can view their own profile; HR/Admin can view all fields. Sensitive fields (salary, performance) are masked for the Employee role.

| Sub-Req | Description | File | Class · Method |
|---------|-------------|------|----------------|
| FR-09-1 | Fetch employee profile data | `src/main/java/com/hr/controller/ProfileController.java` | `ProfileController.getProfileData()` |
| FR-09-2 | Determine permitted fields per role | `src/main/java/com/hr/service/AccessControlService.java` | `AccessControlService.getPermittedFields(role)` |
| FR-09-3 | Validate field access before display | `src/main/java/com/hr/controller/ProfileController.java` | `ProfileController.validateFieldAccess()` → `AccessControlService.canAccess()` |
| FR-09-4 | Mask salary and performance score for Employee role | `src/main/java/com/hr/service/AccessControlService.java` | `AccessControlService.canAccess()` — EMPLOYEE case |
| FR-09-5 | Log read-access to audit trail | `src/main/java/com/hr/service/AuditLogService.java` | `AuditLogService.logReadAccess()` |

**Supporting Files:**
- `src/main/resources/com/hr/employee_profile.fxml` — Profile UI layout

---

### FR-10 Leave & Attendance Dashboard

**Use Case:** UC-10  
**Description:** Employees and HR can view a summary dashboard showing remaining leave balances per type and monthly attendance statistics.

| Sub-Req | Description | File | Class · Method |
|---------|-------------|------|----------------|
| FR-10-1 | Retrieve leave balance per type (ANNUAL, SICK, PERSONAL) | `src/main/java/com/hr/controller/LeaveDashboardController.java` | `LeaveDashboardController.getLeaveBalance()` → `LeaveRequestDAO.getBalance()` |
| FR-10-2 | Retrieve attendance summary (present, absent, late) | `src/main/java/com/hr/controller/LeaveDashboardController.java` | `LeaveDashboardController.getAttendanceSummary()` |
| FR-10-3 | Generate downloadable dashboard report | `src/main/java/com/hr/controller/LeaveDashboardController.java` | `LeaveDashboardController.generateDashboardReport()` |
| FR-10-4 | Display in JavaFX dashboard screen | `src/main/java/com/hr/ui/DashboardController.java` | `DashboardController.viewLeaveAndAttendanceDashboard()` |
| FR-10-5 | Auto-flag attendance status (PRESENT/ABSENT/LATE) | `src/main/java/com/hr/model/AttendanceRecord.java` | `AttendanceRecord.flagAttendanceStatus()` |
| FR-10-6 | Compute total work hours for a record | `src/main/java/com/hr/model/AttendanceRecord.java` | `AttendanceRecord.computeWorkHours()` |

**Supporting Files:**
- `src/main/java/com/hr/dao/AttendanceRecordDAO.java` — `getByEmployee()`
- `src/main/resources/com/hr/dashboard.fxml` — Dashboard UI layout

---

### FR-11 Probation Period Monitoring

**Use Case:** UC-11  
**Description:** HR monitors probation periods, can extend them, and records a final decision (confirm/terminate) with notifications sent to relevant parties.

| Sub-Req | Description | File | Class · Method |
|---------|-------------|------|----------------|
| FR-11-1 | Display active probation records | `src/main/java/com/hr/controller/ProbationMonitorController.java` | `ProbationMonitorController` — list view |
| FR-11-2 | Extend probation period with reason | `src/main/java/com/hr/controller/ProbationMonitorController.java` | `ProbationMonitorController.extendProbation()` |
| FR-11-3 | Record final probation decision (CONFIRM/TERMINATE) | `src/main/java/com/hr/controller/ProbationMonitorController.java` | `ProbationMonitorController.makeProbationDecision()` |
| FR-11-4 | Notify employee and HR of decision | `src/main/java/com/hr/controller/ProbationMonitorController.java` | `ProbationMonitorController.notifyDecision()` → `NotificationService.notifyProbationDecision()` |
| FR-11-5 | Persist probation record updates | `src/main/java/com/hr/dao/ProbationRecordDAO.java` | `ProbationRecordDAO.insert()`, `update()` |
| FR-11-6 | Auto-set probation end date during registration | `src/main/java/com/hr/ui/RegisterEmployeeController.java` | `RegisterEmployeeController.recalcProbation()` |

**Supporting Files:**
- `src/main/java/com/hr/model/ProbationRecord.java` — Probation entity with extensions count, decision
- `src/main/resources/com/hr/probation.fxml` — Probation monitoring UI

---

### FR-12 Performance Evaluation

**Use Case:** UC-12  
**Description:** Evaluators (HR/managers) submit scored evaluations for employees within active evaluation cycles; scores are validated (0–100) and remarks are mandatory.

| Sub-Req | Description | File | Class · Method |
|---------|-------------|------|----------------|
| FR-12-1 | Create and configure evaluation cycles | `src/main/java/com/hr/controller/EvaluationCycleController.java` | `EvaluationCycleController.createCycle()` |
| FR-12-2 | Validate cycle period (start before end, no overlap) | `src/main/java/com/hr/model/EvaluationCycle.java` | `EvaluationCycle.validatePeriod()` |
| FR-12-3 | Distribute evaluation tasks to evaluators | `src/main/java/com/hr/controller/EvaluationCycleController.java` | `EvaluationCycleController.distributeTasks()` → `NotificationService.distributeEvaluationTasks()` |
| FR-12-4 | Submit evaluation with score (0–100) and remarks | `src/main/java/com/hr/controller/PerformanceEvalController.java` | `PerformanceEvalController.submitEvaluation()` |
| FR-12-5 | Validate evaluation form (score range + remarks not empty) | `src/main/java/com/hr/model/PerformanceEvaluation.java` | `PerformanceEvaluation.validateForm()` |
| FR-12-6 | Log evaluation submission to audit trail | `src/main/java/com/hr/service/AuditLogService.java` | `AuditLogService.logEvaluationSubmission()` |
| FR-12-7 | Close evaluation cycle | `src/main/java/com/hr/controller/EvaluationCycleController.java` | `EvaluationCycleController.closeCycle()` |
| FR-12-8 | Persist evaluations to DB | `src/main/java/com/hr/dao/PerformanceEvaluationDAO.java` | `PerformanceEvaluationDAO.insert()`, `update()` |

**Supporting Files:**
- `src/main/java/com/hr/model/EvaluationCycle.java` — Cycle entity with grace period, reminder schedule
- `src/main/resources/com/hr/performance.fxml` — Performance evaluation UI

---

### FR-13 Payroll Processing

**Description:** HR processes monthly payroll for an employee, computing tax (15%), benefits deduction (5%), and net pay; results are persisted and can be deleted.

| Sub-Req | Description | File | Class · Method |
|---------|-------------|------|----------------|
| FR-13-1 | Calculate payroll breakdown (gross → tax → benefits → net) | `src/main/java/com/hr/service/PayrollService.java` | `PayrollService.calculateBreakdown()` |
| FR-13-2 | Persist payroll record | `src/main/java/com/hr/service/PayrollService.java` | `PayrollService.processPayroll()` → `PayrollDAO.insert()` |
| FR-13-3 | Retrieve payroll history per employee | `src/main/java/com/hr/service/PayrollService.java` | `PayrollService.getPayrollsForEmployee()` |
| FR-13-4 | Delete payroll record | `src/main/java/com/hr/service/PayrollService.java` | `PayrollService.deletePayroll()` → `PayrollDAO.delete()` |
| FR-13-5 | Send to external payroll integration | `src/main/java/com/hr/service/PayrollIntegration.java` | `PayrollIntegration.sendToPayrollSystem()` → `PayrollSystemAdapter.adapt()` |
| FR-13-6 | Notify on payroll processed | `src/main/java/com/hr/service/PayrollNotifier.java` | `PayrollNotifier.notifyPayrollProcessed()` |
| FR-13-7 | Display payroll in UI with breakdown | `src/main/java/com/hr/ui/PayrollController.java` | `PayrollController.displayBreakdown()` |

**Supporting Files:**
- `src/main/java/com/hr/model/Payroll.java` — Payroll entity
- `src/main/java/com/hr/dao/PayrollDAO.java` — CRUD
- `src/main/java/com/hr/service/ExternalPayrollSystem.java` — Mock external adapter
- `src/main/resources/com/hr/payroll.fxml` — Payroll UI

---

### FR-14 Audit Logging

**Description:** All sensitive data operations (create, update, delete, read of restricted fields) are recorded in an immutable audit log with user, timestamp, entity, and old/new values.

| Sub-Req | Description | File | Class · Method |
|---------|-------------|------|----------------|
| FR-14-1 | Log any field-level change with old/new values | `src/main/java/com/hr/service/AuditLogService.java` | `AuditLogService.writeAuditLog()` |
| FR-14-2 | Log sensitive read access | `src/main/java/com/hr/service/AuditLogService.java` | `AuditLogService.logReadAccess()` |
| FR-14-3 | Log attendance correction attempts | `src/main/java/com/hr/service/AuditLogService.java` | `AuditLogService.recordCorrectionAttempt()` |
| FR-14-4 | Log performance evaluation submissions | `src/main/java/com/hr/service/AuditLogService.java` | `AuditLogService.logEvaluationSubmission()` |
| FR-14-5 | Persist audit log to DB | `src/main/java/com/hr/dao/AuditLogDAO.java` | `AuditLogDAO.log(userId, action, entityType, entityId, field, oldVal, newVal)` |
| FR-14-6 | Trigger audit log via Observer pattern automatically | `src/main/java/com/hr/service/AuditLogObserver.java` | `AuditLogObserver.onHREvent()` |

---

### FR-15 Report Generation

**Description:** HR can generate Compliance Reports and HR Analytics Reports in PDF, Excel, or CSV formats; reports are archived for future retrieval.

| Sub-Req | Description | File | Class · Method |
|---------|-------------|------|----------------|
| FR-15-1 | Generate compliance report with aggregated data | `src/main/java/com/hr/controller/ComplianceReportController.java` | `ComplianceReportController.generateComplianceReport()` |
| FR-15-2 | Generate HR analytics report | `src/main/java/com/hr/controller/HRAnalyticsController.java` | `HRAnalyticsController.generateAnalyticsReport()` |
| FR-15-3 | Format report as PDF | `src/main/java/com/hr/service/PdfFormatStrategy.java` | `PdfFormatStrategy.format(String)` |
| FR-15-4 | Format report as Excel | `src/main/java/com/hr/service/ExcelFormatStrategy.java` | `ExcelFormatStrategy.format(String)` |
| FR-15-5 | Format report as CSV | `src/main/java/com/hr/service/CsvFormatStrategy.java` | `CsvFormatStrategy.format(String)` |
| FR-15-6 | Register and select format strategy at runtime | `src/main/java/com/hr/service/ReportGenerator.java` | `ReportGenerator.registerStrategy()`, `generateAndFormat()` |
| FR-15-7 | Archive generated report | `src/main/java/com/hr/service/ReportArchive.java` | `ReportArchive.archiveReport()` |
| FR-15-8 | Retrieve archived report | `src/main/java/com/hr/service/ReportArchive.java` | `ReportArchive.retrieveReport()` |
| FR-15-9 | Aggregate attendance & leave data for compliance | `src/main/java/com/hr/service/ComplianceDataAgg.java` | `ComplianceDataAgg.aggregateAttendance()`, `aggregateLeaveCompliance()` |
| FR-15-10 | Aggregate HR metrics by department | `src/main/java/com/hr/service/AnalyticsAggregator.java` | `AnalyticsAggregator.aggregateByDepartment()`, `aggregateByLeaveType()` |
| FR-15-11 | Create report via Factory Method | `src/main/java/com/hr/service/ComplianceReportCreator.java` | `ComplianceReportCreator.create()` |
| FR-15-12 | Persist report to DB | `src/main/java/com/hr/dao/ComplianceReportDAO.java` | `ComplianceReportDAO.insert()` |

**Supporting Files:**
- `src/main/java/com/hr/model/ComplianceReport.java` — Compliance report entity
- `src/main/java/com/hr/model/HRAnalyticsReport.java` — Analytics report entity
- `src/main/java/com/hr/model/Report.java` — Abstract base report (Template Method pattern)
- `src/main/resources/com/hr/analytics.fxml` — Analytics UI

---

## 2. Non-Functional Requirements

---

### NFR-01 Security

**Requirement:** All credentials must be stored and verified using a secure one-way hash. No plaintext passwords may appear in the database.

| Implementation | File | Class · Method |
|---------------|------|----------------|
| SHA-256 password hashing on login | `src/main/java/com/hr/service/AuthService.java` | `AuthService.hashPassword(String)` |
| SHA-256 hashing on account creation | `src/main/java/com/hr/controller/RegisterController.java` | `RegisterController.createUserAccount()` — password hashed before `UserAccountDAO.insert()` |
| Encrypted sensitive data storage | `src/main/java/com/hr/service/SecureStorage.java` | `SecureStorage.encrypt()`, `SecureStorage.decrypt()` |
| Credentials stored as `password_hash` column | `src/main/java/com/hr/dao/UserAccountDAO.java` | Schema column: `password_hash` |

---

### NFR-02 Role-Based Access Control

**Requirement:** The system must enforce three distinct roles — ADMIN, HR, EMPLOYEE — with different permissions for data access and UI actions.

| Implementation | File | Class · Method |
|---------------|------|----------------|
| Role constants and session state | `src/main/java/com/hr/service/SessionManager.java` | `SessionManager.getRole()`, `isAdmin()`, `isHR()`, `isEmployee()` |
| Field-level permission matrix per role | `src/main/java/com/hr/service/AccessControlService.java` | `AccessControlService.getPermittedFields(role)`, `canAccess(role, field)` |
| Role-based sidebar button visibility | `src/main/java/com/hr/ui/MainController.java` | `MainController.applyRoleAccess()` |
| Salary & performance masked for Employee | `src/main/java/com/hr/service/AccessControlService.java` | `canAccess()` — EMPLOYEE branch |
| Capacity override restricted to Admin | `src/main/java/com/hr/controller/RegisterController.java` | `RegisterController.isDepartmentAtCapacity()` — role check |
| Sensitive read access logged | `src/main/java/com/hr/service/AuditLogService.java` | `AuditLogService.logReadAccess()` |

---

### NFR-03 Data Integrity & Validation

**Requirement:** All user inputs must be validated at both the model layer (business rules) and UI layer (form validation) before any database operation.

| Implementation | File | Class · Method |
|---------------|------|----------------|
| Employee form field validation (UI layer) | `src/main/java/com/hr/ui/RegisterEmployeeController.java` | `RegisterEmployeeController.validateForm()` |
| Duplicate NID/email validation (service layer) | `src/main/java/com/hr/controller/RegisterController.java` | `checkNationalIdDuplicate()`, `checkEmailDuplicate()` |
| DB-level uniqueness check | `src/main/java/com/hr/dao/EmployeeDAO.java` | `existsByNationalId()`, `existsByEmail()` |
| Leave working-day calculation (excludes weekends + holidays) | `src/main/java/com/hr/service/LeaveRequestService.java` | `calculateWorkingDays()` |
| Attendance time logic validation | `src/main/java/com/hr/model/AttendanceCorrectionRequest.java` | `validateLogicalConsistency()` |
| Attendance correction 7-day window | `src/main/java/com/hr/model/AttendanceRecord.java` | `validateCorrectionWindow()` |
| Probation notice period validation | `src/main/java/com/hr/model/OffboardingWorkflow.java` | `validateNoticePeriod()` |
| Evaluation cycle period validation | `src/main/java/com/hr/model/EvaluationCycle.java` | `validatePeriod()` |
| Evaluation score range (0–100) + remarks required | `src/main/java/com/hr/model/PerformanceEvaluation.java` | `validateForm()` |
| Department capacity enforcement | `src/main/java/com/hr/service/EmployeeService.java` | `enforceCapacity()` |

---

### NFR-04 Performance & Scalability

**Requirement:** Database connections must be pooled/reused; repeated operations must not open new connections unnecessarily. Performance metrics must be observable.

| Implementation | File | Class · Method |
|---------------|------|----------------|
| Singleton DB connection (reuse across DAOs) | `src/main/java/com/hr/dao/DatabaseConnection.java` | `DatabaseConnection.getInstance()`, `getConnection()` |
| Execution time monitoring | `src/main/java/com/hr/service/PerformanceMonitor.java` | `PerformanceMonitor.monitorExecutionTime()`, `logMetrics()` |
| Lazy data loading in employee card grid | `src/main/java/com/hr/ui/EmployeeController.java` | `EmployeeController.loadData()` — loads on demand |
| Paginated / filtered card rendering | `src/main/java/com/hr/ui/EmployeeController.java` | `EmployeeController.filterCards()`, `populateCards()` |
| Task scheduling for async operations | `src/main/java/com/hr/service/TaskScheduler.java` | `TaskScheduler.scheduleTask()`, `cancelTask()` |

---

### NFR-05 Maintainability & Design Patterns

**Requirement:** The system must be structured using recognized design patterns to ensure low coupling, high cohesion, and ease of future extension.

| Pattern | Purpose | Primary File(s) |
|---------|---------|----------------|
| **Singleton** | One DB connection + one session per run | `DatabaseConnection.java`, `SessionManager.java` |
| **DAO Pattern** | Decouple data access from business logic | All `*DAO.java` in `com.hr.dao` |
| **Service Layer** | Centralize business rules away from UI | All `*Service.java` in `com.hr.service` |
| **MVC** | Separate model, view, controller concerns | `com.hr.model` / `*.fxml` / `com.hr.controller` + `com.hr.ui` |
| **Observer Pattern** | Decouple event publishers from handlers | `HREventPublisher.java` → `NotificationObserver.java`, `AuditLogObserver.java` |
| **Strategy Pattern** | Swap report formats at runtime | `ReportGenerator.java` + `PdfFormatStrategy.java`, `ExcelFormatStrategy.java`, `CsvFormatStrategy.java` |
| **Factory Method** | Standardize report object creation | `ReportCreator.java` → `ComplianceReportCreator.java`, `AnalyticsReportCreator.java` |
| **Adapter Pattern** | Wrap external payroll system API | `PayrollSystemAdapter.java` wraps `ExternalPayrollSystem.java` |
| **Repository Pattern** | Unified data-access abstraction | `EmployeeRepository.java` |
| **Template Method** | Shared report structure with variable parts | Abstract `Report.java` → `ComplianceReport.java`, `HRAnalyticsReport.java` |
| **GRASP: Pure Fabrication** | Stateless utility services | `NotificationService.java`, `AuditLogService.java`, `AccessControlService.java`, `ReportGenerator.java` |

---

### NFR-06 Usability

**Requirement:** The UI must provide intuitive navigation, real-time validation feedback, and responsive animations to guide the user.

| Implementation | File | Class · Method |
|---------------|------|----------------|
| Animated collapsible sidebar | `src/main/java/com/hr/ui/MainController.java` | `MainController.toggleSidebar()` — min+pref+max animated together |
| Role-based sidebar button visibility | `src/main/java/com/hr/ui/MainController.java` | `MainController.applyRoleAccess()` |
| Login shake animation on failure | `src/main/java/com/hr/ui/LoginController.java` | `LoginController.shakeCard()` |
| Inline error messages on forms | `src/main/java/com/hr/ui/RegisterEmployeeController.java` | `RegisterEmployeeController.validateForm()` |
| Card-based employee listing with search | `src/main/java/com/hr/ui/EmployeeController.java` | `filterCards()`, `populateCards()` |
| Real-time probation date recalculation | `src/main/java/com/hr/ui/RegisterEmployeeController.java` | `recalcProbation()` — fires on hire date change |
| Dynamic module loading on navigation | `src/main/java/com/hr/ui/MainController.java` | `MainController.loadModule()` |

---

### NFR-07 Reliability & Error Handling

**Requirement:** The system must gracefully handle failures (email errors, DB errors, capacity limits) without crashing and must provide fallback paths.

| Implementation | File | Class · Method |
|---------------|------|----------------|
| Email failure fallback to in-app notification | `src/main/java/com/hr/service/NotificationService.java` | `sendWelcomeEmailWithFallback()` |
| HR notification fallback on leave submission | `src/main/java/com/hr/service/LeaveRequestService.java` | `applyForLeave()` — catch block triggers fallback notify |
| Capacity warning without hard block (with override) | `src/main/java/com/hr/controller/RegisterController.java` | `isDepartmentAtCapacity()` — returns warning, allows override |
| Probation check does not block SICK leave | `src/main/java/com/hr/service/LeaveRequestService.java` | `applyForLeave()` — SICK leave exempted from probation block |
| SQLException propagation to UI for user feedback | All DAO classes | Methods throw `SQLException`; UI controllers catch and display errors |

---

### NFR-08 Extensibility

**Requirement:** New report formats, notification channels, and HR event handlers must be addable without modifying existing classes (Open/Closed Principle).

| Implementation | File | Class · Method |
|---------------|------|----------------|
| Add new report format by implementing strategy | `src/main/java/com/hr/service/ReportGenerator.java` | `registerStrategy(name, strategy)` — pluggable |
| Add new event handler by implementing observer | `src/main/java/com/hr/service/HREventObserver.java` | `onHREvent()` interface |
| Subscribe new observers without changing publisher | `src/main/java/com/hr/service/HREventPublisher.java` | `subscribe(observer)` |
| Add new report type by extending factory | `src/main/java/com/hr/service/ReportCreator.java` | Abstract `create()` — subclass for new types |
| Add new role permissions by extending access matrix | `src/main/java/com/hr/service/AccessControlService.java` | `getPermittedFields(role)` — add new role case |

---

## Summary Statistics

| Category | Count |
|----------|-------|
| Functional Requirements (total sub-requirements) | 98 |
| Non-Functional Requirements | 8 |
| Use Cases Fully Implemented | 12 (UC-01 through UC-12) |
| Java Source Files | ~80 |
| FXML UI Screens | 16 |
| Design Patterns Applied | 11 |
| Database Tables | 17 |
