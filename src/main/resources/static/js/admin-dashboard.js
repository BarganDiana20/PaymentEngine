
const storedUser = sessionStorage.getItem("user");

const tabButtons = document.querySelectorAll(".tab-btn[data-tab]");
const transactionsContainer = document.getElementById("transactionsContainer");
const fetchBtn = document.getElementById("fetchBtn");
const userNameInput = document.getElementById("userNameInput");
const walletCodeInput = document.getElementById("walletCodeInput"); 
const userGroup = document.getElementById("userGroup");
const walletGroup = document.getElementById("walletGroup");
const resultsBar = document.getElementById("resultsBar");
const resultsBadge = document.getElementById("resultsBadge");

const logoutBtn = document.getElementById("logoutBtn");

if (!storedUser) {
    window.location.href = "/login.html";
} else {
    const user = JSON.parse(storedUser);
    if (user.role !== "ADMIN") {
        window.location.href = "/dashboard.html";
    }
}

let currentTab = "user";

// Handle tab switching
tabButtons.forEach(btn => {
    btn.addEventListener("click", () => {
        if (!btn.dataset.tab) return;

        currentTab = btn.dataset.tab;

        tabButtons.forEach(b => b.classList.remove("active"));
        btn.classList.add("active");

        if (currentTab === "user") {
            userGroup.style.display = "block";
            walletGroup.style.display = "none";
        } else if (currentTab === "wallet") {
            userGroup.style.display = "none";
            walletGroup.style.display = "block";
        }
        
        transactionsContainer.innerHTML = "";
        resultsBar.style.display = "none";
    });
});

// Fetch transactions
fetchBtn.addEventListener("click", async () => {
    transactionsContainer.innerHTML = "<p style='color:white;text-align:center;'>Loading...</p>";
    let url = "";

    try {
        if (currentTab === "user") {
            const username = userNameInput.value.trim();
            if (!username) return showPopup("Please enter username");

            resultsBar.style.display = "flex"; 
            resultsBadge.textContent = `All transactions for user ${username}:`;
            
            url = `/admin/users/username/${encodeURIComponent(username)}/transactions`;
        
        } else {
            const walletCode = walletCodeInput.value.trim();
            if (!walletCode) return showPopup("Enter wallet code");

            resultsBar.style.display = "flex"; 
            resultsBadge.textContent = `All transactions from wallet with code ${walletCode}:`;
            
            url = `/admin/wallets/code/${walletCode}/transactions`;
        }

        const response = await fetch(url, { credentials: "include" });
        
        if (!response.ok) {
            throw new Error("No transactions found or invalid data");
        }

        const transactions = await response.json();
        renderTransactions(transactions);

    } catch (err) {
        showPopup(err.message);
        transactionsContainer.innerHTML = "";
        resultsBar.style.display = "none"; 
    }
});

// Render transaction cards
function renderTransactions(transactions) {
    transactionsContainer.innerHTML = "";
    
    if (!transactions.length) {
        transactionsContainer.innerHTML =  
            "<p style='color:white;text-align:center;'>No transactions found.</p>";
        return;
    }

    transactions.forEach(tx => {
        const type = tx.type?.toLowerCase();
        const isCredit = type === "credit";
        const typeLabel = isCredit ? "⬇️ CREDIT" : "⬆️ DEBIT";
        const amountClass = isCredit ? "tx-credit" : "tx-debit";
        const sign = isCredit ? "+" : "-";

        const date = formatDate(tx.createdAt);
        const status = tx.status ? tx.status.toLowerCase() : "unknown";

        const card = document.createElement("div");
        card.className = "transaction-card";

        card.innerHTML = `
            <div class="tx-header">
                <div class="tx-type">${typeLabel}</div>
                <div class="tx-amount ${amountClass}">
                    ${sign}${tx.amount} ${tx.currency}
                </div>
            </div>
            
            <div class="tx-person">
                 ${tx.type?.toLowerCase() === "credit" ? "From" : "To"} 
                 ${tx.counterpartyFirstName || ""} ${tx.counterpartyLastName || ""}
            </div>

            <div class="tx-desc">Description: ${tx.description || ""} </div>
            <div class="tx-date">
                <i class='fa-regular fa-calendar' style='font-size:14px'></i> ${date}
            </div>
            
            <div class="tx-status">
                Status: ${status.charAt(0).toUpperCase() + status.slice(1)} 
            </div>
        `;           
        
        transactionsContainer.appendChild(card);
    });
}

// Cancel payment
async function cancelPayment(walletId, paymentId, btn) {
    if (!confirm("Are you sure you want to cancel this payment?")) return;

    btn.disabled = true;
    try {
        const response = await fetch(`/wallets/${walletId}/payments/${paymentId}/cancel`, {
            method: "PUT",
            credentials: "include"
        });
        if (!response.ok) throw new Error("Failed to cancel payment");
        btn.parentElement.querySelector("p:nth-child(3)").textContent = "Status: Cancelled";
        btn.remove();
    } catch (err) {
        showPopup(err.message);
        btn.disabled = false;
    }
}


// ==================== FORMAT DATE ====================
function formatDate(dateString) {
  if (!dateString) return "No date";

  const normalized = dateString.replace(" ", "T");
  const parsed = new Date(normalized);

  if (isNaN(parsed)) return dateString;

  const day = parsed.getDate();
  const month = parsed.toLocaleString("en-US", { month: "short" });
  const year = parsed.getFullYear();

  return `${day} ${month} ${year}`;
}

// Logout
logoutBtn.addEventListener("click", async () => {
    await fetch("/users/logout", {
        method: "POST",
        credentials: "include"
    });

    sessionStorage.clear();
    window.location.href = "/login.html";
});

