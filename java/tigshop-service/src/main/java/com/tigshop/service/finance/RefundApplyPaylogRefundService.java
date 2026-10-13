package com.tigshop.service.finance;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tigshop.bean.model.finance.RefundApplyPaylogRefund;

import java.util.List;

/**
 * 退款申请与退款流水关联服务接口
 *
 * @author Tigshop团队
 * @create 2025年11月27日
 */
public interface RefundApplyPaylogRefundService extends IService<RefundApplyPaylogRefund> {

    /**
     * 创建关联关系
     *
     * @param refundApplyId 退款申请ID
     * @param paylogRefundId 退款流水ID
     * @return 是否成功
     */
    boolean createRelation(Integer refundApplyId, Integer paylogRefundId);
}

