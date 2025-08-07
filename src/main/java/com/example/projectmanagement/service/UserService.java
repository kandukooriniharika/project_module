package com.example.projectmanagement.service;

import com.example.projectmanagement.dto.UserDto;
import com.example.projectmanagement.entity.User;
import com.example.projectmanagement.entity.Role;
import com.example.projectmanagement.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ModelMapper modelMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public UserDto createUser(UserDto userDto) {
        // Check if user with email already exists
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new RuntimeException("User with email " + userDto.getEmail() + " already exists");
        }
        
        // Check if user with username already exists
        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new RuntimeException("User with username " + userDto.getUsername() + " already exists");
        }
        
        User user = new User();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setUsername(userDto.getUsername());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        
        // Convert role strings to Role enum
        Set<Role> roles = new HashSet<>();
        if (userDto.getRoles() != null) {
            roles = userDto.getRoles().stream()
                    .map(Role::valueOf)
                    .collect(Collectors.toSet());
        }
        user.setRoles(roles);
        
        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserDto.class);
    }
    
    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return modelMapper.map(user, UserDto.class);
    }
    
    @Transactional(readOnly = true)
    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return modelMapper.map(user, UserDto.class);
    }
    
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    @Transactional(readOnly = true)
    public UserDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        return modelMapper.map(user, UserDto.class);
    }
    
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<UserDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(user -> modelMapper.map(user, UserDto.class));
    }
    
    @Transactional(readOnly = true)
    public List<UserDto> getUsersByRole(Role role) {
        return userRepository.findByRole(role).stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<UserDto> getUsersByProject(Long projectId) {
        return userRepository.findByProjectId(projectId).stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .collect(Collectors.toList());
    }
    
    public UserDto updateUser(Long id, UserDto userDto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        // Check if email is being changed and if it already exists
        if (!existingUser.getEmail().equals(userDto.getEmail()) && 
            userRepository.existsByEmail(userDto.getEmail())) {
            throw new RuntimeException("User with email " + userDto.getEmail() + " already exists");
        }
        
        // Check if username is being changed and if it already exists
        if (!existingUser.getUsername().equals(userDto.getUsername()) && 
            userRepository.existsByUsername(userDto.getUsername())) {
            throw new RuntimeException("User with username " + userDto.getUsername() + " already exists");
        }
        
        existingUser.setName(userDto.getName());
        existingUser.setEmail(userDto.getEmail());
        existingUser.setUsername(userDto.getUsername());
        
        // Update password if provided
        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }
        
        // Convert role strings to Role enum
        if (userDto.getRoles() != null) {
            Set<Role> roles = userDto.getRoles().stream()
                    .map(Role::valueOf)
                    .collect(Collectors.toSet());
            existingUser.setRoles(roles);
        }
        
        User updatedUser = userRepository.save(existingUser);
        return modelMapper.map(updatedUser, UserDto.class);
    }
    
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
    
    @Transactional(readOnly = true)
    public Page<UserDto> searchUsers(String name, Role role, Pageable pageable) {
        if (name != null && role != null) {
            return userRepository.findByNameContaining(name, pageable)
                    .map(user -> modelMapper.map(user, UserDto.class));
        } else if (name != null) {
            return userRepository.findByNameContaining(name, pageable)
                    .map(user -> modelMapper.map(user, UserDto.class));
        } else if (role != null) {
            return userRepository.findByRolePaginated(role, pageable)
                    .map(user -> modelMapper.map(user, UserDto.class));
        } else {
            return userRepository.findAll(pageable)
                    .map(user -> modelMapper.map(user, UserDto.class));
        }
    }
}