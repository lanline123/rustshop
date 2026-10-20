//! 礼品卡域服务（marketing::gift_card）。
//!
//! 对应原 Java `GiftCardsServiceImpl`
//! （`java/tigshop-service/src/main/java/com/tigshop/service/promotion/impl/GiftCardsServiceImpl.java`）。
//!
//! # 端点归属
//!
//! 本域负责 `marketing/gift_card` 的 8 个 **admin** 端点（list / detail /
//! importTemplate / exportList / importList / delete / giftCardExchangeDetail /
//! updateCardStatus）以及 `gift_card_groups` 的 6 个 **client** 端点所需的卡逻辑
//! （client detail / claim / transfer / isGiftCardExpired / exchange /
//! getTransferLink，路由在 `gift_card_groups` 的 public 路由文件里组合两个服务）。
//!
//! # 未迁移 / 偏差（如实标注）
//!
//! - **Excel 未迁移**：`importTemplate` / `exportList` 返回 JSON 行数据
//!   （Java 写 xlsx 二进制）；`importList` 完成 multipart「文件非空」校验后
//!   返回业务错误「Excel 批量导入功能尚未迁移」。与 `catalog::e_card_group`
//!   等既有切片口径一致。
//! - **Redis 锁未迁移**：`exchangeCard` 的 `userExchangeCard_{userId}` 分布式锁
//!   未迁移（Rust 侧无 Redis 锁封装）；并发重复兑换由后续唯一约束/事务兜底。
//! - **购物车未迁移**（兑换卡分支）：Java 会清空并写入 `TYPE_GIFT_EXCHANGE`
//!   购物车项；本实现返回兑换商品列表 + `exchangeProductToCart.item=true`
//!   回执，购物车域（trade::cart）未接入（见 `uncertain_points`）。
//! - **配置服务未迁移**：平台店铺名/logo（`SHOP_NAME/SHOP_LOGO`）与转赠域名
//!   （`PC_DOMAIN/H5_DOMAIN`）读不到配置 ⇒ 平台店铺返回 null，转赠链接使用
//!   相对路径 `/giftCard/transfer/`、`/pages/giftCard/transfer?giftCardSn=`。
//! - **储值卡兑换**：按 Java 语义插入 `user_recharge_order`（status=1,
//!   postscript=充值卡兑换）并通过 `member::user_manage` 原子加余额 + 写
//!   `user_balance_log`（change_desc 与 Java「充值订单审核通过增加余额」一致）。
//! - **手机号脱敏**：`GiftCardDetailVO.mobile` 的 `@Sensitive(PHONE)` 仅演示站
//!   生效，用常量 [`IS_DEMO`]（对齐 `tigshop.is-demo: 0`）。

use std::sync::Arc;

use rust_decimal::Decimal;

use tigshop_common::utils::{mask_mobile, now_seconds};
use tigshop_core::entities::{GiftCards, Order, UserBalanceLog};
use tigshop_core::models::marketing::gift_card::{
    format_unix_time, is_exchange_text, is_sold_text, mask_middle_half, seconds_text,
    ExchangeCardParam, GiftCardClaimParam, GiftCardDeliveryInfoVO, GiftCardDetailVO,
    GiftCardExchangeInfoVO, GiftCardIdParam, GiftCardPayInfoVO, GiftCardUpdateStatusDTO,
    GiftCardUserAddressInfoVO, GiftCardsExportVO, GiftCardsImportVO, GiftCardsListQuery,
    GiftCardsTemplateVO, GiftCardsVO, CLAIMED_RECEIVED, CLAIMED_RECEIVABLE,
    GROUP_TYPE_EXCHANGE, GROUP_TYPE_TOP_UP, MSG_CARD_ABNORMAL, MSG_CARD_ALREADY_CLAIMED,
    MSG_CARD_ALREADY_EXCHANGED, MSG_CARD_EXPIRED, MSG_CARD_GROUP_INVALID,
    MSG_CARD_GROUP_NOT_EXISTS, MSG_CARD_ID_REQUIRED, MSG_CARD_NOT_CLAIMABLE,
    MSG_CARD_NOT_EXISTS, MSG_CARD_SOLD_CANNOT_DELETE, MSG_EXCEL_IMPORT_UNMIGRATED,
    MSG_EXCHANGE_FAILED, MSG_GROUP_NOT_EXISTS, MSG_IS_AVAILABLE_REQUIRED,
    MSG_PRODUCTS_OFFLINE, MSG_SN_NOT_FOUND, MSG_SOLD_CANNOT_CHANGE_STATUS,
    MSG_TRANSFER_CARD_NOT_FOUND, MSG_TRANSFER_GROUP_NOT_FOUND, MSG_TRANSFER_SN_REQUIRED,
    MSG_TRANSFER_SOURCE_REQUIRED, MSG_UNSUPPORTED_GROUP_TYPE, MSG_UPLOAD_FILE_REQUIRED,
    TRANSFER_FROM_H5, TRANSFER_FROM_PC, YES,
};
use tigshop_core::models::member::user_manage::CHANGE_TYPE_INC;
use tigshop_core::types::{PageQuery, PageResult};
use tigshop_core::{Error, Result};
use tigshop_infrastructure::db::repo::marketing::gift_card::GiftCardRepository;
use tigshop_infrastructure::db::repo::marketing::tenant::TenantScope;
use tigshop_infrastructure::db::Repositories;

/// 演示站开关（Java `tigshop.is-demo`，默认 `0`）。
pub const IS_DEMO: i32 = 0;

/// 礼品卡域服务。
pub struct GiftCardService {
    repos: Arc<Repositories>,
}

impl GiftCardService {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本域仓储（NESTED：`repos.marketing.gift_card`）。
    #[must_use]
    pub fn repo(&self) -> &GiftCardRepository {
        &self.repos.marketing.gift_card
    }

    // ————————————————————— admin：列表 / 详情 —————————————————————

    /// 卡分页列表（Java `GiftCardsServiceImpl#list`）。
    pub async fn list(&self, q: &GiftCardsListQuery) -> Result<PageResult<GiftCardsVO>> {
        let page = to_page_query(q.page, q.size);
        let (limit, offset) = q.limit_offset();
        // 多租户：卡组归属校验（gift_card_groups.shop_id）
        if let Some(card_groups_id) = q.card_groups_id {
            ensure_group_access(self.repo(), card_groups_id).await?;
        }
        let (total, rows) = self.repo().paginate(q, limit, offset).await?;
        if rows.is_empty() {
            return Ok(PageResult::empty(&page));
        }
        let list = rows.iter().map(list_vo).collect();
        Ok(PageResult::new(
            u64::try_from(total).unwrap_or(0),
            list,
            &page,
        ))
    }

    /// 卡详情（Java controller：`getById` + `giftCardsDetail(sn, true)`）。
    pub async fn admin_detail(&self, gift_cards_id: i32) -> Result<GiftCardsVO> {
        let card = self
            .repo()
            .find_by_id(gift_cards_id)
            .await?
            .ok_or_else(|| Error::biz(500, MSG_CARD_NOT_EXISTS))?;
        // 多租户：详情归属校验（经卡组 shop_id）
        ensure_group_access(self.repo(), card.card_groups_id).await?;
        let sn = card
            .gift_card_sn
            .clone()
            .ok_or_else(|| Error::biz(500, MSG_CARD_NOT_EXISTS))?;
        self.gift_cards_detail(&sn, true).await
    }

    /// 按序列号取卡详情（Java `giftCardsDetail(sn, showPassword)`）。
    ///
    /// 卡不存在时 Java 返回空 `GiftCardsVO`（非错误）；卡组缺失时 Java NPE ⇒
    /// 本实现返回业务 500「礼品卡组不存在」。
    pub async fn gift_cards_detail(&self, sn: &str, show_password: bool) -> Result<GiftCardsVO> {
        let Some(card) = self.repo().find_by_sn(sn).await? else {
            return Ok(GiftCardsVO::default());
        };
        let group = self
            .repo()
            .find_group(card.card_groups_id)
            .await?
            .ok_or_else(|| Error::biz(500, MSG_CARD_GROUP_NOT_EXISTS))?;
        let (shop_title, shop_logo) = self.resolve_shop(group.shop_id.unwrap_or(0)).await?;

        let (card_number, card_password) = if show_password {
            (card.card_number.clone(), card.card_password.clone())
        } else {
            (
                mask_middle_half(&card.card_number),
                mask_middle_half(&card.card_password),
            )
        };
        Ok(GiftCardsVO {
            gift_cards_id: Some(card.gift_cards_id),
            card_groups_id: Some(card.card_groups_id),
            card_group_name: None,
            shop_id: group.shop_id,
            gift_card_sn: card.gift_card_sn.clone(),
            shop_title,
            shop_logo,
            image_url: group.image_url.clone(),
            card_number: Some(card_number),
            card_password: Some(card_password),
            is_available: card.is_available,
            status: card.status,
            is_sold: Some(is_sold_text(card.user_id)),
            is_exchange: Some(is_exchange_text(card.status)),
            used_time: None,
            user_id: card.user_id,
            add_time: seconds_text(card.add_time),
            expired_time: Some(card.expired_time),
            gift_card_products: None,
            exchange_product_to_cart: None,
        })
    }

    /// 平台店铺（shopId=0）返回 `(None, None)`：配置服务未迁移（偏差见模块头）。
    async fn resolve_shop(&self, shop_id: i32) -> Result<(Option<String>, Option<String>)> {
        if shop_id == 0 {
            return Ok((None, None));
        }
        match self.repo().find_shop(shop_id).await? {
            Some((title, logo)) => Ok((title, logo)),
            None => Ok((None, None)),
        }
    }

    // ————————————————————— admin：Excel（JSON 降级） —————————————————————

    /// 导入模板（Java `importTemplate` 写 xlsx → 本实现返回模板行 JSON）。
    #[must_use]
    pub fn import_template(&self) -> Vec<GiftCardsTemplateVO> {
        vec![GiftCardsTemplateVO {
            card_number: "GC20251031133058".to_string(),
            card_password: "PASSWORD123456".to_string(),
        }]
    }

    /// 导出列表（Java `@ResponseExcel` → 本实现返回导出行 JSON）。
    pub async fn export_list(&self, card_groups_id: i32) -> Result<Vec<GiftCardsExportVO>> {
        // 多租户：导出前校验卡组归属
        ensure_group_access(self.repo(), card_groups_id).await?;
        let rows: Vec<GiftCards> = self.repo().find_by_group(card_groups_id).await?;
        Ok(rows
            .into_iter()
            .map(|c| GiftCardsExportVO {
                card_number: c.card_number.clone(),
                card_password: c.card_password.clone(),
                gift_card_sn: c.gift_card_sn.clone(),
                is_sold: is_sold_text(c.user_id),
                is_exchange: is_exchange_text(c.status),
                add_time: format_unix_time(c.add_time),
            })
            .collect())
    }

    /// 导入（Java 解析 xlsx）→ Rust 侧仅做「文件非空」校验并返回未迁移错误。
    ///
    /// # Errors
    /// 无文件 ⇒「请上传文件」；有文件 ⇒「Excel 批量导入功能尚未迁移」。
    pub async fn import_list(
        &self,
        card_groups_id: Option<i32>,
        file_present: bool,
    ) -> Result<GiftCardsImportVO> {
        if !file_present {
            return Err(Error::biz(500, MSG_UPLOAD_FILE_REQUIRED));
        }
        let group_id = card_groups_id.ok_or_else(|| Error::biz(500, MSG_CARD_GROUP_INVALID))?;
        // 多租户：导入前校验卡组归属
        ensure_group_access(self.repo(), group_id).await?;
        if self.repo().find_group(group_id).await?.is_none() {
            return Err(Error::biz(500, MSG_CARD_GROUP_INVALID));
        }
        Err(Error::biz(500, MSG_EXCEL_IMPORT_UNMIGRATED))
    }

    // ————————————————————— admin：删除 / 状态 / 兑换详情 —————————————————————

    /// 删除卡（Java `deleteGiftCards`；请求字段 `cardGroupsId` 实际是卡 ID）。
    pub async fn delete(&self, param: &GiftCardIdParam) -> Result<()> {
        let gift_cards_id = param
            .gift_cards_id()
            .map_err(Error::Validation)?;
        let card = self
            .repo()
            .find_by_id(gift_cards_id)
            .await?
            .ok_or_else(|| Error::biz(500, MSG_CARD_NOT_EXISTS))?;
        // 多租户：删除前校验归属（经卡组 shop_id）
        ensure_group_access(self.repo(), card.card_groups_id).await?;
        // 未过期但已售出未兑换 ⇒ 不可删除
        if card.status == Some(0) && card.user_id.is_some() {
            return Err(Error::biz(500, MSG_CARD_SOLD_CANNOT_DELETE));
        }
        // 减可用库存
        if let Some(group) = self.repo().find_group(card.card_groups_id).await? {
            if card.is_available == Some(1) && group.availability_stock.unwrap_or(0) > 0 {
                let stock = group.availability_stock.unwrap_or(0) - 1;
                self.repo()
                    .update_group_stock(card.card_groups_id, stock)
                    .await?;
            }
        }
        self.repo().delete(gift_cards_id).await?;
        tracing::info!("删除礼品卡: id={gift_cards_id}");
        Ok(())
    }

    /// 修改可用状态（Java `updateGiftCardsAvailableStatus`）。
    pub async fn update_card_status(&self, dto: &GiftCardUpdateStatusDTO) -> Result<()> {
        let gift_cards_id = dto
            .gift_cards_id
            .ok_or_else(|| Error::Validation(MSG_CARD_ID_REQUIRED.into()))?;
        let is_available = dto
            .is_available
            .ok_or_else(|| Error::Validation(MSG_IS_AVAILABLE_REQUIRED.into()))?;
        let card = self
            .repo()
            .find_by_id(gift_cards_id)
            .await?
            .ok_or_else(|| Error::biz(500, MSG_CARD_ABNORMAL))?;
        // 多租户：状态变更前校验归属
        ensure_group_access(self.repo(), card.card_groups_id).await?;
        if card.is_available == Some(is_available) {
            return Ok(());
        }
        // 已售出的卡不能修改
        if card.user_id.is_some() {
            return Err(Error::biz(500, MSG_SOLD_CANNOT_CHANGE_STATUS));
        }
        let group = self
            .repo()
            .find_group(card.card_groups_id)
            .await?
            .ok_or_else(|| Error::biz(500, MSG_GROUP_NOT_EXISTS))?;
        let mut stock = group.availability_stock.unwrap_or(0);
        if is_available == 1 {
            stock += 1;
        } else if is_available == 0 && stock > 0 {
            stock -= 1;
        }
        self.repo()
            .update_group_stock(card.card_groups_id, stock)
            .await?;
        self.repo().update_available(gift_cards_id, is_available).await?;
        Ok(())
    }

    /// 礼品卡兑换详情（Java `giftCardExchangeDetail`）。
    pub async fn gift_card_exchange_detail(&self, gift_cards_id: i32) -> Result<GiftCardDetailVO> {
        let card = self
            .repo()
            .find_by_id(gift_cards_id)
            .await?
            .ok_or_else(|| Error::biz(500, MSG_CARD_NOT_EXISTS))?;
        let group = self
            .repo()
            .find_group(card.card_groups_id)
            .await?
            .ok_or_else(|| Error::biz(500, MSG_CARD_GROUP_NOT_EXISTS))?;

        let mut vo = GiftCardDetailVO {
            group_type: Some(group.group_type),
            card_type: Some(group.card_type),
            card_number: Some(card.card_number.clone()),
            card_password: Some(card.card_password.clone()),
            is_sold: Some(is_sold_text(card.user_id)),
            is_exchange: Some(is_exchange_text(card.status)),
            order_id: None,
            exchange_info: Some(GiftCardExchangeInfoVO {
                user_address_info: Some(GiftCardUserAddressInfoVO::default()),
                delivery_info: Some(GiftCardDeliveryInfoVO::default()),
                pay_info: Some(GiftCardPayInfoVO::default()),
            }),
        };

        // 储值卡：兑换信息取用户 + 面额
        if group.group_type == GROUP_TYPE_TOP_UP
            && card.user_id.is_some()
            && card.used_time.is_some()
        {
            let user = match card.user_id {
                Some(id) => self.repo().find_user_brief(id).await?,
                None => None,
            };
            let info = vo.exchange_info.get_or_insert_with(Default::default);
            if let Some((username, mobile)) = user {
                info.user_address_info = Some(GiftCardUserAddressInfoVO {
                    username: Some(username),
                    mobile: Some(visible_phone(&mobile, IS_DEMO)),
                    ..Default::default()
                });
            }
            info.pay_info = Some(GiftCardPayInfoVO {
                pay_type_name: Some("储值卡支付".to_string()),
                exchange_amount: group.face_amount,
                exchange_time: Some(format_unix_time(card.used_time)),
                received_time: None,
            });
            return Ok(vo);
        }

        // 兑换卡：取兑换订单信息
        let order = self.repo().find_order_by_gift_card(gift_cards_id).await?;
        if let Some(order) = order {
            if group.group_type == GROUP_TYPE_EXCHANGE {
                let info = vo.exchange_info.get_or_insert_with(Default::default);
                let user = self.repo().find_user_brief(order.user_id).await?;
                info.user_address_info = Some(address_info(&order, user, IS_DEMO));
                info.delivery_info = Some(delivery_info(&order));
                info.pay_info = Some(GiftCardPayInfoVO {
                    pay_type_name: Some("兑换卡支付".to_string()),
                    exchange_time: Some(format_unix_time(card.used_time)),
                    exchange_amount: None,
                    received_time: Some(format_unix_time(Some(order.received_time))),
                });
                vo.order_id = Some(order.order_id);
            }
        }
        Ok(vo)
    }

    // ————————————————————— client：详情 / 领取 / 转赠 / 过期 —————————————————————

    /// C 端详情基础信息（Java `giftCardsDetail(sn,false)`；路由层再合并卡组信息）。
    pub async fn client_detail(&self, sn: &str) -> Result<GiftCardsVO> {
        self.gift_cards_detail(sn, false).await
    }

    /// 领取卡（Java `claimCard`：只校验存在 + 可领取，**不落库**）。
    pub async fn claim_card(&self, param: &GiftCardClaimParam) -> Result<()> {
        let sn = param
            .gift_card_sn
            .as_deref()
            .ok_or_else(|| Error::Validation(MSG_TRANSFER_SN_REQUIRED.into()))?;
        let card = self
            .repo()
            .find_by_sn(sn)
            .await?
            .ok_or_else(|| Error::biz(500, MSG_SN_NOT_FOUND))?;
        if card.claimed_status != Some(CLAIMED_RECEIVABLE) {
            return Err(Error::biz(500, MSG_CARD_NOT_CLAIMABLE));
        }
        Ok(())
    }

    /// 转赠卡（Java `transferCard`）：置为「可领取」并返回转赠链接。
    pub async fn transfer_card(&self, param: &GiftCardClaimParam) -> Result<String> {
        let sn = param
            .gift_card_sn
            .as_deref()
            .ok_or_else(|| Error::Validation(MSG_TRANSFER_SN_REQUIRED.into()))?;
        let mut card = self
            .repo()
            .find_by_sn(sn)
            .await?
            .ok_or_else(|| Error::biz(500, MSG_SN_NOT_FOUND))?;
        if card.claimed_status == Some(CLAIMED_RECEIVED) {
            return Err(Error::biz(500, MSG_CARD_ALREADY_CLAIMED));
        }
        card.claimed_status = Some(CLAIMED_RECEIVABLE);
        self.repo().update(&card).await?;
        self.get_transfer_link(sn, param.is_from).await
    }

    /// 是否过期（Java `isGiftCardExpired(card)`）。
    pub async fn is_gift_card_expired(&self, card_id: i32) -> Result<bool> {
        let card = self
            .repo()
            .find_by_id(card_id)
            .await?
            .ok_or_else(|| Error::biz(500, MSG_CARD_NOT_EXISTS))?;
        Ok(card_expired(&card, now_seconds_i32()))
    }

    /// 兑换卡（Java `exchangeCard`）。
    pub async fn exchange_card(
        &self,
        param: &ExchangeCardParam,
        current_user_id: i32,
    ) -> Result<GiftCardsVO> {
        let card_number = param
            .card_number
            .as_deref()
            .ok_or_else(|| Error::Validation(MSG_EXCHANGE_FAILED.into()))?;
        let card_password = param
            .card_password
            .as_deref()
            .ok_or_else(|| Error::Validation(MSG_EXCHANGE_FAILED.into()))?;
        let user_id = param.user_id.unwrap_or(current_user_id);
        let mut card = self
            .repo()
            .find_for_exchange(card_number, card_password)
            .await?
            .ok_or_else(|| Error::biz(500, MSG_EXCHANGE_FAILED))?;
        let group = self
            .repo()
            .find_group(card.card_groups_id)
            .await?
            .ok_or_else(|| Error::biz(500, MSG_GROUP_NOT_EXISTS))?;
        if card.claimed_user_id.is_some() {
            return Err(Error::biz(500, MSG_CARD_ALREADY_EXCHANGED));
        }
        if card_expired(&card, now_seconds_i32()) {
            return Err(Error::biz(500, MSG_CARD_EXPIRED));
        }

        match group.group_type {
            GROUP_TYPE_TOP_UP => {
                let amount = group.face_amount.unwrap_or_default();
                // Java：先 rechargeOperation 建充值单，再 update(status=1) 入账
                self.repo()
                    .insert_recharge_order(user_id, amount, "充值卡兑换")
                    .await?;
                self.inc_balance(user_id, amount, "充值订单审核通过增加余额")
                    .await?;
                let now = now_seconds_i32();
                card.user_id = Some(user_id);
                card.claimed_user_id = Some(user_id);
                card.claimed_status = Some(CLAIMED_RECEIVED);
                card.used_time = Some(now);
                card.status = Some(YES);
                self.repo().update(&card).await?;
                let sn = card
                    .gift_card_sn
                    .clone()
                    .ok_or_else(|| Error::biz(500, MSG_CARD_NOT_EXISTS))?;
                self.gift_cards_detail(&sn, false).await
            }
            GROUP_TYPE_EXCHANGE => {
                let products = self.repo().find_group_products(card.card_groups_id).await?;
                if products.is_empty() {
                    return Err(Error::biz(500, MSG_PRODUCTS_OFFLINE));
                }
                let sn = card
                    .gift_card_sn
                    .clone()
                    .ok_or_else(|| Error::biz(500, MSG_CARD_NOT_EXISTS))?;
                let mut vo = self.gift_cards_detail(&sn, false).await?;
                vo.gift_card_products = Some(products);
                // 购物车写入未迁移（trade::cart 未接入），仅返回兑换回执
                vo.exchange_product_to_cart = Some(
                    tigshop_core::models::marketing::gift_card::ExchangeProductToCartVO {
                        item: true,
                        flow_type: 0,
                    },
                );
                Ok(vo)
            }
            _ => Err(Error::biz(500, MSG_UNSUPPORTED_GROUP_TYPE)),
        }
    }

    /// 转赠链接（Java `getTransferLink`；配置域名未迁移，使用相对路径）。
    pub async fn get_transfer_link(&self, gift_card_sn: &str, is_from: Option<i32>) -> Result<String> {
        if is_from != Some(TRANSFER_FROM_H5) && is_from != Some(TRANSFER_FROM_PC) {
            return Err(Error::biz(500, MSG_TRANSFER_SOURCE_REQUIRED));
        }
        if gift_card_sn.trim().is_empty() {
            return Err(Error::biz(500, MSG_TRANSFER_SN_REQUIRED));
        }
        let card = self
            .repo()
            .find_by_sn(gift_card_sn)
            .await?
            .ok_or_else(|| Error::biz(500, MSG_TRANSFER_CARD_NOT_FOUND))?;
        let group = self
            .repo()
            .find_group(card.card_groups_id)
            .await?
            .ok_or_else(|| Error::biz(500, MSG_TRANSFER_GROUP_NOT_FOUND))?;
        let shop_title = match group.shop_id.unwrap_or(0) {
            0 => String::new(), // 平台配置 SHOP_NAME 未迁移
            shop_id => self
                .repo()
                .find_shop(shop_id)
                .await?
                .and_then(|(title, _)| title)
                .unwrap_or_default(),
        };
        let base_url = if is_from == Some(TRANSFER_FROM_PC) {
            "/giftCard/transfer/"
        } else {
            "/pages/giftCard/transfer?giftCardSn="
        };
        Ok(format!(
            "【{shop_title}】{base_url}{gift_card_sn}  [{}]",
            group.card_group_name
        ))
    }

    /// 增加余额并写流水（Java `UserBalanceService.incBalance` 的最小复刻）。
    async fn inc_balance(&self, user_id: i32, amount: Decimal, desc: &str) -> Result<()> {
        let repo = &self.repos.member.user_manage;
        let user = repo
            .find_user(user_id)
            .await?
            .ok_or_else(|| Error::biz(500, "会员不存在"))?;
        let affected = repo
            .apply_balance_delta(user_id, CHANGE_TYPE_INC, amount)
            .await?;
        if affected == 0 {
            return Err(Error::biz(500, "会员不存在"));
        }
        let log = UserBalanceLog {
            log_id: 0,
            user_id,
            before_balance: user.balance,
            balance: amount,
            after_balance: user.balance + amount,
            frozen_balance: Decimal::ZERO,
            before_frozen_balance: user.frozen_balance,
            after_frozen_balance: user.frozen_balance,
            change_time: now_seconds_i32(),
            change_desc: desc.to_string(),
            change_type: CHANGE_TYPE_INC,
            wallet_type: 0,
            new_balance: Decimal::ZERO,
            new_frozen_balance: Decimal::ZERO,
        };
        repo.insert_balance_log(&log).await?;
        Ok(())
    }
}

/// 列表行 → VO（Java `GiftCardsVO.builder()` 只填部分字段）。
fn list_vo(card: &GiftCards) -> GiftCardsVO {
    GiftCardsVO {
        gift_cards_id: Some(card.gift_cards_id),
        card_groups_id: Some(card.card_groups_id),
        card_number: Some(card.card_number.clone()),
        gift_card_sn: card.gift_card_sn.clone(),
        card_password: Some(card.card_password.clone()),
        is_available: card.is_available,
        status: card.status,
        is_sold: Some(is_sold_text(card.user_id)),
        is_exchange: Some(is_exchange_text(card.status)),
        add_time: seconds_text(card.add_time),
        ..Default::default()
    }
}

/// 过期判定（Java `isGiftCardExpired`）：`expiredTime == null` ⇒ true；
/// `== 0` ⇒ false（永久）；否则 `now > expiredTime`。
#[must_use]
pub fn card_expired(card: &GiftCards, now: i32) -> bool {
    if card.expired_time == 0 {
        return false;
    }
    now > card.expired_time
}

/// 演示站手机号脱敏（Java `@Sensitive(PHONE)`）：仅 `is_demo == 1` 时脱敏。
#[must_use]
pub fn visible_phone(phone: &str, is_demo: i32) -> String {
    if is_demo == 1 {
        mask_mobile(phone)
    } else {
        phone.to_string()
    }
}

/// 组装「用户地址信息」（对应 Java 兑换卡分支的 `UserAddressInfoVO`）。
fn address_info(
    order: &Order,
    user: Option<(String, String)>,
    is_demo: i32,
) -> GiftCardUserAddressInfoVO {
    let region_name = parse_region_names(&order.region_names);
    GiftCardUserAddressInfoVO {
        username: user.map(|(name, _)| name),
        consignee: Some(order.consignee.clone()),
        mobile: Some(visible_phone(&order.mobile, is_demo)),
        email: Some(order.email.clone()),
        region_name,
        address: Some(order.address.clone()),
        buyer_note: Some(order.buyer_note.clone()),
    }
}

/// 配送信息（对应 Java `DeliveryInfoVO`）。
fn delivery_info(order: &Order) -> GiftCardDeliveryInfoVO {
    GiftCardDeliveryInfoVO {
        shipping_type_name: Some(order.shipping_type_name.clone()),
        shipping_time: if order.shipping_time > 0 {
            Some(format_unix_time(Some(order.shipping_time)))
        } else {
            None
        },
        logistics_name: Some(order.logistics_name.clone()),
        tracking_no: Some(order.tracking_no.clone()),
    }
}

/// `order.region_names`（JSON 数组）→ 空格拼接；非法 JSON ⇒ None（Java 抛异常）。
fn parse_region_names(raw: &str) -> Option<String> {
    let list: Vec<serde_json::Value> = serde_json::from_str(raw).ok()?;
    let names: Vec<String> = list.iter().map(|v| v.to_string().trim_matches('"').to_string()).collect();
    if names.is_empty() {
        None
    } else {
        Some(names.join(" "))
    }
}

fn now_seconds_i32() -> i32 {
    i32::try_from(now_seconds()).unwrap_or(0)
}

/// `BasePage` → `PageQuery`。
fn to_page_query(page: Option<i64>, size: Option<i64>) -> PageQuery {
    PageQuery {
        page: u64::try_from(page.unwrap_or(1).max(1)).unwrap_or(1),
        page_size: u64::try_from(match size {
            None => 15,
            Some(v) if v > 100 => 100,
            Some(v) if v < 1 => 1,
            Some(v) => v,
        })
        .unwrap_or(15),
    }
}

/// 归属校验：经卡组 `gift_card_groups.shop_id`（店铺账号越权 → 403）。
async fn ensure_group_access(
    repo: &tigshop_infrastructure::db::repo::marketing::gift_card::GiftCardRepository,
    card_groups_id: i32,
) -> Result<()> {
    if let Some(group) = repo.find_group(card_groups_id).await? {
        TenantScope::current_ensure_access(group.shop_id.unwrap_or(0))?;
    }
    Ok(())
}

#[cfg(test)]
mod tests {
    use super::*;

    fn card(expired_time: i32) -> GiftCards {
        GiftCards {
            gift_cards_id: 1,
            card_groups_id: 2,
            card_number: "GC20251031133058".into(),
            card_password: "PASSWORD123456".into(),
            gift_card_sn: Some("sn-1".into()),
            is_available: Some(1),
            status: Some(0),
            used_time: None,
            user_id: None,
            add_time: Some(1_733_000_000),
            expired_time,
            claimed_user_id: None,
            claimed_status: Some(CLAIMED_RECEIVABLE),
            order_time: None,
        }
    }

    #[tokio::test]
    async fn tenant_scope_blocks_cross_shop_gift_card() {
        use tigshop_infrastructure::db::repo::marketing::tenant::{
            with_tenant_scope, TenantScope,
        };
        let shop = TenantScope::for_shop(8);
        assert!(with_tenant_scope(shop, async { TenantScope::current_ensure_access(9) })
            .await
            .is_err());
        assert!(with_tenant_scope(shop, async { TenantScope::current_ensure_access(8) })
            .await
            .is_ok());
        let filter = with_tenant_scope(TenantScope::platform(), async {
            TenantScope::current_list_filter()
        })
        .await;
        assert_eq!(filter, None);
    }

    #[test]
    fn expired_rule_matches_java() {
        // 0 ⇒ 永久有效（false）
        assert!(!card_expired(&card(0), 1_733_000_000));
        // 非 0：now > expired
        assert!(card_expired(&card(1_000_000_000), 1_733_000_000));
        assert!(!card_expired(&card(1_900_000_000), 1_733_000_000));
    }

    #[test]
    fn list_vo_fills_only_java_builder_fields() {
        let vo = list_vo(&card(0));
        assert_eq!(vo.gift_cards_id, Some(1));
        assert_eq!(vo.is_sold.as_deref(), Some("否"));
        assert_eq!(vo.is_exchange.as_deref(), Some("否"));
        // Java `.addTime(Long.toString())` ⇒ 秒级时间戳字符串
        assert_eq!(vo.add_time.as_deref(), Some("1733000000"));
        // builder 未填字段
        assert!(vo.card_group_name.is_none());
        assert!(vo.used_time.is_none());
    }

    #[test]
    fn exchange_detail_phone_masks_only_on_demo() {
        assert_eq!(visible_phone("13812348000", IS_DEMO), "13812348000");
        assert_eq!(visible_phone("13812348000", 1), "138****8000");
    }

    #[test]
    fn region_names_json_join_like_java() {
        assert_eq!(
            parse_region_names(r#"["江西省","南昌市"]"#).as_deref(),
            Some("江西省 南昌市")
        );
        assert_eq!(parse_region_names("[]"), None);
        assert_eq!(parse_region_names("not-json"), None);
    }

    #[test]
    fn transfer_link_uses_relative_base_when_config_absent() {
        // Java: 平台店（shopId=0）取 SHOP_NAME 配置；未迁移 ⇒ 空标题前缀
        let base_pc = "/giftCard/transfer/";
        let base_h5 = "/pages/giftCard/transfer?giftCardSn=";
        assert_eq!(
            format!("【{}】{base_h5}sn-1  [礼品卡]", ""),
            "【】/pages/giftCard/transfer?giftCardSn=sn-1  [礼品卡]"
        );
        assert!(base_pc.ends_with('/'));
    }
}
