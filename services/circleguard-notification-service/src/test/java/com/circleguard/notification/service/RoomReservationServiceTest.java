package com.circleguard.notification.service;

import org.junit.jupiter.api.Test;
import java.util.concurrent.CompletableFuture;
import static org.assertj.core.api.Assertions.assertThat;

public class RoomReservationServiceTest {

    @Test
    void testCancelReservation() {
        RoomReservationService roomReservationService = new RoomReservationServiceImpl();

        CompletableFuture<Void> future = roomReservationService.cancelReservation("circle-1", "loc-1");
        future.join(); // Wait for completion
        assertThat(future).isCompleted();
    }
}
