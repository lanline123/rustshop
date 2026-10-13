package com.tigshop.service.deposit;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.deposit.DepositLogListDTO;
import com.tigshop.bean.vo.deposit.DepositLogVO;

/**
 * 平台端保证金全局交易明细服务（W3：交易明细 list/detail）。
 *
 * @author Tigshop团队
 */
public interface DepositLogAdminService {

    /**
     * 全局交易明细分页。筛选：店铺名/交易单号、商户名、交易类型、交易时间快捷区间。
     */
    Page<DepositLogVO> list(DepositLogListDTO listDTO);

    /**
     * 交易明细详情。
     */
    DepositLogVO detail(Integer id);
}
