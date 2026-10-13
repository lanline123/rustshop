/**
 * 会员标签人群能力开关
 *
 * 站内信群发、优惠券手动发放、订单列表筛选三处的「按会员标签圈选人群」依赖服务端的人群圈选接入。
 * 服务端已提供 SendUserTargetDTO / CouponManualIssueParam(TAG) / OrderListPageQuery.memberTagIds，
 * 于 2026-08-04 整体放开。若服务端出现回退，改为 false 即可整体收口，无需回改各业务页面。
 */
export const MEMBER_TAG_AUDIENCE_READY: boolean = true;
