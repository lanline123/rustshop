//! 会员域服务：等级/标签/积分流水。
//! 对应原 Java `UserRankServiceImpl` / `MemberTagServiceImpl` 等。
//!
//! TODO(域实现者)：对照 Java 源补全等级自动升级、标签批量打标等逻辑。

use std::sync::Arc;

use tigshop_core::entities::{MemberTagGroup, UserPointsLog};
use tigshop_core::models::{UserPointsLogQuery, UserRankVO};
use tigshop_core::types::{PageQuery, PageResult};
use tigshop_core::{Error, Result};
use tigshop_infrastructure::db::Repositories;

pub struct MemberService {
    repos: Arc<Repositories>,
}

impl MemberService {
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 全部会员等级。
    ///
    /// 实体列名与旧 VO 字段名不同，映射关系：
    /// - `min_growth_points`（升级门槛，Decimal）→ VO `upgrade_amount`
    /// - `discount`（Decimal 折扣，如 0.0/9.5）→ VO `discount`（整数百分比）
    pub async fn ranks(&self) -> Result<Vec<UserRankVO>> {
        let rows = self.repos.member.member_lists.find_all_ranks().await?;
        Ok(rows
            .iter()
            .map(|r| UserRankVO {
                rank_id: r.rank_id,
                rank_name: Some(r.rank_name.clone()),
                upgrade_amount: Some(r.min_growth_points),
                // Decimal 无法表示时退化为 None，避免展示异常值
                discount: i32::try_from(r.discount).ok(),
            })
            .collect())
    }

    /// 标签组（含组下标签）。
    pub async fn tag_groups(&self) -> Result<Vec<MemberTagGroup>> {
        self.repos.member.member_lists.find_all_tag_groups().await
    }

    /// 积分流水分页。
    pub async fn points_logs(&self, q: &UserPointsLogQuery) -> Result<PageResult<UserPointsLog>> {
        let page = PageQuery {
            page: q.page.unwrap_or(1),
            page_size: q.page_size.unwrap_or(20),
        };
        let (total, list) = self
            .repos.member.member_lists
            .paginate_points_logs(q.user_id, &page)
            .await?;
        Ok(PageResult::new(total, list, &page))
    }

    /// 增加积分并写流水（原子防负）。
    pub async fn add_points(&self, user_id: i32, delta: i32, _reason: &str) -> Result<()> {
        if delta == 0 {
            return Err(Error::Validation("积分变动不能为 0".into()));
        }
        let ok = self.repos.member.member_lists.try_adjust_points(user_id, delta).await?;
        if !ok {
            return Err(Error::biz(400, "积分不足"));
        }
        // TODO(域实现者)：在同一事务中写 user_points_log 流水。
        Ok(())
    }

    /// 会员分页（发放对象选择器，关键字匹配用户名/昵称/手机/邮箱）。
    pub async fn paginate_users(
        &self,
        keyword: Option<&str>,
        page: u64,
        page_size: u64,
    ) -> Result<PageResult<tigshop_core::entities::User>> {
        let page = PageQuery {
            page: page.max(1),
            page_size: page_size.clamp(1, 200),
        };
        let (total, list) = self.repos.member.user.paginate_members(keyword, &page).await?;
        Ok(PageResult::new(total, list, &page))
    }
}