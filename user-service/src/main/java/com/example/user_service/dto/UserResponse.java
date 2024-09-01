package com.example.user_service.dto;

import java.util.List;

public class UserResponse {
    private List<UserDTO> userDTOS;

    public UserResponse(List<UserDTO> userDTOS) {
        this.userDTOS = userDTOS;
    }

    public List<UserDTO> getUserDTOS() {
        return userDTOS;
    }

    public void setUserDTOS(List<UserDTO> userDTOS) {
        this.userDTOS = userDTOS;
    }
}
