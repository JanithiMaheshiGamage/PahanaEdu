package com.pahanaedu.dao;

import com.pahanaedu.model.User;
import com.pahanaedu.util.DBConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserDAO {

    private static final Logger logger = Logger.getLogger(UserDAO.class.getName());
    private static final int BCRYPT_WORKLOAD = 12;

    public String getUserRole(String username, String password) {
        String sql = "SELECT role, password FROM system_users WHERE username = ? AND status = 'active'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");
                if (BCrypt.checkpw(password, storedHash)) {
                    return rs.getString("role");
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting user role", e);
        }
        return null;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id as id, name as full_name, username, email, employee_no, role, " +
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
                user.setEmail(rs.getString("email"));
                user.setEmployeeNo(rs.getString("employee_no"));
                user.setRole(rs.getString("role"));
                user.setStatus(rs.getBoolean("status"));
                users.add(user);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting all users", e);
        }
        return users;
    }

    public User getUserById(int id) {
        String sql = "SELECT user_id as id, name as full_name, username, email, employee_no, role," +
                "CASE WHEN status = 'active' THEN true ELSE false END as status " +
                "FROM system_users WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setFullName(rs.getString("full_name"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setEmployeeNo(rs.getString("employee_no"));
                user.setRole(rs.getString("role"));
                user.setStatus(rs.getBoolean("status"));
                return user;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting user by ID", e);
        }
        return null;
    }

    public User getUserByEmail(String email) {
        String sql = "SELECT user_id as id, name as full_name, username, email, employee_no, role, " +
                "CASE WHEN status = 'active' THEN true ELSE false END as status " +
                "FROM system_users WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setFullName(rs.getString("full_name"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setEmployeeNo(rs.getString("employee_no"));
                user.setRole(rs.getString("role"));
                user.setStatus(rs.getBoolean("status"));
                return user;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting user by email", e);
        }
        return null;
    }

    public boolean insertUser(User user) {
        String sql = "INSERT INTO system_users (name, username, email, employee_no, password, role, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Hash the password before storing
            String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(BCRYPT_WORKLOAD));

            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getEmployeeNo());
            stmt.setString(5, hashedPassword);
            stmt.setString(6, user.getRole());
            stmt.setString(7, user.isStatus() ? "active" : "inactive");
             // Add employee number

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                return false;
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                    return true;
                }
            }
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error inserting user: " + user.toString(), e);
            return false;
        }
    }

    public void updateUser(User user) {
        String sql = "UPDATE system_users SET name=?, username=?, email=?, employee_no=?, role=?, status=? " +
                "WHERE user_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getEmployeeNo());
            stmt.setString(5, user.getRole());
            stmt.setString(6, user.isStatus() ? "active" : "inactive");
            stmt.setInt(7, user.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM system_users WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            int affectedRows = stmt.executeUpdate();
            logger.info("Deleted " + affectedRows + " rows for user ID: " + userId);
            return affectedRows > 0;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting user with ID: " + userId, e);
            return false;
        }
    }

    public void updateUserPassword(int userId, String newPassword) {
        String sql = "UPDATE system_users SET password=? WHERE user_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt(BCRYPT_WORKLOAD));
            stmt.setString(1, hashedPassword);
            stmt.setInt(2, userId);

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public User getUserByUsername(String username) {
        String sql = "SELECT user_id as id, name as full_name, username, email, employee_no, role, " +
                "CASE WHEN status = 'active' THEN true ELSE false END as status " +
                "FROM system_users WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setFullName(rs.getString("full_name"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setEmployeeNo(rs.getString("employee_no"));
                user.setRole(rs.getString("role"));
                user.setStatus(rs.getBoolean("status"));
                return user;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting user by username", e);
        }
        return null;
    }

    public User getUserByCredentials(String username, String password) {
        String sql = "SELECT user_id as id, name as full_name, username, email, employee_no, password, role, " +
                "CASE WHEN status = 'active' THEN true ELSE false END as status " +
                "FROM system_users WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");
                if (password != null && storedHash != null && BCrypt.checkpw(password, storedHash)) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setFullName(rs.getString("full_name"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    user.setEmployeeNo(rs.getString("employee_no"));
                    user.setRole(rs.getString("role"));
                    user.setStatus(rs.getBoolean("status"));
                    return user;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting user credentials", e);
        }
        return null;
    }

    public boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM system_users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM system_users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean userExists(String username) {
        String sql = "SELECT COUNT(*) FROM system_users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateUserPassword(String username, String newPassword) {
        String sql = "UPDATE system_users SET password = ? WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
            stmt.setString(1, hashedPassword);
            stmt.setString(2, username);

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<User> searchUsers(String keyword) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id as id, name as full_name, username, email, employee_no, role, " +
                "CASE WHEN status = 'active' THEN true ELSE false END as status " +
                "FROM system_users WHERE username LIKE ? OR name LIKE ? OR email LIKE ? " +
                "OR employee_no LIKE ? OR CAST(user_id AS CHAR) LIKE ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String search = "%" + keyword + "%";
            stmt.setString(1, search);
            stmt.setString(2, search);
            stmt.setString(3, search);
            stmt.setString(4, search);
            stmt.setString(5, search);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setFullName(rs.getString("full_name"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setEmployeeNo(rs.getString("employee_no"));
                user.setRole(rs.getString("role"));
                user.setStatus(rs.getBoolean("status"));
                users.add(user);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error searching users", e);
        }
        return users;
    }
}