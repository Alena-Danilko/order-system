package com.example.user_service.util;

import com.example.user_service.model.User;
import com.example.user_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class UserValidator implements Validator {
    private final UserService userService;

    @Autowired
    public UserValidator(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return User.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        User user = (User) target;
        if (user.getId() == null) {
            errors.rejectValue("id", "", "There is no user");
        }
        if (user.getFullName() == null) {
            errors.rejectValue("fullName", "", "The field Full Name can't be null");
        }
        if (user.getYearOfBirth() >= 1900) {
            errors.rejectValue("quantity", "", "Year of birth must be greater than 1900");
        }
    }
}
