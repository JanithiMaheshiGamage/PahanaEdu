package com.pahanaedu.dao;

import com.pahanaedu.model.Item;
import com.pahanaedu.util.DBConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.*;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ItemDAOTest {

    private ItemDAO itemDAO;
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
        when(mockConnection.prepareStatement(anyString(), anyInt())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);

        itemDAO = new ItemDAO();
    }

    @After
    public void tearDown() {
        if (mockedDBConnection != null) {
            mockedDBConnection.close();
        }
    }

    @Test
    public void testGetAllItems() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(true, true, false);
        mockItemResultSet();

        // Act
        List<Item> items = itemDAO.getAllItems();

        // Assert
        assertEquals("Should return 2 items", 2, items.size());
        assertEquals("First item should have correct name", "Item One", items.get(0).getName());
        assertEquals("Second item should have correct category", "Electronics", items.get(1).getCategoryName());
    }

    @Test
    public void testGetAllItems_Empty() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(false);

        // Act
        List<Item> items = itemDAO.getAllItems();

        // Assert
        assertTrue("Should return empty list", items.isEmpty());
    }

    @Test
    public void testAddItem_Success() throws SQLException {
        // Arrange
        Item item = new Item();
        item.setName("Test Item");
        item.setCategoryId(1);
        item.setPrice(99.99);
        item.setStockQty(10);
        item.setDescription("Test description");
        item.setCreatedBy("admin");

        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(123);

        // Act
        boolean result = itemDAO.addItem(item);

        // Assert
        assertTrue("Add item should succeed", result);
        assertEquals("Item ID should be set", 123, item.getItemId());
        verify(mockPreparedStatement).setString(1, "Test Item");
        verify(mockPreparedStatement).setInt(2, 1);
        verify(mockPreparedStatement).setDouble(3, 99.99);
        verify(mockPreparedStatement).setInt(4, 10);
        verify(mockPreparedStatement).setString(5, "Test description");
        verify(mockPreparedStatement).setString(6, "admin");
    }

    @Test
    public void testAddItem_Failure() throws SQLException {
        // Arrange
        Item item = new Item();
        item.setName("Test Item");
        item.setCategoryId(1);
        item.setPrice(99.99);
        item.setStockQty(10);
        item.setDescription("Test description");
        item.setCreatedBy("admin");

        when(mockPreparedStatement.executeUpdate()).thenReturn(0);

        // Act
        boolean result = itemDAO.addItem(item);

        // Assert
        assertFalse("Add item should fail", result);
    }

    @Test
    public void testAddItem_NullDescription() throws SQLException {
        // Arrange
        Item item = new Item();
        item.setName("Test Item");
        item.setCategoryId(1);
        item.setPrice(99.99);
        item.setStockQty(10);
        item.setDescription(null);
        item.setCreatedBy("admin");

        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(123);

        // Act
        boolean result = itemDAO.addItem(item);

        // Assert
        assertTrue("Add item should succeed", result);
        verify(mockPreparedStatement).setString(5, "");
    }

    @Test
    public void testUpdateItem_Success() throws SQLException {
        // Arrange
        Item item = new Item();
        item.setItemId(1);
        item.setName("Updated Item");
        item.setCategoryId(2);
        item.setPrice(149.99);
        item.setStockQty(20);
        item.setDescription("Updated description");

        // Act
        boolean result = itemDAO.updateItem(item);

        // Assert
        assertTrue("Update item should succeed", result);
        verify(mockPreparedStatement).setString(1, "Updated Item");
        verify(mockPreparedStatement).setInt(2, 2);
        verify(mockPreparedStatement).setDouble(3, 149.99);
        verify(mockPreparedStatement).setInt(4, 20);
        verify(mockPreparedStatement).setString(5, "Updated description");
        verify(mockPreparedStatement).setInt(6, 1);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    public void testUpdateItem_Failure() throws SQLException {
        // Arrange
        Item item = new Item();
        item.setItemId(1);
        item.setName("Updated Item");
        item.setCategoryId(2);
        item.setPrice(149.99);
        item.setStockQty(20);
        item.setDescription("Updated description");

        when(mockPreparedStatement.executeUpdate()).thenReturn(0);

        // Act
        boolean result = itemDAO.updateItem(item);

        // Assert
        assertFalse("Update item should fail", result);
    }

    @Test
    public void testDeleteItem_Success() throws SQLException {
        // Arrange
        int itemId = 1;
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = itemDAO.deleteItem(itemId);

        // Assert
        assertTrue("Delete item should succeed", result);
        verify(mockPreparedStatement).setInt(1, itemId);
    }

    @Test
    public void testDeleteItem_Failure() throws SQLException {
        // Arrange
        int itemId = 999;
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);

        // Act
        boolean result = itemDAO.deleteItem(itemId);

        // Assert
        assertFalse("Delete item should fail", result);
    }

    @Test
    public void testCountItemsInCategory() throws SQLException {
        // Arrange
        int categoryId = 1;
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(5);

        // Act
        int count = itemDAO.countItemsInCategory(categoryId);

        // Assert
        assertEquals("Should return correct count", 5, count);
        verify(mockPreparedStatement).setInt(1, categoryId);
    }

    @Test
    public void testCountItemsInCategory_Zero() throws SQLException {
        // Arrange
        int categoryId = 999;
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(0);

        // Act
        int count = itemDAO.countItemsInCategory(categoryId);

        // Assert
        assertEquals("Should return zero", 0, count);
    }

    @Test
    public void testSearchItems() throws SQLException {
        // Arrange
        String keyword = "test";
        String searchParam = "%test%";

        when(mockResultSet.next()).thenReturn(true, true, false);
        mockItemResultSet();

        // Act
        List<Item> items = itemDAO.searchItems(keyword);

        // Assert
        assertEquals("Should return 2 items", 2, items.size());
        verify(mockPreparedStatement).setString(1, searchParam);
        verify(mockPreparedStatement).setString(2, searchParam);
        verify(mockPreparedStatement).setString(3, searchParam);
    }

    @Test
    public void testSearchItems_NoResults() throws SQLException {
        // Arrange
        String keyword = "nonexistent";

        when(mockResultSet.next()).thenReturn(false);

        // Act
        List<Item> items = itemDAO.searchItems(keyword);

        // Assert
        assertTrue("Should return empty list", items.isEmpty());
    }

    @Test
    public void testItemNameExists_True() throws SQLException {
        // Arrange
        String itemName = "Existing Item";
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(1);

        // Act
        boolean result = itemDAO.itemNameExists(itemName);

        // Assert
        assertTrue("Item name should exist", result);
        verify(mockPreparedStatement).setString(1, itemName);
    }

    @Test
    public void testItemNameExists_False() throws SQLException {
        // Arrange
        String itemName = "Nonexistent Item";
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(0);

        // Act
        boolean result = itemDAO.itemNameExists(itemName);

        // Assert
        assertFalse("Item name should not exist", result);
    }

    @Test
    public void testGetItemById_Found() throws SQLException {
        // Arrange
        int itemId = 1;
        when(mockResultSet.next()).thenReturn(true);
        mockItemResultSet();

        // Act
        Item item = itemDAO.getItemById(itemId);

        // Assert
        assertNotNull("Item should not be null", item);
        assertEquals("Item ID should match", 1, item.getItemId());
        assertEquals("Item name should match", "Item One", item.getName());
        verify(mockPreparedStatement).setInt(1, itemId);
    }

    @Test
    public void testGetItemById_NotFound() throws SQLException {
        // Arrange
        int itemId = 999;
        when(mockResultSet.next()).thenReturn(false);

        // Act
        Item item = itemDAO.getItemById(itemId);

        // Assert
        assertNull("Item should be null when not found", item);
    }

    @Test
    public void testMapResultSetToItem() throws SQLException {
        // Arrange
        when(mockResultSet.getInt("item_id")).thenReturn(1);
        when(mockResultSet.getString("name")).thenReturn("Test Item");
        when(mockResultSet.getInt("category_id")).thenReturn(2);
        when(mockResultSet.getString("category_name")).thenReturn("Electronics");
        when(mockResultSet.getDouble("price")).thenReturn(99.99);
        when(mockResultSet.getInt("stock_qty")).thenReturn(10);
        when(mockResultSet.getString("description")).thenReturn("Test description");
        when(mockResultSet.getTimestamp("created_date")).thenReturn(new Timestamp(System.currentTimeMillis()));
        when(mockResultSet.getString("created_by")).thenReturn("admin");

        // Act - This tests the private method through getItemById
        when(mockResultSet.next()).thenReturn(true);
        Item item = itemDAO.getItemById(1);

        // Assert
        assertNotNull("Item should not be null", item);
        assertEquals("Item ID should match", 1, item.getItemId());
        assertEquals("Name should match", "Test Item", item.getName());
        assertEquals("Category ID should match", 2, item.getCategoryId());
        assertEquals("Category name should match", "Electronics", item.getCategoryName());
        assertEquals("Price should match", 99.99, item.getPrice(), 0.01);
        assertEquals("Stock quantity should match", 10, item.getStockQty());
        assertEquals("Description should match", "Test description", item.getDescription());
        assertNotNull("Created date should not be null", item.getCreatedDate());
        assertEquals("Created by should match", "admin", item.getCreatedBy());
    }

    @Test
    public void testAddItem_SQLException() throws SQLException {
        // Arrange
        Item item = new Item();
        item.setName("Test Item");
        item.setCategoryId(1);
        item.setPrice(99.99);
        item.setStockQty(10);
        item.setDescription("Test description");
        item.setCreatedBy("admin");

        when(mockConnection.prepareStatement(anyString(), anyInt())).thenThrow(new SQLException("Database error"));

        // Act
        boolean result = itemDAO.addItem(item);

        // Assert
        assertFalse("Add item should fail on SQL exception", false);
    }

    @Test
    public void testUpdateItem_SQLException() throws SQLException {
        // Arrange
        Item item = new Item();
        item.setItemId(1);
        item.setName("Updated Item");
        item.setCategoryId(2);
        item.setPrice(149.99);
        item.setStockQty(20);
        item.setDescription("Updated description");

        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

        // Act
        boolean result = itemDAO.updateItem(item);

        // Assert
        assertFalse("Update item should fail on SQL exception", result);
    }

    // Helper method to mock ResultSet behavior for items
    private void mockItemResultSet() throws SQLException {
        when(mockResultSet.getInt("item_id")).thenReturn(1, 2);
        when(mockResultSet.getString("name")).thenReturn("Item One", "Item Two");
        when(mockResultSet.getInt("category_id")).thenReturn(1, 2);
        when(mockResultSet.getString("category_name")).thenReturn("Books", "Electronics");
        when(mockResultSet.getDouble("price")).thenReturn(29.99, 199.99);
        when(mockResultSet.getInt("stock_qty")).thenReturn(50, 25);
        when(mockResultSet.getString("description")).thenReturn("Description one", "Description two");
        when(mockResultSet.getTimestamp("created_date")).thenReturn(
                new Timestamp(System.currentTimeMillis()),
                new Timestamp(System.currentTimeMillis() - 86400000)
        );
        when(mockResultSet.getString("created_by")).thenReturn("admin", "staff");
    }
}