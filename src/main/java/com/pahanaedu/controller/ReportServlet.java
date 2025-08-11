package com.pahanaedu.controller;

import com.pahanaedu.dao.ReportDAO;
import com.google.gson.Gson;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@WebServlet(name = "ReportServlet", urlPatterns = {"/reports/*"})public class ReportServlet extends HttpServlet {
    private ReportDAO reportDAO;
    private Gson gson = new Gson();

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            reportDAO = new ReportDAO();
        } catch (SQLException e) {
            throw new ServletException("Failed to initialize ReportDAO", e);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        String action = request.getParameter("action");

        try {
            if ("getSalesData".equals(action)) {
                getSalesData(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
            }
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error processing request: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void getSalesData(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ParseException, SQLException {
        String startDateStr = request.getParameter("startDate");
        String endDateStr = request.getParameter("endDate");

        // Handle both cases - with and without time component
        SimpleDateFormat sdf;
        if (startDateStr.contains("T")) {
            sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        } else {
            sdf = new SimpleDateFormat("yyyy-MM-dd");
        }

        Date startDate = sdf.parse(startDateStr);
        Date endDate = sdf.parse(endDateStr);

        // For end date, include the entire day by setting time to 23:59:59
        Calendar cal = Calendar.getInstance();
        cal.setTime(endDate);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        endDate = cal.getTime();

        Map<String, Object> salesData = new HashMap<>();

        // Get sales summary with precise datetime filtering
        Map<String, Object> summary = reportDAO.getSalesSummary(startDate, endDate);
        System.out.println("DEBUG - Summary from DAO: " + summary);
        salesData.put("summary", summary);

        // Get sales by category with precise datetime filtering
        List<Map<String, Object>> byCategory = reportDAO.getSalesByCategory(startDate, endDate);
        salesData.put("byCategory", byCategory);

        // Get top selling items with precise datetime filtering
        List<Map<String, Object>> topItems = reportDAO.getTopSellingItems(startDate, endDate, 10);
        salesData.put("topItems", topItems);

        // Calculate percentages
        double totalRevenue = (double) summary.get("totalRevenue");
        if (totalRevenue > 0) {
            for (Map<String, Object> category : byCategory) {
                double categoryRevenue = (double) category.get("totalRevenue");
                double percentage = (categoryRevenue / totalRevenue) * 100;
                category.put("percentage", Math.round(percentage));
            }
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(gson.toJson(salesData));
    }
}