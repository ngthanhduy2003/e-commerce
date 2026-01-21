package Ecommerce.controller;

import Ecommerce.dto.response.ApiResponse;
import Ecommerce.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Test Controller - Only available in dev/test profiles
 * USE FOR TESTING ONLY - REMOVE IN PRODUCTION
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
@Profile({"dev", "test"})
public class TestController {

    private final ReservationService reservationService;

    /**
     * Manually trigger reservation cleanup
     * Useful for testing without waiting 5 minutes
     */
    @PostMapping("/cleanup-reservations")
    public ResponseEntity<ApiResponse<?>> triggerReservationCleanup() {
        log.info("Manual reservation cleanup triggered");

        try {
            reservationService.cleanupExpiredReservations();
            return ResponseEntity.ok(
                ApiResponse.success("Reservation cleanup completed successfully")
            );
        } catch (Exception e) {
            log.error("Failed to cleanup reservations: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Cleanup failed: " + e.getMessage()));
        }
    }

    /**
     * Get system info for debugging
     */
    @GetMapping("/system-info")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemInfo() {
        Map<String, Object> info = Map.of(
            "timestamp", java.time.LocalDateTime.now().toString(),
            "profile", "dev/test",
            "javaVersion", System.getProperty("java.version"),
            "availableProcessors", Runtime.getRuntime().availableProcessors(),
            "maxMemory", Runtime.getRuntime().maxMemory() / (1024 * 1024) + " MB",
            "freeMemory", Runtime.getRuntime().freeMemory() / (1024 * 1024) + " MB"
        );

        return ResponseEntity.ok(ApiResponse.success("System info retrieved", info));
    }
}

