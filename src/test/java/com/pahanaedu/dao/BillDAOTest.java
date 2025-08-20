package com.pahanaedu.dao;

import com.pahanaedu.model.Bill;
import com.pahanaedu.model.BillItem;
import com.pahanaedu.util.DBConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class BillDAOTest {

    private BillDAO billDAO;
    private MockedStatic<DBConnection> mockedDBConnection;

    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private PreparedStatement mockBillStmt;
    private PreparedStatement mockUpdateCustomerStmt;
    private PreparedStatement mockUpdateStockStmt;
    private PreparedStatement mockItemsStmt;
    private ResultSet mockResultSet;
    private ResultSet mockGeneratedKeys;

    @Before
    public void setUp() throws SQLException {
        // Create mocks
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockBillStmt = mock(PreparedStatement.class);
        mockUpdateCustomerStmt = mock(PreparedStatement.class);
        mockUpdateStockStmt = mock(PreparedStatement.class);
        mockItemsStmt = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
        mockGeneratedKeys = mock(ResultSet.class);

        // Mock the static DBConnection.getConnection() method
        mockedDBConnection = Mockito.mockStatic(DBConnection.class);
        mockedDBConnection.when(DBConnection::getConnection).thenReturn(mockConnection);

        // Default mock behaviors
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(mockBillStmt);

        // Specific prepared statements for createBill
        when(mockConnection.prepareStatement("INSERT INTO bills (bill_no, customer_id, total_amount, payment_method, payment_details, created_by) VALUES (?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)).thenReturn(mockBillStmt);
        when(mockConnection.prepareStatement("UPDATE customers SET units_consumed = units_consumed + 1 WHERE account_no = ?"))
                .thenReturn(mockUpdateCustomerStmt);
        when(mockConnection.prepareStatement("UPDATE items SET stock_qty = stock_qty - ? WHERE item_id = ?"))
                .thenReturn(mockUpdateStockStmt);
        when(mockConnection.prepareStatement("INSERT INTO bill_items (bill_id, item_id, quantity, price, subtotal) VALUES (?, ?, ?, ?, ?)"))
                .thenReturn(mockItemsStmt);

        when(mockBillStmt.executeUpdate()).thenReturn(1);
        when(mockUpdateCustomerStmt.executeUpdate()).thenReturn(1);
        when(mockUpdateStockStmt.executeUpdate()).thenReturn(1);
        when(mockItemsStmt.executeUpdate()).thenReturn(1);

        // Create BillDAO instance without calling constructor that throws SQLException
        billDAO = createBillDAOInstance();
    }

    // Helper method to create BillDAO instance without calling the problematic constructor
    private BillDAO createBillDAOInstance() {
        try {
            // Use reflection to create instance without calling constructor
            BillDAO instance = mock(BillDAO.class, CALLS_REAL_METHODS);

            // Use reflection to set the connection field
            java.lang.reflect.Field connectionField = BillDAO.class.getDeclaredField("connection");
            connectionField.setAccessible(true);
            connectionField.set(instance, mockConnection);

            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create BillDAO instance", e);
        }
    }

    @After
    public void tearDown() {
        if (mockedDBConnection != null) {
            mockedDBConnection.close();
        }
    }

    @Test
    public void testCreateBill_Success() throws SQLException {
        // Arrange
        Bill bill = createTestBill();
        when(mockBillStmt.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockGeneratedKeys.next()).thenReturn(true);
        when(mockGeneratedKeys.getInt(1)).thenReturn(123);

        // Act
        boolean result = billDAO.createBill(bill);

        // Assert
        assertTrue("Bill creation should succeed", result);
        verify(mockConnection).setAutoCommit(false);
        verify(mockBillStmt).setString(1, "BILL-001");
        verify(mockBillStmt).setString(2, "CUST-001");
        verify(mockBillStmt).setDouble(3, 150.0);
        verify(mockBillStmt).setString(4, "CASH");
        verify(mockBillStmt).setString(5, "Paid in full");
        verify(mockBillStmt).setInt(6, 1);
        verify(mockItemsStmt, times(2)).addBatch();
        verify(mockUpdateStockStmt, times(2)).addBatch();
        verify(mockUpdateCustomerStmt).setString(1, "CUST-001");
        verify(mockConnection).commit();
        verify(mockConnection).setAutoCommit(true);
    }

    @Test(expected = SQLException.class)
    public void testCreateBill_InvalidData() throws SQLException {
        // Arrange
        Bill bill = new Bill(); // Invalid bill with no customer ID and items

        // Act
        billDAO.createBill(bill);
    }

    @Test
    public void testCreateBill_NoRowsAffected() throws SQLException {
        // Arrange
        Bill bill = createTestBill();
        when(mockBillStmt.executeUpdate()).thenReturn(0);

        // Act
        boolean result = billDAO.createBill(bill);

        // Assert
        assertFalse("Bill creation should fail when no rows affected", result);
        verify(mockConnection).rollback();
    }

    @Test
    public void testCreateBill_NoGeneratedKeys() throws SQLException {
        // Arrange
        Bill bill = createTestBill();
        when(mockBillStmt.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockGeneratedKeys.next()).thenReturn(false);

        // Act
        boolean result = billDAO.createBill(bill);

        // Assert
        assertFalse("Bill creation should fail when no generated keys", result);
        verify(mockConnection).rollback();
    }

    @Test
    public void testCreateBill_SQLException() throws SQLException {
        // Arrange
        Bill bill = createTestBill();
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenThrow(new SQLException("Database error"));

        // Act
        boolean result = billDAO.createBill(bill);

        // Assert
        assertFalse("Bill creation should fail on SQL exception", result);
    }

    @Test
    public void testGetBillByNumber_Found() throws SQLException {
        // Arrange
        String billNo = "BILL-001";
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        mockBillResultSet();

        // Act
        Bill bill = billDAO.getBillByNumber(billNo);

        // Assert
        assertNotNull("Bill should not be null", bill);
        assertEquals("Bill number should match", "BILL-001", bill.getBillNo());
        verify(mockPreparedStatement).setString(1, billNo);
    }

    @Test
    public void testGetBillByNumber_NotFound() throws SQLException {
        // Arrange
        String billNo = "NONEXISTENT";
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Act
        Bill bill = billDAO.getBillByNumber(billNo);

        // Assert
        assertNull("Bill should be null when not found", bill);
    }

    @Test
    public void testGetBillByNumber_SQLException() throws SQLException {
        // Arrange
        String billNo = "BILL-001";
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

        // Act
        Bill bill = billDAO.getBillByNumber(billNo);

        // Assert
        assertNull("Bill should be null on SQL exception", bill);
    }

    @Test
    public void testGetBillItems() throws SQLException {
        // Arrange
        int billId = 123;
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);
        mockBillItemResultSet();

        // Act
        List<BillItem> items = billDAO.getBillItems(billId);

        // Assert
        assertEquals("Should return 2 bill items", 2, items.size());
        assertEquals("First item should have correct name", "Item One", items.get(0).getItemName());
        assertEquals("Second item should have correct quantity", 2, items.get(1).getQuantity());
        verify(mockPreparedStatement).setInt(1, billId);
    }

    @Test
    public void testGetBillItems_Empty() throws SQLException {
        // Arrange
        int billId = 123;
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Act
        List<BillItem> items = billDAO.getBillItems(billId);

        // Assert
        assertTrue("Should return empty list", items.isEmpty());
    }

    @Test
    public void testGetBillItems_SQLException() throws SQLException {
        // Arrange
        int billId = 123;
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

        // Act
        List<BillItem> items = billDAO.getBillItems(billId);

        // Assert
        assertTrue("Should return empty list on SQL exception", items.isEmpty());
    }

    @Test
    public void testGetBillById_Found() throws SQLException {
        // Arrange
        int billId = 123;

        // Mock bill statement
        PreparedStatement mockBillStmt = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement("SELECT * FROM bills WHERE bill_id = ?")).thenReturn(mockBillStmt);
        when(mockBillStmt.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        mockBillResultSet();

        // Mock items statement
        PreparedStatement mockItemsStmt = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement("SELECT bi.*, i.name as item_name FROM bill_items bi JOIN items i ON bi.item_id = i.item_id WHERE bi.bill_id = ?"))
                .thenReturn(mockItemsStmt);

        ResultSet mockItemsResultSet = mock(ResultSet.class);
        when(mockItemsStmt.executeQuery()).thenReturn(mockItemsResultSet);
        when(mockItemsResultSet.next()).thenReturn(true, false);
        mockSingleBillItemResultSet(mockItemsResultSet);

        // Act
        Bill bill = billDAO.getBillById(billId);

        // Assert
        assertNotNull("Bill should not be null", bill);
        assertEquals("Bill ID should match", 123, bill.getBillId());
        assertEquals("Should have 1 item", 1, bill.getItems().size());
        verify(mockBillStmt).setInt(1, billId);
        verify(mockItemsStmt).setInt(1, billId);
    }

    @Test
    public void testGetBillById_NotFound() throws SQLException {
        // Arrange
        int billId = 999;
        PreparedStatement mockBillStmt = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement("SELECT * FROM bills WHERE bill_id = ?")).thenReturn(mockBillStmt);
        when(mockBillStmt.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Act
        Bill bill = billDAO.getBillById(billId);

        // Assert
        assertNull("Bill should be null when not found", bill);
    }

    @Test
    public void testGetBillById_SQLException() throws SQLException {
        // Arrange
        int billId = 123;
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

        // Act
        Bill bill = billDAO.getBillById(billId);

        // Assert
        assertNull("Bill should be null on SQL exception", bill);
    }

    // Helper method to create a test bill
    private Bill createTestBill() {
        Bill bill = new Bill();
        bill.setBillNo("BILL-001");
        bill.setCustomerId("CUST-001");
        bill.setTotalAmount(150.0);
        bill.setPaymentMethod("CASH");
        bill.setPaymentDetails("Paid in full");
        bill.setCreatedBy(1);

        List<BillItem> items = new ArrayList<>();

        BillItem item1 = new BillItem();
        item1.setItemId(1);
        item1.setQuantity(1);
        item1.setPrice(50.0);
        item1.setSubtotal(50.0);
        items.add(item1);

        BillItem item2 = new BillItem();
        item2.setItemId(2);
        item2.setQuantity(2);
        item2.setPrice(50.0);
        item2.setSubtotal(100.0);
        items.add(item2);

        bill.setItems(items);
        return bill;
    }

    // Helper method to mock bill result set
    private void mockBillResultSet() throws SQLException {
        when(mockResultSet.getInt("bill_id")).thenReturn(123);
        when(mockResultSet.getString("bill_no")).thenReturn("BILL-001");
        when(mockResultSet.getString("customer_id")).thenReturn("CUST-001");
        when(mockResultSet.getDouble("total_amount")).thenReturn(150.0);
        when(mockResultSet.getString("payment_method")).thenReturn("CASH");
        when(mockResultSet.getString("payment_details")).thenReturn("Paid in full");
        when(mockResultSet.getTimestamp("created_date")).thenReturn(new Timestamp(System.currentTimeMillis()));
        when(mockResultSet.getInt("created_by")).thenReturn(1);
    }

    // Helper method to mock bill item result set
    private void mockBillItemResultSet() throws SQLException {
        when(mockResultSet.getInt("bill_item_id")).thenReturn(1, 2);
        when(mockResultSet.getInt("bill_id")).thenReturn(123, 123);
        when(mockResultSet.getInt("item_id")).thenReturn(1, 2);
        when(mockResultSet.getInt("quantity")).thenReturn(1, 2);
        when(mockResultSet.getDouble("price")).thenReturn(50.0, 50.0);
        when(mockResultSet.getDouble("subtotal")).thenReturn(50.0, 100.0);
        when(mockResultSet.getString("item_name")).thenReturn("Item One", "Item Two");
    }

    // Helper method to mock single bill item result set
    private void mockSingleBillItemResultSet(ResultSet resultSet) throws SQLException {
        when(resultSet.getInt("bill_item_id")).thenReturn(1);
        when(resultSet.getInt("bill_id")).thenReturn(123);
        when(resultSet.getInt("item_id")).thenReturn(1);
        when(resultSet.getInt("quantity")).thenReturn(1);
        when(resultSet.getDouble("price")).thenReturn(50.0);
        when(resultSet.getDouble("subtotal")).thenReturn(50.0);
        when(resultSet.getString("item_name")).thenReturn("Test Item");
    }
}