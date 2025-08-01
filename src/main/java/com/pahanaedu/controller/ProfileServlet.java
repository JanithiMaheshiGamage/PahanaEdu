package com.pahanaedu.controller;

import com.pahanaedu.dao.UserDAO;
import com.pahanaedu.model.User;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/update-profile")
public class ProfileServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(ProfileServlet.class.getName());
    private UserDAO userDAO;

    @Override
    public void init() {
        userDAO = new UserDAO();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession();

        try {
            // Check if user is logged in
            String currentUsername = (String) session.getAttribute("username");
            if (currentUsername == null) {
                logger.warning("No user in session");
                response.sendRedirect("login.jsp");
                return;
            }

            // Get form parameters
            String name = request.getParameter("name");
            String email = request.getParameter("email");
            String userIdStr = request.getParameter("userId");

            // Validate input
            if (name == null || name.trim().isEmpty() ||
                    email == null || email.trim().isEmpty() ||
                    userIdStr == null || userIdStr.trim().isEmpty()) {

                session.setAttribute("error", "Missing required fields");
                response.sendRedirect("my_profile.jsp");
                return;
            }

            int userId = Integer.parseInt(userIdStr);

            // Create User object with updated data
            User user = new User();
            user.setId(userId);
            user.setFullName(name);
            user.setEmail(email);

            // Attempt to update profile
            boolean updated = userDAO.updateUserProfile(user);

            if (updated) {
                session.setAttribute("fullName", name); // ✅ Update session for sidebar
                session.setAttribute("success", "Profile updated successfully.");
            } else {
                session.setAttribute("error", "Failed to update profile.");
            }

            response.sendRedirect("my_profile.jsp");

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in ProfileServlet", e);
            session.setAttribute("error", "Server error occurred");
            response.sendRedirect("my_profile.jsp");
        }
    }
}
