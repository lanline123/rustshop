//! 微信直播订单同步回调 域服务（slice-115）。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-115.md`。
//!
//! 对应 Java `WechatLiveSyncCallbackController.callback`
//! （`java/tigshop-api/src/main/java/com/tigshop/api/controller/content/WechatLiveSyncCallbackController.java:34`）
//! → `WechatLiveOrderSyncServiceImpl.handleInboundCallback`
//! （`java/tigshop-service/src/main/java/com/tigshop/service/content/impl/WechatLiveOrderSyncServiceImpl.java:228`）。
//!
//! ## 迁移范围（诚实标注）
//!
//! | Java 步骤 | 本片状态 |
//! |-----------|---------|
//! | `@Valid` 校验 eventType / idempotentKey（`@NotBlank`） | ✅ 已复刻（文案与业务码一致） |
//! | `resolveOrderId`：body 的 orderId → 否则按 orderSn 查 `order` | ✅ 已复刻 |
//! | `shouldAutoCreateOrder` 判定 | ✅ 已复刻（纯函数，见 models） |
//! | `createOrderFromWechatCallback`：**微信回调自动建单** | ❌ **未迁移** → 命中即返回明确业务错误（见 [`AUTO_CREATE_UNMIGRATED_MSG`]） |
//! | roomId 兜底：`wechat_live_order_rel` 最新一条非空 room_id | ✅ 已复刻 |
//! | 写 `wechat_live_sync_log`（INBOUND / 成功 / 固定应答） | ✅ 已复刻 |
//! | `updateRelationStatus(orderId, 部分同步)` | ✅ 已复刻 |
//! | `applyLastCallback` 并入 `order.order_extension` | ✅ 已复刻 |
//! | `@Transactional(rollbackFor = Exception.class)` | ✅ 三步写同事务（见仓储 `write_inbound_callback`） |
//! | `@Idempotent(prefix="wechatLiveSyncCallback", key="#callbackDTO.idempotentKey")` | ❌ **未迁移**（见下） |
//!
//! ### 为什么自动建单不在本片实现
//!
//! Java 的 `createOrderFromWechatCallback`（`:307-544`）会读
//! `user_authorize` / `product` / `product_sku` / `wechat_live_goods` /
//! `wechat_live_room_goods` / `paylog` / `config` 等 7+ 张表、调用微信
//! `WechatApiClient` 与 `WechatOAuthService`，并逐字段拼出 `order` +
//! `order_item`（40 多个字段、含促销快照与地区解析）。这已是一整个
//! 「微信直播建单」域，与本片的 1 个回调端点不是同一个迁移单元；
//! 在本片里凭猜测拼 `order` 行会写出**看起来能跑、金额与状态全错**的脏数据。
//! 因此：命中该分支时返回**明确可操作的业务错误**，而不是静默落一条半残订单。
//!
//! ### 为什么幂等注解未迁移
//!
//! Java `@Idempotent` 由 `IdempotentAspect` 用 **Redis** `tryLock`
//! （键 `idempotent:wechatLiveSyncCallback:{idempotentKey}`，默认 10 分钟，
//! 冲突文案「操作正在处理中，请勿重复提交」）实现，且 `finally` 里删键，
//! 所以它只拦「并发重复提交」，不拦「先后两次相同回调」。
//! Rust 侧 `CacheService` 存在，但 `Slice115Service` 的构造签名固定为
//! `new(repos: Arc<Repositories>)`，`Repositories` 不含 Redis 连接，
//! 本片又禁止改共享文件（`services/src/lib.rs` / `db/mod.rs` / `main.rs`），
//! 故**无法**在本切片接入该锁。当前实现下，重复回调会重复写日志行
//! （Java 在锁释放后同样会重复写，差异仅在「并发窗口内的重复提交」）。

use std::sync::Arc;

use tigshop_core::models::slice115::{WechatLiveSyncCallbackDTO, AUTO_CREATE_UNMIGRATED_MSG, SERVICE_DATA_ERROR};
use tigshop_core::{Error, Result};
use tigshop_infrastructure::db::repo::slice115::InboundLogWrite;
use tigshop_infrastructure::db::Repositories;

/// 微信直播订单同步回调 域服务。
pub struct Slice115Service {
    repos: Arc<Repositories>,
}

impl Slice115Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice115::Slice115Repository {
        &self.repos.slice115
    }

    /// 处理入站回调（对应 Java `handleInboundCallback`）。
    ///
    /// 返回 `Ok(())` 即「回调已接受」，与 Java `void`（信封 `data = null`）一致。
    ///
    /// # Errors
    /// - `1001` + `eventType不能为空` / `idempotentKey不能为空`（`@Valid`）；
    /// - `1001` + [`AUTO_CREATE_UNMIGRATED_MSG`]（需要自动建单的回调）；
    /// - `Database(...)`：落库失败（整事务回滚）。
    pub async fn handle_inbound_callback(&self, dto: &WechatLiveSyncCallbackDTO) -> Result<()> {
        // 1. Java `@Valid @RequestBody`
        dto.validate()?;

        // 2. Java `resolveOrderId`：优先 body 的 orderId，其次按 orderSn 查单
        let mut order_id = dto.declared_order_id();
        if order_id.is_none() {
            if let Some(order_sn) = dto.effective_order_sn() {
                order_id = self.repo().find_order_id_by_sn(order_sn).await?;
            }
        }

        // 3. Java `if (orderId == null && shouldAutoCreateOrder(...)) createOrder...`
        if order_id.is_none() && dto.should_auto_create_order() {
            tracing::warn!(
                event_type = %dto.event_type.as_deref().unwrap_or(""),
                idempotent_key = %dto.idempotent_key.as_deref().unwrap_or(""),
                "微信直播回调要求自动建单，但该链路未迁移，返回业务错误（未写任何数据）"
            );
            return Err(Error::biz(SERVICE_DATA_ERROR, AUTO_CREATE_UNMIGRATED_MSG));
        }

        // 4. Java roomId 兜底：dto.roomId ?? rel 表最新一条非空 room_id
        let mut room_id = dto.room_id;
        if room_id.is_none() {
            if let Some(oid) = order_id {
                room_id = self.repo().find_latest_room_id_by_order(oid).await?;
            }
        }

        // 5. 三步写库（同事务）
        self.repo()
            .write_inbound_callback(&InboundLogWrite {
                order_id,
                aftersale_id: dto.aftersale_id,
                refund_id: dto.refund_id,
                room_id,
                event_type: dto.event_type.clone().unwrap_or_default(),
                idempotent_key: dto.idempotent_key.clone().unwrap_or_default(),
                request_data: dto.request_data_json(),
                callback_snapshot: dto.callback_snapshot(),
                now: current_seconds(),
            })
            .await
    }
}

/// Java `StringUtils.getCurrentTime()`：秒级时间戳。
#[must_use]
pub fn current_seconds() -> i64 {
    chrono::Utc::now().timestamp()
}

/// 请求体反序列化（对应 Spring 的 `@RequestBody` +
/// `HttpMessageNotReadableException` → 业务码 1001「参数错误」）。
///
/// 用 `serde_json::from_slice` 而非 axum 的 `Json` 提取器，是为了把
/// 「JSON 不合法」的错误码/文案对齐 Java（`Json` 提取器只会给出 400 + 英文文本）。
///
/// # Errors
/// `1001` + 「参数错误」。
pub fn parse_callback_body(raw: &[u8]) -> Result<WechatLiveSyncCallbackDTO> {
    match serde_json::from_slice::<WechatLiveSyncCallbackDTO>(raw) {
        Ok(dto) => Ok(dto),
        Err(e) => {
            // 原始细节只进日志，不外泄（Java 同样只回「参数错误」）
            tracing::warn!("微信直播回调请求体解析失败: {e}");
            Err(Error::biz(SERVICE_DATA_ERROR, PARAM_INVALID_MSG))
        }
    }
}

/// `error.param.invalid=参数错误`（i18n `messages.properties:80`）。
pub const PARAM_INVALID_MSG: &str = "参数错误";

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn parse_body_ok_on_java_shape() {
        let raw = br#"{"eventType":"WECHAT_ORDER_CALLBACK","idempotentKey":"k-1","orderSn":"WX123"}"#;
        let dto = parse_callback_body(raw).unwrap();
        assert_eq!(dto.event_type.as_deref(), Some("WECHAT_ORDER_CALLBACK"));
        assert_eq!(dto.order_sn.as_deref(), Some("WX123"));
        assert_eq!(dto.order_id, None);
    }

    #[test]
    fn parse_body_invalid_json_yields_java_param_error() {
        let cases: Vec<&[u8]> = vec![
            b"".as_slice(),
            b"not json".as_slice(),
            b"{\"eventType\":".as_slice(),
            b"[]".as_slice(),
            br#"{"eventType":123,"idempotentKey":"k"}"#.as_slice(),
        ];
        for raw in cases {
            let err = parse_callback_body(raw).unwrap_err();
            match err {
                Error::Biz { code, msg } => {
                    assert_eq!(code, SERVICE_DATA_ERROR);
                    assert_eq!(msg, PARAM_INVALID_MSG);
                }
                other => panic!("期望 Biz，得到 {other:?}"),
            }
        }
    }

    #[test]
    fn validation_precedes_auto_create_decision() {
        // 缺 eventType：即使 payload 要求自动建单，也必须先报校验错（Java @Valid 在切面/方法体之前）
        let dto: WechatLiveSyncCallbackDTO = serde_json::from_value(serde_json::json!({
            "idempotentKey": "k",
            "payload": {"productId": 1}
        }))
        .unwrap();
        let err = dto.validate().unwrap_err().to_string();
        assert!(err.contains("eventType不能为空"), "{err}");
        assert!(dto.should_auto_create_order());
    }

    #[test]
    fn auto_create_only_when_no_order_resolvable() {
        // Java：orderId 已知（body 或 orderSn 命中）→ 不走自动建单
        let dto: WechatLiveSyncCallbackDTO = serde_json::from_value(serde_json::json!({
            "eventType": "E", "idempotentKey": "K", "orderId": 9,
            "payload": {"items": [{"productId": 1}]}
        }))
        .unwrap();
        assert!(dto.declared_order_id().is_some());
        // 有 productId 但 orderId 为 null → 需要自动建单（本构建会报错）
        let dto: WechatLiveSyncCallbackDTO = serde_json::from_value(serde_json::json!({
            "eventType": "E", "idempotentKey": "K",
            "payload": {"productId": 1}
        }))
        .unwrap();
        assert!(dto.declared_order_id().is_none());
        assert!(dto.should_auto_create_order());
    }

    #[test]
    fn current_seconds_is_second_granularity() {
        // 与库表 `int`/`bigint` 秒级语义一致（不是毫秒）
        let now = current_seconds();
        assert!(now > 1_700_000_000 && now < 4_000_000_000, "{now}");
    }
}
