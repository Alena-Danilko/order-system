package com.example.order_service.dto;

import java.util.List;

public class OrderResponse {
    private List<OrderDTO> orderDTO;

    public OrderResponse(List<OrderDTO> orderDTO) {
        this.orderDTO = orderDTO;
    }

    public List<OrderDTO> getOrderDTO() {
        return orderDTO;
    }

    public void setOrderDTO(List<OrderDTO> orderDTO) {
        this.orderDTO = orderDTO;
    }
}
