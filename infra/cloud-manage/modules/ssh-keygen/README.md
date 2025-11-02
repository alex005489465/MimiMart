# SSH Key Generator

## 目標

自動化生成 SSH 金鑰對，用於伺服器存取管理（如 AWS EC2、VPS 等）。

## 功能

- 支援 Ed25519 和 RSA 4096 金鑰類型
- 優先使用系統 ssh-keygen，降級使用 Node.js crypto
- 配置化管理，可批次生成多個金鑰
- 自動生成 Terraform 格式公鑰
- 跨平台支援（Windows、Linux、macOS）

## 前置條件

- Node.js >= 14.0.0
- （建議）OpenSSH ssh-keygen

## 執行步驟

### 1. 複製配置範例

```bash
cp config.json.example config.json
```

### 2. 編輯配置檔案

編輯 `config.json`：

```json
{
  "default_key_type": "ed25519",
  "default_output_dir": "./output",
  "keys": [
    {
      "name": "your-server-key",
      "comment": "Your Server SSH Access"
    }
  ]
}
```

配置說明：
- `default_key_type`: 預設金鑰類型（`ed25519` 或 `rsa`）
- `name`: 金鑰名稱（不含副檔名）
- `comment`: 公鑰註解
- `type`: （可選）覆寫預設金鑰類型
- `bits`: （可選）RSA 位元數，預設 4096

### 3. 生成金鑰

```bash
node generate-key.js
```

或使用 npm script：

```bash
npm run generate
```

### 4. 查看輸出

生成的檔案位於 `output/` 目錄：

```
output/
├── your-server-key.pem           # 私鑰
├── your-server-key.pub           # 公鑰
└── your-server-key.terraform.txt # Terraform 格式公鑰
```

## 驗證

執行以下命令查看已生成的金鑰：

```bash
npm run list
```

清空 output 目錄（會刪除所有金鑰）：

```bash
npm run clean
```

## 完成

金鑰已生成，私鑰權限已設定為 400（僅擁有者可讀）。
