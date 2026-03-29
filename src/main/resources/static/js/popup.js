function showPopup(message, callback) {
    const popup = document.querySelector(".popup-overlay");
    popup.querySelector(".popup-message").textContent = message;
    popup.style.display = "flex";

    const btn = popup.querySelector(".popup-btn");
    btn.onclick = () => {
        popup.style.display = "none";
        if (callback) callback();
    };
}