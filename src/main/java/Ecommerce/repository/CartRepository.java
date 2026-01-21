package Ecommerce.repository;

import Ecommerce.model.entity.Cart;
import Ecommerce.model.enums.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByCartToken(String cartToken);
    List<Cart> findByStatusAndExpiresAtBefore(CartStatus status, LocalDateTime dateTime);
}

