package com.hr.service;

// GoF Design Pattern: Strategy — defines the interchangeable formatting algorithm.
//
// Justification: Different stakeholders consume reports in different formats
// (PDF for legal archival, Excel for data analysis, CSV for system integration).
// Instead of if-else chains inside ReportGenerator, each format is its own
// strategy class that can be injected or swapped at runtime.  Adding a new format
// requires only a new implementation — no changes to existing code (Open/Closed).

public interface ReportFormatStrategy {

    /**
     * Formats raw aggregated data into the target output format.
     *
     * @param data pre-aggregated report content string
     * @return formatted report representation
     */
    String format(String data);

    /** Human-readable format name used in report metadata (e.g. "PDF"). */
    String getFormatName();
}
