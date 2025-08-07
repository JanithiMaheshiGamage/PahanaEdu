package com.pahanaedu.controller;

import com.pahanaedu.dao.BillDAO;
import com.pahanaedu.dao.CustomerDAO;
import com.pahanaedu.dao.ItemDAO;
import com.pahanaedu.model.*;
import com.google.gson.Gson;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/billing")
public class BillingServlet extends HttpServlet {
    private CustomerDAO customerDAO;
    private ItemDAO itemDAO;
    private BillDAO billDAO;
    private Gson gson = new Gson();

    @Override
    public void init() throws ServletException {
        super.init();
        customerDAO = new CustomerDAO();
        itemDAO = new ItemDAO();
        try {
            billDAO = new BillDAO();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("checkCustomer".equals(action)) {
            try {
                checkCustomer(request, response);
            } catch (SQLException ex) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Database error occurred");
                response.getWriter().write(gson.toJson(errorResponse));
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid action");
            response.getWriter().write(gson.toJson(errorResponse));
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        try {
            switch (action) {
                case "checkCustomer":
                    checkCustomer(request, response);
                    break;
                case "selectCustomer":
                    selectCustomer(request, response);
                    break;
                case "clearCustomer":
                    clearCustomer(request, response);
                    break;
                case "addCustomer":
                    addCustomer(request, response);
                    break;
                case "generateBill":
                    generateBill(request, response);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
            }
        } catch (SQLException ex) {
            throw new ServletException(ex);
        }
    }

    private void checkCustomer(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String phone = request.getParameter("phone");
        if (phone == null || phone.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Phone number is required\"}");
            return;
        }

        try {
            List<Customer> customers = customerDAO.getCustomersByPhone(phone);

            Map<String, Object> responseData = new HashMap<>();
            if (customers.isEmpty()) {
                responseData.put("error", "No customers found with this phone number");
            } else {
                responseData.put("customers", customers);
            }

            response.getWriter().write(new Gson().toJson(responseData));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Server error occurred\"}");
            e.printStackTrace();
        }
    }

    private void selectCustomer(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Customer customer = gson.fromJson(request.getReader(), Customer.class);
        request.getSession().setAttribute("selectedCustomer", customer);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private void clearCustomer(HttpServletRequest request, HttpServletResponse response) {
        request.getSession().removeAttribute("selectedCustomer");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private void addCustomer(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // Parse the JSON request
            Customer customer = gson.fromJson(request.getReader(), Customer.class);

            // Generate account number
            customer.setAccountNo(customerDAO.generateNewAccountNumber());

            // Set created by (from session)
            HttpSession session = request.getSession();
            String username = (String) session.getAttribute("username");
            customer.setCreatedBy(username != null ? username : "system");

            // Set default values
            customer.setUnitsConsumed(0);

            // Validate required fields
            if (customer.getName() == null || customer.getName().trim().isEmpty() ||
                    customer.getPhoneNo() == null || customer.getPhoneNo().trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Name and phone number are required\"}");
                return;
            }

            // Check if phone number already exists
            if (customerDAO.phoneNoExists(customer.getPhoneNo())) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                response.getWriter().write("{\"error\":\"Phone number already exists\"}");
                return;
            }

            // Insert customer
            boolean success = customerDAO.insertCustomer(customer);

            if (success) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(customer));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\":\"Failed to save customer to database\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Server error: " + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }

    private Map<String, String> validateBill(Bill bill) throws SQLException {
        Map<String, String> errors = new HashMap<>();

        // 1. Validate customer
        if (bill.getCustomerId() == null || bill.getCustomerId().trim().isEmpty()) {
            errors.put("customer", "Customer not selected");
        } else if (customerDAO.getCustomerByAccountNo(bill.getCustomerId()) == null) {
            errors.put("customer", "Invalid customer selected");
        }

        // 2. Validate items
        if (bill.getItems() == null || bill.getItems().isEmpty()) {
            errors.put("items", "No items in the bill");
        } else {
            for (BillItem item : bill.getItems()) {
                Item dbItem = itemDAO.getItemById(item.getItemId());
                if (dbItem == null) {
                    errors.put("items", "Invalid item in cart: " + item.getItemId());
                    break;
                }
                if (item.getQuantity() <= 0) {
                    errors.put("items", "Invalid quantity for item: " + item.getItemId());
                    break;
                }
                if (dbItem.getStockQty() < item.getQuantity()) {
                    errors.put("items", "Insufficient stock for item: " + dbItem.getName());
                    break;
                }
            }
        }

        // 3. Validate payment
        if (!"cash".equals(bill.getPaymentMethod()) && !"card".equals(bill.getPaymentMethod())) {
            errors.put("payment", "Invalid payment method");
        }
        if (bill.getTotalAmount() <= 0) {
            errors.put("total", "Invalid total amount");
        }

        return errors;
    }

    private void generateBill(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            Bill bill = gson.fromJson(request.getReader(), Bill.class);
            HttpSession session = request.getSession();

            // Get user ID from session (not username)
            Integer userId = (Integer) session.getAttribute("userId");
            if (userId == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\":\"User not authenticated\"}");
                return;
            }

            // Set createdBy with the numeric user ID
            bill.setCreatedBy(userId);

            // Rest of your validation and bill creation logic...
            Map<String, String> errors = validateBill(bill);

            if (!errors.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(errors));
                return;
            }

            boolean success = billDAO.createBill(bill);

            if (success) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("success", true);
                responseData.put("message", "Bill created successfully");
                responseData.put("billNo", bill.getBillNo());
                response.getWriter().write(gson.toJson(responseData));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\":\"Failed to create bill\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Server error: " + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }
}