package com.example.order_service.service;

import com.example.order_service.model.Order;
import com.example.order_service.model.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OrderListener {
    private final OrderService orderService;
    private final EmailSender emailSender;

    @Autowired
    public OrderListener(OrderService orderService, EmailSender emailSender) {
        this.orderService = orderService;
        this.emailSender = emailSender;
    }

    @KafkaListener(topics = "order-topic", groupId = "order-service-group")
    public void handleOrderEvents(String message) {
        if (message.startsWith("Order Created: ")) {
            Long orderId = Long.parseLong(message.substring(15));
            Optional<Order> optionalOrder = orderService.getOrderById(orderId);
            if (optionalOrder.isPresent()) {
                Order order = optionalOrder.get();
                sendNotification(order, orderId,"The order " + orderId + " has been successfully created");
            }
        } else if (message.startsWith("Order Deleted: ")) {
            Long orderId = Long.parseLong(message.substring(15));
            sendNotification(null, orderId,"The order " + orderId + " has been cancelled");
        } else if (message.startsWith("Order Status Updated: ")) {
            String[] parts = message.substring(22).split(" to ");
            Long orderId = Long.parseLong(parts[0]);
            OrderStatus newStatus = OrderStatus.valueOf(parts[1]);
            Optional<Order> optionalOrder = orderService.getOrderById(orderId);
                if (optionalOrder.isPresent()) {
                    Order order = optionalOrder.get();
                    sendNotification(order, orderId, "Order status " + orderId + " has been updated to " + newStatus);
                }
        }
    }

    private void sendNotification(Order order, Long orderId, String message) {
        String email = order.getEmail();
        String subject = "Information about order: "+ orderId;
        String body = message;
        emailSender.sendEmail(email, subject, body);
    }
}
