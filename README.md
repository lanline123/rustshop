# rustshop

TigShop Rust 后端构建仓库（仅包含 Rust 后端源码 + CI 配置）。

## 构建

- 推送代码到 `main` 会自动触发 `.github/workflows/build-rust.yml`
- 构建环境：`rockylinux:8` 容器（glibc 2.28，与目标服务器 Alibaba Cloud Linux 3 一致）
- 产物发布到 **Releases**：`https://github.com/lanline123/rustshop/releases`
  - 资产名：`tigshop`（Linux x86_64 可执行文件）

## 手动触发

GitHub → Actions → build-rust-backend → Run workflow

## 部署

把 Release 里的 `tigshop` 上传到服务器 `/www/wwwroot/tigshop-rust/tigshop`，
然后 `systemctl restart tigshop`（配置文件 config.yaml 不在此仓库，单独部署）。