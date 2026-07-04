const form = document.querySelector("#controlSettingForm");
const saveButton = document.querySelector("#saveButton");
const resetDefaultsButton = document.querySelector("#resetDefaultsButton");
const dirtyHint = document.querySelector("#dirtyHint");
const toast = document.querySelector("#toast");
const controlledSettings = document.querySelector("#controlledSettings");
const enabledBadge = document.querySelector("#enabledBadge");
const summaryDark = document.querySelector("#summaryDark");
const summaryAir = document.querySelector("#summaryAir");
const summaryCooldown = document.querySelector("#summaryCooldown");

let savedSetting = null;
let toastTimer = null;
let saving = false;

const sliderUnits = {
    darkLuxThreshold: "lux",
    brightLuxThreshold: "lux",
    darkDetectionTimeThreshold: "분",
    brightDetectionTimeThreshold: "분",
    airQualityDetectionTimeThreshold: "분",
    badAirQualityRatioThreshold: "%",
    cleanAirQualityRatioThreshold: "%",
    cooldownMinutes: "분",
};

function field(fieldName) {
    return form.querySelector(`[data-field='${fieldName}']`);
}

function slider(fieldName) {
    return form.querySelector(`[data-slider='${fieldName}']`);
}

function valuePill(fieldName) {
    return form.querySelector(`[data-value='${fieldName}']`);
}

function numericValue(fieldName) {
    return Number(field(fieldName).value);
}

function currentSetting() {
    return {
        enabled: field("enabled").checked,
        darkLuxThreshold: numericValue("darkLuxThreshold"),
        brightLuxThreshold: numericValue("brightLuxThreshold"),
        darkDetectionTimeThreshold: numericValue("darkDetectionTimeThreshold"),
        brightDetectionTimeThreshold: numericValue("brightDetectionTimeThreshold"),
        airQualityDetectionTimeThreshold: numericValue("airQualityDetectionTimeThreshold"),
        badAirQualityRatioThreshold: numericValue("badAirQualityRatioThreshold"),
        cleanAirQualityRatioThreshold: numericValue("cleanAirQualityRatioThreshold"),
        cooldownMinutes: numericValue("cooldownMinutes"),
    };
}

function normalizeSetting(setting) {
    return {
        enabled: Boolean(setting.enabled),
        darkLuxThreshold: Number(setting.darkLuxThreshold),
        brightLuxThreshold: Number(setting.brightLuxThreshold),
        darkDetectionTimeThreshold: Number(setting.darkDetectionTimeThreshold),
        brightDetectionTimeThreshold: Number(setting.brightDetectionTimeThreshold),
        airQualityDetectionTimeThreshold: Number(setting.airQualityDetectionTimeThreshold),
        badAirQualityRatioThreshold: Number(setting.badAirQualityRatioThreshold),
        cleanAirQualityRatioThreshold: Number(setting.cleanAirQualityRatioThreshold),
        cooldownMinutes: Number(setting.cooldownMinutes),
    };
}

function sameSetting(first, second) {
    return JSON.stringify(normalizeSetting(first)) === JSON.stringify(normalizeSetting(second));
}

function renderSetting(setting) {
    const normalized = normalizeSetting(setting);

    setControlValue("enabled", normalized.enabled);
    setControlValue("darkLuxThreshold", normalized.darkLuxThreshold);
    setControlValue("brightLuxThreshold", normalized.brightLuxThreshold);
    setControlValue("darkDetectionTimeThreshold", normalized.darkDetectionTimeThreshold);
    setControlValue("brightDetectionTimeThreshold", normalized.brightDetectionTimeThreshold);
    setControlValue("airQualityDetectionTimeThreshold", normalized.airQualityDetectionTimeThreshold);
    setControlValue("badAirQualityRatioThreshold", normalized.badAirQualityRatioThreshold);
    setControlValue("cleanAirQualityRatioThreshold", normalized.cleanAirQualityRatioThreshold);
    setControlValue("cooldownMinutes", normalized.cooldownMinutes);

    renderSummary(normalized);
    renderEnabledState(normalized.enabled);
    renderDirtyState();
}

function setControlValue(fieldName, value) {
    if (fieldName === "enabled") {
        field(fieldName).checked = Boolean(value);
        return;
    }

    const hiddenInput = field(fieldName);
    const rangeInput = slider(fieldName);
    const numeric = Number(value);

    if (numeric > Number(rangeInput.max)) {
        rangeInput.max = String(numeric);
    }

    if (numeric < Number(rangeInput.min)) {
        rangeInput.min = String(numeric);
    }

    hiddenInput.value = numeric;
    rangeInput.value = numeric;
    renderSlider(fieldName);
}

function renderSlider(fieldName) {
    const rangeInput = slider(fieldName);
    if (!rangeInput) {
        return;
    }

    const min = Number(rangeInput.min);
    const max = Number(rangeInput.max);
    const value = Number(rangeInput.value);
    const fill = max === min ? 0 : ((value - min) / (max - min)) * 100;
    rangeInput.style.setProperty("--fill", `${Math.min(100, Math.max(0, fill))}%`);

    const unit = sliderUnits[fieldName] || "";
    valuePill(fieldName).textContent = `${value}${unit}`;
}

function syncSlider(fieldName) {
    const value = Number(slider(fieldName).value);
    field(fieldName).value = value;
    renderSlider(fieldName);
}

function renderSummary(setting = currentSetting()) {
    enabledBadge.dataset.enabled = String(setting.enabled);
    enabledBadge.textContent = setting.enabled ? "사용 중" : "꺼짐";

    if (!setting.enabled) {
        summaryDark.textContent = "자동제어가 꺼져 있어 조건을 만족해도 공기청정기를 제어하지 않습니다.";
        summaryAir.textContent = "아래 기준은 저장된 채로 유지되며, 다시 켜면 그대로 적용됩니다.";
        summaryCooldown.textContent = `자동제어를 다시 켜면 제어 후 ${setting.cooldownMinutes}분 동안 추가 제어를 멈춥니다.`;
        return;
    }

    summaryDark.textContent = `어두움이 ${setting.darkDetectionTimeThreshold}분 지속되면 공기청정기를 끕니다.`;
    summaryAir.textContent =
        `밝음이 ${setting.brightDetectionTimeThreshold}분 이상 지속되고, 최근 ${setting.airQualityDetectionTimeThreshold}분 중 ` +
        `나쁜 공기질 ${setting.badAirQualityRatioThreshold}% 이상이면 켭니다.`;
    summaryCooldown.textContent =
        `최근 ${setting.airQualityDetectionTimeThreshold}분 중 깨끗한 공기질 ${setting.cleanAirQualityRatioThreshold}% 이상이면 끄고, ` +
        `제어 후 ${setting.cooldownMinutes}분 동안 추가 제어를 멈춥니다.`;
}

function renderEnabledState(enabled) {
    controlledSettings.dataset.enabled = String(enabled);
    form.querySelectorAll("input[type='range'][data-slider]").forEach((rangeInput) => {
        rangeInput.disabled = saving || !enabled;
    });
}

function renderDirtyState() {
    if (!savedSetting) {
        saveButton.disabled = true;
        saveButton.classList.remove("is-dirty");
        dirtyHint.textContent = "설정을 불러오는 중";
        return;
    }

    const dirty = !sameSetting(currentSetting(), savedSetting);
    saveButton.disabled = !dirty;
    saveButton.classList.toggle("is-dirty", dirty);
    dirtyHint.textContent = dirty ? "저장하지 않은 변경 사항이 있습니다." : "변경 사항 없음";
}

function showToast(message, type) {
    clearTimeout(toastTimer);
    toast.textContent = message;
    toast.dataset.type = type;
    toast.hidden = false;

    toastTimer = setTimeout(() => {
        toast.hidden = true;
    }, 2400);
}

function setSaving(nextSaving) {
    saving = nextSaving;
    saveButton.disabled = nextSaving;
    resetDefaultsButton.disabled = nextSaving;
    saveButton.textContent = nextSaving ? "저장 중..." : "저장";
    field("enabled").disabled = nextSaving;
    renderEnabledState(field("enabled").checked);
}

async function loadSetting() {
    try {
        const response = await fetch("/admin/control-settings/value");
        if (!response.ok) {
            showToast("설정을 불러오지 못했습니다.", "error");
            return;
        }

        const body = await response.json();
        savedSetting = normalizeSetting(body.result);
        renderSetting(savedSetting);
    } catch (error) {
        showToast("설정을 불러오지 못했습니다.", "error");
    }
}

async function loadDefaultSetting() {
    resetDefaultsButton.disabled = true;

    try {
        const response = await fetch("/admin/control-settings/default-value");
        if (!response.ok) {
            showToast("기본값을 불러오지 못했습니다.", "error");
            return;
        }

        const body = await response.json();
        renderSetting(body.result);
        showToast("기본값으로 되돌렸습니다. 저장해야 적용됩니다.", "success");
    } catch (error) {
        showToast("기본값을 불러오지 못했습니다.", "error");
    } finally {
        resetDefaultsButton.disabled = false;
        renderDirtyState();
    }
}

form.addEventListener("input", () => {
    renderSummary();
    renderEnabledState(field("enabled").checked);
    renderDirtyState();
});

form.addEventListener("change", () => {
    renderSummary();
    renderEnabledState(field("enabled").checked);
    renderDirtyState();
});

form.querySelectorAll("input[type='range'][data-slider]").forEach((rangeInput) => {
    rangeInput.addEventListener("input", () => {
        syncSlider(rangeInput.dataset.slider);
    });
});

resetDefaultsButton.addEventListener("click", () => {
    loadDefaultSetting();
});

form.addEventListener("submit", async (event) => {
    event.preventDefault();

    if (saveButton.disabled) {
        return;
    }

    setSaving(true);

    try {
        const response = await fetch(form.action, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(currentSetting()),
        });

        const body = await response.json().catch(() => null);

        if (response.ok) {
            savedSetting = normalizeSetting(body.result);
            renderSetting(savedSetting);
            showToast("자동제어 설정을 저장했습니다.", "success");
            return;
        }

        showToast(body?.message || "저장에 실패했습니다.", "error");
    } catch (error) {
        showToast("저장에 실패했습니다.", "error");
    } finally {
        setSaving(false);
        renderDirtyState();
    }
});

loadSetting();
