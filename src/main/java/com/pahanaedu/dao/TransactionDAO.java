package com.pahanaedu.dao;

import com.pahanaedu.model.Transaction;
import com.pahanaedu.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {
    public List<Transaction> getAllTransactions(String searchKeyword) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT b.bill_id, b.bill_no, c.name as customer_name, " +
                "b.total_amount, b.payment_method, b.created_date as paid_date, " +
                "'paid' as status " +
                "FROM bills b " +
                "JOIN customers c ON b.customer_id = c.account_no " +
                "WHERE (? IS NULL OR b.bill_no LIKE ? OR c.name LIKE ?) " +
                "ORDER BY b.created_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (searchKeyword != null && !searchKeyword.isEmpty()) {
                String searchParam = "%" + searchKeyword + "%";
                stmt.setString(1, searchKeyword);
                stmt.setString(2, searchParam);
                stmt.setString(3, searchParam);
            } else {
                stmt.setNull(1, Types.VARCHAR);
                stmt.setNull(2, Types.VARCHAR);
                stmt.setNull(3, Types.VARCHAR);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Transaction t = new Transaction();
                    t.setBillId(rs.getInt("bill_id"));
                    t.setBillNo(rs.getString("bill_no"));
                    t.setCustomerName(rs.getString("customer_name"));
                    t.setTotalAmount(rs.getDouble("total_amount"));
                    t.setPaymentMethod(rs.getString("payment_method"));
                    t.setPaidDate(rs.getTimestamp("paid_date"));
                    t.setStatus(rs.getString("status"));
                    transactions.add(t);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    public List<Transaction> getTransactionsByUser(int userId, String searchKeyword) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT b.bill_id, b.bill_no, c.name as customer_name, " +
                "b.total_amount, b.payment_method, b.created_date as paid_date, " +
                "'paid' as status " +
                "FROM bills b " +
                "JOIN customers c ON b.customer_id = c.account_no " +
                "WHERE b.created_by = ? " +
                "AND (? IS NULL OR b.bill_no LIKE ? OR c.name LIKE ?) " +
                "ORDER BY b.created_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            if (searchKeyword != null && !searchKeyword.isEmpty()) {
                String searchParam = "%" + searchKeyword + "%";
                stmt.setString(2, searchKeyword);
                stmt.setString(3, searchParam);
                stmt.setString(4, searchParam);
            } else {
                stmt.setNull(2, Types.VARCHAR);
                stmt.setNull(3, Types.VARCHAR);
                stmt.setNull(4, Types.VARCHAR);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Transaction t = new Transaction();
                    t.setBillId(rs.getInt("bill_id"));
                    t.setBillNo(rs.getString("bill_no"));
                    t.setCustomerName(rs.getString("customer_name"));
                    t.setTotalAmount(rs.getDouble("total_amount"));
                    t.setPaymentMethod(rs.getString("payment_method"));
                    t.setPaidDate(rs.getTimestamp("paid_date"));
                    t.setStatus(rs.getString("status"));
                    transactions.add(t);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }
}