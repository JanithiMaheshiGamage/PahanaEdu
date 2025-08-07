package com.pahanaedu.dao;

import com.pahanaedu.model.Item;
import com.pahanaedu.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ItemDAO {
    private static final Logger logger = Logger.getLogger(ItemDAO.class.getName());

    public List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT i.*, c.category_name FROM items i LEFT JOIN categories c ON i.category_id = c.category_id ORDER BY i.name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                items.add(mapResultSetToItem(rs));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting all items", e);
        }
        return items;
    }

    public boolean addItem(Item item) {
        String sql = "INSERT INTO items (name, category_id, price, stock_qty, description, created_by) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Set parameters
            stmt.setString(1, item.getName());
            stmt.setInt(2, item.getCategoryId());
            stmt.setDouble(3, item.getPrice());
            stmt.setInt(4, item.getStockQty());
            stmt.setString(5, item.getDescription() != null ? item.getDescription() : "");
            stmt.setString(6, item.getCreatedBy());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                return false;
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    item.setItemId(generatedKeys.getInt(1));
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error adding item: " + e.getMessage(), e);
            logger.severe("SQL State: " + e.getSQLState());
            logger.severe("Error Code: " + e.getErrorCode());
            return false;
        }
    }

    public boolean updateItem(Item item) {
        String sql = "UPDATE items SET name=?, category_id=?, price=?, stock_qty=?, description=? WHERE item_id=?";
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, item.getName());
            stmt.setInt(2, item.getCategoryId());
            stmt.setDouble(3, item.getPrice());
            stmt.setInt(4, item.getStockQty());
            stmt.setString(5, item.getDescription());
            stmt.setInt(6, item.getItemId());

            int affectedRows = stmt.executeUpdate();
            conn.commit();

            return affectedRows > 0;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.log(Level.SEVERE, "Error rolling back transaction", ex);
                }
            }
            logger.log(Level.SEVERE, "Error updating item", e);
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Error closing resources", e);
            }
        }
    }

    public boolean deleteItem(int itemId) {
        String sql = "DELETE FROM items WHERE item_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, itemId);

            int affectedRows = stmt.executeUpdate();
            conn.commit();

            return affectedRows > 0;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.log(Level.SEVERE, "Error rolling back transaction", ex);
                }
            }
            logger.log(Level.SEVERE, "Error deleting item", e);
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Error closing resources", e);
            }
        }
    }

    public int countItemsInCategory(int categoryId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM items WHERE category_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, categoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public List<Item> searchItems(String keyword) {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT i.*, c.category_name FROM items i " +
                "LEFT JOIN categories c ON i.category_id = c.category_id " +
                "WHERE i.name LIKE ? OR i.description LIKE ? OR c.category_name LIKE ? " +
                "ORDER BY i.name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchParam = "%" + keyword + "%";
            stmt.setString(1, searchParam);
            stmt.setString(2, searchParam);
            stmt.setString(3, searchParam);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapResultSetToItem(rs));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error searching items", e);
        }
        return items;
    }

    public boolean itemNameExists(String itemName) {
        String sql = "SELECT COUNT(*) FROM items WHERE name = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, itemName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error checking item name existence", e);
        }
        return false;
    }

    private Item mapResultSetToItem(ResultSet rs) throws SQLException {
        Item item = new Item();
        item.setItemId(rs.getInt("item_id"));
        item.setName(rs.getString("name"));
        item.setCategoryId(rs.getInt("category_id"));
        item.setCategoryName(rs.getString("category_name"));
        item.setPrice(rs.getDouble("price"));
        item.setStockQty(rs.getInt("stock_qty"));
        item.setDescription(rs.getString("description"));
        item.setCreatedDate(rs.getTimestamp("created_date"));
        item.setCreatedBy(rs.getString("created_by"));
        return item;
    }

    public Item getItemById(int itemId) {
        String sql = "SELECT i.*, c.category_name FROM items i " +
                "LEFT JOIN categories c ON i.category_id = c.category_id " +
                "WHERE i.item_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, itemId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToItem(rs);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting item by ID", e);
        }
        return null;
    }
}