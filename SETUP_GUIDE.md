# AstralW 新设备配置指南

## 1. 环境要求

| 工具 | 版本 |
|------|------|
| Android Studio | 最新版 |
| JDK | 21 |
| Android SDK | API 35 (最低 26) |
| Python | 3.11+ |
| Git | 最新版 |

---

## 2. 克隆项目

```bash
# 前端 (Android)
git clone https://github.com/Melody-learning/cfd_client.git

# 后端 (Python)
# 后端项目不在 git，需要从旧设备拷贝 astralw_back 目录
```

---

## 3. 前端配置 (cfd_client)

### 3.1 `local.properties` (自动生成或手动创建)

文件位置：`项目根目录/local.properties`

```properties
sdk.dir=C\:\\Users\\你的用户名\\AppData\\Local\\Android\\Sdk
```

> Android Studio 打开项目后通常会自动生成

### 3.2 ⚠️ BASE_URL (必须手动修改)

文件位置：`core/core-data/src/main/java/com/astralw/core/data/di/DataModule.kt`

```kotlin
// 第 51 行，改为新设备电脑局域网 IP
private const val BASE_URL = "http://你的电脑IP:8000/"
```

**获取电脑 IP**：
```bash
# Windows
ipconfig   # 找 WiFi 适配器的 IPv4 地址

# Mac
ifconfig | grep "inet " | grep -v 127.0.0.1
```

> [!IMPORTANT]
> 手机和电脑必须在同一局域网（同一 WiFi）

---

## 4. 后端配置 (astralw_back)

### 4.1 安装依赖

```bash
cd astralw_back
pip install -r requirements.txt
```

### 4.2 `.env` 文件 (可选)

后端**没有 `.env` 文件**，敏感配置目前硬编码在 `app/config.py`。

如需覆盖配置，在 `astralw_back/` 根目录创建 `.env`：

```env
# MT5 服务器 (默认值已配置，通常不用改)
MT5_SERVER_HOST=43.128.39.163
MT5_SERVER_PORT=443
MT5_MANAGER_LOGIN=1015
MT5_MANAGER_PASSWORD=你的密码
MT5_WEBAPI_PASSWORD=你的WebAPI密码

# JWT (开发阶段可用默认值)
JWT_SECRET_KEY=dev-secret-key-change-in-production

# 数据库 (默认 SQLite，不用改)
DATABASE_URL=sqlite+aiosqlite:///./astralw_gateway.db
```

> [!NOTE]
> `.env` 会自动覆盖 `config.py` 中的默认值。MT5 密码如果已经硬编码在 `config.py` 中则不需要 `.env`

### 4.3 启动后端

```bash
cd astralw_back
python -m uvicorn app.main:app --host 0.0.0.0 --port 8000
```

---

## 5. 配置清单 (Checklist)

- [ ] 安装 Android Studio + JDK 21
- [ ] 安装 Python 3.11+
- [ ] `git clone` 前端项目
- [ ] 拷贝后端项目 `astralw_back/`
- [ ] `pip install -r requirements.txt`
- [ ] 修改 `DataModule.kt` 中的 `BASE_URL` 为电脑 IP
- [ ] 启动后端 `python -m uvicorn ...`
- [ ] Android Studio 打开前端项目，等 Gradle Sync 完成
- [ ] 连接手机（同一 WiFi），Run
