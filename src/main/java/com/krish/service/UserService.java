package com.krish.service;

import com.krish.modal.User;
import com.krish.payload.dto.UserDTO;

import java.util.List;

public interface UserService {

    public User getCurrentUser() throws Exception;
    public List<UserDTO> getAllUsers();
    User findById(Long id) throws Exception;
}
