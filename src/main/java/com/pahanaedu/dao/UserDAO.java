package com.pahanaedu.dao;

import com.pahanaedu.model.User;
import com.pahanaedu.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public String getUserRole(String username, String password) {
        String sql = "SELECT role FROM system_users WHERE username = ? AND password = ? AND status = 'active'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("role");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id as id, name as full_name, username, password, role, " +
                "CASE WHEN status = 'active' THEN true ELSE false END as status " +
                "FROM system_users";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setFullName(rs.getString("full_name"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));
                user.setStatus(rs.getBoolean("status"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public void insertUser(User user) {
        String sql = "INSERT INTO system_users (name, username, password, role, status) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getPassword());
            stmt.setString(4, user.getRole());
            stmt.setString(5, user.isStatus() ? "active" : "inactive");

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateUser(User user) {
        String sql = "UPDATE system_users SET name=?, username=?, password=?, role=?, status=? " +
                "WHERE user_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getPassword());
            stmt.setString(4, user.getRole());
            stmt.setString(5, user.isStatus() ? "active" : "inactive");
            stmt.setInt(6, user.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<User> searchUsers(String keyword) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id as id, name as full_name, username, password, role, " +
                "CASE WHEN status = 'active' THEN true ELSE false END as status " +
                "FROM system_users WHERE username LIKE ? OR name LIKE ? OR CAST(user_id AS CHAR) LIKE ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String search = "%" + keyword + "%";
            stmt.setString(1, search);
            stmt.setString(2, search);
            stmt.setString(3, search);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setFullName(rs.getString("full_name"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));
                user.setStatus(rs.getBoolean("status"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
}