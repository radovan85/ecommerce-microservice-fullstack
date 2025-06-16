package com.radovan.spring.controllers;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<String> healthCheck(HttpServletRequest request) {
        System.out.println("Health check called from: " + request.getRemoteAddr());
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }
}