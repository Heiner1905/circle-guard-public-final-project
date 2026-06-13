package com.circleguard.dashboard.service;

import com.circleguard.dashboard.client.PromotionClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private JdbcTemplate jdbc;

    @Mock
    private PromotionClient promotionClient;

    @Mock
    private KAnonymityFilter kAnonymityFilter;

    @InjectMocks
    private AnalyticsService service;

    @Test
    void getCampusSummaryDelegatesToPromotionService() {
        Map<String, Object> stats = Map.of("totalUsers", 120);
        when(promotionClient.getHealthStats()).thenReturn(stats);

        assertSame(stats, service.getCampusSummary());
    }

    @Test
    void getDepartmentStatsAppliesKAnonymityFilter() {
        Map<String, Object> raw = Map.of("department", "Health", "totalUsers", 4);
        Map<String, Object> filtered = Map.of("department", "Health", "totalUsers", "<5");
        when(promotionClient.getHealthStatsByDepartment("Health")).thenReturn(raw);
        when(kAnonymityFilter.apply(raw)).thenReturn(filtered);

        assertSame(filtered, service.getDepartmentStats("Health"));
    }

    @Test
    void getEntryTrendsMasksSmallCounts() {
        UUID locationId = UUID.randomUUID();
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("entry_count", 3L);
        List<Map<String, Object>> rows = new ArrayList<>(List.of(row));
        when(jdbc.queryForList(anyString(), eq(locationId))).thenReturn(rows);

        List<Map<String, Object>> result = service.getEntryTrends(locationId);

        assertEquals("<5", result.get(0).get("entry_count"));
        assertEquals("Insufficient data for privacy", result.get(0).get("note"));
    }

    @Test
    void getTimeSeriesFallsBackWhenDashboardTableIsMissing() {
        when(jdbc.queryForList(anyString(), eq(2))).thenThrow(new IllegalStateException("missing table"));

        List<Map<String, Object>> result = service.getTimeSeries("hourly", 2);

        assertEquals(8, result.size());
        assertTrue(result.stream().allMatch(point -> point.containsKey("status")));
    }
}
