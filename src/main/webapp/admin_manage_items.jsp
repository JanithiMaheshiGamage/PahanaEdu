<%@ page import="com.pahanaedu.dao.ItemDAO" %>
<%@ page import="com.pahanaedu.model.Item" %>
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

  ItemDAO itemDAO = new ItemDAO();
  List<Item> items = null;
  List<String> categories = null;

  items = (List<Item>) request.getAttribute("items");
  categories = (List<String>) request.getAttribute("categories");

  if (items == null) {
    items = itemDAO.getAllItems();
  }
  if (categories == null) {
    categories = itemDAO.getAllCategories();
  }

  // Notification variables
  String success = (String) httpSession.getAttribute("success");
  String error = (String) httpSession.getAttribute("error");
  if (success != null) httpSession.removeAttribute("success");
  if (error != null) httpSession.removeAttribute("error");

  // Handle search
  String searchKeyword = request.getParameter("search");
  if (searchKeyword != null && !searchKeyword.isEmpty()) {
    items = itemDAO.searchItems(searchKeyword);
  }
%>

<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>PAHANA EDU - Item Management</title>
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

<!-- Add Category Modal -->
<div id="addCategoryModal" class="modal">
  <div class="modal-content">
    <span class="close-modal">&times;</span>
    <h3>Add New Category</h3>
    <form id="addCategoryForm">
      <div class="form-group">
        <label for="newCategoryName">Category Name</label>
        <input type="text" id="newCategoryName" name="newCategoryName" placeholder="Enter category name" required>
      </div>
      <div class="form-actions">
        <button type="button" class="btn btn-secondary" id="cancelAddCategory">Cancel</button>
        <button type="submit" class="btn btn-primary">Add Category</button>
      </div>
    </form>
  </div>
</div>

<div class="admin-container">
  <!-- Sidebar -->
  <jsp:include page="sidebar.jsp">
    <jsp:param name="activePage" value="items" />
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
      <h1 class="page-title">Item Management</h1>

      <!-- Item Form -->
      <form method="post" class="user-form" id="itemForm" action="${pageContext.request.contextPath}/manage-items" onsubmit="return validateItemForm()">
        <input type="hidden" id="itemId" name="itemId">
        <input type="hidden" name="action" id="formAction" value="add">

        <div class="form-row">
          <div class="form-group">
            <label for="name">Item Name</label>
            <input type="text" id="name" name="name" placeholder="Enter item name" required>
          </div>
          <div class="form-group" style="position: relative;">
            <label for="category">Category</label>
            <div style="display: flex; align-items: center; gap: 8px;">
              <select id="category" name="category" required style="flex: 1;">
                <option value="">Select category</option>
                <% for (String category : categories) { %>
                <option value="<%= category %>"><%= category %></option>
                <% } %>
              </select>
              <button type="button" id="addCategoryBtn" class="btn btn-secondary" style="white-space: nowrap;">
                <i class="fas fa-plus"></i> Add
              </button>
            </div>
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="price">Price</label>
            <input type="number" id="price" name="price" placeholder="Enter price" min="0" step="0.01" required>
          </div>
          <div class="form-group">
            <label for="stockQty">Stock Quantity</label>
            <input type="number" id="stockQty" name="stockQty" placeholder="Enter quantity" min="0" required>
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="description">Description</label>
            <textarea id="description" name="description" rows="3" placeholder="Enter description"></textarea>
          </div>
        </div>

        <div class="form-actions">
          <button type="button" class="btn btn-secondary" id="clearBtn">Clear</button>
          <button type="submit" class="btn btn-primary" id="addBtn">Add Item</button>
          <button type="submit" class="btn btn-primary" id="updateBtn" style="display:none">Update Item</button>
          <button type="button" class="btn btn-tertiary" id="cancelEditBtn" style="display:none">Cancel Edit</button>
        </div>
      </form>

      <!-- Search Bar -->
      <form method="get" class="search-bar">
        <input type="text" name="search" placeholder="Search items..." value="<%= searchKeyword != null ? searchKeyword : "" %>">
        <button type="submit"><i class="fas fa-search"></i></button>
        <% if (searchKeyword != null && !searchKeyword.isEmpty()) { %>
        <a href="admin_manage_items.jsp" class="btn btn-secondary" style="margin-left: 10px;">Clear Search</a>
        <% } %>
      </form>

      <!-- Item Table -->
      <div class="user-table-container">
        <table class="user-table">
          <thead>
          <tr>
            <th colspan="8" style="text-align: right;">
              <button type="button" class="btn btn-primary" id="newItemBtn">
                <i class="fas fa-plus"></i> New Item
              </button>
            </th>
          </tr>
          <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Category</th>
            <th>Price</th>
            <th>Stock</th>
            <th>Created Date</th>
            <th>Created By</th>
            <th>Actions</th>
          </tr>
          </thead>
          <tbody>
          <% if(items != null && !items.isEmpty()) { %>
          <% for (Item item : items) { %>
          <tr>
            <td><%= item.getItemId() %></td>
            <td><%= item.getName() %></td>
            <td><%= item.getCategoryName() %></td>
            <td><%= String.format("%.2f", item.getPrice()) %></td>
            <td><%= item.getStockQty() %></td>
            <td><%= item.getCreatedDate() != null ? item.getCreatedDate().toString() : "" %></td>
            <td><%= item.getCreatedBy() %></td>
            <td>
              <button class="edit-btn" data-id="<%= item.getItemId() %>">
                <i class="fas fa-edit"></i> Edit
              </button>
              <button class="delete-btn" data-id="<%= item.getItemId() %>">
                <i class="fas fa-trash-alt"></i> Delete
              </button>
            </td>
          </tr>
          <% } %>
          <% } else { %>
          <tr>
            <td colspan="8" style="text-align: center;">No items found</td>
          </tr>
          <% } %>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</div>

<script>

  // Modal functionality
  function setupModal() {
    const modal = document.getElementById('addCategoryModal');
    const btn = document.getElementById('addCategoryBtn');
    const closeBtn = document.querySelector('.close-modal');
    const cancelBtn = document.getElementById('cancelAddCategory');

    if (!modal || !btn) {
      console.error('Modal elements not found');
      return;
    }

    // Open modal
    btn.addEventListener('click', function(e) {
      e.preventDefault();
      modal.style.display = 'block';
      document.body.classList.add('body-modal-open');
    });

    // Close modal
    function closeModal() {
      modal.style.display = 'none';
      document.body.classList.remove('body-modal-open');
    }

    closeBtn.addEventListener('click', closeModal);
    cancelBtn.addEventListener('click', closeModal);

    // Close when clicking outside modal
    window.addEventListener('click', function(event) {
      if (event.target === modal) {
        closeModal();
      }
    });

    // Handle form submission
    document.getElementById('addCategoryForm').addEventListener('submit', function(e) {
      e.preventDefault();
      const newCategoryName = document.getElementById('newCategoryName').value.trim();

      if (newCategoryName === '') {
        alert('Please enter a category name');
        return;
      }

      fetch('${pageContext.request.contextPath}/manage-categories', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `action=add&categoryName=${encodeURIComponent(newCategoryName)}`
      })
              .then(response => response.json())
              .then(data => {
                if (data.success) {
                  const categorySelect = document.getElementById('category');
                  const newOption = document.createElement('option');
                  newOption.value = newCategoryName;
                  newOption.textContent = newCategoryName;
                  newOption.selected = true;
                  categorySelect.appendChild(newOption);

                  document.getElementById('addCategoryForm').reset();
                  closeModal();
                  alert('Category added successfully!');
                } else {
                  alert(data.message || 'Failed to add category');
                }
              })
              .catch(error => {
                console.error('Error:', error);
                alert('Error adding category');
              });
    });
  }

  document.addEventListener('DOMContentLoaded', function() {
    setupModal();
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
    function validateItemForm() {
      const price = document.getElementById('price').value;
      const stockQty = document.getElementById('stockQty').value;

      if (parseFloat(price) < 0) {
        alert('Price must be positive');
        return false;
      }

      if (parseInt(stockQty) < 0) {
        alert('Stock quantity must be positive');
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

    // Add new item
    document.getElementById('newItemBtn').addEventListener('click', resetFormToAddMode);

    // Cancel edit functionality
    document.getElementById('cancelEditBtn').addEventListener('click', function() {
      resetFormToAddMode();
    });

    function resetFormToAddMode() {
      document.getElementById('itemForm').reset();
      document.getElementById('itemId').value = '';
      document.getElementById('formAction').value = 'add';

      // Update UI
      document.getElementById('addBtn').style.display = 'block';
      document.getElementById('updateBtn').style.display = 'none';
      document.getElementById('cancelEditBtn').style.display = 'none';
      document.querySelector('.page-title').textContent = 'Item Management';
    }

    // Edit button functionality
    const editButtons = document.querySelectorAll('.edit-btn');
    const form = document.querySelector('.user-form');
    const addBtn = document.getElementById('addBtn');
    const updateBtn = document.getElementById('updateBtn');
    const formAction = document.getElementById('formAction');
    const itemIdInput = document.getElementById('itemId');

    editButtons.forEach(button => {
      button.addEventListener('click', function() {
        const itemId = this.getAttribute('data-id');

        fetch('${pageContext.request.contextPath}/manage-items?action=get&itemId=' + itemId)
                .then(response => response.json())
                .then(item => {
                  document.getElementById('itemId').value = item.itemId;
                  document.getElementById('name').value = item.name;
                  document.getElementById('category').value = item.categoryName;
                  document.getElementById('price').value = item.price;
                  document.getElementById('stockQty').value = item.stockQty;
                  document.getElementById('description').value = item.description;

                  formAction.value = 'update';

                  addBtn.style.display = 'none';
                  updateBtn.style.display = 'block';
                  document.getElementById('cancelEditBtn').style.display = 'block';
                  document.querySelector('.page-title').textContent = 'Update Item';
                })
                .catch(error => {
                  console.error('Error fetching item:', error);
                  alert('Error loading item details');
                });
      });
    });

    // Delete button functionality
    const deleteButtons = document.querySelectorAll('.delete-btn');
    deleteButtons.forEach(button => {
      button.addEventListener('click', function() {
        const itemId = this.getAttribute('data-id');
        if (confirm('Are you sure you want to delete this item?')) {
          const form = document.createElement('form');
          form.method = 'POST';
          form.action = '${pageContext.request.contextPath}/manage-items';

          const actionInput = document.createElement('input');
          actionInput.type = 'hidden';
          actionInput.name = 'action';
          actionInput.value = 'delete';
          form.appendChild(actionInput);

          const itemIdInput = document.createElement('input');
          itemIdInput.type = 'hidden';
          itemIdInput.name = 'itemId';
          itemIdInput.value = itemId;
          form.appendChild(itemIdInput);

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