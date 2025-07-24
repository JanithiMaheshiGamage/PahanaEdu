package com.pahanaedu.controller;

import com.pahanaedu.dao.UserDAO;

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

        String role = userDAO.getUserRole(username, password);

        if (role != null) {
            HttpSession session = request.getSession();
            session.setAttribute("username", username);
            session.setAttribute("role", role);

            if ("admin".equals(role)) {
                response.sendRedirect("admin_manage_users.jsp");
            } else if ("staff".equals(role)) {
                response.sendRedirect("staff_billing.jsp");
            } else {
                response.sendRedirect("login.jsp?message=invalid");
            }
        } else {
            response.sendRedirect("login.jsp?message=invalid");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect("login.jsp");
    }
}
