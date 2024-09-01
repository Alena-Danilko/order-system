package com.example.user_service.service;

import com.example.user_service.model.Order;
import com.example.user_service.model.User;
import com.example.user_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String USER_TOPIC = "user-topic";

    @Autowired
    public UserService(UserRepository userRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.userRepository = userRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public List<User> getAll(){
        return userRepository.findAll();
    }
    public Optional<User> getUserById(Long id){
        return userRepository.findById(id);
    }

    public User createUser(User user){
        user.setCreatedAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);
        kafkaTemplate.send(USER_TOPIC, "User Created: " + savedUser.getId());
        return savedUser;
    }

    public void deleteUser(Long id){
        userRepository.deleteById(id);
        kafkaTemplate.send(USER_TOPIC, "User Deleted: " + id);
    }

    public User addOrder(Order order, User user){
        List<Order> orders = user.getOrders();
        orders.add(order);
        User updatedUser = userRepository.save(user);
        kafkaTemplate.send(USER_TOPIC, "Add Order to User with ID: " + updatedUser.getId());
        return updatedUser;
    }

}
