package com.pahanaedu.dao;

import java.sql.*;
import java.util.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import com.pahanaedu.util.DBConnection;

public class ReportDAO {
    private Connection connection;

    public ReportDAO() throws SQLException {
        this.connection = DBConnection.getConnection();
    }

    public Map<String, Object> getSalesSummary(Date startDate, Date endDate) throws SQLException {
        System.out.println("DEBUG - Querying between: " + startDate + " and " + endDate);

        Map<String, Object> summary = new HashMap<>();
        String sql = "SELECT COUNT(*) as transactionCount, COALESCE(SUM(total_amount), 0) as totalRevenue " +
                "FROM bills WHERE created_date BETWEEN ? AND ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setTimestamp(1, new Timestamp(startDate.getTime()));
            stmt.setTimestamp(2, new Timestamp(endDate.getTime()));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("transactionCount");
                    double revenue = rs.getDouble("totalRevenue");

                    System.out.println("DEBUG - Raw results - Count: " + count + ", Revenue: " + revenue);

                    summary.put("transactionCount", count);
                    summary.put("totalRevenue", revenue);
                }
            }
        }
        return summary;
    }

    public List<Map<String, Object>> getDailySales(Date startDate, Date endDate) throws SQLException {
        List<Map<String, Object>> dailySales = new ArrayList<>();
        String sql = "SELECT DATE(created_date) as date, SUM(total_amount) as total " +
                "FROM bills " +
                "WHERE created_date BETWEEN ? AND ? " +
                "GROUP BY DATE(created_date) " +
                "ORDER BY DATE(created_date)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setTimestamp(1, new Timestamp(startDate.getTime()));
            stmt.setTimestamp(2, new Timestamp(endDate.getTime()));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> dayData = new HashMap<>();
                    dayData.put("date", rs.getDate("date"));
                    dayData.put("total", rs.getDouble("total"));
                    dailySales.add(dayData);
                }
            }
        }
        return dailySales;
    }

    public List<Map<String, Object>> getSalesByCategory(java.util.Date startDate, java.util.Date endDate) throws SQLException {
        List<Map<String, Object>> categories = new ArrayList<>();
        String sql = "SELECT c.name as category, COUNT(bi.item_id) as itemsSold, " +
                "COALESCE(SUM(bi.subtotal), 0) as totalRevenue " +
                "FROM bill_items bi " +
                "JOIN items i ON bi.item_id = i.item_id " +
                "JOIN categories c ON i.category_id = c.category_id " +
                "JOIN bills b ON bi.bill_id = b.bill_id " +
                "WHERE b.created_date BETWEEN ? AND ? " +
                "GROUP BY c.name " +
                "ORDER BY totalRevenue DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setTimestamp(1, new Timestamp(startDate.getTime()));
            stmt.setTimestamp(2, new Timestamp(endDate.getTime()));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> category = new HashMap<>();
                    category.put("category", rs.getString("category"));
                    category.put("itemsSold", rs.getInt("itemsSold"));
                    category.put("totalRevenue", rs.getDouble("totalRevenue"));
                    categories.add(category);
                }
            }
        }
        return categories;
    }

    public List<Map<String, Object>> getTopSellingItems(java.util.Date startDate, java.util.Date endDate, int limit) throws SQLException {
        List<Map<String, Object>> items = new ArrayList<>();
        String sql = "SELECT i.name as item, c.name as category, SUM(bi.quantity) as quantitySold, " +
                "COALESCE(SUM(bi.subtotal), 0) as revenue " +
                "FROM bill_items bi " +
                "JOIN items i ON bi.item_id = i.item_id " +
                "JOIN categories c ON i.category_id = c.category_id " +
                "JOIN bills b ON bi.bill_id = b.bill_id " +
                "WHERE b.created_date BETWEEN ? AND ? " +
                "GROUP BY i.name, c.name " +
                "ORDER BY quantitySold DESC " +
                "LIMIT ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setTimestamp(1, new Timestamp(startDate.getTime()));
            stmt.setTimestamp(2, new Timestamp(endDate.getTime()));
            stmt.setInt(3, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("item", rs.getString("item"));
                    item.put("category", rs.getString("category"));
                    item.put("quantitySold", rs.getInt("quantitySold"));
                    item.put("revenue", rs.getDouble("revenue"));
                    items.add(item);
                }
            }
        }
        return items;
    }

    // Helper method to ensure we have all dates in the range (including days with no sales)
    public List<Map<String, Object>> fillDateGaps(List<Map<String, Object>> dailySales,
                                                  java.util.Date startDate,
                                                  java.util.Date endDate) {
        List<Map<String, Object>> filledData = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);

        // Convert existing sales to map by date for quick lookup
        Map<java.sql.Date, Double> salesMap = new HashMap<>();
        for (Map<String, Object> day : dailySales) {
            salesMap.put((java.sql.Date) day.get("date"), (Double) day.get("total"));
        }

        // Iterate through each day in the range
        while (!calendar.getTime().after(endDate)) {
            java.util.Date currentDate = calendar.getTime();
            java.sql.Date sqlDate = new java.sql.Date(currentDate.getTime());

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", sqlDate);
            dayData.put("total", salesMap.getOrDefault(sqlDate, 0.0));
            filledData.add(dayData);

            calendar.add(Calendar.DATE, 1);
        }

        return filledData;
    }
}