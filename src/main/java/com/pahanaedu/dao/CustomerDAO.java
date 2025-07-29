package com.pahanaedu.dao;

import com.pahanaedu.model.Customer;
import com.pahanaedu.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CustomerDAO {
    private static final Logger logger = Logger.getLogger(CustomerDAO.class.getName());

    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers ORDER BY account_no";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                customers.add(mapResultSetToCustomer(rs));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting all customers", e);
        }
        return customers;
    }

    public Customer getCustomerByAccountNo(String accountNo) {
        String sql = "SELECT * FROM customers WHERE account_no = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, accountNo);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToCustomer(rs);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting customer by account no", e);
        }
        return null;
    }

    public boolean insertCustomer(Customer customer) {
        String sql = "INSERT INTO customers (account_no, name, nic, phone_no, email, address, units_consumed, created_by) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, customer.getAccountNo());
            stmt.setString(2, customer.getName());
            stmt.setString(3, customer.getNic());
            stmt.setString(4, customer.getPhoneNo());
            stmt.setString(5, customer.getEmail());
            stmt.setString(6, customer.getAddress());
            stmt.setDouble(7, customer.getUnitsConsumed());
            stmt.setString(8, customer.getCreatedBy());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error inserting customer. SQL: " + sql, e);
            return false;
        }
    }

    public boolean updateCustomer(Customer customer) {
        String sql = "UPDATE customers SET name=?, nic=?, phone_no=?, email=?, address=? " +
                "WHERE account_no=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getNic());
            stmt.setString(3, customer.getPhoneNo());
            stmt.setString(4, customer.getEmail());
            stmt.setString(5, customer.getAddress());
            stmt.setString(6, customer.getAccountNo());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating customer", e);
            return false;
        }
    }

    public boolean deleteCustomer(String accountNo) {
        String sql = "DELETE FROM customers WHERE account_no = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, accountNo);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting customer", e);
            return false;
        }
    }

    public boolean nicExists(String nic) {
        return checkExists("nic", nic);
    }

    public boolean phoneNoExists(String phoneNo) {
        return checkExists("phone_no", phoneNo);
    }

    private boolean checkExists(String column, String value) {
        String sql = "SELECT COUNT(*) FROM customers WHERE " + column + " = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, value);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error checking " + column + " exists", e);
            return false;
        }
    }

    public List<Customer> searchCustomers(String keyword) {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers WHERE account_no LIKE ? OR name LIKE ? OR nic LIKE ? " +
                "OR phone_no LIKE ? OR email LIKE ? ORDER BY account_no";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchParam = "%" + keyword + "%";
            for (int i = 1; i <= 5; i++) {
                stmt.setString(i, searchParam);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                customers.add(mapResultSetToCustomer(rs));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error searching customers", e);
        }
        return customers;
    }

    public String generateNewAccountNumber() {
        String sql = "SELECT LPAD(IFNULL(MAX(CAST(account_no AS UNSIGNED)), 0) + 1, 6, '0') FROM customers";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getString(1);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error generating new account number", e);
        }
        return "000001"; // Fallback if table is empty
    }

    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        customer.setAccountNo(rs.getString("account_no"));
        customer.setName(rs.getString("name"));
        customer.setNic(rs.getString("nic"));
        customer.setPhoneNo(rs.getString("phone_no"));
        customer.setEmail(rs.getString("email"));
        customer.setAddress(rs.getString("address"));
        customer.setUnitsConsumed(rs.getDouble("units_consumed"));
        customer.setCreatedDate(rs.getTimestamp("created_date"));
        customer.setCreatedBy(rs.getString("created_by"));
        return customer;
    }
}