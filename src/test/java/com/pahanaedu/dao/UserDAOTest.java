package com.pahanaedu.dao;

import com.pahanaedu.model.User;
import com.pahanaedu.util.DBConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.*;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class UserDAOTest {

    private UserDAO userDAO;
    private MockedStatic<DBConnection> mockedDBConnection;
    private MockedStatic<org.mindrot.jbcrypt.BCrypt> mockedBCrypt;

    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;

    @Before
    public void setUp() throws SQLException {
        // Create mocks
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        // Mock the static DBConnection.getConnection() method
        mockedDBConnection = Mockito.mockStatic(DBConnection.class);
        mockedDBConnection.when(DBConnection::getConnection).thenReturn(mockConnection);

        // Mock the static BCrypt methods
        mockedBCrypt = Mockito.mockStatic(org.mindrot.jbcrypt.BCrypt.class);

        // Default mock behaviors
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockConnection.prepareStatement(anyString(), anyInt())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);

        userDAO = new UserDAO();
    }

    @After
    public void tearDown() {
        if (mockedDBConnection != null) {
            mockedDBConnection.close();
        }
        if (mockedBCrypt != null) {
            mockedBCrypt.close();
        }
    }

    @Test
    public void testGetUserRole_ValidCredentials() throws SQLException {
        // Arrange
        String username = "testuser";
        String password = "password123";
        String expectedRole = "admin";
        String mockHash = "mock-bcrypt-hash";

        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("password")).thenReturn(mockHash);
        when(mockResultSet.getString("role")).thenReturn(expectedRole);

        // Mock BCrypt to return true for valid credentials
        mockedBCrypt.when(() -> org.mindrot.jbcrypt.BCrypt.checkpw(password, mockHash))
                .thenReturn(true);

        // Act
        String result = userDAO.getUserRole(username, password);

        // Assert
        assertEquals("Should return correct role", expectedRole, result);
        verify(mockPreparedStatement).setString(1, username);
    }

    @Test
    public void testGetUserRole_InvalidPassword() throws SQLException {
        // Arrange
        String username = "testuser";
        String password = "wrongpassword";
        String mockHash = "mock-bcrypt-hash";

        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("password")).thenReturn(mockHash);
        when(mockResultSet.getString("role")).thenReturn("admin");

        // Mock BCrypt to return false for invalid password
        mockedBCrypt.when(() -> org.mindrot.jbcrypt.BCrypt.checkpw(password, mockHash))
                .thenReturn(false);

        // Act
        String result = userDAO.getUserRole(username, password);

        // Assert
        assertNull("Should return null for invalid password", result);
    }

    @Test
    public void testGetUserRole_UserNotFound() throws SQLException {
        // Arrange
        String username = "nonexistent";
        String password = "password123";

        when(mockResultSet.next()).thenReturn(false);

        // Act
        String result = userDAO.getUserRole(username, password);

        // Assert
        assertNull("Should return null for non-existent user", result);
    }

    @Test
    public void testGetAllUsers() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getInt("id")).thenReturn(1, 2);
        when(mockResultSet.getString("full_name")).thenReturn("User One", "User Two");
        when(mockResultSet.getString("username")).thenReturn("user1", "user2");
        when(mockResultSet.getString("email")).thenReturn("user1@test.com", "user2@test.com");
        when(mockResultSet.getString("employee_no")).thenReturn("EMP001", "EMP002");
        when(mockResultSet.getString("role")).thenReturn("admin", "staff");
        when(mockResultSet.getBoolean("status")).thenReturn(true, true);

        // Act
        List<User> users = userDAO.getAllUsers();

        // Assert
        assertEquals("Should return 2 users", 2, users.size());
        assertEquals("First user should have correct name", "User One", users.get(0).getFullName());
        assertEquals("Second user should have correct role", "staff", users.get(1).getRole());
    }

    @Test
    public void testGetUserById_Found() throws SQLException {
        // Arrange
        int userId = 1;
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("id")).thenReturn(userId);
        when(mockResultSet.getString("full_name")).thenReturn("Test User");
        when(mockResultSet.getString("username")).thenReturn("testuser");
        when(mockResultSet.getString("email")).thenReturn("test@example.com");
        when(mockResultSet.getString("employee_no")).thenReturn("EMP001");
        when(mockResultSet.getString("role")).thenReturn("admin");
        when(mockResultSet.getBoolean("status")).thenReturn(true);

        // Act
        User user = userDAO.getUserById(userId);

        // Assert
        assertNotNull("User should not be null", user);
        assertEquals("User ID should match", userId, user.getId());
        assertEquals("Username should match", "testuser", user.getUsername());
        verify(mockPreparedStatement).setInt(1, userId);
    }

    @Test
    public void testGetUserById_NotFound() throws SQLException {
        // Arrange
        int userId = 999;
        when(mockResultSet.next()).thenReturn(false);

        // Act
        User user = userDAO.getUserById(userId);

        // Assert
        assertNull("User should be null when not found", user);
    }

    @Test
    public void testInsertUser_Success() throws SQLException {
        // Arrange
        User user = new User();
        user.setFullName("Test User");
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setEmployeeNo("EMP001");
        user.setPassword("password123");
        user.setRole("admin");
        user.setStatus(true);

        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(1);

        // Mock BCrypt.hashpw to return a mock hash
        String mockHash = "mock-hashed-password";
        mockedBCrypt.when(() -> org.mindrot.jbcrypt.BCrypt.hashpw(anyString(), anyString()))
                .thenReturn(mockHash);
        mockedBCrypt.when(() -> org.mindrot.jbcrypt.BCrypt.gensalt(anyInt()))
                .thenReturn("mock-salt");

        // Act
        boolean result = userDAO.insertUser(user);

        // Assert
        assertTrue("Insert should succeed", result);
        assertEquals("User ID should be set", 1, user.getId());
        verify(mockPreparedStatement).setString(1, user.getFullName());
        verify(mockPreparedStatement).setString(2, user.getUsername());
        verify(mockPreparedStatement).setString(3, user.getEmail());
        verify(mockPreparedStatement).setString(4, user.getEmployeeNo());
        verify(mockPreparedStatement).setString(5, mockHash);
        verify(mockPreparedStatement).setString(6, user.getRole());
        verify(mockPreparedStatement).setString(7, "active");
    }

    @Test
    public void testUpdateUser() throws SQLException {
        // Arrange
        User user = new User();
        user.setId(1);
        user.setFullName("Updated Name");
        user.setUsername("updateduser");
        user.setEmail("updated@example.com");
        user.setEmployeeNo("EMP001");
        user.setRole("staff");
        user.setStatus(false);

        // Act
        userDAO.updateUser(user);

        // Assert
        verify(mockPreparedStatement).setString(1, user.getFullName());
        verify(mockPreparedStatement).setString(2, user.getUsername());
        verify(mockPreparedStatement).setString(3, user.getEmail());
        verify(mockPreparedStatement).setString(4, user.getEmployeeNo());
        verify(mockPreparedStatement).setString(5, user.getRole());
        verify(mockPreparedStatement).setString(6, "inactive");
        verify(mockPreparedStatement).setInt(7, user.getId());
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    public void testDeleteUser_Success() throws SQLException {
        // Arrange
        int userId = 1;
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = userDAO.deleteUser(userId);

        // Assert
        assertTrue("Delete should succeed", result);
        verify(mockPreparedStatement).setInt(1, userId);
    }

    @Test
    public void testDeleteUser_Failure() throws SQLException {
        // Arrange
        int userId = 999;
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);

        // Act
        boolean result = userDAO.deleteUser(userId);

        // Assert
        assertFalse("Delete should fail", result);
    }

    @Test
    public void testGetUserByUsername_Found() throws SQLException {
        // Arrange
        String username = "testuser";
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("full_name")).thenReturn("Test User");
        when(mockResultSet.getString("username")).thenReturn(username);
        when(mockResultSet.getString("email")).thenReturn("test@example.com");
        when(mockResultSet.getString("employee_no")).thenReturn("EMP001");
        when(mockResultSet.getString("role")).thenReturn("admin");
        when(mockResultSet.getBoolean("status")).thenReturn(true);

        // Act
        User user = userDAO.getUserByUsername(username);

        // Assert
        assertNotNull("User should not be null", user);
        assertEquals("Username should match", username, user.getUsername());
        verify(mockPreparedStatement).setString(1, username);
    }

    @Test
    public void testUsernameExists_True() throws SQLException {
        // Arrange
        String username = "existinguser";
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(1);

        // Act
        boolean result = userDAO.usernameExists(username);

        // Assert
        assertTrue("Username should exist", result);
        verify(mockPreparedStatement).setString(1, username);
    }

    @Test
    public void testUsernameExists_False() throws SQLException {
        // Arrange
        String username = "nonexistent";
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(0);

        // Act
        boolean result = userDAO.usernameExists(username);

        // Assert
        assertFalse("Username should not exist", result);
    }

    @Test
    public void testEmailExists_True() throws SQLException {
        // Arrange
        String email = "existing@example.com";
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(1);

        // Act
        boolean result = userDAO.emailExists(email);

        // Assert
        assertTrue("Email should exist", result);
        verify(mockPreparedStatement).setString(1, email);
    }

    @Test
    public void testEmailExists_False() throws SQLException {
        // Arrange
        String email = "nonexistent@example.com";
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(0);

        // Act
        boolean result = userDAO.emailExists(email);

        // Assert
        assertFalse("Email should not exist", result);
    }

    @Test
    public void testSearchUsers() throws SQLException {
        // Arrange
        String keyword = "test";
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("full_name")).thenReturn("Test User");
        when(mockResultSet.getString("username")).thenReturn("testuser");
        when(mockResultSet.getString("email")).thenReturn("test@example.com");
        when(mockResultSet.getString("employee_no")).thenReturn("EMP001");
        when(mockResultSet.getString("role")).thenReturn("admin");
        when(mockResultSet.getBoolean("status")).thenReturn(true);

        // Act
        List<User> users = userDAO.searchUsers(keyword);

        // Assert
        assertEquals("Should return 1 user", 1, users.size());
        assertEquals("User should have correct username", "testuser", users.get(0).getUsername());
        // Verify all parameters are set correctly
        verify(mockPreparedStatement, times(5)).setString(anyInt(), eq("%test%"));
    }

    @Test
    public void testUpdateUserPassword() throws SQLException {
        // Arrange
        String username = "testuser";
        String newPassword = "newpassword123";
        String mockHash = "mock-hashed-password";

        // Mock BCrypt methods - use anyInt() to match any parameter
        mockedBCrypt.when(() -> org.mindrot.jbcrypt.BCrypt.hashpw(anyString(), anyString()))
                .thenReturn(mockHash);
        mockedBCrypt.when(() -> org.mindrot.jbcrypt.BCrypt.gensalt(anyInt()))
                .thenReturn("mock-salt");

        // Make sure executeUpdate returns 1 (success)
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = userDAO.updateUserPassword(username, newPassword);

        // Assert
        assertTrue("Password update should succeed", result);
        verify(mockPreparedStatement).setString(1, mockHash);
        verify(mockPreparedStatement).setString(2, username);
    }

    @Test
    public void testGetUserByCredentials_Valid() throws SQLException {
        // Arrange
        String username = "testuser";
        String password = "password123";
        String mockHash = "mock-bcrypt-hash";

        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("full_name")).thenReturn("Test User");
        when(mockResultSet.getString("username")).thenReturn(username);
        when(mockResultSet.getString("email")).thenReturn("test@example.com");
        when(mockResultSet.getString("employee_no")).thenReturn("EMP001");
        when(mockResultSet.getString("password")).thenReturn(mockHash);
        when(mockResultSet.getString("role")).thenReturn("admin");
        when(mockResultSet.getBoolean("status")).thenReturn(true);

        // Mock BCrypt to return true for valid credentials
        mockedBCrypt.when(() -> org.mindrot.jbcrypt.BCrypt.checkpw(password, mockHash))
                .thenReturn(true);

        // Act
        User user = userDAO.getUserByCredentials(username, password);

        // Assert
        assertNotNull("User should not be null", user);
        assertEquals("Username should match", username, user.getUsername());
    }

    @Test
    public void testGetUserByCredentials_Invalid() throws SQLException {
        // Arrange
        String username = "testuser";
        String password = "wrongpassword";
        String mockHash = "mock-bcrypt-hash";

        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("password")).thenReturn(mockHash);

        // Mock BCrypt to return false for invalid password
        mockedBCrypt.when(() -> org.mindrot.jbcrypt.BCrypt.checkpw(password, mockHash))
                .thenReturn(false);

        // Act
        User user = userDAO.getUserByCredentials(username, password);

        // Assert
        assertNull("User should be null for invalid credentials", user);
    }
}