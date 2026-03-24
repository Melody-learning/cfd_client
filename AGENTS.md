# AstralW - AI Agent 操作手册

## 项目信息
- **类型**: 安卓原生应用 (CFD 差价合约交易)
- **语言**: Kotlin
- **UI**: Jetpack Compose (禁止 XML Layout)
- **构建**: Gradle KTS, Version Catalog (libs.versions.toml)
- **最低 SDK**: 26 (Android 8.0)
- **目标 SDK**: 35
- **JDK**: 21
- **包名**: com.astralw.*

## 架构规则
- 严格遵循 Clean Architecture: UI Layer → Domain Layer → Data Layer
- 所有 UI 使用 Jetpack Compose，**禁止 XML Layout**
- ViewModel 只暴露 `StateFlow`，**禁止 LiveData**
- Repository 模式管理所有数据访问
- 依赖注入使用 Hilt，**禁止手动构造**
- 单向数据流 (UDF): UI 观察 State → 发送 Event → ViewModel 处理 → 更新 State

## 模块结构
- `:app` — 应用入口，仅做模块聚合和导航
- `:core:core-common` — 通用工具、扩展函数
- `:core:core-ui` — 共享 UI 组件、DesignTokens、主题
- `:core:core-network` — Retrofit + OkHttp + WebSocket
- `:core:core-data` — Repository 实现、数据模型
- `:core:core-database` — Room 数据库
- `:domain:domain-math` — CFD 核心算式 (纯 Kotlin JVM，零 Android 依赖)
- `:feature:feature-*` — 各功能孤岛模块

## 编码规范
- Kotlin 官方编码风格
- `camelCase` 函数/变量, `PascalCase` 类/接口
- **禁止使用 `!!` (not-null assertion)**
- 所有公共 API 必须有 KDoc 注释
- 字符串必须资源化，**禁止硬编码字符串**
- 使用 `kotlinx.serialization` 进行 JSON 序列化

## ⚠️ 铁律 (违反即打回)
1. **零容忍纠错**: 任何 `Unresolved reference` 必须 1 回合内修好，严禁在报错状态下开新功能
2. **Logic-First**: 先契约定义 → 状态建模 (UiState) → 单元测试 → 最后 UI
3. **全站禁止 Double**: 金额/价格/保证金/盈亏强制 `BigDecimal`，传输用 `String`
4. **行情节流**: 行情 Flow 必须使用 `.sample(500)` 防止高频刷新
5. **异步 Loading 态**: 所有网络数据加载必须处理 `Loading / Success / Error` 状态，禁止渲染空数据
6. **原型铁律**: 有高保真原型时，必须 100% 还原布局结构

## UI 设计系统
- 所有颜色引用 `DesignTokens.SemanticColors`
- 所有间距引用 `DesignTokens.SpacingTokens`
- Linear Pro 风格: 纯黑背景 `#000000`, `0.5dp` 极细边框, `1px` 顶部微光
- 涨跌颜色: 绿涨 `PriceUp` / 红跌 `PriceDown`

## 命令
```bash
# 构建
./gradlew assembleDebug

# 单元测试 (domain-math 优先)
./gradlew :domain:domain-math:test
./gradlew test

# Lint
./gradlew ktlintCheck detekt
```

## 开发流程 (SDD)
每个功能模块必须遵循:
1. **Specify**: 写 SPEC.md (功能规格、用户故事、验收标准)
2. **Plan**: 写 PLAN.md (技术方案、数据流)
3. **Tasks**: 写 TASKS.md (原子任务清单，每个 ≤ 2h)
4. **Implement**: 编码 + 测试
5. **Verify**: 单元测试 + UI 验证

## 🔗 跨项目协作 (前端 ↔ 后端)

**后端项目路径**: `e:\ai-coding-study\astralw_back`

### 共享文档位置
- **后端接口文档**: `e:\ai-coding-study\astralw_back\share\api_spec.md` (后端维护)
- **接口变更记录**: `e:\ai-coding-study\astralw_back\share\api_changelog.md` (后端维护)

### 前端侧协作规则
1. **对接接口前**：先读 `astralw_back/share/api_spec.md` 了解当前接口规格
2. **发现接口问题**：读 `astralw_back/share/api_changelog.md` 确认是否已有变更
3. **需要新接口时**：在 `astralw_back/share/api_spec.md` 末尾追加 `## 待实现` 区块，写明期望的接口路径、参数、响应格式
4. **做完 UI 后**：总结所需接口并更新到 `api_spec.md` 的待实现区块，方便后端对话读取
