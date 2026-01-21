package Ecommerce.controller;

import Ecommerce.dto.response.ApiResponse;
import Ecommerce.dto.response.OrderResponse;
import Ecommerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
public class TrackingController {

    private final OrderService orderService;

    @GetMapping("/{trackingToken}")
    public ResponseEntity<ApiResponse<OrderResponse>> trackOrder(@PathVariable String trackingToken) {
        OrderResponse order = orderService.getOrderByTrackingToken(trackingToken);
        return ResponseEntity.ok(ApiResponse.success(order));
    }
}

