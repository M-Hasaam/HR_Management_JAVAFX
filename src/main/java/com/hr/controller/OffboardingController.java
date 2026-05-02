package com.hr.controller;
// GRASP Pattern: Controller — handles UC-04 Initiate Employee Offboarding
// Participants: OffboardingWorkflow (Creator), Employee (Information Expert),
//              PayrollModule (External System via PayrollNotifier — Pure Fabrication)

import com.hr.dao.OffboardingWorkflowDAO;
import com.hr.model.Employee;
import com.hr.model.OffboardingWorkflow;
import com.hr.service.EmployeeService;
import com.hr.service.PayrollNotifier;

import java.sql.SQLException;
import java.time.LocalDate;

public class OffboardingController {

    private final EmployeeService employeeService;
    private final OffboardingWorkflowDAO offboardingDAO;
    private final PayrollNotifier payrollNotifier; // Pure Fabrication

    public OffboardingController() throws SQLException {
        this.employeeService = new EmployeeService();
        this.offboardingDAO  = new OffboardingWorkflowDAO();
        this.payrollNotifier = new PayrollNotifier(); // no SQLException — plain constructor
    }

    /**
     * UC-04: Initiate the offboarding workflow for an employee.
     * Creates an OffboardingWorkflow record, marks the employee as OFFBOARDING,
     * and triggers the final payroll settlement.
     *
     * @param employeeID      ID of the employee being offboarded
     * @param separationType  e.g. RESIGNATION, RETIREMENT, CONTRACT_EXPIRY, TERMINATION
     * @param lastWorkingDate the employee's last day
     * @throws SQLException             on database error
     * @throws IllegalArgumentException if the notice-period policy is violated
     */
    public void initiateOffboarding(int employeeID, String separationType,
                                    LocalDate lastWorkingDate) throws SQLException {
        Employee emp = employeeService.getEmployee(employeeID);

        OffboardingWorkflow wf = new OffboardingWorkflow();
        wf.setEmployeeId(employeeID);
        wf.setSeparationType(separationType);
        wf.setLastWorkingDate(lastWorkingDate);
        wf.setStatus("OFFBOARDING_PENDING_CLEARANCE");
        wf.setFinalSettlementStatus("PENDING");

        if (!wf.validateNoticePeriod()) {
            throw new IllegalArgumentException(
                    "Notice period policy violated for separation type: " + separationType);
        }

        offboardingDAO.insert(wf);

        emp.setStatus("OFFBOARDING");
        employeeService.updateEmployee(emp);

        payrollNotifier.triggerFinalSettlement(employeeID);
    }

    /**
     * Directly triggers final payroll settlement (can be called standalone).
     */
    public void triggerFinalSettlement(int employeeID) {
        payrollNotifier.triggerFinalSettlement(employeeID);
    }

    /**
     * Returns the offboarding workflow record for a given employee, or {@code null} if none exists.
     */
    public OffboardingWorkflow getOffboardingRecord(int employeeID) throws SQLException {
        return offboardingDAO.getByEmployee(employeeID);
    }
}
