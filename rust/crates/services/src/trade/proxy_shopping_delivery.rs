//! 代客下单配送方式域服务（trade::proxy_shopping_delivery）。
//!
//! 两个端点均先执行 Java 校验（「请选择用户」等），再返回明确「未迁移」业务错误：
//! O2O 配送距离计算 / 运费模板引擎尚未迁移，不编造配送数据。

use std::sync::Arc;

use tigshop_core::models::trade::proxy_shopping_delivery::{
    validate_user_id, DeliveryOptionVO, DeliveryOptionsQuery, ProxyStoreShippingTypeQuery,
    ShippingTypeVO, CART_TYPE_ADMIN_SHOPPING, MSG_DELIVERY_NOT_MIGRATED,
    MSG_STORE_SHIPPING_NOT_MIGRATED,
};
use tigshop_core::{Error, Result};
use tigshop_infrastructure::db::repo::trade::proxy_shopping_delivery::ProxyShoppingDeliveryRepository;
use tigshop_infrastructure::db::Repositories;

/// 代客下单配送方式服务。
pub struct ProxyShoppingDeliveryService {
    repos: Arc<Repositories>,
}

impl ProxyShoppingDeliveryService {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本域仓储（NESTED：`self.repos.trade.proxy_shopping_delivery`）。
    #[must_use]
    pub fn repo(&self) -> &ProxyShoppingDeliveryRepository {
        &self.repos.trade.proxy_shopping_delivery
    }

    /// 配送选项（Java `getDeliveryOptions`）：校验用户后返回未迁移错误。
    pub async fn get_delivery_options(
        &self,
        query: &DeliveryOptionsQuery,
        shop_id: Option<i32>,
    ) -> Result<DeliveryOptionVO> {
        let user_id = validate_user_id(query.user_id).map_err(Error::Validation)?;
        let _ = self.repo().sum_cart_quantity(user_id, shop_id).await?;
        Err(Error::biz_msg(MSG_DELIVERY_NOT_MIGRATED))
    }

    /// 店铺配送方式（Java `getStoreShippingType`）。
    pub async fn get_store_shipping_type(
        &self,
        query: &ProxyStoreShippingTypeQuery,
        shop_id: Option<i32>,
    ) -> Result<Vec<(i32, Vec<ShippingTypeVO>)>> {
        let user_id = validate_user_id(query.user_id).map_err(Error::Validation)?;
        let effective_shop = query.shop_id.or(shop_id);
        let check_service = crate::trade::check::CheckService::new(self.repos.clone());
        let store_query = tigshop_core::models::trade::check::StoreShippingTypeQuery {
            address_id: query.address_id,
            flow_type: Some(CART_TYPE_ADMIN_SHOPPING),
            shop_id: effective_shop,
            user_id: Some(user_id),
            pay_type_id: query.pay_type_id,
            use_point: query.use_point,
            use_balance: query.use_balance,
            use_coupon_ids: query.use_coupon_ids.clone(),
            exclude_quick_product: Some(false),
        };
        let map = check_service.store_shipping_types(&store_query).await?;
        let mut list: Vec<(i32, Vec<ShippingTypeVO>)> = Vec::new();
        for (sid, types) in map {
            let vos: Vec<ShippingTypeVO> = types
                .into_iter()
                .map(|t| ShippingTypeVO {
                    shipping_type_id: t.shipping_type_id,
                    shipping_type_name: t.shipping_type_name.clone(),
                    origin_shipping_type_name: t.shipping_type_name,
                    shipping_default_id: Some(0),
                    shipping_type_desc: Some(String::new()),
                    shipping_time_desc: Some(String::new()),
                    is_support_cod: t.is_support_cod,
                })
                .collect();
            list.push((sid, vos));
        }
        list.sort_by_key(|(sid, _)| *sid);
        Ok(list)
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn user_validation_reused() {
        assert!(validate_user_id(None).is_err());
    }

    #[test]
    fn not_migrated_messages_present() {
        assert!(MSG_DELIVERY_NOT_MIGRATED.contains("尚未迁移"));
    }
}
