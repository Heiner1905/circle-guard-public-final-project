package com.circleguard.dashboard.controller;

import com.circleguard.dashboard.service.AnalyticsService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnalyticsController.class)
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyticsService analyticsService;

    @Test
    void shouldReturnHealthBoardStats() throws Exception {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalGreen", 1500);
        stats.put("totalExposed", 45);

        Mockito.when(analyticsService.getGlobalHealthStats()).thenReturn(stats);

        mockMvc.perform(get("/api/v1/analytics/health-board")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalGreen").value(1500))
                .andExpect(jsonPath("$.totalExposed").value(45));
    }

    @Test
    void shouldReturnTrendsForLocation() throws Exception {
        UUID locationId = UUID.randomUUID();
        List<Map<String, Object>> trends = new ArrayList<>();
        Map<String, Object> entry = new HashMap<>();
        entry.put("hour", "08:00");
        entry.put("count", 120);
        trends.add(entry);

        Mockito.when(analyticsService.getEntryTrends(locationId)).thenReturn(trends);

        mockMvc.perform(get("/api/v1/analytics/trends/" + locationId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].hour").value("08:00"))
                .andExpect(jsonPath("$[0].count").value(120));
    }

    @Test
    void shouldReturnSummary() throws Exception {
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalStudents", 5000);
        summary.put("activeCampuses", 3);
        summary.put("totalEntries", 15000);

        Mockito.when(analyticsService.getCampusSummary()).thenReturn(summary);

        mockMvc.perform(get("/api/v1/analytics/summary")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalStudents").value(5000))
                .andExpect(jsonPath("$.activeCampuses").value(3))
                .andExpect(jsonPath("$.totalEntries").value(15000));
    }

    @Test
    void shouldReturnDepartmentStats() throws Exception {
        String department = "Computer Science";
        Map<String, Object> departmentStats = new HashMap<>();
        departmentStats.put("department", department);
        departmentStats.put("studentCount", 1200);
        departmentStats.put("entryCount", 4500);
        departmentStats.put("averageOccupancy", 75.5);

        Mockito.when(analyticsService.getDepartmentStats(department)).thenReturn(departmentStats);

        mockMvc.perform(get("/api/v1/analytics/department/{department}", department)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.department").value(department))
                .andExpect(jsonPath("$.studentCount").value(1200))
                .andExpect(jsonPath("$.entryCount").value(4500))
                .andExpect(jsonPath("$.averageOccupancy").value(75.5));
    }

    @Test
    void shouldReturnTimeSeriesWithDefaultParameters() throws Exception {
        List<Map<String, Object>> timeSeries = new ArrayList<>();
        Map<String, Object> dataPoint = new HashMap<>();
        dataPoint.put("timestamp", "2024-01-01T10:00:00");
        dataPoint.put("count", 250);
        timeSeries.add(dataPoint);

        Mockito.when(analyticsService.getTimeSeries("hourly", 24)).thenReturn(timeSeries);

        mockMvc.perform(get("/api/v1/analytics/time-series")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].timestamp").value("2024-01-01T10:00:00"))
                .andExpect(jsonPath("$[0].count").value(250));
    }

    @Test
    void shouldReturnTimeSeriesWithCustomParameters() throws Exception {
        List<Map<String, Object>> timeSeries = new ArrayList<>();
        Map<String, Object> dataPoint = new HashMap<>();
        dataPoint.put("timestamp", "2024-01-01T00:00:00");
        dataPoint.put("count", 100);
        timeSeries.add(dataPoint);

        Mockito.when(analyticsService.getTimeSeries("daily", 7)).thenReturn(timeSeries);

        mockMvc.perform(get("/api/v1/analytics/time-series")
                .param("period", "daily")
                .param("limit", "7")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].timestamp").value("2024-01-01T00:00:00"))
                .andExpect(jsonPath("$[0].count").value(100));
    }

    @Test
    void shouldReturnEmptyTimeSeriesWhenNoData() throws Exception {
        List<Map<String, Object>> emptyTimeSeries = new ArrayList<>();

        Mockito.when(analyticsService.getTimeSeries("hourly", 24)).thenReturn(emptyTimeSeries);

        mockMvc.perform(get("/api/v1/analytics/time-series")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldReturnEmptyDepartmentStatsWhenDepartmentNotFound() throws Exception {
        String department = "NonExistentDept";
        Map<String, Object> emptyStats = new HashMap<>();

        Mockito.when(analyticsService.getDepartmentStats(department)).thenReturn(emptyStats);

        mockMvc.perform(get("/api/v1/analytics/department/{department}", department)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldHandleSpecialCharactersInDepartmentName() throws Exception {
        String department = "Engineering & Applied Sciences";
        Map<String, Object> departmentStats = new HashMap<>();
        departmentStats.put("department", department);
        departmentStats.put("studentCount", 800);

        Mockito.when(analyticsService.getDepartmentStats(department)).thenReturn(departmentStats);

        mockMvc.perform(get("/api/v1/analytics/department/{department}", department)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.department").value(department))
                .andExpect(jsonPath("$.studentCount").value(800));
    }
}