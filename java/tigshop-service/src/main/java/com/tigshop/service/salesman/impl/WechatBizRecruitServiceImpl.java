// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.salesman.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.tigshop.bean.enums.salesman.DistributionModeEnum;
import com.tigshop.bean.enums.salesman.RecruitMethodEnum;
import com.tigshop.bean.enums.salesman.SalesmanConfigTypeEnum;
import com.tigshop.bean.model.salesman.Salesman;
import com.tigshop.bean.param.salesman.SalesmanSaveParam;
import com.tigshop.bean.vo.salesman.SalesmanConfigVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.service.salesman.SalesmanConfigService;
import com.tigshop.service.salesman.SalesmanService;
import com.tigshop.service.salesman.WechatBizRecruitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

/**
 * 微商模式下级招募服务实现
 *
 * @author Tigshop团队
 * @since 2026/6/12
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WechatBizRecruitServiceImpl implements WechatBizRecruitService {

    private final SalesmanConfigService salesmanConfigService;

    private final ObjectProvider<SalesmanService> salesmanService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void autoRecruitOnOrderSubmit(Integer buyerUserId, Collection<Integer> promoterSalesmanIds) {
        if (buyerUserId == null || CollUtil.isEmpty(promoterSalesmanIds)) {
            return;
        }
        SalesmanConfigVO config = salesmanConfigService.detail(SalesmanConfigTypeEnum.SALESMAN_CONFIG.getCode());
        if (!DistributionModeEnum.isWechatBiz(config.getDistributionMode())) {
            return;
        }
        if (!RecruitMethodEnum.contains(config.getRecruitMethods(), RecruitMethodEnum.CONSUME_AUTO)) {
            return;
        }
        // 已是分销员不重复开通、不改上级
        if (getSalesmanByUserId(buyerUserId) != null) {
            return;
        }
        // 同单多个推广人时取第一个有效归因
        for (Integer promoterSalesmanId : promoterSalesmanIds) {
            if (promoterSalesmanId == null) {
                continue;
            }
            Salesman promoter = salesmanService.getIfAvailable().getById(promoterSalesmanId);
            if (promoter == null) {
                continue;
            }
            if (buyerUserId.equals(promoter.getUserId())) {
                // 买家即推广人本人
                continue;
            }
            recruit(buyerUserId, promoter);
            log.info("微商模式消费自动开通：userId={} 成为分销员 salesmanId={} 的下级", buyerUserId, promoter.getSalesmanId());
            return;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void acceptCardInvite(Integer userId, Integer salesmanId) {
        SalesmanConfigVO config = salesmanConfigService.detail(SalesmanConfigTypeEnum.SALESMAN_CONFIG.getCode());
        Assert.isTrue(DistributionModeEnum.isWechatBiz(config.getDistributionMode()),
                () -> new GlobalException("当前未开启微商模式"));
        Assert.isTrue(RecruitMethodEnum.contains(config.getRecruitMethods(), RecruitMethodEnum.CARD_INVITE),
                () -> new GlobalException("当前未开启名片邀请开通"));

        Salesman inviter = salesmanService.getIfAvailable().getById(salesmanId);
        Assert.notNull(inviter, () -> new GlobalException("邀请人不存在"));
        Assert.isFalse(userId.equals(inviter.getUserId()), () -> new GlobalException("不能接受自己的名片邀请"));

        // 幂等：已是分销员直接返回，不改变既有上级
        if (getSalesmanByUserId(userId) != null) {
            return;
        }
        recruit(userId, inviter);
        log.info("微商模式名片邀请开通：userId={} 成为分销员 salesmanId={} 的下级", userId, inviter.getSalesmanId());
    }

    private void recruit(Integer userId, Salesman promoter) {
        SalesmanSaveParam saveParam = new SalesmanSaveParam();
        saveParam.setUserId(userId);
        saveParam.setLevel(1);
        saveParam.setGroupId(0);
        saveParam.setPid(promoter.getSalesmanId());
        salesmanService.getIfAvailable().create(saveParam);
        // 招募下级仅建立上下级分销关系（salesman.pid），不写入客户表 salesman_customer。
        // salesman_customer 仅记录新用户注册携带 salesmanId 形成的客户绑定。
    }

    private Salesman getSalesmanByUserId(Integer userId) {
        return salesmanService.getIfAvailable().lambdaQuery()
                .eq(Salesman::getUserId, userId)
                .last("limit 1")
                .one();
    }
}
