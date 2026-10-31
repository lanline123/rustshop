# build-135 生产部署记录（2026-10-31）

## 构建

```text
GitHub Actions workflow: build-rust-backend
run: 41168814643
run_number: 135
commit: 35f0efa08e37f1ca8bbec950df991ec154a02617
release: build-135
binary: rustshop 52,609,560 bytes
sha256: c4f05fbf61736f4c08c911650fd795d84f248470d545701211edc2db09a1118d
```

## 测试

```text
cargo test --workspace -j 1
约 4562 passed / 0 failed / 1 ignored
```

## 部署

```text
systemd: tigshop
WorkingDirectory: /www/wwwroot/tigshop-rust
ExecStart: /www/wwwroot/tigshop-rust/tigshop
backup: /www/backup/tigshop.preB135.bak
部署方式: 宝塔计划任务 b135deploy（已删除）
```

部署后校验：

```text
b135.bin: OK
HTTP=200 ACTIVE=active
c4f05fbf61736f4c08c911650fd795d84f248470d545701211edc2db09a1118d  /www/wwwroot/tigshop-rust/tigshop
DONE
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
```

## 本版新增

```text
商户进件 submit/apply/approve/refresh 渠道适配器
商户进件媒体上传/预览
预售定金支付
商户免审核开店
微信直播建单关联/出站日志/上下文快照
商户审核通知、分账提交、通用支付网关
```

## 缺口核对

```text
路由契约 missing=0
运行时未迁移：仅未知支付方式回退（预期行为）
全工作区测试：0 failed
```

## 回滚

```bash
systemctl stop tigshop
cp -f /www/backup/tigshop.preB135.bak /www/wwwroot/tigshop-rust/tigshop
chmod +x /www/wwwroot/tigshop-rust/tigshop
systemctl start tigshop
```
