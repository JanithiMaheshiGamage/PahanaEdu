
if (!window.contextPath) {
    console.warn('contextPath not defined, defaulting to empty string');
    window.contextPath = '';
}

document.addEventListener('DOMContentLoaded', function() {
    // Configurable endpoints (adjust these to match your servlet mappings)
    const endpoints = {
        searchItems: `${window.contextPath}/search-items`,
        checkCustomer: `${window.contextPath}/billing?action=checkCustomer`, // change if your mapping is different
        addCustomer: `${window.contextPath}/billing?action=addCustomer`,
        selectCustomer: `${window.contextPath}/billing?action=selectCustomer`,
        clearCustomer: `${window.contextPath}/billing?action=clearCustomer`,
        generateBill: `${window.contextPath}/billing?action=generateBill`,
        downloadBill: `${window.contextPath}/billing?action=downloadBill`
    };
    // Add this function to generate a bill number
    function generateBillNumber() {
        const now = new Date();
        const year = now.getFullYear().toString().slice(-2);
        const month = (now.getMonth() + 1).toString().padStart(2, '0');
        const day = now.getDate().toString().padStart(2, '0');
        const random = Math.floor(1000 + Math.random() * 9000);
        return `BILL-${year}${month}${day}-${random}`;
    }

    // Add these validation functions
    function validateCustomerSelection() {
        const selectedCustomer = document.querySelector('#selectedCustomerCard');
        if (!selectedCustomer) {
            showNotification('Please select a customer first', 'error');
            return false;
        }
        return true;
    }

    function validatePaymentMethod() {
        const paymentMethod = document.querySelector('input[name="paymentMethod"]:checked');
        if (!paymentMethod) {
            showNotification('Please select a payment method', 'error');
            return false;
        }
        return true;
    }

    function validateCashPayment(totalAmount) {
        const amountReceived = parseFloat(document.getElementById('amountReceived').value) || 0;
        if (isNaN(amountReceived)) {
            showNotification('Please enter a valid amount received', 'error');
            return false;
        }
        if (amountReceived < totalAmount) {
            showNotification('Amount received is less than the total amount', 'error');
            return false;
        }
        return true;
    }

    function validateCartItems() {
        const cartItems = document.querySelectorAll('#cartItems tr[data-item-id]');
        if (cartItems.length === 0) {
            showNotification('Please add items to the cart', 'error');
            return false;
        }
        return true;
    }

    function validateCardPayment() {
        if (document.querySelector('input[name="paymentMethod"][value="card"]:checked')) {
            const cardNumber = document.getElementById('cardNumber').value.trim();
            const cardHolder = document.getElementById('cardHolder').value.trim();
            const expiryDate = document.getElementById('expiryDate').value.trim();
            const cvv = document.getElementById('cvv').value.trim();

            if (!/^\d{16}$/.test(cardNumber.replace(/\s/g, ''))) {
                showNotification('Please enter a valid 16-digit card number', 'error');
                return false;
            }
            if (!cardHolder || cardHolder.length < 3) {
                showNotification('Please enter card holder name', 'error');
                return false;
            }
            if (!/^(0[1-9]|1[0-2])\/?([0-9]{2})$/.test(expiryDate)) {
                showNotification('Please enter a valid expiry date (MM/YY)', 'error');
                return false;
            }
            if (!/^\d{3,4}$/.test(cvv)) {
                showNotification('Please enter a valid CVV (3 or 4 digits)', 'error');
                return false;
            }
        }
        return true;
    }

// Confirm bill button handler
    const confirmBillBtn = document.getElementById('confirmBillBtn');
    if (confirmBillBtn) {
        confirmBillBtn.addEventListener('click', async function() {
            // Validate all inputs
            if (!validateCustomerSelection()) return;
            if (!validateCartItems()) return;
            if (!validatePaymentMethod()) return;

            const cartTotal = parseFloat(document.getElementById('cartTotal').textContent.replace('LKR', '').trim()) || 0;
            const paymentMethod = document.querySelector('input[name="paymentMethod"]:checked').value;

            // Payment-specific validation
            if (paymentMethod === 'cash') {
                if (!validateCashPayment(cartTotal)) return;
            } else {
                if (!validateCardPayment()) return;
            }

            const username = document.getElementById('userData')?.dataset?.username || 'system';

            // Prepare bill data
            const billData = {
                billNo: generateBillNumber(),
                customerId: document.querySelector('#selectedCustomerCard').getAttribute('data-customer-id'),
                totalAmount: cartTotal,
                paymentMethod: paymentMethod,
                paymentDetails: paymentMethod === 'cash' ?
                    `Cash received: LKR ${parseFloat(document.getElementById('amountReceived').value).toFixed(2)}` :
                    `Card: ${document.getElementById('cardNumber').value.substring(0, 4)}...${document.getElementById('cardNumber').value.substring(15)}`,
                items: Array.from(document.querySelectorAll('#cartItems tr[data-item-id]')).map(itemRow => ({
                    itemId: parseInt(itemRow.getAttribute('data-item-id')),
                    quantity: parseInt(itemRow.querySelector('.item-quantity').textContent),
                    price: parseFloat(itemRow.querySelector('td:nth-child(4)').textContent.replace('LKR', '').trim()),
                    subtotal: parseFloat(itemRow.querySelector('.item-subtotal').textContent.replace('LKR', '').trim()),
                    createdBy: username
                }))
            };

            try {
                setButtonLoading(confirmBillBtn, true);

                const resp = await fetch(endpoints.generateBill, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json'
                    },
                    body: JSON.stringify(billData),
                    credentials: 'include'
                });

                const responseData = await resp.json();

                if (!resp.ok) {
                    // Handle validation errors from backend
                    if (responseData.errors) {
                        const errorMessages = Object.values(responseData.errors).join('\n');
                        throw new Error(errorMessages);
                    }
                    throw new Error(responseData.error || 'Failed to generate bill');
                }

                if (responseData.success) {
                    showNotification('Bill generated successfully! Bill No: ' + responseData.billNo, 'success');

                    // Add download button
                    const downloadBtn = document.createElement('button');
                    downloadBtn.className = 'btn btn-primary';
                    downloadBtn.innerHTML = '<i class="fas fa-file-pdf"></i> Download Bill';
                    downloadBtn.style.marginLeft = '10px';
                    downloadBtn.onclick = () => {
                        window.open(`${window.contextPath}/billing?action=downloadBill&billNo=${responseData.billNo}`, '_blank');
                    };

                    // Clear existing download button if any
                    const existingDownloadBtn = document.querySelector('.download-bill-btn');
                    if (existingDownloadBtn) {
                        existingDownloadBtn.remove();
                    }
                    downloadBtn.classList.add('download-bill-btn');
                    document.querySelector('.action-buttons').appendChild(downloadBtn);

                    // Reset form
                    document.getElementById('clearCartBtn').click();
                    document.getElementById('clearCustomerBtn').click();
                    document.getElementById('amountReceived').value = '';
                    document.getElementById('changeAmount').textContent = '0.00';

                    if (document.querySelector('input[name="paymentMethod"][value="card"]:checked')) {
                        document.getElementById('cardNumber').value = '';
                        document.getElementById('cardHolder').value = '';
                        document.getElementById('expiryDate').value = '';
                        document.getElementById('cvv').value = '';
                    }
                } else {
                    throw new Error(responseData.message || 'Failed to generate bill');
                }
            } catch (error) {
                console.error('Error generating bill:', error);
                showNotification(error.message || 'Error generating bill', 'error');
            } finally {
                setButtonLoading(confirmBillBtn, false);
            }
        });
    }

    // Real-time validation for cash payment
    document.getElementById('amountReceived')?.addEventListener('input', function() {
        const total = parseFloat(document.getElementById('cartTotal').textContent.replace('LKR', '').trim()) || 0;
        const received = parseFloat(this.value) || 0;
        const changeEl = document.getElementById('changeAmount');

        if (received >= total) {
            changeEl.textContent = (received - total).toFixed(2);
            changeEl.style.color = 'green';
        } else {
            changeEl.textContent = '0.00';
            changeEl.style.color = 'red';
        }
    });

    // Card validation
    document.getElementById('cardNumber')?.addEventListener('blur', function() {
        const cardNumber = this.value.replace(/\s/g, '');
        if (!/^\d{16}$/.test(cardNumber)) {
            this.classList.add('invalid');
        } else {
            this.classList.remove('invalid');
            this.value = cardNumber.replace(/(\d{4})/g, '$1 ').trim();
        }
    });

    document.getElementById('expiryDate')?.addEventListener('blur', function() {
        if (!/^(0[1-9]|1[0-2])\/?([0-9]{2})$/.test(this.value)) {
            this.classList.add('invalid');
        } else {
            this.classList.remove('invalid');
        }
    });

    document.getElementById('cvv')?.addEventListener('blur', function() {
        if (!/^\d{3,4}$/.test(this.value)) {
            this.classList.add('invalid');
        } else {
            this.classList.remove('invalid');
        }
    });


    // Initialize Select2 for item search
    $('#itemSearch').select2({
        placeholder: "Search by item ID or name",
        minimumInputLength: 1,
        ajax: {
            url: endpoints.searchItems,
            dataType: 'json',
            delay: 250,
            data: function(params) {
                return {
                    term: params.term // Make sure this matches the parameter name in your servlet
                };
            },
            processResults: function(data) {
                // Handle both success and error cases
                if (data.error) {
                    console.error("Search error:", data.error);
                    return { results: [] };
                }

                return {
                    results: (data.items || []).map(item => ({
                        id: item.itemId,
                        text: `${item.name} (${item.categoryName}) - LKR ${Number(item.price).toFixed(2)}`,
                        item: item // Store the full item object for later use
                    }))
                };
            },
            cache: true
        }
    });

    // Notification helper (jQuery used for convenience)
    function showNotification(message, type = 'info') {
        const notification = $(`
            <div class="notification ${type}">
                <span>${message}</span>
                <button class="close-btn">&times;</button>
            </div>
        `);
        $('body').append(notification);
        notification.find('.close-btn').click(() => notification.remove());
        setTimeout(() => notification.fadeOut(200, () => notification.remove()), 5000);
    }

    // UI helpers
    function setButtonLoading(btn, isLoading, loadingHtml = '<i class="fas fa-spinner fa-spin"></i>') {
        if (isLoading) {
            btn.dataset.origHtml = btn.innerHTML;
            btn.disabled = true;
            btn.innerHTML = `${loadingHtml} Loading...`;
        } else {
            btn.disabled = false;
            if (btn.dataset.origHtml) btn.innerHTML = btn.dataset.origHtml;
        }
    }

    // Update date/time
    function updateDateTime() {
        const now = new Date();
        const options = {
            day: '2-digit', month: '2-digit', year: 'numeric',
            hour: '2-digit', minute: '2-digit', hour12: true
        };
        const el = document.getElementById('currentDateTime');
        if (el) el.textContent = now.toLocaleString('en-GB', options);
    }
    updateDateTime();
    setInterval(updateDateTime, 60000);

    // Payment method toggle
    const paymentMethods = document.querySelectorAll('input[name="paymentMethod"]');
    const cashPaymentDetails = document.getElementById('cashPaymentDetails');
    const cardPaymentDetails = document.getElementById('cardPaymentDetails');
    if (paymentMethods) {
        paymentMethods.forEach(method => {
            method.addEventListener('change', function() {
                if (this.value === 'cash') {
                    cashPaymentDetails.style.display = 'block';
                    cardPaymentDetails.style.display = 'none';
                } else {
                    cashPaymentDetails.style.display = 'none';
                    cardPaymentDetails.style.display = 'block';
                }
            });
        });
    }

    // DOM elements used frequently
    const checkCustomerBtn = document.getElementById('checkCustomerBtn');
    const customerPhoneInput = document.getElementById('customerPhone');
    const customerSearchResults = document.getElementById('customerSearchResults');
    const amountReceivedInput = document.getElementById('amountReceived');
    const changeAmountDiv = document.getElementById('changeAmount');
    const cartItemsTable = document.getElementById('cartItems');
    const addItemBtn = document.getElementById('addItemBtn');
    const itemQuantityInput = document.getElementById('itemQuantity');

    // Utility: try to parse JSON safely
    async function safeJsonResponse(response) {
        const contentType = response.headers.get('content-type') || '';
        const text = await response.text();
        if (contentType.includes('application/json')) {
            try {
                return JSON.parse(text);
            } catch (e) {
                console.warn('Failed to parse JSON despite content-type; returning raw text', e);
                return { __raw: text };
            }
        } else {
            // not JSON: try JSON.parse anyway (some servers mis-set content-type)
            try {
                return JSON.parse(text);
            } catch (e) {
                return { __raw: text };
            }
        }
    }

    // Centralized checkCustomer function (used by click handler)
    async function checkCustomerByPhone(phone) {
        const url = `${endpoints.checkCustomer}&phone=${encodeURIComponent(phone)}`;
        setButtonLoading(checkCustomerBtn, true, '<i class="fas fa-spinner fa-spin"></i>');
        try {
            const response = await fetch(url, { method: 'GET', credentials: 'same-origin' });
            if (!response.ok) {
                // attempt to read body for debug
                const txt = await response.text();
                console.error(`HTTP error ${response.status}:`, txt);
                customerSearchResults.style.display = 'block';
                customerSearchResults.innerHTML = `
                    <div class="search-placeholder error">
                        <i class="fas fa-exclamation-circle"></i>
                        <p>Error: Server responded with status ${response.status}</p>
                    </div>`;
                showNotification(`Server error: ${response.status}`, 'error');
                return;
            }

            const payload = await safeJsonResponse(response);

            // If server returned HTML or raw text, payload will have __raw
            if (payload && payload.__raw) {
                console.error('Server returned non-JSON response for checkCustomer:', payload.__raw);
                customerSearchResults.style.display = 'block';
                customerSearchResults.innerHTML = `
                    <div class="search-placeholder error">
                        <i class="fas fa-exclamation-circle"></i>
                        <p>Unexpected server response. Check server logs (see console).</p>
                    </div>`;
                showNotification('Unexpected server response', 'error');
                return;
            }

            // Success path (assume expected JSON structure)
            customerSearchResults.style.display = 'block';
            if (payload.error) {
                customerSearchResults.innerHTML = `
                    <div class="search-placeholder error">
                        <i class="fas fa-exclamation-circle"></i>
                        <p>${payload.error}</p>
                    </div>`;
            } else if (Array.isArray(payload.customers) && payload.customers.length > 0) {
                let html = '<div class="customer-list">';
                payload.customers.forEach(customer => {
                    const nicHtml = customer.nic ? `<p><i class="fas fa-id-card"></i> ${customer.nic}</p>` : '';
                    html += `
                        <div class="customer-result" data-customer='${JSON.stringify(customer)}'>
                            <div class="customer-avatar"><i class="fas fa-user"></i></div>
                            <div class="customer-info">
                                <h4>${customer.name}</h4>
                                <p><i class="fas fa-phone"></i> ${customer.phoneNo}</p>
                                ${nicHtml}
                            </div>
                            <button class="btn btn-sm btn-primary select-customer">Select</button>
                        </div>`;
                });
                html += '</div>';
                customerSearchResults.innerHTML = html;

                // Attach handlers
                customerSearchResults.querySelectorAll('.select-customer').forEach(btn => {
                    btn.addEventListener('click', function() {
                        const customerData = JSON.parse(this.closest('.customer-result').getAttribute('data-customer'));
                        selectCustomer(customerData);
                    });
                });
            } else {
                customerSearchResults.innerHTML = `
                    <div class="search-placeholder">
                        <i class="fas fa-user-slash"></i>
                        <p>${payload.message || 'No customer found with this phone number'}</p>
                    </div>`;
            }
        } catch (err) {
            console.error('Error during checkCustomer:', err);
            customerSearchResults.style.display = 'block';
            customerSearchResults.innerHTML = `
                <div class="search-placeholder error">
                    <i class="fas fa-exclamation-circle"></i>
                    <p>Error searching for customer</p>
                </div>`;
            showNotification('Error searching for customer', 'error');
        } finally {
            setButtonLoading(checkCustomerBtn, false, '<i class="fas fa-spinner fa-spin"></i>');
            // restore text to "Check" explicitly if needed
            checkCustomerBtn.innerHTML = '<i class="fas fa-check"></i> Check';
        }
    }

    // Attach click handler
    if (checkCustomerBtn) {
        checkCustomerBtn.addEventListener('click', function() {
            const phoneNumber = (customerPhoneInput && customerPhoneInput.value || '').trim();
            if (!phoneNumber) {
                alert('Please enter a phone number');
                return;
            }
            checkCustomerByPhone(phoneNumber);
        });
    }

    // Select customer function (posts JSON to server to store in session)
    function selectCustomer(customer) {
        fetch(endpoints.selectCustomer, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(customer),
            credentials: 'same-origin'
        })
            .then(resp => {
                if (resp.ok) location.reload();
                else {
                    resp.text().then(t => {
                        console.error('selectCustomer error:', t);
                        showNotification('Failed to select customer', 'error');
                    });
                }
            })
            .catch(err => {
                console.error('selectCustomer error:', err);
                showNotification('Failed to select customer', 'error');
            });
    }

    // Clear selected customer
    const clearCustomerBtn = document.getElementById('clearCustomerBtn');
    if (clearCustomerBtn) {
        clearCustomerBtn.addEventListener('click', function() {
            fetch(endpoints.clearCustomer, { method: 'POST', credentials: 'same-origin' })
                .then(resp => { if (resp.ok) location.reload(); else showNotification('Failed to clear customer', 'error'); })
                .catch(err => { console.error(err); showNotification('Error clearing customer', 'error'); });
        });
    }

    // Add item to cart
    addItemBtn && addItemBtn.addEventListener('click', function() {
        const selectedData = $('#itemSearch').select2('data')[0];
        if (!selectedData || !selectedData.item) {
            alert('Please select an item');
            return;
        }

        const quantity = parseInt(itemQuantityInput.value, 10) || 1;
        if (quantity <= 0) {
            alert('Quantity must be at least 1');
            return;
        }

        const item = selectedData.item;
        const subtotal = item.price * quantity;

        // Remove placeholder "No items added yet" row if present
        const placeholderRow = cartItemsTable.querySelector('tr > td[colspan="7"], tr td[style*="No items added yet"]');
        if (placeholderRow) {
            cartItemsTable.innerHTML = ''; // clear placeholder
        }

        // Check if item already exists in cart
        let row = cartItemsTable.querySelector(`tr[data-item-id="${item.itemId}"]`);
        if (row) {
            const qtyCell = row.querySelector('.item-quantity');
            const existingQty = parseInt(qtyCell.textContent, 10) || 0;
            const newQty = existingQty + quantity;
            qtyCell.textContent = newQty;
            row.querySelector('.item-subtotal').textContent = `LKR ${(item.price * newQty).toFixed(2)}`;
        } else {
            row = document.createElement('tr');
            row.setAttribute('data-item-id', item.itemId);
            row.innerHTML = `
                <td>${item.itemId}</td>
                <td>${item.name}</td>
                <td>${item.categoryName || ''}</td>
                <td>LKR ${Number(item.price).toFixed(2)}</td>
                <td class="item-quantity">${quantity}</td>
                <td class="item-subtotal">LKR ${Number(subtotal).toFixed(2)}</td>
                <td><button class="btn btn-secondary remove-item-btn" data-item-id="${item.itemId}">
                    <i class="fas fa-trash"></i> Remove
                </button></td>
            `;
            cartItemsTable.appendChild(row);

            // Add event listener to remove button
            row.querySelector('.remove-item-btn').addEventListener('click', function() {
                const id = this.dataset.itemId;
                removeItemFromCart(id);
            });
        }

        updateCartTotal();
        $('#itemSearch').val(null).trigger('change');
        itemQuantityInput.value = 1;
    });

    // Remove item
    function removeItemFromCart(itemId) {
        const row = cartItemsTable.querySelector(`tr[data-item-id="${itemId}"]`);
        if (row) {
            row.remove();
            // If table becomes empty, add placeholder row
            if (!cartItemsTable.querySelector('tr')) {
                cartItemsTable.innerHTML = '<tr><td colspan="7" style="text-align: center;">No items added yet</td></tr>';
            }
            updateCartTotal();
        }
    }

    // Update cart total
    function updateCartTotal() {
        let total = 0;
        cartItemsTable.querySelectorAll('.item-subtotal').forEach(cell => {
            const val = parseFloat(cell.textContent.replace('LKR', '').trim()) || 0;
            total += val;
        });
        const cartTotalEl = document.getElementById('cartTotal');
        if (cartTotalEl) cartTotalEl.textContent = `LKR ${total.toFixed(2)}`;

        // update change if cash
        if (document.querySelector('input[name="paymentMethod"]:checked')?.value === 'cash') {
            const received = parseFloat(amountReceivedInput.value) || 0;
            const change = received - total;
            changeAmountDiv.textContent = change >= 0 ? change.toFixed(2) : '0.00';
        }
    }

    // Clear cart
    const clearCartBtn = document.getElementById('clearCartBtn');
    if (clearCartBtn) {
        clearCartBtn.addEventListener('click', function() {
            if (confirm('Are you sure you want to clear all items from the cart?')) {
                cartItemsTable.innerHTML = '<tr><td colspan="7" style="text-align: center;">No items added yet</td></tr>';
                updateCartTotal();
            }
        });
    }

    // Add customer modal handling and form submission
    const addCustomerModal = document.getElementById('addCustomerModal');
    const addCustomerBtn = document.getElementById('addCustomerBtn');
    const closeModalButtons = document.querySelectorAll('.close-modal');
    const customerForm = document.getElementById('customerForm');

    addCustomerBtn && addCustomerBtn.addEventListener('click', function() {
        addCustomerModal.style.display = 'block';
        document.body.classList.add('body-modal-open');
    });

    closeModalButtons.forEach(button => {
        button.addEventListener('click', function() {
            addCustomerModal.style.display = 'none';
            document.body.classList.remove('body-modal-open');
        });
    });

    if (customerForm) {
        customerForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            const saveBtn = this.querySelector('button[type="submit"]');
            setButtonLoading(saveBtn, true);

            try {
                // Construct customer data
                const customerData = {
                    name: document.getElementById('newCustomerName').value.trim(),
                    phoneNo: document.getElementById('newCustomerPhone').value.trim(),
                    nic: document.getElementById('newCustomerNIC').value.trim(),
                    email: document.getElementById('newCustomerEmail').value.trim(),
                    address: document.getElementById('newCustomerAddress').value.trim()
                };

                // Validate required fields
                if (!customerData.name || !customerData.phoneNo) {
                    throw new Error("Name and Phone Number are required");
                }

                // Use the correct endpoint
                const resp = await fetch(`${window.contextPath}/billing?action=addCustomer`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json'
                    },
                    body: JSON.stringify(customerData),
                    credentials: 'include'
                });

                if (!resp.ok) {
                    const errorData = await resp.json().catch(() => ({}));
                    throw new Error(errorData.error || `Server responded with status ${resp.status}`);
                }

                const data = await resp.json();

                // Close modal and select the new customer
                addCustomerModal.style.display = 'none';
                document.body.classList.remove('body-modal-open');
                selectCustomer(data);

                // Show success message
                showNotification('Customer added successfully!', 'success');

            } catch (error) {
                console.error('Error:', error);
                showNotification(error.message || 'Error adding customer', 'error');
            } finally {
                setButtonLoading(saveBtn, false);
            }
        });
    }

    // Close modals on outside click
    window.addEventListener('click', function(event) {
        if (event.target === addCustomerModal) {
            addCustomerModal.style.display = 'none';
            document.body.classList.remove('body-modal-open');
        }
        const billConfirmationModal = document.getElementById('billConfirmationModal');
        if (event.target === billConfirmationModal) {
            billConfirmationModal.style.display = 'none';
            document.body.classList.remove('body-modal-open');
        }
    });

    if (responseData.success) {
        showNotification('Bill generated successfully! Bill No: ' + responseData.billNo, 'success');

        // Add download button
        const downloadBtn = document.createElement('button');
        downloadBtn.className = 'btn btn-primary';
        downloadBtn.innerHTML = '<i class="fas fa-download"></i> Download Bill';
        downloadBtn.onclick = () => {
            window.open(`${window.contextPath}/billing?action=downloadBill&billNo=${responseData.billNo}`, '_blank');
        };

        // Add it near your confirmation button or in a suitable place
        document.querySelector('.action-buttons').appendChild(downloadBtn);

        // Reset form...
    }


});
