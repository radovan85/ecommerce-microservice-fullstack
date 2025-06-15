package com.radovan.spring.interceptors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.radovan.spring.broker.CustomerNatsSender;
import com.radovan.spring.exceptions.SuspendedUserException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthInterceptor implements HandlerInterceptor {

	private final CustomerNatsSender customerNatsSender;

	@Autowired
	public AuthInterceptor(CustomerNatsSender customerNatsSender) {
		this.customerNatsSender = customerNatsSender;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			return true;
		}

		try {
			customerNatsSender.retrieveCurrentUser();
			return true;
		} catch (SuspendedUserException e) {
			// System.err.println("*** [ERROR] Suspendovan korisnik detektovan: " +
			// e.getMessage());
			// response.sendError(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS.value(), "Account
			// suspended");
			throw e;
			// return false;
		} catch (Exception e) {
			System.err.println("*** [ERROR] Greška pri proveri korisnika: " + e.getMessage());
			return true;
		}
	}
}