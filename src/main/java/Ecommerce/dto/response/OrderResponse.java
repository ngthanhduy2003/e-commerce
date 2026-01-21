package Ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private String orderCode;
    private String status;
    private String paymentMethod;
    private BigDecimal totalMoney;
    private String currency;
    private String shippingAddress;
    private String trackingToken;
    private List<OrderItemResponse> items;
    private String createdAt;
    private String paidAt;
}

