const storedUser = sessionStorage.getItem("user");

if(!storedUser){
    window.location.href = "/login.html";
}

const user = JSON.parse(storedUser);

document.getElementById("profileFullName").textContent =
`${user.firstName || ""} ${user.lastName || ""}`;

document.getElementById("profileUsername").textContent =
user.username || "-";

document.getElementById("profileEmail").textContent =
user.email || "-";


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
