package com.example.vongtay.security;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {
    @GetMapping("/login") public String login() { return "auth/login"; }
    @GetMapping("/access-denied") public String denied() { return "auth/denied"; }
}
