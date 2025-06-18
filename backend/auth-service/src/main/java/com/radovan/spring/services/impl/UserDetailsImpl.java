package com.radovan.spring.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import com.radovan.spring.converter.TempConverter;
import com.radovan.spring.dto.UserDto;
import com.radovan.spring.entity.UserEntity;
import com.radovan.spring.services.UserService;

@Service
public class UserDetailsImpl implements UserDetailsService {

    private UserService userService;
    private TempConverter tempConverter;

    @Autowired
    private void initialize(UserService userService, TempConverter tempConverter) {
        this.userService = userService;
        this.tempConverter = tempConverter;
    }

    @Override
    public UserEntity loadUserByUsername(String name) {

        UserEntity returnValue = null;
        UserDto userDto = userService.getUserByEmail(name);
        returnValue = tempConverter.userDtoToEntity(userDto);
        return returnValue;

    }
}