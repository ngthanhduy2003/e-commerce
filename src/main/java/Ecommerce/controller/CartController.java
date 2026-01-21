package Ecommerce.controller;

import Ecommerce.dto.request.AddCartItemRequest;
import Ecommerce.dto.request.UpdateCartItemRequest;
import Ecommerce.dto.response.ApiResponse;
import Ecommerce.dto.response.CartResponse;
import Ecommerce.dto.response.ReservationResponse;
import Ecommerce.service.CartService;
import Ecommerce.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ApiResponse<CartResponse>> createCart() {
        CartResponse cart = cartService.createCart();
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("Cart created successfully", cart));
    }

    @GetMapping("/{cartToken}")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(@PathVariable String cartToken) {
        CartResponse cart = cartService.getCart(cartToken);
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    @PostMapping("/{cartToken}/items")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @PathVariable String cartToken,
            @Valid @RequestBody AddCartItemRequest request) {
        CartResponse cart = cartService.addItem(cartToken, request);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart", cart));
    }

    @PatchMapping("/{cartToken}/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(
            @PathVariable String cartToken,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        CartResponse cart = cartService.updateItem(cartToken, itemId, request);
        return ResponseEntity.ok(ApiResponse.success("Item updated", cart));
    }

    @DeleteMapping("/{cartToken}/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @PathVariable String cartToken,
            @PathVariable Long itemId) {
        CartResponse cart = cartService.removeItem(cartToken, itemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart", cart));
    }

    @PostMapping("/{cartToken}/reserve")
    public ResponseEntity<ApiResponse<ReservationResponse>> reserveCart(@PathVariable String cartToken) {
        ReservationResponse response = reservationService.createReservation(cartToken);

        if (response.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success("Reservation created", response));
        } else {
            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.<ReservationResponse>builder()
                    .success(false)
                    .message("Reservation failed")
                    .data(response)
                    .build());
        }
    }

    @PostMapping("/{cartToken}/release")
    public ResponseEntity<ApiResponse<Void>> releaseReservation(@PathVariable String cartToken) {
        reservationService.releaseReservation(cartToken);
        return ResponseEntity.ok(ApiResponse.success("Reservation released", null));
    }
}

