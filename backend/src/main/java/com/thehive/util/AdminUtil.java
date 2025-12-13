package com.thehive.util;

import com.thehive.model.entity.User;
import com.thehive.model.enums.UserRole;
import com.thehive.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AdminUtil {
    
    public static boolean isAdmin(UserRepository userRepository) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return false;
        }
        
        Integer userId = (Integer) authentication.getPrincipal();
        User user = userRepository.findById(userId).orElse(null);
        return user != null && user.getRole() == UserRole.ADMIN;
    }
    
    public static void requireAdmin(UserRepository userRepository) {
        if (!isAdmin(userRepository)) {
            throw new RuntimeException("Admin access required");
        }
    }
}

