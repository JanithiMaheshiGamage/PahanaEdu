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

    private void generateBill(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        Bill bill = gson.fromJson(request.getReader(), Bill.class);
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        bill.setCreatedBy(user.getId());  // Changed from getUserId() to getId()

        boolean success = billDAO.createBill(bill);

        if (success) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("{\"success\": true}");
        } else {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to generate bill");
        }
    }
}