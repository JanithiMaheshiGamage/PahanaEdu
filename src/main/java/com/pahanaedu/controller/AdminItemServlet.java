package com.pahanaedu.controller;

import com.pahanaedu.dao.ItemDAO;
import com.pahanaedu.model.Item;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;
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

        // Handle AJAX request for single item data
        if ("get".equals(request.getParameter("action"))) {
            handleGetItemRequest(request, response);
            return;
        }

        // Handle regular page load with search functionality
        try {
            String search = request.getParameter("search");
            List<Item> items;

            if (search != null && !search.trim().isEmpty()) {
                items = itemDAO.searchItems(search.trim());
            } else {
                items = itemDAO.getAllItems();
            }

            request.setAttribute("items", items);
            request.setAttribute("categories", itemDAO.getAllCategories());
        } catch (Exception e) {
            session.setAttribute("error", "Error loading items: " + e.getMessage());
            logger.log(Level.SEVERE, "Error loading items", e);
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher("/admin_manage_items.jsp");
        dispatcher.forward(request, response);
    }

    private void handleGetItemRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            int itemId = Integer.parseInt(request.getParameter("itemId"));
            Item item = itemDAO.getItemById(itemId);

            if (item == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Item not found");
                return;
            }

            // Create JSON response
            String jsonResponse = String.format(
                    "{\"itemId\":%d,\"name\":\"%s\",\"categoryName\":\"%s\",\"price\":%.2f,\"stockQty\":%d,\"description\":\"%s\"}",
                    item.getItemId(),
                    escapeJson(item.getName()),
                    escapeJson(item.getCategoryName()),
                    item.getPrice(),
                    item.getStockQty(),
                    escapeJson(item.getDescription() != null ? item.getDescription() : "")
            );

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(jsonResponse);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid item ID");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing item request", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing request");
        }
    }

    // Helper method to escape JSON strings
    private String escapeJson(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
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
                response.sendRedirect(request.getContextPath() + "/admin_manage_items.jsp");
                return;
            }

            Item item = createItemFromRequest(request, session);

            if ("add".equals(action)) {
                handleAddAction(item, session);
            } else if ("update".equals(action)) {
                handleUpdateAction(request, item, session);
            }

            response.sendRedirect(request.getContextPath() + "/admin_manage_items.jsp");
        } catch (NumberFormatException e) {
            session.setAttribute("error", "Invalid numeric value");
            response.sendRedirect(request.getContextPath() + "/admin_manage_items.jsp");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing request", e);
            session.setAttribute("error", "Error processing request: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"));
            response.sendRedirect(request.getContextPath() + "/admin_manage_items.jsp");
        }
    }

    private void handleDeleteAction(HttpServletRequest request, HttpSession session) throws Exception {
        String itemId = request.getParameter("itemId");
        logger.info("Delete request received for item: " + itemId);

        if (itemId == null || itemId.isEmpty()) {
            session.setAttribute("error", "Item ID is required for deletion");
        } else {
            boolean success = itemDAO.deleteItem(Integer.parseInt(itemId));
            if (success) {
                session.setAttribute("success", "Item deleted successfully!");
            } else {
                session.setAttribute("error", "Failed to delete item");
            }
        }
    }

    private Item createItemFromRequest(HttpServletRequest request, HttpSession session) throws Exception {
        Item item = new Item();
        item.setName(request.getParameter("name"));

        // Get category ID from name
        String categoryName = request.getParameter("category");
        int categoryId = itemDAO.getCategoryIdByName(categoryName);
        if (categoryId == -1) {
            throw new Exception("Invalid category selected");
        }
        item.setCategoryId(categoryId);

        item.setPrice(Double.parseDouble(request.getParameter("price")));
        item.setStockQty(Integer.parseInt(request.getParameter("stockQty")));
        item.setDescription(request.getParameter("description"));
        item.setCreatedBy((String) session.getAttribute("username"));

        // Validation
        if (item.getName() == null || item.getName().trim().isEmpty()) {
            throw new Exception("Item name is required");
        }
        if (item.getPrice() < 0) {
            throw new Exception("Price must be positive");
        }
        if (item.getStockQty() < 0) {
            throw new Exception("Stock quantity must be positive");
        }

        return item;
    }

    private void handleAddAction(Item item, HttpSession session) throws Exception {
        logger.info("Attempting to add item: " + item.toString());

        boolean success = itemDAO.insertItem(item);
        if (success) {
            session.setAttribute("success", "Item added successfully!");
        } else {
            throw new Exception("Failed to add item - check database logs");
        }
    }

    private void handleUpdateAction(HttpServletRequest request, Item item, HttpSession session) throws Exception {
        item.setItemId(Integer.parseInt(request.getParameter("itemId")));

        boolean success = itemDAO.updateItem(item);
        if (success) {
            session.setAttribute("success", "Item updated successfully!");
        } else {
            throw new Exception("Failed to update item");
        }
    }
}