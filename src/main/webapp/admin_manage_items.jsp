<%@ page import="com.pahanaedu.dao.ItemDAO" %>
<%@ page import="com.pahanaedu.dao.CategoryDAO" %>
<%@ page import="com.pahanaedu.model.Item" %>
<%@ page import="com.pahanaedu.model.Category" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.text.SimpleDateFormat" %>
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
  CategoryDAO categoryDAO = new CategoryDAO();
  List<Item> items = null;
  List<Category> categories = categoryDAO.getAllCategories();

  items = (List<Item>) request.getAttribute("items");
  if (items == null) {
    items = itemDAO.getAllItems();
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

<div class="admin-container">
  <!-- Sidebar -->
  <% if ("admin".equals(role)) { %>
  <jsp:include page="sidebar.jsp">
    <jsp:param name="activePage" value="items" />
  </jsp:include>
  <% } else { %>
  <jsp:include page="staff_sidebar.jsp">
    <jsp:param name="activePage" value="items" />
  </jsp:include>
  <% } %>

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
            <input type="text" id="name" name="name" placeholder="Enter item name" required autocomplete="off">
          </div>
          <div class="form-group">
            <label for="category">Category</label>
            <div class="category-selector">
              <select id="category" name="categoryId" required>
                <option value="">Select a category</option>
                <% for (Category category : categories) { %>
                <option value="<%= category.getCategoryId() %>"><%= category.getCategoryName() %></option>
                <% } %>
              </select>
              <button type="button" id="addCategoryBtn" class="btn btn-secondary">
                <i class="fas fa-plus"></i> Add Category
              </button>
            </div>
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="price">Price (Rs.)</label>
            <input type="number" id="price" name="price" placeholder="Enter price" step="0.01" min="0" required>
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
          <button type="button" class="btn btn-secondary" id="clearItemBtn">Clear</button>
          <button type="submit" class="btn btn-primary" id="addItemBtn">Add Item</button>
          <button type="submit" class="btn btn-primary" id="updateItemBtn" style="display:none">Update Item</button>
          <button type="button" class="btn btn-tertiary" id="cancelItemEditBtn" style="display:none">Cancel Edit</button>
        </div>
      </form>

      <!-- Category Modal -->
      <div id="categoryModal" class="modal" style="display:none;">
        <div class="modal-content">
          <span class="close-modal">&times;</span>
          <h2>Add New Category</h2>
          <form id="categoryForm">
            <div class="form-group">
              <input type="text" id="newCategoryName" name="newCategoryName" placeholder="Enter category name" required>
            </div>
            <div class="form-actions">
              <button type="button" class="btn btn-secondary" id="cancelCategoryBtn">Cancel</button>
              <button type="submit" class="btn btn-primary">Add Category</button>
            </div>
          </form>
        </div>
      </div>

      <!-- Search Bar -->
      <form method="get" class="search-bar">
        <input type="text" name="search" placeholder="Search items..." value="<%= searchKeyword != null ? searchKeyword : "" %>">
        <button type="submit"><i class="fas fa-search"></i></button>
        <% if (searchKeyword != null && !searchKeyword.isEmpty()) { %>
        <a href="admin_manage_items.jsp" class="btn btn-secondary" style="margin-left: 10px;">Clear Search</a>
        <% } %>
      </form>

      <!-- Items Table -->
      <h2 class="section-title">Items</h2>
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
            <th>Item ID</th>
            <th>Name</th>
            <th>Category</th>
            <th>Price (Rs.)</th>
            <th>Stock Qty</th>
            <th>Description</th>
            <th>Created</th>
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
            <td><%= item.getDescription() != null ? item.getDescription() : "" %></td>
            <td>
              <%= item.getCreatedBy() != null ? item.getCreatedBy() : "System" %> -
              <%= item.getCreatedDate() != null ?
                      new SimpleDateFormat("yyyy-MM-dd").format(item.getCreatedDate()) :
                      "N/A" %>
            </td>
            <td>
              <button class="edit-btn" data-id="<%= item.getItemId() %>">
                <i class="fas fa-edit"></i> Edit
              </button>
            </td>
            <td>
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


      <% if ("admin".equalsIgnoreCase(role)) { %>
      <div class="categories-section">
        <h2 class="section-title">Categories</h2>
        <div class="user-table-container">
          <table class="user-table">
            <thead>
            <tr>
              <th>Category ID</th>
              <th>Category Name</th>
              <th>Created Date</th>
              <th>Actions</th>
            </tr>
            </thead>
            <tbody>
            <% if(categories == null || categories.isEmpty()) { %>
            <tr>
              <td colspan="4" style="text-align: center;">No categories found</td>
            </tr>
            <% } else { %>
            <% for (Category category : categories) { %>
            <tr>
              <td><%= category.getCategoryId() %></td>
              <td><%= category.getCategoryName() %></td>
              <td>
                <%= category.getCreatedDate() != null ?
                        new SimpleDateFormat("yyyy-MM-dd HH:mm").format(category.getCreatedDate()) :
                        "N/A" %>
              </td>
              <td>
                <button class="delete-btn" data-id="<%= category.getCategoryId() %>">
                  <i class="fas fa-trash-alt"></i> Delete
                </button>
              </td>
            </tr>
            <% } %>
            <% } %>
            </tbody>
          </table>
        </div>
      </div>
      <% } %>
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
    function validateItemForm() {
      const name = document.getElementById('name').value.trim();
      const category = document.getElementById('category').value;
      const price = document.getElementById('price').value;
      const stockQty = document.getElementById('stockQty').value;

      // Clear previous errors
      document.querySelectorAll('.error-message').forEach(el => el.remove());

      let isValid = true;

      if (!name) {
        showFieldError('name', 'Item name is required');
        isValid = false;
      }

      if (!category) {
        showFieldError('category', 'Category is required');
        isValid = false;
      }

      const priceVal = parseFloat(price);
      if (isNaN(priceVal) || priceVal <= 0) {
        showFieldError('price', 'Please enter a valid positive price');
        isValid = false;
      }

      const qtyVal = parseInt(stockQty);
      if (isNaN(qtyVal) || qtyVal < 0) {
        showFieldError('stockQty', 'Please enter a valid stock quantity (0 or more)');
        isValid = false;
      }

      return isValid;
    }

    function showFieldError(fieldId, message) {
      const field = document.getElementById(fieldId);
      const errorDiv = document.createElement('div');
      errorDiv.className = 'error-message';
      errorDiv.style.color = 'red';
      errorDiv.style.fontSize = '0.8rem';
      errorDiv.style.marginTop = '5px';
      errorDiv.textContent = message;

      // Insert after the field
      field.parentNode.insertBefore(errorDiv, field.nextSibling);

      // Highlight field
      field.style.borderColor = 'red';
      setTimeout(() => {
        field.style.borderColor = '';
      }, 3000);
    }

    function showError(message) {
      const errorDiv = document.createElement('div');
      errorDiv.className = 'notification error';
      errorDiv.innerHTML = `<span>${message}</span><button class="close-btn">&times;</button>`;
      document.body.prepend(errorDiv);
      setTimeout(() => errorDiv.remove(), 5000);
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
    document.getElementById('newItemBtn')?.addEventListener('click', resetItemFormToAddMode);

    // Cancel edit functionality
    document.getElementById('cancelItemEditBtn')?.addEventListener('click', function() {
      resetItemFormToAddMode();
    });

    function resetItemFormToAddMode() {
      document.getElementById('itemForm').reset();
      document.getElementById('itemId').value = '';
      document.getElementById('formAction').value = 'add';

      // Update UI
      document.getElementById('addItemBtn').style.display = 'block';
      document.getElementById('updateItemBtn').style.display = 'none';
      document.getElementById('cancelItemEditBtn').style.display = 'none';
      document.querySelector('.page-title').textContent = 'Item Management';
    }

    // Edit button functionality - FIXED
    const editButtons = document.querySelectorAll('.edit-btn');
    const addItemBtn = document.getElementById('addItemBtn');
    const updateItemBtn = document.getElementById('updateItemBtn');
    const formAction = document.getElementById('formAction');
    const itemIdInput = document.getElementById('itemId');

    editButtons.forEach(button => {
      button.addEventListener('click', function() {
        const row = this.closest('tr');
        const cells = row.querySelectorAll('td');

        // Fill form fields - FIXED: using 'name' instead of 'itemName'
        document.getElementById('itemId').value = cells[0].textContent;
        document.getElementById('name').value = cells[1].textContent;

        // Set category - need to find the option that matches the text
        const categorySelect = document.getElementById('category');
        const categoryName = cells[2].textContent;
        for (let i = 0; i < categorySelect.options.length; i++) {
          if (categorySelect.options[i].text === categoryName) {
            categorySelect.selectedIndex = i;
            break;
          }
        }

        document.getElementById('price').value = cells[3].textContent.replace(/[^\d.]/g, '');
        document.getElementById('stockQty').value = cells[4].textContent;
        document.getElementById('description').value = cells[5].textContent || '';

        // Set hidden fields
        formAction.value = 'update';

        // Update UI
        addItemBtn.style.display = 'none';
        updateItemBtn.style.display = 'block';
        document.getElementById('cancelItemEditBtn').style.display = 'block';
        document.querySelector('.page-title').textContent = 'Update Item';

        // Scroll to form
        document.getElementById('itemForm').scrollIntoView({ behavior: 'smooth' });
      });
    });

    // Delete button functionality - IMPROVED
    const deleteButtons = document.querySelectorAll('.delete-btn');
    deleteButtons.forEach(button => {
      button.addEventListener('click', function() {
        const itemId = this.getAttribute('data-id');
        if (confirm('Are you sure you want to delete this item?')) {
          // Create a hidden form to submit the delete request
          const form = document.createElement('form');
          form.method = 'POST';
          form.action = '${pageContext.request.contextPath}/manage-items';

          // Add CSRF token if available
          const csrf = document.querySelector('input[name="_csrf"]');
          if (csrf) {
            form.appendChild(csrf.cloneNode());
          }

          // Add action parameter
          const actionInput = document.createElement('input');
          actionInput.type = 'hidden';
          actionInput.name = 'action';
          actionInput.value = 'delete';
          form.appendChild(actionInput);

          // Add itemId parameter
          const itemIdInput = document.createElement('input');
          itemIdInput.type = 'hidden';
          itemIdInput.name = 'itemId';
          itemIdInput.value = itemId;
          form.appendChild(itemIdInput);

          // Submit the form
          document.body.appendChild(form);
          form.submit();
        }
      });
    });

    // Clear button functionality
    const clearButton = document.getElementById('clearItemBtn');
    clearButton?.addEventListener('click', function() {
      resetItemFormToAddMode();
    });

    // Category Modal functionality
    const modal = document.getElementById('categoryModal');
    const addCategoryBtn = document.getElementById('addCategoryBtn');
    const closeModal = document.querySelector('.close-modal');
    const cancelCategoryBtn = document.getElementById('cancelCategoryBtn');

    addCategoryBtn?.addEventListener('click', function() {
      modal.style.display = 'block';
    });

    closeModal?.addEventListener('click', function() {
      modal.style.display = 'none';
    });

    cancelCategoryBtn?.addEventListener('click', function() {
      modal.style.display = 'none';
    });

    window.addEventListener('click', function(event) {
      if (event.target === modal) {
        modal.style.display = 'none';
      }
    });

    // Handle category form submission...................................
    const categoryForm = document.getElementById('categoryForm');
    if (categoryForm) {
      categoryForm.addEventListener('submit', function(e) {
        e.preventDefault();
        const categoryName = document.getElementById('newCategoryName').value.trim();

        if (!categoryName) {
          alert('Category name is required');
          return;
        }

        // Show loading state
        const submitBtn = this.querySelector('button[type="submit"]');
        const originalText = submitBtn.textContent;
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Adding...';

        // Create form data with the required action parameter
        const formData = new FormData();
        formData.append('action', 'add');
        formData.append('categoryName', categoryName);

        fetch('${pageContext.request.contextPath}/manage-categories', {
          method: 'POST',
          body: formData
        })
                .then(response => {
                  if (!response.ok) {
                    throw new Error('Network response was not ok');
                  }
                  return response.json();
                })
                .then(data => {
                  if (data.success) {
                    // Success handling remains the same
                    const categorySelect = document.getElementById('category');
                    const newOption = document.createElement('option');
                    newOption.value = data.categoryId;
                    newOption.textContent = categoryName;
                    categorySelect.appendChild(newOption);
                    newOption.selected = true;

                    // Close modal and reset form
                    modal.style.display = 'none';
                    this.reset();
                  } else {
                    alert(data.message || 'Failed to add category');
                  }
                })
                .catch(error => {
                  console.error('Error:', error);
                  alert('An error occurred while adding the category');
                })
                .finally(() => {
                  submitBtn.disabled = false;
                  submitBtn.innerHTML = originalText;
                });
      });
    }

    // Delete category button functionality (for admin) - IMPROVED
    function handleDeleteCategory(event) {
      const button = event.target.closest('.delete-category-btn');
      const categoryId = button.getAttribute('data-id');

      if (confirm('Are you sure you want to delete this category? Items in this category will not be deleted but will lose their category association.')) {
        const originalText = button.innerHTML;
        button.disabled = true;
        button.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Deleting...';

        const formData = new FormData();
        formData.append('action', 'delete');
        formData.append('categoryId', categoryId);

        fetch('${pageContext.request.contextPath}/manage-categories', {
          method: 'POST',
          body: formData
        })
                .then(response => {
                  if (!response.ok) throw new Error('Network error');
                  return response.json();
                })
                .then(data => {
                  if (data.success) {
                    // Remove from dropdown
                    const categorySelect = document.getElementById('category');
                    for (let i = 0; i < categorySelect.options.length; i++) {
                      if (categorySelect.options[i].value === categoryId) {
                        categorySelect.remove(i);
                        break;
                      }
                    }

                    // Remove from table
                    button.closest('tr').remove();

                    // Show empty state if no categories left
                    const categoriesTable = document.querySelector('.categories-section table tbody');
                    if (categoriesTable && categoriesTable.querySelectorAll('tr').length === 0) {
                      categoriesTable.innerHTML = `
                        <tr>
                            <td colspan="4" style="text-align: center;">No categories found</td>
                        </tr>
                    `;
                    }

                    showSuccess('Category deleted successfully!');
                  } else {
                    throw new Error(data.message || 'Failed to delete category');
                  }
                })
                .catch(error => {
                  console.error('Error:', error);
                  showError(error.message);
                })
                .finally(() => {
                  button.disabled = false;
                  button.innerHTML = originalText;
                });
      }
    }

// Initialize delete handlers for existing categories
    document.querySelectorAll('.delete-category-btn').forEach(btn => {
      btn.addEventListener('click', handleDeleteCategory);
    });

    function showSuccess(message) {
      const successDiv = document.createElement('div');
      successDiv.className = 'notification success';
      successDiv.innerHTML = `<span>${message}</span><button class="close-btn">&times;</button>`;
      document.body.prepend(successDiv);
      setTimeout(() => successDiv.remove(), 5000);
    }

    function showError(message) {
      const errorDiv = document.createElement('div');
      errorDiv.className = 'notification error';
      errorDiv.innerHTML = `<span>${message}</span><button class="close-btn">&times;</button>`;
      document.body.prepend(errorDiv);
      setTimeout(() => errorDiv.remove(), 5000);
    }


  });
</script>
</body>
</html>