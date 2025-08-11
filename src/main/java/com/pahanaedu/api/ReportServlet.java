package com.pahanaedu.api;

import com.pahanaedu.dao.ReportDAO;
import com.pahanaedu.util.DBConnection;
import com.google.gson.Gson;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "ReportServlet", urlPatterns = {"/api/reports/*"})
public class ReportServlet extends HttpServlet {
    private ReportDAO reportDAO;
    private final Gson gson = new Gson();

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            Connection connection = DBConnection.getConnection();
            this.reportDAO = new ReportDAO(connection);
        } catch (SQLException e) {
            throw new ServletException("Failed to initialize ReportDAO", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();
        if (pathInfo == null) pathInfo = "";

        try {
            String startDate = request.getParameter("startDate");
            String endDate = request.getParameter("endDate");

            switch(pathInfo) {
                case "/sales":
                    handleSalesReport(response, startDate, endDate);
                    break;
                case "/inventory":
                    handleInventoryReport(response);
                    break;
                case "/customers":
                    handleCustomerReport(response, startDate, endDate);
                    break;
                case "/popular-items":
                    handlePopularItemsReport(response, startDate, endDate);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void handleSalesReport(HttpServletResponse response, String startDate, String endDate)
            throws SQLException, IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("dailySales", reportDAO.getDailySales(startDate, endDate));
        data.put("paymentMethods", reportDAO.getPaymentMethodDistribution(startDate, endDate));
        response.getWriter().write(gson.toJson(data));
    }

    private void handleInventoryReport(HttpServletResponse response)
            throws SQLException, IOException {
        Map<String, Object> data = reportDAO.getInventoryByCategory();
        response.getWriter().write(gson.toJson(data));
    }

    private void handleCustomerReport(HttpServletResponse response, String startDate, String endDate)
            throws SQLException, IOException {
        Map<String, Object> data = reportDAO.getCustomerAcquisition(startDate, endDate);
        response.getWriter().write(gson.toJson(data));
    }

    private void handlePopularItemsReport(HttpServletResponse response, String startDate, String endDate)
            throws SQLException, IOException {
        Map<String, Object> data = reportDAO.getPopularItems(startDate, endDate);
        response.getWriter().write(gson.toJson(data));
    }

    @Override
    public void destroy() {
        super.destroy();
        if (reportDAO != null) {
            reportDAO.close();
        }
    }
}