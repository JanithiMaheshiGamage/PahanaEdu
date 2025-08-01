package com.pahanaedu.controller;

import com.pahanaedu.dao.CategoryDAO;
import org.json.JSONObject;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

@WebServlet("/manage-categories")
@MultipartConfig
public class AdminCategoryServlet extends HttpServlet {
    private CategoryDAO categoryDAO;

    @Override
    public void init() {
        categoryDAO = new CategoryDAO();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        JSONObject jsonResponse = new JSONObject();

        try {
            String action = getParameterFromRequest(request, "action");
            String categoryName = getParameterFromRequest(request, "categoryName");

            System.out.println("[DEBUG] Action: " + action);
            System.out.println("[DEBUG] Category: " + categoryName);

            if (action == null || action.trim().isEmpty()) {
                throw new Exception("No action specified");
            }

            switch (action) {
                case "add":
                    handleAddCategory(categoryName, jsonResponse);
                    break;
                case "delete":
                    handleDeleteCategory(request, jsonResponse);
                    break;
                default:
                    throw new Exception("Invalid action specified");
            }

            jsonResponse.put("success", true);
        } catch (Exception e) {
            jsonResponse.put("success", false);
            jsonResponse.put("message", e.getMessage());
            e.printStackTrace();
        }

        try (PrintWriter out = response.getWriter()) {
            out.print(jsonResponse.toString());
        }
    }

    /**
     * Universal parameter getter that works for both regular and multipart requests
     */
    private String getParameterFromRequest(HttpServletRequest request, String paramName)
            throws IOException, ServletException {
        // First try regular parameters
        String value = request.getParameter(paramName);
        if (value != null) {
            return value;
        }

        // Fall back to multipart
        try {
            Part part = request.getPart(paramName);
            if (part != null) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(part.getInputStream()))) {
                    return reader.readLine();
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading multipart parameter: " + e.getMessage());
        }
        return null;
    }

    private void handleAddCategory(String categoryName, JSONObject jsonResponse) throws Exception {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            throw new Exception("Category name is required");
        }

        categoryName = categoryName.trim();

        if (categoryDAO.categoryExists(categoryName)) {
            throw new Exception("Category '" + categoryName + "' already exists");
        }

        int categoryId = categoryDAO.addCategory(categoryName);
        if (categoryId <= 0) {
            throw new Exception("Failed to add category to database");
        }

        jsonResponse.put("categoryId", categoryId);
        jsonResponse.put("categoryName", categoryName);
    }

    private void handleDeleteCategory(HttpServletRequest request, JSONObject jsonResponse) throws Exception {
        String categoryIdStr = getParameterFromRequest(request, "categoryId");

        if (categoryIdStr == null || categoryIdStr.trim().isEmpty()) {
            throw new Exception("Category ID is required");
        }

        int categoryId = Integer.parseInt(categoryIdStr);

        if (!categoryDAO.deleteCategory(categoryId)) {
            throw new Exception("Failed to delete category. It may not exist or has associated items.");
        }
    }
}