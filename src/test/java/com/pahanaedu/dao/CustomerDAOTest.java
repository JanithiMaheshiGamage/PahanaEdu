package com.pahanaedu.dao;

import com.pahanaedu.model.Customer;
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

public class CustomerDAOTest {

    private CustomerDAO customerDAO;
    private MockedStatic<DBConnection> mockedDBConnection;

    private Connection mockConnection;
    private Statement mockStatement;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;

    @Before
    public void setUp() throws SQLException {
        // Create mocks
        mockConnection = mock(Connection.class);
        mockStatement = mock(Statement.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        // Mock the static DBConnection.getConnection() method
        mockedDBConnection = Mockito.mockStatic(DBConnection.class);
        mockedDBConnection.when(DBConnection::getConnection).thenReturn(mockConnection);

        // Default mock behaviors
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        customerDAO = new CustomerDAO();
    }

    @After
    public void tearDown() {
        if (mockedDBConnection != null) {
            mockedDBConnection.close();
        }
    }

    @Test
    public void testGetAllCustomers() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(true, true, false);
        mockCustomerResultSet();

        // Act
        List<Customer> customers = customerDAO.getAllCustomers();

        // Assert
        assertEquals("Should return 2 customers", 2, customers.size());
        assertEquals("First customer should have correct account no", "000001", customers.get(0).getAccountNo());
        assertEquals("Second customer should have correct name", "Customer Two", customers.get(1).getName());
    }

    @Test
    public void testGetAllCustomers_Empty() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(false);

        // Act
        List<Customer> customers = customerDAO.getAllCustomers();

        // Assert
        assertTrue("Should return empty list", customers.isEmpty());
    }

    @Test
    public void testGetCustomerByAccountNo_Found() throws SQLException {
        // Arrange
        String accountNo = "000001";
        when(mockResultSet.next()).thenReturn(true);
        mockCustomerResultSet();

        // Act
        Customer customer = customerDAO.getCustomerByAccountNo(accountNo);

        // Assert
        assertNotNull("Customer should not be null", customer);
        assertEquals("Account number should match", accountNo, customer.getAccountNo());
        verify(mockPreparedStatement).setString(1, accountNo);
    }

    @Test
    public void testGetCustomerByAccountNo_NotFound() throws SQLException {
        // Arrange
        String accountNo = "999999";
        when(mockResultSet.next()).thenReturn(false);

        // Act
        Customer customer = customerDAO.getCustomerByAccountNo(accountNo);

        // Assert
        assertNull("Customer should be null when not found", customer);
    }

    @Test
    public void testInsertCustomer_Success() throws SQLException {
        // Arrange
        Customer customer = new Customer();
        customer.setAccountNo("000001");
        customer.setName("Test Customer");
        customer.setNic("123456789V");
        customer.setPhoneNo("0771234567");
        customer.setEmail("test@example.com");
        customer.setAddress("Test Address");
        customer.setUnitsConsumed(100.5);
        customer.setCreatedBy("admin");

        // Act
        boolean result = customerDAO.insertCustomer(customer);

        // Assert
        assertTrue("Insert customer should succeed", result);
        verify(mockPreparedStatement).setString(1, "000001");
        verify(mockPreparedStatement).setString(2, "Test Customer");
        verify(mockPreparedStatement).setString(3, "123456789V");
        verify(mockPreparedStatement).setString(4, "0771234567");
        verify(mockPreparedStatement).setString(5, "test@example.com");
        verify(mockPreparedStatement).setString(6, "Test Address");
        verify(mockPreparedStatement).setDouble(7, 100.5);
        verify(mockPreparedStatement).setString(8, "admin");
    }

    @Test
    public void testInsertCustomer_Failure() throws SQLException {
        // Arrange
        Customer customer = new Customer();
        customer.setAccountNo("000001");
        customer.setName("Test Customer");
        customer.setNic("123456789V");
        customer.setPhoneNo("0771234567");
        customer.setEmail("test@example.com");
        customer.setAddress("Test Address");
        customer.setUnitsConsumed(100.5);
        customer.setCreatedBy("admin");

        when(mockPreparedStatement.executeUpdate()).thenReturn(0);

        // Act
        boolean result = customerDAO.insertCustomer(customer);

        // Assert
        assertFalse("Insert customer should fail", result);
    }

    @Test
    public void testUpdateCustomer_Success() throws SQLException {
        // Arrange
        Customer customer = new Customer();
        customer.setAccountNo("000001");
        customer.setName("Updated Customer");
        customer.setNic("987654321V");
        customer.setPhoneNo("0777654321");
        customer.setEmail("updated@example.com");
        customer.setAddress("Updated Address");

        // Act
        boolean result = customerDAO.updateCustomer(customer);

        // Assert
        assertTrue("Update customer should succeed", result);
        verify(mockPreparedStatement).setString(1, "Updated Customer");
        verify(mockPreparedStatement).setString(2, "987654321V");
        verify(mockPreparedStatement).setString(3, "0777654321");
        verify(mockPreparedStatement).setString(4, "updated@example.com");
        verify(mockPreparedStatement).setString(5, "Updated Address");
        verify(mockPreparedStatement).setString(6, "000001");
    }

    @Test
    public void testUpdateCustomer_Failure() throws SQLException {
        // Arrange
        Customer customer = new Customer();
        customer.setAccountNo("000001");
        customer.setName("Updated Customer");
        customer.setNic("987654321V");
        customer.setPhoneNo("0777654321");
        customer.setEmail("updated@example.com");
        customer.setAddress("Updated Address");

        when(mockPreparedStatement.executeUpdate()).thenReturn(0);

        // Act
        boolean result = customerDAO.updateCustomer(customer);

        // Assert
        assertFalse("Update customer should fail", result);
    }

    @Test
    public void testDeleteCustomer_Success() throws SQLException {
        // Arrange
        String accountNo = "000001";

        // Act
        boolean result = customerDAO.deleteCustomer(accountNo);

        // Assert
        assertTrue("Delete customer should succeed", result);
        verify(mockPreparedStatement).setString(1, accountNo);
    }

    @Test
    public void testDeleteCustomer_Failure() throws SQLException {
        // Arrange
        String accountNo = "999999";

        when(mockPreparedStatement.executeUpdate()).thenReturn(0);

        // Act
        boolean result = customerDAO.deleteCustomer(accountNo);

        // Assert
        assertFalse("Delete customer should fail", result);
    }

    @Test
    public void testNicExists_True() throws SQLException {
        // Arrange
        String nic = "123456789V";
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(1);

        // Act
        boolean result = customerDAO.nicExists(nic);

        // Assert
        assertTrue("NIC should exist", result);
        verify(mockPreparedStatement).setString(1, nic);
    }

    @Test
    public void testNicExists_False() throws SQLException {
        // Arrange
        String nic = "999999999V";
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(0);

        // Act
        boolean result = customerDAO.nicExists(nic);

        // Assert
        assertFalse("NIC should not exist", result);
    }

    @Test
    public void testPhoneNoExists_True() throws SQLException {
        // Arrange
        String phoneNo = "0771234567";
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(1);

        // Act
        boolean result = customerDAO.phoneNoExists(phoneNo);

        // Assert
        assertTrue("Phone number should exist", result);
        verify(mockPreparedStatement).setString(1, phoneNo);
    }

    @Test
    public void testPhoneNoExists_False() throws SQLException {
        // Arrange
        String phoneNo = "0779999999";
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(0);

        // Act
        boolean result = customerDAO.phoneNoExists(phoneNo);

        // Assert
        assertFalse("Phone number should not exist", result);
    }

    @Test
    public void testSearchCustomers() throws SQLException {
        // Arrange
        String keyword = "test";
        String searchParam = "%test%";

        when(mockResultSet.next()).thenReturn(true, true, false);
        mockCustomerResultSet();

        // Act
        List<Customer> customers = customerDAO.searchCustomers(keyword);

        // Assert
        assertEquals("Should return 2 customers", 2, customers.size());

        // Verify all 5 parameters are set with searchParam
        for (int i = 1; i <= 5; i++) {
            verify(mockPreparedStatement).setString(i, searchParam);
        }
    }

    @Test
    public void testSearchCustomers_NoResults() throws SQLException {
        // Arrange
        String keyword = "nonexistent";

        when(mockResultSet.next()).thenReturn(false);

        // Act
        List<Customer> customers = customerDAO.searchCustomers(keyword);

        // Assert
        assertTrue("Should return empty list", customers.isEmpty());
    }

    @Test
    public void testGetCustomersByPhone() throws SQLException {
        // Arrange
        String phone = "077";
        String searchParam = "%077%";

        when(mockResultSet.next()).thenReturn(true, true, false);
        mockCustomerResultSet();

        // Act
        List<Customer> customers = customerDAO.getCustomersByPhone(phone);

        // Assert
        assertEquals("Should return 2 customers", 2, customers.size());
        verify(mockPreparedStatement).setString(1, searchParam);
    }

    @Test
    public void testGetCustomersByPhone_NoResults() throws SQLException {
        // Arrange
        String phone = "999";

        when(mockResultSet.next()).thenReturn(false);

        // Act
        List<Customer> customers = customerDAO.getCustomersByPhone(phone);

        // Assert
        assertTrue("Should return empty list", customers.isEmpty());
    }

    @Test
    public void testGenerateNewAccountNumber_FromDatabase() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString(1)).thenReturn("000123");

        // Act
        String accountNo = customerDAO.generateNewAccountNumber();

        // Assert
        assertEquals("Should generate correct account number", "000123", accountNo);
    }

    @Test
    public void testGenerateNewAccountNumber_Fallback() throws SQLException {
        // Arrange
        when(mockStatement.executeQuery(anyString())).thenThrow(new SQLException("Database error"));

        // Act
        String accountNo = customerDAO.generateNewAccountNumber();

        // Assert
        assertEquals("Should use fallback account number", "000001", accountNo);
    }

    @Test
    public void testGenerateNewAccountNumber_EmptyTable() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString(1)).thenReturn("000001"); // Database returns padded value

        // Act
        String accountNo = customerDAO.generateNewAccountNumber();

        // Assert
        assertEquals("Should generate first account number", "000001", accountNo);
    }

    @Test
    public void testMapResultSetToCustomer() throws SQLException {
        // Arrange
        when(mockResultSet.getString("account_no")).thenReturn("000001");
        when(mockResultSet.getString("name")).thenReturn("Test Customer");
        when(mockResultSet.getString("nic")).thenReturn("123456789V");
        when(mockResultSet.getString("phone_no")).thenReturn("0771234567");
        when(mockResultSet.getString("email")).thenReturn("test@example.com");
        when(mockResultSet.getString("address")).thenReturn("Test Address");
        when(mockResultSet.getDouble("units_consumed")).thenReturn(150.75);
        when(mockResultSet.getTimestamp("created_date")).thenReturn(new Timestamp(System.currentTimeMillis()));
        when(mockResultSet.getString("created_by")).thenReturn("admin");

        // Act - This tests the private method through getCustomerByAccountNo
        when(mockResultSet.next()).thenReturn(true);
        Customer customer = customerDAO.getCustomerByAccountNo("000001");

        // Assert
        assertNotNull("Customer should not be null", customer);
        assertEquals("Account number should match", "000001", customer.getAccountNo());
        assertEquals("Name should match", "Test Customer", customer.getName());
        assertEquals("NIC should match", "123456789V", customer.getNic());
        assertEquals("Phone number should match", "0771234567", customer.getPhoneNo());
        assertEquals("Email should match", "test@example.com", customer.getEmail());
        assertEquals("Address should match", "Test Address", customer.getAddress());
        assertEquals("Units consumed should match", 150.75, customer.getUnitsConsumed(), 0.01);
        assertNotNull("Created date should not be null", customer.getCreatedDate());
        assertEquals("Created by should match", "admin", customer.getCreatedBy());
    }

    @Test
    public void testGetCustomerByAccountNo_SQLException() throws SQLException {
        // Arrange
        String accountNo = "000001";
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

        // Act
        Customer customer = customerDAO.getCustomerByAccountNo(accountNo);

        // Assert
        assertNull("Customer should be null on SQL exception", customer);
    }

    @Test
    public void testInsertCustomer_SQLException() throws SQLException {
        // Arrange
        Customer customer = new Customer();
        customer.setAccountNo("000001");
        customer.setName("Test Customer");

        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

        // Act
        boolean result = customerDAO.insertCustomer(customer);

        // Assert
        assertFalse("Insert should fail on SQL exception", result);
    }

    // Helper method to mock ResultSet behavior for customers
    private void mockCustomerResultSet() throws SQLException {
        when(mockResultSet.getString("account_no")).thenReturn("000001", "000002");
        when(mockResultSet.getString("name")).thenReturn("Customer One", "Customer Two");
        when(mockResultSet.getString("nic")).thenReturn("123456789V", "987654321V");
        when(mockResultSet.getString("phone_no")).thenReturn("0771234567", "0777654321");
        when(mockResultSet.getString("email")).thenReturn("one@example.com", "two@example.com");
        when(mockResultSet.getString("address")).thenReturn("Address One", "Address Two");
        when(mockResultSet.getDouble("units_consumed")).thenReturn(100.5, 200.75);
        when(mockResultSet.getTimestamp("created_date")).thenReturn(
                new Timestamp(System.currentTimeMillis()),
                new Timestamp(System.currentTimeMillis() - 86400000)
        );
        when(mockResultSet.getString("created_by")).thenReturn("admin", "staff");
    }
}