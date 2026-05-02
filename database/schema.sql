-- ============================================================
-- HR Management System - Database Schema
-- Drop all tables in reverse dependency order, then recreate
-- ============================================================

CREATE DATABASE IF NOT EXISTS hr_management;
USE hr_management;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS org_history;
DROP TABLE IF EXISTS audit_log;
DROP TABLE IF EXISTS hr_analytics_reports;
DROP TABLE IF EXISTS compliance_reports;
DROP TABLE IF EXISTS performance_evaluations;
DROP TABLE IF EXISTS evaluation_cycles;
DROP TABLE IF EXISTS probation_records;
DROP TABLE IF EXISTS offboarding_workflows;
DROP TABLE IF EXISTS attendance_corrections;
DROP TABLE IF EXISTS attendance_records;
DROP TABLE IF EXISTS leave_balances;
DROP TABLE IF EXISTS leave_requests;
DROP TABLE IF EXISTS public_holidays;
DROP TABLE IF EXISTS payroll;
DROP TABLE IF EXISTS user_accounts;
DROP TABLE IF EXISTS employees;
DROP TABLE IF EXISTS designations;
DROP TABLE IF EXISTS departments;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 1. departments
-- ============================================================
CREATE TABLE departments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    manager_id INT DEFAULT NULL,
    parent_dept_id INT DEFAULT NULL,
    max_headcount INT DEFAULT 50,
    current_headcount INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 2. designations
-- ============================================================
CREATE TABLE designations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    salary_grade VARCHAR(20)
);

-- ============================================================
-- 3. employees
-- ============================================================
CREATE TABLE employees (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    national_id VARCHAR(20) UNIQUE,
    date_of_birth DATE,
    gender VARCHAR(10),
    department_id INT,
    designation_id INT,
    position VARCHAR(100),
    employment_type ENUM('FULL_TIME','PART_TIME','CONTRACT','PERMANENT') DEFAULT 'FULL_TIME',
    basic_salary DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    hire_date DATE NOT NULL,
    probation_end_date DATE,
    status ENUM('ACTIVE','INACTIVE','ON_LEAVE','OFFBOARDING') DEFAULT 'ACTIVE',
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL,
    FOREIGN KEY (designation_id) REFERENCES designations(id) ON DELETE SET NULL
);

-- ============================================================
-- 4. user_accounts
-- ============================================================
CREATE TABLE user_accounts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT UNIQUE NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('ADMIN','HR','EMPLOYEE') NOT NULL DEFAULT 'EMPLOYEE',
    account_status ENUM('ACTIVE','INACTIVE','LOCKED') DEFAULT 'ACTIVE',
    last_login TIMESTAMP NULL,
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
);

-- ============================================================
-- 5. payroll
-- ============================================================
CREATE TABLE payroll (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    pay_period_start DATE NOT NULL,
    pay_period_end DATE NOT NULL,
    basic_salary DECIMAL(10,2) NOT NULL,
    tax_deduction DECIMAL(10,2) NOT NULL,
    benefits_deduction DECIMAL(10,2) NOT NULL,
    net_pay DECIMAL(10,2) NOT NULL,
    processed_date DATE NOT NULL,
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
);

-- ============================================================
-- 6. leave_requests
-- ============================================================
CREATE TABLE leave_requests (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    leave_type ENUM('ANNUAL','SICK','PERSONAL') NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    days_requested INT NOT NULL,
    reason TEXT,
    status ENUM('PENDING','APPROVED','REJECTED','PENDING_DOCUMENT') DEFAULT 'PENDING',
    approved_by VARCHAR(100),
    approved_date DATE,
    comments TEXT,
    applied_date DATE NOT NULL,
    document_path VARCHAR(500),
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
);

-- ============================================================
-- 7. leave_balances
-- ============================================================
CREATE TABLE leave_balances (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL UNIQUE,
    annual_balance INT DEFAULT 21,
    sick_balance INT DEFAULT 14,
    personal_balance INT DEFAULT 7,
    year INT DEFAULT 2026,
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
);

-- ============================================================
-- 8. public_holidays  (UC-06 Alternative Flow 2b)
-- ============================================================
CREATE TABLE public_holidays (
    id INT AUTO_INCREMENT PRIMARY KEY,
    holiday_date DATE NOT NULL UNIQUE,
    description VARCHAR(200) NOT NULL
);

INSERT INTO public_holidays (holiday_date, description) VALUES
('2025-03-23', 'Pakistan Day'),
('2025-05-01', 'Labour Day'),
('2025-08-14', 'Independence Day'),
('2025-12-25', 'Christmas Day'),
('2026-03-23', 'Pakistan Day'),
('2026-05-01', 'Labour Day'),
('2026-08-14', 'Independence Day'),
('2026-12-25', 'Christmas Day');

-- ============================================================
-- 9. attendance_records
-- ============================================================
CREATE TABLE attendance_records (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    attendance_date DATE NOT NULL,
    check_in_time DATETIME,
    check_out_time DATETIME,
    total_hours DECIMAL(5,2),
    attendance_status ENUM('PRESENT','LATE','EARLY_DEPARTURE','ABSENT','INCOMPLETE') DEFAULT 'PRESENT',
    correction_flag BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
);

-- ============================================================
-- 9. attendance_corrections
-- ============================================================
CREATE TABLE attendance_corrections (
    id INT AUTO_INCREMENT PRIMARY KEY,
    attendance_id INT NOT NULL,
    employee_id INT NOT NULL,
    original_value VARCHAR(255),
    corrected_value VARCHAR(255),
    justification TEXT,
    status ENUM('PENDING_HR_REVIEW','APPROVED','REJECTED') DEFAULT 'PENDING_HR_REVIEW',
    review_date DATE,
    FOREIGN KEY (attendance_id) REFERENCES attendance_records(id),
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);

-- ============================================================
-- 10. offboarding_workflows
-- ============================================================
CREATE TABLE offboarding_workflows (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    separation_type ENUM('RESIGNATION','RETIREMENT','CONTRACT_EXPIRY','TERMINATION') NOT NULL,
    hire_date DATE,
    last_working_date DATE NOT NULL,
    exit_reason TEXT,
    status ENUM('OFFBOARDING_PENDING_CLEARANCE','CLEARANCE_COMPLETE','ARCHIVED') DEFAULT 'OFFBOARDING_PENDING_CLEARANCE',
    checklist_items TEXT,
    final_settlement_status ENUM('PENDING','PROCESSING','SETTLED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);

-- ============================================================
-- 11. probation_records
-- ============================================================
CREATE TABLE probation_records (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    extensions INT DEFAULT 0,
    decision ENUM('CONFIRMED','EXTENDED','TERMINATED','PENDING') DEFAULT 'PENDING',
    decision_date DATE,
    reason TEXT,
    status ENUM('ACTIVE','CLOSED') DEFAULT 'ACTIVE',
    decision_made_by INT,
    notes TEXT,
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);

-- ============================================================
-- 12. evaluation_cycles
-- ============================================================
CREATE TABLE evaluation_cycles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cycle_name VARCHAR(100) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    evaluation_type VARCHAR(50),
    applicable_scope VARCHAR(100),
    status ENUM('DRAFT','ACTIVE','CLOSED') DEFAULT 'DRAFT',
    reminder_schedule VARCHAR(100),
    grace_period_until DATE NULL
);

-- ============================================================
-- 13. performance_evaluations
-- ============================================================
CREATE TABLE performance_evaluations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    evaluator_id INT NOT NULL,
    cycle_id INT,
    evaluation_date DATE NOT NULL,
    aggregate_score DECIMAL(5,2),
    remarks TEXT,
    status ENUM('DRAFT','SUBMITTED','APPROVED') DEFAULT 'SUBMITTED',
    FOREIGN KEY (employee_id) REFERENCES employees(id),
    FOREIGN KEY (cycle_id) REFERENCES evaluation_cycles(id)
);

-- ============================================================
-- 14. compliance_reports
-- ============================================================
CREATE TABLE compliance_reports (
    id INT AUTO_INCREMENT PRIMARY KEY,
    report_type VARCHAR(50) NOT NULL,
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    generated_by INT,
    parameters TEXT,
    format VARCHAR(20) DEFAULT 'PDF',
    status ENUM('GENERATED','ARCHIVED') DEFAULT 'GENERATED',
    archive_path VARCHAR(500)
);

-- ============================================================
-- 15. hr_analytics_reports
-- ============================================================
CREATE TABLE hr_analytics_reports (
    id INT AUTO_INCREMENT PRIMARY KEY,
    creator_id INT,
    report_period VARCHAR(50),
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    summary_metrics TEXT
);

-- ============================================================
-- 16. audit_log
-- ============================================================
CREATE TABLE audit_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    action VARCHAR(100),
    entity_type VARCHAR(50),
    entity_id INT,
    field_name VARCHAR(100),
    old_value TEXT,
    new_value TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 17. org_history
-- ============================================================
CREATE TABLE org_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    old_dept_id INT,
    new_dept_id INT,
    effective_date DATE,
    remark TEXT,
    recorded_by INT,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- SAMPLE DATA
-- ============================================================

-- Departments (insert without manager_id first to avoid FK cycle)
INSERT INTO departments (id, name, description, max_headcount, current_headcount) VALUES
(1, 'Engineering',     'Software development and infrastructure',  100, 1),
(2, 'Human Resources', 'HR operations and talent management',       30, 1),
(3, 'Finance',         'Financial planning and accounting',          20, 1),
(4, 'Marketing',       'Brand, campaigns and market research',       25, 1);

-- Designations
INSERT INTO designations (id, title, description, salary_grade) VALUES
(1, 'Software Engineer', 'Develops and maintains software systems', 'G4'),
(2, 'HR Manager',        'Manages HR operations and team',          'G6'),
(3, 'Finance Analyst',   'Financial analysis and reporting',        'G5');

-- Employees
INSERT INTO employees (id, first_name, last_name, email, phone, national_id, date_of_birth, gender,
    department_id, designation_id, position, employment_type, basic_salary, hire_date, probation_end_date, status)
VALUES
(1, 'Ali',    'Hassan', 'ali.hassan@hrms.com',   '03001234567', '3520112345671', '1990-05-15', 'Male',
    1, 1, 'Software Engineer', 'FULL_TIME', 85000.00, '2023-01-10', '2023-04-10', 'ACTIVE'),
(2, 'Sara',   'Ahmed',  'sara.ahmed@hrms.com',   '03009876543', '3520198765432', '1988-11-22', 'Female',
    2, 2, 'HR Manager',       'FULL_TIME', 95000.00, '2022-06-01', '2022-09-01', 'ACTIVE'),
(3, 'Usman',  'Khan',   'usman.khan@hrms.com',   '03331122334', '3520133445566', '1992-03-30', 'Male',
    3, 3, 'Finance Analyst',  'FULL_TIME', 78000.00, '2023-03-15', '2023-06-15', 'ACTIVE'),
(4, 'Ayesha', 'Malik',  'ayesha.malik@hrms.com', '03218765432', '3520187654321', '1995-07-08', 'Female',
    4, 1, 'Marketing Analyst','PART_TIME', 60000.00, '2024-01-20', '2024-04-20', 'ACTIVE');

-- Set department manager after employees exist
UPDATE departments SET manager_id = 2 WHERE id = 2;

-- Leave balances for all 4 employees
INSERT INTO leave_balances (employee_id, annual_balance, sick_balance, personal_balance, year) VALUES
(1, 21, 14, 7, 2026),
(2, 21, 14, 7, 2026),
(3, 21, 14, 7, 2026),
(4, 21, 14, 7, 2026);
