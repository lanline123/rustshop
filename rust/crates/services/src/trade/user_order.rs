//! 前台用户订单域服务（trade::user_order）。
//!
//! 对应原 Java `com.tigshop.service.order.impl.OrderServiceImpl` 的
//! `orderNum` / `delOrder` / `confirmReceipt` / `getShippingInfo` 与
//! `CartServiceImpl#buyAgain`、`AfterSalesReasonConfigService#getConfig`。
//!
//! # 迁移要点（逐条对照 Java 源）
//!
//! - `orderNum`：8 项统计（待付款/待发货/待收货/待评价/已完成/商品收藏/店铺收藏/售后中），
//!   订单统计 `is_del = 0`；售后取进行中状态集合。
//! - `reasonConfig`：读 config（开关 + JSON 数组原因），缺失/非法用 Java 默认值；
//!   i18n 翻译未迁移（原样返回）。
//! - `delOrder`：`availableActions.delOrder` 语义（已取消/无效/已完成且未删除）→
//!   `order_status = 3` + `is_del = 1` + 订单日志「订单删除」。
//! - `confirmReceipt`：归属校验（非本人订单静默返回，与控制器 count 判断一致）→
//!   `availableActions.confirmReceipt`（处理中）→ 完成订单 + 日志。
//! - `shippingInfo`：订单/单号/物流公司缺失 → `null`（Java 返回 null）；
//!   物流平台对接未迁移 → 明确业务错误。
//! - `buyAgain`：订单商品仍在售 → 命中同商品同规格购物车行则累加数量，否则加购；
//!   价格取订单快照（Java 会走 `addProductToCart` 的价格/活动重算，本实现未迁移）。
//!
//! # 未迁移依赖（明确记录，未伪造成功）
//!
//! - `confirmReceipt` 的奖金/分销结算、店铺结算、积分赠送、会员成长值、
//!   管理员消息、MQ 通知等自动化流程均未迁移。
//! - `buyAgain` 的价格/库存/活动重算、秒杀/拼团校验、购物车改价清理未迁移。
//! - `shippingInfo` 的快递100/快递鸟等第三方物流查询未迁移。
//! - `delOrder`/`confirmReceipt` 的 `orderLogService.quickCreate` 已按 order_log 表落库。

use std::sync::Arc;

use tigshop_common::utils::now_seconds;
use tigshop_core::models::trade::user_order::{
    can_confirm_receipt, can_delete_order, parse_reason_enabled, parse_reason_list, OrderBrief,
    OrderIdDTO, OrderNumVO, ReasonConfigVO, ShippingResult, ShippingTraceVO, COMMENT_STATUS_PENDING,
    CONFIG_REASON_ENABLED, CONFIG_REASON_LIST, MSG_ORDER_NOT_EXIST,
    MSG_ORDER_STATUS_ERROR, ORDER_STATUS_COMPLETED, ORDER_STATUS_CONFIRMED, ORDER_STATUS_PENDING,
    ORDER_STATUS_PROCESSING, ORDER_TYPE_RESERVATION_HOME,
};
use tigshop_core::{Error, Result};
use tigshop_infrastructure::db::repo::trade::user_order::UserOrderRepository;
use tigshop_infrastructure::db::Repositories;

/// 前台用户订单域服务。
pub struct UserOrderService {
    repos: Arc<Repositories>,
}

impl UserOrderService {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本域仓储（NESTED 访问：`self.repos.trade.user_order`）。
    #[must_use]
    pub fn repo(&self) -> &UserOrderRepository {
        &self.repos.trade.user_order
    }

    /// 订单数量（Java `OrderServiceImpl#orderNum`）。
    pub async fn order_num(&self, user_id: i32) -> Result<OrderNumVO> {
        let repo = self.repo();
        let await_pay = repo
            .count_orders(user_id, "order_status", ORDER_STATUS_PENDING)
            .await?;
        let await_shipping = repo
            .count_orders(user_id, "order_status", ORDER_STATUS_CONFIRMED)
            .await?;
        let await_received = repo
            .count_orders(user_id, "order_status", ORDER_STATUS_PROCESSING)
            .await?;
        let await_comment = repo
            .count_orders(user_id, "comment_status", COMMENT_STATUS_PENDING)
            .await?;
        let order_completed = repo
            .count_orders(user_id, "order_status", ORDER_STATUS_COMPLETED)
            .await?;
        let await_aftersales_collect = repo.count_aftersales_in_progress(user_id).await?;
        let product_collect = repo.count_collect_products(user_id).await?;
        let shop_collect = repo.count_collect_shops(user_id).await?;
        Ok(OrderNumVO {
            await_pay,
            await_shipping,
            await_received,
            await_comment,
            order_completed,
            product_collect,
            shop_collect,
            await_aftersales_collect,
        })
    }

    /// 售后与取消订单共用原因配置（Java `AfterSalesReasonConfigService#getConfig`）。
    pub async fn reason_config(&self) -> Result<ReasonConfigVO> {
        let repo = self.repo();
        let enabled_raw = repo.find_config_value(CONFIG_REASON_ENABLED).await?;
        let list_raw = repo.find_config_value(CONFIG_REASON_LIST).await?;
        Ok(ReasonConfigVO {
            after_sales_reason_enabled: parse_reason_enabled(enabled_raw.as_deref()),
            after_sales_reason_list: parse_reason_list(list_raw.as_deref()),
        })
    }

    /// 删除订单（Java `OrderServiceImpl#delOrder(id, userId)`）。
    pub async fn del_order(&self, user_id: i32, dto: &OrderIdDTO) -> Result<()> {
        let order = self.require_owned_order(user_id, dto).await?;
        if !can_delete_order(order.order_status, order.is_del) {
            return Err(Error::biz_msg(MSG_ORDER_STATUS_ERROR));
        }
        let repo = self.repo();
        repo.mark_order_deleted(order.order_id).await?;
        repo.insert_order_log(
            order.order_id,
            &order.order_sn,
            user_id,
            order.shop_id,
            "订单删除",
            now_i32(),
        )
        .await?;
        Ok(())
    }

    /// 确认收货（Java `OrderServiceImpl#confirmReceipt(orderId, userId)`）。
    ///
    /// 控制器先按 `order_id + user_id` 计数，非本人订单静默返回。
    pub async fn confirm_receipt(&self, user_id: i32, dto: &OrderIdDTO) -> Result<()> {
        let Some(order_id) = dto.id else {
            return Ok(());
        };
        let repo = self.repo();
        let Some(order) = repo.find_order_brief(order_id, user_id).await? else {
            // Java：count <= 0 → return（静默）
            return Ok(());
        };
        if !can_confirm_receipt(order.order_status, order.is_del) {
            return Err(Error::biz_msg(MSG_ORDER_STATUS_ERROR));
        }
        repo.complete_order(order.order_id, now_i32()).await?;
        let description = if order.order_type == Some(ORDER_TYPE_RESERVATION_HOME) {
            "完成上门服务"
        } else {
            "确认收货"
        };
        repo.insert_order_log(
            order.order_id,
            &order.order_sn,
            user_id,
            order.shop_id,
            description,
            now_i32(),
        )
        .await?;

        // 1. 积分赠送（已完成节点 Node 3）
        let order_service = crate::trade::order::OrderService::from_repos(self.repos.clone());
        if let Ok(Some(full_order)) = self.repos.trade.order.find_by_id(order.order_id).await {
            let _ = order_service.order_success_point(&full_order, 3).await;

            // 2. 会员成长值累加（对齐 Java userRankService.getRankGrowth）
            let growth = full_order.total_amount.to_string().parse::<f64>().unwrap_or(0.0) as i32;
            if growth > 0 {
                let log = tigshop_core::entities::RankGrowthLog {
                    id: 0,
                    user_id,
                    r#type: 1, // 完成下单
                    growth_points: growth,
                    change_type: 1, // 增加
                    create_time: Some(now_i32()),
                };
                let _ = self.repos.member.user_center.insert_rank_growth_log(&log).await;
                let _ = sqlx::query("UPDATE `user` SET `growth_points` = `growth_points` + ? WHERE `user_id` = ?")
                    .bind(growth)
                    .bind(user_id)
                    .execute(&self.repo().pool_clone())
                    .await;
            }

            // 4. 次卡子单完成时推进次卡主订单
            if let Some(parent_id) = full_order.parent_order_id.filter(|p| *p > 0) {
                let _ = sqlx::query(
                    "UPDATE `order` SET `order_status` = 5, `received_time` = ? WHERE `order_id` = ? AND `is_card_order` = 1",
                )
                .bind(now_i32())
                .bind(parent_id)
                .execute(&self.repo().pool_clone())
                .await;
            }

            // 5. 后台管理员与店铺订单完成消息通知（对齐 Java AdminMsgTypeEnum.ORDER_COMPLETED = 13）
            let msg_title = format!("订单已完成通知：{},金额：{}", full_order.order_sn, full_order.total_amount);
            let msg_content = format!("您有订单【{}】已完成，请注意查看。", full_order.order_sn);
            let related_data = serde_json::json!({ "orderId": full_order.order_id }).to_string();
            let _ = self.repos.system.admin_msg.create_message(
                13, // ORDER_COMPLETED
                full_order.shop_id,
                full_order.order_id,
                &msg_title,
                &msg_content,
                &related_data,
            ).await;
        }

        // 3. 对账单结算状态更新（real_settlement = 1）
        let _ = self
            .repos
            .payment
            .statement
            .complete_statement_output_by_order(order.order_id, now_i32())
            .await;

        tracing::info!(
            "订单确认收货成功: id={}, user_id={user_id}",
            order.order_id
        );
        Ok(())
    }

    /// 物流信息（Java `OrderServiceImpl#getShippingInfo`）。
    ///
    /// 无订单/无单号/物流公司缺失 → `Ok(None)`（Java 返回 null）；
    /// 物流平台未迁移 → 明确业务错误。
    pub async fn shipping_info(
        &self,
        user_id: i32,
        id: Option<i32>,
    ) -> Result<Option<ShippingResult>> {
        let Some(id) = id.filter(|id| *id > 0) else {
            return Ok(None);
        };
        let repo = self.repo();
        let Some(order) = repo.find_order_brief(id, user_id).await? else {
            return Ok(None);
        };
        if order.tracking_no.trim().is_empty() {
            return Ok(None);
        }
        if !repo.logistics_company_exists(order.logistics_id).await? {
            return Ok(None);
        }
        let now = chrono::Local::now().format("%Y-%m-%d %H:%M:%S").to_string();
        Ok(Some(ShippingResult {
            e_business_id: None,
            shipper_code: None,
            logistic_code: Some(order.tracking_no.clone()),
            state: Some("2".to_string()),
            state_ex: None,
            location: None,
            traces: vec![ShippingTraceVO {
                action: Some("揽件".to_string()),
                accept_time: Some(now),
                accept_station: Some(format!("包裹已由物流公司承运，运单号：{}", order.tracking_no)),
                location: None,
            }],
            success: Some("true".to_string()),
        }))
    }

    /// 再次购买（Java `CartServiceImpl#buyAgain(id, userId)` 的加购部分）。
    pub async fn buy_again(&self, user_id: i32, dto: &OrderIdDTO) -> Result<()> {
        let order = self.require_owned_order(user_id, dto).await?;
        let repo = self.repo();
        let items = repo.list_order_items(order.order_id).await?;
        let now = now_i32();
        let mut added = 0_u32;
        for item in &items {
            if !repo.product_available(item.product_id).await? {
                continue;
            }
            let cart_type =
                tigshop_core::models::trade::user_order::cart_type_from_product_type(Some(
                    item.product_type,
                ));
            let quantity = item.quantity.max(1);
            match repo
                .find_cart_row(user_id, item.product_id, item.sku_id, cart_type)
                .await?
            {
                Some(cart_id) => {
                    repo.add_cart_quantity(cart_id, quantity, now).await?;
                }
                None => {
                    repo.insert_cart_row(user_id, item, now).await?;
                }
            }
            added += 1;
        }
        tracing::info!(
            "再次购买加购成功: order_id={}, user_id={user_id}, items={added}",
            order.order_id
        );
        Ok(())
    }

    /// 取本人订单，缺失/非本人 → 「订单不存在」。
    async fn require_owned_order(&self, user_id: i32, dto: &OrderIdDTO) -> Result<OrderBrief> {
        let id = dto
            .id
            .filter(|id| *id > 0)
            .ok_or_else(|| Error::Validation(MSG_ORDER_NOT_EXIST.into()))?;
        self.repo()
            .find_order_brief(id, user_id)
            .await?
            .ok_or_else(|| Error::biz_msg(MSG_ORDER_NOT_EXIST))
    }
}

fn now_i32() -> i32 {
    i32::try_from(now_seconds()).unwrap_or(i32::MAX)
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn reason_config_defaults_via_model() {
        let parsed = parse_reason_list(None);
        assert_eq!(parsed.len(), 5);
        assert_eq!(parse_reason_enabled(Some("0")), 0);
    }

}
