//! 轻量定时任务：替代 Java 的 RabbitMQ 延迟队列（订单超时取消/自动收货）。
//!
//! Java 在 `OrderCheckServiceImpl` / `OrderServiceImpl` 里通过 RabbitMQ
//! `x-delay` 延迟消息 + `RabbitMqConsumer` 实现「未付款订单超时取消」
//! 「发货后自动收货」。本实现改为**轮询扫描 + 幂等条件更新**，无需 MQ：
//!
//! - `order_auto_cancel`：默认每分钟扫描 `pay_status=1 && order_status=0` 且
//!   `add_time <= now - autoCancelOrderMinute*60` 的订单，调用订单取消（回滚库存）；
//! - `order_auto_receive`：默认每 5 分钟扫描 `order_status=2` 且
//!   `shipping_time <= now - autoDeliveryDays*86400` 的订单，置为已完成。
//!
//! 配置项与 Java 一致：`config` 表 `autoCancelOrderMinute` / `autoDeliveryDays`；
//! 值为空或 <= 0 时该任务跳过（与 Java 默认关闭一致）。
//!
//! 运维/测试可通过环境变量调整节奏：
//! `TIG_SCHEDULER__ENABLED=false` 关闭；`TIG_SCHEDULER__DELAY_SECS`（默认 30）、
//! `TIG_SCHEDULER__CANCEL_SECS`（默认 60）、`TIG_SCHEDULER__RECEIVE_SECS`（默认 300）。
//!
//! 多实例说明：当前无分布式锁（单实例部署）。多实例时需加 Redis `SET NX`
//! 或改用 MQ；任务本身幂等，重复执行不会重复扣库存/改状态。
use std::sync::Arc;
use std::time::Duration;

use tokio::task::JoinHandle;

use tigshop_common::utils;
use tigshop_core::Result;
use tigshop_infrastructure::db::Repositories;

use crate::trade::order::OrderService;

/// 未付款订单超时时间（分钟）配置项。
pub const CFG_AUTO_CANCEL_ORDER_MINUTE: &str = "autoCancelOrderMinute";
/// 自动收货天数配置项。
pub const CFG_AUTO_DELIVERY_DAYS: &str = "autoDeliveryDays";

/// 每轮扫描的最大订单数，避免一次锁太多行。
const SCAN_BATCH: u64 = 100;

/// 读取环境变量秒数（缺省返回 default）。
fn env_secs(key: &str, default: u64) -> u64 {
    std::env::var(key)
        .ok()
        .and_then(|v| v.parse::<u64>().ok())
        .filter(|v| *v > 0)
        .unwrap_or(default)
}

/// 解析整数配置（缺省/非法 → None）。
#[must_use]
pub fn parse_positive_i64(raw: Option<&str>) -> Option<i64> {
    raw.map(str::trim)
        .and_then(|v| v.parse::<i64>().ok())
        .filter(|v| *v > 0)
}

/// 解析天数配置（支持 `30` / `30.0`；缺省/非法 → None），返回秒数。
#[must_use]
pub fn parse_positive_days(raw: Option<&str>) -> Option<i64> {
    let value = raw.map(str::trim)?.parse::<f64>().ok()?;
    if value <= 0.0 {
        return None;
    }
    Some((value * 86_400.0) as i64)
}

/// 启动全部定时任务，返回 JoinHandle（由进程生命周期管理）。
pub fn spawn_all(repos: Arc<Repositories>, order: OrderService) -> Vec<JoinHandle<()>> {
    vec![
        tokio::spawn(order_auto_cancel_loop(repos.clone(), order.clone())),
        tokio::spawn(order_auto_receive_loop(repos.clone())),
        tokio::spawn(groupon_expire_loop(repos)),
    ]
}

/// 订单超时取消循环。
async fn order_auto_cancel_loop(repos: Arc<Repositories>, order: OrderService) {
    // 启动后延迟，避开启动高峰
    tokio::time::sleep(Duration::from_secs(env_secs(
        "TIG_SCHEDULER__DELAY_SECS",
        30,
    )))
    .await;
    let mut ticker = tokio::time::interval(Duration::from_secs(env_secs(
        "TIG_SCHEDULER__CANCEL_SECS",
        60,
    )));
    loop {
        ticker.tick().await;
        match run_auto_cancel(&repos, &order).await {
            Ok(0) => {}
            Ok(n) => tracing::info!("定时任务：超时取消订单 {n} 笔"),
            Err(e) => tracing::warn!("定时任务：订单超时取消失败: {e}"),
        }
    }
}

/// 自动收货循环。
async fn order_auto_receive_loop(repos: Arc<Repositories>) {
    tokio::time::sleep(Duration::from_secs(
        env_secs("TIG_SCHEDULER__DELAY_SECS", 30) + 30,
    ))
    .await;
    let mut ticker = tokio::time::interval(Duration::from_secs(env_secs(
        "TIG_SCHEDULER__RECEIVE_SECS",
        300,
    )));
    loop {
        ticker.tick().await;
        match run_auto_receive(&repos).await {
            Ok(0) => {}
            Ok(n) => tracing::info!("定时任务：自动确认收货 {n} 笔"),
            Err(e) => tracing::warn!("定时任务：自动收货失败: {e}"),
        }
    }
}

/// 拼团过期循环（每 60s；`TIG_SCHEDULER__GROUPON_SECS` 可调）。
async fn groupon_expire_loop(repos: Arc<Repositories>) {
    tokio::time::sleep(Duration::from_secs(
        env_secs("TIG_SCHEDULER__DELAY_SECS", 30) + 15,
    ))
    .await;
    let mut ticker = tokio::time::interval(Duration::from_secs(env_secs(
        "TIG_SCHEDULER__GROUPON_SECS",
        60,
    )));
    loop {
        ticker.tick().await;
        match run_groupon_expire(&repos).await {
            Ok(0) => {}
            Ok(n) => tracing::info!("定时任务：拼团过期置失败 {n} 团"),
            Err(e) => tracing::warn!("定时任务：拼团过期处理失败: {e}"),
        }
    }
}

/// 执行一轮拼团过期：待成团且超过截止时间的开团记录整团置失败。
///
/// # Errors
/// 数据库错误。
pub async fn run_groupon_expire(repos: &Repositories) -> Result<usize> {
    let now = utils::now_seconds() as i64;
    let records = repos
        .marketing
        .groupon_promotion_record
        .find_expired_waiting(now, SCAN_BATCH)
        .await?;
    let mut count = 0;
    for record in records {
        let affected = repos
            .marketing
            .groupon_promotion_record
            .mark_failed_by_open(record.groupon_promotion_record_id)
            .await?;
        if affected > 0 {
            count += 1;
            tracing::info!(
                "拼团过期: open_record={} 影响成员 {}",
                record.groupon_promotion_record_id,
                affected
            );
        }
    }
    Ok(count)
}

/// 执行一轮超时取消，返回处理笔数。
///
/// # Errors
/// 数据库错误或配置读取失败时返回错误（loop 侧只记日志）。
pub async fn run_auto_cancel(repos: &Repositories, order_service: &OrderService) -> Result<u64> {
    let raw = repos
        .system
        .config_basic
        .find_by_code(CFG_AUTO_CANCEL_ORDER_MINUTE)
        .await?;
    let Some(minutes) = parse_positive_i64(raw.as_deref()) else {
        return Ok(0);
    };
    let now = i64::from(utils::now_seconds());
    let cutoff = now
        .saturating_sub(minutes * 60)
        .clamp(0, i64::from(i32::MAX)) as i32;
    let ids = repos
        .trade
        .order
        .find_unpaid_before(cutoff, SCAN_BATCH)
        .await?;
    let mut done = 0_u64;
    for order_id in ids {
        // 复用订单取消逻辑（状态条件更新 + 回滚库存）；并发下可能返回冲突，忽略
        match order_service.cancel(order_id, None).await {
            Ok(()) => done += 1,
            Err(e) => tracing::debug!("自动取消订单 {order_id} 跳过: {e}"),
        }
    }
    Ok(done)
}

/// 执行一轮自动收货，返回处理笔数。
pub async fn run_auto_receive(repos: &Repositories) -> Result<u64> {
    let raw = repos
        .system
        .config_basic
        .find_by_code(CFG_AUTO_DELIVERY_DAYS)
        .await?;
    let Some(seconds) = parse_positive_days(raw.as_deref()) else {
        return Ok(0);
    };
    let now = i64::from(utils::now_seconds());
    let cutoff = now
        .saturating_sub(seconds)
        .clamp(0, i64::from(i32::MAX)) as i32;
    let ids = repos
        .trade
        .order
        .find_shipped_before(cutoff, SCAN_BATCH)
        .await?;
    let mut done = 0_u64;
    for order_id in ids {
        if repos
            .trade
            .order
            .mark_received(order_id, now as i32)
            .await?
        {
            done += 1;
        }
    }
    Ok(done)
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn parse_minutes_rules() {
        assert_eq!(parse_positive_i64(Some("30")), Some(30));
        assert_eq!(parse_positive_i64(Some(" 0 ")), None);
        assert_eq!(parse_positive_i64(Some("-5")), None);
        assert_eq!(parse_positive_i64(Some("abc")), None);
        assert_eq!(parse_positive_i64(None), None);
    }

    #[test]
    fn parse_days_rules() {
        assert_eq!(parse_positive_days(Some("7")), Some(7 * 86_400));
        assert_eq!(parse_positive_days(Some("0.5")), Some(43_200));
        assert_eq!(parse_positive_days(Some("0")), None);
        assert_eq!(parse_positive_days(None), None);
    }

    #[test]
    fn env_secs_falls_back() {
        assert_eq!(env_secs("TIG_SCHEDULER__NOT_EXISTS", 42), 42);
    }
}
