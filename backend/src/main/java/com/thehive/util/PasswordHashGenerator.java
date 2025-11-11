package com.thehive.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Generate hashes for test passwords
        System.out.println("Password: password123");
        System.out.println("Hash: " + encoder.encode("password123"));
        System.out.println();
        
        System.out.println("Password: test123");
        System.out.println("Hash: " + encoder.encode("test123"));
        System.out.println();
        
        System.out.println("Password: admin123");
        System.out.println("Hash: " + encoder.encode("admin123"));
    }
}

