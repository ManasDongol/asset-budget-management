const toggle = document.getElementById("toggleAddUser");
        const addUserBtn = document.getElementById("addUserBtn");
        const toggleText = document.getElementById("toggleText");

        toggle.addEventListener("change", () => {
            if (toggle.checked) {
                addUserBtn.disabled = false;
                toggleText.textContent = "Add User Enabled";
            } else {
                addUserBtn.disabled = true;
                toggleText.textContent = "Add User Disabled";
            }
        });