package Ecommerce.service;

import Ecommerce.config.ReservationConfig;
import Ecommerce.dto.response.ReservationItemStatus;
import Ecommerce.dto.response.ReservationResponse;
import Ecommerce.exception.ResourceNotFoundException;
import Ecommerce.model.entity.Cart;
import Ecommerce.model.entity.CartItem;
import Ecommerce.model.entity.StockReservation;
import Ecommerce.model.enums.CartStatus;
import Ecommerce.model.enums.ReservationStatus;
import Ecommerce.repository.CartRepository;
import Ecommerce.repository.ProductVariantRepository;
import Ecommerce.repository.StockReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    private final CartRepository cartRepository;
    private final StockReservationRepository reservationRepository;
    private final ProductVariantRepository variantRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ReservationConfig reservationConfig;

    private static final String REDIS_RESERVATION_PREFIX = "reservation:";

    @Transactional
    public ReservationResponse createReservation(String cartToken) {
        Cart cart = cartRepository.findByCartToken(cartToken)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        if (cart.getStatus() != CartStatus.ACTIVE) {
            return ReservationResponse.builder()
                .success(false)
                .message("Cart is not active")
                .build();
        }

        if (cart.getItems().isEmpty()) {
            return ReservationResponse.builder()
                .success(false)
                .message("Cart is empty")
                .build();
        }

        List<ReservationItemStatus> itemStatuses = new ArrayList<>();
        boolean allReserved = true;

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(reservationConfig.getExpirationMinutes());

        for (CartItem item : cart.getItems()) {
            try {
                // Check stock availability
                var variant = variantRepository.findById(item.getSku().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product variant not found"));

                if (variant.getStockQuantity() < item.getQuantity()) {
                    itemStatuses.add(ReservationItemStatus.builder()
                        .skuId(item.getSku().getId())
                        .quantity(item.getQuantity())
                        .reserved(false)
                        .message("Insufficient stock. Available: " + variant.getStockQuantity())
                        .build());
                    allReserved = false;
                    continue;
                }

                // Create DB reservation
                StockReservation reservation = StockReservation.builder()
                    .sku(variant)
                    .cartToken(cartToken)
                    .quantity(item.getQuantity())
                    .status(ReservationStatus.RESERVED)
                    .expiresAt(expiresAt)
                    .build();

                reservationRepository.save(reservation);

                // Create Redis reservation
                String redisKey = REDIS_RESERVATION_PREFIX + cartToken + ":" + item.getSku().getId();
                redisTemplate.opsForValue().set(
                    redisKey,
                    reservation.getId(),
                    reservationConfig.getExpirationMinutes(),
                    TimeUnit.MINUTES
                );

                itemStatuses.add(ReservationItemStatus.builder()
                    .skuId(item.getSku().getId())
                    .quantity(item.getQuantity())
                    .reserved(true)
                    .message("Reserved successfully")
                    .build());

                log.info("Created reservation for cart: {}, sku: {}, quantity: {}",
                    cartToken, item.getSku().getId(), item.getQuantity());

            } catch (Exception e) {
                log.error("Failed to reserve item: {}", e.getMessage(), e);
                itemStatuses.add(ReservationItemStatus.builder()
                    .skuId(item.getSku().getId())
                    .quantity(item.getQuantity())
                    .reserved(false)
                    .message("Reservation failed: " + e.getMessage())
                    .build());
                allReserved = false;
            }
        }

        return ReservationResponse.builder()
            .success(allReserved)
            .message(allReserved ? "All items reserved successfully" : "Some items could not be reserved")
            .items(itemStatuses)
            .build();
    }

    @Transactional
    public void releaseReservation(String cartToken) {
        List<StockReservation> reservations = reservationRepository.findByCartTokenAndStatus(
            cartToken, ReservationStatus.RESERVED);

        for (StockReservation reservation : reservations) {
            reservation.setStatus(ReservationStatus.RELEASED);
            reservationRepository.save(reservation);

            // Remove from Redis
            String redisKey = REDIS_RESERVATION_PREFIX + cartToken + ":" + reservation.getSku().getId();
            redisTemplate.delete(redisKey);

            log.info("Released reservation for cart: {}, sku: {}",
                cartToken, reservation.getSku().getId());
        }
    }

    @Transactional
    public void consumeReservation(String cartToken, Long orderId) {
        List<StockReservation> reservations = reservationRepository.findByCartTokenAndStatus(
            cartToken, ReservationStatus.RESERVED);

        for (StockReservation reservation : reservations) {
            reservation.setStatus(ReservationStatus.CONSUMED);
            reservationRepository.save(reservation);

            // Remove from Redis
            String redisKey = REDIS_RESERVATION_PREFIX + cartToken + ":" + reservation.getSku().getId();
            redisTemplate.delete(redisKey);

            log.info("Consumed reservation for order: {}, sku: {}",
                orderId, reservation.getSku().getId());
        }
    }

    @Transactional
    public void cleanupExpiredReservations() {
        List<StockReservation> expired = reservationRepository.findByStatusAndExpiresAtBefore(
            ReservationStatus.RESERVED, LocalDateTime.now());

        for (StockReservation reservation : expired) {
            reservation.setStatus(ReservationStatus.RELEASED);
            reservationRepository.save(reservation);

            // Remove from Redis
            String redisKey = REDIS_RESERVATION_PREFIX + reservation.getCartToken() + ":" +
                reservation.getSku().getId();
            redisTemplate.delete(redisKey);

            log.info("Cleaned up expired reservation: {}", reservation.getId());
        }

        log.info("Cleaned up {} expired reservations", expired.size());
    }
}

