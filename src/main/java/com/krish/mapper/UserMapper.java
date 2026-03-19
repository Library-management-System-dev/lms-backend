package com.krish.mapper;

import com.krish.modal.User;
import com.krish.payload.dto.UserDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserMapper {
    public static UserDTO toDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setEmail(user.getEmail());
        userDTO.setFullName(user.getFullName());
        userDTO.setPhone(user.getPhone());
        userDTO.setLastLogin(user.getLastLogin());
        userDTO.setRole(user.getRole());


        return userDTO;
    }

    public static List<UserDTO> toDTOList(List<User> users){
        return users.stream()
                .map(UserMapper::toDTO)
                .collect(Collectors.toList());
    }

    public static Set<UserDTO> toDTOSet(Set<User> users){
        return users.stream()
                .map(UserMapper::toDTO)
                .collect(Collectors.toSet());
    }

    public static User toEntity(UserDTO userDTO) {
        User createduser = new User();
        createduser.setEmail(userDTO.getEmail());
        createduser.setPassword(userDTO.getPassword());
        createduser.setCreatedAt(LocalDateTime.now());
        createduser.setFullName(userDTO.getFullName());
        createduser.setPhone(userDTO.getPhone());
        createduser.setRole(userDTO.getRole());

        return createduser;
    }



}
