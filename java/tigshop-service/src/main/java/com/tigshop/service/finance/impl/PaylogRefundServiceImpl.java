package com.tigshop.service.finance.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tigshop.bean.model.finance.PaylogRefund;
import com.tigshop.mapper.finance.PaylogRefundMapper;
import com.tigshop.service.finance.PaylogRefundService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
* @author kidd
*/
@Service
public class PaylogRefundServiceImpl extends ServiceImpl<PaylogRefundMapper, PaylogRefund> implements PaylogRefundService {

    /** 处理状态：0 申请中（异步在途），1 成功 */
    private static final List<Integer> OCCUPIED_STATUS = List.of(0, 1);

    @Override
    public BigDecimal sumOccupiedRefundAmount(Integer paylogId) {
        if (paylogId == null) {
            return BigDecimal.ZERO;
        }
        return this.lambdaQuery()
                .eq(PaylogRefund::getPaylogId, paylogId)
                .in(PaylogRefund::getStatus, OCCUPIED_STATUS)
                .list()
                .stream()
                .map(PaylogRefund::getRefundAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}




