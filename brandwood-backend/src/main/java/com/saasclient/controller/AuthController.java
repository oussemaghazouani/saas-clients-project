package com.saasclient.controller;

import com.saasclient.dto.*;
import com.saasclient.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register/client")
    public ResponseEntity<ApiResponse> registerClient(@Valid @RequestBody RegisterClientRequest req) {
        return ResponseEntity.ok(authService.registerClient(req));
    }

    @PostMapping("/register/prestataire")
    public ResponseEntity<ApiResponse> registerPrestataire(@Valid @RequestBody RegisterPrestataireRequest req) {
        return ResponseEntity.ok(authService.registerPrestataire(req));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse> verifyEmail(@Valid @RequestBody VerifyCodeRequest req) {
        return ResponseEntity.ok(authService.verifyEmail(req));
    }

    @PostMapping("/resend-code")
    public ResponseEntity<ApiResponse> resendCode(@RequestParam String email) {
        return ResponseEntity.ok(authService.resendVerificationCode(email));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        return ResponseEntity.ok(authService.forgotPassword(req));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        return ResponseEntity.ok(authService.resetPassword(req));
    }
}
