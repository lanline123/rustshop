//! 微信菜单服务（content::wechat_menu）。
//!
//! 对应原 Java `com.tigshop.service.setting.wechat.impl.WechatMenuServiceImpl`。
//!
//! 构造签名固定：`pub fn new(repos: Arc<Repositories>) -> Self`。
//!
//! ## 已迁移（2）
//! - `get`：读 `wechat_menu` 单例并解析 `menu_json`；空配置/解析失败 → 空树
//! - `saveAndPublish`：**全量本地校验**（一级/二级/规则，文案逐字）后返回
//!   明确「未迁移」业务错误（微信 `menu/create` 推送 + 落库未迁移，既不推送
//!   也不落库，避免 DB 与微信 key 错位）
//!
//! ## 与 Java 的差异
//! 见 [`crate::models::content::wechat_menu`] 模块头。

use std::sync::Arc;

use tigshop_core::models::content::wechat_menu::{
    parse_menu_tree, validate_menu, SaveAndPublishVO, WxMenuSaveDTO, WxMenuVO,
    MSG_PUBLISH_NOT_MIGRATED,
};
use tigshop_core::{Error, Result};
use tigshop_infrastructure::db::Repositories;

/// 微信菜单服务。
pub struct WechatMenuService {
    repos: Arc<Repositories>,
}

impl WechatMenuService {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储（NESTED 访问：`self.repos.content.wechat_menu`）。
    #[must_use]
    pub fn repo(
        &self,
    ) -> &tigshop_infrastructure::db::repo::content::wechat_menu::WechatMenuRepository {
        &self.repos.content.wechat_menu
    }

    /// 获取菜单详情（Java `get`）。
    ///
    /// # Errors
    /// 数据库失败 → `Error::Database`。
    pub async fn get(&self) -> Result<WxMenuVO> {
        let Some(entity) = self.repo().find_first().await? else {
            return Ok(WxMenuVO {
                id: None,
                tree: Default::default(),
                published_at: None,
                updated_at: None,
            });
        };
        Ok(WxMenuVO {
            id: Some(entity.id),
            tree: parse_menu_tree(Some(&entity.menu_json)),
            published_at: entity.published_at,
            updated_at: Some(entity.updated_at),
        })
    }

    /// 保存并发布（Java `saveAndPublish`）：本地全量校验 → 明确「未迁移」。
    ///
    /// # Errors
    /// - 校验失败 → 业务错误（逐字文案）
    /// - 校验通过 → 业务错误「未迁移」（微信推送 + 落库未迁移）
    pub fn save_and_publish(&self, dto: &WxMenuSaveDTO) -> Result<SaveAndPublishVO> {
        validate_menu(dto).map_err(Error::biz_msg)?;
        Err(Error::biz_msg(MSG_PUBLISH_NOT_MIGRATED))
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use tigshop_core::models::content::wechat_menu::{click_rule, leaf_node, WxMenuTree};

    #[test]
    fn save_and_publish_validates_before_not_migrated() {
        // 空菜单 → 校验错误（不是未迁移）
        let err = validate_menu(&WxMenuSaveDTO::default()).unwrap_err();
        assert_eq!(err, "菜单结构不能为空");
        // 合法菜单 → 校验通过（后续由 service 返回未迁移）
        let dto = WxMenuSaveDTO {
            tree: Some(WxMenuTree {
                buttons: vec![leaf_node("资讯", click_rule("MENU_1", "欢迎"))],
            }),
        };
        assert!(validate_menu(&dto).is_ok());
    }

    #[test]
    fn not_migrated_message_is_explicit() {
        assert!(MSG_PUBLISH_NOT_MIGRATED.contains("尚未迁移"));
        assert!(MSG_PUBLISH_NOT_MIGRATED.contains("menu/create"));
    }

    #[test]
    fn get_vo_defaults_when_missing() {
        // 不触库：仅验证空 VO 结构与序列化
        let vo = WxMenuVO {
            id: None,
            tree: WxMenuTree::default(),
            published_at: None,
            updated_at: None,
        };
        let json = serde_json::to_value(&vo).unwrap();
        assert!(json["id"].is_null());
        assert!(json["tree"]["buttons"].as_array().unwrap().is_empty());
        assert!(json["publishedAt"].is_null());
    }

    #[test]
    fn save_vo_shape() {
        let vo = SaveAndPublishVO {
            published: false,
            err_msg: Some(MSG_PUBLISH_NOT_MIGRATED.into()),
            published_at: None,
        };
        let json = serde_json::to_value(&vo).unwrap();
        assert_eq!(json["published"], false);
        assert_eq!(json["errMsg"], MSG_PUBLISH_NOT_MIGRATED);
        assert!(json["publishedAt"].is_null());
    }
}
