const sidebar = document.getElementById("sidebar");
	    const showBtn = document.getElementById("Sidebar-show-Btn");
	    const hideBtn = document.getElementById("Sidebar-hide-Btn");

	    // Show sidebar
	    showBtn.addEventListener("click", () => {
	        sidebar.classList.add("active");
	    });

	    // Hide sidebar
	    hideBtn.addEventListener("click", () => {
	        sidebar.classList.remove("active");
	    });
		

		    document.querySelectorAll('.menu-toggle').forEach(menu => {
		        menu.addEventListener('click', function() {
		            const parent = this.parentElement;
		            parent.classList.toggle('active');
		        });
		    });

