package com.pahanaedu.controller;

import com.pahanaedu.dao.UserDAO;
import com.pahanaedu.model.User;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/resetPassword")
public class ResetPasswordServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(ResetPasswordServlet.class.getName());
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if ("verify".equals(action)) {
            handleVerification(request, response);
        } else if ("reset".equals(action)) {
            handlePasswordReset(request, response);
        } else {
            redirectWithError(response, "invalid_action");
        }
    }

    private void handleVerification(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String username = request.getParameter("username");
        String email = request.getParameter("email");

        logger.log(Level.INFO, "Verification request for username: {0}, email: {1}",
                new Object[]{username, email});

        if (username == null || username.trim().isEmpty() ||
                email == null || email.trim().isEmpty()) {
            redirectWithError(response, "empty_fields");
            return;
        }

        try {
            User user = userDAO.getUserByUsername(username);
            if (user == null || !email.equalsIgnoreCase(user.getEmail())) {
                logger.log(Level.INFO, "Username/email mismatch");
                redirectWithError(response, "invalid_credentials");
                return;
            }

            // Store verified user in session
            HttpSession session = request.getSession();
            session.setAttribute("resetUser", user);
            response.sendRedirect("password_reset.jsp?verified=true");

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Verification error", e);
            redirectWithError(response, "server_error");
        }
    }

    private void handlePasswordReset(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("resetUser");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        if (user == null) {
            redirectWithError(response, "session_expired");
            return;
        }

        if (newPassword == null || confirmPassword == null ||
                !newPassword.equals(confirmPassword)) {
            redirectWithError(response, "password_mismatch");
            return;
        }

        if (!isPasswordStrong(newPassword)) {
            redirectWithError(response, "weak_password");
            return;
        }

        try {
            boolean updateSuccess = userDAO.updateUserPassword(user.getUsername(), newPassword);
            if (!updateSuccess) {
                redirectWithError(response, "database_error");
                return;
            }

            // Clear session after successful reset
            session.removeAttribute("resetUser");
            // Redirect to login page with success message
            response.sendRedirect("login.jsp?reset=success");

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Password reset error", e);
            redirectWithError(response, "server_error");
        }
    }

    private void redirectWithError(HttpServletResponse response, String errorType) throws IOException {
        response.sendRedirect("password_reset.jsp?error=" + errorType);
    }

    private boolean isPasswordStrong(String password) {
        return password != null && password.length() >= 8 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[a-z].*") &&
                password.matches(".*\\d.*");
    }
}