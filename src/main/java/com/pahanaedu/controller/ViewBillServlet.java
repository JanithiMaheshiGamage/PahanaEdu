package com.pahanaedu.controller;

import com.pahanaedu.dao.BillDAO;
import com.pahanaedu.dao.CustomerDAO;
import com.pahanaedu.model.Bill;
import com.pahanaedu.model.BillItem;
import com.pahanaedu.model.Customer;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/view-bill")
public class ViewBillServlet extends HttpServlet {
    private BillDAO billDAO;
    private CustomerDAO customerDAO;

    @Override
    public void init() throws ServletException {
        try {
            super.init();
            billDAO = new BillDAO();  // This can throw SQLException
            customerDAO = new CustomerDAO();
        } catch (SQLException e) {
            throw new ServletException("Failed to initialize DAO objects", e);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String billIdParam = request.getParameter("billId");
        if (billIdParam == null || billIdParam.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bill ID is required");
            return;
        }

        try {
            int billId = Integer.parseInt(billIdParam);
            Bill bill = billDAO.getBillById(billId);  // This can throw SQLException

            if (bill == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Bill not found");
                return;
            }

            // Check permissions
            HttpSession session = request.getSession(false);
            String role = (String) session.getAttribute("role");
            Integer userId = (Integer) session.getAttribute("userId");

            if (!"admin".equals(role) && !userId.equals(bill.getCreatedBy())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
                return;
            }

            Customer customer = customerDAO.getCustomerByAccountNo(bill.getCustomerId());

            // Use items already loaded by getBillById()
            List<BillItem> items = bill.getItems();

            request.setAttribute("bill", bill);
            request.setAttribute("customer", customer);
            request.setAttribute("items", items);

            request.getRequestDispatcher("/WEB-INF/view_bill.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Bill ID format");
        }
    }
}