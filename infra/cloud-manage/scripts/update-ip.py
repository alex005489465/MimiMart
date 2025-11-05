#!/usr/bin/env python3
"""
IP 自動更新腳本

用途: 自動檢測開發者 IP 變更並更新 Terraform 配置
執行: python scripts/update-ip.py [--force] [--check-only]

流程:
1. 檢測當前 IPv4 和 IPv6 地址
2. 比對 developer-ips.json 中的舊 IP
3. 如有變更,更新 JSON 並部署 Terraform
4. 依序更新: S3 → WAF → Security Groups
"""

import json
import subprocess
import sys
import argparse
import io
from pathlib import Path
from datetime import date
from typing import Dict, Tuple, Optional

# 修正 Windows 終端機編碼問題
if sys.platform == 'win32':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
    sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8')

try:
    import requests
except ImportError:
    print("❌ 缺少依賴: requests")
    print("請執行: pip install requests")
    sys.exit(1)


class IPUpdater:
    """IP 自動更新管理器"""

    def __init__(self, project_root: Path, force: bool = False, check_only: bool = False):
        self.project_root = project_root
        self.force = force
        self.check_only = check_only
        self.json_file = project_root / "modules" / "ip-ranges" / "developer-ips.json"

        # 需要部署的 Terraform 模組 (依序執行)
        self.terraform_modules = [
            {
                "name": "AWS S3 開發環境",
                "env_file": "aws/.env",
                "command": "cd s3 && terraform apply -auto-approve"
            },
            {
                "name": "Cloudflare WAF",
                "env_file": "cloudflare/.env",
                "command": "cd waf && terraform apply -auto-approve"
            },
            {
                "name": "AWS Security Groups",
                "env_file": "aws/.env",
                "command": "cd security-groups && terraform apply -auto-approve"
            }
        ]

    def log(self, message: str, level: str = "INFO"):
        """輸出日誌訊息"""
        symbols = {
            "INFO": "ℹ",
            "SUCCESS": "✓",
            "ERROR": "❌",
            "WARNING": "⚠",
            "PROGRESS": "⟳"
        }
        symbol = symbols.get(level, "•")
        print(f"{symbol} {message}")

    def get_current_ip(self, ip_version: str = "v4") -> Optional[str]:
        """
        獲取當前公網 IP

        Args:
            ip_version: "v4" 或 "v6"

        Returns:
            IP 地址字串,失敗返回 None
        """
        urls = {
            "v4": "https://api.ipify.org",
            "v6": "https://api64.ipify.org"
        }

        url = urls.get(ip_version)
        if not url:
            self.log(f"不支援的 IP 版本: {ip_version}", "ERROR")
            return None

        # 重試機制
        for attempt in range(3):
            try:
                response = requests.get(url, timeout=5)
                response.raise_for_status()
                ip = response.text.strip()

                # 驗證 IP 格式
                if ip and self._validate_ip_format(ip, ip_version):
                    return ip
                else:
                    self.log(f"無效的 IP 格式: {ip}", "WARNING")

            except requests.RequestException as e:
                if attempt < 2:  # 不是最後一次嘗試
                    self.log(f"第 {attempt + 1} 次獲取 IP{ip_version} 失敗,重試中...", "WARNING")
                else:
                    self.log(f"獲取 IP{ip_version} 失敗: {e}", "ERROR")

        return None

    def _validate_ip_format(self, ip: str, ip_version: str) -> bool:
        """驗證 IP 格式"""
        if ip_version == "v4":
            parts = ip.split('.')
            if len(parts) != 4:
                return False
            try:
                return all(0 <= int(part) <= 255 for part in parts)
            except ValueError:
                return False
        elif ip_version == "v6":
            # 簡單的 IPv6 驗證 (包含冒號和十六進位字元)
            return ':' in ip and all(c in '0123456789abcdefABCDEF:' for c in ip)
        return False

    def read_json_config(self) -> Dict:
        """讀取 developer-ips.json 配置"""
        try:
            if not self.json_file.exists():
                self.log(f"配置文件不存在: {self.json_file}", "ERROR")
                sys.exit(1)

            with open(self.json_file, 'r', encoding='utf-8') as f:
                data = json.load(f)

            return data
        except json.JSONDecodeError as e:
            self.log(f"JSON 格式錯誤: {e}", "ERROR")
            sys.exit(1)
        except Exception as e:
            self.log(f"讀取配置失敗: {e}", "ERROR")
            sys.exit(1)

    def update_json_config(self, ipv4: str, ipv6: str) -> bool:
        """
        更新 JSON 配置文件

        Args:
            ipv4: 新的 IPv4 地址
            ipv6: 新的 IPv6 地址

        Returns:
            更新成功返回 True
        """
        try:
            data = self.read_json_config()

            # 更新 IP (自動加上 CIDR 後綴)
            data['developer_ips_v4'] = [f"{ipv4}/32"]
            data['developer_ips_v6'] = [f"{ipv6}/128"]

            # 更新日期
            data['last_updated'] = str(date.today())

            # 寫回文件 (保持格式化)
            with open(self.json_file, 'w', encoding='utf-8') as f:
                json.dump(data, f, indent=2, ensure_ascii=False)
                f.write('\n')  # 結尾換行

            self.log(f"已更新配置: {self.json_file.name}", "SUCCESS")
            return True

        except Exception as e:
            self.log(f"更新配置失敗: {e}", "ERROR")
            return False

    def check_ip_changes(self) -> Tuple[bool, Optional[str], Optional[str], Optional[str], Optional[str]]:
        """
        檢查 IP 是否變更

        Returns:
            (是否有變更, 舊IPv4, 新IPv4, 舊IPv6, 新IPv6)
        """
        # 獲取當前 IP
        current_ipv4 = self.get_current_ip("v4")
        current_ipv6 = self.get_current_ip("v6")

        if not current_ipv4 or not current_ipv6:
            self.log("無法獲取當前 IP,請檢查網路連線", "ERROR")
            sys.exit(1)

        # 讀取舊配置
        config = self.read_json_config()
        old_ipv4_list = config.get('developer_ips_v4', [])
        old_ipv6_list = config.get('developer_ips_v6', [])

        # 提取 IP (移除 CIDR 後綴)
        old_ipv4 = old_ipv4_list[0].split('/')[0] if old_ipv4_list else None
        old_ipv6 = old_ipv6_list[0].split('/')[0] if old_ipv6_list else None

        # 比對變更
        ipv4_changed = old_ipv4 != current_ipv4
        ipv6_changed = old_ipv6 != current_ipv6
        has_changes = ipv4_changed or ipv6_changed

        return has_changes, old_ipv4, current_ipv4, old_ipv6, current_ipv6

    def deploy_terraform(self) -> bool:
        """
        部署 Terraform 配置

        Returns:
            所有模組部署成功返回 True
        """
        self.log("開始部署 Terraform 模組...", "PROGRESS")

        for i, module in enumerate(self.terraform_modules, 1):
            name = module['name']
            env_file = module['env_file']
            command = module['command']

            self.log(f"[{i}/{len(self.terraform_modules)}] 部署 {name}...", "PROGRESS")

            try:
                # 構建 docker-compose 命令
                docker_cmd = [
                    'docker-compose',
                    '--env-file', env_file,
                    'run', '--rm',
                    'terraform',
                    command
                ]

                # 執行命令 (明確指定 UTF-8 編碼處理輸出)
                result = subprocess.run(
                    docker_cmd,
                    cwd=str(self.project_root),
                    capture_output=True,
                    text=True,
                    encoding='utf-8',
                    errors='replace',  # 遇到無法解碼的字元時用 ? 替換
                    check=True
                )

                self.log(f"{name} 部署成功", "SUCCESS")

            except subprocess.CalledProcessError as e:
                self.log(f"{name} 部署失敗", "ERROR")
                self.log(f"錯誤訊息: {e.stderr}", "ERROR")
                return False
            except Exception as e:
                self.log(f"{name} 部署失敗: {e}", "ERROR")
                return False

        return True

    def run(self):
        """執行完整的 IP 更新流程"""
        print("=" * 50)
        print("IP 自動更新腳本")
        print("=" * 50)

        # 1. 檢查 IP 變更
        self.log("檢測當前 IP 地址...", "PROGRESS")
        has_changes, old_ipv4, new_ipv4, old_ipv6, new_ipv6 = self.check_ip_changes()

        # 顯示 IP 資訊
        print()
        print(f"  IPv4: {old_ipv4} → {new_ipv4} {'(變更)' if old_ipv4 != new_ipv4 else '(未變更)'}")
        print(f"  IPv6: {old_ipv6} → {new_ipv6} {'(變更)' if old_ipv6 != new_ipv6 else '(未變更)'}")
        print()

        # 如果只是檢查模式,直接結束
        if self.check_only:
            self.log("僅檢查模式,不執行更新", "INFO")
            return

        # 判斷是否需要更新
        if not has_changes and not self.force:
            self.log("IP 未變更,無需更新", "INFO")
            self.log("提示: 使用 --force 參數可強制重新部署", "INFO")
            return

        if self.force and not has_changes:
            self.log("強制模式: 即使 IP 未變更也執行部署", "WARNING")

        # 2. 更新 JSON 配置
        self.log("更新 developer-ips.json...", "PROGRESS")
        if not self.update_json_config(new_ipv4, new_ipv6):
            sys.exit(1)

        # 3. 部署 Terraform
        if not self.deploy_terraform():
            self.log("部署失敗,請檢查錯誤訊息", "ERROR")
            sys.exit(1)

        # 4. 完成
        print()
        print("=" * 50)
        self.log("所有更新完成!", "SUCCESS")
        print("=" * 50)


def main():
    """主程式入口"""
    # 解析命令行參數
    parser = argparse.ArgumentParser(
        description='IP 自動更新腳本',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
使用範例:
  python scripts/update-ip.py              # 正常執行
  python scripts/update-ip.py --force      # 強制重新部署
  python scripts/update-ip.py --check-only # 僅檢查 IP
        """
    )
    parser.add_argument(
        '--force',
        action='store_true',
        help='強制重新部署 (即使 IP 未變更)'
    )
    parser.add_argument(
        '--check-only',
        action='store_true',
        help='僅檢查 IP 變更,不執行更新'
    )

    args = parser.parse_args()

    # 確定專案根目錄
    script_dir = Path(__file__).parent
    project_root = script_dir.parent  # infra/cloud-manage

    # 執行更新
    updater = IPUpdater(project_root, force=args.force, check_only=args.check_only)
    updater.run()


if __name__ == '__main__':
    try:
        main()
    except KeyboardInterrupt:
        print("\n\n⚠ 使用者中斷執行")
        sys.exit(1)
    except Exception as e:
        print(f"\n❌ 未預期的錯誤: {e}")
        sys.exit(1)
