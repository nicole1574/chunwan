package com.chunwan.kg.controller;

import com.chunwan.kg.dto.LoginRequest;
import com.chunwan.kg.dto.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        UserDetails user = userDetailsService.loadUserByUsername(request.username());
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            return ResponseEntity.status(401).build();
        }
        String tokenValue = request.username() + ":" + request.password();
        String token = "Basic " + Base64.getEncoder().encodeToString(tokenValue.getBytes(StandardCharsets.UTF_8));
        String role = user.getAuthorities().stream().findFirst().map(a -> a.getAuthority()).orElse("ROLE_USER");
        return ResponseEntity.ok(new LoginResponse(token, role));
    }
}
