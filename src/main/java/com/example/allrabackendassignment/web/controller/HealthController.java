package com.example.allrabackendassignment.web.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController {

    public String healthCheck() {
        return "Healthy";
    }
}
