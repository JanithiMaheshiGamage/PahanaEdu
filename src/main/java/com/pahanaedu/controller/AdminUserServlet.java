package com.pahanaedu.controller;

import com.pahanaedu.dao.UserDAO;
import com.pahanaedu.model.User;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/admin/manage-users")
public class AdminUserServlet extends HttpServlet {
    private UserDAO userDAO;

    @Override
    public void init() {
        userDAO = new UserDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String search = request.getParameter("search");
        try {
            List<User> users;
            if (search != null && !search.trim().isEmpty()) {
                users = userDAO.searchUsers(search.trim());
            } else {
                users = userDAO.getAllUsers();
            }
            request.setAttribute("users", users);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Unable to load users.");
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher("/admin_manage_users.jsp");
        dispatcher.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        String idStr = request.getParameter("id");
        String fullName = request.getParameter("fullName");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String role = request.getParameter("role");
        boolean status = "on".equals(request.getParameter("status"));

        User user = new User();
        user.setFullName(fullName);
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);
        user.setStatus(status);

        try {
            if ("update".equals(action) && idStr != null && !idStr.isEmpty()) {
                user.setId(Integer.parseInt(idStr));
                userDAO.updateUser(user);
            } else {
                userDAO.insertUser(user);
            }
            response.sendRedirect("manage-users");
        } catch (Exception e) { // or just catch Exception
            e.printStackTrace();
            request.setAttribute("error", "Database error: " + e.getMessage());
            doGet(request, response);
        }
    }
}
