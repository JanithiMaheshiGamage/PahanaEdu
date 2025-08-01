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
        String username = request.getParameter("username");
        String currentPwd = request.getParameter("currentPassword");
        String newPwd = request.getParameter("newPassword");
        String confirmPwd = request.getParameter("confirmPassword");

        try {
            if (username == null || currentPwd == null || newPwd == null || confirmPwd == null) {
                session.setAttribute("pwdError", "All fields are required.");
                response.sendRedirect("my_profile.jsp");
                return;
            }

            if (!newPwd.equals(confirmPwd)) {
                session.setAttribute("pwdError", "New passwords do not match.");
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

            if (user == null || !BCrypt.checkpw(currentPwd, user.getPassword())) {
                session.setAttribute("pwdError", "Current password is incorrect.");
                response.sendRedirect("my_profile.jsp");
                return;
            }

            // Hash new password
            String hashedPwd = BCrypt.hashpw(newPwd, BCrypt.gensalt(12));

            boolean updated = userDAO.updateUserPassword(username, hashedPwd);
            if (updated) {
                session.setAttribute("pwdMsg", "Password updated successfully.");
            } else {
                session.setAttribute("pwdError", "Failed to update password.");
            }

        } catch (Exception e) {
            logger.severe("Error changing password: " + e.getMessage());
            session.setAttribute("pwdError", "An error occurred.");
        }

        response.sendRedirect("my_profile.jsp");
    }

    private boolean isPasswordStrong(String password) {
        return password.length() >= 8 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[a-z].*") &&
                password.matches(".*\\d.*");
    }
}
