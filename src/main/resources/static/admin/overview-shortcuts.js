const maxRows = 10;
const form = document.querySelector("#shortcutForm");
const rows = document.querySelector("#shortcutRows");
const rowCount = document.querySelector("#rowCount");
const template = document.querySelector("#rowTemplate");
const successMessage = document.querySelector("#successMessage");
const errorMessage = document.querySelector("#errorMessage");
const previewShortcutGrid = document.querySelector("#previewShortcutGrid");

function hideNotices() {
    successMessage.hidden = true;
    errorMessage.hidden = true;
}

function showNotice(element, message) {
    element.textContent = message;
    element.hidden = false;
}

function fieldValue(row, field) {
    return row.querySelector(`input[data-field='${field}']`).value.trim();
}

function currentShortcuts() {
    return [...rows.querySelectorAll("tr")]
        .map((row) => ({
            sortOrder: Number(fieldValue(row, "sortOrder")),
            name: fieldValue(row, "name"),
            iconUrl: fieldValue(row, "iconUrl"),
            targetUrl: fieldValue(row, "targetUrl"),
        }))
        .sort((first, second) => first.sortOrder - second.sortOrder);
}

function fallbackLetter(name) {
    return (name || "K").trim().charAt(0).toUpperCase();
}

function createFallbackIcon(name) {
    const fallback = document.createElement("div");
    fallback.className = "preview-icon preview-icon-fallback";
    fallback.textContent = fallbackLetter(name);
    return fallback;
}

function createPreviewIcon(shortcut) {
    if (!shortcut.iconUrl) {
        return createFallbackIcon(shortcut.name);
    }

    const icon = document.createElement("div");
    icon.className = "preview-icon";

    const image = document.createElement("img");
    image.src = shortcut.iconUrl;
    image.alt = "";
    image.loading = "lazy";
    image.addEventListener(
        "error",
        () => {
            icon.replaceWith(createFallbackIcon(shortcut.name));
        },
        {once: true}
    );

    icon.appendChild(image);
    return icon;
}

function renderPreview() {
    const shortcuts = currentShortcuts();
    previewShortcutGrid.replaceChildren();

    if (shortcuts.length === 0) {
        const empty = document.createElement("div");
        empty.className = "preview-empty";
        empty.textContent = "등록된 바로가기가 없습니다.";
        previewShortcutGrid.appendChild(empty);
        return;
    }

    shortcuts.forEach((shortcut) => {
        const tile = document.createElement("div");
        tile.className = "preview-shortcut";

        const name = document.createElement("div");
        name.className = "preview-name";
        name.textContent = shortcut.name || "이름 없음";

        tile.appendChild(createPreviewIcon(shortcut));
        tile.appendChild(name);
        previewShortcutGrid.appendChild(tile);
    });
}

function resequence() {
    [...rows.querySelectorAll("tr")].forEach((row, index) => {
        row.querySelectorAll("input[data-field]").forEach((input) => {
            input.name = `shortcuts[${index}].${input.dataset.field}`;
        });
        row.querySelector("input[data-field='sortOrder']").value = index;
    });
    rowCount.textContent = rows.querySelectorAll("tr").length;
    renderPreview();
}

document.querySelector("#addRow").addEventListener("click", () => {
    if (rows.querySelectorAll("tr").length >= maxRows) {
        return;
    }

    rows.appendChild(template.content.firstElementChild.cloneNode(true));
    resequence();
});

rows.addEventListener("click", (event) => {
    const button = event.target.closest("button[data-action]");
    if (!button) {
        return;
    }

    const row = button.closest("tr");
    const action = button.dataset.action;

    if (action === "remove") {
        row.remove();
    }

    if (action === "up" && row.previousElementSibling) {
        rows.insertBefore(row, row.previousElementSibling);
    }

    if (action === "down" && row.nextElementSibling) {
        rows.insertBefore(row.nextElementSibling, row);
    }

    resequence();
});

rows.addEventListener("input", (event) => {
    if (!event.target.matches("input[data-field]")) {
        return;
    }

    renderPreview();
});

form.addEventListener("submit", async (event) => {
    event.preventDefault();
    hideNotices();
    resequence();

    try {
        const response = await fetch(form.action, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({shortcuts: currentShortcuts()}),
        });

        if (response.ok) {
            showNotice(successMessage, "기본 바로가기를 저장했습니다.");
            return;
        }

        const errorBody = await response.json().catch(() => null);
        showNotice(errorMessage, errorBody?.message || "저장에 실패했습니다.");
    } catch (error) {
        showNotice(errorMessage, "저장에 실패했습니다.");
    }
});

resequence();
