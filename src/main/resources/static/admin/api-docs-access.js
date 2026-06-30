const apiDocStatus = document.querySelector("#apiDocStatus");
const apiDocToggle = document.querySelector("#apiDocToggle");

function renderApiDocAccess(enabled) {
    apiDocStatus.dataset.enabled = String(enabled);
    apiDocStatus.textContent = enabled ? "공개 중" : "비공개";
    apiDocToggle.textContent = enabled ? "비활성화" : "활성화";
    apiDocToggle.dataset.nextEnabled = String(!enabled);
    apiDocToggle.disabled = false;
}

function renderApiDocAccessError() {
    apiDocStatus.dataset.enabled = "false";
    apiDocStatus.textContent = "상태 확인 실패";
    apiDocToggle.textContent = "다시 시도";
    apiDocToggle.dataset.nextEnabled = "true";
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

apiDocToggle.addEventListener("click", async () => {
    const nextEnabled = apiDocToggle.dataset.nextEnabled === "true";
    apiDocToggle.disabled = true;

    try {
        const response = await fetch("/admin/api-docs-access", {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({enabled: nextEnabled}),
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
});

loadApiDocAccess();
