package com.tigshop.service.finance;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.finance.CreditOrderListDTO;
import com.tigshop.bean.model.finance.CreditOrder;
import com.tigshop.bean.vo.finance.CreditOrderVO;
import com.tigshop.service.common.BaseService;

import java.math.BigDecimal;
import java.util.List;

/**
 * 账期订单服务接口
 *
 * @author Tigshop团队
 * @create 2026-01-14
 */
public interface CreditOrderService extends BaseService<CreditOrder> {

    /**
     * 账期订单列表
     *
     * @param listDTO 查询参数
     * @return 分页列表
     */
    Page<CreditOrderVO> list(CreditOrderListDTO listDTO);

    /**
     * 创建账期订单记录
     *
     * @param orderId      订单ID
     * @param orderSn      订单号
     * @param userId       用户ID
     * @param creditUserId 授信会员ID
     * @param creditAmount 账期支付金额
     */
    void createCreditOrder(Integer orderId, String orderSn, Integer userId, Integer creditUserId, BigDecimal creditAmount);

    /**
     * 获取待结算订单列表
     *
     * @param userId 用户ID
     * @return 待结算订单列表
     */
    List<CreditOrderVO> getPendingOrders(Integer userId);

    /**
     * 根据订单ID获取账期订单
     *
     * @param orderId 订单ID
     * @return 账期订单
     */
    CreditOrder getByOrderId(Integer orderId);

    /**
     * 更新订单结算状态
     *
     * @param orderIds     订单ID列表
     * @param settlementId 结算ID
     * @param settlementSn 流水单号
     */
    void updateSettlementStatus(List<Integer> orderIds, Integer settlementId, String settlementSn);

    /**
     * 撤销订单结算状态
     *
     * @param settlementId 结算ID
     */
    void cancelSettlementStatus(Integer settlementId);

    /**
     * 统计用户待结算订单数和金额
     *
     * @param userId 用户ID
     * @return [订单数, 金额]
     */
    Object[] countPendingOrders(Integer userId);
}
