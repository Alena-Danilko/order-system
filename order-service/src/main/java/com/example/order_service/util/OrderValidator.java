package com.example.order_service.util;

import com.example.order_service.model.Order;
import com.example.order_service.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class OrderValidator implements Validator {
    private final OrderService orderService;

    @Autowired
    public OrderValidator(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return Order.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Order order = (Order) target;
        if (order.getId() != null) {
            errors.rejectValue("id", "", "A new order can't already have ID");
        }
        if (order.getUserId() == null) {
            errors.rejectValue("userId", "", "User ID can't be null");
        }
        if (order.getQuantity() <= 0) {
            errors.rejectValue("quantity", "", "Quantity must be greater than 0");
        }
        if (order.getDeliveryAddress() == null || order.getDeliveryAddress().isEmpty()) {
            errors.rejectValue("deliveryAddress", "", "Delivery address cannot be null or empty");
        }
    }
}
