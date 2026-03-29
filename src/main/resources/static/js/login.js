document.getElementById("loginForm").addEventListener("submit", async function (e) {
    e.preventDefault();

    const usernameOrEmail = document.getElementById("usernameOrEmail").value;
    const password = document.getElementById("password").value;

  try{
      const response = await fetch("/users/login", {
          method: "POST",
          headers: { "Content-Type": "application/x-www-form-urlencoded" },
          credentials: "include",
          body: new URLSearchParams({
            usernameOrEmail: usernameOrEmail,
            password: password,
          }),
        });

       if (!response.ok) {
          showPopup("Invalid username or password. Please try again.");
          return
        }

        const user = await response.json();
        // salvam user in sessionStorage
        sessionStorage.setItem("user", JSON.stringify(user));

        if (user.role === "ADMIN") {
                    window.location.href = "/admin-dashboard.html";
                } else {
                    window.location.href = "/dashboard.html";
                }

        } catch (error) {
                showPopup(error.message); }
});
