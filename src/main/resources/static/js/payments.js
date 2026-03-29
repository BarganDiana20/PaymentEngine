// CHECK LOGIN
const user = JSON.parse(sessionStorage.getItem("user"));
if (!user) {
  window.location.href = "/login.html";
}

// ==================== ELEMENTE ====================
const fromWalletSelect = document.getElementById("fromWallet");
const walletAvailable = document.getElementById("walletAvailable");
const currencyLabel = document.getElementById("currencyLabel");
const toWalletCodeInput = document.getElementById("toWalletCode");
const amountInput = document.getElementById("amount");
const descriptionInput = document.getElementById("description");
const submitBtn = document.getElementById("submitPayment");
const messageDiv = document.getElementById("paymentMessage");
const backBtn = document.getElementById("backToDashboard");
const scheduleCheckbox = document.getElementById("scheduleCheckbox");
const scheduledAtInput = document.getElementById("scheduledAt");

// ==================== LOAD USER WALLETS ====================
async function loadUserWallets() {
  try {
    const response = await fetch("/wallets/me");
    const wallets = await response.json();

    fromWalletSelect.innerHTML = "";

    wallets.forEach(wallet => {
      const option = document.createElement("option");
      option.value = wallet.id;
      option.text = wallet.nameWallet;
      option.dataset.balance = wallet.balance;
      option.dataset.currency = wallet.currency;
      fromWalletSelect.appendChild(option);
    });
   
    updateWalletInfo();  // display amount and currency for the first wallet  
  } catch (err) {
    console.error(err);
    fromWalletSelect.innerHTML = "<option>Error loading wallets</option>";
  }
}

// ==================== UPDATE AMOUNT AVAILABLE FROM WALLET ====================
function updateWalletInfo() {
  const selectedOption = fromWalletSelect.selectedOptions[0];
  
  if (!selectedOption) return;

  walletAvailable.innerText = `${selectedOption.dataset.balance} ${selectedOption.dataset.currency}`;
  currencyLabel.innerText = selectedOption.dataset.currency;
}

// ==================== EVENT CHANGE SELECT ====================
fromWalletSelect.addEventListener("change", updateWalletInfo);

// ==================== TOGGLE SCHEDULE INPUT ====================
scheduleCheckbox.addEventListener("change", () => {
    scheduledAtInput.style.display = scheduleCheckbox.checked ? "block" : "none";
});

// ==================== SUBMIT PAYMENT ====================
submitBtn.addEventListener("click", async () => {
  try {
    const walletId = parseInt(fromWalletSelect.value);
    const toWalletCode = parseInt(toWalletCodeInput.value);
    const amount = parseFloat(amountInput.value);
    const description = descriptionInput.value || "";
  
    if (!walletId || !toWalletCode || !amount) {
      showPopup("Please complete all required fields.");
      return;
    }

    let url = `/wallets/${walletId}/payments/transfer`;
    let body = new URLSearchParams({
            toWalletCode,
            amount,
            description
      });

    //Scheduled payment
    if (scheduleCheckbox.checked) {
      if (!scheduledAtInput.value) {
          showPopup("Select date and time for scheduled payment.");
          return;
      }
      url = `/wallets/${walletId}/payments/scheduled`;
      body.append("scheduledAt", scheduledAtInput.value);
    }

    const response = await fetch(url, {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: body
    });

    if (response.ok) { 
      const data = await response.json();
      const selectedOption = fromWalletSelect.selectedOptions[0];
      const currency = selectedOption.dataset.currency;

      let message;

    // ==================== MESSAGE LOGIC ====================
    if (data.scheduledAt) {
        // SCHEDULED PAYMENT
        const scheduledDate = formatDate(data.scheduledAt);
        message = `Scheduled payment created successfully ✔️
The transaction of ${amount} ${currency} to wallet ${toWalletCode} was scheduled for ${scheduledDate}.`;
      } else {
        // INSTANT PAYMENT
        const createdDate = formatDate(data.createdAt);
        message = `Payment completed successfully ✔️
The transaction of ${amount} ${currency} to wallet ${toWalletCode} was completed on ${createdDate}.`;
      }
     
      showPopup(message, () => {
          // reset inputs
          amountInput.value = "";
          descriptionInput.value = "";
          toWalletCodeInput.value = "";
          scheduleCheckbox.checked = false;
          scheduledAtInput.value = "";
          scheduledAtInput.style.display = "none";

          loadUserWallets(); // refresh balances
          window.location.href = "dashboard.html";
      });
      
    } else {
      console.error(await response.text());
      showPopup("Payment failed. Please check your input and try again.");
    }

  } catch (err) {
    console.error(err);
    showPopup("Server error. Please try again later.");
  }
});

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


// ==================== BACK BUTTON ====================
backBtn.addEventListener("click", () => {
  window.location.href = "/dashboard.html";
});

// ==================== INIT ====================
document.addEventListener("DOMContentLoaded", loadUserWallets);

document.getElementById("dashboardTab").addEventListener("click", () => {
  window.location.href = "/dashboard.html";
});

document.getElementById("paymentTab").addEventListener("click", () => {
  window.location.href = "/payments.html";
});

document.getElementById("walletsTab").addEventListener("click", () => {
  window.location.href = "/wallets.html";
});

document.getElementById("logoutBtn").addEventListener("click", async () => {
  await fetch("/users/logout", { method: "POST" });
  sessionStorage.clear();
  window.location.href = "/login.html";
});