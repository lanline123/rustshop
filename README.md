# rustshop

Rust 后端构建仓库（仅包含后端源码 + CI 配置）。

## 构建

- 推送代码到 `main` 会自动触发 `.github/workflows/build-rust.yml`
- 构建环境：`rockylinux:8` 容器（glibc 2.28，与目标服务器 Alibaba Cloud Linux 3 一致）
- 产物发布到 **Releases**：`https://github.com/lanline123/rustshop/releases`
  - 资产为一个 Linux x86_64 可执行文件

## 手动触发

GitHub → Actions → build-rust-backend → Run workflow

## 部署

把 Release 里的可执行文件上传到服务器部署目录，
然后重启对应的 systemd 服务（运行配置文件不在本仓库，单独部署）。
