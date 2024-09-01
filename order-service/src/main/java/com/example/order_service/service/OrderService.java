package com.example.order_service.service;

import com.example.order_service.model.Order;
import com.example.order_service.model.OrderStatus;
import com.example.order_service.model.OrderStatusUpdate;
import com.example.order_service.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String ORDER_TOPIC = "order-topic";

    @Autowired
    public OrderService(OrderRepository orderRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public List<Order> getAll(){
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(Long id){
        return orderRepository.findById(id);
    }

    @Transactional
    public Order createOrder(Order order) {
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);

        kafkaTemplate.send(ORDER_TOPIC, "Order Created: " + savedOrder.getId());
        return savedOrder;
    }

    @Transactional
    public void delete(Long id){
        orderRepository.deleteById(id);
        kafkaTemplate.send(ORDER_TOPIC, "Order Deleted: " + id);
    }

    @Transactional
    public Order updateOrderStatus(Long id, OrderStatusUpdate orderStatusUpdate) {
        OrderStatus newStatus = orderStatusUpdate.getNewStatus();
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());

        Order updatedOrder = orderRepository.save(order);

        kafkaTemplate.send(ORDER_TOPIC, "Order Status Updated: " + updatedOrder.getId() + " to " + newStatus);
        return updatedOrder;
    }
}
