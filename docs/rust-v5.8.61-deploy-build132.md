# build-132 生产部署记录（2026-10-31）

## 构建

```text
GitHub Actions workflow: build-rust-backend
run: 41162302623
run_number: 132
commit: f804f6995353e13de2dafdb6b9cfe202b0b240e4
release: build-132
binary: rustshop 52,540,776 bytes
sha256: 2f6e0eb6c926259bfc3573c7680d4fb162ede3f51c80095e40250e07c58b3c21
```

## 测试与前端

```text
cargo test --workspace -j 1
约 4562 passed / 0 failed / 1 ignored

rust/web/admin-semi npm run build
tsc + vite 通过（仅 chunk 体积警告）
```

## 部署

```text
systemd: tigshop
WorkingDirectory: /www/wwwroot/tigshop-rust
ExecStart: /www/wwwroot/tigshop-rust/tigshop
backup: /www/backup/tigshop.preB132.bak
部署方式: 宝塔计划任务 b132deploy（已删除）
```

部署后二进制校验：

```text
2f6e0eb6c926259bfc3573c7680d4fb162ede3f51c80095e40250e07c58b3c21
```

服务状态：

```text
tigshop active
HTTP=200
```

## 线上冒烟

```text
/                                         200
/api/health                               200
/adminapi/health                          200
/api/product/product/list                 200
/api/home/home/pcIndex                    200
/api/search/image/capabilities            200
/admin/                                   200
/adminapi/setting/imageSearch/detail      200
```

## 回滚

```bash
systemctl stop tigshop
cp -f /www/backup/tigshop.preB132.bak /www/wwwroot/tigshop-rust/tigshop
chmod +x /www/wwwroot/tigshop-rust/tigshop
systemctl start tigshop
```

## 结论

```text
Linux 构建成功
全量测试通过
生产切换成功
核心页面/新接口冒烟通过
```
