# build-123 生产部署记录（2026-10-31）

## 构建

```text
GitHub Actions workflow: build-rust-backend
run: 41118372489
run_number: 123
commit: f0b6f3299934adcae3f574b49a57fb099d9909ab
release: build-123
binary: rustshop 51,593,624 bytes
sha256: b18357bf8258638b80b046d211883e07ce8866884df6c84dc322662db388a08a
```

## 部署

```text
systemd: tigshop
WorkingDirectory: /www/wwwroot/tigshop-rust
ExecStart: /www/wwwroot/tigshop-rust/tigshop
backup: /www/backup/tigshop.preB123.bak
```

部署后二进制校验：

```text
b18357bf8258638b80b046d211883e07ce8866884df6c84dc322662db388a08a
```

服务状态：

```text
tigshop active
admin health 200
```

## 线上冒烟

```text
/                                 200
/api/health                       200
/adminapi/health                  200
/api/product/product/list         200
/api/home/home/pcIndex            200
/api/search/image/capabilities    200
/admin/                           200
admin login                       success
```

新接口：

```text
/adminapi/product/product/saleStatus?productId=1576   200
/adminapi/merchant/shopGroup/list?shopType=1           200
/adminapi/supplyChain/supplier/list                    200
/adminapi/shop/tip/list                                200
/adminapi/setting/imageSearch/detail                   200
/adminapi/order/offlinePaymentClaim/list               200
```

## 回滚

```bash
systemctl stop tigshop
cp -f /www/backup/tigshop.preB123.bak /www/wwwroot/tigshop-rust/tigshop
chmod +x /www/wwwroot/tigshop-rust/tigshop
systemctl start tigshop
```

## 结论

```text
Linux 构建成功
生产切换成功
旧页面与新接口冒烟通过
```
