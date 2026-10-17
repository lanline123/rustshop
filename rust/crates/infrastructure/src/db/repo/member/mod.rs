//! member 领域仓储。

pub mod feedback;
pub mod member_lists;
pub mod member_tag;
pub mod member_tag_group;
pub mod user;
pub mod user_center;
pub mod user_company;
pub mod user_manage;
pub mod user_points_log;
pub mod user_rank;

use crate::db::DbPool;

#[derive(Clone)]
pub struct Repositories {
    pub feedback: feedback::FeedbackRepository,
    pub member_lists: member_lists::MemberRepository,
    pub member_tag: member_tag::MemberTagRepository,
    pub member_tag_group: member_tag_group::MemberTagGroupRepository,
    pub user: user::UserRepository,
    pub user_center: user_center::UserCenterRepository,
    pub user_company: user_company::UserCompanyRepository,
    pub user_manage: user_manage::UserManageRepository,
    pub user_points_log: user_points_log::UserPointsLogRepository,
    pub user_rank: user_rank::UserRankRepository,
}

impl Repositories {
    #[must_use]
    pub fn new(pool: DbPool) -> Self {
        Self {
            feedback: feedback::FeedbackRepository::new(pool.clone()),
            member_lists: member_lists::MemberRepository::new(pool.clone()),
            member_tag: member_tag::MemberTagRepository::new(pool.clone()),
            member_tag_group: member_tag_group::MemberTagGroupRepository::new(pool.clone()),
            user: user::UserRepository::new(pool.clone()),
            user_center: user_center::UserCenterRepository::new(pool.clone()),
            user_company: user_company::UserCompanyRepository::new(pool.clone()),
            user_manage: user_manage::UserManageRepository::new(pool.clone()),
            user_points_log: user_points_log::UserPointsLogRepository::new(pool.clone()),
            user_rank: user_rank::UserRankRepository::new(pool.clone()),
        }
    }
}
