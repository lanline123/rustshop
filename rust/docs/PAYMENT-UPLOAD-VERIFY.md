# 支付 / 上传 联调记录（已实测）

> 环境：MySQL 8.4（`tigshop-java`，demo.sql）+ Rust 服务（8080/8181）。
> 本轮把「支付（线下 + 余额）」与「上传全量」打通，以下命令均已实际执行通过。

## 0. C 端测试账号

`demo.sql` 的 `user` 表是空的；用脚本插入一个测试会员：

```bash
mysql -h 127.0.0.1 -u root -proot --default-character-set=utf8mb4 tigshop-java \
  -e "source D:/tigshop/java-b2b2b-pro-v5.8.57/rust/tools/compat/seed_test_member.sql"
```

- 用户名：`testbuyer`
- 手机号：`13800000001`
- 密码：`admin123`（BCrypt 哈希复用 demo admin 的哈希）
- 余额：`1000.00`

C 端登录（**手机号或用户名都可**，已兼容）：

```bash
curl -s -X POST http://127.0.0.1:8181/api/user/login/signin \
  -H 'Content-Type: application/json' \
  -d '{"username":"testbuyer","password":"admin123"}'
```

## 1. 余额支付（下单流程内，前端 `useBalance`）

前端结算页把 `useBalance` 作为**数字金额**提交；本轮修复：DTO 兼容数字/布尔，
下单时按 `min(请求额, 应付, 可用余额)` 原子扣减。

| 场景 | 请求 | 实测结果 |
|---|---|---|
| 全额抵扣 | `useBalance=10000`，订单 40 元 | `returnType=2`、`onlineAmount=0`；订单 `pay_status=2/order_status=1`、`paid_amount=40`、`balance=40`；用户余额 1000→960；写出 `paylog(pay_code=balance, pay_amount=40, pay_status=1)` |
| 部分抵扣 | `useBalance=10`，订单 40 元 | `returnType=1`、`onlineAmount=30`；订单 `paid_amount=10/unpaid_amount=30`；`paylog(balance,10,已支付)` |

## 2. 线下汇款 + 后台确认收款

1. 下单时 `payTypeId=3`（线下），订单保持待付款；
2. 前端/接口调用：
   ```bash
   curl -s -H "Authorization: Bearer $C_TOKEN" \
     "http://127.0.0.1:8181/api/order/pay/create?id=<orderId>&type=offline"
   # → {"orderId":4,"orderAmount":"40.00","payInfo":{"paySn":"PAY...","payType":"offline"}}
   ```
3. 后台确认收款：
   ```bash
   curl -s -X POST http://127.0.0.1:8080/adminapi/order/order/setPaid \
     -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' \
     -d '{"id":4,"description":"银行转账已到账"}'
   ```
4. 实测订单变为 `paid_amount=40 / unpaid_amount=0 / pay_status=2 / order_status=1`。

> 注意：`paylog(pay_code=offline)` 行仍为 `pay_status=0`（后台确认只更新订单）。
> 若需要资金流水完全一致，下一步可在 `order_admin::set_paid` 里同步把该订单
> 待支付 paylog 置为已支付。

## 3. 上传全量（本地存储 + `/static`）

| 接口 | 实测 |
|---|---|
| `POST /adminapi/setting/galleryPic/uploadImg?galleryId=0` | 返回 `/static/gallery/...png`，`gallery_pic` 新增行，静态可访问 |
| `POST /adminapi/setting/galleryVideoInfo/uploadVideo` | 返回 `/static/video/...mp4` |
| `POST /adminapi/setting/config/uploadFile?type=1` | 返回 `/static/config/...png` |
| `POST /adminapi/setting/appVersion/uploadFile` | 返回 `/static/app/...mp4`（APK 同路径） |

## 4. 本轮同时修复的阻塞问题

1. **无 SKU 商品下单误报库存不足**：扣库存时把 `sku_id=None` 传成了 `Some(0)`，
   导致 `UPDATE product_sku WHERE sku_id=0` 影响 0 行；已改为按购物车行的真实 `sku_id`（`None` 走 `product_stock`）。
2. **订单写入缺 `order_extension` 列**：`order` 表中该列 NOT NULL 且无默认值，
   insert 语句漏列导致 `1364 Field 'order_extension' doesn't have a default value`；已补列/绑定。
3. **C 端登录只认手机号**：改为先按手机号、再按用户名兜底（前端登录框可能填账号）。
4. **`OrderCreateDTO.useBalance` 类型**：由 `bool` 改为 `Decimal`（兼容数字/布尔/字符串）。
5. **`OrderCreateVO.returnType`**：补 `returnType`（余额全额抵扣返回 2，前端直接跳订单列表）。

## 5. 下一步（按既定优先级）

- 在线支付渠道（微信 v3 / 支付宝 / PayPal / 汇付等）：等沙箱证书，用 `reqwest + rsa/aes-gcm` 协议级实现；
- 后台确认收款时同步 paylog 状态；
- 验证码（先确认登录失败阈值不触发，再决定实现 AJ-Captcha）；
- IM WebSocket → 定时任务/MQ → RBAC。
