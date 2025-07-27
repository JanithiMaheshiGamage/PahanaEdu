package com.pahanaedu.controller;

import com.pahanaedu.dao.UserDAO;
import com.pahanaedu.model.User;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/manage-users")
public class AdminUserServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(AdminUserServlet.class.getName());
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
            logger.log(Level.SEVERE, "Error loading users", e);
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
            if (action == null) {
                throw new Exception("No action specified");
            }

            // Handle delete action first since it doesn't need User object
            else if ("delete".equals(action)) {
                String userId = request.getParameter("userId");
                logger.info("Delete request received for user ID: " + userId);

                if (userId == null || userId.isEmpty()) {
                    session.setAttribute("error", "User ID is required for deletion");
                    logger.warning("Delete attempt with empty user ID");
                } else {
                    try {
                        int id = Integer.parseInt(userId);
                        logger.info("Attempting to delete user with ID: " + id);
                        boolean success = userDAO.deleteUser(id);

                        if (success) {
                            session.setAttribute("success", "User deleted successfully!");
                            logger.info("Successfully deleted user ID: " + id);
                        } else {
                            session.setAttribute("error", "Failed to delete user");
                            logger.warning("Failed to delete user ID: " + id);
                        }
                    } catch (NumberFormatException e) {
                        session.setAttribute("error", "Invalid user ID format");
                        logger.log(Level.SEVERE, "Invalid user ID format: " + userId, e);
                    } catch (Exception e) {
                        session.setAttribute("error", "Error deleting user: " + e.getMessage());
                        logger.log(Level.SEVERE, "Error deleting user ID: " + userId, e);
                    }
                }
                response.sendRedirect(request.getContextPath() + "/admin_manage_users.jsp");
                return;
            }

            // Handle add/update actions
            User user = new User();
            user.setFullName(request.getParameter("fullname"));
            user.setUsername(request.getParameter("username"));
            user.setEmail(request.getParameter("email"));
            user.setRole(request.getParameter("role"));
            user.setStatus("on".equals(request.getParameter("status")));
            user.setEmployeeNo(request.getParameter("employeeNo"));

            logger.info("Processing action: " + action + " for user: " + user.getUsername()
                    + " Employee No: " + user.getEmployeeNo());

            if ("add".equals(action)) {

                // Set password only for new users
                user.setPassword(request.getParameter("password"));
                // Validate for new user
                if (userDAO.usernameExists(user.getUsername())) {
                    session.setAttribute("error", "Username already exists");
                    response.sendRedirect(request.getContextPath() + "/admin_manage_users.jsp");
                    return;
                }

                if (userDAO.emailExists(user.getEmail())) {
                    session.setAttribute("error", "Email already exists");
                    response.sendRedirect(request.getContextPath() + "/admin_manage_users.jsp");
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
                if (userId == null || userId.isEmpty()) {
                    throw new Exception("User ID is required for update");
                }

                user.setId(Integer.parseInt(userId));
                User existingUser = userDAO.getUserById(user.getId());
                if (existingUser == null) {
                    session.setAttribute("error", "User not found");
                    response.sendRedirect(request.getContextPath() + "/admin_manage_users.jsp");
                    return;
                }

                // Keep the existing password (don't update it)
                user.setPassword(existingUser.getPassword());

                // Check if username is being changed to an existing one
                if (!existingUser.getUsername().equals(user.getUsername()) &&
                        userDAO.usernameExists(user.getUsername())) {
                    session.setAttribute("error", "Username already exists");
                    response.sendRedirect(request.getContextPath() + "/admin_manage_users.jsp");
                    return;
                }

                // Check if email is being changed to an existing one
                if (!existingUser.getEmail().equals(user.getEmail()) &&
                        userDAO.emailExists(user.getEmail())) {
                    session.setAttribute("error", "Email already exists");
                    response.sendRedirect(request.getContextPath() + "/admin_manage_users.jsp");
                    return;
                }

                userDAO.updateUser(user);
                session.setAttribute("success", "User updated successfully!");
            }

            response.sendRedirect(request.getContextPath() + "/admin_manage_users.jsp");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing request", e);
            session.setAttribute("error", "Error: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin_manage_users.jsp");
        }
    }
}