#!/usr/bin/env bash
#
# 本地/CI 安全扫描一键脚本（非侵入：不改 pom.xml / Jenkins 业务构建）
# 覆盖：依赖漏洞(SCA, OWASP Dependency-Check) + 密钥泄漏(gitleaks)
# 依赖：Docker（无需本地安装扫描工具）
#
# 用法：
#   bash scripts/security-scan.sh              # 全部扫描
#   bash scripts/security-scan.sh sca          # 仅依赖漏洞
#   bash scripts/security-scan.sh secrets      # 仅密钥泄漏
#
# 产出目录：docs/security/scan-reports/<yyyyMMdd-HHmmss>/
#   - dependency-check/  (HTML + JSON + SARIF)
#   - gitleaks-report.json
#   - SUMMARY.md         (给交付整改对照用的摘要)
#
# 注意：大报告默认被 .gitignore 排除，不入库；仅 TEMPLATE/ 与本脚本入库。

set -euo pipefail

# ---- 路径 ----
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
TS="$(date +%Y%m%d-%H%M%S)"
OUT_DIR="${REPO_ROOT}/docs/security/scan-reports/${TS}"
MODE="${1:-all}"

TRIVY_IMAGE="aquasec/trivy:latest"
GITLEAKS_IMAGE="zricethezav/gitleaks:latest"
SEMGREP_IMAGE="semgrep/semgrep:latest"

# ---- 前置检查 ----
if ! command -v docker >/dev/null 2>&1; then
  echo "[ERROR] 需要 Docker。请先安装并启动 Docker Desktop / dockerd。" >&2
  exit 1
fi

mkdir -p "${OUT_DIR}"
echo "[INFO] 扫描输出目录: ${OUT_DIR}"

GIT_COMMIT="$(git -C "${REPO_ROOT}" rev-parse --short HEAD 2>/dev/null || echo 'unknown')"
GIT_BRANCH="$(git -C "${REPO_ROOT}" rev-parse --abbrev-ref HEAD 2>/dev/null || echo 'unknown')"

run_sca() {
  echo "[INFO] === 依赖漏洞扫描 (Trivy fs) ==="
  local sca_out="${OUT_DIR}/dependency-check"
  mkdir -p "${sca_out}"
  # Trivy 自带漏洞库、无需 NVD API Key；缓存漏洞库避免每次重复下载
  local trivy_cache="${HOME}/.cache/trivy"
  mkdir -p "${trivy_cache}"
  # 挂本地 Maven 仓库 + --offline-scan：用已构建的 ~/.m2 离线解析父 POM，避免 Maven Central 429 限流
  local m2_dir="${HOME}/.m2"
  local m2_mount=()
  if [ -d "${m2_dir}" ]; then
    m2_mount=(-v "${m2_dir}:/root/.m2:ro")
  else
    echo "[WARN] 未发现 ${m2_dir}，Trivy 可能因联网解析依赖被 Maven Central 限流；建议先 mvn compile 预热本地仓库"
  fi
  # 输出 JSON（机器可读）+ table（人读）；扫描器仅 vuln
  docker run --rm \
    -v "${REPO_ROOT}:/src:ro" \
    -v "${sca_out}:/out" \
    -v "${trivy_cache}:/root/.cache/trivy" \
    "${m2_mount[@]}" \
    "${TRIVY_IMAGE}" \
    fs --scanners vuln --offline-scan \
    --skip-dirs "**/target" --skip-dirs "**/node_modules" \
    --format json --output /out/trivy-report.json \
    /src \
    || echo "[WARN] Trivy 以非零码退出（详见报告）"
  # 再出一份人读 table
  docker run --rm \
    -v "${REPO_ROOT}:/src:ro" \
    -v "${sca_out}:/out" \
    -v "${trivy_cache}:/root/.cache/trivy" \
    "${m2_mount[@]}" \
    "${TRIVY_IMAGE}" \
    fs --scanners vuln --offline-scan \
    --skip-dirs "**/target" --skip-dirs "**/node_modules" \
    --format table --output /out/trivy-report.txt \
    /src \
    || true
  echo "[INFO] 依赖漏洞报告(JSON): ${sca_out}/trivy-report.json"
  echo "[INFO] 依赖漏洞报告(TXT):  ${sca_out}/trivy-report.txt"
}

run_secrets() {
  echo "[INFO] === 密钥泄漏扫描 (gitleaks) ==="
  local gl_out="${OUT_DIR}/gitleaks-report.json"
  # 仓库只读挂载到 /repo，输出目录单独可写挂载到 /out（避免往只读路径写报告）
  docker run --rm \
    -v "${REPO_ROOT}:/repo:ro" \
    -v "${OUT_DIR}:/out" \
    "${GITLEAKS_IMAGE}" \
    detect --source /repo --no-git \
    --report-format json --report-path /out/gitleaks-report.json \
    || echo "[WARN] gitleaks 发现疑似密钥（详见报告）"
  echo "[INFO] 密钥泄漏报告: ${gl_out}"
}

run_sast() {
  echo "[INFO] === 源码静态扫描 (Semgrep: 注入/XSS/反序列化等) ==="
  local sast_out="${OUT_DIR}/semgrep-report.json"
  local semgrep_cache="${HOME}/.cache/semgrep"
  mkdir -p "${semgrep_cache}"
  # 使用 Semgrep 官方安全审计规则集（java + owasp top10 类）；离线扫描仓库源码
  docker run --rm \
    -v "${REPO_ROOT}:/src:ro" \
    -v "${OUT_DIR}:/out" \
    "${SEMGREP_IMAGE}" \
    semgrep scan --config "p/java" --config "p/owasp-top-ten" --config "p/secrets" \
    --metrics off --json --output /out/semgrep-report.json \
    --exclude "target" --exclude "node_modules" --exclude "*.min.js" \
    /src \
    || echo "[WARN] Semgrep 以非零码退出（通常是发现问题导致，报告已生成）"
  echo "[INFO] 源码静态报告: ${sast_out}"
}

case "${MODE}" in
  sca)     run_sca ;;
  secrets) run_secrets ;;
  sast)    run_sast ;;
  all)     run_sca; run_secrets; run_sast ;;
  *) echo "用法: bash scripts/security-scan.sh [all|sca|secrets|sast]"; exit 2 ;;
esac

# ---- 生成摘要 ----
SUMMARY="${OUT_DIR}/SUMMARY.md"
{
  echo "# 安全扫描摘要"
  echo
  echo "- 扫描时间: ${TS}"
  echo "- 分支/commit: ${GIT_BRANCH} @ ${GIT_COMMIT}"
  echo "- 扫描项: ${MODE}"
  echo "- 工具: Trivy (${TRIVY_IMAGE}) / gitleaks (${GITLEAKS_IMAGE})"
  echo
  echo "## 结果文件"
  echo "- 依赖漏洞: dependency-check/trivy-report.txt (人读) / trivy-report.json (机读)"
  echo "- 密钥泄漏: gitleaks-report.json"
  echo
  echo "## 下一步"
  echo "1. 打开 HTML 报告，按 Critical/High 逐条处理依赖升级。"
  echo "2. 用 docs/security/scan-reports/TEMPLATE/ 里的模板填写正式交付材料。"
  echo "3. 关键交付如需盖章报告，转商业工具/外包复扫（见选型指南）。"
} > "${SUMMARY}"

echo "[DONE] 摘要: ${SUMMARY}"
