// ================= REGISTER =================
const registerForm = document.getElementById("registerForm")
    if (registerForm) {
        registerForm.addEventListener("submit", async (e) => {
        e.preventDefault();

        const user = {
            firstName: document.getElementById("firstName").value.trim(),
            lastName: document.getElementById("lastName").value.trim(),
            username: document.getElementById("username").value.trim(),
            email: document.getElementById("email").value.trim(),
            password: document.getElementById("password").value
        };
        
        if (!user.firstName || !user.lastName || !user.username || !user.email || !user.password) {
            showPopup("Please fill in all fields.");
            return;
        }

        try {
            const response = await fetch(`/users/register`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(user)
            });

            if (response.ok) {
                showPopup("Registration successful! You can now log in.",  () => {
                window.location.href = "login.html";
                });
            } else {
                const error = await response.text();
                showPopup("Registration failed: " + error);
            }
        } catch (err) {
            console.error("Server error:", err);
            showPopup("Server error. Try again later.");
        }
    });
}

