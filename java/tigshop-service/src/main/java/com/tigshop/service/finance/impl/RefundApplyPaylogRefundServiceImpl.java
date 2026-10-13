package com.tigshop.service.finance.impl;

import com.tigshop.bean.model.finance.RefundApplyPaylogRefund;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.finance.RefundApplyPaylogRefundService;
import com.tigshop.mapper.finance.RefundApplyPaylogRefundMapper;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 退款申请与退款流水关联服务实现
 *
 * @author Tigshop团队
 * @create 2025年11月27日
 */
@Slf4j
@Service
public class RefundApplyPaylogRefundServiceImpl extends BaseServiceImpl<RefundApplyPaylogRefundMapper, RefundApplyPaylogRefund> implements RefundApplyPaylogRefundService {


    @Override
    public boolean createRelation(Integer refundApplyId, Integer paylogRefundId) {
        if (refundApplyId == null || paylogRefundId == null) {
            log.warn("创建退款关联关系失败，参数不能为空：refundApplyId={}, paylogRefundId={}", refundApplyId, paylogRefundId);
            return false;
        }

        RefundApplyPaylogRefund relation = RefundApplyPaylogRefund.builder().refundApplyId(refundApplyId).paylogRefundId(paylogRefundId).build();
        return this.save(relation);
    }
}

