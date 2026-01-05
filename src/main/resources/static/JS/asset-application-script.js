// asset-application-script.js

const table = document.getElementById('info-Table').querySelector("tbody");
const addBtn = document.getElementById('addRowBtn');
const recommendationText = document.getElementById('recommendationText');

// Utility: Format numbers (Indian style if needed)
function formatIndianNumber(value) {
  if (!value) return '';
  // Remove non-digit except dot
  value = value.replace(/[^\d.]/g, '');
  return value;
}

// Update recommendation paragraph based on selected provinces
function updateParagraph() {
  const selects = table.querySelectorAll('select');
  const selectedProvinces = new Set();

  selects.forEach(sel => {
    const selectedOption = sel.options[sel.selectedIndex];
    if (selectedOption && selectedOption.value) {
      const provinceMatch = selectedOption.textContent.match(/\(([^)]+)\)$/);
      if (provinceMatch) selectedProvinces.add(provinceMatch[1].trim());
    }
  });

  const provincesArray = Array.from(selectedProvinces);
  if (provincesArray.length > 1) {
    recommendationText.textContent =
      "विभिन्न प्रदेश कार्यालयको सिफारिस अनुसार तपशिल अनुसारको सामान वा आवश्यक बजेट व्यवस्थाको लागि सिफारिस गरिएको व्यहोरा अनुरोध छ ।";
  } else if (provincesArray.length === 1) {
    recommendationText.textContent =
      provincesArray[0] +
      " कार्यालयको सिफारिस अनुसार तपशिल अनुसारको सामान वा आवश्यक बजेट व्यवस्थाको लागि सिफारिस गरिएको व्यहोरा अनुरोध छ ।";
  } else {
    recommendationText.textContent =
      "प्रदेश कार्यालयको सिफारिस अनुसार तपशिल अनुसारको सामान वा आवश्यक बजेट व्यवस्थाको लागि सिफारिस गरिएको व्यहोरा अनुरोध छ ।";
  }
}

// Create branch select dynamically
function createBranchSelect(nameAttr, selectedValue = "") {
  const container = document.createElement("div");
  container.classList.add("branch-select-wrapper");

  const select = document.createElement("select");
  select.name = nameAttr;
  select.required = true;

  const defaultOption = document.createElement("option");
  defaultOption.value = "";
  defaultOption.disabled = true;
  defaultOption.selected = true;
  defaultOption.textContent = "-- शाखा चयन गर्नुहोस् --";
  select.appendChild(defaultOption);

  branches.forEach(b => {
    const opt = document.createElement("option");
    opt.value = b.branchCode;
    opt.textContent = `${b.branchNameNepali} (${b.provinceCode.provinceNameNepali})`;
    if (b.branchCode === selectedValue) opt.selected = true;
    select.appendChild(opt);
  });

  const span = document.createElement("span");
  span.classList.add("branch-print-text");
  span.textContent = selectedValue ? select.options[select.selectedIndex].textContent : "";

  select.addEventListener("change", () => {
    span.textContent = select.options[select.selectedIndex].textContent;
    updateParagraph();
  });

  container.appendChild(select);
  container.appendChild(span);

  return container;
}

// Update quantity and amount units (थान, /-)
function updateUnitsDisplay() {
  const rows = table.querySelectorAll('tr');
  rows.forEach(row => {
    const qtyInput = row.querySelector('input.quantity');
    if (qtyInput) {
	  qtyInput.value = formatIndianNumber(qtyInput.value);
      let thaanSpan = row.querySelector('.quantity-unit');
      if (!thaanSpan) {
        thaanSpan = document.createElement('span');
        thaanSpan.classList.add('quantity-unit');
        qtyInput.after(thaanSpan);
      }
      thaanSpan.textContent = qtyInput.value.trim() ? 'थान' : '';
   
    }

    const amountInput = row.querySelector('input.number:not(.requested-amount)');
    if (amountInput) {
      let amountSpan = row.querySelector('.amount-unit');
      if (!amountSpan) {
        amountSpan = document.createElement('span');
        amountSpan.classList.add('amount-unit');
        amountInput.after(amountSpan);
      }
      amountInput.value = formatIndianNumber(amountInput.value);
      amountSpan.textContent = amountInput.value.trim() ? '/-' : '';
    }

    const requestedInput = row.querySelector('input.requested-amount');
    if (requestedInput) {
      let requestedSpan = row.querySelector('.requested-unit');
      if (!requestedSpan) {
        requestedSpan = document.createElement('span');
        requestedSpan.classList.add('requested-unit');
        requestedInput.after(requestedSpan);
      }
      requestedInput.value = formatIndianNumber(requestedInput.value);
      requestedSpan.textContent = requestedInput.value.trim() ? '/-' : '';
    }
  });
}

// Update row indices and input names
function updateSerialNumbers() {
  const rows = table.querySelectorAll('tr');
  rows.forEach((row, idx) => {
    row.cells[0].textContent = idx + 1;
    row.querySelectorAll('input, select, textarea').forEach(el => {
      if (el.name) {
        const field = el.name.substring(el.name.indexOf('.') + 1);
        el.name = `assetHistories[${idx}].${field}`;
      }
    });
  });
}

// Attach remove event to a button
function attachRemoveEvent(btn) {
  btn.addEventListener('click', function () {
    const row = this.closest('tr');
    if (table.rows.length > 1) { // keep at least 1 row
      row.remove();
      updateSerialNumbers();
      updateParagraph();
      updateUnitsDisplay();
    } else {
      alert("Cannot remove all rows!");
    }
  });
}

// Add row button
addBtn.addEventListener('click', () => {
  const rowIndex = table.rows.length;
  const newRow = document.createElement('tr');

  newRow.innerHTML = `
    <td style="font-family: 'Himali';">${rowIndex}</td>
    <td class="printable-input"></td>
    <td><input type="text" name="assetHistories[${rowIndex}].itemDetails" class="printable-input"></td>
    <td><input type="text" name="assetHistories[${rowIndex}].quantity" class="printable-input quantity" required></td>
    <td><input type="text" name="assetHistories[${rowIndex}].amount" step="0.01" class="printable-input number"></td>
    <td><input type="text" name="assetHistories[${rowIndex}].requestedAmount" step="0.01" class="printable-input number requested-amount"></td>
    <td><textarea name="assetHistories[${rowIndex}].remark" class="printable-textarea"></textarea></td>
    <td><button type="button" class="removeRowBtn">-</button></td>
  `;

  // Insert dynamic branch select into 2nd cell
  const branchSelectContainer = createBranchSelect(`assetHistories[${rowIndex}].branch.branchCode`);
  newRow.cells[1].appendChild(branchSelectContainer);

  table.appendChild(newRow);

  attachRemoveEvent(newRow.querySelector('.removeRowBtn'));
  updateSerialNumbers();
  updateParagraph();
  updateUnitsDisplay();
});

// Initialize remove buttons for existing rows
document.querySelectorAll('.removeRowBtn').forEach(btn => attachRemoveEvent(btn));

// Update units on input dynamically
document.addEventListener('input', e => {
  if (e.target.matches('input.quantity, input.number, input.requested-amount')) {
    updateUnitsDisplay();
  }
});

// On page load: initialize branch select text and recommendation
document.addEventListener("DOMContentLoaded", () => {
  table.querySelectorAll(".branch-select-wrapper").forEach(wrapper => {
    const select = wrapper.querySelector("select");
    const span = wrapper.querySelector(".branch-print-text");
    if (select && span) {
      const selectedOption = select.options[select.selectedIndex];
      if (selectedOption) span.textContent = selectedOption.textContent;

      select.addEventListener("change", () => {
        span.textContent = select.options[select.selectedIndex].textContent;
        updateParagraph();
      });
    }
  });

  updateParagraph();
  updateUnitsDisplay();
});
