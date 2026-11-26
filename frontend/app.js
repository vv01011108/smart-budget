const isLocal = ["localhost", "127.0.0.1"].includes(window.location.hostname);
const API_BASE = isLocal
    ? "http://localhost:8080/api/v1"
    : "https://smart-budget-api-mui6.onrender.com/api/v1";

const yearMonthInput = document.getElementById("yearMonthInput");
const alertBox = document.getElementById("alertBox");

// 예산 탭 요약 카드 계산용 상태
const state = {
    budget: null,
    fixedTotal: 0,
    txTotal: 0,
};

const formatKRW = (value) => `${Number(value ?? 0).toLocaleString("ko-KR")}원`;

function showAlert(message, type = "success") {
    alertBox.textContent = message;
    alertBox.className = `alert alert-${type}`;
    clearTimeout(showAlert._timer);
    showAlert._timer = setTimeout(() => alertBox.classList.add("hidden"), 4000);
}

// 바텀바 탭 전환
function switchTab(tabName) {
    document.querySelectorAll(".tab-panel").forEach((panel) => {
        panel.classList.toggle("active", panel.id === `tab-${tabName}`);
    });
    document.querySelectorAll(".nav-item").forEach((btn) => {
        btn.classList.toggle("active", btn.dataset.tab === tabName);
    });
    window.scrollTo({ top: 0, behavior: "smooth" });
}

document.querySelectorAll(".nav-item").forEach((btn) => {
    btn.addEventListener("click", () => switchTab(btn.dataset.tab));
});

// 예산 요약 카드 갱신
function updateOverview() {
    document.getElementById("overviewFixed").textContent = formatKRW(state.fixedTotal);
    document.getElementById("overviewTx").textContent = formatKRW(state.txTotal);

    if (state.budget === null) {
        document.getElementById("overviewBudget").textContent = "미등록";
        document.getElementById("overviewRemaining").textContent = "예산을 등록해 주세요";
    } else {
        const remaining = state.budget - state.fixedTotal - state.txTotal;
        document.getElementById("overviewBudget").textContent = formatKRW(state.budget);
        document.getElementById("overviewRemaining").textContent = formatKRW(remaining);
    }
}

// 공통 fetch 래퍼
async function apiFetch(path, options = {}) {
    const response = await fetch(`${API_BASE}${path}`, {
        headers: { "Content-Type": "application/json", ...options.headers },
        ...options,
    });

    if (response.status === 204) return null;

    const data = await response.json().catch(() => null);
    if (!response.ok) {
        const message = data?.message || `요청 실패 (HTTP ${response.status})`;
        throw new Error(message);
    }
    return data;
}

// 1. 예산 조회 & 저장
async function loadBudget() {
    const yearMonth = yearMonthInput.value;
    try {
        const data = await apiFetch(`/budgets/${yearMonth}`);
        const amount = Number(data.amount ?? data.monthlyBudget);
        state.budget = amount;
        document.getElementById("budgetAmount").value = amount;
    } catch {
        state.budget = null;
        document.getElementById("budgetAmount").value = "";
    }
    updateOverview();
}

document.getElementById("budgetForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const yearMonth = yearMonthInput.value;
    const amount = Number(document.getElementById("budgetAmount").value);

    try {
        await apiFetch(`/budgets/${yearMonth}`, {
            method: "PUT",
            body: JSON.stringify({ yearMonth, amount }),
        });
        showAlert(`${yearMonth} 월 예산이 저장되었습니다.`);
        await loadBudget();
    } catch (err) {
        showAlert(err.message, "error");
    }
});

// 2. 구매 시뮬레이터 & AI 재무 조언
function renderSimulation(sim, aiComment = null, aiGenerated = false) {
    const box = document.getElementById("simResult");
    const badge = document.getElementById("simStatusBadge");
    const aiTag = document.getElementById("simAiTag");

    badge.textContent = sim.status;
    badge.className = `badge badge-${sim.status}`;
    aiTag.classList.toggle("hidden", !aiGenerated);

    document.getElementById("simMessage").textContent = aiComment || sim.message;
    document.getElementById("mCurrentRemaining").textContent = formatKRW(sim.currentRemainingBudget);
    document.getElementById("mAfterRemaining").textContent = formatKRW(sim.remainingAfterPurchase);
    document.getElementById("mRemainingDays").textContent = `${sim.remainingDays}일`;
    document.getElementById("mDailyAvailable").textContent = formatKRW(sim.dailyAvailableAfterPurchase);

    box.classList.remove("hidden");
}

function getSimulatorPayload() {
    return {
        yearMonth: yearMonthInput.value,
        itemName: document.getElementById("simItemName").value.trim(),
        plannedAmount: Number(document.getElementById("simPlannedAmount").value),
    };
}

document.getElementById("simOnlyBtn").addEventListener("click", async () => {
    const payload = getSimulatorPayload();
    if (!payload.itemName || !payload.plannedAmount) {
        return showAlert("구매 예정 항목과 금액을 입력해 주세요.", "error");
    }
    try {
        const sim = await apiFetch("/simulator", {
            method: "POST",
            body: JSON.stringify(payload),
        });
        renderSimulation(sim);
    } catch (err) {
        showAlert(err.message, "error");
    }
});

document.getElementById("simulatorForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const btn = document.getElementById("aiAdviceBtn");
    btn.disabled = true;
    btn.textContent = "AI 분석 중...";

    try {
        const result = await apiFetch("/ai/advice", {
            method: "POST",
            body: JSON.stringify(getSimulatorPayload()),
        });
        renderSimulation(result.simulation, result.aiComment, result.aiGenerated);
    } catch (err) {
        showAlert(err.message, "error");
    } finally {
        btn.disabled = false;
        btn.textContent = "AI 조언 받기";
    }
});

// 3. AI 결제 문자 자동 파싱
document.getElementById("parseSmsBtn").addEventListener("click", async () => {
    const rawText = document.getElementById("rawSmsInput").value.trim();
    if (!rawText) return showAlert("결제 문자를 입력해 주세요.", "error");

    const btn = document.getElementById("parseSmsBtn");
    btn.disabled = true;
    btn.textContent = "분석 중...";

    try {
        const parsed = await apiFetch("/ai/parse-transaction", {
            method: "POST",
            body: JSON.stringify({ rawText }),
        });

        document.getElementById("txTitle").value = parsed.title ?? "";
        document.getElementById("txAmount").value = parsed.amount ?? "";
        if (parsed.category) document.getElementById("txCategory").value = parsed.category;
        if (parsed.transactionDate) document.getElementById("txDate").value = parsed.transactionDate;
        document.getElementById("txMemo").value = parsed.memo ?? "";

        showAlert("AI가 결제 문자를 채웠습니다. 확인 후 등록해 주세요!");
    } catch (err) {
        showAlert(err.message, "error");
    } finally {
        btn.disabled = false;
        btn.textContent = "AI로 자동 채우기";
    }
});

// 4. 변동 지출 조회 / 등록 / 삭제
async function loadTransactions() {
    const yearMonth = yearMonthInput.value;
    const listEl = document.getElementById("transactionList");
    const totalEl = document.getElementById("txTotalDisplay");

    try {
        const items = await apiFetch(`/transactions?yearMonth=${yearMonth}`);
        listEl.innerHTML = "";
        let total = 0;

        if (!items.length) {
            listEl.innerHTML = `<li class="item-row">등록된 변동 지출이 없습니다.</li>`;
        } else {
            items.forEach((item) => {
                total += Number(item.amount);
                const li = document.createElement("li");
                li.className = "item-row";
                li.innerHTML = `
                    <div>
                        <strong>${item.title}</strong>
                        <div class="item-meta">${item.transactionDate} · ${item.category}${item.memo ? ` · ${item.memo}` : ""}</div>
                    </div>
                    <div class="item-right">
                        <strong>${formatKRW(item.amount)}</strong>
                        <button class="btn btn-danger" onclick="deleteTransaction(${item.id})">삭제</button>
                    </div>
                `;
                listEl.appendChild(li);
            });
        }
        state.txTotal = total;
        totalEl.textContent = formatKRW(total);
        updateOverview();
    } catch (err) {
        showAlert(err.message, "error");
    }
}

document.getElementById("transactionForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const payload = {
        title: document.getElementById("txTitle").value.trim(),
        amount: Number(document.getElementById("txAmount").value),
        category: document.getElementById("txCategory").value,
        transactionDate: document.getElementById("txDate").value,
        memo: document.getElementById("txMemo").value.trim(),
    };

    try {
        await apiFetch("/transactions", {
            method: "POST",
            body: JSON.stringify(payload),
        });
        e.target.reset();
        document.getElementById("rawSmsInput").value = "";
        document.getElementById("txDate").value = new Date().toISOString().slice(0, 10);
        showAlert("변동 지출이 등록되었습니다.");
        await loadTransactions();
    } catch (err) {
        showAlert(err.message, "error");
    }
});

window.deleteTransaction = async (id) => {
    try {
        await apiFetch(`/transactions/${id}`, { method: "DELETE" });
        showAlert("변동 지출이 삭제되었습니다.");
        await loadTransactions();
    } catch (err) {
        showAlert(err.message, "error");
    }
};

// 5. 고정 지출 조회 / 등록 / 삭제
async function loadFixedExpenses() {
    const listEl = document.getElementById("fixedExpenseList");
    const totalEl = document.getElementById("fixedTotalDisplay");

    try {
        const items = await apiFetch("/fixed-expenses");
        listEl.innerHTML = "";
        let total = 0;

        if (!items.length) {
            listEl.innerHTML = `<li class="item-row">등록된 고정 지출이 없습니다.</li>`;
        } else {
            items.forEach((item) => {
                total += Number(item.amount);
                const li = document.createElement("li");
                li.className = "item-row";
                li.innerHTML = `
                    <div>
                        <strong>${item.title ?? item.name}</strong>
                        <div class="item-meta">매월 ${item.billingDay}일 결제</div>
                    </div>
                    <div class="item-right">
                        <strong>${formatKRW(item.amount)}</strong>
                        <button class="btn btn-danger" onclick="deleteFixedExpense(${item.id})">삭제</button>
                    </div>
                `;
                listEl.appendChild(li);
            });
        }
        state.fixedTotal = total;
        totalEl.textContent = formatKRW(total);
        updateOverview();
    } catch (err) {
        showAlert(err.message, "error");
    }
}

document.getElementById("fixedExpenseForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const payload = {
        name: document.getElementById("fixedTitle").value.trim(),
        amount: Number(document.getElementById("fixedAmount").value),
        billingDay: Number(document.getElementById("fixedBillingDay").value),
    };

    try {
        await apiFetch("/fixed-expenses", {
            method: "POST",
            body: JSON.stringify(payload),
        });
        e.target.reset();
        showAlert("고정 지출이 추가되었습니다.");
        await loadFixedExpenses();
    } catch (err) {
        showAlert(err.message, "error");
    }
});

window.deleteFixedExpense = async (id) => {
    try {
        await apiFetch(`/fixed-expenses/${id}`, { method: "DELETE" });
        showAlert("고정 지출이 삭제되었습니다.");
        await loadFixedExpenses();
    } catch (err) {
        showAlert(err.message, "error");
    }
};

// 전체 새로고침 & 초기화
async function loadAll() {
    await Promise.all([loadBudget(), loadTransactions(), loadFixedExpenses()]);
}

document.getElementById("refreshBtn").addEventListener("click", loadAll);
yearMonthInput.addEventListener("change", loadAll);

const today = new Date();
yearMonthInput.value = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, "0")}`;
document.getElementById("txDate").value = today.toISOString().slice(0, 10);
loadAll();