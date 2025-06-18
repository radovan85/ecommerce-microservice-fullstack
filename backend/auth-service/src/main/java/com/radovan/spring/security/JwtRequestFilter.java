package com.radovan.spring.security;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.radovan.spring.converter.TempConverter;
import com.radovan.spring.dto.UserDto;
import com.radovan.spring.services.UserService;
import com.radovan.spring.utils.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

	private UserService userService;
	private JwtUtil jwtUtil;
	private TempConverter tempConverter;

	@Autowired
	private void initialize(JwtUtil jwtUtil, TempConverter tempConverter, UserService userService) {
		this.jwtUtil = jwtUtil;
		this.tempConverter = tempConverter;
		this.userService = userService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String token = extractToken(request);
		if (token == null || !jwtUtil.validateToken(token)) {
			filterChain.doFilter(request, response);
			return;
		}

		String email = jwtUtil.extractUsername(token);
		if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			setAuthentication(email, token, request);
		}

		filterChain.doFilter(request, response);
	}

	private String extractToken(HttpServletRequest request) {
		return Optional.ofNullable(request.getHeader("Authorization")).filter(header -> header.startsWith("Bearer "))
				.map(header -> header.substring(7)).orElse(null);
	}

	private void setAuthentication(String email, String token, HttpServletRequest request) {
		UserDto userDto = userService.getUserByEmail(email);
		UserDetails userDetails = tempConverter.userDtoToEntity(userDto);

		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, token,
				userDetails.getAuthorities());
		authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}
}
