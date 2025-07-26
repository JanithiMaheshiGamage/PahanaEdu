package com.pahanaedu.controller;

import com.pahanaedu.dao.UserDAO;
import com.pahanaedu.model.User;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
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
        HttpSession session = request.getSession();
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
            session.setAttribute("error", "Error loading users: " + e.getMessage());
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher("/admin_manage_users.jsp");
        dispatcher.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();

        try {
            String action = request.getParameter("action");
            User user = new User();
            user.setFullName(request.getParameter("fullname"));
            user.setUsername(request.getParameter("username"));
            user.setEmail(request.getParameter("email"));
            user.setPassword(request.getParameter("password"));
            user.setRole(request.getParameter("role"));
            user.setStatus("on".equals(request.getParameter("status")));

            if ("add".equals(action)) {
                // Validate for new user
                if (userDAO.usernameExists(user.getUsername())) {
                    session.setAttribute("error", "Username already exists");
                    response.sendRedirect("admin_manage_users.jsp");
                    return;
                }

                if (userDAO.emailExists(user.getEmail())) {
                    session.setAttribute("error", "Email already exists");
                    response.sendRedirect("admin_manage_users.jsp");
                    return;
                }

                boolean success = userDAO.insertUser(user);
                if (success) {
                    session.setAttribute("success", "User added successfully!");
                } else {
                    session.setAttribute("error", "Failed to add user");
                }
            }
            else if ("update".equals(action)) {
                String userId = request.getParameter("userId");
                if (userId != null && !userId.isEmpty()) {
                    user.setId(Integer.parseInt(userId));

                    // Get existing user data for validation
                    User existingUser = userDAO.getUserById(user.getId());
                    if (existingUser == null) {
                        session.setAttribute("error", "User not found");
                        response.sendRedirect("admin_manage_users.jsp");
                        return;
                    }

                    // Check if username is being changed to an existing one
                    if (!existingUser.getUsername().equals(user.getUsername()) &&
                            userDAO.usernameExists(user.getUsername())) {
                        session.setAttribute("error", "Username already exists");
                        response.sendRedirect("admin_manage_users.jsp");
                        return;
                    }

                    // Check if email is being changed to an existing one
                    if (!existingUser.getEmail().equals(user.getEmail()) &&
                            userDAO.emailExists(user.getEmail())) {
                        session.setAttribute("error", "Email already exists");
                        response.sendRedirect("admin_manage_users.jsp");
                        return;
                    }

                    userDAO.updateUser(user);
                    session.setAttribute("success", "User updated successfully!");
                }
            }

            response.sendRedirect("admin_manage_users.jsp");
        } catch (Exception e) {
            session.setAttribute("error", "Error: " + e.getMessage());
            response.sendRedirect("admin_manage_users.jsp");
        }
    }
}