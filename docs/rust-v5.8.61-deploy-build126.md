# build-126 生产部署记录（2026-10-31）

## 构建

```text
run: 41129992315
run_number: 126
commit: 5d341be54bdd9ec15a415525c3bea83a36d7fcd5
release: build-126
binary: 51,625,448 bytes
sha256: 732fae346f054a7295e804ded49f24ad7511de8671e26b5d2dcc2b5ccbb59f8b
```

## 部署

```text
systemd: tigshop
backup: /www/backup/tigshop.preB126.bak
HTTP=200
ACTIVE=active
binary checksum: 732fae34...f8b
```

## 冒烟

```text
/api/health                       200
/api/home/home/pcIndex            200
/api/product/product/list         200
admin login                       success
/adminapi/pos/table/list?shopId=0 200
```

POS 桌台列表线上返回 `code=0`。

## 回滚

```bash
systemctl stop tigshop
cp -f /www/backup/tigshop.preB126.bak /www/wwwroot/tigshop-rust/tigshop
chmod +x /www/wwwroot/tigshop-rust/tigshop
systemctl start tigshop
```
