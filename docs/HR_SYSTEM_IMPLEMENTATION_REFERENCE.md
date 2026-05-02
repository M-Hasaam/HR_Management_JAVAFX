# HR Management System — Complete Implementation Reference

> Extracted from: 15 SSDs (main + alternate), 15 SDs (internal class-level), 1 Class Diagram, 1 Refined Domain Model  
> Use this document as the single source of truth for implementation.

---

## Table of Contents

1. [Module 1 — Employee Management (UC-01 to UC-05)](#module-1--employee-management)
2. [Module 2 — Leave and Attendance Management (UC-06 to UC-10)](#module-2--leave-and-attendance-management)
3. [Module 3 — Performance and Compliance Management (UC-11 to UC-15)](#module-3--performance-and-compliance-management)
4. [Class Diagram — All Classes, Attributes, Methods, Relationships](#class-diagram)
5. [Refined Domain Model — Entities and Associations](#refined-domain-model)

---

## Module 1 — Employee Management

### UC-01 — Register New Employee

| Field | Value |
|-------|-------|
| Use Case ID | UC-01 |
| Use Case Name | Register New Employee |
| Primary Actor | HR Officer |
| Description | HR Officer fills the registration form with personal details, department/designation, and uploads required documents. System performs duplicate check, validates inputs, computes probation period, and generates a unique employeeID on success. |

---

#### UC-01 SSD — Main Success Scenario

**Actors:** `:HR Officer` → `:System`

| Step | Direction | Message | Parameters | Response |
|------|-----------|---------|------------|----------|
| 1 | → | `navigateToRegisterModule()` | — | `displayRegistrationForm()` |
| 2 | → | `enterPersonalDetails()` | name, nationalID, dob, gender, contact, address | `duplicateCheckResult()` |
| 3 | → | `selectDepartmentDesignation()` | dept, designation, empType, joiningDate | `validationResult()` + `probationPeriod()` |
| 4 | → | `uploadDocuments()` | contractFile, idScan | `uploadConfirmation()` |
| 5 | → | `submitRegistration()` | all form data | `employeeID` + `successConfirmation()` |

---

#### UC-01 SSD — Alternate: Duplicate National ID Detected

**Actors:** `:HR Officer` → `:System`

| Step | Direction | Message | Parameters | Response |
|------|-----------|---------|------------|----------|
| 1 | → | `navigateToRegisterModule()` | — | `displayRegistrationForm()` |
| 2 | → | `enterPersonalDetails()` | name, duplicateNationalID, … | — |
| — | ALT | **[Duplicate National ID Detected]** | | |
| 3 | ← | `displayDuplicateError()` | nationalID conflict | — |
| 4 | → | `correctNationalID()` | newNationalID | `validationPassed()` |
| 5 | → | `submitRegistration()` | — | `employeeID` + `successConfirmation()` |

---

#### UC-01 SD — Success Scenario (Internal Class Level)

**Participants:** `:HR Officer` | `:RegisterController` (Controller) | `:EmployeeRepository` (Pure Fabrication) | `:Employee` (Information Expert) | `:NotificationService` (Pure Fabrication)

| Step | From | To | Method | Return |
|------|------|----|--------|--------|
| 1 | HR Officer | RegisterController | `registerNewEmployee(employeeData)` | — |
| 2 | RegisterController | EmployeeRepository | `validateDuplicate(nationalID, email)` | — |
| 3 | EmployeeRepository | Employee | `checkDuplicate(nationalID)` | `false` (no duplicate) |
| 4 | EmployeeRepository | RegisterController | — | `noDuplicate` |
| 5 | RegisterController | EmployeeRepository | `create(employeeData)` | — |
| 6 | EmployeeRepository | Employee | `save(employee)` | `employeeID` |
| 7 | EmployeeRepository | RegisterController | — | `employeeID` |
| 8 | RegisterController | NotificationService | `sendWelcomeEmail(employee)` | `emailQueued` |
| 9 | RegisterController | HR Officer | — | `successConfirmation(employeeID)` |

**GRASP Patterns:**
- `RegisterController` → Controller
- `EmployeeRepository` → Pure Fabrication (handles persistence)
- `Employee` → Information Expert (owns duplicate-check logic)
- `NotificationService` → Pure Fabrication (handles email dispatch)

---

### UC-02 — Update Employee Record

| Field | Value |
|-------|-------|
| Use Case ID | UC-02 |
| Use Case Name | Update Employee Record |
| Primary Actor | HR Officer |
| Description | HR Officer locates an employee record and modifies one or more fields. System validates changes, writes a timestamped audit log capturing old and new values, and saves the updated record. |

---

#### UC-02 SSD — Main Success Scenario

**Actors:** `:HR Officer` → `:System`

| Step | Direction | Message | Parameters | Response |
|------|-----------|---------|------------|----------|
| 1 | → | `searchEmployee()` | employeeID | `matchingRecords()` |
| 2 | → | `selectEmployee()` + `clickEditRecord()` | employeeID | `editableProfileForm(currentData)` |
| 3 | → | `modifyFields()` | updatedValues | `realTimeValidation(fieldValue)` |
| 4 | → | `saveChanges()` | reason, updatedValues | `successConfirmation()` + `auditLogWritten()` |

---

#### UC-02 SSD — Alternate: Concurrent Edit Conflict

**Actors:** `:HR Officer` → `:System`

| Step | Direction | Message | Parameters | Response |
|------|-----------|---------|------------|----------|
| 1 | → | `searchEmployee()` | employeeID | `editableProfileForm()` |
| 2 | → | `saveChanges()` | reason, updatedValues | — |
| — | ALT | **[Concurrent Edit Conflict Detected]** | | |
| 3 | ← | `conflictWarning()` | oldValues, newValues | — |
| 4 | → | `confirmRetainValues()` | selectedValues | `saveConfirmed()` |
| 5 | ← | `successConfirmation()` | — | — |

---

#### UC-02 SD — Success Scenario (Internal Class Level)

**Participants:** `:HR Officer` | `:UpdateController` (Controller) | `:Employee` (Information Expert) | `:AuditLogService` (Pure Fabrication) | `:PayrollNotifier` (Pure Fabrication)

| Step | From | To | Method | Return |
|------|------|----|--------|--------|
| 1 | HR Officer | UpdateController | `updateEmployeeRecord(employeeID, updatedValues, reason)` | — |
| 2 | UpdateController | Employee | `getEmployee(employeeID)` | `employeeData` |
| 3 | UpdateController | Employee | `validateAndUpdate(updatedValues)` | `updated` |
| 4 | UpdateController | AuditLogService | `writeAuditLog(employeeID, fieldName, oldVal, newVal, updateID)` | `logged` |
| 5 | UpdateController | PayrollNotifier | `notifyPayroll(employeeID, designation, salary)` | `notified` |
| 6 | UpdateController | HR Officer | — | `successConfirmation()` |

**GRASP Patterns:**
- `UpdateController` → Controller
- `Employee` → Information Expert (validates own data)
- `AuditLogService` → Pure Fabrication (audit persistence)
- `PayrollNotifier` → Pure Fabrication (cross-system notification)

---

### UC-03 — Assign Employee to Department

| Field | Value |
|-------|-------|
| Use Case ID | UC-03 |
| Use Case Name | Assign Employee to Department |
| Primary Actor | HR Officer / Admin |
| Description | Admin or HR Officer assigns or transfers an employee to a department with an effective date. System validates departmental capacity, updates the hierarchy, records history, and notifies managers. |

---

#### UC-03 SSD — Main Success Scenario

**Actors:** `:Admin` → `:System`

| Step | Direction | Message | Parameters | Response |
|------|-----------|---------|------------|----------|
| 1 | → | `navigateToOrgStructure()` | departmentID | `departmentDetails(headcount, manager, slots)` |
| 2 | → | `searchAndSelectEmployee()` | employeeID | `employeeCurrentDept()` + `summary()` |
| 3 | → | `setEffectiveDate()` | date, remark | `validationResult(capacityOK)` |
| 4 | → | `confirmAssignment()` | — | `assignmentConfirmed()` + `notificationsSent()` |

---

#### UC-03 SSD — Alternate: Department At Max Capacity

**Actors:** `:Admin` → `:System`

| Step | Direction | Message | Parameters | Response |
|------|-----------|---------|------------|----------|
| 1 | → | `navigateToOrgStructure()` | departmentID | `departmentDetails()` |
| 2 | → | `setEffectiveDate()` | date | — |
| — | ALT | **[Department Max Headcount Reached]** | | |
| 3 | ← | `capacityWarning()` | deptID, maxCount | — |
| 4 | → | `provideJustification()` | overrideReason | `overrideLogged()` |
| 5 | → | `confirmAssignment()` | — | `assignmentConfirmed()` |

---

#### UC-03 SD — Success Scenario (Internal Class Level)

**Participants:** `:Admin` | `:AssignmentController` (Controller) | `:Department` (Information Expert) | `:Employee` (Information Expert) | `:NotificationService` (Pure Fabrication)

| Step | From | To | Method | Return |
|------|------|----|--------|--------|
| 1 | Admin | AssignmentController | `assignEmployeeToDepartment(employeeID, deptID, effectiveDate, remark)` | — |
| 2 | AssignmentController | Department | `validateCapacity(deptID)` | `capacityOK` |
| 3 | AssignmentController | Department | `updateDepartment(employeeID, deptID)` | `updated` |
| 4 | AssignmentController | Employee | `updateReportingLine(employeeID, deptID)` | — |
| 5 | AssignmentController | Employee | `recordOrgHistory(employeeID, oldDept, newDept, effectiveDate)` | `historyRecorded` |
| 6 | AssignmentController | NotificationService | `notifyManagers(prevMgrID, newMgrID)` | `notified` |
| 7 | AssignmentController | Admin | — | `assignmentConfirmed()` |

**GRASP Patterns:**
- `AssignmentController` → Controller
- `Department` → Information Expert (owns capacity logic)
- `Employee` → Information Expert (owns reporting-line data)
- `NotificationService` → Pure Fabrication

---

### UC-04 — Initiate Employee Offboarding

| Field | Value |
|-------|-------|
| Use Case ID | UC-04 |
| Use Case Name | Initiate Employee Offboarding |
| Primary Actor | HR Officer |
| Description | HR Officer starts the offboarding workflow by specifying separation type, effective date, and exit reason. System creates a checklist, schedules access revocation, triggers final settlement, and updates employee status. |

---

#### UC-04 SSD — Main Success Scenario

**Actors:** `:HR Officer` → `:System`

| Step | Direction | Message | Parameters | Response |
|------|-----------|---------|------------|----------|
| 1 | → | `navigateToEmployeeProfile()` + `selectInitiateOffboarding()` | — | `offboardingForm(separationType options)` |
| 2 | → | `selectSeparationType()` | type, lastWorkingDate | `noticePeriodValidation()` |
| 3 | → | `assignOffboardingTasks()` | IT, Finance, Employee | `checklistGenerated()` + `notificationsSent()` |
| 4 | → | `submitOffboardingRequest()` | all data | `statusUpdated(Offboarding-PendingClearance)` + `payrollTriggered()` |

---

#### UC-04 SSD — Alternate: Termination For Cause — Letter Required

**Actors:** `:HR Officer` → `:System`

| Step | Direction | Message | Parameters | Response |
|------|-----------|---------|------------|----------|
| 1 | → | `selectInitiateOffboarding()` | — | `offboardingForm()` |
| 2 | → | `selectSeparationType()` | 'Termination For Cause', date | — |
| — | ALT | **[Termination For Cause — Letter Required]** | | |
| 3 | ← | `promptTerminationLetterUpload()` | — | — |
| 4 | → | `uploadTerminationLetter()` | file | `letterValidated()` |
| 5 | → | `submitOffboardingRequest()` | — | `statusUpdated()` + `payrollTriggered()` |

---

#### UC-04 SD — Success Scenario (Internal Class Level)

**Participants:** `:HR Officer` | `:OffboardingController` (Controller) | `:OffboardingWorkflow` (Creator) | `:Employee` (Information Expert) | `:PayrollModule` (External System)

| Step | From | To | Method | Return |
|------|------|----|--------|--------|
| 1 | HR Officer | OffboardingController | `initiateOffboarding(employeeID, separationType, lastWorkingDate)` | — |
| 2 | OffboardingController | OffboardingWorkflow | `validateNoticePeriod(separationType, lastWorkingDate)` | `valid` |
| 3 | OffboardingController | OffboardingWorkflow | `create(employeeID, separationType, lastWorkingDate)` | — |
| 4 | OffboardingWorkflow | OffboardingWorkflow | `generateChecklist(IT, Finance, Employee)` | — |
| 5 | OffboardingWorkflow | Employee | `updateStatus('Offboarding-PendingClearance')` | `statusUpdated` |
| 6 | OffboardingController | PayrollModule | `triggerFinalSettlement(employeeID)` | `settlementQueued` |
| 7 | OffboardingController | HR Officer | — | `offboardingInitiated(workflowID)` |

**GRASP Patterns:**
- `OffboardingController` → Controller
- `OffboardingWorkflow` → Creator (creates and owns checklist)
- `Employee` → Information Expert (owns status)
- `PayrollModule` → External System (GoF: Facade pattern boundary)

---

### UC-05 — View Employee Profile

| Field | Value |
|-------|-------|
| Use Case ID | UC-05 |
| Use Case Name | View Employee Profile |
| Primary Actor | Employee / HR |
| Description | An authenticated user accesses a consolidated employee profile with personal details, employment history, leave balances, attendance summary, and performance ratings. Role-based field masking is enforced server-side. |

---

#### UC-05 SSD — Main Success Scenario

**Actors:** `:User (HR/Employee)` → `:System`

| Step | Direction | Message | Parameters | Response |
|------|-----------|---------|------------|----------|
| 1 | → | `navigateToEmployeeDirectory()` / `globalSearch()` | query | `matchingRecords(scope-filtered)` |
| 2 | → | `selectEmployee()` | employeeID | `renderedProfile(personalInfo, empDetails, leaveBalance, attendance, performanceHistory)` |
| 3 | → | `applyFilter()` | section | `filteredSection(maskedFields)` |

---

#### UC-05 SSD — Alternate: Unauthorized Field Access Attempt

**Actors:** `:User` → `:System`

| Step | Direction | Message | Parameters | Response |
|------|-----------|---------|------------|----------|
| 1 | → | `selectEmployee()` | employeeID | `renderedProfile()` |
| 2 | → | `accessField()` | 'salary' | — |
| — | ALT | **[Unauthorized Field Access — Non-HR User]** | | |
| 3 | ← | `accessRestricted()` | 'salary' field masked | — |
| 4 | ← | `unauthorizedAccessLogCreated()` | — | — |
| 5 | → | `viewPermittedSections()` | — | `maskedProfile(permittedFieldsOnly)` |

---

#### UC-05 SD — Success Scenario (Internal Class Level)

**Participants:** `:User` | `:ProfileController` (Controller) | `:Employee` (Information Expert) | `:AccessControlService` (Pure Fabrication) | `:AuditLogService` (Pure Fabrication)

| Step | From | To | Method | Return |
|------|------|----|--------|--------|
| 1 | User | ProfileController | `viewEmployeeProfile(employeeID)` | — |
| 2 | ProfileController | Employee | `getEmployee(employeeID)` | `employeeData` |
| 3 | ProfileController | AccessControlService | `getPermittedFields(userRole, employeeID)` | `permittedFields[]` |
| 4 | ProfileController | AuditLogService | `logReadAccess(userID, employeeID, sensitiveFields)` | `logged` |
| 5 | ProfileController | User | — | `maskedProfile(permittedFields)` |

**GRASP Patterns:**
- `ProfileController` → Controller
- `Employee` → Information Expert
- `AccessControlService` → Pure Fabrication (RBAC enforcement)
- `AuditLogService` → Pure Fabrication (GoF: Observer-like audit trail)

---

## Module 2 — Leave and Attendance Management

### UC-06 — Submit Leave Request

| Field | Value |
|-------|-------|
| Use Case ID | UC-06 |
| Use Case Name | Submit Leave Request |
| Primary Actor | Employee |
| Description | Employee submits a formal leave request by selecting leave type and dates. System validates balance and policy calendar, records the request as Pending Approval, blocks dates in the attendance calendar, and notifies HR. |

---

#### UC-06 SSD — Main Success Scenario

**Actors:** `:Employee` → `:System`

| Step | Direction | Message | Parameters | Response |
|------|-----------|---------|------------|----------|
| 1 | → | `navigateToLeaveManagement()` + `clickNewLeaveRequest()` | — | `leaveRequestForm(currentBalances)` |
| 2 | → | `selectLeaveDetails()` | leaveType, startDate, endDate, reason | `workingDays` + `balanceCheck` + `blackoutCheck()` |
| 3 | → | `submitRequest()` | all data | `requestRecorded(PendingApproval)` + `datesBlocked()` + `HRNotified()` |

---

#### UC-06 SSD — Alternate: Insufficient Leave Balance

**Actors:** `:Employee` → `:System`

| Step | Direction | Message | Parameters | Response |
|------|-----------|---------|------------|----------|
| 1 | → | `navigateToLeaveManagement()` | — | `leaveRequestForm()` |
| 2 | → | `selectLeaveDetails()` | leaveType, startDate, endDate | — |
| — | ALT | **[Insufficient Leave Balance]** | | |
| 3 | ← | `insufficientBalanceError()` | remainingDays | — |
| 4 | ← | `submissionBlocked()` | policy explanation | — |
| 5 | → | `adjustDates()` | newStartDate, newEndDate | `validationPassed()` |

---

#### UC-06 SD — Success Scenario (Internal Class Level)

**Participants:** `:Employee` | `:LeaveController` (Controller) | `:LeaveBalance` (Information Expert) | `:LeaveRequest` (Creator) | `:NotificationService` (Pure Fabrication)

| Step | From | To | Method | Return |
|------|------|----|--------|--------|
| 1 | Employee | LeaveController | `submitLeaveRequest(employeeID, leaveType, startDate, endDate, reason)` | — |
| 2 | LeaveController | LeaveBalance | `checkBalance(employeeID, leaveType, days)` | `balanceSufficient` |
| 3 | LeaveController | LeaveBalance | `checkBlackoutPeriod(startDate, endDate)` | `noConflict` |
| 4 | LeaveController | LeaveRequest | `create(employeeID, leaveType, dates, reason, 'PendingApproval')` | — |
| 5 | LeaveController | LeaveRequest | `blockDatesInCalendar(employeeID, dates)` | `leaveRequestID` |
| 6 | LeaveController | NotificationService | `notifyHR(hrOfficerID, leaveRequestID)` | `notified` |
| 7 | LeaveController | Employee | — | `confirmation(leaveRequestID, 'PendingApproval')` |

**GRASP Patterns:**
- `LeaveController` → Controller
- `LeaveBalance` → Information Expert (owns balance data + blackout logic)
- `LeaveRequest` → Creator (creates leave records)
- `NotificationService` → Pure Fabrication

---

### UC-07 — Approve or Reject Leave Request

| Field | Value |
|-------|-------|
| Use Case ID | UC-07 |
| Use Case Name | Approve or Reject Leave Request |
| Primary Actor | HR Officer |
| Description | HR Officer reviews a pending leave request and approves or rejects it with a mandatory comment. On approval, system deducts leave balance, updates attendance calendar, and notifies employee. |

---

#### UC-07 SSD — Main Success Scenario

**Actors:** `:HR Officer` → `:System`

| Step | Direction | Message | Parameters | Response |
|------|-----------|---------|------------|----------|
| 1 | → | `navigateToPendingLeaveQueue()` | — | `pendingRequestsList(sorted by date)` |
| 2 | → | `selectRequest()` | leaveRequestID | `requestDetails()` + `teamAttendanceCalendar()` |
| 3 | → | `makeDecision()` | decision, comment | `commentValidated(min 10 chars)` |
| 4 | → | `confirmDecision()` | — | `statusUpdated(Approved)` + `balanceDeducted()` + `calendarConfirmed()` + `employeeNotified()` |

---

#### UC-07 SSD — Alternate: Coverage Below Threshold

**Actors:** `:HR Officer` → `:System`

| Step | Direction | Message | Parameters | Response |
|------|-----------|---------|------------|----------|
| 1 | → | `selectRequest()` | leaveRequestID | `requestDetails()` |
| 2 | → | `makeDecision()` | 'Approve', comment | — |
| — | ALT | **[Coverage Below Minimum Threshold]** | | |
| 3 | ← | `coverageWarning()` | threshold, currentCoverage | — |
| 4 | → | `acknowledgeWarning()` / `override()` | — | `warningAcknowledged()` |
| 5 | → | `confirmDecision()` | — | `Approved` + `balanceDeducted()` + `employeeNotified()` |

---

#### UC-07 SD — Success Scenario (Internal Class Level)

**Participants:** `:HR Officer` | `:LeaveApprovalController` (Controller) | `:LeaveRequest` (Information Expert) | `:LeaveBalance` (Information Expert) | `:NotificationService` (Pure Fabrication)

| Step | From | To | Method | Return |
|------|------|----|--------|--------|
| 1 | HR Officer | LeaveApprovalController | `processLeaveDecision(leaveRequestID, decision, comment)` | — |
| 2 | LeaveApprovalController | LeaveRequest | `getLeaveRequest(leaveRequestID)` | `leaveRequestData` |
| 3 | LeaveApprovalController | LeaveRequest | `validateComment(comment)` | `commentValid` |
| 4 | LeaveApprovalController | LeaveRequest | `updateStatus(leaveRequestID, 'Approved')` | — |
| 5 | LeaveApprovalController | LeaveBalance | `deductBalance(employeeID, leaveType, days)` | `balanceDeducted` |
| 6 | LeaveApprovalController | LeaveRequest | `confirmCalendar(employeeID, dates)` | `calendarConfirmed` |
| 7 | LeaveApprovalController | NotificationService | `notifyEmployee(employeeID, 'Approved', comment)` | `notified` |
| 8 | LeaveApprovalController | HR Officer | — | `decisionConfirmed(leaveRequestID, status)` |

**GRASP Patterns:**
- `LeaveApprovalController` → Controller
- `LeaveRequest` → Information Expert (owns request state + comment validation)
- `LeaveBalance` → Information Expert (owns balance deduction)
- `NotificationService` → Pure Fabrication

---

### UC-08 — Record Daily Attendance

| Field | Value |
|-------|-------|
| Use Case ID | UC-08 |
| Use Case Name | Record Daily Attendance |
| Primary Actor | Employee |
| Description | System records check-in and check-out timestamps via biometric device, web portal, or mobile app. Computes work hours, flags late arrivals and early departures, and stores attendance linked to payroll. |

---

#### UC-08 SSD — Main Success Scenario

**Actors:** `:Employee` → `:System`

| Step | Direction | Message | Parameters | Response |
|------|-----------|---------|------------|----------|
| 1 | → | `checkIn()` | employeeID, timestamp, method | `attendanceRecordCreated(attendanceID)` |
| 2 | → | `checkOut()` | employeeID, timestamp | `computeWorkHours()` + `flagExceptions()` |
| 3 | → | `notifyPayroll()` | attendanceID, status | `attendanceComplete(totalHours, exceptions)` |

---

#### UC-08 SSD — Alternate: Missing Check-out

**Actors:** `:Employee` → `:System`

| Step | Direction | Message | Parameters | Response |
|------|-----------|---------|------------|----------|
| 1 | → | `checkIn()` | employeeID, timestamp | `attendanceRecordCreated()` |
| — | ALT | **[Missing Check-out After Threshold]** | | |
| 2 | ← | `flagMissingCheckOut()` | attendanceID | — |
| 3 | ← | `sendAlert()` | hrOfficerID, employeeID, 'Missing checkout' | — |
| 4 | ← | `markRecordIncomplete()` | attendanceID | — |
| 5 | → | `hrReviewAndCorrect()` | attendanceID, estimatedCheckout | `recordCorrected(totalHours)` |

---

#### UC-08 SD — Success Scenario (Internal Class Level)

**Participants:** `:Employee` | `:AttendanceController` (Controller) | `:AttendanceRecord` (Information Expert) | `:PayrollNotifier` (Pure Fabrication)

| Step | From | To | Method | Return |
|------|------|----|--------|--------|
| 1 | Employee | AttendanceController | `checkIn(employeeID, timestamp, method)` | — |
| 2 | AttendanceController | AttendanceRecord | `create(employeeID, checkInTime)` | `attendanceID` |
| 3 | Employee | AttendanceController | `checkOut(employeeID, timestamp)` | — |
| 4 | AttendanceController | AttendanceRecord | `updateCheckOut(checkOutTime)` | — |
| 5 | AttendanceController | AttendanceRecord | `computeWorkHours()` | `totalHours` + `exceptions{}` |
| 6 | AttendanceController | PayrollNotifier | `notifyPayroll(attendanceID, totalHours)` | `recorded` |
| 7 | AttendanceController | Employee | — | `attendanceConfirmed(totalHours)` |

**GRASP Patterns:**
- `AttendanceController` → Controller
- `AttendanceRecord` → Information Expert (computes own hours and exceptions)
- `PayrollNotifier` → Pure Fabrication

---

### UC-09 — Request Attendance Correction

| Field | Value |
|-------|-------|
| Use Case ID | UC-09 |
| Use Case Name | Request Attendance Correction |
| Primary Actor | Employee |
| Description | Employee submits a correction request specifying affected date, incorrect value, corrected value, and reason. HR reviews and approved corrections update the record with an audit annotation. |

---

#### UC-09 SSD — Main Success Scenario

**Actors:** `:Employee` → `:System`

| Step | Direction | Message | Parameters | Response |
|------|-----------|---------|------------|----------|
| 1 | → | `navigateToAttendanceHistory()` | — | `attendanceLogDisplayed(history)` |
| 2 | → | `selectErroneousRecord()` | attendanceID | `recordDetailsDisplayed(details)` |
| 3 | → | `submitCorrectionRequest()` | attendanceID, field, originalValue, correctedValue, reason | `validationResult(inCorrectionWindow)` |
| 4 | → | `requestRecorded()` | correctionID, 'PendingHRReview' | `confirmationWithID(correctionID)` |

---

#### UC-09 SSD — Alternate: Correction Window Expired

**Actors:** `:Employee` → `:System`

| Step | Direction | Message | Parameters | Response |
|------|-----------|---------|------------|----------|
| 1 | → | `navigateToAttendanceHistory()` | — | `attendanceLogDisplayed()` |
| 2 | → | `submitCorrectionRequest()` | attendanceID, field, correctedValue, reason | — |
| — | ALT | **[Correction Window Expired (> 7 Days)]** | | |
| 3 | ← | `displayError()` | 'Correction window expired' | — |
| 4 | ← | `suggestContactHR()` | — | — |
| 5 | → | `submissionBlocked()` | — | — |

---

#### UC-09 SD — Success Scenario (Internal Class Level)

**Participants:** `:Employee` | `:CorrectionController` (Controller) | `:AttendanceRecord` (Information Expert) | `:CorrectionRequest` (Creator) | `:AuditLogService` (Pure Fabrication)

| Step | From | To | Method | Return |
|------|------|----|--------|--------|
| 1 | Employee | CorrectionController | `submitCorrectionRequest(attendanceID, field, originalVal, correctedVal, reason)` | — |
| 2 | CorrectionController | AttendanceRecord | `validateCorrectionWindow(attendanceID)` | `withinWindow` |
| 3 | CorrectionController | CorrectionRequest | `create(attendanceID, field, originalVal, correctedVal, reason)` | — |
| 4 | CorrectionRequest | CorrectionRequest | `validateLogicalConsistency()` | `consistent` |
| 5 | CorrectionController | AuditLogService | `recordCorrectionAttempt(employeeID, attendanceID, action, timestamp)` | `logged` |
| 6 | CorrectionController | Employee | — | `confirmationWithID(correctionID, 'PendingHRReview')` |

**GRASP Patterns:**
- `CorrectionController` → Controller
- `AttendanceRecord` → Information Expert (owns correction window boundary)
- `CorrectionRequest` → Creator (creates and validates correction)
- `AuditLogService` → Pure Fabrication

---

### UC-10 — View Leave Balance and Attendance Summary

| Field | Value |
|-------|-------|
| Use Case ID | UC-10 |
| Use Case Name | View Leave Balance and Attendance Summary |
| Primary Actor | Employee / HR |
| Description | Authorized users access a real-time dashboard showing remaining leave balances by type and monthly attendance KPIs. System aggregates data from leave and attendance modules and supports export to PDF or Excel. |

---

#### UC-10 SSD — Main Success Scenario

**Actors:** `:User (Employee/HR)` → `:System`

| Step | Direction | Message | Parameters | Response |
|------|-----------|---------|------------|----------|
| 1 | → | `navigateToLeaveAndAttendanceDashboard()` | — | `dashboardDisplayed(balances, summary)` |
| 2 | → | `applyDateFilter()` | startDate, endDate | `aggregatedData(filtered)` |
| 3 | → | `selectExportFormat()` | formatType | `reportGenerated(format)` |

---

#### UC-10 SSD — Alternate: No Data Available

**Actors:** `:User` → `:System`

| Step | Direction | Message | Parameters | Response |
|------|-----------|---------|------------|----------|
| 1 | → | `navigateToLeaveAndAttendanceDashboard()` | — | — |
| 2 | → | `applyDateFilter()` | futureStartDate, futureEndDate | — |
| — | ALT | **[No Data Found for Selected Period]** | | |
| 3 | ← | `displayNoDataMessage()` | — | — |
| 4 | ← | `suggestAdjustFilter()` | — | — |
| 5 | → | `adjustDateRange()` | newStartDate, newEndDate | `dataDisplayed(balances, summary)` |

---

#### UC-10 SD — Success Scenario (Internal Class Level)

**Participants:** `:User` | `:DashboardController` (Controller) | `:LeaveBalance` (Information Expert) | `:AttendanceRecord` (Information Expert) | `:ReportGenerator` (Pure Fabrication)

| Step | From | To | Method | Return |
|------|------|----|--------|--------|
| 1 | User | DashboardController | `viewLeaveAndAttendanceDashboard(userID)` | — |
| 2 | DashboardController | LeaveBalance | `getLeaveBalance(userID, year)` | `balanceData[]` |
| 3 | DashboardController | AttendanceRecord | `getAttendanceSummary(userID, dateRange)` | `attendanceData[]` |
| 4 | DashboardController | ReportGenerator | `generateReport(balanceData, attendanceData, format)` | `report(formatted)` |
| 5 | DashboardController | User | — | `dashboardDisplayed(report)` |

**GRASP Patterns:**
- `DashboardController` → Controller
- `LeaveBalance` → Information Expert
- `AttendanceRecord` → Information Expert
- `ReportGenerator` → Pure Fabrication (GoF: Strategy for format selection)

---

## Module 3 — Performance and Compliance Management

### UC-11 — Initiate Performance Evaluation Cycle

| Field | Value |
|-------|-------|
| Use Case ID | UC-11 |
| Use Case Name | Initiate Performance Evaluation Cycle |
| Primary Actor | Admin |
| Description | Admin configures and launches a performance evaluation cycle by defining period, scope, evaluators, and deadlines. System distributes tasks, opens the evaluation module, and schedules automated reminder notifications. |

---

#### UC-11 SSD — Main Success Scenario

**Actors:** `:Admin` → `:System`

| Step | Direction | Message | Parameters | Response |
|------|-----------|---------|------------|----------|
| 1 | → | `navigateToPerformanceManagement()` | — | `configurationForm()` |
| 2 | → | `configureCycle()` | cycleName, period, departments, evaluationType | `validatePeriod()` |
| 3 | → | `assignEvaluators()` | departmentID, evaluatorID[] | `validateAssignments()` |
| 4 | → | `activateCycle()` | — | `cycleActivated()` + `tasksCreated()` + `remindersScheduled()` |

---

#### UC-11 SSD — Alternate: Overlapping Cycle Detected

**Actors:** `:Admin` → `:System`

| Step | Direction | Message | Parameters | Response |
|------|-----------|---------|------------|----------|
| 1 | → | `configureCycle()` | cycleName, period, departments | — |
| — | ALT | **[Overlapping Cycle Period Detected]** | | |
| 2 | ← | `displayConflictWarning()` | conflictingCycleID | — |
| 3 | ← | `offerModifyPeriodOrCancel()` | — | — |
| 4 | → | `adjustCyclePeriod()` | newStartDate, newEndDate | `cycleConfirmed()` |

---

#### UC-11 SD — Success Scenario (Internal Class Level)

**Participants:** `:Admin` | `:EvaluationController` (Controller) | `:EvaluationCycle` (Creator) | `:NotificationService` (Pure Fabrication) | `:TaskScheduler` (Pure Fabrication)

| Step | From | To | Method | Return |
|------|------|----|--------|--------|
| 1 | Admin | EvaluationController | `initiateEvaluationCycle(cycleName, period, scope, evaluationType)` | — |
| 2 | EvaluationController | EvaluationCycle | `validatePeriod(startDate, endDate)` | `valid` |
| 3 | EvaluationController | EvaluationCycle | `create(cycleName, period, scope, evaluationType)` | `cycleID` |
| 4 | EvaluationController | NotificationService | `distributeEvaluationTasks(cycleID, evaluators)` | `tasksDistributed()` |
| 5 | EvaluationController | TaskScheduler | `scheduleReminders(cycleID, deadline)` | `remindersScheduled()` |
| 6 | EvaluationController | Admin | — | `cycleActivated(cycleID)` |

**GRASP Patterns:**
- `EvaluationController` → Controller
- `EvaluationCycle` → Creator (creates the cycle entity)
- `NotificationService` → Pure Fabrication
- `TaskScheduler` → Pure Fabrication (GoF: Command pattern for deferred tasks)

---

### UC-12 — Submit Employee Performance Evaluation

| Field | Value |
|-------|-------|
| Use Case ID | UC-12 |
| Use Case Name | Submit Employee Performance Evaluation |
| Primary Actor | HR / Evaluator |
| Description | Assigned evaluator completes a structured performance evaluation form for an employee during an active evaluation cycle by scoring competencies and KPIs. System validates completeness, calculates aggregate score, and records in history. |

---

#### UC-12 SSD — Main Success Scenario

**Actors:** `:Evaluator (HR)` → `:System`

| Step | Direction | Message | Parameters | Response |
|------|-----------|---------|------------|----------|
| 1 | → | `navigateToPendingEvaluations()` | — | `displayEvaluationList()` |
| 2 | → | `selectEmployee()` | employeeID | `displayEvaluationForm(template)` |
| 3 | → | `completeEvaluation()` | scores, feedback | `validateCompleteness()` |
| 4 | → | `submitEvaluation()` | all data | `evaluationStored()` + `scoreCalculated()` + `employeeNotified()` |

---

#### UC-12 SSD — Alternate: Incomplete Evaluation Submission

**Actors:** `:Evaluator` → `:System`

| Step | Direction | Message | Parameters | Response |
|------|-----------|---------|------------|----------|
| 1 | → | `selectEmployee()` | — | `displayEvaluationForm()` |
| 2 | → | `partiallyCompleteEvaluation()` | incompleteScores | — |
| — | ALT | **[Mandatory Sections Incomplete]** | | |
| 3 | ← | `highlightIncompleteFields()` | — | — |
| 4 | ← | `displayError()` | 'Complete all mandatory sections' | — |
| 5 | → | `completeAndResubmit()` | allScores, feedback | `evaluationAccepted()` |

---

#### UC-12 SD — Success Scenario (Internal Class Level)

**Participants:** `:Evaluator (HR)` | `:EvaluationController` (Controller) | `:PerformanceEvaluation` (Creator/Expert) | `:NotificationService` (Pure Fabrication) | `:AuditLogService` (Pure Fabrication)

| Step | From | To | Method | Return |
|------|------|----|--------|--------|
| 1 | Evaluator | EvaluationController | `submitPerformanceEvaluation(employeeID, scores, feedback, cycleID)` | — |
| 2 | EvaluationController | PerformanceEvaluation | `validateForm(scores, feedback)` | `formValid` |
| 3 | EvaluationController | PerformanceEvaluation | `create(employeeID, evaluatorID, scores, feedback; aggregateScore)` | `evaluationID` + `aggregateScore` |
| 4 | EvaluationController | NotificationService | `notifyEmployee(employeeID, evaluationID)` | `notified` |
| 5 | EvaluationController | AuditLogService | `logEvaluationSubmission(evaluatorID, employeeID, cycleID)` | `logged` |
| 6 | EvaluationController | Evaluator | — | `confirmationWithScore(evaluationID, aggregateScore)` |

**GRASP Patterns:**
- `EvaluationController` → Controller
- `PerformanceEvaluation` → Creator + Information Expert (creates and scores evaluation)
- `NotificationService` → Pure Fabrication
- `AuditLogService` → Pure Fabrication

---

### UC-13 — Monitor Employee Probation Status

| Field | Value |
|-------|-------|
| Use Case ID | UC-13 |
| Use Case Name | Monitor Employee Probation Status |
| Primary Actor | HR Officer |
| Description | HR Officer monitors employees on probation, tracking evaluation milestones and decision dates. System sends automated deadline alerts and allows HR to record confirmation, extension, or termination decisions. |

---

#### UC-13 SSD — Main Success Scenario

**Actors:** `:HR Officer` → `:System`

| Step | Direction | Message | Parameters | Response |
|------|-----------|---------|------------|----------|
| 1 | → | `navigateToProbationDashboard()` | — | `displayProbationCases()` |
| 2 | → | `selectEmployee()` | employeeID | `displayProbationRecord()` |
| 3 | → | `selectDecision()` | decision, notes | `validateDecision()` |
| 4 | → | `submitDecision()` | all data | `decisionRecorded()` + `statusUpdated()` + `employeeNotified()` |

---

#### UC-13 SSD — Alternate: Probation Extension Decision

**Actors:** `:HR Officer` → `:System`

| Step | Direction | Message | Parameters | Response |
|------|-----------|---------|------------|----------|
| 1 | → | `navigateToProbationDashboard()` | — | — |
| 2 | → | `selectEmployee()` | — | — |
| 3 | → | `selectExtension()` | newEndDate | — |
| — | ALT | **[Total Duration Exceeds Policy Maximum]** | | |
| 4 | ← | `displayPolicyBreach()` | maxDurationExceeded | — |
| 5 | ← | `requireAdminOverride()` | — | — |
| 6 | → | `submitDecisionWithNotes()` | — | `extensionRecorded()` + `employeeNotified()` |

---

#### UC-13 SD — Success Scenario (Internal Class Level)

**Participants:** `:HR Officer` | `:ProbationController` (Controller) | `:ProbationRecord` (Information Expert) | `:Employee` (Information Expert) | `:NotificationService` (Pure Fabrication)

| Step | From | To | Method | Return |
|------|------|----|--------|--------|
| 1 | HR Officer | ProbationController | `monitorProbationStatus(employeeID)` | — |
| 2 | ProbationController | ProbationRecord | `getProbationRecord(employeeID)` | `probationData` |
| 3 | ProbationController | ProbationRecord | `recordDecision(decision, endDate, notes)` | — |
| 4 | ProbationRecord | Employee | `updateStatus(newStatus)` | `statusUpdated` |
| 5 | ProbationController | NotificationService | `notifyProbationDecision(employeeID, decision, notes)` | `notified` |
| 6 | ProbationController | HR Officer | — | `confirmationWithDecision(decision, status)` |

**GRASP Patterns:**
- `ProbationController` → Controller
- `ProbationRecord` → Information Expert (owns decision logic)
- `Employee` → Information Expert (owns employment status)
- `NotificationService` → Pure Fabrication

---

### UC-14 — Generate Compliance Report

| Field | Value |
|-------|-------|
| Use Case ID | UC-14 |
| Use Case Name | Generate Compliance Report |
| Primary Actor | Admin |
| Description | Admin generates a formal compliance report for a specified period and compliance domain. System aggregates policy adherence data, applies regulatory filters, formats time-stamped output, and archives it for a minimum of five years. |

---

#### UC-14 SSD — Main Success Scenario

**Actors:** `:Admin` → `:System`

| Step | Direction | Message | Parameters | Response |
|------|-----------|---------|------------|----------|
| 1 | → | `navigateToReports()` | — | `displayReportOptions()` |
| 2 | → | `selectComplianceReport()` | domain, period, format | `validateReportParameters()` |
| 3 | → | `initiateGeneration()` | — | `reportGenerated()` + `archived()` + `downloadReady()` |

---

#### UC-14 SSD — Alternate: Report Generation Exceeds Time Limit

**Actors:** `:Admin` → `:System`

| Step | Direction | Message | Parameters | Response |
|------|-----------|---------|------------|----------|
| 1 | → | `selectComplianceReport()` | domain, period | `estimateGenerationTime()` |
| — | ALT | **[Generation Time > 30 Seconds]** | | |
| 2 | ← | `switchToAsyncProcessing()` | — | — |
| 3 | ← | `queueBackgroundJob()` | reportID | — |
| 4 | ← | `reportReadyNotification()` | reportID, downloadLink | — |

---

#### UC-14 SD — Success Scenario (Internal Class Level)

**Participants:** `:Admin` | `:ReportController` (Controller) | `:ComplianceDataAgg` (Information Expert) | `:ReportGenerator` (Pure Fabrication) | `:SecureStorage` (Pure Fabrication)

| Step | From | To | Method | Return |
|------|------|----|--------|--------|
| 1 | Admin | ReportController | `generateComplianceReport(domain, period, format)` | — |
| 2 | ReportController | ComplianceDataAgg | `aggregateComplianceData(domain, period)` | `complianceData[]` |
| 3 | ReportController | ReportGenerator | `generateAndFormat(complianceData, format)` | `reportFile(timestamp, generatorID)` |
| 4 | ReportController | SecureStorage | `archiveReport(reportFile, retentionPolicy)` | `archived(reportID, storageLocation)` |
| 5 | ReportController | Admin | — | `reportReady(reportID, downloadLink)` |

**GRASP Patterns:**
- `ReportController` → Controller
- `ComplianceDataAgg` → Information Expert (aggregates compliance data)
- `ReportGenerator` → Pure Fabrication (GoF: Template Method for report format)
- `SecureStorage` → Pure Fabrication (retention policy enforcement)

---

### UC-15 — Generate HR Analytics Report

| Field | Value |
|-------|-------|
| Use Case ID | UC-15 |
| Use Case Name | Generate HR Analytics Report |
| Primary Actor | Admin |
| Description | Admin generates an analytics report covering workforce KPIs such as headcount, attrition, leave utilization, and performance benchmarks. System queries all HR modules, applies statistical aggregation, and renders interactive charts and data tables. |

---

#### UC-15 SSD — Main Success Scenario

**Actors:** `:Admin` → `:System`

| Step | Direction | Message | Parameters | Response |
|------|-----------|---------|------------|----------|
| 1 | → | `navigateToAnalytics()` | — | `displayReportBuilder()` |
| 2 | → | `selectMetrics()` | headcount, attrition, leave, attendance, performance | `displayPreview()` |
| 3 | → | `initiateGeneration()` | all metrics | `reportWithChartsAndTables()` |

---

#### UC-15 SSD — Alternate: Insufficient Data for Metrics

**Actors:** `:Admin` → `:System`

| Step | Direction | Message | Parameters | Response |
|------|-----------|---------|------------|----------|
| 1 | → | `selectMetrics()` | metrics | — |
| — | ALT | **[Insufficient Data for Selected Metric]** | | |
| 2 | ← | `disableMetric()` | disabledMetric, reason | — |
| 3 | ← | `suggestAlternatives()` | availableMetrics | — |
| 4 | → | `selectAlternativeMetrics()` | — | `reportGenerated()` |

---

#### UC-15 SD — Success Scenario (Internal Class Level)

**Participants:** `:Admin` | `:AnalyticsController` (Controller) | `:AnalyticsAggregator` (Information Expert) | `:DataVisualization` (Pure Fabrication) | `:ReportArchive` (Pure Fabrication)

| Step | From | To | Method | Return |
|------|------|----|--------|--------|
| 1 | Admin | AnalyticsController | `generateHRAnalyticsReport(metrics, dateRange, scope)` | — |
| 2 | AnalyticsController | AnalyticsAggregator | `validateMetricsAvailability(metrics)` | `availableMetrics` |
| 3 | AnalyticsController | AnalyticsAggregator | `aggregateKPI(headcount, attrition, leave, attendance)` | `kpiData(metrics)` |
| 4 | AnalyticsController | DataVisualization | `generateCharts(kpiData)` | `chartsAndTables(formatted)` |
| 5 | AnalyticsController | ReportArchive | `archiveReport(reportID, creatorID, timestamp)` | `archived(reportID)` |
| 6 | AnalyticsController | Admin | — | `reportReady(reportID, reportData)` |

**GRASP Patterns:**
- `AnalyticsController` → Controller
- `AnalyticsAggregator` → Information Expert (owns KPI aggregation)
- `DataVisualization` → Pure Fabrication (GoF: Strategy for chart rendering)
- `ReportArchive` → Pure Fabrication

---

## Class Diagram

> Extracted from Section 4.1. The class diagram integrates all operations from all Sequence Diagrams and reflects the full domain model.

### Controller Classes

#### `RegisterController`
- **Role:** Controller (GRASP)
- **Methods:**
  - `registerNewEmployee(employeeData): void`
  - `validateDuplicate(nationalID, email): boolean`

#### `UpdateController`
- **Role:** Controller
- **Methods:**
  - `updateEmployeeRecord(employeeID, updatedValues, reason): void`
  - `getEmployee(employeeID): Employee`

#### `AssignmentController`
- **Role:** Controller
- **Methods:**
  - `assignEmployeeToDepartment(employeeID, deptID, effectiveDate, remark): void`

#### `OffboardingController`
- **Role:** Controller
- **Methods:**
  - `initiateOffboarding(employeeID, separationType, lastWorkingDate): void`
  - `triggerFinalSettlement(employeeID): void`

#### `ProfileController`
- **Role:** Controller
- **Methods:**
  - `viewEmployeeProfile(employeeID): void`
  - `getPermittedFields(userRole, employeeID): List<String>`

#### `LeaveController`
- **Role:** Controller
- **Methods:**
  - `submitLeaveRequest(employeeID, leaveType, startDate, endDate, reason): void`

#### `LeaveApprovalController`
- **Role:** Controller
- **Methods:**
  - `processLeaveDecision(leaveRequestID, decision, comment): void`

#### `AttendanceController`
- **Role:** Controller
- **Methods:**
  - `checkIn(employeeID, timestamp, method): String`
  - `checkOut(employeeID, timestamp): void`

#### `CorrectionController`
- **Role:** Controller
- **Methods:**
  - `submitCorrectionRequest(attendanceID, field, originalVal, correctedVal, reason): void`

#### `DashboardController`
- **Role:** Controller
- **Methods:**
  - `viewLeaveAndAttendanceDashboard(userID): void`
  - `getLeaveBalance(userID, year): List<LeaveBalance>`
  - `getAttendanceSummary(userID, dateRange): List<AttendanceRecord>`

#### `EvaluationController`
- **Role:** Controller
- **Methods:**
  - `initiateEvaluationCycle(cycleName, period, scope, evaluationType): void`
  - `submitPerformanceEvaluation(employeeID, scores, feedback, cycleID): void`

#### `ProbationController`
- **Role:** Controller
- **Methods:**
  - `monitorProbationStatus(employeeID): void`
  - `getProbationRecord(employeeID): ProbationRecord`
  - `recordDecision(decision, endDate, notes): void`

#### `ReportController`
- **Role:** Controller
- **Methods:**
  - `generateComplianceReport(domain, period, format): void`

#### `AnalyticsController`
- **Role:** Controller
- **Methods:**
  - `generateHRAnalyticsReport(metrics, dateRange, scope): void`
  - `validateMetricsAvailability(metrics): List<String>`
  - `aggregateKPI(headcount, attrition, leave, attendance): Map`

---

### Domain/Entity Classes

#### `Employee`
- **Role:** Information Expert
- **Attributes:**
  - `+employeeID: String`
  - `+fullName: String`
  - `+phoneNumber: String`
  - `+email: String`
  - `+dateOfBirth: Date`
  - `+nationalID: String`
  - `+gender: String`
  - `+designation: String`
  - `+departmentID: String`
  - `+employmentType: String`
  - `+employmentStatus: String`
  - `+probationEndDate: Date`
- **Methods:**
  - `+validateAndUpdate(updatedValues): boolean`
  - `+checkDuplicate(nationalID): boolean`
  - `+updateStatus(newStatus): void`
  - `+updateReportingLine(employeeID, deptID): void`
  - `+recordOrgHistory(employeeID, oldDept, newDept, effectiveDate): void`
  - `+save(employee): String`

#### `Department`
- **Role:** Information Expert
- **Attributes:**
  - `+departmentID: String`
  - `+departmentName: String`
  - `+managerID: String`
  - `+parentDepartmentID: String`
  - `+description: String`
  - `+maxHeadcount: Integer`
  - `+currentHeadcount: Integer`
- **Methods:**
  - `+validateCapacity(deptID): boolean`
  - `+updateDepartment(employeeID, deptID): void`

#### `Designation`
- **Attributes:**
  - `+designationID: String`
  - `+title: String`
  - `+description: String`
  - `+salaryGrade: String`

#### `HR`
- **Attributes:**
  - `+hrID: String`
  - `+employeeID: String`
  - `+department: String`
- **Methods:**
  - `+reviewLeaveRequest(): void`
  - `+approveLeaveRequest(): void`
  - `+approveCorrectionRequest(): void`

#### `Admin`
- **Attributes:**
  - `+adminID: String`
  - `+employeeID: String`
  - `+permissions: String[]`
- **Methods:**
  - `+generateComplianceReport(): void`
  - `+configureEvaluationCycle(): void`
  - `+generateHRAnalyticsReport(): void`

#### `UserAccount`
- **Attributes:**
  - `+userID: String`
  - `+employeeID: String`
  - `+username: String`
  - `+role: String`
  - `+permissions: String[]`
  - `+accountStatus: String`
  - `+lastLogin: DateTime`

#### `LeaveRequest`
- **Role:** Creator + Information Expert
- **Attributes:**
  - `+leaveType: String`
  - `+employeeID: String`
  - `+startDate: Date`
  - `+endDate: Date`
  - `+workingDays: Integer`
  - `+reason: String`
  - `+status: String`
  - `+approvedBy: String`
  - `+approvedDate: Date`
  - `+comments: String`
- **Methods:**
  - `+create(employeeID, leaveType, dates, reason, status): String`
  - `+blockDatesInCalendar(employeeID, dates): void`
  - `+validateComment(comment): boolean`
  - `+updateStatus(leaveRequestID, status): void`
  - `+getLeaveRequest(leaveRequestID): LeaveRequest`
  - `+confirmCalendar(employeeID, dates): void`

#### `LeaveBalance`
- **Role:** Information Expert
- **Attributes:**
  - `+balanceID: String`
  - `+employeeID: String`
  - `+leaveType: String`
  - `+totalEntitled: Integer`
  - `+used: Integer`
  - `+remaining: Integer`
  - `+year: Integer`
- **Methods:**
  - `+checkBalance(employeeID, leaveType, days): boolean`
  - `+checkBlackoutPeriod(startDate, endDate): boolean`
  - `+deductBalance(employeeID, leaveType, days): void`
  - `+getLeaveBalance(userID, year): List<LeaveBalance>`

#### `AttendanceRecord`
- **Role:** Information Expert
- **Attributes:**
  - `+attendanceID: String`
  - `+employeeID: String`
  - `+attendanceDate: Date`
  - `+checkInTime: DateTime`
  - `+checkOutTime: DateTime`
  - `+totalHours: Float`
  - `+attendanceStatus: String`
  - `+correctionFlag: Boolean`
- **Methods:**
  - `+create(employeeID, checkInTime): String`
  - `+updateCheckOut(checkOutTime): void`
  - `+computeWorkHours(): Float`
  - `+validateCorrectionWindow(attendanceID): boolean`
  - `+getAttendanceSummary(userID, dateRange): List<AttendanceRecord>`

#### `AttendanceCorrectionRequest`
- **Role:** Creator
- **Attributes:**
  - `+correctionID: String`
  - `+attendanceID: String`
  - `+employeeID: String`
  - `+originalValue: String`
  - `+correctedValue: String`
  - `+justification: String`
  - `+status: String`
  - `+reviewDate: Date`
- **Methods:**
  - `+create(attendanceID, field, originalVal, correctedVal, reason): String`
  - `+validateLogicalConsistency(): boolean`
  - `+logCorrectionAttempt(): void`

#### `OffboardingWorkflow`
- **Role:** Creator
- **Attributes:**
  - `+workflowID: String`
  - `+employeeID: String`
  - `+separationType: String`
  - `+hireDate: Date`
  - `+lastWorkingDate: Date`
  - `+exitReason: String`
  - `+status: String`
  - `+checklistItems: String[]`
  - `+finalSettlementStatus: String`
- **Methods:**
  - `+validateNoticePeriod(separationType, lastWorkingDate): boolean`
  - `+create(employeeID, separationType, lastWorkingDate): void`
  - `+generateChecklist(IT, Finance, Employee): void`

#### `ProbationRecord`
- **Role:** Information Expert
- **Attributes:**
  - `+probationID: String`
  - `+employeeID: String`
  - `+startDate: Date`
  - `+endDate: Date`
  - `+extensions: Integer`
  - `+decision: String`
  - `+decisionDate: Date`
  - `+reason: String`
  - `+status: String`
  - `+decisionMadeID: String`
  - `+notes: String`
- **Methods:**
  - `+getProbationRecord(employeeID): ProbationRecord`
  - `+recordDecision(decision, endDate, notes): void`

#### `PerformanceEvaluation`
- **Role:** Creator + Information Expert
- **Attributes:**
  - `+evaluationID: String`
  - `+employeeID: String`
  - `+evaluatorID: String`
  - `+cycleID: String`
  - `+evaluationDate: Date`
  - `+aggregateScore: Float`
  - `+remarks: String`
  - `+status: String`
- **Methods:**
  - `+validateForm(scores, feedback): boolean`
  - `+create(employeeID, evaluatorID, scores, feedback, aggregateScore): String`

#### `EvaluationCycle`
- **Role:** Creator
- **Attributes:**
  - `+cycleID: String`
  - `+cycleName: String`
  - `+startDate: Date`
  - `+endDate: Date`
  - `+evaluationType: String`
  - `+applicableScope: String`
  - `+status: String`
  - `+reminderSchedule: String`
- **Methods:**
  - `+validatePeriod(startDate, endDate): boolean`
  - `+create(cycleName, period, scope, evaluationType): String`

#### `ComplianceReport`
- **Attributes:**
  - `+reportID: String`
  - `+reportType: String`
  - `+generatedAt: DateTime`
  - `+generatedBy: String`
  - `+parameters: String`
  - `+format: String`
  - `+status: String`
  - `+archivePath: String`

#### `HRAnalyticsReport`
- **Attributes:**
  - `+reportID: String`
  - `+creatorID: String`
  - `+reportPeriod: String`
  - `+generatedAt: DateTime`
  - `+summaryMetrics: String`

---

### Pure Fabrication / Service Classes

| Class | Role | Key Methods |
|-------|------|-------------|
| `EmployeeRepository` | Pure Fabrication | `validateDuplicate()`, `create()`, `save()` |
| `NotificationService` | Pure Fabrication | `sendWelcomeEmail()`, `notifyHR()`, `notifyEmployee()`, `notifyManagers()`, `notifyProbationDecision()`, `distributeEvaluationTasks()` |
| `AuditLogService` | Pure Fabrication | `writeAuditLog()`, `logReadAccess()`, `recordCorrectionAttempt()`, `logEvaluationSubmission()` |
| `PayrollNotifier` | Pure Fabrication | `notifyPayroll()` |
| `AccessControlService` | Pure Fabrication | `getPermittedFields(userRole, employeeID)` |
| `ReportGenerator` | Pure Fabrication | `generateReport()`, `generateAndFormat()` |
| `ComplianceDataAgg` | Information Expert | `aggregateComplianceData(domain, period)` |
| `AnalyticsAggregator` | Information Expert | `validateMetricsAvailability()`, `aggregateKPI()` |
| `DataVisualization` | Pure Fabrication | `generateCharts(kpiData)` |
| `ReportArchive` | Pure Fabrication | `archiveReport()` |
| `SecureStorage` | Pure Fabrication | `archiveReport(reportFile, retentionPolicy)` |
| `TaskScheduler` | Pure Fabrication | `scheduleReminders(cycleID, deadline)` |
| `PayrollModule` | External System | `triggerFinalSettlement(employeeID)` |

---

### Class Relationships Summary

| From | Relationship | To | Multiplicity |
|------|-------------|-----|-------------|
| Designation | assigned to | Department | 0..1 → 1 |
| Department | contains | Employee | 1 → 0..* |
| Employee | has | OffboardingWorkflow | 1 → 0..1 |
| Employee | has | AttendanceRecord | 1 → 0..* |
| Employee | submits | LeaveRequest | 1 → 0..* |
| Employee | generates | ProbationRecord | 1 → 0..1 |
| Employee | generates | PerformanceEvaluation | 1 → 0..* |
| Employee | has | LeaveBalance | 1 → 0..* |
| AttendanceRecord | corrected by | AttendanceCorrectionRequest | 1 → 0..* |
| LeaveBalance | deducted from | LeaveRequest | — |
| EvaluationCycle | contains | PerformanceEvaluation | 1 → 0..* |
| Admin | generates | ComplianceReport | — |
| Admin | generates | HRAnalyticsReport | — |
| Admin | configures | EvaluationCycle | — |
| HR | monitors | Employee | — |
| HR | reviews/approves | LeaveRequest | — |
| HR | approves | AttendanceCorrectionRequest | — |
| UserAccount | 1 | Employee | 0..1 |

---

## Refined Domain Model

> From Section 5 — Refined based on SSD/SD analysis. New entities added: `OffboardingWorkflow`, `LeaveBalance`. New relationships identified during analysis are incorporated.

### Entity: `Designation`
- `+designationID: String` (FK to Department)
- `+title: String`
- `+description: String`
- `+salaryGrade: String`
- **Relation:** assigned to `Department` (0..1 → 1)

### Entity: `Department`
- `+departmentID: String`
- `+departmentName: String`
- `+managerID: String`
- `+parentDepartmentID: String`
- `+description: String`
- `+maxHeadcount: Integer`
- `+currentHeadcount: Integer`
- **Relations:** contains `Employee` (1 → 0..*), subtype of `Department` (self)

### Entity: `Employee`
- `+employeeID: String`
- `+fullName: String`
- `+phoneNumber: String`
- `+email: String`
- `+dateOfBirth: Date`
- `+nationalID: String`
- `+gender: String`
- `+designation: String`
- `+departmentID: String`
- `+employmentType: String`
- `+employmentStatus: String`
- `+probationEndDate: Date`
- **Relations:** monitors (HR), has (OffboardingWorkflow 0..1), has (AttendanceRecord 0..*), submits (LeaveRequest 0..*), generates (ProbationRecord 0..1), evaluates (PerformanceEvaluation 0..*), has (LeaveBalance 0..*)

### Entity: `HR`
- `+hrID: String`
- `+employeeID: String`
- `+department: String`
- **Relations:** monitors `Employee` (1), reviews/approves `LeaveRequest`, approves `AttendanceCorrectionRequest`

### Entity: `Admin`
- `+adminID: String`
- `+employeeID: String`
- `+permissions: String[]`
- **Relations:** generates `ComplianceReport`, generates `HRAnalyticsReport`, configures `EvaluationCycle`

### Entity: `UserAccount`
- `+userID: String`
- `+employeeID: String`
- `+username: String`
- `+role: String`
- `+permissions: String[]`
- `+accountStatus: String`
- `+lastLogin: DateTime`
- **Relation:** 1 UserAccount → 0..1 Employee

### Entity: `OffboardingWorkflow` *(added in refinement)*
- `+workflowID: String`
- `+employeeID: String`
- `+separationType: String`
- `+hireDate: Date`
- `+lastWorkingDate: Date`
- `+exitReason: String`
- `+status: String`
- `+checklistItems: String[]`
- `+finalSettlementStatus: String`
- **Relation:** belongs to one `Employee`

### Entity: `AttendanceRecord`
- `+attendanceID: String`
- `+employeeID: String`
- `+attendanceDate: Date`
- `+checkInTime: DateTime`
- `+checkOutTime: DateTime`
- `+totalHours: Float`
- `+attendanceStatus: String`
- `+correctionFlag: Boolean`
- **Relations:** has (AttendanceCorrectionRequest 0..*), deducted from (LeaveBalance)

### Entity: `AttendanceCorrectionRequest`
- `+correctionID: String`
- `+attendanceID: String`
- `+employeeID: String`
- `+originalValue: String`
- `+correctedValue: String`
- `+justification: String`
- `+status: String`
- `+reviewDate: Date`
- **Methods:** `+logCorrectionAttempt()`, `+validateLogicalConsistency()`
- **Relation:** corrects `AttendanceRecord`

### Entity: `LeaveBalance` *(added in refinement)*
- `+balanceID: String`
- `+employeeID: String`
- `+leaveType: String`
- `+totalEntitled: Integer`
- `+used: Integer`
- `+remaining: Integer`
- `+year: Integer`
- **Relation:** deducted when `LeaveRequest` is approved

### Entity: `LeaveRequest`
- `+leaveType: String`
- `+employeeID: String`
- `+startDate: Date`
- `+endDate: Date`
- `+workingDays: Integer`
- `+reason: String`
- `+status: String`
- `+approvedBy: String`
- `+approvedDate: Date`
- `+comments: String`
- **Relations:** submitted by `Employee`, approved by `HR`

### Entity: `ProbationRecord`
- `+probationID: String`
- `+employeeID: String`
- `+startDate: Date`
- `+endDate: Date`
- `+extensions: Integer`
- `+decision: String`
- `+decisionDate: Date`
- `+reason: String`
- `+status: String`
- `+decisionMadeID: String`
- `+notes: String`
- **Relation:** belongs to `Employee`

### Entity: `PerformanceEvaluation`
- `+evaluationID: String`
- `+employeeID: String`
- `+evaluatorID: String`
- `+cycleID: String`
- `+evaluationDate: Date`
- `+aggregateScore: Float`
- `+remarks: String`
- `+status: String`
- **Relations:** belongs to `Employee`, encompassed by `EvaluationCycle`

### Entity: `EvaluationCycle`
- `+cycleID: String`
- `+cycleName: String`
- `+startDate: Date`
- `+endDate: Date`
- `+evaluationType: String`
- `+applicableScope: String`
- `+status: String`
- `+reminderSchedule: String`
- **Relations:** contains `PerformanceEvaluation`, configured by `Admin`

### Entity: `ComplianceReport`
- `+reportID: String`
- `+reportType: String`
- `+generatedAt: DateTime`
- `+generatedBy: String`
- `+parameters: String`
- `+format: String`
- `+status: String`
- `+archivePath: String`
- **Relation:** generated by `Admin`

### Entity: `HRAnalyticsReport`
- `+reportID: String`
- `+creatorID: String`
- `+reportPeriod: String`
- `+generatedAt: DateTime`
- `+summaryMetrics: String`
- **Relation:** generated by `Admin`

---

### Domain Model — Association Summary Table

| Entity A | Association | Entity B | Multiplicity |
|----------|-------------|----------|-------------|
| Designation | assigned to | Department | 0..1 → 1 |
| Department | contains | Employee | 1 → 0..* |
| Employee | has | OffboardingWorkflow | 1 → 0..1 |
| Employee | has | AttendanceRecord | 1 → 0..* |
| Employee | submits | LeaveRequest | 1 → 0..* |
| Employee | generates | ProbationRecord | 1 → 0..1 |
| Employee | generates | PerformanceEvaluation | 1 → 0..* |
| Employee | has | LeaveBalance | 1 → 0..* |
| AttendanceRecord | corrected by | AttendanceCorrectionRequest | 1 → 0..* |
| LeaveBalance | evaluates as | LeaveRequest | 0..* → 0..* |
| EvaluationCycle | contains | PerformanceEvaluation | 1 → 0..* |
| HR | monitors | Employee | 1 → 0..* |
| HR | reviews/approves | LeaveRequest | — |
| Admin | generates | ComplianceReport | 1 → 0..* |
| Admin | generates | HRAnalyticsReport | 1 → 0..* |
| Admin | configures | EvaluationCycle | 1 → 0..* |
| UserAccount | 1-1 | Employee | 0..1 → 1 |

---

*End of Implementation Reference — HR Management System*
