package com.tigshop.service.finance;

import com.tigshop.bean.model.finance.PaylogRefund;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;

/**
* @author kidd
*/
public interface PaylogRefundService extends IService<PaylogRefund> {

    /**
     * 汇总某笔支付记录上已占用的退款额度。
     * <p>统计口径为「已成功(1) + 处理中(0)」：退款审核事务在渠道异常或返回 FAIL 时整体回滚，
     * 失败尝试不会留下行，因此表内存量只有这两种，两者都已占用可退余额。
     *
     * @param paylogId 支付记录 id
     * @return 已占用额度，无记录时为 0
     */
    BigDecimal sumOccupiedRefundAmount(Integer paylogId);
}
