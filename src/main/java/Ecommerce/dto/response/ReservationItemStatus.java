package Ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationItemStatus {
    private Long skuId;
    private Integer quantity;
    private boolean reserved;
    private String message;
}

