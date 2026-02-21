// app.js - Funcționalități AJAX pentru Banca Comercială

// ============================================
// UTILITARE
// ============================================

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('ro-RO', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function formatCurrency(amount, currency = 'MDL') {
    return new Intl.NumberFormat('ro-RO', {
        style: 'currency',
        currency: currency,
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    }).format(amount);
}

function showMessage(message, type = 'info') {
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${type}`;
    messageDiv.textContent = message;

    const container = document.querySelector('.container');
    container.insertBefore(messageDiv, container.firstChild);

    setTimeout(() => {
        messageDiv.remove();
    }, 5000);
}

// ============================================
// ÎNCĂRCARE TRANZACȚII CU AJAX
// ============================================

function loadTransactions(accountNumber, limit = 10) {
    fetch(`/api/transactions/account/${accountNumber}/last?limit=${limit}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Eroare la încărcarea tranzacțiilor');
            }
            return response.json();
        })
        .then(transactions => {
            displayTransactions(transactions);
        })
        .catch(error => {
            console.error('Error:', error);
            showMessage('Nu s-au putut încărca tranzacțiile', 'error');
        });
}

function displayTransactions(transactions) {
    const tbody = document.querySelector('#transactions-table tbody');
    if (!tbody) return;

    tbody.innerHTML = '';

    if (transactions.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center">Nu există tranzacții</td></tr>';
        return;
    }

    transactions.forEach(t => {
        const row = tbody.insertRow();
        row.className = t.status.toLowerCase();

        row.innerHTML = `
            <td>${formatDate(t.timestamp)}</td>
            <td>${t.type}</td>
            <td>${formatCurrency(t.amount, t.currency)}</td>
            <td>${t.currency}</td>
            <td>${t.description || '-'}</td>
            <td><span class="status-badge ${t.status.toLowerCase()}">${t.status}</span></td>
        `;
    });
}

// ============================================
// CURS VALUTAR ÎN TIMP REAL
// ============================================

function loadExchangeRates() {
    fetch('/api/public/exchange-rates')
        .then(response => response.json())
        .then(rates => {
            displayExchangeRates(rates);
        })
        .catch(error => {
            console.error('Error loading rates:', error);
        });
}

function displayExchangeRates(rates) {
    const container = document.querySelector('#exchange-rates');
    if (!container) return;

    let html = '<table><tr><th>Monedă</th><th>Curs (MDL)</th><th>Convertor</th></tr>';

    Object.entries(rates).forEach(([currency, rate]) => {
        if (currency !== 'MDL') {
            html += `
                <tr>
                    <td><strong>${currency}</strong></td>
                    <td>${rate.toFixed(4)} MDL</td>
                    <td>
                        <input type="number" id="amount-${currency}" placeholder="100" value="100">
                        <button onclick="convertCurrency('${currency}', ${rate})">=</button>
                        <span id="result-${currency}"></span> MDL
                    </td>
                </tr>
            `;
        }
    });

    html += '</table>';
    container.innerHTML = html;
}

function convertCurrency(currency, rate) {
    const amount = document.getElementById(`amount-${currency}`).value;
    const result = document.getElementById(`result-${currency}`);
    result.textContent = (amount * rate).toFixed(2);
}

// ============================================
// VALIDĂRI FORMULARE ÎN TIMP REAL
// ============================================

document.addEventListener('DOMContentLoaded', function() {
    // Validare număr cont (16 cifre)
    const accountInput = document.getElementById('accountNumber');
    if (accountInput) {
        accountInput.addEventListener('input', function(e) {
            const value = e.target.value.replace(/\D/g, '');
            if (value.length === 16) {
                e.target.classList.add('valid');
                e.target.classList.remove('invalid');
            } else {
                e.target.classList.add('invalid');
                e.target.classList.remove('valid');
            }
            e.target.value = value;
        });
    }

    // Validare sumă pozitivă
    const amountInputs = document.querySelectorAll('input[type="number"]');
    amountInputs.forEach(input => {
        input.addEventListener('input', function(e) {
            const value = parseFloat(e.target.value);
            if (value > 0) {
                e.target.classList.add('valid');
                e.target.classList.remove('invalid');
            } else {
                e.target.classList.add('invalid');
                e.target.classList.remove('valid');
            }
        });
    });

    // Încarcă cursul valutar dacă există containerul
    if (document.getElementById('exchange-rates')) {
        loadExchangeRates();
    }
});

// ============================================
// GRAFICE SIMPLE (dacă există canvas)
// ============================================

function drawBalanceChart(data) {
    const canvas = document.getElementById('balance-chart');
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    const width = canvas.width;
    const height = canvas.height;

    ctx.clearRect(0, 0, width, height);

    const maxValue = Math.max(...Object.values(data));
    const barWidth = width / (Object.keys(data).length * 2);

    let x = 50;
    Object.entries(data).forEach(([currency, value]) => {
        const barHeight = (value / maxValue) * (height - 100);

        // Desenează bara
        ctx.fillStyle = '#007bff';
        ctx.fillRect(x, height - barHeight - 50, barWidth, barHeight);

        // Desenează textul
        ctx.fillStyle = '#333';
        ctx.font = '12px Arial';
        ctx.fillText(currency, x, height - 30);
        ctx.fillText(value.toFixed(0), x, height - barHeight - 60);

        x += barWidth + 20;
    });
}

// ============================================
// CONFIRMARE ÎNAINTE DE ACȚIUNI CRITICE
// ============================================

function confirmAction(message, actionUrl) {
    if (confirm(message)) {
        window.location.href = actionUrl;
    }
}

// Adaugă event listeners pentru butoanele de ștergere/blocare
document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('.confirm-action').forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            const message = this.dataset.confirm || 'Sigur doriți să continuați?';
            const url = this.href;
            confirmAction(message, url);
        });
    });
});