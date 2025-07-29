package com.pahanaedu.controller;

import com.pahanaedu.dao.ItemDAO;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/manage-categories")
public class AdminCategoryServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(AdminCategoryServlet.class.getName());
    private ItemDAO itemDAO;

    @Override
    public void init() {
        itemDAO = new ItemDAO();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            String action = request.getParameter("action");
            if ("add".equals(action)) {
                String categoryName = request.getParameter("categoryName");

                if (categoryName == null || categoryName.trim().isEmpty()) {
                    out.print("{\"success\": false, \"message\": \"Category name is required\"}");
                    return;
                }

                if (itemDAO.categoryExists(categoryName)) {
                    out.print("{\"success\": false, \"message\": \"Category already exists\"}");
                    return;
                }

                boolean success = itemDAO.addCategory(categoryName);
                out.print("{\"success\": " + success + "}");
            } else {
                out.print("{\"success\": false, \"message\": \"Invalid action\"}");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing category request", e);
            out.print("{\"success\": false, \"message\": \"Error processing request\"}");
        }
    }
}