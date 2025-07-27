package com.pahanaedu.controller;

import com.pahanaedu.dao.UserDAO;
import com.pahanaedu.model.User;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            response.sendRedirect("login.jsp?message=empty");
            return;
        }

        try {
            User user = userDAO.getUserByCredentials(username, password);

            if (user != null && user.isStatus()) {
                HttpSession session = request.getSession();
                session.setAttribute("username", user.getUsername());
                session.setAttribute("role", user.getRole());
                session.setAttribute("fullName", user.getFullName());

                if ("admin".equals(user.getRole())) {
                    response.sendRedirect("admin_manage_users.jsp");
                } else if ("staff".equals(user.getRole())) {
                    response.sendRedirect("staff_billing.jsp");
                } else {
                    response.sendRedirect("login.jsp?message=invalid");
                }
            } else {
                response.sendRedirect("login.jsp?message=invalid");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("login.jsp?message=error");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect("login.jsp");
    }
}
