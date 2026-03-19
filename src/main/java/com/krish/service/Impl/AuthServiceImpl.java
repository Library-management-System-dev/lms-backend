package com.krish.service.Impl;

import com.krish.configurations.JwtProvider;
import com.krish.domain.UserRole;
import com.krish.exception.UserException;
import com.krish.mapper.UserMapper;
import com.krish.modal.PasswordResetToken;
import com.krish.modal.User;
import com.krish.payload.dto.UserDTO;
import com.krish.payload.response.AuthResponse;
import com.krish.repository.PasswordResetTokenRepository;
import com.krish.repository.UserRepository;
import com.krish.service.AuthService;
import com.krish.service.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final CustomUserServiceImplementation customUserServiceImplementation;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    @Override
    public AuthResponse login(String username, String password) throws UserException {
        Authentication authentication = authenticate(username,password);

        SecurityContextHolder.getContext().setAuthentication(authentication);
//        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
//        String role = authorities.iterator().next().getAuthority();
        String token = jwtProvider.generateToken(authentication);

        User user = userRepository.findByEmail(username);

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        AuthResponse response = new AuthResponse();
        response.setJwt(token);
        response.setTitle("Login Success");
        response.setMessage("Welcome back "+username);
        response.setUser(UserMapper.toDTO(user));
        return response;
    }

    private Authentication authenticate(String username, String password) throws UserException {
        UserDetails userDetails = customUserServiceImplementation.loadUserByUsername(username);
        if(userDetails == null){
            throw new UserException("User not found with email - " + username);
        }
        if(!passwordEncoder.matches(password, userDetails.getPassword())){
            throw new UserException("password not match");
        }
        return new UsernamePasswordAuthenticationToken(username,null,userDetails.getAuthorities());
    }

    @Override
    public AuthResponse signup(UserDTO req) throws UserException {
        User user = userRepository.findByEmail(req.getEmail());

        if(user!=null){
            throw new UserException("email already registered");
        }
        User createdUser = new User();
        createdUser.setEmail(req.getEmail());
        createdUser.setPassword(passwordEncoder.encode(req.getPassword()));
        createdUser.setPhone(req.getPhone());
        createdUser.setFullName(req.getFullName());
        createdUser.setLastLogin(LocalDateTime.now());
        createdUser.setRole(UserRole.ROLE_USER);
        createdUser.setAuthProvider(com.krish.domain.AuthProvider.LOCAL);

        User savedUser = userRepository.save(createdUser);

        UserDetails userDetails = customUserServiceImplementation.loadUserByUsername(savedUser.getEmail());
        Authentication auth = new UsernamePasswordAuthenticationToken(
                savedUser.getEmail(), null, userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        String jwt = jwtProvider.generateToken(auth);

        AuthResponse response = new AuthResponse();
        response.setJwt(jwt);
        response.setTitle("Welcome "+createdUser.getFullName());
        response.setMessage("register success");
        response.setUser(UserMapper.toDTO(savedUser));

        return response;
    }

    @Transactional
    public void createPasswordResetToken(String email) throws UserException {

        String frontendUrl = "http://localhost:5173";
        User user = userRepository.findByEmail(email);
        if(user == null){
            throw new UserException("User not found with given email");
        }

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken= PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(5))
                .build();

        passwordResetTokenRepository.save(resetToken);
        String resetLink = frontendUrl + "/reset-password?token=" + token;
        String subject = "Password Reset Request";
        String body = "Click the following link to reset your password.Use this link (valid 5 minutes): "+resetLink;
        // sent email
        emailService.sendEmail(user.getEmail(), subject, body);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) throws Exception, UserException {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(
                        ()-> new UserException("token not valid")
                );
        if(resetToken.isExpired()){
            passwordResetTokenRepository.delete(resetToken);
            throw new Exception("token expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        passwordResetTokenRepository.delete(resetToken);

    }
}

