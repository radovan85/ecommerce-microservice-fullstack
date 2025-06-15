package com.radovan.spring.services;

import com.radovan.spring.dto.UserDto;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

public interface UserService {

	List<UserDto> listAll();

	UserDto getCurrentUser();

	UserDto getUserById(Integer userId);

	UserDto getUserByEmail(String email);

	Optional<Authentication> authenticateUser(String username, String password);

	Boolean isAdmin();

	Boolean isAdmin(Integer userId);

	void suspendUser(Integer userId);

	void reactivateUser(Integer userId);

	UserDto addUser(UserDto user);

	void deleteUser(Integer userId);

}