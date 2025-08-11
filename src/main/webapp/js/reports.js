// Global variables for chart instances
let salesChart;
let inventoryChart;

// Initialize the page when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    initializePage();
});

function initializePage() {
    // Set up current date and time
    updateDateTime();
    setInterval(updateDateTime, 1000);

    // Initialize date pickers with default range (last 30 days)
    setDefaultDateRange();

    // Initialize charts with empty data
    initializeCharts();

    // Set up event listeners
    setupEventListeners();

    // Load initial data
    loadInitialData();
}

function setDefaultDateRange() {
    const endDate = new Date();
    const startDate = new Date();
    startDate.setDate(startDate.getDate() - 30);

    document.getElementById('startDate').valueAsDate = startDate;
    document.getElementById('endDate').valueAsDate = endDate;
}

function initializeCharts() {
    // Initialize sales chart with empty data
    const salesCtx = document.getElementById('salesChart').getContext('2d');
    salesChart = new Chart(salesCtx, {
        type: 'line',
        data: {
            labels: [],
            datasets: [{
                label: 'Daily Sales',
                data: [],
                backgroundColor: 'rgba(75, 192, 192, 0.2)',
                borderColor: 'rgba(75, 192, 192, 1)',
                borderWidth: 2,
                tension: 0.4
            }]
        },
        options: getChartOptions('Daily Sales (LKR)')
    });

    // Initialize inventory chart
    const inventoryCtx = document.getElementById('inventoryChart').getContext('2d');
    inventoryChart = new Chart(inventoryCtx, {
        type: 'doughnut',
        data: {
            labels: ['Loading...'],
            datasets: [{
                data: [1],
                backgroundColor: ['rgba(201, 203, 207, 0.7)']
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    position: 'bottom'
                }
            }
        }
    });
}

function getChartOptions(title) {
    return {
        responsive: true,
        plugins: {
            title: {
                display: true,
                text: title
            },
            tooltip: {
                callbacks: {
                    label: function(context) {
                        return 'LKR ' + context.parsed.y.toFixed(2);
                    }
                }
            }
        },
        scales: {
            y: {
                beginAtZero: true,
                ticks: {
                    callback: function(value) {
                        return 'LKR ' + value;
                    }
                }
            }
        }
    };
}

function setupEventListeners() {
    // Filter form submission
    document.getElementById('reportFilterForm').addEventListener('submit', function(e) {
        e.preventDefault();
        applyFilters();
    });

    // Export button
    document.getElementById('exportBtn').addEventListener('click', exportReport);
}

function loadInitialData() {
    applyFilters();
}

function updateDateTime() {
    const now = new Date();
    const options = {
        weekday: 'long',
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    };
    document.getElementById('currentDateTime').textContent = now.toLocaleDateString('en-US', options);
}

async function applyFilters() {
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;

    if (!validateDates(startDate, endDate)) {
        return;
    }

    try {
        showLoadingState();
        updatePeriodDisplay(startDate, endDate);

        // Construct URL with context path
        const contextPath = window.location.pathname.split('/')[1] || '';
        const url = `/${contextPath}/reports?action=getSalesData&startDate=${startDate}T00:00:00&endDate=${endDate}T23:59:59`;

        console.log("Request URL:", url); // Debug log

        const response = await fetch(url, {
            headers: {
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error(`Server returned ${response.status}: ${response.statusText}`);
        }

        const reportData = await response.json();
        updateDashboard(reportData);
    } catch (error) {
        handleError(error);
    }
}

function validateDates(startDate, endDate) {
    if (!startDate || !endDate) {
        showError('Please select both start and end dates');
        return false;
    }

    if (new Date(startDate) > new Date(endDate)) {
        showError('End date must be after start date');
        return false;
    }

    return true;
}

function showLoadingState() {
    document.getElementById('totalRevenue').textContent = 'Loading...';
    document.getElementById('transactionCount').textContent = 'Loading...';
}

async function fetchReportData(startDate, endDate) {
    const response = await fetch(`/reports?action=getSalesData&startDate=${startDate}&endDate=${endDate}`);

    if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = await response.json();

    if (!data.success) {
        throw new Error(data.message || 'Failed to load report data');
    }

    return data;
}

function updateDashboard(data) {
    // Update summary metrics
    updateSummaryMetrics(data.summary);

    // Update sales chart
    updateSalesChart(data.dailySales);

    // Update tables
    updateSalesByCategoryTable(data.byCategory);
    updateTopSellingItemsTable(data.topItems);
}

function updateSummaryMetrics(summary) {
    document.getElementById('totalRevenue').textContent = formatCurrency(summary.totalRevenue);
    document.getElementById('transactionCount').textContent = summary.transactionCount.toLocaleString();
}

function updateSalesChart(dailySales) {
    const labels = dailySales.map(item => formatDateForChart(new Date(item.date)));
    const data = dailySales.map(item => item.total || 0);

    salesChart.data.labels = labels;
    salesChart.data.datasets[0].data = data;
    salesChart.update();
}

function formatDateForChart(date) {
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
}

function updateSalesByCategoryTable(categories) {
    const tbody = document.querySelector('.report-table:first-of-type tbody');
    tbody.innerHTML = '';

    categories.forEach(category => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${category.category}</td>
            <td>${category.itemsSold}</td>
            <td>${formatCurrency(category.totalRevenue)}</td>
            <td>${category.percentage}%</td>
        `;
        tbody.appendChild(row);
    });
}

function updateTopSellingItemsTable(items) {
    const tbody = document.querySelector('.report-table:last-of-type tbody');
    tbody.innerHTML = '';

    items.forEach(item => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${item.item}</td>
            <td>${item.category}</td>
            <td>${item.quantitySold}</td>
            <td>${formatCurrency(item.revenue)}</td>
        `;
        tbody.appendChild(row);
    });
}

function updatePeriodDisplay(startDate, endDate) {
    const start = new Date(startDate);
    const end = new Date(endDate);

    document.getElementById('salesPeriod').textContent =
        `${formatDateForDisplay(start)} to ${formatDateForDisplay(end)}`;
}

function formatDateForDisplay(date) {
    return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
    });
}

function formatCurrency(amount) {
    return 'LKR ' + (amount || 0).toLocaleString('en-US', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}

function exportReport() {
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;

    if (!validateDates(startDate, endDate)) {
        return;
    }

    // Show loading state on button
    const exportBtn = document.getElementById('exportBtn');
    const originalHtml = exportBtn.innerHTML;
    exportBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Exporting...';
    exportBtn.disabled = true;

    // Trigger download
    const url = `/reports/export?startDate=${startDate}&endDate=${endDate}`;
    const a = document.createElement('a');
    a.href = url;
    a.download = `sales_report_${startDate}_to_${endDate}.pdf`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);

    // Reset button after delay
    setTimeout(() => {
        exportBtn.innerHTML = originalHtml;
        exportBtn.disabled = false;
    }, 2000);
}

function handleError(error) {
    console.error('Error:', error);
    showError(error.message);
    resetMetrics();
}

function resetMetrics() {
    document.getElementById('totalRevenue').textContent = 'LKR 0.00';
    document.getElementById('transactionCount').textContent = '0';
}

function showError(message) {
    // Remove any existing errors
    const existingErrors = document.querySelectorAll('.error-message');
    existingErrors.forEach(el => el.remove());

    // Create error element
    const errorDiv = document.createElement('div');
    errorDiv.className = 'error-message alert alert-danger';
    errorDiv.innerHTML = `
        <i class="fas fa-exclamation-circle"></i>
        <span>${message}</span>
    `;

    // Insert error message
    const contentWrapper = document.querySelector('.content-wrapper');
    contentWrapper.insertBefore(errorDiv, contentWrapper.firstChild);

    // Auto-remove after 5 seconds
    setTimeout(() => {
        errorDiv.remove();
    }, 5000);
}