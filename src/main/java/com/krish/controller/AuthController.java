package com.krish.controller;

import com.krish.exception.UserException;
import com.krish.payload.dto.UserDTO;
import com.krish.payload.request.ForgotPasswordRequest;
import com.krish.payload.request.LoginRequest;
import com.krish.payload.request.ResetPasswordRequest;
import com.krish.payload.response.ApiResponse;
import com.krish.payload.response.AuthResponse;
import com.krish.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signupHandler(
            @Valid @RequestBody UserDTO req
    ) throws UserException {
        AuthResponse res = authService.signup(req);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginHandler(
            @Valid @RequestBody LoginRequest req
    ) throws UserException {
        AuthResponse res = authService.login(req.getEmail(), req.getPassword());
        return ResponseEntity.ok(res);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) throws UserException {

        authService.createPasswordResetToken(request.getEmail());

        ApiResponse res = new ApiResponse(

                "A reset link has been sent to your email", true
        );
        return ResponseEntity.ok(res);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) throws UserException, Exception {
        authService.resetPassword(request.getToken(), request.getPassword());
        ApiResponse res = new ApiResponse(
                "Password reset successful", true
        );
        return ResponseEntity.ok(res);
    }



}
