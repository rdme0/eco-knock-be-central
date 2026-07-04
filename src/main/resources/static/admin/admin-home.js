const apiDocStatus = document.querySelector("#apiDocStatus");
const apiDocToggle = document.querySelector("#apiDocToggle");
const controlSettingStatus = document.querySelector("#controlSettingStatus");
const controlSettingToggle = document.querySelector("#controlSettingToggle");

let controlSetting = null;

function renderStatus(statusElement, enabled, enabledText, disabledText) {
    statusElement.dataset.enabled = String(enabled);
    statusElement.textContent = enabled ? enabledText : disabledText;
}

function renderApiDocAccess(enabled) {
    renderStatus(apiDocStatus, enabled, "공개 중", "비공개");
    apiDocToggle.checked = enabled;
    apiDocToggle.disabled = false;
}

function renderApiDocAccessError() {
    renderStatus(apiDocStatus, false, "공개 중", "상태 확인 실패");
    apiDocToggle.checked = false;
    apiDocToggle.disabled = false;
}

async function loadApiDocAccess() {
    apiDocToggle.disabled = true;

    try {
        const response = await fetch("/admin/api-docs-access");
        if (!response.ok) {
            renderApiDocAccessError();
            return;
        }

        const body = await response.json();
        renderApiDocAccess(Boolean(body.result?.enabled));
    } catch (error) {
        renderApiDocAccessError();
    }
}

async function updateApiDocAccess(enabled) {
    apiDocToggle.disabled = true;

    try {
        const response = await fetch("/admin/api-docs-access", {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({enabled}),
        });

        if (!response.ok) {
            renderApiDocAccessError();
            return;
        }

        const body = await response.json();
        renderApiDocAccess(Boolean(body.result?.enabled));
    } catch (error) {
        renderApiDocAccessError();
    }
}

function renderControlSetting(setting) {
    controlSetting = setting;
    renderStatus(controlSettingStatus, setting.enabled, "사용 중", "사용 안 함");
    controlSettingToggle.checked = setting.enabled;
    controlSettingToggle.disabled = false;
}

function renderControlSettingError() {
    controlSetting = null;
    renderStatus(controlSettingStatus, false, "사용 중", "상태 확인 실패");
    controlSettingToggle.checked = false;
    controlSettingToggle.disabled = false;
}

async function loadControlSetting() {
    controlSettingToggle.disabled = true;

    try {
        const response = await fetch("/admin/control-settings/value");
        if (!response.ok) {
            renderControlSettingError();
            return;
        }

        const body = await response.json();
        renderControlSetting(body.result);
    } catch (error) {
        renderControlSettingError();
    }
}

async function updateControlSetting(enabled) {
    controlSettingToggle.disabled = true;

    try {
        const response = await fetch("/admin/control-settings/enabled", {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({enabled}),
        });

        if (!response.ok) {
            renderControlSettingError();
            return;
        }

        const body = await response.json();
        renderControlSetting(body.result);
    } catch (error) {
        renderControlSettingError();
    }
}

apiDocToggle.addEventListener("change", () => {
    updateApiDocAccess(apiDocToggle.checked);
});

controlSettingToggle.addEventListener("change", () => {
    updateControlSetting(controlSettingToggle.checked);
});

loadApiDocAccess();
loadControlSetting();
