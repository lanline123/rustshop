# build-133 生产部署记录（2026-10-31）

## 构建

```text
GitHub Actions workflow: build-rust-backend
run: 41164972185
run_number: 133
commit: bc80f6a682438f6b3d6545fe821e8b69132fba73
release: build-133
binary: rustshop 52,577,992 bytes
sha256: 693c2ebd491b79d07ea0719120f040b1a35c1025c46f54a66360f5584970f14f
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
backup: /www/backup/tigshop.preB133.bak
部署方式: 宝塔计划任务 b133deploy（已删除）
```

部署后校验：

```text
b133.bin: OK
HTTP=200 ACTIVE=active
693c2ebd491b79d07ea0719120f040b1a35c1025c46f54a66360f5584970f14f  /www/wwwroot/tigshop-rust/tigshop
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
/adminapi/setting/imageSearch/detail      200
```

## 本版新增

```text
商户免审核直接开店
预售定金支付创建
商户审核结果通知适配器
分账批量提交适配器
通用支付网关适配器
微信直播建单关联/出站日志/直播上下文快照
Stripe/Xendit/PayPal/Antom 退款回调补齐
Apple Pay 可配置收单
子商户绑定
```

## 回滚

```bash
systemctl stop tigshop
cp -f /www/backup/tigshop.preB133.bak /www/wwwroot/tigshop-rust/tigshop
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
