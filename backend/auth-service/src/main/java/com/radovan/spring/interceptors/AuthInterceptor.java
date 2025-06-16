package com.radovan.spring.interceptors;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.radovan.spring.dto.UserDto;
import com.radovan.spring.exceptions.SuspendedUserException;
import com.radovan.spring.services.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthInterceptor implements HandlerInterceptor {

	@Autowired
	private UserService userService;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		UserDto authUser = new UserDto();
		authUser.setEnabled((short) 1);

		try {
			Optional<UserDto> authUserOpt = Optional.ofNullable(userService.getCurrentUser());
			if (authUserOpt.isPresent()) {
				authUser = authUserOpt.get();
			}
		} catch (Exception exc) {

		}

		if (authUser.getEnabled() == 0) {
			Error error = new Error("Account suspended");
			throw new SuspendedUserException(error);
		}

		return true;
	}

}