package Ecommerce.service;

import Ecommerce.dto.request.UpdateOrderStatusRequest;
import Ecommerce.dto.response.OrderItemResponse;
import Ecommerce.dto.response.OrderResponse;
import Ecommerce.exception.ResourceNotFoundException;
import Ecommerce.model.entity.Order;
import Ecommerce.model.entity.OrderItem;
import Ecommerce.model.enums.OrderStatus;
import Ecommerce.repository.OrderRepository;
import Ecommerce.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductVariantRepository variantRepository;

    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return toOrderResponse(order);
    }

    public OrderResponse getOrderByCode(String orderCode) {
        Order order = orderRepository.findByOrderCode(orderCode)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return toOrderResponse(order);
    }

    public OrderResponse getOrderByTrackingToken(String trackingToken) {
        Order order = orderRepository.findByTrackingToken(trackingToken)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return toOrderResponse(order);
    }

    public Page<OrderResponse> getOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
            .map(this::toOrderResponse);
    }

    public Page<OrderResponse> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable)
            .map(this::toOrderResponse);
    }

    public Page<OrderResponse> getOrdersByUserId(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable)
            .map(this::toOrderResponse);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        OrderStatus oldStatus = order.getStatus();
        OrderStatus newStatus = request.getStatus();

        // Handle stock rollback when cancelling
        if (newStatus == OrderStatus.CANCELLED && oldStatus != OrderStatus.CANCELLED) {
            for (OrderItem item : order.getItems()) {
                variantRepository.incrementStock(item.getSku().getId(), item.getQuantity());
                log.info("Rolled back stock for SKU: {}, quantity: {}",
                    item.getSku().getId(), item.getQuantity());
            }
        }

        // Mark as paid
        if (newStatus == OrderStatus.PAID && order.getPaidAt() == null) {
            order.setPaidAt(LocalDateTime.now());
        }

        order.setStatus(newStatus);
        order = orderRepository.save(order);

        log.info("Order {} status updated from {} to {}",
            order.getOrderCode(), oldStatus, newStatus);

        return toOrderResponse(order);
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
        request.setStatus(OrderStatus.CANCELLED);
        updateOrderStatus(orderId, request);
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
