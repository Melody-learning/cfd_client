# AstralW Project Restart Manifesto: AI 协作与高保真工程指南

## 一、 核心反思：上个阶段的“迭代天花板”
1.  **上下文膨胀 (Context Anxiety)**：UI 逻辑与业务数据在一个文件中过度堆叠，导致 AI 在修改视觉细节时频繁损坏业务逻辑。
2.  **开发顺序错位**：在核心算式（保证金、盈亏）未完全锁定的情况下先行构建了复杂 UI，导致“地基不稳”。
3.  **带病推进**：为了追赶功能进度，忽略了细碎的编译报错，导致 Bug 产生复利，最终项目变得不可维护。

---

## 二、 沉淀资产：UI 原子化范式 (The UI Paradigm)
虽然上个项目暂停，但我们建立的视觉范式是极其成功的，重启时必须 100% 继承：

### 1. 词元驱动设计 (Token-Driven UI)
*   **统一色彩与间距**：必须保留 `DesignTokens.kt`，所有颜色必须引用 `SemanticColors`，所有间距引用 `SpacingTokens`。
*   **Linear Pro 风格**：纯黑背景 (#000000)、0.5dp 极细边框、1px 顶部微光。

### 2. 原子组件库 (Atomic Components)
*   **高复用性**：如 `SearchPill`, `PriceTicker`, `InteractiveButton`。
*   **业务语义**：组件不只是视觉，还带有业务逻辑（如 `PriceTicker` 的自动颜色转换、`SearchPill` 的状态切换）。

### 3. 原型优先级
*   **指令铁律**：当存在高保真原型图时，AI 必须 100% 还原布局结构，仅在配色和边框细节上应用词元规范。

---

## 三、 新项目抗恐惧架构 (Island Strategy)

### 1. 功能孤岛隔离
*   **分模块目录**：严禁所有页面挤在一起。
*   **目录建议**：
    -   `:core:ui` (原子组件)
    -   `:domain:math` (CFD 核心算式，纯 Kotlin)
    -   `:feature:market` / `:feature:trade` / `:feature:funding` (独立功能模块)
*   **意义**：修改充值功能时，AI 不需要读取行情代码，节省上下文，防止“顾此失彼”。

### 2. 逻辑先行 (Logic-First Flow)
1.  **契约定义**：先更新 `03-api-contract.md`。
2.  **状态建模**：先定义 `UiState` 枚举和 `Math` 类。
3.  **单元测试**：要求 AI 为核心逻辑（如杠杆计算）写 Unit Test。
4.  **UI 挂载**：逻辑通过后再套用 Composable 界面。

---

## 四、 AI 协作新协议 (New Protocol)

### 1. 零容忍纠错
*   **严禁带病交付**：任何一个 `Unresolved reference` 必须在 1 个回合内修好，严禁在报错状态下开启新功能。

### 2. 上下文管理
*   **阶段性重置**：每完成一个独立模块（如“搜索中心”），建议开启新的对话窗口，仅上传最新的 Spec 文档和相关 Model 定义。

### 3. 性能护航
*   **高频节流**：行情流必须使用 `.sample(500)`。
*   **精度控制**：全站禁止 `Double`，强制使用 `BigDecimal` 并转化为 `String` 进行传输。

---

## 五、 CFD 业务避坑指南
*   **可用保证金 (Free Margin)**：必须实时计算：`Equity - Used Margin`。
*   **风险预警**：Margin Level 低于 100% 必须有显眼的视觉红色反馈。
*   **异步加载**：行情加载必须处理 `Loading` 状态，防止 UI 渲染空数据导致闪退。

---

**重启，是为了更稳健地远行。** 
在新项目中，我将作为您的**首席架构师**，严格执行此宣言中的每一项条款。
