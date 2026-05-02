package com.hr.service;

// GRASP Pattern: Information Expert — owns KPI aggregation across all HR modules.
// This class holds all three DAOs needed to compute the four key HR KPIs, making
// it the single expert responsible for cross-module metrics.  No domain class
// spans all three data sources, so a Pure-Fabrication/Information-Expert hybrid
// is the appropriate GRASP solution.

import com.hr.dao.AttendanceRecordDAO;
import com.hr.dao.EmployeeDAO;
import com.hr.dao.LeaveRequestDAO;
import com.hr.model.Employee;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsAggregator {

    private final EmployeeDAO        employeeDAO;
    private final LeaveRequestDAO    leaveDAO;
    private final AttendanceRecordDAO attendanceDAO;

    public AnalyticsAggregator() throws SQLException {
        this.employeeDAO   = new EmployeeDAO();
        this.leaveDAO      = new LeaveRequestDAO();
        this.attendanceDAO = new AttendanceRecordDAO();
    }

    /**
     * Validates which of the requested metric names are actually available.
     * In this implementation all requested metrics are supported; any unknown
     * metric names are logged and excluded from the returned list.
     *
     * @param metrics List of metric names requested by the caller
     * @return Filtered list containing only recognised metric names
     */
    public List<String> validateMetricsAvailability(List<String> metrics) {
        List<String> supported = List.of("headcount", "attrition", "leave", "attendance");
        List<String> unavailable = new ArrayList<>();
        for (String m : metrics) {
            if (!supported.contains(m)) {
                unavailable.add(m);
            }
        }
        return unavailable;
    }

    /**
     * Builds a KPI map based on the flags provided by the caller.
     *
     * <ul>
     *   <li>headcount       — total number of employees in the system</li>
     *   <li>attrition       — number of employees with status INACTIVE</li>
     *   <li>leaveUtilization— total leave requests recorded</li>
     *   <li>attendanceRate  — percentage of non-absent attendance records</li>
     * </ul>
     *
     * @param headcount   include headcount KPI
     * @param attrition   include attrition KPI
     * @param leave       include leave-utilization KPI
     * @param attendance  include attendance-rate KPI
     * @return Map of KPI name → computed value
     * @throws SQLException if a database query fails
     */
    public Map<String, Object> aggregateKPI(boolean headcount, boolean attrition,
                                             boolean leave, boolean attendance) throws SQLException {
        Map<String, Object> kpi = new HashMap<>();

        List<Employee> allEmployees = employeeDAO.getAll();

        if (headcount) {
            kpi.put("headcount", allEmployees.size());
        }

        if (attrition) {
            long inactive = allEmployees.stream()
                    .filter(e -> "INACTIVE".equalsIgnoreCase(e.getStatus()))
                    .count();
            kpi.put("attrition", (int) inactive);
        }

        if (leave) {
            int leaveCount = leaveDAO.getAll().size();
            kpi.put("leaveUtilization", leaveCount);
        }

        if (attendance) {
            double rate = attendanceDAO.getAttendanceRate();
            kpi.put("attendanceRate", rate);
        }

        return kpi;
    }
}
