package com.pahanaedu.controller;

import com.pahanaedu.dao.ItemDAO;
import com.pahanaedu.model.Item;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/manage-items")
public class AdminItemServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(AdminItemServlet.class.getName());
    private ItemDAO itemDAO;

    @Override
    public void init() {
        itemDAO = new ItemDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String search = request.getParameter("search");

        try {
            if (search != null && !search.trim().isEmpty()) {
                request.setAttribute("items", itemDAO.searchItems(search.trim()));
            } else {
                request.setAttribute("items", itemDAO.getAllItems());
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading items", e);
            session.setAttribute("error", "Error loading items: " + e.getMessage());
        }

        request.getRequestDispatcher("/admin_manage_items.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession();
        String action = request.getParameter("action");

        try {
            if (action == null) {
                throw new Exception("No action specified");
            }

            switch (action) {
                case "add":
                    handleAddItem(request, session);
                    break;
                case "update":
                    handleUpdateItem(request, session);
                    break;
                case "delete":
                    handleDeleteItem(request, session);
                    break;
                default:
                    throw new Exception("Invalid action specified");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing request", e);
            session.setAttribute("error", e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/manage-items");
    }

    private void handleAddItem(HttpServletRequest request, HttpSession session) throws Exception {
        // Validate and create item
        Item item = validateAndCreateItem(request, session);

        // Check if item name already exists
        if (itemDAO.itemNameExists(item.getName())) {
            throw new Exception("Item with name '" + item.getName() + "' already exists");
        }

        // Add item to database
        if (!itemDAO.addItem(item)) {
            throw new Exception("Failed to add item. Please try again.");
        }

        session.setAttribute("success", "Item added successfully!");
    }

    private void handleUpdateItem(HttpServletRequest request, HttpSession session) throws Exception {
        // Validate item ID
        String itemIdStr = request.getParameter("itemId");
        if (itemIdStr == null || itemIdStr.trim().isEmpty()) {
            throw new Exception("Item ID is required for update");
        }

        // Validate and create item
        Item item = validateAndCreateItem(request, session);
        item.setItemId(Integer.parseInt(itemIdStr));

        // Update item in database
        if (!itemDAO.updateItem(item)) {
            throw new Exception("Failed to update item. It may not exist in the system.");
        }

        session.setAttribute("success", "Item updated successfully!");
    }

    private void handleDeleteItem(HttpServletRequest request, HttpSession session) throws Exception {
        // Validate item ID
        String itemIdStr = request.getParameter("itemId");
        if (itemIdStr == null || itemIdStr.trim().isEmpty()) {
            throw new Exception("Item ID is required for deletion");
        }

        int itemId = Integer.parseInt(itemIdStr);

        // Delete item from database
        if (!itemDAO.deleteItem(itemId)) {
            throw new Exception("Failed to delete item. It may not exist in the system.");
        }

        session.setAttribute("success", "Item deleted successfully!");
    }

    private Item validateAndCreateItem(HttpServletRequest request, HttpSession session) throws Exception {
        // Validate required fields
        String name = request.getParameter("name");
        String categoryIdStr = request.getParameter("categoryId");
        String priceStr = request.getParameter("price");
        String stockQtyStr = request.getParameter("stockQty");

        if (name == null || name.trim().isEmpty()) {
            throw new Exception("Item name is required");
        }
        if (categoryIdStr == null || categoryIdStr.trim().isEmpty()) {
            throw new Exception("Category is required");
        }
        if (priceStr == null || priceStr.trim().isEmpty()) {
            throw new Exception("Price is required");
        }
        if (stockQtyStr == null || stockQtyStr.trim().isEmpty()) {
            throw new Exception("Stock quantity is required");
        }

        // Parse numeric values
        double price;
        int stockQty;
        int categoryId;

        try {
            price = Double.parseDouble(priceStr);
            if (price <= 0) throw new Exception("Price must be greater than 0");

            stockQty = Integer.parseInt(stockQtyStr);
            if (stockQty < 0) throw new Exception("Stock quantity cannot be negative");

            categoryId = Integer.parseInt(categoryIdStr);
        } catch (NumberFormatException e) {
            throw new Exception("Invalid numeric value in form");
        }

        // Create item object
        Item item = new Item();
        item.setName(name.trim());
        item.setCategoryId(categoryId);
        item.setPrice(price);
        item.setStockQty(stockQty);
        item.setDescription(request.getParameter("description"));

        // Set createdBy from session
        String username = (String) session.getAttribute("username");
        if (username == null || username.trim().isEmpty()) {
            throw new Exception("User session expired. Please login again.");
        }
        item.setCreatedBy(username);

        return item;
    }
}