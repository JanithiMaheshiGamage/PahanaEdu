<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Login - Pahana Edu</title>
</head>
<body>
<h2>Login</h2>

<%
    String message = request.getParameter("message");
    if ("logout".equals(message)) {
%>
<p style="color:green;">Logged out successfully.</p>
<%
} else if ("invalid".equals(message)) {
%>
<p style="color:red;">Invalid username or password. Please try again.</p>
<%
    }
%>

<form method="post" action="login">
    <label>Username:</label>
    <input type="text" name="username" required /> <br/><br/>

    <label>Password:</label>
    <input type="password" name="password" required /> <br/><br/>

    <input type="submit" value="Login" />
</form>
</body>
</html>
