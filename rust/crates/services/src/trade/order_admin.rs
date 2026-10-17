//! 订单管理服务（trade::order_admin）：订单列表、订单详情、发货、后台取消订单。
//! 对应原 Java `OrderServiceImpl`（6289 行）中的
//! `list` / `detail` / `deliverOrder` / `cancelOrderByAdmin` 四条链路，
//! 以及 `OrderController` 中对应端点。
//!
//! 迁移要点（逐条对照 Java 源）：
//! - `list`：`buildQueryWrapper` 的全部筛选条件在仓储层按同序绑定；
//!   列表所需关联数据（明细 / 用户 / 店铺 / 在售商品 / 整单售后 / 拼团记录）
//!   全部**批量预取**，与原 Java 的 N+1 消除改造一致。
//! - `detail`：失效父单（`is_valid = 0` 且无子单）返回 `5004 订单已被拆分为多个订单`；
//!   后台状态头文案覆盖仅在「状态名为标准文案」时生效（对齐 Java 的字符串比较）。
//! - `deliver`：状态门（仅「已确认，待发货」可发货）+ 整单售后拦截 +
//!   发货数量校验 + **原子条件更新**（`WHERE order_status = 1`，防并发重复发货）。
//! - `cancelOrderByAdmin`：仅「已确认，待发货」可取消，按整单生成
//!   `aftersales`（类型=仅退款，状态=审核处理中）+ 明细 + 日志，单事务写入。
//!
//! **未迁移（本切片明确不做，已在 `uncertain_points` 中列明）**：
//! - 部分发货拆单（Java `createSplitOrder` / 售后迁移 / 税费尾差补齐 / 分销订单拆分）
//! - 电子面单（`shippingMethod = 4`，依赖快递服务商路由）
//! - 售后自动审核与退款执行（依赖配置项 `afterSalesReview` 与退款域）
//! - 站内消息、赠积分、微信小程序发货信息录入、自动确认收货延时消息
//! - `product_comment_enabled` / `after_sales_limit_days` / `auto_delivery_days`
//!   / `auto_cancel_order_minute` 等配置驱动的行为（Rust 侧尚无配置服务）

use std::collections::{HashMap, HashSet};
use std::sync::Arc;

use rust_decimal::Decimal;
use tigshop_common::utils;
use tigshop_core::entities::{Aftersales, AftersalesItem, AftersalesLog, Order, OrderItem, Shop, User};
use tigshop_core::models::trade::order_admin::{
    aftersales_status, aftersales_type, join_status, order_type, pay_type, shipping_method,
    DeliverOrderParam, OrderActionVO, OrderItemVO, OrderListQuery, OrderShopVO, OrderStatus,
    OrderUserVO, OrderVO, PayStatus, ShippingStatus, AftersalesApplyAdminParam,
};
use tigshop_core::types::PageResult;
use tigshop_core::{Error, Result};
use tigshop_infrastructure::db::Repositories;

/// 待支付订单支付倒计时兜底分钟数（Java `AUTO_CANCEL_ORDER_MINUTE` 未配置时同为 30）。
const DEFAULT_CANCEL_MINUTE: i32 = 30;

/// 订单类型「不支持售后」的取值（Java `OrderType.getNoAfterSalesType`：7 付费商品、8 卡密）。
fn is_no_aftersales_type(order_type: Option<i32>) -> bool {
    matches!(order_type, Some(7 | 8))
}

/// 预约订单（Java `OrderType.isReservationOrder`：9 预约到店、10 预约上门）。
fn is_reservation_order(order_type: Option<i32>) -> bool {
    matches!(order_type, Some(order_type::RESERVATION_STORE | order_type::RESERVATION_HOME))
}

/// 同城配送（Java `OrderDeliveryTypeEnum.SAME_CITY`，`order.shipping_type_id == 3`）。
fn is_same_city(shipping_type_id: i32) -> bool {
    shipping_type_id == 3
}

/// 门店状态文案（Java `ShopType.getTypeName`：1 开业 4 停业 10 打烊）。
fn shop_status_text(status: Option<i32>) -> String {
    match status {
        Some(1) => "开业".into(),
        Some(4) => "停业".into(),
        Some(10) => "打烊".into(),
        _ => String::new(),
    }
}

/// 秒级时间戳 → `yyyy-MM-dd HH:mm:ss`（Java `TigUtils.handelTime`：0 或负数返回空串）。
fn fmt_time(secs: i32) -> String {
    if secs <= 0 {
        return String::new();
    }
    use chrono::TimeZone;
    match chrono::Local.timestamp_opt(i64::from(secs), 0) {
        chrono::LocalResult::Single(dt) => dt.format("%Y-%m-%d %H:%M:%S").to_string(),
        _ => String::new(),
    }
}

/// JSON 数组字符串 → `Vec<i32>`（对应 Java `JsonUtil.jsonToList`，非法时返回空表）。
fn parse_i32_list(raw: &str) -> Vec<i32> {
    serde_json::from_str::<Vec<i32>>(raw).unwrap_or_default()
}

/// JSON 数组字符串 → `Vec<String>`。
fn parse_string_list(raw: &str) -> Vec<String> {
    serde_json::from_str::<Vec<String>>(raw).unwrap_or_default()
}

/// 解析 JSON 对象字符串（非法返回 None）。
fn parse_json(raw: &str) -> Option<serde_json::Value> {
    serde_json::from_str::<serde_json::Value>(raw).ok()
}

/// 解析收货人信息 JSON；`regionName` 为空且 `regionNames` 非空时按 Java 逻辑补全。
fn parse_address_data(raw: Option<&str>) -> Option<serde_json::Value> {
    let mut v = parse_json(raw?)?;
    if !v.is_object() {
        return Some(v);
    }
    let region_name_blank = v
        .get("regionName")
        .and_then(serde_json::Value::as_str)
        .is_none_or(|s| s.trim().is_empty());
    if region_name_blank {
        let joined = v
            .get("regionNames")
            .and_then(serde_json::Value::as_array)
            .map(|arr| {
                arr.iter()
                    .filter_map(serde_json::Value::as_str)
                    .collect::<Vec<_>>()
                    .join(" ")
            })
            .unwrap_or_default();
        if !joined.is_empty() {
            v["regionName"] = serde_json::Value::String(joined);
        }
    }
    Some(v)
}

/// 订单状态名（按订单类型/配送类型选择枚举，对齐 Java `list`/`OrderVO` 构造）。
fn order_status_name(order: &Order) -> String {
    let code = order.order_status;
    if is_reservation_order(order.order_type) {
        OrderStatus::reservation_name(code).to_string()
    } else if is_same_city(order.shipping_type_id) {
        OrderStatus::same_city_name(code).to_string()
    } else {
        OrderStatus::name_of(code)
    }
}

/// 发货计划：本次应发（含售后占用数量）与剩余数量。
#[derive(Debug, Clone, Default, PartialEq, Eq)]
pub struct DeliverPlan {
    /// `(item_id, 本次发货数量)`——已并入售后占用数量
    pub deliver: Vec<(i32, i32)>,
    /// `(item_id, 剩余数量)`
    pub remain: Vec<(i32, i32)>,
}

/// 计算发货/剩余明细。
///
/// 逐条对照 Java `DeliverOrderParam#getDeliverItems` / `#getRemainItems`：
/// - 未指定发货数量的明细：若存在有效售后占用则按占用数量发货，否则整条留作剩余；
/// - 指定发货数量的明细：必须 `0 < 数量 <= 购买数量`，否则「发货数量错误」；
///   未全额发货时，售后占用数量并入发货单，超出可发数量报
///   「发货数量超出可发数量，该商品有部分数量正在售后中」。
pub fn compute_deliver_plan(
    items: &[OrderItem],
    deliver_map: &HashMap<i32, i32>,
    aftersales: &HashMap<i32, i32>,
) -> Result<DeliverPlan> {
    let mut plan = DeliverPlan::default();
    for item in items {
        let qty = item.quantity;
        match deliver_map.get(&item.item_id).copied() {
            None => {
                match aftersales.get(&item.item_id).copied().filter(|n| *n > 0) {
                    None => plan.remain.push((item.item_id, qty)),
                    Some(occupied) => {
                        let remain = qty - occupied;
                        if remain > 0 {
                            plan.remain.push((item.item_id, remain));
                        }
                        plan.deliver.push((item.item_id, occupied));
                    }
                }
            }
            Some(deliver_qty) => {
                if deliver_qty <= 0 || deliver_qty > qty {
                    return Err(Error::biz_msg("发货数量错误"));
                }
                let occupied = aftersales.get(&item.item_id).copied().unwrap_or(0);
                if deliver_qty == qty {
                    plan.deliver.push((item.item_id, qty));
                } else {
                    let total = deliver_qty.saturating_add(occupied);
                    if total > qty {
                        return Err(Error::biz_msg(
                            "发货数量超出可发数量，该商品有部分数量正在售后中",
                        ));
                    }
                    plan.deliver.push((item.item_id, total));
                    let remain = qty - deliver_qty - occupied;
                    if remain > 0 {
                        plan.remain.push((item.item_id, remain));
                    }
                }
            }
        }
    }
    Ok(plan)
}

/// `build_vo` 的外部数据集合。
struct VoInput<'a> {
    order: &'a Order,
    items: &'a [OrderItem],
    user: Option<&'a User>,
    shop: Option<&'a Shop>,
    /// 是否整单售后进行中（`confirmReceipt` 拦截条件）
    full_aftersales: bool,
    /// 订单内仍有在售商品（`rebuy` 条件）
    rebuy_available: bool,
    /// 拼团记录 `(groupon_promotion_record_id, join_status)`
    groupon: Option<(i64, i32)>,
    /// Java `OrderVO.wayBill`：`list` 走默认构造（false），`detail` 走 `OrderVO(Order)`（true）
    way_bill: bool,
    now: i32,
}

/// 组装订单 VO（对齐 Java `OrderVO` 构造 + `list` 循环内的派生字段）。
fn build_vo(input: VoInput<'_>) -> OrderVO {
    let VoInput {
        order: o,
        items,
        user,
        shop,
        full_aftersales,
        rebuy_available,
        groupon,
        way_bill,
        now,
    } = input;

    let region_names = parse_string_list(&o.region_names);
    let user_address = if region_names.is_empty() {
        None
    } else {
        Some(format!("{} {}", region_names.join(" "), o.address))
    };

    let mut status_name = order_status_name(o);
    let mut join_status_code = None;
    let mut join_status_desc = None;
    let mut groupon_record_id = None;
    let mut deliver_blocked_by_groupon = false;

    if o.order_type == Some(order_type::PIN) {
        let cancelled = o.order_status == OrderStatus::Cancelled as i32;
        if cancelled {
            join_status_desc = Some("已取消".to_string());
        }
        if let Some((rec_id, js)) = groupon {
            groupon_record_id = Some(rec_id);
            join_status_code = Some(js);
            if !cancelled {
                join_status_desc = Some(join_status::description(js));
            }
            if o.order_status == OrderStatus::Confirmed as i32 && js == join_status::WAIT_GROUP {
                status_name = "已付款，待成团".to_string();
                deliver_blocked_by_groupon = true;
            }
            if js == join_status::FAIL {
                status_name = if cancelled {
                    "拼团失败".to_string()
                } else {
                    "拼团失败，待退款".to_string()
                };
            }
        }
    }

    // 待支付订单支付截止时间（Java：下单时间 + 自动取消分钟数，配置缺失时兜底 30）
    let pay_end_time = if o.order_status == OrderStatus::Pending as i32 && o.add_time > 0 {
        Some(fmt_time(
            o.add_time.saturating_add(DEFAULT_CANCEL_MINUTE * 60),
        ))
    } else {
        None
    };

    let mut actions = available_actions(o, full_aftersales, rebuy_available);
    if deliver_blocked_by_groupon {
        actions.deliver = false;
    }

    OrderVO {
        order_id: o.order_id,
        order_type: o.order_type,
        order_sn: o.order_sn.clone(),
        user_id: o.user_id,
        parent_order_id: o.parent_order_id,
        parent_order_sn: o.parent_order_sn.clone(),
        order_status: o.order_status,
        shipping_status: o.shipping_status,
        pay_status: o.pay_status,
        consignee: o.consignee.clone(),
        address: o.address.clone(),
        mobile: o.mobile.clone(),
        email: o.email.clone(),
        buyer_note: o.buyer_note.clone(),
        admin_note: o.admin_note.clone(),
        shipping_method: o.shipping_method,
        logistics_id: o.logistics_id,
        logistics_provider: o.logistics_provider,
        logistics_name: o.logistics_name.clone(),
        shipping_type_id: o.shipping_type_id,
        shipping_type_name: o.shipping_type_name.clone(),
        tracking_no: o.tracking_no.clone(),
        pay_type_id: o.pay_type_id,
        use_points: o.use_points,
        is_need_commisson: o.is_need_commisson,
        distribution_status: o.distribution_status,
        referrer_user_id: o.referrer_user_id,
        is_del: o.is_del,
        shop_id: o.shop_id,
        is_store_splited: o.is_store_splited,
        is_valid: o.is_valid,
        comment_status: o.comment_status,
        total_amount: o.total_amount,
        paid_amount: o.paid_amount,
        unpaid_amount: o.unpaid_amount,
        unrefund_amount: o.unrefund_amount,
        product_amount: o.product_amount,
        coupon_amount: o.coupon_amount,
        points_amount: o.points_amount,
        discount_amount: o.discount_amount,
        balance: o.balance,
        credit_amount: o.credit_amount,
        is_credit_order: o.is_credit_order,
        online_paid_amount: o.online_paid_amount,
        offline_paid_amount: o.offline_paid_amount,
        service_fee: o.service_fee,
        shipping_fee: o.shipping_fee,
        invoice_fee: o.invoice_fee,
        order_extension: o.order_extension.clone(),
        order_source: o.order_source.clone(),
        out_trade_no: o.out_trade_no.clone(),
        is_settlement: o.is_settlement,
        is_exchange_order: o.is_exchange_order,
        mark: o.mark,
        is_presale: o.is_presale,
        presale_type: o.presale_type,
        vendor_id: o.vendor_id,
        cancel_remark: o.cancel_remark.clone(),
        round_off_cents_amount: o.round_off_cents_amount,
        round_off_dime_amount: o.round_off_dime_amount,
        tax_rate: o.tax_rate.unwrap_or(Decimal::ZERO),
        tax_rate_amount: o.tax_rate_amount.unwrap_or(Decimal::ZERO),
        discount_change_price: o.discount_change_price,
        order_status_name: status_name,
        order_status_tip: None,
        pay_status_name: PayStatus::name_of(o.pay_status),
        shipping_status_name: ShippingStatus::name_of(o.shipping_status),
        add_time: fmt_time(o.add_time),
        pay_time: (o.pay_time > 0).then(|| fmt_time(o.pay_time)),
        shipping_time: (o.shipping_time > 0).then(|| fmt_time(o.shipping_time)),
        received_time: (o.received_time > 0).then(|| fmt_time(o.received_time)),
        region_ids: parse_i32_list(&o.region_ids),
        region_names,
        address_data: parse_address_data(o.address_data.as_deref()),
        invoice_data: o.invoice_data.as_deref().and_then(parse_json),
        user_address,
        server_time: Some(fmt_time(now)),
        pay_end_time,
        way_bill,
        pre_order_status: None,
        pre_order_status_desc: None,
        is_change_order_status: 0,
        to_aftersalses: actions.to_aftersales,
        join_status: join_status_code,
        join_status_desc,
        groupon_promotion_record_id: groupon_record_id,
        items: items.iter().map(to_item_vo).collect(),
        user: user.map(to_user_vo),
        shop: shop.map(to_shop_vo),
        shop_name: shop.and_then(|s| s.shop_title.clone()),
        vendor_name: String::new(),
        available_actions: actions,
        host: None,
        print_time: None,
        sub_order_ids: None,
        pay_code: None,
    }
}

/// 订单明细实体 → VO。
fn to_item_vo(it: &OrderItem) -> OrderItemVO {
    OrderItemVO {
        item_id: it.item_id,
        order_id: it.order_id,
        order_sn: it.order_sn.clone(),
        user_id: it.user_id,
        product_id: it.product_id,
        product_name: it.product_name.clone(),
        product_sn: it.product_sn.clone(),
        pic_thumb: it.pic_thumb.clone(),
        sku_id: it.sku_id,
        sku_data: it.sku_data.clone(),
        price: it.price,
        origin_price: it.origin_price,
        quantity: it.quantity,
        delivery_quantity: it.delivery_quantity,
        product_type: it.product_type,
        is_gift: it.is_gift,
        shop_id: it.shop_id,
        suppliers_id: it.suppliers_id,
        vendor_id: it.vendor_id,
        card_group_name: it.card_group_name.clone(),
        promotion_data: it.promotion_data.clone(),
        sku_value: None,
        product_weight: None,
        commission: it.commission.clone(),
    }
}

/// 用户实体 → VO。
fn to_user_vo(u: &User) -> OrderUserVO {
    OrderUserVO {
        user_id: u.user_id,
        username: u.username.clone(),
        nickname: u.nickname.clone(),
        avatar: u.avatar.clone(),
        mobile: u.mobile.clone(),
        email: u.email.clone(),
    }
}

/// 店铺实体 → VO。
fn to_shop_vo(s: &Shop) -> OrderShopVO {
    OrderShopVO {
        shop_id: s.shop_id,
        shop_title: s.shop_title.clone().unwrap_or_default(),
        status_text: shop_status_text(s.status),
        kefu_inlet: s.kefu_inlet.clone(),
        shop_logo: s.shop_logo.clone(),
    }
}

/// 可操作项（逐条对照 Java `OrderServiceImpl#availableActions`）。
///
/// 依赖未迁移服务/配置的项取安全默认值（`to_aftersales` 恒 false：
/// Java 由 `orderAfterSalesService.getOrderAfterSalesToAction` 判定）；
/// `to_comment` 依赖配置项 `product_comment_enabled`，同样恒 false。
fn available_actions(o: &Order, full_aftersales: bool, rebuy_available: bool) -> OrderActionVO {
    let mut a = OrderActionVO::default();
    match OrderStatus::of(o.order_status) {
        Some(OrderStatus::Pending) => {
            a.set_confirm = true;
            a.cancel_order = true;
            a.modify_order = true;
            a.modify_shipping_info = true;
            a.modify_order_money = true;
            a.modify_order_product = true;
            a.set_paid = true;
            a.modify_order_consignee = true;
            a.to_pay = true;
            // Java：后台用户（`SecurityUtils.isAdminUser()` 即 adminId > 0，本切片
            // 四条端点全在管理端）不允许对「非线下支付」订单执行「设置已支付」
            if o.pay_type_id != pay_type::OFFLINE {
                a.set_paid = false;
            }
        }
        Some(OrderStatus::Confirmed) => {
            a.split_order = true;
            a.deliver = true;
            a.modify_order_consignee = true;
            if o.pay_status != PayStatus::Paid as i32 {
                a.set_paid = true;
                a.cancel_order = true;
            }
        }
        Some(OrderStatus::Processing) => {
            // Java 另有「自提订单不展示确认收货」判断（`OrderVO.isPickup` 由 O2O 域写入，未迁移）
            if !full_aftersales {
                a.confirm_receipt = true;
            }
            a.modify_shipping_info = true;
        }
        Some(OrderStatus::Completed) => {
            a.rebuy = true;
        }
        Some(OrderStatus::Cancelled) => {
            a.cancel_order = false;
            a.rebuy = true;
            a.del_order = true;
        }
        Some(OrderStatus::Invalid) => {
            a.del_order = true;
        }
        None => {}
    }

    if o.pay_status == PayStatus::Paid as i32 {
        a.set_unpaid = false;
    }

    // 未支付拆单子单：只读，禁止所有操作
    let is_unpaid_split_child =
        o.parent_order_id.is_some_and(|p| p > 0) && o.pay_status == PayStatus::Unpaid as i32;
    if is_unpaid_split_child {
        return OrderActionVO::default();
    }

    // 互斥的 kill-switch（Java `OrderServiceImpl:1082-1086` 逐字对照）：
    //   if (isStoreSplited == 1) setSplitOrder(false); else setDeliver(false);
    // 即已是店铺拆单订单时不再提供「拆单」入口；尚未拆单时反而**关闭发货**入口
    // （必须先拆单才能发货）。两者永远不可能同时为 true。
    if o.is_store_splited == 1 {
        a.split_order = false;
    } else {
        a.deliver = false;
    }
    if o.is_del == 1 {
        a.del_order = false;
    }
    if o.order_type.is_some_and(|t| t > 1) {
        a.rebuy = false;
        if is_no_aftersales_type(o.order_type) {
            a.to_aftersales = false;
        }
    }
    if a.rebuy && !rebuy_available {
        a.rebuy = false;
    }
    a
}

pub struct OrderAdminService {
    repos: Arc<Repositories>,
}

impl OrderAdminService {
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    // ————————————————————— 列表 —————————————————————

    /// 订单分页（对应 Java `OrderController.list` / `OrderServiceImpl#list`）。
    ///
    /// `platform_view`：Java 用请求头 `x-shop-id` / `x-vendor-id` 判定
    /// 「平台 / 店铺（门店、供应商）视角」；Rust 侧 JWT 尚未签发 shop_id，
    /// 改由路由层根据 JWT 的 `admin_type` 传入（`admin` 或缺失 → 平台视角）。
    pub async fn list(
        &self,
        q: &OrderListQuery,
        platform_view: bool,
    ) -> Result<PageResult<OrderVO>> {
        // 会员标签筛选依赖 `user_member_tag` 域（未迁移）：显式报错优于静默忽略
        if q.member_tag_ids.as_ref().is_some_and(|v| !v.is_empty()) {
            return Err(Error::Validation(
                "暂不支持按会员标签筛选订单（会员标签服务未迁移）".into(),
            ));
        }

        let page = q.page_query();
        let (total, orders) = self.repos.trade.order_admin.paginate_orders(q, platform_view).await?;
        if orders.is_empty() {
            return Ok(PageResult::empty(&page));
        }

        let order_ids: Vec<i32> = orders.iter().map(|o| o.order_id).collect();
        let items = self
            .repos.trade.order_admin
            .find_items_by_order_ids(&order_ids)
            .await?;
        let mut items_by_order: HashMap<i32, Vec<OrderItem>> = HashMap::new();
        for it in items {
            items_by_order.entry(it.order_id).or_default().push(it);
        }

        let user_ids = distinct_i32(orders.iter().map(|o| o.user_id));
        let users = self.repos.trade.order_admin.find_users(&user_ids).await?;
        let user_map: HashMap<i32, User> = users.into_iter().map(|u| (u.user_id, u)).collect();

        let shop_ids = distinct_i32(orders.iter().map(|o| o.shop_id).filter(|v| *v > 0));
        let shops = self.repos.trade.order_admin.find_shops(&shop_ids).await?;
        let shop_map: HashMap<i32, Shop> = shops.into_iter().map(|s| (s.shop_id, s)).collect();

        let covered: HashSet<i32> = self
            .repos.trade.order_admin
            .fully_covered_order_ids(&order_ids, &aftersales_status::valid_statuses())
            .await?
            .into_iter()
            .collect();

        let product_ids = distinct_i32(
            orders
                .iter()
                .flat_map(|o| items_by_order.get(&o.order_id).map(Vec::as_slice).unwrap_or(&[]))
                .map(|it| it.product_id),
        );
        let on_sale: HashSet<i32> = self
            .repos.trade.order_admin
            .on_sale_product_ids(&product_ids)
            .await?
            .into_iter()
            .collect();

        let groupon: HashMap<i32, (i64, i32)> = self
            .repos.trade.order_admin
            .find_groupon_records(&order_ids)
            .await?
            .into_iter()
            .map(|(order_id, rec_id, js)| (order_id, (rec_id, js)))
            .collect();

        let now = utils::now_seconds() as i32;
        let mut list = Vec::with_capacity(orders.len());
        for o in &orders {
            let empty: Vec<OrderItem> = Vec::new();
            let order_items = items_by_order.get(&o.order_id).unwrap_or(&empty);
            let rebuy_available = order_items.is_empty()
                || order_items.iter().any(|it| on_sale.contains(&it.product_id));
            list.push(build_vo(VoInput {
                order: o,
                items: order_items,
                user: user_map.get(&o.user_id),
                shop: (o.shop_id > 0)
                    .then(|| shop_map.get(&o.shop_id))
                    .flatten(),
                full_aftersales: covered.contains(&o.order_id),
                rebuy_available,
                groupon: groupon.get(&o.order_id).copied(),
                way_bill: false,
                now,
            }));
        }
        Ok(PageResult::new(total, list, &page))
    }

    // ————————————————————— 详情 —————————————————————

    /// 订单详情（对应 Java `OrderController.detail` / `OrderServiceImpl#detail(id)`）。
    pub async fn detail(&self, order_id: i32) -> Result<OrderVO> {
        let order = self
            .repos.trade.order_admin
            .find_order(order_id)
            .await?
            .ok_or_else(|| Error::biz_msg("订单不存在"))?;

        // 失效父单（已拆单）且找不到子单 → Java 抛 5004「订单已被拆分为多个订单」
        if order.is_valid == 0 && self.repos.trade.order_admin.count_children(order_id).await? == 0 {
            return Err(Error::biz(5004, "订单已被拆分为多个订单"));
        }

        let items = self.repos.trade.order_admin.find_items(order_id).await?;
        let users = self.repos.trade.order_admin.find_users(&[order.user_id]).await?;
        let shops = if order.shop_id > 0 {
            self.repos.trade.order_admin.find_shops(&[order.shop_id]).await?
        } else {
            Vec::new()
        };
        let total_qty = self.repos.trade.order_admin.order_items_total_quantity(order_id).await?;
        let covered_qty = self
            .repos.trade.order_admin
            .aftersales_covered_quantity(order_id, &aftersales_status::valid_statuses())
            .await?;
        let product_ids = distinct_i32(items.iter().map(|it| it.product_id));
        let on_sale: HashSet<i32> = self
            .repos.trade.order_admin
            .on_sale_product_ids(&product_ids)
            .await?
            .into_iter()
            .collect();
        let groupon = self
            .repos.trade.order_admin
            .find_groupon_records(&[order_id])
            .await?
            .into_iter()
            .next()
            .map(|(_, rec_id, js)| (rec_id, js));

        let rebuy_available =
            items.is_empty() || items.iter().any(|it| on_sale.contains(&it.product_id));
        let mut vo = build_vo(VoInput {
            order: &order,
            items: &items,
            user: users.first(),
            shop: shops.first(),
            full_aftersales: total_qty > 0 && covered_qty >= total_qty,
            rebuy_available,
            groupon,
            way_bill: true,
            now: utils::now_seconds() as i32,
        });

        // 后台状态头文案覆盖：仅当当前状态名仍为标准文案时覆盖（Java 用字符串相等判断）
        if let Some(admin) = OrderStatus::of(order.order_status)
            .and_then(OrderStatus::admin_name)
            .filter(|_| vo.order_status_name == OrderStatus::name_of(order.order_status))
        {
            vo.order_status_name = admin.to_string();
        }
        vo.order_status_tip = OrderStatus::of(order.order_status).map(|s| s.tip().to_string());
        Ok(vo)
    }

    // ————————————————————— 发货 —————————————————————

    /// 发货（对应 Java `OrderController.deliver` / `OrderServiceImpl#deliverOrder`）。
    ///
    /// 仅支持**整单发货**：部分发货在 Java 中会拆分订单（`createSplitOrder`），
    /// 该流程未迁移，故显式拒绝而不是落一半数据。
    pub async fn deliver(&self, param: &DeliverOrderParam) -> Result<()> {
        let order_id = param
            .id
            .ok_or_else(|| Error::Validation("订单 ID不能为空".into()))?;
        let order = self
            .repos.trade.order_admin
            .find_order(order_id)
            .await?
            .ok_or_else(|| Error::biz_msg("未知的订单"))?;

        // 状态门：等价 Java `Assert.isTrue(orderVO.getAvailableActions().getDeliver(), "状态错误")`
        // —— 已确认待发货 + 已按店铺拆分（支付成功时自动拆分）+ 非未支付拆单子单。
        // 整单售后标记只影响 confirmReceipt，不影响 deliver，故此处传 false。
        if !available_actions(&order, false, true).deliver {
            return Err(Error::biz_msg("状态错误"));
        }
        // 拼团待成团不允许发货（Java `detail` 中把 availableActions.deliver 置 false）
        let groupon = if order.order_type == Some(order_type::PIN) {
            self.repos.trade.order_admin
                .find_groupon_records(&[order_id])
                .await?
                .into_iter()
                .next()
        } else {
            None
        };
        if groupon.is_some_and(|(_, _, js)| js == join_status::WAIT_GROUP) {
            return Err(Error::biz_msg("状态错误"));
        }

        // 整单售后进行中禁止发货（Java `hasInProgressFullOrderAftersales`）
        let total_qty = self.repos.trade.order_admin.order_items_total_quantity(order_id).await?;
        if total_qty > 0 {
            let covered_qty = self
                .repos.trade.order_admin
                .aftersales_covered_quantity(order_id, &aftersales_status::valid_statuses())
                .await?;
            if covered_qty >= total_qty {
                return Err(Error::biz_msg("该订单存在售后，无法发货"));
            }
        }

        if param.deliver_data.as_ref().is_none_or(Vec::is_empty) {
            return Err(Error::Validation("请填写发货数量".into()));
        }

        let items = self.repos.trade.order_admin.find_items(order_id).await?;
        let occupied = self
            .repos.trade.order_admin
            .aftersales_quantity_by_item(order_id, &aftersales_status::valid_statuses())
            .await?;
        let plan = compute_deliver_plan(&items, &param.deliver_quantity_map(), &occupied)?;
        if plan.deliver.is_empty() {
            return Err(Error::Validation("请填写发货数量".into()));
        }
        if !plan.remain.is_empty() {
            return Err(Error::biz_msg(
                "部分发货需要拆分订单，Rust 侧拆单流程未迁移，请按整单发货",
            ));
        }

        let method = param.shipping_method.unwrap_or(shipping_method::LOGISTICS);
        let (logistics_id, logistics_provider, logistics_name, tracking_no) = match method {
            shipping_method::LOGISTICS => {
                let lid = param
                    .logistics_id
                    .filter(|v| *v > 0)
                    .ok_or_else(|| Error::biz_msg("物流公司不能为空"))?;
                let name = self
                    .repos.trade.order_admin
                    .find_company_name(lid)
                    .await?
                    .unwrap_or_default();
                let tn = param
                    .tracking_no
                    .as_deref()
                    .map(str::trim)
                    .filter(|s| !s.is_empty())
                    .ok_or_else(|| Error::biz_msg("物流快递单号不能为空"))?
                    .to_string();
                // 物流服务商由 Java `logisticsProviderRouter.getActive()`（配置驱动）决定，
                // 该配置未迁移：保留订单既有 provider，避免写入臆测值
                (lid, order.logistics_provider, name, tn)
            }
            shipping_method::MERCHANT => (0, 0, "商家配送".to_string(), String::new()),
            shipping_method::NONE => (0, 0, "无需配送".to_string(), String::new()),
            shipping_method::WAYBILL => {
                return Err(Error::biz_msg(
                    "电子面单发货依赖快递服务商路由，Rust 侧未迁移，请改用物流/商家配送",
                ))
            }
            other => {
                return Err(Error::Validation(format!("无效的配送方式: {other}")));
            }
        };

        // 同城配送不改动发货状态（Java 对 SAME_CITY 不 setShippingStatus）
        let shipping_status = if is_same_city(order.shipping_type_id) {
            order.shipping_status
        } else {
            ShippingStatus::Sent as i32
        };
        let now = utils::now_seconds() as i32;
        let updated = self
            .repos.trade.order_admin
            .update_deliver(
                order_id,
                method,
                shipping_status,
                now,
                logistics_id,
                logistics_provider,
                &logistics_name,
                &tracking_no,
            )
            .await?;
        if !updated {
            return Err(Error::Conflict("订单状态已变更，请刷新后重试".into()));
        }
        tracing::info!(
            "订单发货成功: order_id={order_id}, method={method}, tracking_no={tracking_no}, items={}",
            plan.deliver.len()
        );
        Ok(())
    }

    // ————————————————————— 后台取消订单 —————————————————————

    /// 后台取消订单（对应 Java `OrderController.cancelOrderByAdmin` /
    /// `OrderServiceImpl#cancelOrderByAdmin`）。
    ///
    /// 语义与原 Java 一致：**不直接改订单状态**，而是按整单创建一张
    /// 「仅退款」售后申请（状态=审核处理中），由售后审核流程完成退款/关单。
    ///
    /// **未迁移**：Java 随后按配置项 `afterSalesReview` 决定是否自动审核通过
    /// （并触发退款、回滚库存等）。本实现停在「申请已创建」，需要人工审核。
    ///
    /// 返回新建的 `aftersale_id`。
    pub async fn cancel_order_by_admin(
        &self,
        param: &AftersalesApplyAdminParam,
        operator: Option<&str>,
    ) -> Result<i32> {
        let order_id = param
            .order_id
            .ok_or_else(|| Error::Validation("订单ID不能为空".into()))?;
        let order = self
            .repos.trade.order_admin
            .find_order(order_id)
            .await?
            .ok_or_else(|| Error::biz_msg("订单不存在"))?;

        // Java：订单必须处于「已确认，待发货」，否则「已发货订单不能取消」
        if order.order_status != OrderStatus::Confirmed as i32 {
            return Err(Error::biz_msg("已发货订单不能取消"));
        }
        // Java `afterSalesApply` 的前置校验
        if order.pay_status != PayStatus::Paid as i32 {
            return Err(Error::biz_msg("未支付订单不允许售后"));
        }
        if order.order_source == "pos" {
            return Err(Error::biz_msg("收银台订单无法售后"));
        }

        let items = self.repos.trade.order_admin.find_items(order_id).await?;
        if items.is_empty() {
            return Err(Error::biz_msg("请填写申请售后数量"));
        }
        if items.iter().any(|it| it.gift_card_id.is_some_and(|v| v > 0)) {
            return Err(Error::biz_msg("电子礼品卡不支持申请售后"));
        }

        // 数量校验（Java `validateOrderQuantity`，isAdminCancel=true 文案）
        let occupied = self
            .repos.trade.order_admin
            .aftersales_quantity_by_item(order_id, &aftersales_status::valid_statuses())
            .await?;
        for it in &items {
            if self
                .repos.trade.order_admin
                .has_applied_aftersales(order_id, it.item_id, &aftersales_status::applied_statuses())
                .await?
            {
                return Err(Error::biz_msg("订单已申请售后，无法取消，请先处理售后"));
            }
            let used = occupied.get(&it.item_id).copied().unwrap_or(0);
            // Java `validateOrderQuantity`：申请数量 + 已占用数量 <= 购买数量；
            // 后台取消订单按整单申请（申请数量 = 购买数量），故存在任何有效占用即不足
            if used > 0 {
                return Err(Error::biz_msg("该商品可申请数量不足"));
            }
        }

        let now = utils::now_seconds() as i32;
        let description = param
            .description
            .as_deref()
            .map(str::trim)
            .filter(|s| !s.is_empty())
            .unwrap_or("商家取消订单")
            .to_string();
        let pics = param
            .pics
            .as_ref()
            .map(|v| serde_json::to_string(v).unwrap_or_default())
            .unwrap_or_default();

        let aftersales = Aftersales {
            aftersale_id: 0,
            aftersale_type: aftersales_type::PAY_RETURN,
            status: aftersales_status::IN_REVIEW,
            pics: pics.clone(),
            description: description.clone(),
            reply: String::new(),
            add_time: now,
            tracking_no: String::new(),
            logistics_name: String::new(),
            return_address: None,
            aftersale_reason: Some("其他".into()),
            aftersales_sn: Some(aftersales_sn(order_id, now)),
            order_id: Some(order.order_id),
            user_id: Some(order.user_id),
            refund_amount: param.refund_amount.unwrap_or(Decimal::ZERO),
            shop_id: order.shop_id,
            audit_time: 0,
            deal_time: 0,
            final_time: 0,
            return_goods_tip: String::new(),
            // Java：订单绑定供应商时带上供应商ID
            vendor_id: order.vendor_id.filter(|v| *v > 0),
            close_type: 0,
        };
        let af_items: Vec<AftersalesItem> = items
            .iter()
            .map(|it| AftersalesItem {
                aftersales_item_id: 0,
                order_item_id: Some(it.item_id),
                number: Some(it.quantity),
                aftersale_id: None,
            })
            .collect();
        let log = AftersalesLog {
            log_id: 0,
            aftersale_id: 0,
            log_info: format!("会员发起了仅退款: {description}"),
            add_time: now,
            admin_name: String::new(),
            refund_money: Decimal::ZERO,
            refund_type: 0,
            refund_desc: String::new(),
            // Java：`SecurityUtils.getCurrentUsername()`（当前登录管理员）
            user_name: operator.map(ToString::to_string),
            return_pic: (!pics.is_empty()).then_some(pics),
            shop_id: Some(order.shop_id),
            vendor_id: order.vendor_id.filter(|v| *v > 0),
        };

        let aftersale_id = self
            .repos.trade.order_admin
            .insert_aftersales_bundle(&aftersales, &af_items, &log)
            .await?;
        tracing::info!(
            "后台取消订单已创建售后申请: order_id={order_id}, aftersale_id={aftersale_id}, items={}",
            af_items.len()
        );
        Ok(aftersale_id)
    }
}

/// 售后单号：`yyyyMMddHHmmss` + 5 位数字（Java
/// `LocalDateTime.now().format("yyyyMMddHHmmss") + RandomUtil.randomNumbers(5)`）。
///
/// Rust 侧未引入 `rand` 依赖，后缀由「毫秒时间戳 + 订单号」派生，保证同一订单
/// 重复申请时不会生成相同单号。
fn aftersales_sn(order_id: i32, now_secs: i32) -> String {
    use chrono::TimeZone;
    let ts = chrono::Local
        .timestamp_opt(i64::from(now_secs), 0)
        .single()
        .map(|dt| dt.format("%Y%m%d%H%M%S").to_string())
        .unwrap_or_default();
    let suffix = (utils::now_millis() + i64::from(order_id)).rem_euclid(100_000);
    format!("{ts}{suffix:05}")
}

/// 去重后的 i32 列表（保持首次出现顺序）。
fn distinct_i32<I: Iterator<Item = i32>>(it: I) -> Vec<i32> {
    let mut seen = HashSet::new();
    let mut out = Vec::new();
    for v in it {
        if seen.insert(v) {
            out.push(v);
        }
    }
    out
}

#[cfg(test)]
mod tests {
    use super::*;
    use std::str::FromStr;

    fn item(item_id: i32, quantity: i32) -> OrderItem {
        OrderItem {
            item_id,
            order_id: 1,
            quantity,
            ..Default::default()
        }
    }

    fn order(order_status: i32, pay_status: i32) -> Order {
        Order {
            order_id: 1,
            order_sn: "SN1".into(),
            user_id: 2,
            order_status,
            pay_status,
            shipping_status: 0,
            add_time: 1_700_000_000,
            is_valid: 1,
            shop_id: 0,
            order_type: Some(order_type::NORMAL),
            total_amount: Decimal::from_str("100.00").unwrap(),
            ..Default::default()
        }
    }

    // ————————————————— 发货数量计算 —————————————————

    #[test]
    fn deliver_plan_full_delivery_has_no_remain() {
        let items = vec![item(1, 2), item(2, 3)];
        let mut map = HashMap::new();
        map.insert(1, 2);
        map.insert(2, 3);
        let plan = compute_deliver_plan(&items, &map, &HashMap::new()).unwrap();
        assert_eq!(plan.deliver, vec![(1, 2), (2, 3)]);
        assert!(plan.remain.is_empty());
    }

    #[test]
    fn deliver_plan_partial_reports_remain() {
        let items = vec![item(1, 5)];
        let mut map = HashMap::new();
        map.insert(1, 2);
        let plan = compute_deliver_plan(&items, &map, &HashMap::new()).unwrap();
        assert_eq!(plan.deliver, vec![(1, 2)]);
        assert_eq!(plan.remain, vec![(1, 3)]);
    }

    #[test]
    fn deliver_plan_rejects_bad_quantity() {
        let items = vec![item(1, 2)];
        let mut map = HashMap::new();
        map.insert(1, 3); // 超过购买数量
        assert!(compute_deliver_plan(&items, &map, &HashMap::new()).is_err());
        map.insert(1, 0); // 非正数
        assert!(compute_deliver_plan(&items, &map, &HashMap::new()).is_err());
    }

    #[test]
    fn deliver_plan_merges_aftersales_quantity() {
        // 购买 5 件，售后占用 2 件，本次发 3 件 → 发货单并入售后 2 件 = 5，无剩余
        let items = vec![item(1, 5)];
        let mut map = HashMap::new();
        map.insert(1, 3);
        let mut af = HashMap::new();
        af.insert(1, 2);
        let plan = compute_deliver_plan(&items, &map, &af).unwrap();
        assert_eq!(plan.deliver, vec![(1, 5)]);
        assert!(plan.remain.is_empty());

        // 发 1 件 + 售后 2 件 = 3 < 5 → 剩余 2
        map.insert(1, 1);
        let plan = compute_deliver_plan(&items, &map, &af).unwrap();
        assert_eq!(plan.deliver, vec![(1, 3)]);
        assert_eq!(plan.remain, vec![(1, 2)]);
    }

    #[test]
    fn deliver_plan_rejects_quantity_over_available_with_aftersales() {
        let items = vec![item(1, 4)];
        let mut map = HashMap::new();
        map.insert(1, 3); // 3 + 售后2 = 5 > 4
        let mut af = HashMap::new();
        af.insert(1, 2);
        let err = compute_deliver_plan(&items, &map, &af).unwrap_err();
        assert!(err.to_string().contains("发货数量超出可发数量"));
    }

    #[test]
    fn deliver_plan_treats_aftersales_only_items_as_deliverable() {
        // 未指定发货数量但存在售后占用 → 按占用数量发货，剩余保留
        let items = vec![item(1, 4)];
        let mut af = HashMap::new();
        af.insert(1, 1);
        let plan = compute_deliver_plan(&items, &HashMap::new(), &af).unwrap();
        assert_eq!(plan.deliver, vec![(1, 1)]);
        assert_eq!(plan.remain, vec![(1, 3)]);
    }

    // ————————————————— 可操作项 —————————————————

    #[test]
    fn available_actions_follow_java_status_matrix() {
        // 待支付：可取消/改价/设置已支付
        let a = available_actions(&order(0, 0), false, true);
        assert!(a.cancel_order && a.set_confirm && a.to_pay && a.modify_order_money);
        assert!(!a.deliver && !a.confirm_receipt);
        // 后台用户对非线下支付订单不下发「设置已支付」入口（pay_type_id 默认 0）
        assert!(!a.set_paid);
        let mut offline = order(0, 0);
        offline.pay_type_id = pay_type::OFFLINE;
        assert!(available_actions(&offline, false, true).set_paid);

        // 已确认待发货 + 已支付：`deliver` 与 `split_order` **互斥**。
        // Java `OrderServiceImpl:1082-1086`：
        //   if (isStoreSplited == 1) setSplitOrder(false); else setDeliver(false);
        // 默认 `is_store_splited == 0` → 关闭发货入口、保留拆单入口。
        let confirmed = order(1, 2);
        let a = available_actions(&confirmed, false, true);
        assert!(a.split_order && !a.deliver);
        assert!(!a.cancel_order);
        assert!(!a.set_unpaid);

        // `is_store_splited == 1`（已拆单）：关闭拆单入口、保留发货入口
        let mut splited = order(1, 2);
        splited.is_store_splited = 1;
        let a = available_actions(&splited, false, true);
        assert!(a.deliver && !a.split_order);

        // 已确认待发货 + 未支付：可取消 + 可设置已支付（该分支不受线下支付限制），
        // 同样受 `is_store_splited` 互斥规则约束（默认 0 → 保留拆单、关闭发货）
        let unpaid = order(1, 0);
        let a = available_actions(&unpaid, false, true);
        assert!(a.split_order && !a.deliver && a.cancel_order && a.set_paid);

        // 处理中：可确认收货；整单售后进行中则不可
        let a = available_actions(&order(2, 2), false, true);
        assert!(a.confirm_receipt);
        let a = available_actions(&order(2, 2), true, true);
        assert!(!a.confirm_receipt);

        // 已完成：可再次购买（无在售商品时关闭）
        let a = available_actions(&order(5, 2), false, true);
        assert!(a.rebuy);
        let a = available_actions(&order(5, 2), false, false);
        assert!(!a.rebuy);

        // 已取消 / 无效
        let a = available_actions(&order(3, 0), false, true);
        assert!(!a.cancel_order && a.rebuy && a.del_order);
        let a = available_actions(&order(4, 0), false, true);
        assert!(a.del_order && !a.rebuy);
    }

    #[test]
    fn available_actions_respects_split_flags() {
        // Java `OrderServiceImpl:1082-1086` 是**互斥**的 kill-switch：
        //   if (isStoreSplited == 1) setSplitOrder(false); else setDeliver(false);
        // 因此 `is_store_splited == 1` → 关闭拆单入口，`deliver` 保留；
        // `is_store_splited == 0`  → 关闭发货入口，`split_order` 保留。
        // ⚠️ 早先本测试把两个分支写反了（与服务实现一致地反），此处按 Java 修正。
        let mut o = order(1, 2);
        o.is_store_splited = 1;
        let a = available_actions(&o, false, true);
        assert!(!a.split_order, "is_store_splited=1 必须关闭拆单入口");
        assert!(a.deliver, "is_store_splited=1 时发货入口应保留");

        // is_store_splited = 0 → 关闭发货，保留拆分
        let o = order(1, 2);
        let a = available_actions(&o, false, true);
        assert!(a.split_order, "is_store_splited=0 时拆单入口应保留");
        assert!(!a.deliver, "is_store_splited=0 必须关闭发货入口");

        // 未支付拆单子单：全只读
        let mut o = order(1, 0);
        o.parent_order_id = Some(9);
        let a = available_actions(&o, false, true);
        let json = serde_json::to_value(&a).unwrap();
        assert!(json.as_object().unwrap().values().all(|v| v == &serde_json::json!(false)));

        // 卡密订单（type 8）不再下发售后入口
        let mut o = order(5, 2);
        o.order_type = Some(8);
        let a = available_actions(&o, false, true);
        assert!(!a.to_aftersales && !a.rebuy);
    }

    // ————————————————— 派生字段 —————————————————

    #[test]
    fn status_name_switches_by_order_kind() {
        let mut o = order(2, 2);
        assert_eq!(order_status_name(&o), "处理中，已发货");

        o.order_type = Some(order_type::RESERVATION_STORE);
        assert_eq!(order_status_name(&o), "预约成功");

        o.order_type = Some(order_type::NORMAL);
        o.shipping_type_id = 3; // 同城配送
        assert_eq!(order_status_name(&o), "配送中");
    }

    #[test]
    fn fmt_time_handles_zero_and_positive() {
        assert_eq!(fmt_time(0), "");
        assert_eq!(fmt_time(-1), "");
        assert_eq!(fmt_time(1_700_000_000).len(), 19);
    }

    #[test]
    fn parse_lists_tolerate_bad_json() {
        assert_eq!(parse_i32_list("[1,2,3]"), vec![1, 2, 3]);
        assert!(parse_i32_list("").is_empty());
        assert!(parse_i32_list("not json").is_empty());
        assert_eq!(parse_string_list(r#"["A","B"]"#), vec!["A", "B"]);
        assert!(parse_json("").is_none());
    }

    #[test]
    fn address_data_fills_region_name() {
        let v = parse_address_data(Some(r#"{"regionNames":["浙江省","杭州市"],"regionName":""}"#))
            .unwrap();
        assert_eq!(v["regionName"], "浙江省 杭州市");

        // 已有 regionName 时不覆盖
        let v = parse_address_data(Some(r#"{"regionNames":["浙江省"],"regionName":"X"}"#)).unwrap();
        assert_eq!(v["regionName"], "X");

        assert!(parse_address_data(None).is_none());
    }

    #[test]
    fn shop_status_text_matches_shop_type() {
        assert_eq!(shop_status_text(Some(1)), "开业");
        assert_eq!(shop_status_text(Some(10)), "打烊");
        assert_eq!(shop_status_text(None), "");
    }

    #[test]
    fn aftersales_sn_is_unique_per_call() {
        let a = aftersales_sn(1, 1_700_000_000);
        let b = aftersales_sn(2, 1_700_000_000);
        assert_eq!(a.len(), 19); // 14 时间 + 5 数字
        assert_ne!(a, b);
    }

    #[test]
    fn build_vo_derives_user_address_and_pay_end_time() {
        let mut o = order(0, 0);
        o.region_names = r#"["浙江省","杭州市"]"#.into();
        o.address = "文一西路 1 号".into();
        o.mobile = "13812348000".into();
        let vo = build_vo(VoInput {
            order: &o,
            items: &[],
            user: None,
            shop: None,
            full_aftersales: false,
            rebuy_available: true,
            groupon: None,
            way_bill: true,
            now: 1_700_000_000,
        });
        assert_eq!(vo.user_address.as_deref(), Some("浙江省 杭州市 文一西路 1 号"));
        // 待支付：支付截止时间 = add_time + 30 分钟
        assert!(vo.pay_end_time.is_some());
        assert_eq!(vo.pay_status_name, "未支付");
        assert_eq!(vo.order_status_tip, None); // tip 仅详情接口下发
        assert!(vo.way_bill);
        assert!(vo.available_actions.to_pay);
    }

    #[test]
    fn build_vo_applies_groupon_overrides() {
        let mut o = order(1, 2);
        o.order_type = Some(order_type::PIN);
        // is_store_splited = 1 时发货入口默认保留，才能验证拼团把 deliver 关掉
        o.is_store_splited = 1;
        let vo = build_vo(VoInput {
            order: &o,
            items: &[],
            user: None,
            shop: None,
            full_aftersales: false,
            rebuy_available: true,
            groupon: Some((77, join_status::WAIT_GROUP)),
            way_bill: false,
            now: 1_700_000_000,
        });
        assert_eq!(vo.order_status_name, "已付款，待成团");
        assert_eq!(vo.groupon_promotion_record_id, Some(77));
        assert_eq!(vo.join_status, Some(join_status::WAIT_GROUP));
        assert_eq!(vo.join_status_desc.as_deref(), Some("待成团"));
        // 待成团不允许发货
        assert!(!vo.available_actions.deliver);
        assert!(!vo.way_bill);
    }

    #[test]
    fn user_and_shop_vo_mapping() {
        let u = User {
            user_id: 5,
            username: "u5".into(),
            nickname: Some("昵称".into()),
            avatar: "a.png".into(),
            mobile: "13800000000".into(),
            email: "u@example.com".into(),
            ..Default::default()
        };
        let vo = to_user_vo(&u);
        assert_eq!(vo.user_id, 5);
        assert_eq!(vo.nickname.as_deref(), Some("昵称"));

        let s = Shop {
            shop_id: 3,
            shop_title: Some("旗舰店".into()),
            status: Some(1),
            shop_logo: Some("l.png".into()),
            kefu_inlet: Some("kf".into()),
            ..Default::default()
        };
        let vo = to_shop_vo(&s);
        assert_eq!(vo.shop_title, "旗舰店");
        assert_eq!(vo.status_text, "开业");
    }
}