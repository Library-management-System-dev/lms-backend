package com.krish.service;

import com.krish.exception.UserException;
import com.krish.payload.dto.UserDTO;
import com.krish.payload.response.AuthResponse;

public interface AuthService {

    AuthResponse login(String username, String password) throws UserException;
    AuthResponse signup(UserDTO req) throws UserException;

    void createPasswordResetToken(String email) throws UserException;
    void resetPassword(String token, String newPassword) throws UserException, Exception;



}
