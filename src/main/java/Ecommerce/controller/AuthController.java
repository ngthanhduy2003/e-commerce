package Ecommerce.controller;

import Ecommerce.dto.request.LoginRequest;
import Ecommerce.dto.request.RegisterRequest;
import Ecommerce.dto.response.ApiResponse;
import Ecommerce.dto.response.AuthResponse;
import Ecommerce.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received for email: {}", request.getEmail());
        AuthResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for email: {}", request.getEmail());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error("Refresh token is required"));
        }

        log.info("Token refresh request received");
        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateToken() {
        // If we reach here, the JWT filter has already validated the token
        // and set the authentication in the security context
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> data = Map.of(
            "valid", true,
            "username", authentication.getName(),
            "authorities", authentication.getAuthorities()
        );

        return ResponseEntity.ok(ApiResponse.success("Token is valid", data));
    }
}

