package Ecommerce.scheduler;

import Ecommerce.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationCleanupScheduler {

    private final ReservationService reservationService;

    // Run every 5 minutes
    @Scheduled(fixedRate = 300000)
    public void cleanupExpiredReservations() {
        log.info("Starting cleanup of expired reservations...");
        try {
            reservationService.cleanupExpiredReservations();
        } catch (Exception e) {
            log.error("Failed to cleanup expired reservations: {}", e.getMessage(), e);
        }
    }
}

