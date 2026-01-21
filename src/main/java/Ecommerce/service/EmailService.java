package Ecommerce.service;

import Ecommerce.model.entity.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendOrderConfirmation(Order order) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(order.getUser() != null ? order.getUser().getEmail() : "customer@example.com");
            message.setSubject("Order Confirmation - " + order.getOrderCode());
            message.setText(buildOrderConfirmationText(order));

            mailSender.send(message);
            log.info("Order confirmation email sent for order: {}", order.getOrderCode());
        } catch (Exception e) {
            log.error("Failed to send order confirmation email: {}", e.getMessage(), e);
        }
    }

    private String buildOrderConfirmationText(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append("====================================\n");
        sb.append("   ORDER CONFIRMATION\n");
        sb.append("====================================\n\n");

        sb.append("Thank you for your order!\n\n");

        sb.append("ORDER DETAILS:\n");
        sb.append("----------------------------------\n");
        sb.append("Order Code: ").append(order.getOrderCode()).append("\n");
        sb.append("Status: ").append(order.getStatus()).append("\n");
        sb.append("Total Amount: ").append(order.getTotalMoney()).append(" ").append(order.getCurrency()).append("\n");
        sb.append("Payment Method: ").append(order.getPaymentMethod()).append("\n");
        sb.append("Shipping Address: ").append(order.getShippingAddress()).append("\n\n");

        sb.append("ORDER ITEMS:\n");
        sb.append("----------------------------------\n");
        order.getItems().forEach(item -> {
            sb.append("• ").append(item.getSku().getProduct().getName());
            if (item.getSku().getSize() != null) {
                sb.append(" - Size: ").append(item.getSku().getSize());
            }
            if (item.getSku().getColor() != null && !item.getSku().getColor().isEmpty()) {
                sb.append(" - Color: ").append(item.getSku().getColor());
            }
            sb.append("\n");
            sb.append("  Quantity: ").append(item.getQuantity()).append("x");
            sb.append(" @ ").append(item.getPriceCheckout()).append(" ").append(order.getCurrency());
            sb.append(" = ").append(item.getPriceCheckout().multiply(
                java.math.BigDecimal.valueOf(item.getQuantity())
            )).append(" ").append(order.getCurrency()).append("\n\n");
        });

        sb.append("====================================\n");
        sb.append("   TRACK YOUR ORDER\n");
        sb.append("====================================\n\n");
        sb.append("Click the link below to track your order status:\n");
        sb.append("(No login required - just click and view)\n\n");

        // Full tracking URL
        String trackingUrl = "http://localhost:8080/api/tracking/" + order.getTrackingToken();
        sb.append("🔗 ").append(trackingUrl).append("\n\n");

        sb.append("Or use this tracking code:\n");
        sb.append("📦 ").append(order.getTrackingToken()).append("\n\n");

        sb.append("====================================\n");
        sb.append("Order created at: ").append(order.getCreatedAt()).append("\n");
        sb.append("====================================\n\n");

        sb.append("If you have any questions, please contact our support team.\n");
        sb.append("Thank you for shopping with us!\n");

        return sb.toString();
    }
}

