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
        sb.append("Thank you for your order!\n\n");
        sb.append("Order Code: ").append(order.getOrderCode()).append("\n");
        sb.append("Total Amount: ").append(order.getTotalMoney()).append(" ").append(order.getCurrency()).append("\n");
        sb.append("Payment Method: ").append(order.getPaymentMethod()).append("\n");
        sb.append("Shipping Address: ").append(order.getShippingAddress()).append("\n\n");
        sb.append("Track your order at: /tracking/").append(order.getTrackingToken()).append("\n\n");
        sb.append("Order Items:\n");

        order.getItems().forEach(item -> {
            sb.append("- ").append(item.getSku().getProduct().getName())
                .append(" (").append(item.getQuantity()).append("x)")
                .append(" - ").append(item.getPriceCheckout()).append("\n");
        });

        return sb.toString();
    }
}

