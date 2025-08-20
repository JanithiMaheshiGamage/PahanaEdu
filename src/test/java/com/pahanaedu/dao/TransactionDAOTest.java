package com.pahanaedu.dao;

import com.pahanaedu.model.Transaction;
import com.pahanaedu.util.DBConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.*;
import java.sql.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class TransactionDAOTest {

    private TransactionDAO transactionDAO;
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
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        transactionDAO = new TransactionDAO();
    }

    @After
    public void tearDown() {
        if (mockedDBConnection != null) {
            mockedDBConnection.close();
        }
    }

    @Test
    public void testGetAllTransactions_WithSearchKeyword() throws SQLException {
        // Arrange
        String searchKeyword = "test";
        String searchParam = "%test%";

        when(mockResultSet.next()).thenReturn(true, true, false);
        mockTransactionResultSet();

        // Act
        List<Transaction> transactions = transactionDAO.getAllTransactions(searchKeyword);

        // Assert
        assertEquals("Should return 2 transactions", 2, transactions.size());
        assertEquals("First transaction should have correct bill no", "B001", transactions.get(0).getBillNo());
        assertEquals("Second transaction should have correct customer name", "Customer Two", transactions.get(1).getCustomerName());

        // Verify search parameters are set correctly
        verify(mockPreparedStatement).setString(1, searchKeyword);
        verify(mockPreparedStatement).setString(2, searchParam);
        verify(mockPreparedStatement).setString(3, searchParam);
    }

    @Test
    public void testGetAllTransactions_WithoutSearchKeyword() throws SQLException {
        // Arrange
        String searchKeyword = null;

        when(mockResultSet.next()).thenReturn(true, false);
        mockTransactionResultSet();

        // Act
        List<Transaction> transactions = transactionDAO.getAllTransactions(searchKeyword);

        // Assert
        assertEquals("Should return 1 transaction", 1, transactions.size());

        // Verify null parameters are set
        verify(mockPreparedStatement).setNull(1, Types.VARCHAR);
        verify(mockPreparedStatement).setNull(2, Types.VARCHAR);
        verify(mockPreparedStatement).setNull(3, Types.VARCHAR);
    }

    @Test
    public void testGetAllTransactions_EmptySearchKeyword() throws SQLException {
        // Arrange
        String searchKeyword = "";

        when(mockResultSet.next()).thenReturn(true, false);
        mockTransactionResultSet();

        // Act
        List<Transaction> transactions = transactionDAO.getAllTransactions(searchKeyword);

        // Assert
        assertEquals("Should return 1 transaction", 1, transactions.size());

        // Verify null parameters are set for empty string
        verify(mockPreparedStatement).setNull(1, Types.VARCHAR);
        verify(mockPreparedStatement).setNull(2, Types.VARCHAR);
        verify(mockPreparedStatement).setNull(3, Types.VARCHAR);
    }

    @Test
    public void testGetAllTransactions_NoResults() throws SQLException {
        // Arrange
        String searchKeyword = "nonexistent";

        when(mockResultSet.next()).thenReturn(false);

        // Act
        List<Transaction> transactions = transactionDAO.getAllTransactions(searchKeyword);

        // Assert
        assertTrue("Should return empty list", transactions.isEmpty());
        verify(mockPreparedStatement).setString(1, searchKeyword);
        verify(mockPreparedStatement).setString(2, "%nonexistent%");
        verify(mockPreparedStatement).setString(3, "%nonexistent%");
    }

    @Test
    public void testGetTransactionsByUser_WithSearchKeyword() throws SQLException {
        // Arrange
        int userId = 1;
        String searchKeyword = "test";
        String searchParam = "%test%";

        when(mockResultSet.next()).thenReturn(true, true, false);
        mockTransactionResultSet();

        // Act
        List<Transaction> transactions = transactionDAO.getTransactionsByUser(userId, searchKeyword);

        // Assert
        assertEquals("Should return 2 transactions", 2, transactions.size());

        // Verify user ID and search parameters are set correctly
        verify(mockPreparedStatement).setInt(1, userId);
        verify(mockPreparedStatement).setString(2, searchKeyword);
        verify(mockPreparedStatement).setString(3, searchParam);
        verify(mockPreparedStatement).setString(4, searchParam);
    }

    @Test
    public void testGetTransactionsByUser_WithoutSearchKeyword() throws SQLException {
        // Arrange
        int userId = 1;
        String searchKeyword = null;

        when(mockResultSet.next()).thenReturn(true, false);
        mockTransactionResultSet();

        // Act
        List<Transaction> transactions = transactionDAO.getTransactionsByUser(userId, searchKeyword);

        // Assert
        assertEquals("Should return 1 transaction", 1, transactions.size());

        // Verify user ID and null parameters are set
        verify(mockPreparedStatement).setInt(1, userId);
        verify(mockPreparedStatement).setNull(2, Types.VARCHAR);
        verify(mockPreparedStatement).setNull(3, Types.VARCHAR);
        verify(mockPreparedStatement).setNull(4, Types.VARCHAR);
    }

    @Test
    public void testGetTransactionsByUser_EmptySearchKeyword() throws SQLException {
        // Arrange
        int userId = 1;
        String searchKeyword = "";

        when(mockResultSet.next()).thenReturn(true, false);
        mockTransactionResultSet();

        // Act
        List<Transaction> transactions = transactionDAO.getTransactionsByUser(userId, searchKeyword);

        // Assert
        assertEquals("Should return 1 transaction", 1, transactions.size());

        // Verify user ID and null parameters are set for empty string
        verify(mockPreparedStatement).setInt(1, userId);
        verify(mockPreparedStatement).setNull(2, Types.VARCHAR);
        verify(mockPreparedStatement).setNull(3, Types.VARCHAR);
        verify(mockPreparedStatement).setNull(4, Types.VARCHAR);
    }

    @Test
    public void testGetTransactionsByUser_NoResults() throws SQLException {
        // Arrange
        int userId = 999;
        String searchKeyword = "nonexistent";

        when(mockResultSet.next()).thenReturn(false);

        // Act
        List<Transaction> transactions = transactionDAO.getTransactionsByUser(userId, searchKeyword);

        // Assert
        assertTrue("Should return empty list", transactions.isEmpty());
        verify(mockPreparedStatement).setInt(1, userId);
        verify(mockPreparedStatement).setString(2, searchKeyword);
        verify(mockPreparedStatement).setString(3, "%nonexistent%");
        verify(mockPreparedStatement).setString(4, "%nonexistent%");
    }

    @Test
    public void testGetAllTransactions_SQLException() throws SQLException {
        // Arrange
        String searchKeyword = "test";

        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

        // Act
        List<Transaction> transactions = transactionDAO.getAllTransactions(searchKeyword);

        // Assert
        assertTrue("Should return empty list on SQL exception", transactions.isEmpty());
    }

    @Test
    public void testGetTransactionsByUser_SQLException() throws SQLException {
        // Arrange
        int userId = 1;
        String searchKeyword = "test";

        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

        // Act
        List<Transaction> transactions = transactionDAO.getTransactionsByUser(userId, searchKeyword);

        // Assert
        assertTrue("Should return empty list on SQL exception", transactions.isEmpty());
    }

    // Helper method to mock ResultSet behavior for transactions
    private void mockTransactionResultSet() throws SQLException {
        when(mockResultSet.getInt("bill_id")).thenReturn(1, 2);
        when(mockResultSet.getString("bill_no")).thenReturn("B001", "B002");
        when(mockResultSet.getString("customer_name")).thenReturn("Customer One", "Customer Two");
        when(mockResultSet.getDouble("total_amount")).thenReturn(100.0, 200.0);
        when(mockResultSet.getString("payment_method")).thenReturn("Cash", "Card");
        when(mockResultSet.getTimestamp("paid_date")).thenReturn(
                new Timestamp(System.currentTimeMillis()),
                new Timestamp(System.currentTimeMillis() - 86400000) // yesterday
        );
        when(mockResultSet.getString("status")).thenReturn("paid", "paid");
    }

    @Test
    public void testTransactionProperties() throws SQLException {
        // Arrange
        String searchKeyword = "test";

        when(mockResultSet.next()).thenReturn(true, false);
        mockTransactionResultSet();

        // Act
        List<Transaction> transactions = transactionDAO.getAllTransactions(searchKeyword);
        Transaction transaction = transactions.get(0);

        // Assert
        assertNotNull("Transaction should not be null", transaction);
        assertEquals("Bill ID should match", 1, transaction.getBillId());
        assertEquals("Bill no should match", "B001", transaction.getBillNo());
        assertEquals("Customer name should match", "Customer One", transaction.getCustomerName());
        assertEquals("Total amount should match", 100.0, transaction.getTotalAmount(), 0.01);
        assertEquals("Payment method should match", "Cash", transaction.getPaymentMethod());
        assertNotNull("Paid date should not be null", transaction.getPaidDate());
        assertEquals("Status should match", "paid", transaction.getStatus());
    }
}