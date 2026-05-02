package com.hr.service;

// GRASP Pattern: Pure Fabrication (GoF: Strategy pattern for chart rendering).
// Chart rendering is not a responsibility of any domain object.  By isolating it
// here, the rendering "strategy" (bar chart, pie chart, line graph, etc.) can be
// swapped at runtime without affecting callers — a classic Strategy application.

import java.util.Map;

public class DataVisualization {

    /**
     * Generates visual charts from a KPI data map.
     * In a real system this would delegate to a charting library (e.g. JFreeChart,
     * Chart.js via WebView, or an external BI tool).  Here we return a descriptive
     * string suitable for logging and testing.
     *
     * @param kpiData Map of KPI names to their computed values
     * @return A description of the charts that were generated
     */
    public String generateCharts(Map<String, Object> kpiData) {
        return "Charts generated for metrics: " + kpiData.keySet()
                + " | Data: " + kpiData.values();
    }
}
