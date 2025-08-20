package com.pahanaedu.dao;

import com.pahanaedu.model.Category;
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

public class CategoryDAOTest {

    private CategoryDAO categoryDAO;
    private MockedStatic<DBConnection> mockedDBConnection;

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

        // Default mock behaviors
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        categoryDAO = new CategoryDAO();
    }

    @After
    public void tearDown() {
        if (mockedDBConnection != null) {
            mockedDBConnection.close();
        }
    }

    @Test
    public void testGetAllCategories() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(true, true, false);
        mockCategoryResultSet();

        // Act
        List<Category> categories = categoryDAO.getAllCategories();

        // Assert
        assertEquals("Should return 2 categories", 2, categories.size());
        assertEquals("First category should have correct ID", 1, categories.get(0).getCategoryId());
        assertEquals("First category should have correct name", "Electronics", categories.get(0).getCategoryName());
        assertEquals("Second category should have correct ID", 2, categories.get(1).getCategoryId());
        assertEquals("Second category should have correct name", "Books", categories.get(1).getCategoryName());
    }

    @Test
    public void testGetAllCategories_Empty() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(false);

        // Act
        List<Category> categories = categoryDAO.getAllCategories();

        // Assert
        assertTrue("Should return empty list", categories.isEmpty());
    }

    @Test
    public void testGetAllCategories_SQLException() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

        // Act
        List<Category> categories = categoryDAO.getAllCategories();

        // Assert
        assertTrue("Should return empty list on SQL exception", categories.isEmpty());
    }

    @Test
    public void testCategoryExists_True() throws SQLException {
        // Arrange
        String categoryName = "Electronics";
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(1);

        // Act
        boolean result = categoryDAO.categoryExists(categoryName);

        // Assert
        assertTrue("Category should exist", result);
        verify(mockPreparedStatement).setString(1, categoryName);
    }

    @Test
    public void testCategoryExists_False() throws SQLException {
        // Arrange
        String categoryName = "NonExistent";
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(0);

        // Act
        boolean result = categoryDAO.categoryExists(categoryName);

        // Assert
        assertFalse("Category should not exist", result);
    }

    @Test
    public void testCategoryExists_SQLException() throws SQLException {
        // Arrange
        String categoryName = "Electronics";
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

        // Act
        boolean result = categoryDAO.categoryExists(categoryName);

        // Assert
        assertFalse("Should return false on SQL exception", result);
    }

    @Test
    public void testAddCategory_Success() throws SQLException {
        // Arrange
        String categoryName = "New Category";
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        // Mock generated keys
        ResultSet mockGeneratedKeys = mock(ResultSet.class);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockGeneratedKeys.next()).thenReturn(true);
        when(mockGeneratedKeys.getInt(1)).thenReturn(5); // New category ID

        // Act
        int result = categoryDAO.addCategory(categoryName);

        // Assert
        assertEquals("Should return new category ID", 5, result);
        verify(mockPreparedStatement).setString(1, categoryName);
    }

    @Test
    public void testAddCategory_NoRowsAffected() throws SQLException {
        // Arrange
        String categoryName = "New Category";
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);

        // Act
        int result = categoryDAO.addCategory(categoryName);

        // Assert
        assertEquals("Should return -1 when no rows affected", -1, result);
    }

    @Test
    public void testAddCategory_NoGeneratedKeys() throws SQLException {
        // Arrange
        String categoryName = "New Category";
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        // Mock generated keys - no keys returned
        ResultSet mockGeneratedKeys = mock(ResultSet.class);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockGeneratedKeys.next()).thenReturn(false);

        // Act
        int result = categoryDAO.addCategory(categoryName);

        // Assert
        assertEquals("Should return -1 when no generated keys", -1, result);
    }

    @Test
    public void testAddCategory_SQLException() throws SQLException {
        // Arrange
        String categoryName = "New Category";
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenThrow(new SQLException("Database error"));

        // Act
        int result = categoryDAO.addCategory(categoryName);

        // Assert
        assertEquals("Should return -1 on SQL exception", -1, result);
    }

    @Test
    public void testDeleteCategory_Success() throws SQLException {
        // Arrange
        int categoryId = 1;

        // Act
        boolean result = categoryDAO.deleteCategory(categoryId);

        // Assert
        assertTrue("Delete should succeed", result);
        verify(mockPreparedStatement).setInt(1, categoryId);
    }

    @Test
    public void testDeleteCategory_Failure() throws SQLException {
        // Arrange
        int categoryId = 999;
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);

        // Act
        boolean result = categoryDAO.deleteCategory(categoryId);

        // Assert
        assertFalse("Delete should fail", result);
    }

    @Test
    public void testDeleteCategory_SQLException() throws SQLException {
        // Arrange
        int categoryId = 1;
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

        // Act
        boolean result = categoryDAO.deleteCategory(categoryId);

        // Assert
        assertFalse("Should return false on SQL exception", result);
    }

    @Test
    public void testMapResultSetToCategory() throws SQLException {
        // Arrange
        Timestamp createdDate = new Timestamp(System.currentTimeMillis());
        when(mockResultSet.getInt("category_id")).thenReturn(1);
        when(mockResultSet.getString("category_name")).thenReturn("Test Category");
        when(mockResultSet.getTimestamp("created_date")).thenReturn(createdDate);

        // Act - This tests the private method through getAllCategories
        when(mockResultSet.next()).thenReturn(true, false);
        List<Category> categories = categoryDAO.getAllCategories();

        // Assert
        assertEquals("Should return one category", 1, categories.size());
        Category category = categories.get(0);
        assertEquals("Category ID should match", 1, category.getCategoryId());
        assertEquals("Category name should match", "Test Category", category.getCategoryName());
        assertEquals("Created date should match", createdDate, category.getCreatedDate());
    }

    // Helper method to mock ResultSet behavior for categories
    private void mockCategoryResultSet() throws SQLException {
        when(mockResultSet.getInt("category_id")).thenReturn(1, 2);
        when(mockResultSet.getString("category_name")).thenReturn("Electronics", "Books");
        when(mockResultSet.getTimestamp("created_date")).thenReturn(
                new Timestamp(System.currentTimeMillis()),
                new Timestamp(System.currentTimeMillis() - 86400000)
        );
    }
}