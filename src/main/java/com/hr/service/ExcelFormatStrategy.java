package com.hr.service;

// GoF Design Pattern: Strategy — concrete Excel strategy.
// Justification: HR managers need spreadsheet exports for further data analysis
// and pivot tables.  Excel layout (tab-separated, header row) differs enough
// from PDF that it warrants its own strategy class.

public class ExcelFormatStrategy implements ReportFormatStrategy {

    @Override
    public String format(String data) {
        return "[EXCEL REPORT]\n"
                + "Report Type\tData\tTimestamp\n"
                + data + "\t[auto-calculated]\t[now]\n"
                + "Format: XLSX (Excel-compatible)";
    }

    @Override
    public String getFormatName() { return "EXCEL"; }
}
