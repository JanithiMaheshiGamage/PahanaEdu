<%@ page import="com.pahanaedu.dao.UserDAO" %>
<%@ page import="com.pahanaedu.model.User" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    // Check if user is logged in
    HttpSession httpSession = request.getSession(false);
    String username = (httpSession != null) ? (String) httpSession.getAttribute("username") : null;
    String role = (httpSession != null) ? (String) httpSession.getAttribute("role") : null;
    String fullName = (httpSession != null) ? (String) httpSession.getAttribute("fullName") : null;

    if (username == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    UserDAO userDAO = new UserDAO();
    List<User> users = userDAO.getAllUsers();

    // Handle form submissions
    if ("POST".equalsIgnoreCase(request.getMethod())) {
        String action = request.getParameter("action");
        User user = new User();

        if ("add".equals(action) || "update".equals(action)) {
            user.setFullName(request.getParameter("fullname"));
            user.setUsername(request.getParameter("username"));
            user.setPassword(request.getParameter("password"));
            user.setRole(request.getParameter("role"));
            user.setStatus("on".equals(request.getParameter("status")));

            if ("update".equals(action)) {
                user.setId(Integer.parseInt(request.getParameter("userId")));
                userDAO.updateUser(user);
            } else {
                userDAO.insertUser(user);
            }

            // Refresh user list after update
            users = userDAO.getAllUsers();
        }
    }

    // Handle search
    String searchKeyword = request.getParameter("search");
    if (searchKeyword != null && !searchKeyword.isEmpty()) {
        users = userDAO.searchUsers(searchKeyword);
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>PAHANA EDU - User Management</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="css/styles.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
</head>
<body>
<div class="admin-container">
    <!-- Sidebar -->
    <div class="sidebar">
        <div class="user-profile">
            <img src="images/UserImage.png" alt="User Avatar" class="user-avatar">
            <div class="user-name"><%= fullName != null ? fullName : "Admin User" %></div>
            <div class="user-role"><%= role != null ? role : "Admin" %></div>
        </div>

        <div class="nav-menu">
            <div class="nav-item active">
                <i class="fas fa-users"></i>
                <span>System Users</span>
            </div>
            <div class="nav-item">
                <i class="fas fa-user-tag"></i>
                <span>Customers</span>
            </div>
            <div class="nav-item">
                <i class="fas fa-boxes"></i>
                <span>Items</span>
            </div>
            <div class="nav-item">
                <i class="fas fa-exchange-alt"></i>
                <span>Transactions</span>
            </div>
            <div class="nav-item">
                <i class="fas fa-chart-bar"></i>
                <span>Reports</span>
            </div>
            <div class="nav-item">
                <i class="fas fa-question-circle"></i>
                <span>Help</span>
            </div>
            <div class="nav-item">
                <i class="fas fa-user-circle"></i>
                <span>My Profile</span>
            </div>
        </div>

        <a href="logout" class="logout-btn">
            <i class="fas fa-sign-out-alt"></i>
            <span>Logout</span>
        </a>
    </div>

    <!-- Main Content -->
    <div class="main-content">
        <!-- Topbar -->
        <div class="topbar">
            <div class="logo"><img src="images/Topnavlogo.png" alt="PAHANA EDU Logo" class="logo-image"></div>
            <div class="datetime" id="currentDateTime"></div>
        </div>

        <!-- Content Wrapper -->
        <div class="content-wrapper">
            <h1 class="page-title">System Users</h1>

            <!-- User Form -->
            <form method="post" class="user-form">
                <input type="hidden" id="userId" name="userId">
                <input type="hidden" name="action" id="formAction" value="add">

                <div class="form-row">
                    <div class="form-group">
                        <label for="fullname">Full Name</label>
                        <input type="text" id="fullname" name="fullname" placeholder="Enter full name" required>
                    </div>
                    <div class="form-group">
                        <label for="username">Username</label>
                        <input type="text" id="username" name="username" placeholder="Enter username" required>
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label for="password">Password</label>
                        <input type="password" id="password" name="password" placeholder="Enter password" required>
                    </div>
                    <div class="form-group">
                        <label for="role">User Role</label>
                        <select id="role" name="role" required>
                            <option value="">Select role</option>
                            <option value="admin">Admin</option>
                            <option value="staff">Staff</option>
                        </select>
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label>Status</label>
                        <div class="toggle-switch">
                            <label class="switch">
                                <input type="checkbox" id="status" name="status" checked>
                                <span class="slider"></span>
                            </label>
                            <span id="statusLabel">Active</span>
                        </div>
                    </div>
                </div>

                <div class="form-actions">
                    <button type="button" class="btn btn-secondary" id="clearBtn">Clear</button>
                    <button type="submit" class="btn btn-primary" id="addBtn">Add</button>
                    <button type="submit" class="btn btn-primary" id="updateBtn" style="display:none">Update</button>
                    <button type="button" class="btn btn-tertiary" id="cancelEditBtn" style="display:none">Cancel Edit</button>
                </div>
            </form>

            <!-- Search Bar -->
            <form method="get" class="search-bar">
                <input type="text" name="search" placeholder="Search users..." value="<%= searchKeyword != null ? searchKeyword : "" %>">
                <button type="submit"><i class="fas fa-search"></i></button>
                <% if (searchKeyword != null && !searchKeyword.isEmpty()) { %>
                <a href="admin_manage_users.jsp" class="btn btn-secondary" style="margin-left: 10px;">Clear Search</a>
                <% } %>
            </form>

            <!-- User Table -->
            <div class="user-table-container">
                <table class="user-table">
                    <thead>
                    <tr>
                        <th colspan="6" style="text-align: right;">
                            <button type="button" class="btn btn-primary" id="newUserBtn">
                                <i class="fas fa-plus"></i> New User
                            </button>
                        </th>
                    </tr>
                    <tr>
                        <th>User ID</th>
                        <th>Full Name</th>
                        <th>Username</th>
                        <th>User Role</th>
                        <th>Status</th>
                        <th>Action</th>
                    </tr>
                    </thead>
                    <tbody>
                    <% if(users != null && !users.isEmpty()) { %>
                    <% for (User user : users) { %>
                    <tr>
                        <td><%= user.getId() %></td>
                        <td><%= user.getFullName() %></td>
                        <td><%= user.getUsername() %></td>
                        <td><%= user.getRole() %></td>
                        <td>
                                <span class="status-badge <%= user.isStatus() ? "status-active" : "status-inactive" %>">
                                    <%= user.isStatus() ? "Active" : "Inactive" %>
                                </span>
                        </td>
                        <td><button class="edit-btn" data-id="<%= user.getId() %>">Edit</button></td>
                    </tr>
                    <% } %>
                    <% } else { %>
                    <tr>
                        <td colspan="6" style="text-align: center;">No users found</td>
                    </tr>
                    <% } %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<script>
    document.addEventListener('DOMContentLoaded', function() {
        // Update current date and time
        function updateDateTime() {
            const now = new Date();
            const options = {
                day: '2-digit',
                month: '2-digit',
                year: 'numeric',
                hour: '2-digit',
                minute: '2-digit',
                hour12: true
            };
            document.getElementById('currentDateTime').textContent = now.toLocaleString('en-GB', options);
        }
        updateDateTime();
        setInterval(updateDateTime, 60000);

        //add user
        document.getElementById('newUserBtn').addEventListener('click', resetFormToAddMode);

        // Cancel edit functionality
        document.getElementById('cancelEditBtn').addEventListener('click', function() {
            resetFormToAddMode();
        });

        function resetFormToAddMode() {
            form.reset();
            toggleSwitch.checked = true;
            toggleStatus.textContent = 'Active';
            userIdInput.value = '';
            formAction.value = 'add';
            addBtn.style.display = 'block';
            updateBtn.style.display = 'none';
            document.getElementById('cancelEditBtn').style.display = 'none';
            document.querySelector('.page-title').textContent = 'System Users';
        }

// Modify your existing edit button handler:
        editButtons.forEach(button => {
            button.addEventListener('click', function() {
                // ... existing edit code ...
                document.getElementById('cancelEditBtn').style.display = 'block';
            });
        });

        // Toggle switch functionality
        const toggleSwitch = document.getElementById('status');
        const toggleStatus = document.getElementById('statusLabel');
        toggleSwitch.addEventListener('change', function() {
            toggleStatus.textContent = this.checked ? 'Active' : 'Inactive';
        });

        // Edit button functionality
        const editButtons = document.querySelectorAll('.edit-btn');
        const form = document.querySelector('.user-form');
        const addBtn = document.getElementById('addBtn');
        const updateBtn = document.getElementById('updateBtn');
        const formAction = document.getElementById('formAction');
        const userIdInput = document.getElementById('userId');

        editButtons.forEach(button => {
            button.addEventListener('click', function() {
                const row = this.closest('tr');
                const cells = row.querySelectorAll('td');

                document.getElementById('fullname').value = cells[1].textContent;
                document.getElementById('username').value = cells[2].textContent;
                document.getElementById('password').value = '';

                const roleSelect = document.getElementById('role');
                const role = cells[3].textContent;
                for (let i = 0; i < roleSelect.options.length; i++) {
                    if (roleSelect.options[i].text === role) {
                        roleSelect.selectedIndex = i;
                        break;
                    }
                }

                const status = cells[4].querySelector('.status-badge').textContent.trim();
                toggleSwitch.checked = status === 'Active';
                toggleStatus.textContent = status;

                userIdInput.value = cells[0].textContent;
                formAction.value = 'update';
                addBtn.style.display = 'none';
                updateBtn.style.display = 'block';
                document.querySelector('.page-title').textContent = 'Update User';
            });
        });

        // Clear button functionality
        const clearButton = document.getElementById('clearBtn');
        clearButton.addEventListener('click', function() {
            form.reset();
            toggleSwitch.checked = true;
            toggleStatus.textContent = 'Active';
            userIdInput.value = '';
            formAction.value = 'add';
            addBtn.style.display = 'block';
            updateBtn.style.display = 'none';
            document.querySelector('.page-title').textContent = 'System Users';
        });
    });
</script>
</body>
</html>