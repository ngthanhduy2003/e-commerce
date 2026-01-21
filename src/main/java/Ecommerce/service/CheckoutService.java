package Ecommerce.service;

import Ecommerce.dto.request.CheckoutRequest;
import Ecommerce.dto.response.OrderItemResponse;
import Ecommerce.dto.response.OrderResponse;
import Ecommerce.exception.InvalidCartException;
import Ecommerce.exception.OutOfStockException;
import Ecommerce.exception.ResourceNotFoundException;
import Ecommerce.model.entity.*;
import Ecommerce.model.enums.CartStatus;
import Ecommerce.model.enums.OrderStatus;
import Ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckoutService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final ProductVariantRepository variantRepository;
    private final ReservationService reservationService;
    private final EmailService emailService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String IDEMPOTENCY_KEY_PREFIX = "checkout:idempotency:";

    @Transactional
    public OrderResponse checkout(CheckoutRequest request) {
        // Check idempotency
        String idempotencyKey = IDEMPOTENCY_KEY_PREFIX + request.getIdempotencyKey();
        String existingOrderCode = (String) redisTemplate.opsForValue().get(idempotencyKey);

        if (existingOrderCode != null) {
            log.info("Idempotent checkout detected, returning existing order: {}", existingOrderCode);
            Order existingOrder = orderRepository.findByOrderCode(existingOrderCode)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
            return toOrderResponse(existingOrder);
        }

        // Find cart
        Cart cart = cartRepository.findByCartToken(request.getCartToken())
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        if (cart.getStatus() != CartStatus.ACTIVE) {
            throw new InvalidCartException("Cart is not active");
        }

        if (cart.getItems().isEmpty()) {
            throw new InvalidCartException("Cart is empty");
        }

        // Atomic stock decrement for each item
        Map<Long, Integer> stockUpdates = new HashMap<>();

        for (CartItem item : cart.getItems()) {
            int affectedRows = variantRepository.decrementStock(
                item.getSku().getId(),
                item.getQuantity()
            );

            if (affectedRows == 0) {
                // Rollback: restore previously decremented stock
                for (Map.Entry<Long, Integer> entry : stockUpdates.entrySet()) {
                    variantRepository.incrementStock(entry.getKey(), entry.getValue());
                }

                throw new OutOfStockException(
                    "Insufficient stock for product variant: " + item.getSku().getId()
                );
            }

            stockUpdates.put(item.getSku().getId(), item.getQuantity());
        }

        // Calculate total
        BigDecimal totalMoney = cart.getItems().stream()
            .map(item -> item.getSku().getPrice().multiply(new BigDecimal(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Create order
        Order order = Order.builder()
            .user(cart.getUser())
            .status(OrderStatus.PENDING)
            .paymentMethod(request.getPaymentMethod())
            .totalMoney(totalMoney)
            .shippingAddress(request.getShippingAddress())
            .build();

        // Add order items
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = OrderItem.builder()
                .order(order)
                .sku(cartItem.getSku())
                .priceCheckout(cartItem.getSku().getPrice())
                .quantity(cartItem.getQuantity())
                .build();
            order.getItems().add(orderItem);
        }

        order = orderRepository.save(order);

        // Mark cart as checked out
        cart.setStatus(CartStatus.CHECKED_OUT);
        cartRepository.save(cart);

        // Consume reservations
        try {
            reservationService.consumeReservation(request.getCartToken(), order.getId());
        } catch (Exception e) {
            log.error("Failed to consume reservation, but order created: {}", e.getMessage());
        }

        // Store idempotency key
        redisTemplate.opsForValue().set(
            idempotencyKey,
            order.getOrderCode(),
            24,
            TimeUnit.HOURS
        );

        // Send confirmation email async
        try {
            emailService.sendOrderConfirmation(order);
        } catch (Exception e) {
            log.error("Failed to send order confirmation email: {}", e.getMessage());
        }

        log.info("Order created successfully: {}", order.getOrderCode());

        return toOrderResponse(order);
    }

    private OrderResponse toOrderResponse(Order order) {
        return OrderResponse.builder()
            .id(order.getId())
            .orderCode(order.getOrderCode())
            .status(order.getStatus().name())
            .paymentMethod(order.getPaymentMethod().name())
            .totalMoney(order.getTotalMoney())
            .currency(order.getCurrency())
            .shippingAddress(order.getShippingAddress())
            .trackingToken(order.getTrackingToken())
            .items(order.getItems().stream()
                .map(this::toOrderItemResponse)
                .collect(Collectors.toList()))
            .createdAt(order.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .paidAt(order.getPaidAt() != null ?
                order.getPaidAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null)
            .build();
    }

    private OrderItemResponse toOrderItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
            .id(item.getId())
            .skuId(item.getSku().getId())
            .productName(item.getSku().getProduct().getName())
            .size(item.getSku().getSize())
            .color(item.getSku().getColor())
            .priceCheckout(item.getPriceCheckout())
            .quantity(item.getQuantity())
            .subtotal(item.getPriceCheckout().multiply(new BigDecimal(item.getQuantity())))
            .build();
    }
}

