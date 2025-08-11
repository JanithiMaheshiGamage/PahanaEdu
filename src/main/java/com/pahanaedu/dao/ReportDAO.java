package com.pahanaedu.dao;

import com.pahanaedu.model.ReportItem;
import java.sql.*;
import java.util.*;
import java.util.Date;

public class ReportDAO {
    private final Connection connection;

    public ReportDAO(Connection connection) {
        this.connection = Objects.requireNonNull(connection, "Database connection cannot be null");
    }

    // Transaction Methods
    public int getTransactionCount(String startDate, String endDate) throws SQLException {
        String sql = "SELECT COUNT(*) FROM bills WHERE created_date BETWEEN ? AND ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, startDate);
            stmt.setString(2, endDate);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public double getTotalRevenue(String startDate, String endDate) throws SQLException {
        String sql = "SELECT SUM(total_amount) FROM bills WHERE created_date BETWEEN ? AND ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, startDate);
            stmt.setString(2, endDate);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0;
    }

    public double getAverageTransactionValue(String startDate, String endDate) throws SQLException {
        int count = getTransactionCount(startDate, endDate);
        if (count == 0) return 0;
        return getTotalRevenue(startDate, endDate) / count;
    }

    public List<ReportItem> getRecentTransactions(String startDate, String endDate, int limit) throws SQLException {
        List<ReportItem> transactions = new ArrayList<>();
        String sql = "SELECT b.bill_id, b.bill_no, c.name as customer_name, b.total_amount, " +
                "b.payment_method, b.created_date " +
                "FROM bills b JOIN customers c ON b.customer_id = c.account_no " +
                "WHERE b.created_date BETWEEN ? AND ? " +
                "ORDER BY b.created_date DESC LIMIT ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, startDate);
            stmt.setString(2, endDate);
            stmt.setInt(3, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                transactions.add(new ReportItem(
                        rs.getInt("bill_id"),
                        rs.getString("bill_no"),
                        rs.getString("customer_name"),
                        rs.getDouble("total_amount"),
                        rs.getString("payment_method"),
                        rs.getDate("created_date")
                ));
            }
        }
        return transactions;
    }

    // Inventory Methods
    public int getItemCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM items";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public int getLowStockItemCount(int threshold) throws SQLException {
        String sql = "SELECT COUNT(*) FROM items WHERE stock_qty > 0 AND stock_qty <= ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, threshold);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public int getOutOfStockItemCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM items WHERE stock_qty = 0";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public List<ReportItem> getLowStockItems(int threshold) throws SQLException {
        List<ReportItem> items = new ArrayList<>();
        String sql = "SELECT i.item_id, i.name, c.name as category, i.price, i.stock_qty " +
                "FROM items i JOIN categories c ON i.category_id = c.category_id " +
                "WHERE i.stock_qty > 0 AND i.stock_qty <= ? ORDER BY i.stock_qty ASC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, threshold);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                items.add(new ReportItem(
                        rs.getInt("item_id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getDouble("price"),
                        rs.getInt("stock_qty")
                ));
            }
        }
        return items;
    }

    // Customer Methods
    public int getCustomerCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM customers";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public int getActiveCustomerCount(String startDate, String endDate) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT customer_id) FROM bills WHERE created_date BETWEEN ? AND ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, startDate);
            stmt.setString(2, endDate);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public double getAverageTransactionsPerCustomer(String startDate, String endDate) throws SQLException {
        int customers = getActiveCustomerCount(startDate, endDate);
        if (customers == 0) return 0;
        return (double) getTransactionCount(startDate, endDate) / customers;
    }

    public List<ReportItem> getTopSpendingCustomers(String startDate, String endDate, int limit) throws SQLException {
        List<ReportItem> customers = new ArrayList<>();
        String sql = "SELECT c.account_no, c.name, COUNT(b.bill_id) as transaction_count, " +
                "SUM(b.total_amount) as total_spent, MAX(b.created_date) as last_transaction " +
                "FROM customers c JOIN bills b ON c.account_no = b.customer_id " +
                "WHERE b.created_date BETWEEN ? AND ? " +
                "GROUP BY c.account_no, c.name " +
                "ORDER BY total_spent DESC LIMIT ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, startDate);
            stmt.setString(2, endDate);
            stmt.setInt(3, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                customers.add(new ReportItem(
                        rs.getString("account_no"),
                        rs.getString("name"),
                        rs.getDouble("total_spent"),
                        rs.getInt("transaction_count"),  // This should match the SQL alias
                        rs.getDate("last_transaction")
                ));
            }
        }
        return customers;
    }

    // Popular Items Methods
    public int getTotalItemsSoldCount(String startDate, String endDate) throws SQLException {
        String sql = "SELECT SUM(quantity) FROM bill_items bi " +
                "JOIN bills b ON bi.bill_id = b.bill_id " +
                "WHERE b.created_date BETWEEN ? AND ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, startDate);
            stmt.setString(2, endDate);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public double getAverageItemsPerTransaction(String startDate, String endDate) throws SQLException {
        int transactions = getTransactionCount(startDate, endDate);
        if (transactions == 0) return 0;
        return (double) getTotalItemsSoldCount(startDate, endDate) / transactions;
    }

    public List<ReportItem> getTopSellingItems(String startDate, String endDate, int limit) throws SQLException {
        List<ReportItem> items = new ArrayList<>();
        String sql = "SELECT i.item_id, i.name, c.name as category, " +
                "SUM(bi.quantity) as quantity_sold, SUM(bi.subtotal) as total_revenue " +
                "FROM items i JOIN categories c ON i.category_id = c.category_id " +
                "JOIN bill_items bi ON i.item_id = bi.item_id " +
                "JOIN bills b ON bi.bill_id = b.bill_id " +
                "WHERE b.created_date BETWEEN ? AND ? " +
                "GROUP BY i.item_id, i.name, c.name " +
                "ORDER BY quantity_sold DESC LIMIT ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, startDate);
            stmt.setString(2, endDate);
            stmt.setInt(3, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                items.add(new ReportItem(
                        rs.getInt("item_id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getDouble("total_revenue"),
                        rs.getInt("quantity_sold")
                ));
            }
        }
        return items;
    }

    // Chart Data Methods
    public Map<String, Object> getDailySales(String startDate, String endDate) throws SQLException {
        Map<String, Object> result = new HashMap<>();
        List<String> labels = new ArrayList<>();
        List<Double> values = new ArrayList<>();

        String sql = "SELECT DATE(created_date) as day, SUM(total_amount) as total " +
                "FROM bills " +
                "WHERE created_date BETWEEN ? AND ? " +
                "GROUP BY DATE(created_date) " +
                "ORDER BY day";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, startDate);
            stmt.setString(2, endDate);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                labels.add(rs.getString("day"));
                values.add(rs.getDouble("total"));
            }
        }

        result.put("labels", labels);
        result.put("values", values);
        return result;
    }

    public Map<String, Object> getPaymentMethodDistribution(String startDate, String endDate) throws SQLException {
        Map<String, Object> result = new HashMap<>();
        List<String> labels = new ArrayList<>();
        List<Double> values = new ArrayList<>();

        String sql = "SELECT payment_method, SUM(total_amount) as total " +
                "FROM bills " +
                "WHERE created_date BETWEEN ? AND ? " +
                "GROUP BY payment_method";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, startDate);
            stmt.setString(2, endDate);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                labels.add(rs.getString("payment_method").toUpperCase());
                values.add(rs.getDouble("total"));
            }
        }

        result.put("labels", labels);
        result.put("values", values);
        return result;
    }

    public Map<String, Object> getInventoryByCategory() throws SQLException {
        Map<String, Object> result = new HashMap<>();
        List<String> categories = new ArrayList<>();
        List<Integer> stockLevels = new ArrayList<>();

        String sql = "SELECT c.name as category, SUM(i.stock_qty) as stock " +
                "FROM items i JOIN categories c ON i.category_id = c.category_id " +
                "GROUP BY c.name";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                categories.add(rs.getString("category"));
                stockLevels.add(rs.getInt("stock"));
            }
        }

        result.put("categories", categories);
        result.put("stockLevels", stockLevels);
        return result;
    }

    public Map<String, Object> getCustomerAcquisition(String startDate, String endDate) throws SQLException {
        Map<String, Object> result = new HashMap<>();
        List<String> labels = new ArrayList<>();
        List<Integer> newCustomers = new ArrayList<>();
        List<Integer> repeatCustomers = new ArrayList<>();

        String sql = "SELECT WEEK(created_date) as week, " +
                "COUNT(CASE WHEN is_new = 1 THEN 1 END) as new_customers, " +
                "COUNT(CASE WHEN is_new = 0 THEN 1 END) as repeat_customers " +
                "FROM ( " +
                "  SELECT c.account_no, c.created_date, " +
                "    CASE WHEN EXISTS (SELECT 1 FROM bills b WHERE b.customer_id = c.account_no " +
                "      AND b.created_date < ?) THEN 0 ELSE 1 END as is_new " +
                "  FROM customers c " +
                "  WHERE c.created_date BETWEEN ? AND ? " +
                ") as customer_status " +
                "GROUP BY WEEK(created_date) " +
                "ORDER BY week";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, startDate);
            stmt.setString(2, startDate);
            stmt.setString(3, endDate);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                labels.add("Week " + rs.getInt("week"));
                newCustomers.add(rs.getInt("new_customers"));
                repeatCustomers.add(rs.getInt("repeat_customers"));
            }
        }

        result.put("labels", labels);
        result.put("newCustomers", newCustomers);
        result.put("repeatCustomers", repeatCustomers);
        return result;
    }

    public Map<String, Object> getPopularItems(String startDate, String endDate) throws SQLException {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> items = new ArrayList<>();

        String sql = "SELECT i.item_id, i.name, SUM(bi.quantity) as quantity, SUM(bi.subtotal) as total " +
                "FROM bill_items bi " +
                "JOIN items i ON bi.item_id = i.item_id " +
                "JOIN bills b ON bi.bill_id = b.bill_id " +
                "WHERE b.created_date BETWEEN ? AND ? " +
                "GROUP BY i.item_id, i.name " +
                "ORDER BY quantity DESC " +
                "LIMIT 10";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, startDate);
            stmt.setString(2, endDate);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> item = new HashMap<>();
                item.put("name", rs.getString("name"));
                item.put("quantity", rs.getInt("quantity"));
                item.put("total", rs.getDouble("total"));
                items.add(item);
            }
        }

        result.put("items", items);
        return result;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
}