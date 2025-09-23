document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("userForm");
    const formBtn = document.getElementById("formBtn");
	const HeadingText = document.getElementById("headingText");
	const SubHeadingText = document.getElementById("subheadingText");

    document.querySelectorAll(".edit-btn").forEach(btn => {
        btn.addEventListener("click", e => {
            e.preventDefault();

            // Fill form with selected user’s data
            document.getElementById("userId").value = btn.dataset.id;
            document.getElementById("username").value = btn.dataset.username;
            document.getElementById("staffCode").value = btn.dataset.staffcode;
            document.getElementById("authoriser").value = btn.dataset.authoriser;

            // Select password fields
            const passwordField = document.getElementById("password");
            const confirmPasswordField = document.getElementById("confirmPassword");

            // Clear values
            passwordField.value = "";
            confirmPasswordField.value = "";

            // Remove required attribute
            passwordField.removeAttribute("required");
            confirmPasswordField.removeAttribute("required");

            // Update placeholder text
            passwordField.placeholder = "Leave empty to keep current";
            confirmPasswordField.placeholder = "Leave empty to keep current";

            // Change form action & button text
            form.setAttribute("action", "/updatetheuser"); 
			
            formBtn.innerHTML = '<i class="fa fa-save"></i> Update User';
			HeadingText.innerHTML='<h1>Update User</h1>';
			SubHeadingText.innerHTML='<h2> <i class="fa fa-pencil"></i> Update Old User </h2>'
        });
    });
});