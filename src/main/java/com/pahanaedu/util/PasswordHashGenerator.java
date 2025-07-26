package com.pahanaedu.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        String[] users = {
                "admin:admin123",  // Default admin credentials
                "johndoe:password123" // Temporary password for johndoe
        };

        for (String user : users) {
            String[] parts = user.split(":");
            String hash = BCrypt.hashpw(parts[1], BCrypt.gensalt(12));
            System.out.println("UPDATE system_users SET password = '" + hash +
                    "' WHERE username = '" + parts[0] + "';");
        }
    }
}