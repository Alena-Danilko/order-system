package com.example.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class UserDTO {
    @NotNull
    private String fullName;

    @NotNull
    private int yearOfBirth;

    @NotNull
    @Email
    private String email;

//    private List<OrderDTO> orderDTOS;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getYearOfBirth() {
        return yearOfBirth;
    }

    public void setYearOfBirth(int yearOfBirth) {
        this.yearOfBirth = yearOfBirth;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
//
//    public List<OrderDTO> getOrderDTOS() {
//        return orderDTOS;
//    }
//
//    public void setOrderDTOS(List<OrderDTO> orderDTOS) {
//        this.orderDTOS = orderDTOS;
//    }
}
