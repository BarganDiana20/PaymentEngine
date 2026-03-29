// ==================== CHECK LOGIN ====================
const user = JSON.parse(sessionStorage.getItem("user"));
if (!user) {
  window.location.href = "/login.html";
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

// ==================== ELEMENTE HTML ====================
const walletsList = document.getElementById("wallets-list");
const walletDetails = document.getElementById("wallet-details");
const transactionsList = document.getElementById("transactions-list");
const allTransactionsBtn = document.getElementById("allTransactionsBtn");
const walletsTitle = document.querySelector(".wallets-title");

const createWalletBtn = document.getElementById("createWalletBtn");
const createWalletForm = document.getElementById("create-wallet-form");
const submitWalletBtn = document.getElementById("submitWalletBtn");
const backCreateWalletBtn = document.getElementById("backCreateWalletBtn");
const backCreateWrapper = document.getElementById("backCreateWrapper");
const backBtn = document.getElementById("backBtn");

// ==================== CREATE WALLETS ====================
createWalletBtn.addEventListener("click", () => {

  walletsList.style.display = "none";
  walletDetails.style.display = "none";
  transactionsList.style.display = "none";
  allTransactionsBtn.style.display = "none";

  createWalletBtn.style.display = "none";
  backCreateWrapper.style.display = "block";
  createWalletForm.style.display = "flex";

  walletsTitle.innerText = "Create New Wallet"; 
});

backCreateWalletBtn.addEventListener("click", () => {

  createWalletForm.style.display = "none";
  backCreateWrapper.style.display = "none";

  createWalletBtn.style.display = "inline-block";
  walletsList.style.display = "flex";

  walletsTitle.innerText = "My Wallets";

});

submitWalletBtn.addEventListener("click", async () => {

  const name = document.getElementById("walletName").value;
  const balance = document.getElementById("walletBalance").value;
  const currency = document.getElementById("walletCurrency").value;

  if (!name || !balance || !currency) {
    showPopup("Please fill all fields.");
    return;
  }

  try {
    const response = await fetch(
      `/wallets?nameWallet=${name}&balance=${balance}&currency=${currency}`,
      { method: "POST" }
    );

    if (response.status === 201) {

      showPopup("Wallet created successfully!", () =>{

      createWalletForm.style.display = "none";
      backCreateWrapper.style.display = "none";
      createWalletBtn.style.display = "inline-block";
      walletsList.style.display = "flex";

      // reset inputuri
      document.getElementById("walletName").value = "";
      document.getElementById("walletBalance").value = "";
      document.getElementById("walletCurrency").value = "";

      walletsTitle.innerText = "My Wallets";

      loadWallets(); // refresh list
      });
    } else {
      showPopup("Failed to create wallet.");
    }

  } catch (err) {
    console.error(err);
    showPopup("Server error. Please try again later.");
  }

});

// ==================== LOAD WALLETS ====================
async function loadWallets() {
  try {
    const response = await fetch("/wallets/me");
    const wallets = await response.json();

    if (!Array.isArray(wallets) || wallets.length === 0) {
      walletsList.innerHTML = "<p>No wallets available!</p>";
      return;
    }

    wallets.sort((a, b) => a.id - b.id);

    walletsList.innerHTML = "";
    walletDetails.style.display = "none";
    transactionsList.style.display = "none";
    allTransactionsBtn.style.display = "none";
    createWalletBtn.style.display = "inline-block";

    wallets.forEach((wallet, index) => {
      const status = wallet.active ? "ACTIVE" : "INACTIVE";
      const label = index === 0 ? `${wallet.nameWallet}` : wallet.nameWallet;
      
      const card = document.createElement("div");
      card.classList.add("wallet-card");
      card.innerHTML = `
      <div class="wallet-row">
        <div class="wallet-icon2">
            <img src="images/wallet-icon2.png" alt="Wallet Icon">
        </div>
        
        <div class="wallet-info-column">
            <div class="wallet-name">${label}</div>
            <div class="wallet-amount">${wallet.balance} ${wallet.currency}</div>
            <div class="wallet-status ${status.toLowerCase()}"> ${status}</div>
          </div>
        
         <i class='fa-solid fa-chevron-right chevron' style="font-size: 15px;"></i>
      </div>
    `;

      card.addEventListener("click", () => showWalletDetails(wallet));
      walletsList.appendChild(card);
    });
  } catch (err) {
    console.error(err);
    walletsList.innerHTML = "<p>Could not load wallets</p>";
  }

}

// ==================== SHOW WALLET DETAILS ====================
async function showWalletDetails(wallet) {
  createWalletBtn.style.display = "none";

  walletsList.style.display = "none";
  walletsTitle.style.display = "block";
  transactionsList.style.display = "none";
  walletDetails.style.display = "block";
  

  allTransactionsBtn.style.display = "inline-block";
  allTransactionsBtn.innerText = "All Transactions History";
  allTransactionsBtn.onclick = () => showAllTransactions(wallet);

  let recentTx = [];

  try {
    const txResponse = await fetch(`/wallets/${wallet.id}/transactions/details`);
    if (txResponse.ok) {
      const allTx = await txResponse.json();
      allTx.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)); 
      recentTx = allTx.slice(0, 1); 
    } 
} catch (err) { 
    console.error("Error loading transactions:", err); }

  const status = wallet.active ? "ACTIVE" : "INACTIVE";

    walletDetails.innerHTML = `
        <div class="wallet-details-card">
            <h3>${wallet.nameWallet}</h3>
            <p><strong>Wallet code:</strong> ${wallet.walletCode}</p>
            <p><strong>Available Balance:</strong> ${wallet.balance} ${wallet.currency}</p>
            <p><strong>Status:</strong> 
                <span class="wallet-status ${status.toLowerCase()}">${status}</span>
            </p>
        </div>

        <h4>Recent Transactions:</h4>
            ${
              recentTx.length > 0
                ? recentTx
                    .map((tx) => {
                      const isCredit = tx.type.toLowerCase() === "credit" && tx.walletId === wallet.id;
                      const icon = isCredit ? "trending_up" : "trending_down";
                      const sign = isCredit ? "+" : "-";
                      const amountClass = isCredit ? "credit" : "debit";
                      
                      return `
                         <div class="recent-tx-card">
                            <div class="tx-icon-box ${isCredit ? "credit-bg" : "debit-bg"}">
                              <span class="material-symbols-outlined">${icon}</span>
                            </div>

                            <div class="tx-info">
                              <p class="tx-person2">
                                ${tx.counterpartyFirstName || ""} ${tx.counterpartyLastName || ""}
                              </p>
                              <p class="tx-desc">${tx.description || "No description"}</p>
                            </div>

                            <div class="tx-amount-box">
                              <p class="tx-amount ${amountClass}">
                                ${sign}${tx.amount} ${wallet.currency}
                              </p>
                            </div>
                          </div>
                       `;
                  })
                .join("")
              : "<li>No recent transactions</li>"
            }
     `;
}

// ==================== SHOW ALL TRANSACTIONS ====================
async function showAllTransactions(wallet) {
  createWalletBtn.style.display = "none";
  walletsTitle.style.display = "none";
  walletsTitle.innerText = "Transactions History";

  walletDetails.style.display = "none";
  walletsList.style.display = "none";
  transactionsList.style.display = "block";
  allTransactionsBtn.style.display = "none";

  backBtn.style.display = "inline-block";
  backBtn.onclick = () => {
        backBtn.style.display = "none";
        walletsTitle.innerText = "My Wallets";
        showWalletDetails(wallet);
  };

  let allTx = [];

  try {
    const response = await fetch(`/wallets/${wallet.id}/transactions/details`);
    if (response.ok) {
      allTx = await response.json();
    }
  } catch (err) {
    console.error("Error loading all transactions:", err);
  }

  if (!Array.isArray(allTx) || allTx.length === 0) {
    transactionsList.innerHTML = "<p>No transactions found.</p>";
    return;
  }

  transactionsList.innerHTML = `
    <h3>Transactions History:</h3>
     ${allTx.map((tx) => {
        const type = tx.type?.toLowerCase();
        const isCredit = type === "credit";
        const typeLabel = isCredit ? "⬇️ CREDIT" : "⬆️ DEBIT";
        const amountClass = isCredit ? "tx-credit" : "tx-debit";
        const sign = isCredit ? "+" : "-";

        const date = formatDate(tx.createdAt);
        const status = tx.status ? tx.status.toLowerCase() : "unknown";

        return `
         <div class="transaction-card"> 
                  <div class="tx-left"> 
                    <div class="tx-type">${typeLabel}</div> 

                    <div class="tx-person">
                        ${tx.type?.toLowerCase() === "credit" ? "From" : "To"} 
                        ${tx.counterpartyFirstName || ""} ${tx.counterpartyLastName || ""}
                    </div>

                    <div class="tx-desc">Description: ${tx.description || "No description"}</div>
                    <div class="tx-date">
                      <i class='fa-regular fa-calendar' style='font-size:14px'></i> ${date}
                    </div>
                    <div class="tx-status"> 
                        Status: ${status.charAt(0).toUpperCase() + status.slice(1)}
                    </div> 
                </div> 
                
            <div class="tx-amount ${amountClass}"> 
                ${sign}${tx.amount} ${wallet.currency} 
            </div> 
        </div> 
    `;
    }).join("")}
`;
}
    

    
// ==================== INIT ====================
document.addEventListener("DOMContentLoaded", loadWallets);
/* ================= TAB EVENTS ================= */

document.getElementById("dashboardTab").addEventListener("click", () => {
  window.location.href = "/dashboard.html";
});

document.getElementById("paymentTab").addEventListener("click", () => {
  window.location.href = "/payments.html";
});

document.getElementById("walletsTab").addEventListener("click", () => {
  location.reload();
});

document.getElementById("logoutBtn").addEventListener("click", async () => {
  await fetch("/users/logout", { method: "POST" });
  sessionStorage.clear();
  window.location.href = "/login.html";
});