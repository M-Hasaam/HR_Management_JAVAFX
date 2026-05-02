-- UC-06 Migration: run this ONCE on your existing hr_management database
-- (only needed if you already have the database — fresh installs use schema.sql)

USE hr_management;

-- 1. Add document_path column to leave_requests
ALTER TABLE leave_requests
    ADD COLUMN IF NOT EXISTS document_path VARCHAR(500);

-- 2. Add PENDING_DOCUMENT to the status ENUM
ALTER TABLE leave_requests
    MODIFY COLUMN status ENUM('PENDING','APPROVED','REJECTED','PENDING_DOCUMENT') DEFAULT 'PENDING';

-- 3. Create public_holidays table
CREATE TABLE IF NOT EXISTS public_holidays (
    id INT AUTO_INCREMENT PRIMARY KEY,
    holiday_date DATE NOT NULL UNIQUE,
    description VARCHAR(200) NOT NULL
);

-- 4. Seed public holidays (Pakistan + common)
INSERT IGNORE INTO public_holidays (holiday_date, description) VALUES
('2025-03-23', 'Pakistan Day'),
('2025-05-01', 'Labour Day'),
('2025-08-14', 'Independence Day'),
('2025-12-25', 'Christmas Day'),
('2026-03-23', 'Pakistan Day'),
('2026-05-01', 'Labour Day'),
('2026-08-14', 'Independence Day'),
('2026-12-25', 'Christmas Day');
