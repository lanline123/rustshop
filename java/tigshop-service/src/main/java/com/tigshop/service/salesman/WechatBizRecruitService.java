// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.salesman;

import java.util.Collection;

/**
 * 微商模式下级招募服务（消费自动开通 / 名片邀请开通）
 *
 * <p>仅微商模式（distributionMode=2）生效，整组文件随 VORT common.wechatBiz 裁剪。</p>
 *
 * @author Tigshop团队
 * @since 2026/6/12
 */
public interface WechatBizRecruitService {

    /**
     * 提交订单时的消费自动开通。
     * <p>微商模式且招募方式含「消费自动开通」时，买家经分销员推广下单
     * 自动成为该分销员的下级分销员（仅建立 salesman.pid 上下级关系，
     * 不写入客户表 salesman_customer）。买家已是分销员、买家即推广人本人时跳过。</p>
     *
     * @param buyerUserId         买家用户ID
     * @param promoterSalesmanIds 订单项携带的推广分销员ID集合（取第一个有效归因）
     */
    void autoRecruitOnOrderSubmit(Integer buyerUserId, Collection<Integer> promoterSalesmanIds);

    /**
     * 名片邀请开通：扫分销员名片二维码一键成为其下级分销员。
     * <p>仅建立 salesman.pid 上下级关系，不写入客户表 salesman_customer。
     * 已是分销员时幂等返回，不改变既有上级关系。</p>
     *
     * @param userId     当前登录用户ID
     * @param salesmanId 邀请人分销员ID
     */
    void acceptCardInvite(Integer userId, Integer salesmanId);
}
