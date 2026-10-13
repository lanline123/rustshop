package com.tigshop.mapper.finance;

import com.tigshop.bean.model.finance.CreditSettlement;
import com.tigshop.mapper.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 账期结算Mapper
 *
 * @author Tigshop团队
 * @create 2026-01-14
 */
public interface CreditSettlementMapper extends BaseMapper<CreditSettlement> {

    /**
     * 根据流水单号查询结算记录
     *
     * @param settlementSn 回款流水单号
     * @return 结算记录
     */
    @Select("SELECT * FROM credit_settlement WHERE settlement_sn = #{settlementSn}")
    CreditSettlement selectBySettlementSn(@Param("settlementSn") String settlementSn);
}
