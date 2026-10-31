# B4 图片搜索实现记录

## 状态

```text
配置 / 任务台账 / 接口契约：已完成
阿里云图库检索适配：未接入
编译：cargo check 通过
部署：未部署
```

## 管理端

```text
GET  /adminapi/setting/imageSearch/regions
GET  /adminapi/setting/imageSearch/detail
POST /adminapi/setting/imageSearch/save
POST /adminapi/setting/imageSearch/check
POST /adminapi/setting/imageSearch/build
POST /adminapi/setting/imageSearch/syncProduct
GET  /adminapi/setting/imageSearch/task
POST /adminapi/setting/imageSearch/retry
```

配置键：

```text
imageSearchEnabled / H5Enabled / PcEnabled / AppEnabled / ShopEnabled
imageSearchProvider / IndexName / SimilarityPercent / MaxResults
imageSearchAliyunRegionId / AccessKeyId / AccessKeySecret / SecurityToken
imageSearchAliyunVpc / ConnectTimeoutMillis / ReadTimeoutMillis
imageSearchVersion
```

保存逻辑：

```text
version CAS 防止并发覆盖
逐项写 config 表
凭证读取时返回掩码
```

任务台账：

```text
image_search_sync_job
```

build / syncProduct 创建 PENDING 任务，task 查询、retry 重置为 PENDING。

## 公开端

```text
GET  /api/search/image/capabilities
POST /api/search/image/search
POST /api/search/image/results
```

capabilities 返回启用端和格式。

search/results 当前行为：

```text
未启用 → 400 图像搜索未启用
已启用但云适配未接入 → 400 图像搜索服务暂不可用
```

`search` 不返回伪造结果，避免前端把空列表当成真实搜索。

## 验证

```text
cargo check -p tigshop-admin-api -p tigshop-api
Finished
```

## 未完成

```text
阿里云 ImageSearch SDK / 签名与上传
图片预处理、限流、超时
image_search_image 台账写入
全量构建 worker
公开端真实分页结果
商品可见性/价格二次过滤
完整二进制 build / 部署
```

## 下一步

B4 剩余项需要接入阿里云图库。可选：

```text
A. Rust 内实现阿里云 RPC 签名与 ImageSearch 调用
B. 单独 sidecar 服务承接云图库，Rust 只做配置/台账/转发
```

建议选 B，避免把云厂商 SDK 和签名逻辑耦合进主 API。

## 2026-10-31 补充：阿里云图库检索

已接入：

```text
POST /api/search/image/search
```

流程：

```text
读取 imageSearch* 配置
校验启用、provider=aliyun、凭证/index/region
解析 multipart file
base64 PicContent
阿里云 ImageSearch RPC SearchImageByPic
按 Auctions ProductId 查商品
过滤上架/未删除/禁售
返回商品记录
```

签名：

```text
RPC HMAC-SHA1
Version 2019-03-25
POST&%2F&canonical
```

`results` 当前无跨请求会话，返回空会话；后续可加 Redis 会话。

验证：

```text
cargo test -p tigshop-api image_search
2 passed
cargo test --workspace --lib
全部通过
```

SQL 冒烟：

```text
本地 product_id=621 返回 JSON 商品记录
```

## 2026-10-31 补充：真实 OSS 上传 + 图库构建 worker

修正：

```text
ImageSearch API Version 2019-03-25 → 2020-12-14
SearchImageByPic 不再传 base64，改为 AuthorizeFileUpload → OSS → PicContent URL
```

新增后台能力：

```text
setting/imageSearch/check  真实调用 Detail
setting/imageSearch/build  后台扫描商品图并 AddImage
setting/imageSearch/syncProduct 单商品构建
setting/imageSearch/retry  重新执行任务
```

构建流程：

```text
商品主图/原图/大图/缩略图
→ 下载
→ AuthorizeFileUpload
→ OSS 上传
→ AddImage(PicName=product-{id}.jpg, ProductId=商品ID)
→ 更新 image_search_sync_job 进度
```

真实测试已完成：

```text
AddImage 200
SearchImageByPic 200
Score=1.0
ProductId=1576
```

实例：

```text
batterysearch / cn-beijing
```
