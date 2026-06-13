package com.circleguard.dashboard.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class KAnonymityFilterTest {

    private KAnonymityFilter filter;

    @BeforeEach
    void setUp() {
        filter = new KAnonymityFilter();
    }

    @Test
    void apply_WithNullStats_ReturnsEmptyMap() {
        // Act
        Map<String, Object> result = filter.apply(null);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void apply_WithDefaultK_AndTotalUsersBelowK_MasksEntireResult() {
        // Arrange
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalUsers", 3);
        stats.put("department", "Computer Science");
        stats.put("timestamp", "2024-01-01");
        stats.put("activeCount", 3);
        stats.put("exposedCount", 1);

        // Act
        Map<String, Object> result = filter.apply(stats);

        // Assert
        assertThat(result).containsEntry("note", "Insufficient data for privacy");
        assertThat(result).containsEntry("department", "Computer Science");
        assertThat(result).containsEntry("timestamp", "2024-01-01");
        assertThat(result).containsEntry("totalUsers", "<5");
        assertThat(result).doesNotContainKey("activeCount");
        assertThat(result).doesNotContainKey("exposedCount");
    }

    @Test
    void apply_WithDefaultK_AndTotalUsersAboveK_OnlyMasksSmallCounts() {
        // Arrange
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalUsers", 10);
        stats.put("department", "Computer Science");
        stats.put("activeCount", 8);      // 8 >= 5, no mask
        stats.put("exposedCount", 2);     // 2 < 5, should be masked

        // Act
        Map<String, Object> result = filter.apply(stats);

        // Assert
        assertThat(result).containsEntry("totalUsers", 10);
        assertThat(result).containsEntry("activeCount", 8);
        assertThat(result).containsEntry("exposedCount", "<5");  // Masked because 2 < 5
        assertThat(result).doesNotContainKey("note");
    }

    @Test
    void apply_WithCustomK_AndTotalUsersBelowK_MasksEntireResult() {
        // Arrange
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalUsers", 2);
        stats.put("department", "Mathematics");
        stats.put("activeCount", 2);
        stats.put("exposedCount", 0);

        // Act
        Map<String, Object> result = filter.apply(stats, 3);

        // Assert
        assertThat(result).containsEntry("note", "Insufficient data for privacy");
        assertThat(result).containsEntry("department", "Mathematics");
        assertThat(result).containsEntry("totalUsers", "<3");
        assertThat(result).doesNotContainKey("activeCount");
        assertThat(result).doesNotContainKey("exposedCount");
    }

    @Test
    void apply_WithCustomK_AndTotalUsersAboveK_OnlyMasksSmallCounts() {
        // Arrange
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalUsers", 10);
        stats.put("department", "Physics");
        stats.put("activeCount", 10);     // 10 >= 3, no mask
        stats.put("exposedCount", 2);     // 2 < 3, should be masked

        // Act
        Map<String, Object> result = filter.apply(stats, 3);

        // Assert
        assertThat(result).containsEntry("totalUsers", 10);
        assertThat(result).containsEntry("activeCount", 10);
        assertThat(result).containsEntry("exposedCount", "<3");  // Masked because 2 < 3
        assertThat(result).doesNotContainKey("note");
    }

    @Test
    void apply_MasksIndividualCountFieldsBelowK() {
        // Arrange
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalUsers", 50);
        stats.put("activeCount", 3);
        stats.put("exposedCount", 4);
        stats.put("confirmedCount", 10);
        stats.put("recoveredCount", 2);
        stats.put("nonCountField", "shouldNotBeMasked");

        // Act
        Map<String, Object> result = filter.apply(stats, 5);

        // Assert
        assertThat(result).containsEntry("totalUsers", 50);
        assertThat(result).containsEntry("activeCount", "<5");
        assertThat(result).containsEntry("exposedCount", "<5");
        assertThat(result).containsEntry("confirmedCount", 10);
        assertThat(result).containsEntry("recoveredCount", "<5");
        assertThat(result).containsEntry("nonCountField", "shouldNotBeMasked");
    }

    @Test
    void apply_DoesNotMaskZeroCounts() {
        // Arrange
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalUsers", 50);
        stats.put("activeCount", 0);
        stats.put("exposedCount", 0);

        // Act
        Map<String, Object> result = filter.apply(stats);

        // Assert
        assertThat(result).containsEntry("activeCount", 0);
        assertThat(result).containsEntry("exposedCount", 0);
    }

    @Test
    void apply_WithTotalUsersAsString_HandlesGracefully() {
        // Arrange
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalUsers", "invalid");
        stats.put("activeCount", 3);

        // Act
        Map<String, Object> result = filter.apply(stats);

        // Assert
        assertThat(result).containsEntry("activeCount", "<5");
        assertThat(result).doesNotContainKey("note");
    }

    @Test
    void apply_WithMissingTotalUsers_DoesNotMaskEntireResult() {
        // Arrange
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("department", "Engineering");
        stats.put("activeCount", 3);
        stats.put("exposedCount", 4);

        // Act
        Map<String, Object> result = filter.apply(stats, 5);

        // Assert
        assertThat(result).doesNotContainKey("note");
        assertThat(result).containsEntry("activeCount", "<5");
        assertThat(result).containsEntry("exposedCount", "<5");
        assertThat(result).containsEntry("department", "Engineering");
    }

    @Test
    void apply_WithCountFieldsNotEndingWithCount_DoesNotMask() {
        // Arrange
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalUsers", 50);
        stats.put("active", 3);
        stats.put("exposed", 4);
        stats.put("peopleCount", 2); // This one ends with "Count" so should be masked

        // Act
        Map<String, Object> result = filter.apply(stats, 5);

        // Assert
        assertThat(result).containsEntry("active", 3);
        assertThat(result).containsEntry("exposed", 4);
        assertThat(result).containsEntry("peopleCount", "<5");
    }

    @Test
    void apply_WithEmptyMap_ReturnsEmptyMap() {
        // Arrange
        Map<String, Object> stats = new LinkedHashMap<>();

        // Act
        Map<String, Object> result = filter.apply(stats);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void apply_WithTotalUsersExactlyAtK_DoesNotMaskTotal() {
        // Arrange
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalUsers", 5);
        stats.put("activeCount", 5);     // 5 >= 5, no mask
        stats.put("exposedCount", 2);    // 2 < 5, should be masked

        // Act
        Map<String, Object> result = filter.apply(stats, 5);

        // Assert
        assertThat(result).containsEntry("totalUsers", 5);
        assertThat(result).containsEntry("activeCount", 5);
        assertThat(result).containsEntry("exposedCount", "<5");  // Masked
        assertThat(result).doesNotContainKey("note");
    }

    @Test
    void apply_WithTotalUsersBelowK_ButNoDepartmentOrTimestamp_ReturnsMinimalMaskedResult() {
        // Arrange
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalUsers", 2);
        stats.put("activeCount", 2);

        // Act
        Map<String, Object> result = filter.apply(stats, 5);

        // Assert
        assertThat(result).containsEntry("note", "Insufficient data for privacy");
        assertThat(result).containsEntry("totalUsers", "<5");
        assertThat(result).hasSize(2);
    }

    @Test
    void apply_PreservesOriginalMapOrder() {
        // Arrange
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalUsers", 50);
        stats.put("department", "Math");
        stats.put("activeCount", 3);
        stats.put("exposedCount", 4);
        stats.put("timestamp", "2024-01-01");

        // Act
        Map<String, Object> result = filter.apply(stats, 5);

        // Assert
        assertThat(result.keySet()).containsExactly(
            "totalUsers", "department", "activeCount", "exposedCount", "timestamp"
        );
    }

    @Test
    void apply_WithAllCountsAboveThreshold_NoMasking() {
        // Arrange
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalUsers", 100);
        stats.put("activeCount", 50);
        stats.put("exposedCount", 30);
        stats.put("confirmedCount", 10);

        // Act
        Map<String, Object> result = filter.apply(stats, 5);

        // Assert
        assertThat(result).containsEntry("totalUsers", 100);
        assertThat(result).containsEntry("activeCount", 50);
        assertThat(result).containsEntry("exposedCount", 30);
        assertThat(result).containsEntry("confirmedCount", 10);
        assertThat(result).doesNotContainKey("note");
    }

    @Test
    void apply_WithMixedCounts_MasksOnlySmallValues() {
        // Arrange
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalUsers", 100);
        stats.put("greenCount", 45);
        stats.put("yellowCount", 4);   // Should be masked
        stats.put("redCount", 1);      // Should be masked
        stats.put("blueCount", 12);

        // Act
        Map<String, Object> result = filter.apply(stats, 5);

        // Assert
        assertThat(result).containsEntry("greenCount", 45);
        assertThat(result).containsEntry("yellowCount", "<5");
        assertThat(result).containsEntry("redCount", "<5");
        assertThat(result).containsEntry("blueCount", 12);
    }
}