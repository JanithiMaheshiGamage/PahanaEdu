<%@ page import="com.pahanaedu.dao.CustomerDAO" %>
<%@ page import="com.pahanaedu.model.Customer" %>
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

    CustomerDAO customerDAO = new CustomerDAO();
    List<Customer> customers = null;

    customers = (List<Customer>) request.getAttribute("customers");
    if (customers == null) {
        customers = customerDAO.getAllCustomers();
    }

    // Notification variables
    String success = (String) httpSession.getAttribute("success");
    String error = (String) httpSession.getAttribute("error");
    if (success != null) httpSession.removeAttribute("success");
    if (error != null) httpSession.removeAttribute("error");

    // Handle search
    String searchKeyword = request.getParameter("search");
    if (searchKeyword != null && !searchKeyword.isEmpty()) {
        customers = customerDAO.searchCustomers(searchKeyword);
    }
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>PAHANA EDU - Customer Management</title>
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
    <jsp:include page="sidebar.jsp">
        <jsp:param name="activePage" value="customers" />
    </jsp:include>

    <!-- Main Content -->
    <div class="main-content">
        <!-- Topbar -->
        <div class="topbar">
            <div class="logo"><img src="images/Topnavlogo.png" alt="PAHANA EDU Logo" class="logo-image"></div>
            <div class="datetime" id="currentDateTime"></div>
        </div>

        <!-- Content Wrapper -->
        <div class="content-wrapper">
            <h1 class="page-title">Customer Management</h1>

            <!-- Customer Form -->
            <form method="post" class="user-form" id="customerForm" action="${pageContext.request.contextPath}/manage-customers" onsubmit="return validateForm()">
                <input type="hidden" id="accountNo" name="accountNo">
                <input type="hidden" name="action" id="formAction" value="add">

                <div class="form-row">
                    <div class="form-group">
                        <label for="name">Full Name</label>
                        <input type="text" id="name" name="name" placeholder="Enter customer name" required>
                    </div>
                    <div class="form-group">
                        <label for="nic">NIC Number</label>
                        <input type="text" id="nic" name="nic" placeholder="Enter NIC" required maxlength="12">
                        <small class="form-text">Must be exactly 12 digits</small>
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label for="phoneNo">Phone Number</label>
                        <input type="text" id="phoneNo" name="phoneNo" placeholder="Enter phone number" required maxlength="10">
                        <small class="form-text">Must be exactly 10 digits</small>
                    </div>
                    <div class="form-group">
                        <label for="email">Email</label>
                        <input type="email" id="email" name="email" placeholder="Enter email">
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label for="address">Address</label>
                        <textarea id="address" name="address" rows="3" placeholder="Enter address" required></textarea>
                    </div>
                </div>

                <div class="form-actions">
                    <button type="button" class="btn btn-secondary" id="clearBtn">Clear</button>
                    <button type="submit" class="btn btn-primary" id="addBtn">Add Customer</button>
                    <button type="submit" class="btn btn-primary" id="updateBtn" style="display:none">Update Customer</button>
                    <button type="button" class="btn btn-tertiary" id="cancelEditBtn" style="display:none">Cancel Edit</button>
                </div>
            </form>

            <!-- Search Bar -->
            <form method="get" class="search-bar">
                <input type="text" name="search" placeholder="Search customers..." value="<%= searchKeyword != null ? searchKeyword : "" %>">
                <button type="submit"><i class="fas fa-search"></i></button>
                <% if (searchKeyword != null && !searchKeyword.isEmpty()) { %>
                <a href="admin_manage_customers.jsp" class="btn btn-secondary" style="margin-left: 10px;">Clear Search</a>
                <% } %>
            </form>

            <!-- Customer Table -->
            <div class="user-table-container">
                <table class="user-table">
                    <thead>
                    <tr>
                        <th colspan="10" style="text-align: right;">
                            <button type="button" class="btn btn-primary" id="newCustomerBtn">
                                <i class="fas fa-plus"></i> New Customer
                            </button>
                        </th>
                    </tr>
                    <tr>
                        <th>Account No</th>
                        <th>Name</th>
                        <th>NIC</th>
                        <th>Phone No</th>
                        <th>Email</th>
                        <th>Address</th>
                        <th>Units</th>
                        <th>Created Date</th>
                        <th>Created By</th>
                        <th>Actions</th>
                    </tr>
                    </thead>
                    <tbody>
                    <% if(customers != null && !customers.isEmpty()) { %>
                    <% for (Customer customer : customers) { %>
                    <tr>
                        <td><%= customer.getAccountNo() %></td>
                        <td><%= customer.getName() %></td>
                        <td><%= customer.getNic() %></td>
                        <td><%= customer.getPhoneNo() %></td>
                        <td><%= customer.getEmail() %></td>
                        <td><%= customer.getAddress() %></td>
                        <td><%= customer.getUnitsConsumed() %></td>
                        <td><%= customer.getCreatedDate() != null ? customer.getCreatedDate().toString() : "" %></td>
                        <td><%= customer.getCreatedBy() %></td>
                        <td>
                            <button class="edit-btn" data-id="<%= customer.getAccountNo() %>">
                                <i class="fas fa-edit"></i> Edit
                            </button>
                        </td>
                        <td>
                            <button class="delete-btn" data-id="<%= customer.getAccountNo() %>">
                                <i class="fas fa-trash-alt"></i> Delete
                            </button>
                        </td>
                    </tr>
                    <% } %>
                    <% } else { %>
                    <tr>
                        <td colspan="10" style="text-align: center;">No customers found</td>
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
        function validateForm() {
            const nic = document.getElementById('nic').value.trim();
            const phoneNo = document.getElementById('phoneNo').value.trim();

            // Validate NIC (12 digits)
            if (!/^\d{12}$/.test(nic)) {
                alert('NIC must contain exactly 12 digits');
                return false;
            }

            // Validate Phone Number (10 digits)
            if (!/^\d{10}$/.test(phoneNo)) {
                alert('Phone number must contain exactly 10 digits');
                return false;
            }

            return true;
        }

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

        // Add new customer
        document.getElementById('newCustomerBtn').addEventListener('click', resetFormToAddMode);

        // Cancel edit functionality
        document.getElementById('cancelEditBtn').addEventListener('click', function() {
            resetFormToAddMode();
        });

        function resetFormToAddMode() {
            document.getElementById('customerForm').reset();
            document.getElementById('accountNo').value = '';
            document.getElementById('formAction').value = 'add';

            // Update UI
            document.getElementById('addBtn').style.display = 'block';
            document.getElementById('updateBtn').style.display = 'none';
            document.getElementById('cancelEditBtn').style.display = 'none';
            document.querySelector('.page-title').textContent = 'Customer Management';
        }

        // Edit button functionality
        const editButtons = document.querySelectorAll('.edit-btn');
        const form = document.querySelector('.user-form');
        const addBtn = document.getElementById('addBtn');
        const updateBtn = document.getElementById('updateBtn');
        const formAction = document.getElementById('formAction');
        const accountNoInput = document.getElementById('accountNo');

        editButtons.forEach(button => {
            button.addEventListener('click', function() {
                const row = this.closest('tr');
                const cells = row.querySelectorAll('td');

                // Fill form fields
                document.getElementById('accountNo').value = cells[0].textContent;
                document.getElementById('name').value = cells[1].textContent;
                document.getElementById('nic').value = cells[2].textContent;
                document.getElementById('phoneNo').value = cells[3].textContent;
                document.getElementById('email').value = cells[4].textContent;
                document.getElementById('address').value = cells[5].textContent;

                // Set hidden fields
                formAction.value = 'update';

                // Update UI
                addBtn.style.display = 'none';
                updateBtn.style.display = 'block';
                document.getElementById('cancelEditBtn').style.display = 'block';
                document.querySelector('.page-title').textContent = 'Update Customer';
            });
        });

        // Delete button functionality - Updated to match users page
        const deleteButtons = document.querySelectorAll('.delete-btn');
        deleteButtons.forEach(button => {
            button.addEventListener('click', function() {
                const accountNo = this.getAttribute('data-id');
                if (confirm('Are you sure you want to delete this customer?')) {
                    // Create a hidden form to submit the delete request
                    const form = document.createElement('form');
                    form.method = 'POST';
                    form.action = '${pageContext.request.contextPath}/manage-customers';

                    // Add action parameter
                    const actionInput = document.createElement('input');
                    actionInput.type = 'hidden';
                    actionInput.name = 'action';
                    actionInput.value = 'delete';
                    form.appendChild(actionInput);

                    // Add accountNo parameter
                    const accountNoInput = document.createElement('input');
                    accountNoInput.type = 'hidden';
                    accountNoInput.name = 'accountNo';
                    accountNoInput.value = accountNo;
                    form.appendChild(accountNoInput);

                    // Add CSRF token if needed (you might need to add this)
                    // const csrfInput = document.createElement('input');
                    // csrfInput.type = 'hidden';
                    // csrfInput.name = 'csrfToken';
                    // csrfInput.value = '<%= session.getAttribute("csrfToken") %>';
                    // form.appendChild(csrfInput);

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