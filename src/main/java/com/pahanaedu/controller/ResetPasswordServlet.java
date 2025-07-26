package com.pahanaedu.controller;

import com.pahanaedu.dao.UserDAO;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

@WebServlet("/resetPassword")
public class ResetPasswordServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        // Debug logging
        System.out.println("Password reset attempt for user: " + username);
        System.out.println("New password: " + newPassword);
        System.out.println("Confirm password: " + confirmPassword);

        // Validate input parameters
        if (username == null || username.trim().isEmpty()) {
            response.sendRedirect("password_reset.jsp?error=invalid_user");
            return;
        }

        // Validate passwords match
        if (!newPassword.equals(confirmPassword)) {
            System.out.println("Password mismatch error");
            response.sendRedirect("password_reset.jsp?username=" + username + "&error=mismatch");
            return;
        }

        // Validate password strength
        PasswordStrengthResult strengthResult = checkPasswordStrength(newPassword);
        if (!strengthResult.isStrong()) {
            System.out.println("Password strength error: " + strengthResult.getMessage());
            response.sendRedirect("password_reset.jsp?username=" + username + "&error=weak");
            return;
        }

        try {
            // Verify user exists before attempting password update
            if (!userDAO.userExists(username)) {
                System.out.println("User does not exist: " + username);
                response.sendRedirect("password_reset.jsp?username=" + username + "&error=invalid_user");
                return;
            }

            System.out.println("Attempting to update password for: " + username);
            boolean success = userDAO.updateUserPassword(username, newPassword);

            if (success) {
                System.out.println("Password updated successfully for: " + username);
                // Add success message to session to display on login page
                request.getSession().setAttribute("resetMessage", "Password reset successfully. Please login with your new password.");
                response.sendRedirect("login.jsp");
            } else {
                System.out.println("Password update failed for: " + username);
                response.sendRedirect("password_reset.jsp?username=" + username + "&error=database");
            }
        } catch (Exception e) {
            System.err.println("Error updating password for " + username);
            e.printStackTrace();
            response.sendRedirect("password_reset.jsp?username=" + username + "&error=database");
        }
    }

    private PasswordStrengthResult checkPasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return new PasswordStrengthResult(false, "Password cannot be empty");
        }

        boolean hasUppercase = !password.equals(password.toLowerCase());
        boolean hasLowercase = !password.equals(password.toUpperCase());
        boolean hasNumber = password.matches(".*\\d.*");
        boolean hasMinLength = password.length() >= 8;

        StringBuilder message = new StringBuilder();

        if (!hasMinLength) {
            message.append("Password must be at least 8 characters long. ");
        }
        if (!hasUppercase) {
            message.append("Password must contain at least one uppercase letter. ");
        }
        if (!hasLowercase) {
            message.append("Password must contain at least one lowercase letter. ");
        }
        if (!hasNumber) {
            message.append("Password must contain at least one number. ");
        }

        boolean isStrong = hasMinLength && hasUppercase && hasLowercase && hasNumber;

        return new PasswordStrengthResult(isStrong, message.toString().trim());
    }

    // Helper class for password strength results
    private static class PasswordStrengthResult {
        private final boolean strong;
        private final String message;

        public PasswordStrengthResult(boolean strong, String message) {
            this.strong = strong;
            this.message = message;
        }

        public boolean isStrong() {
            return strong;
        }

        public String getMessage() {
            return message;
        }
    }
}