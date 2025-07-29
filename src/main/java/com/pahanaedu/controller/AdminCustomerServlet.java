package com.pahanaedu.controller;

import com.pahanaedu.dao.CustomerDAO;
import com.pahanaedu.model.Customer;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/manage-customers")
public class AdminCustomerServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(AdminCustomerServlet.class.getName());
    private CustomerDAO customerDAO;

    @Override
    public void init() {
        customerDAO = new CustomerDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String search = request.getParameter("search");

        try {
            List<Customer> customers;
            if (search != null && !search.trim().isEmpty()) {
                customers = customerDAO.searchCustomers(search.trim());
            } else {
                customers = customerDAO.getAllCustomers();
            }
            request.setAttribute("customers", customers);
        } catch (Exception e) {
            session.setAttribute("error", "Error loading customers: " + e.getMessage());
            logger.log(Level.SEVERE, "Error loading customers", e);
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher("/admin_manage_customers.jsp");
        dispatcher.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();

        try {
            String action = request.getParameter("action");
            if (action == null) {
                throw new Exception("No action specified");
            }

            if ("delete".equals(action)) {
                handleDeleteAction(request, session);
                response.sendRedirect(request.getContextPath() + "/admin_manage_customers.jsp");
                return;
            }

            // Validate input parameters first
            String nic = request.getParameter("nic");
            String phoneNo = request.getParameter("phoneNo");

            // Validate NIC (12 digits)
            if (nic == null || !nic.matches("\\d{12}")) {
                session.setAttribute("error", "NIC must contain exactly 12 digits");
                response.sendRedirect(request.getContextPath() + "/admin_manage_customers.jsp");
                return;
            }

            // Validate Phone Number (10 digits)
            if (phoneNo == null || !phoneNo.matches("\\d{10}")) {
                session.setAttribute("error", "Phone number must contain exactly 10 digits");
                response.sendRedirect(request.getContextPath() + "/admin_manage_customers.jsp");
                return;
            }

            Customer customer = createCustomerFromRequest(request, session);

            if ("add".equals(action)) {
                handleAddAction(customer, session);
            } else if ("update".equals(action)) {
                handleUpdateAction(request, customer, session);
            }

            response.sendRedirect(request.getContextPath() + "/admin_manage_customers.jsp");
        } catch (NumberFormatException e) {
            session.setAttribute("error", "Invalid units consumed value");
            response.sendRedirect(request.getContextPath() + "/admin_manage_customers.jsp");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing request", e);
            session.setAttribute("error", "Error processing request: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"));
            response.sendRedirect(request.getContextPath() + "/admin_manage_customers.jsp");
        }
    }

    private void handleDeleteAction(HttpServletRequest request, HttpSession session) throws Exception {
        String accountNo = request.getParameter("accountNo");
        logger.info("Delete request received for account: " + accountNo);

        if (accountNo == null || accountNo.isEmpty()) {
            session.setAttribute("error", "Account number is required for deletion");
        } else {
            boolean success = customerDAO.deleteCustomer(accountNo);
            if (success) {
                session.setAttribute("success", "Customer deleted successfully!");
            } else {
                session.setAttribute("error", "Failed to delete customer");
            }
        }
    }

    private Customer createCustomerFromRequest(HttpServletRequest request, HttpSession session) throws Exception {
        Customer customer = new Customer();
        customer.setName(request.getParameter("name"));
        customer.setNic(request.getParameter("nic"));
        customer.setPhoneNo(request.getParameter("phoneNo"));
        customer.setEmail(request.getParameter("email"));
        customer.setAddress(request.getParameter("address"));

        // Add validation for required fields
        if (customer.getName() == null || customer.getName().trim().isEmpty()) {
            throw new Exception("Name is required");
        }
        if (customer.getNic() == null || customer.getNic().trim().isEmpty()) {
            throw new Exception("NIC is required");
        }
        if (customer.getPhoneNo() == null || customer.getPhoneNo().trim().isEmpty()) {
            throw new Exception("Phone number is required");
        }
        if (customer.getAddress() == null || customer.getAddress().trim().isEmpty()) {
            throw new Exception("Address is required");
        }

        customer.setCreatedBy((String) session.getAttribute("username"));
        return customer;
    }

    private void handleAddAction(Customer customer, HttpSession session) throws Exception {
        // Generate new account number
        String newAccountNo = customerDAO.generateNewAccountNumber();
        customer.setAccountNo(newAccountNo);

        // Set default units consumed
        customer.setUnitsConsumed(0);

        logger.info("Attempting to add customer: " + customer.toString());

        if (customerDAO.nicExists(customer.getNic())) {
            throw new Exception("NIC already exists");
        }
        if (customerDAO.phoneNoExists(customer.getPhoneNo())) {
            throw new Exception("Phone number already exists");
        }

        boolean success = customerDAO.insertCustomer(customer);
        if (success) {
            session.setAttribute("success", "Customer added successfully with account #" + newAccountNo);
        } else {
            throw new Exception("Failed to add customer - check database logs. Account #: " + newAccountNo);
        }
    }


    private void handleUpdateAction(HttpServletRequest request, Customer customer, HttpSession session) throws Exception {
        customer.setAccountNo(request.getParameter("accountNo"));
        Customer existingCustomer = customerDAO.getCustomerByAccountNo(customer.getAccountNo());

        if (!existingCustomer.getNic().equals(customer.getNic()) &&
                customerDAO.nicExists(customer.getNic())) {
            throw new Exception("NIC already exists");
        }
        if (!existingCustomer.getPhoneNo().equals(customer.getPhoneNo()) &&
                customerDAO.phoneNoExists(customer.getPhoneNo())) {
            throw new Exception("Phone number already exists");
        }

        boolean success = customerDAO.updateCustomer(customer);
        if (success) {
            session.setAttribute("success", "Customer updated successfully!");
        } else {
            throw new Exception("Failed to update customer");
        }
    }


}

