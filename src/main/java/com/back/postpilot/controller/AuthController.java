package com.back.postpilot.controller;

import com.back.postpilot.DTO.AuthRequestDTO;
import com.back.postpilot.DTO.AuthResponseDTO;
import com.back.postpilot.DTO.UserProfileDTO;
import com.back.postpilot.entity.UserEntity;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.back.postpilot.service.JWTService;
import com.back.postpilot.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequestDTO request) {
        log.debug(request.toString());
        try {
            // 1. Authenticate user with Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // 2. Get the user entity to extract role
            UserEntity user = userDetailsService.findByUsername(request.getUsername());

            // 3. Generate JWT with username and role
            String token = jwtService.generateToken(
                    user.getUsername(),
                    user.getRole().toString() // ✅ This will be "ADMIN", "USER", or "SUPER_ADMIN"
            );

            return ResponseEntity.ok(new AuthResponseDTO(token));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid username or password");
        }
    }

}