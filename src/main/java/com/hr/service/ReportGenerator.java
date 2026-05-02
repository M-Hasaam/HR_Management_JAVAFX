package com.hr.service;

// GRASP Pattern: Pure Fabrication — report generation is not a domain responsibility.
// GoF Pattern: Strategy — the output format (PDF / Excel / CSV) is injected as a
//   ReportFormatStrategy and applied at generation time.  Callers can switch formats
//   without modifying this class (Open/Closed Principle).
//
// Justification for Strategy here: compliance officers need PDF, managers need Excel,
// integration scripts need CSV.  Rather than adding if-else blocks per format,
// each concrete strategy encapsulates its own rendering logic.

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class ReportGenerator {

    // Registry maps format name → strategy (populated with built-in strategies)
    private final Map<String, ReportFormatStrategy> strategies = new HashMap<>();

    public ReportGenerator() {
        // Register default strategies
        registerStrategy(new PdfFormatStrategy());
        registerStrategy(new ExcelFormatStrategy());
        registerStrategy(new CsvFormatStrategy());
    }

    /** Allows callers or subclasses to plug in additional or overriding strategies. */
    public void registerStrategy(ReportFormatStrategy strategy) {
        strategies.put(strategy.getFormatName().toUpperCase(), strategy);
    }

    /**
     * Generates a combined leave-balance and attendance summary report.
     *
     * @param balanceData    leave balance data object (opaque for formatting)
     * @param attendanceData attendance records data object
     * @param format         target format name ("PDF", "EXCEL", "CSV")
     * @return formatted report string produced by the chosen strategy
     */
    public String generateReport(Object balanceData, Object attendanceData, String format) {
        String raw = "Leave Balance and Attendance Summary | Generated: "
                + LocalDateTime.now()
                + " | Balance: " + balanceData
                + " | Attendance: " + attendanceData;
        return applyStrategy(raw, format);
    }

    /**
     * Formats a pre-aggregated compliance data string.
     *
     * @param complianceData pre-aggregated compliance data string
     * @param format         target format name
     * @return formatted compliance report string
     */
    public String generateAndFormat(String complianceData, String format) {
        String raw = "Compliance Data: " + complianceData + " | Generated: " + LocalDateTime.now();
        return applyStrategy(raw, format);
    }

    /** Applies the registered strategy for the given format, falling back to plain text. */
    private String applyStrategy(String raw, String format) {
        ReportFormatStrategy strategy = strategies.get(format != null ? format.toUpperCase() : "PDF");
        if (strategy == null) {
            // Fallback — plain text (no matching strategy registered)
            return "[REPORT:" + format + "] " + raw;
        }
        return strategy.format(raw);
    }
}
