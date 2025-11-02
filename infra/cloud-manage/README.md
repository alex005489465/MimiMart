# MimiMart 雲端基礎設施管理

使用容器化 Terraform 管理 MimiMart 專案的多雲平台基礎設施。

## 架構概覽

```
infra/cloud-manage/
├── docker-compose.yml         # 共用容器配置
├── cloudflare/               # Cloudflare 平台模組
│   ├── dns/                  # DNS 記錄管理
│   └── waf/                  # Web 應用防火牆規則
└── aws/                      # AWS 平台模組
    └── s3/                   # S3 物件儲存
```

## 設計理念

- **雲平台分離**: 每個平台獨立資料夾,各自管理配置和機密
- **自動平台切換**: 透過 `.env` 中的 `PLATFORM` 變數自動識別平台
- **容器化執行**: 零本機安裝,所有操作在容器內執行

## 模組文檔

詳細的模組配置和使用說明,請參閱各模組的 README:

### Cloudflare 平台
- [DNS 模組](./cloudflare/dns/README.md) - DNS 記錄管理
- [WAF 模組](./cloudflare/waf/README.md) - Web 應用防火牆規則
- [Cloudflare 總覽](./cloudflare/README.md) - Cloudflare 平台說明

### AWS 平台
- [S3 模組](./aws/s3/README.md) - S3 物件儲存管理
- [AWS 總覽](./aws/README.md) - AWS 平台說明

## 安全注意事項

- **絕不提交 `.env` 至版本控制** - 所有 `.env` 檔案已加入 `.gitignore`
- **定期輪換 API 金鑰** - Cloudflare API Token 和 AWS Access Key
- **使用最小權限原則** - API Token 和 IAM 政策只授予必要權限
