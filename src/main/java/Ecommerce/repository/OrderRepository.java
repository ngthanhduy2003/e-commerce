package Ecommerce.repository;

import Ecommerce.model.entity.Order;
import Ecommerce.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderCode(String orderCode);
    Optional<Order> findByTrackingToken(String trackingToken);
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    Page<Order> findByUserId(Long userId, Pageable pageable);
}

