package com.pahanaedu.controller;

import com.pahanaedu.dao.UserDAO;
import com.pahanaedu.model.User;
import org.mindrot.jbcrypt.BCrypt;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.logging.Logger;

@WebServlet("/changePassword")
public class ChangePasswordServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(ChangePasswordServlet.class.getName());
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        String username = (String) session.getAttribute("username"); // Get username from session instead of parameter
        String currentPwd = request.getParameter("currentPassword");
        String newPwd = request.getParameter("newPassword");
        String confirmPwd = request.getParameter("confirmPassword");

        try {
            // Validate inputs
            if (username == null) {
                session.setAttribute("pwdError", "Session expired. Please login again.");
                response.sendRedirect("login.jsp");
                return;
            }

            if (currentPwd == null || currentPwd.trim().isEmpty() ||
                    newPwd == null || newPwd.trim().isEmpty() ||
                    confirmPwd == null || confirmPwd.trim().isEmpty()) {
                session.setAttribute("pwdError", "All fields are required.");
                response.sendRedirect("my_profile.jsp");
                return;
            }

            if (!newPwd.equals(confirmPwd)) {
                session.setAttribute("pwdError", "New password and confirmation do not match.");
                response.sendRedirect("my_profile.jsp");
                return;
            }

            if (currentPwd.equals(newPwd)) {
                session.setAttribute("pwdError", "New password must be different from current password.");
                response.sendRedirect("my_profile.jsp");
                return;
            }

            if (!isPasswordStrong(newPwd)) {
                session.setAttribute("pwdError", "Password must be at least 8 characters with uppercase, lowercase, and a number.");
                response.sendRedirect("my_profile.jsp");
                return;
            }

            // Get user from DB
            User user = userDAO.getUserByUsername(username);

            if (user == null) {
                session.setAttribute("pwdError", "User not found.");
                response.sendRedirect("my_profile.jsp");
                return;
            }

            if (!BCrypt.checkpw(currentPwd, user.getPassword())) {
                session.setAttribute("pwdError", "Current password is incorrect.");
                response.sendRedirect("my_profile.jsp");
                return;
            }

            // Hash new password
            String hashedPwd = BCrypt.hashpw(newPwd, BCrypt.gensalt(12));

            boolean updated = userDAO.updateUserPassword(username, hashedPwd);
            if (updated) {
                session.setAttribute("pwdMsg", "Password updated successfully.");
                // Clear any previous error
                session.removeAttribute("pwdError");
            } else {
                session.setAttribute("pwdError", "Failed to update password. Please try again.");
            }

        } catch (Exception e) {
            logger.severe("Error changing password: " + e.getMessage());
            session.setAttribute("pwdError", "A system error occurred. Please try again later.");
        }

        response.sendRedirect("my_profile.jsp");
    }

    private boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isLowerCase(c)) hasLower = true;
            if (Character.isDigit(c)) hasDigit = true;

            if (hasUpper && hasLower && hasDigit) {
                return true;
            }
        }

        return false;
    }
}