package Ecommerce.service;

import Ecommerce.dto.request.AddCartItemRequest;
import Ecommerce.dto.request.UpdateCartItemRequest;
import Ecommerce.dto.response.CartItemResponse;
import Ecommerce.dto.response.CartResponse;
import Ecommerce.exception.InvalidCartException;
import Ecommerce.exception.ResourceNotFoundException;
import Ecommerce.model.entity.Cart;
import Ecommerce.model.entity.CartItem;
import Ecommerce.model.entity.ProductVariant;
import Ecommerce.model.enums.CartStatus;
import Ecommerce.repository.CartItemRepository;
import Ecommerce.repository.CartRepository;
import Ecommerce.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository productVariantRepository;

    @Transactional
    public CartResponse createCart() {
        Cart cart = Cart.builder()
            .status(CartStatus.ACTIVE)
            .expiresAt(LocalDateTime.now().plusMinutes(15))
            .build();

        cart = cartRepository.save(cart);
        return toCartResponse(cart);
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(String cartToken) {
        Cart cart = findCartByToken(cartToken);
        return toCartResponse(cart);
    }

    @Transactional
    public CartResponse addItem(String cartToken, AddCartItemRequest request) {
        Cart cart = findCartByToken(cartToken);

        if (cart.getStatus() != CartStatus.ACTIVE) {
            throw new InvalidCartException("Cart is not active");
        }

        ProductVariant sku = productVariantRepository.findById(request.getSkuId())
            .orElseThrow(() -> new ResourceNotFoundException("Product variant not found"));

        // Check if item already exists in cart
        CartItem existingItem = cartItemRepository.findByCartIdAndSkuId(cart.getId(), sku.getId())
            .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
            cartItemRepository.save(existingItem);
        } else {
            CartItem newItem = CartItem.builder()
                .cart(cart)
                .sku(sku)
                .quantity(request.getQuantity())
                .build();
            cart.getItems().add(newItem);
            cartItemRepository.save(newItem);
        }

        return toCartResponse(cart);
    }

    @Transactional
    public CartResponse updateItem(String cartToken, Long itemId, UpdateCartItemRequest request) {
        Cart cart = findCartByToken(cartToken);

        CartItem item = cart.getItems().stream()
            .filter(i -> i.getId().equals(itemId))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);

        return toCartResponse(cart);
    }

    @Transactional
    public CartResponse removeItem(String cartToken, Long itemId) {
        Cart cart = findCartByToken(cartToken);

        CartItem item = cart.getItems().stream()
            .filter(i -> i.getId().equals(itemId))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        cart.getItems().remove(item);
        cartItemRepository.delete(item);

        return toCartResponse(cart);
    }

    private Cart findCartByToken(String cartToken) {
        return cartRepository.findByCartToken(cartToken)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
    }

    private CartResponse toCartResponse(Cart cart) {
        BigDecimal totalAmount = cart.getItems().stream()
            .map(item -> item.getSku().getPrice().multiply(new BigDecimal(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
            .cartToken(cart.getCartToken())
            .status(cart.getStatus().name())
            .items(cart.getItems().stream()
                .map(this::toCartItemResponse)
                .collect(Collectors.toList()))
            .totalAmount(totalAmount)
            .expiresAt(cart.getExpiresAt() != null ?
                cart.getExpiresAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null)
            .build();
    }

    private CartItemResponse toCartItemResponse(CartItem item) {
        BigDecimal subtotal = item.getSku().getPrice().multiply(new BigDecimal(item.getQuantity()));

        return CartItemResponse.builder()
            .id(item.getId())
            .skuId(item.getSku().getId())
            .productName(item.getSku().getProduct().getName())
            .size(item.getSku().getSize())
            .color(item.getSku().getColor())
            .price(item.getSku().getPrice())
            .quantity(item.getQuantity())
            .subtotal(subtotal)
            .build();
    }
}

