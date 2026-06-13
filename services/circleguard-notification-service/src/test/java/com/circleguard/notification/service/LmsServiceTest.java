package com.circleguard.notification.service;

import org.junit.jupiter.api.Test;
import java.util.concurrent.CompletableFuture;
import static org.assertj.core.api.Assertions.assertThat;

public class LmsServiceTest {

    @Test
    void testRemoteAttendanceSync() {
        LmsService lmsService = new LmsServiceImpl();

        CompletableFuture<Void> future = lmsService.syncRemoteAttendance("student-123", "PROBABLE");
        future.join(); // Wait for completion
        assertThat(future).isCompleted();
    }
}
