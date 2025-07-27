<%@ page import="com.pahanaedu.dao.UserDAO" %>
<%@ page import="com.pahanaedu.model.User" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
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
    List<User> users = null;

    // First try to get users from request attribute
    users = (List<User>) request.getAttribute("users");

    // If not found in request, get from DAO
    if (users == null) {
        users = userDAO.getAllUsers();
    }

    // Notification variables
    String success = (String) httpSession.getAttribute("success");
    String error = (String) httpSession.getAttribute("error");
    if (success != null) httpSession.removeAttribute("success");
    if (error != null) httpSession.removeAttribute("error");

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

<!-- Notification Messages -->
<% if (success != null) { %>
<div class="notification success">
    <span><%= success %></span>
    <button class="close-btn">&times;</button>
</div>
<% } %>
<% if (error != null) { %>
<div class="notification error">
    <span><%= error %></span>
    <button class="close-btn">&times;</button>
</div>
<% } %>

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
            <form method="post" class="user-form" onsubmit="return validateForm()" action="${pageContext.request.contextPath}/manage-users">
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
                        <label for="email">Email</label>
                        <input type="email" id="email" name="email" placeholder="Enter email" required>
                    </div>

                    <div class="form-group">
                        <label for="employeeNo">Employee Number</label>
                        <input type="text" id="employeeNo" name="employeeNo" placeholder="Enter employee number" required>
                    </div>
                </div>


                <div class="form-row">
                    <div class="form-group">
                        <label for="password">Password</label>
                        <input type="password" id="password" name="password" placeholder="Enter password" required>
                        <small class="form-text">Must be at least 8 characters with uppercase, lowercase, and numbers</small>
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
                        <th colspan="9" style="text-align: right;"> <!-- Changed from 8 to 9 -->
                            <button type="button" class="btn btn-primary" id="newUserBtn">
                                <i class="fas fa-plus"></i> New User
                            </button>
                        </th>
                    </tr>
                    <tr>
                        <th>User ID</th>
                        <th>Full Name</th>
                        <th>Employee No</th>
                        <th>Username</th>
                        <th>Email</th>
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
                        <td><%= user.getEmployeeNo() %></td>
                        <td><%= user.getUsername() %></td>
                        <td class="email-cell"><%= user.getEmail() %></td>
                        <td><%= user.getRole() %></td>
                        <td>
                                <span class="status-badge <%= user.isStatus() ? "status-active" : "status-inactive" %>">
                                    <%= user.isStatus() ? "Active" : "Inactive" %>
                                </span>
                        </td>
                        <td><button class="edit-btn" data-id="<%= user.getId() %>">Edit</button></td>
                        <td>
                            <button class="delete-btn" data-id="<%= user.getId() %>">
                                <i class="fas fa-trash-alt"></i> Delete
                            </button>
                        </td>
                    </tr>
                    <% } %>
                    <% } else { %>
                    <tr>
                        <td colspan="9" style="text-align: center;">No users found</td>
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
        // Notification handling
        document.querySelectorAll('.close-btn').forEach(btn => {
            btn.addEventListener('click', function() {
                const notification = this.closest('.notification');
                notification.classList.add('fade-out');
                setTimeout(() => notification.remove(), 500);
            });
        });

        // Auto-close notifications after 5 seconds
        setTimeout(() => {
            document.querySelectorAll('.notification').forEach(notification => {
                notification.classList.add('fade-out');
                setTimeout(() => notification.remove(), 500);
            });
        }, 5000);

        // Form validation
        function validateUserForm() {
            const username = document.getElementById('username').value.trim();
            const email = document.getElementById('email').value.trim();
            const password = document.getElementById('password');
            const role = document.getElementById('role').value;
            const employeeNo = document.getElementById('employeeNo').value.trim();
            const isEditMode = document.getElementById('formAction').value === 'update';

            if (!username || !email || !role || !employeeNo) {
                alert('Please fill all required fields');
                return false;
            }

            // Skip password validation in edit mode (since it's disabled)
            if (!isEditMode && (!password.value ||
                password.value.length < 8 ||
                !/[A-Z]/.test(password.value) ||
                !/[a-z]/.test(password.value) ||
                !/[0-9]/.test(password.value))) {
                alert('Password must be at least 8 characters with uppercase, lowercase, and numbers');
                return false;
            }

            if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
                alert('Please enter a valid email address');
                return false;
            }

            return true;
        }

        // Update the form's onsubmit to use the correct function
        document.querySelector('.user-form').onsubmit = validateUserForm;

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
            document.querySelector('.user-form').reset();
            document.getElementById('status').checked = true;
            document.getElementById('statusLabel').textContent = 'Active';
            document.getElementById('userId').value = '';
            document.getElementById('formAction').value = 'add';

            // Re-enable password field for new users
            const passwordField = document.getElementById('password');
            passwordField.disabled = false;
            passwordField.placeholder = "Enter password";
            passwordField.required = true;

            // Update UI
            document.getElementById('addBtn').style.display = 'block';
            document.getElementById('updateBtn').style.display = 'none';
            document.getElementById('cancelEditBtn').style.display = 'none';
            document.querySelector('.page-title').textContent = 'System Users';
        }

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

                // Fill form fields
                document.getElementById('fullname').value = cells[1].textContent;
                document.getElementById('employeeNo').value = cells[2].textContent;
                document.getElementById('username').value = cells[3].textContent;
                document.getElementById('email').value = cells[4].textContent;

                // Disable password field and set placeholder
                const passwordField = document.getElementById('password');
                passwordField.disabled = true;
                passwordField.value = ''; // Clear the password field for security
                passwordField.placeholder = "Password cannot be changed by admin";
                passwordField.required = false;

                // Set role dropdown - FIXED THIS PART
                const roleSelect = document.getElementById('role');
                const role = cells[5].textContent.trim().toLowerCase(); // Convert to lowercase to match option values
                for (let i = 0; i < roleSelect.options.length; i++) {
                    if (roleSelect.options[i].value === role) {
                        roleSelect.selectedIndex = i;
                        break;
                    }
                }

                // Set status toggle
                const status = cells[6].querySelector('.status-badge').textContent.trim();
                toggleSwitch.checked = status === 'Active';
                toggleStatus.textContent = status;

                // Set hidden fields
                userIdInput.value = cells[0].textContent;
                formAction.value = 'update';

                // Update UI
                addBtn.style.display = 'none';
                updateBtn.style.display = 'block';
                document.getElementById('cancelEditBtn').style.display = 'block';
                document.querySelector('.page-title').textContent = 'Update User';
            });
        });

        // Delete button functionality
        const deleteButtons = document.querySelectorAll('.delete-btn');
        deleteButtons.forEach(button => {
            button.addEventListener('click', function() {
                const userId = this.getAttribute('data-id');
                if (confirm('Are you sure you want to delete this user?')) {
                    // Create a form to submit
                    const form = document.createElement('form');
                    form.method = 'POST';
                    form.action = '${pageContext.request.contextPath}/manage-users';

                    // Add action parameter
                    const actionInput = document.createElement('input');
                    actionInput.type = 'hidden';
                    actionInput.name = 'action';
                    actionInput.value = 'delete';
                    form.appendChild(actionInput);

                    // Add userId parameter
                    const userIdInput = document.createElement('input');
                    userIdInput.type = 'hidden';
                    userIdInput.name = 'userId';
                    userIdInput.value = userId;
                    form.appendChild(userIdInput);

                    // Submit the form
                    document.body.appendChild(form);
                    form.submit();
                }
            });
        });

        // Clear button functionality
        const clearButton = document.getElementById('clearBtn');
        clearButton.addEventListener('click', function() {
            resetFormToAddMode();
        });
    });
</script>
</body>
</html>