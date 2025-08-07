package com.pahanaedu.controller;

import com.pahanaedu.dao.ItemDAO;
import com.pahanaedu.model.Item;
import com.google.gson.Gson;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/search-items")
public class SearchItemsServlet extends HttpServlet {
    private ItemDAO itemDAO;
    private Gson gson = new Gson();

    @Override
    public void init() throws ServletException {
        super.init();
        itemDAO = new ItemDAO();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String term = request.getParameter("term");
        if (term == null || term.trim().isEmpty()) {
            response.getWriter().write("{\"items\":[]}");
            return;
        }

        try {
            List<Item> items = itemDAO.searchItems(term);
            response.getWriter().write(gson.toJson(new ItemSearchResponse(items)));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Server error occurred\"}");
            e.printStackTrace();
        }
    }

    // Helper class for JSON response structure
    private static class ItemSearchResponse {
        private List<Item> items;

        public ItemSearchResponse(List<Item> items) {
            this.items = items;
        }

        public List<Item> getItems() {
            return items;
        }
    }
}