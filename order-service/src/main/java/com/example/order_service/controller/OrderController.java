package com.example.order_service.controller;

import com.example.order_service.dto.OrderDTO;
import com.example.order_service.dto.OrderResponse;
import com.example.order_service.model.Order;
import com.example.order_service.model.OrderStatusUpdate;
import com.example.order_service.repository.OrderRepository;
import com.example.order_service.service.OrderService;
import com.example.order_service.util.ErrorResponse;
import com.example.order_service.util.OrderException;
import com.example.order_service.util.OrderValidator;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/orders")
public class OrderController {
    @Value("${spring.application.name}")
    private String applicationName;
    private final Logger log = LoggerFactory.getLogger(OrderController.class);
    private static final String ENTITY_NAME = "order";

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final ModelMapper modelMapper;
    private final OrderValidator orderValidator;

    @Autowired
    public OrderController(OrderService orderService, OrderRepository orderRepository, ModelMapper modelMapper, OrderValidator orderValidator) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.modelMapper = modelMapper;
        this.orderValidator = orderValidator;
    }

    @GetMapping()
    public OrderResponse getAllOrders (){
        log.debug("REST request to get all Orders");
        return new OrderResponse(orderService.getAll().stream().map(this::convertToOrderDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id){
        log.debug("REST request to get Order with ID: {}", id);
        Optional<Order> order = orderService.getOrderById(id);
        return order.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping()
    public ResponseEntity<Order> createOrder(@Valid @RequestBody OrderDTO orderDTO,
                                             BindingResult bindingResult) throws URISyntaxException {
        Order order = convertToOrder(orderDTO);
        log.debug("REST request to save Order : {}", order);

        orderValidator.validate(order, bindingResult);
        if(bindingResult.hasErrors()){
            handleException(bindingResult);
        }
        Order createdOrder = orderRepository.save(order);
        orderService.createOrder(createdOrder);

        HttpHeaders headers = new HttpHeaders();
        String message = String.format("A new "+ ENTITY_NAME +" is created with ID "+ createdOrder.getId().toString());
        headers.add("X-" + applicationName + "-alert", message);
        headers.add("X-" + applicationName + "-params", createdOrder.getId().toString());

        return ResponseEntity.created(new URI("/orders" + createdOrder.getId())).headers(headers).body(createdOrder);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable Long id, @RequestBody OrderStatusUpdate statusUpdate) {
        log.debug("REST request to update Order Status to: {}", statusUpdate.toString());
        Order order = orderService.updateOrderStatus(id, statusUpdate);

        HttpHeaders headers = new HttpHeaders();
        String message = String.format("Status of order with ID was updated to "+ statusUpdate);
        headers.add("X-" + applicationName + "-alert", message);
        headers.add("X-" + applicationName + "-params", order.getId().toString());

        return new ResponseEntity<>(order, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteOrder(@PathVariable Long id){
        log.debug("REST request to delete Order with ID: {}", id);
        orderService.delete(id);
    }

    private OrderDTO convertToOrderDTO(Order order) {
        return modelMapper.map(order, OrderDTO.class);
    }

    private Order convertToOrder(OrderDTO orderDTO) {
        return modelMapper.map(orderDTO, Order.class);
    }

    @ExceptionHandler
    private ResponseEntity<ErrorResponse> handleResponse(OrderException e){
        log.error("OrderException occurred: {}", e.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    private void handleException(BindingResult bindingResult){
        StringBuilder errorMessage = new StringBuilder();
        List<FieldError> errors = bindingResult.getFieldErrors();
        for(FieldError error: errors){
            errorMessage.append(error.getField()).append(" - ").append(error.getDefaultMessage()).append(";");
        }
        throw new OrderException(errorMessage.toString());
    }

}
