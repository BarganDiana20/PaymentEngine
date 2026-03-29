const user = JSON.parse(sessionStorage.getItem("user"));

if (!user) {
    window.location.href = "/login.html";
}

document.getElementById("greeting-text").innerText =
        "Hello, " + user.firstName + "!";

const profileBtn = document.getElementById("profileBtn");
profileBtn.addEventListener("click", () => {
    window.location.href = "/profile.html";
});

const walletInfo = document.getElementById("wallet-info");

async function loadMainWallet() {
    try {
        const response = await fetch(`/wallets/me`);
        if (!response.ok) throw new Error("Failed to load wallet");

        const wallets = await response.json();
        
        if (!Array.isArray(wallets) || wallets.length === 0) {
            walletInfo.innerHTML = "<p>No wallets available!</p>";
            return;
        }
        
        wallets.sort((a, b) => a.id - b.id);

        const mainWallet = wallets[0]; // the first wallet created is considered the user's main wallet
        walletInfo.innerHTML = `
            <div class="main-wallet-card">
                <h2>Main Wallet</h2>
                <h3>${mainWallet.nameWallet} </h3>
                <div class="wallet-icon"> 
                   <img src="images/wallet-icon2.png" alt="Wallet Icon" width="100">
                </div>
                <div class="main-wallet-balance">
                    ${mainWallet.balance}  ${mainWallet.currency}
                </div>
                
            </div>
        `;
    } catch (err) {
        console.error(err);
        walletInfo.innerHTML = "<p>Error loading wallet.</p>";
    }
}
loadMainWallet();

// Events for Dashboard, Payment, Wallets and Logout buttons
document.getElementById("dashboardTab").addEventListener("click", () => {
    location.reload();
});

document.getElementById("paymentTab").addEventListener("click", () => {
        window.location.href = "/payments.html";
});

document.getElementById("walletsTab").addEventListener("click", () => {
    window.location.href = "/wallets.html";
});

document.getElementById("logoutBtn").addEventListener("click", async function() {

    await fetch("/users/logout", { method: "POST" });
    sessionStorage.clear();
    window.location.href = "login.html";
});