package com.radovan.spring.security;

import com.radovan.spring.utils.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@Order(1)
public class SecurityFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;

	@Autowired
	public SecurityFilter(JwtUtil jwtUtil) {
		this.jwtUtil = jwtUtil;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
	        throws ServletException, IOException {

	    if (isPublicRoute(request)) {
	        filterChain.doFilter(request, response);
	        return;
	    }

	    String authHeader = request.getHeader("Authorization");
	    String token = authHeader != null && authHeader.startsWith("Bearer ") ? authHeader.substring(7) : null;

	    if (token == null) {
	        sendUnauthorizedResponse(response, "Missing authorization token");
	        return;
	    }

	    try {
	        if (!jwtUtil.validateToken(token)) {
	            sendUnauthorizedResponse(response, "Invalid token");
	            return;
	        }

	        Optional<String> emailOpt = jwtUtil.extractUsername(token);
	        Optional<List<String>> rolesOpt = jwtUtil.extractRoles(token);

	        if (emailOpt.isEmpty()) {
	            sendUnauthorizedResponse(response, "Invalid user in token");
	            return;
	        }

	        String email = emailOpt.get();
	        List<GrantedAuthority> authorities = List.copyOf(
	        	    rolesOpt.orElse(Collections.emptyList()).stream()
	        	        .map(SimpleGrantedAuthority::new) // ✅ `SimpleGrantedAuthority` već implementira `GrantedAuthority`
	        	        .toList() 
	        	);


	        SecurityContextHolder.getContext().setAuthentication(
	                new UsernamePasswordAuthenticationToken(email, token, authorities)
	        );

	        filterChain.doFilter(request, response);
	    } catch (Exception e) {
	        sendUnauthorizedResponse(response, "Token processing error: " + e.getMessage());
	    }
	}


	private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
		response.setStatus(HttpStatus.FORBIDDEN.value());
		response.setContentType("application/json");
		response.getWriter().write(String.format("{\"error\":\"%s\",\"status\":403}", message));
	}

	private List<String> getUnsecuredRoutes() {
		return List.of("/api/health"); // for routes without security checking
	}

	private boolean isPublicRoute(HttpServletRequest request) {
		return getUnsecuredRoutes().contains(request.getRequestURI())
				|| "OPTIONS".equalsIgnoreCase(request.getMethod());
	}
}