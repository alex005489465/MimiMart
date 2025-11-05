# IP 自動更新腳本

## 概述

當開發者的公網 IP 變更時，此腳本會自動：
1. 檢測當前 IPv4 和 IPv6 地址
2. 更新 IP 白名單配置文件
3. 依序部署相依的基礎設施模組

## 快速開始

### 1. 安裝依賴（首次執行）

```bash
# 安裝 Python 依賴
pip install -r scripts/requirements.txt
```

### 2. 執行腳本

```bash
# 正常執行（自動檢測 IP 變更）
python scripts/update-ip.py

# 強制重新部署（即使 IP 未變更）
python scripts/update-ip.py --force

# 僅檢查 IP 變更，不執行更新
python scripts/update-ip.py --check-only
```

## 命令參數

| 參數 | 說明 |
|------|------|
| 無參數 | 正常執行，IP 變更時自動更新並部署 |
| `--force` | 強制重新部署，即使 IP 未變更 |
| `--check-only` | 僅檢查 IP 變更，不執行更新 |
| `--help` | 顯示幫助訊息 |

## 腳本行為

### 執行流程

```
1. 檢測當前 IP
   ├─ 調用公開 IP 檢測服務 (IPv4)
   └─ 調用公開 IP 檢測服務 (IPv6)

2. 比對舊 IP
   └─ 讀取配置文件

3. 判斷是否需要更新
   ├─ 無變更 → 結束
   ├─ 有變更 → 繼續
   └─ --force → 繼續

4. 更新配置
   ├─ 更新 IPv4 (自動加 /32 CIDR)
   ├─ 更新 IPv6 (自動加 /128 CIDR)
   └─ 更新時間戳記

5. 部署基礎設施
   └─ 依序部署相依模組
```

### 錯誤處理

- **網路錯誤**：自動重試 3 次
- **配置格式錯誤**：顯示錯誤訊息並停止
- **部署失敗**：停止後續模組部署

## 故障排除

### 問題 1：找不到 requests 模組

```
❌ 缺少依賴: requests
請執行: pip install requests
```

**解決方式**：
```bash
pip install -r scripts/requirements.txt
```

### 問題 2：無法獲取當前 IP

```
❌ 獲取 IPvX 失敗: ...
```

**可能原因**：
- 網路連線問題
- 防火牆阻擋
- IP 檢測服務無法訪問

**解決方式**：
- 檢查網路連線
- 確認可以訪問外部 IP 檢測服務
- 手動檢查網路狀態

### 問題 3：部署失敗

```
❌ XXX 部署失敗
錯誤訊息: ...
```

**可能原因**：
- Docker 服務未啟動
- 環境變數配置錯誤
- 雲端服務認證失敗
- 權限不足

**解決方式**：
1. 確認 Docker 服務正在運行
2. 檢查環境變數配置
3. 驗證雲端服務認證資訊
4. 手動執行部署命令檢查詳細錯誤

### 問題 4：配置格式錯誤

```
❌ JSON 格式錯誤: ...
```

**解決方式**：
1. 使用 JSON 驗證工具檢查配置文件
2. 參考配置範例文件修正格式
3. 或從備份恢復文件

## 技術細節

### 依賴套件

- **requests** (>=2.31.0)：用於 HTTP 請求

### 相容性

- **Python**：3.7+（建議 3.13）
- **作業系統**：Windows / Linux / macOS
- **Shell**：Git Bash / PowerShell / CMD / Bash

### 檔案結構

```
scripts/
├── update-ip.py          # 主腳本
├── requirements.txt      # Python 依賴
├── README.md             # 本文檔 (公開)
└── CLAUDE.md             # Claude Code 協作指引 (不提交 Git)
```

## 授權

內部專案使用。
