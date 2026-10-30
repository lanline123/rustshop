# build-124 生产部署记录（2026-10-31）

## 构建

```text
run: 41123454217
run_number: 124
commit: f3aafd35b3b2eaf74cade4cf89d8d978778689b3
release: build-124
binary size: 51,617,512
sha256: feabee9c81927c004968e614a6c87bb6428ce11369df56f8a36aee6029600f4c
```

## 部署

```text
systemd: tigshop
backup: /www/backup/tigshop.preB124.bak
binary checksum on server: feabee9c...f4c
HTTP=200
ACTIVE=active
```

## 冒烟

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

图搜未配置时：

```text
POST /api/search/image/search
{"code":400,"msg":"图像搜索未启用"}
```

符合预期，需后台配置阿里云图搜凭证并启用后才可识图。

## 回滚

```bash
systemctl stop tigshop
cp -f /www/backup/tigshop.preB124.bak /www/wwwroot/tigshop-rust/tigshop
chmod +x /www/wwwroot/tigshop-rust/tigshop
systemctl start tigshop
```
