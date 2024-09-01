package com.example.user_service.controller;

import com.example.user_service.dto.OrderDTO;
import com.example.user_service.dto.UserDTO;
import com.example.user_service.dto.UserResponse;
import com.example.user_service.model.Order;
import com.example.user_service.model.User;
import com.example.user_service.repository.UserRepository;
import com.example.user_service.service.UserService;
import com.example.user_service.util.ErrorResponse;
import com.example.user_service.util.UserException;
import com.example.user_service.util.UserValidator;
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
@RequestMapping("/users")
public class UserController {
    @Value("${spring.application.name}")
    private String applicationName;
    private final Logger log = LoggerFactory.getLogger(UserController.class);
    private static final String ENTITY_NAME = "user";
    private final UserService userService;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final UserValidator userValidator;

    @Autowired
    public UserController(UserService userService, UserRepository userRepository, ModelMapper modelMapper, UserValidator userValidator) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.userValidator = userValidator;
    }

    @GetMapping()
    public UserResponse getAllUsers (){
        log.debug("REST request to get all Users");
        return new UserResponse(userService.getAll().stream().map(this::convertToUserDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id){
        log.debug("REST request to get User with ID: {}", id);
        Optional<User> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping()
    public ResponseEntity<User> createUser(@Valid @RequestBody UserDTO userDTO,
                                           BindingResult bindingResult) throws URISyntaxException {
        User user = convertToUser(userDTO);
        log.debug("REST request to save User : {}", user);

        userValidator.validate(user, bindingResult);
        if(bindingResult.hasErrors()){
            handleException(bindingResult);
        }
        User createdUser = userRepository.save(user);
        userService.createUser(createdUser);

        HttpHeaders headers = new HttpHeaders();
        String message = String.format("A new "+ ENTITY_NAME +" is created with ID "+ createdUser.getId().toString());
        headers.add("X-" + applicationName + "-alert", message);
        headers.add("X-" + applicationName + "-params", createdUser.getId().toString());

        return ResponseEntity.created(new URI("/users" + createdUser.getId())).headers(headers).body(createdUser);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        log.debug("REST request to delete User with ID: {}", id);
        userService.deleteUser(id);
    }

    @PostMapping("/createOrder/{userId}")
    public ResponseEntity<Order> createOrder(@PathVariable Long userId, @Valid @RequestBody OrderDTO orderDTO,
                                             BindingResult bindingResult) {
        Order order = convertToOrder(orderDTO);
        log.debug("REST request to save Order : {} for User ID: {}", order, userId);
        User user = userRepository.findById(userId).orElse(null);
        userValidator.validate(user, bindingResult);
        if(bindingResult.hasErrors()){
            handleException(bindingResult);
        }
        userService.addOrder(order, user);
        return ResponseEntity.ok().body(order);
    }

    private UserDTO convertToUserDTO(User user) {
        return modelMapper.map(user, UserDTO.class);
    }

    private User convertToUser(UserDTO userDTO) {
        return modelMapper.map(userDTO, User.class);
    }

    private Order convertToOrder(OrderDTO orderDTO) {
        return modelMapper.map(orderDTO, Order.class);
    }

    @ExceptionHandler
    private ResponseEntity<ErrorResponse> handleResponse(UserException e){
        log.error("UserException occurred: {}", e.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    private void handleException(BindingResult bindingResult){
        StringBuilder errorMessage = new StringBuilder();
        List<FieldError> errors = bindingResult.getFieldErrors();
        for(FieldError error: errors){
            errorMessage.append(error.getField()).append(" - ").append(error.getDefaultMessage()).append(";");
        }
        throw new UserException(errorMessage.toString());
    }

}
