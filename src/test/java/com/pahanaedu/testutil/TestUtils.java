package com.pahanaedu.testutil;

import com.pahanaedu.model.User;

public class TestUtils {

    public static User createTestUser() {
        User user = new User();
        user.setId(1);
        user.setFullName("Test User");
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setEmployeeNo("EMP001");
        user.setPassword("password123");
        user.setRole("admin");
        user.setStatus(true);
        return user;
    }

    public static User createTestUser(int id, String username, String role) {
        User user = new User();
        user.setId(id);
        user.setFullName("Test User " + id);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setEmployeeNo("EMP" + String.format("%03d", id));
        user.setPassword("password123");
        user.setRole(role);
        user.setStatus(true);
        return user;
    }
}