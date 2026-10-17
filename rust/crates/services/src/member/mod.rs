//! member 领域服务。

pub mod feedback;
pub mod login;
pub mod member_lists;
pub mod member_tag;
pub mod member_tag_group;
pub mod shop_member;
pub mod user;
pub mod user_center;
pub mod user_company;
pub mod user_manage;
pub mod user_points_log;
pub mod user_rank;

use std::sync::Arc;

/// member 领域服务集合（由组合根装配一次，api 层只持有 `Arc<Services>`）。
#[derive(Clone)]
pub struct Services {
    pub feedback: Arc<feedback::FeedbackService>,
    pub member_lists: Arc<member_lists::MemberService>,
    pub member_tag: Arc<member_tag::MemberTagService>,
    pub member_tag_group: Arc<member_tag_group::MemberTagGroupService>,
    pub user: Arc<user::UserService>,
    pub user_center: Arc<user_center::UserCenterService>,
    pub user_company: Arc<user_company::UserCompanyService>,
    pub user_manage: Arc<user_manage::UserManageService>,
    pub user_points_log: Arc<user_points_log::UserPointsLogService>,
    pub user_rank: Arc<user_rank::UserRankService>,
    pub login: Arc<login::LoginService>,
    pub shop_member: Arc<shop_member::ShopMemberService>,
}
