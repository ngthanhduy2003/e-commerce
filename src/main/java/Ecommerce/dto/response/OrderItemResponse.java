package Ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    private Long id;
    private Long skuId;
    private String productName;
    private Integer size;
    private String color;
    private BigDecimal priceCheckout;
    private Integer quantity;
    private BigDecimal subtotal;
}

