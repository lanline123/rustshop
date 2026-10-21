package com.tigshop.service.finance;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.finance.CreditSettlementCancelDTO;
import com.tigshop.bean.dto.finance.CreditSettlementListDTO;
import com.tigshop.bean.dto.finance.CreditSettlementSubmitDTO;
import com.tigshop.bean.dto.finance.CreditSettlementUserListDTO;
import com.tigshop.bean.model.finance.CreditSettlement;
import com.tigshop.bean.vo.finance.CreditSettlementUserVO;
import com.tigshop.bean.vo.finance.CreditSettlementVO;
import com.tigshop.service.common.BaseService;

/**
 * 账期结算服务接口
 *
 * @author Tigshop团队
 * @create 2026-01-14
 */
public interface CreditSettlementService extends BaseService<CreditSettlement> {

    /**
     * 获取结算会员列表（有待结算订单的会员）
     *
     * @param listDTO 查询参数
     * @return 分页列表
     */
    Page<CreditSettlementUserVO> getUserList(CreditSettlementUserListDTO listDTO);

    /**
     * 确认结算
     *
     * @param submitDTO 结算参数
     * @return 是否成功
     */
    boolean submit(CreditSettlementSubmitDTO submitDTO);

    /**
     * 撤销结算
     *
     * @param cancelDTO 撤销参数
     * @return 是否成功
     */
    boolean cancel(CreditSettlementCancelDTO cancelDTO);

    /**
     * 历史结算列表
     *
     * @param listDTO 查询参数
     * @return 分页列表
     */
    Page<CreditSettlementVO> history(CreditSettlementListDTO listDTO);

    /**
     * 根据流水单号检查是否存在
     *
     * @param settlementSn 流水单号
     * @return 是否存在
     */
    boolean existsBySettlementSn(String settlementSn);
}
