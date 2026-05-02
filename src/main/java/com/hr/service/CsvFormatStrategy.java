package com.hr.service;

// GoF Design Pattern: Strategy — concrete CSV strategy.
// Justification: CSV is required for integration with third-party payroll and
// compliance tools that consume flat-file imports.  Its comma-delimited layout
// is distinct from PDF and Excel, justifying a dedicated strategy class.

public class CsvFormatStrategy implements ReportFormatStrategy {

    @Override
    public String format(String data) {
        return "report_type,content,format\n"
                + "HR_REPORT,\"" + data.replace("\"", "\"\"") + "\",CSV";
    }

    @Override
    public String getFormatName() { return "CSV"; }
}
