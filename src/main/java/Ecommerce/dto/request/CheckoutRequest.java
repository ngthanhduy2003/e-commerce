package Ecommerce.dto.request;

import Ecommerce.model.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {

    @NotBlank(message = "Cart token is required")
    private String cartToken;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    private String paymentDetails;

    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;
}

