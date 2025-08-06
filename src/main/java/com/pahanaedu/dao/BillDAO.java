package com.pahanaedu.dao;

import com.pahanaedu.model.Bill;
import com.pahanaedu.model.BillItem;
import com.pahanaedu.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BillDAO {
    private Connection connection;

    public BillDAO() throws SQLException {
        this.connection = DBConnection.getConnection();
    }

    // Create a new bill with its items
    public boolean createBill(Bill bill) {
        String billSQL = "INSERT INTO bills (bill_no, customer_id, total_amount, payment_method, payment_details, created_by) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        String itemsSQL = "INSERT INTO bill_items (bill_id, item_id, quantity, price, subtotal) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement billStmt = connection.prepareStatement(billSQL, Statement.RETURN_GENERATED_KEYS)) {
            // Start transaction
            connection.setAutoCommit(false);

            // Insert bill
            billStmt.setString(1, bill.getBillNo());
            billStmt.setString(2, bill.getCustomerId());
            billStmt.setDouble(3, bill.getTotalAmount());
            billStmt.setString(4, bill.getPaymentMethod());
            billStmt.setString(5, bill.getPaymentDetails());
            billStmt.setInt(6, bill.getCreatedBy());

            int affectedRows = billStmt.executeUpdate();

            if (affectedRows == 0) {
                connection.rollback();
                return false;
            }

            // Get generated bill ID
            try (ResultSet generatedKeys = billStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int billId = generatedKeys.getInt(1);

                    // Insert bill items
                    try (PreparedStatement itemsStmt = connection.prepareStatement(itemsSQL)) {
                        for (BillItem item : bill.getItems()) {
                            itemsStmt.setInt(1, billId);
                            itemsStmt.setInt(2, item.getItemId());
                            itemsStmt.setInt(3, item.getQuantity());
                            itemsStmt.setDouble(4, item.getPrice());
                            itemsStmt.setDouble(5, item.getSubtotal());
                            itemsStmt.addBatch();
                        }

                        itemsStmt.executeBatch();
                    }

                    connection.commit();
                    return true;
                } else {
                    connection.rollback();
                    return false;
                }
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Get bill by ID
    public Bill getBillById(int billId) {
        String billSQL = "SELECT * FROM bills WHERE bill_id = ?";
        String itemsSQL = "SELECT * FROM bill_items WHERE bill_id = ?";

        Bill bill = null;

        try (PreparedStatement billStmt = connection.prepareStatement(billSQL)) {
            billStmt.setInt(1, billId);

            try (ResultSet rs = billStmt.executeQuery()) {
                if (rs.next()) {
                    bill = new Bill();
                    bill.setBillId(rs.getInt("bill_id"));
                    bill.setBillNo(rs.getString("bill_no"));
                    bill.setCustomerId(rs.getString("customer_id"));
                    bill.setTotalAmount(rs.getDouble("total_amount"));
                    bill.setPaymentMethod(rs.getString("payment_method"));
                    bill.setPaymentDetails(rs.getString("payment_details"));
                    bill.setCreatedDate(rs.getTimestamp("created_date"));
                    bill.setCreatedBy(rs.getInt("created_by"));

                    // Get bill items
                    List<BillItem> items = new ArrayList<>();
                    try (PreparedStatement itemsStmt = connection.prepareStatement(itemsSQL)) {
                        itemsStmt.setInt(1, billId);

                        try (ResultSet itemsRs = itemsStmt.executeQuery()) {
                            while (itemsRs.next()) {
                                BillItem item = new BillItem();
                                item.setBillItemId(itemsRs.getInt("bill_item_id"));
                                item.setBillId(itemsRs.getInt("bill_id"));
                                item.setItemId(itemsRs.getInt("item_id"));
                                item.setQuantity(itemsRs.getInt("quantity"));
                                item.setPrice(itemsRs.getDouble("price"));
                                item.setSubtotal(itemsRs.getDouble("subtotal"));

                                items.add(item);
                            }
                        }
                    }

                    bill.setItems(items);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return bill;
    }

    // Other methods like getBillsByCustomer, getRecentBills, etc. can be added here
}