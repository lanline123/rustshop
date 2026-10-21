package com.tigshop.service.finance;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.finance.profitsharing.RefundSplitReversal;
import com.tigshop.bean.model.finance.ProfitSharingOrder;
import com.tigshop.bean.param.finance.ProfitSharingSubmitParam;
import com.tigshop.bean.query.finance.ProfitSharingPageQuery;
import com.tigshop.bean.vo.finance.ProfitSharingOrderVO;
import com.tigshop.service.common.BaseService;

import java.math.BigDecimal;
import java.util.List;

/**
 * 微信线上分账业务 Service
 *
 * @author Tigshop团队
 */
public interface ProfitSharingOrderService extends BaseService<ProfitSharingOrder> {

    /**
     * 分账单分页列表
     */
    Page<ProfitSharingOrderVO> listPage(ProfitSharingPageQuery query);

    /**
     * 批量发起分账（settlementIds 在单体下为订单ID列表）
     *
     * @param orderIds    订单ID列表
     * @param triggerType 触发方式 code（1手动 2自动）
     * @param operatorId  操作人（手动时记录）
     */
    void submitBatchProfitSharing(List<Long> orderIds, Integer triggerType, Long operatorId);

    /** 定时失败重试也须经过订单互斥和退款检查，不得直接调用渠道。 */
    void retryProfitSharing(Long profitSharingOrderId);

    /**
     * 手动完结分账单
     */
    void manualFinishProfitSharing(Long profitSharingOrderId, Long operatorId);

    /**
     * 处理分账回调
     */
    void handleNotify(String outOrderNo, String status, String channelOrderId);

    /**
     * 退款前判断是否存在需要回退的成功分账。
     * 原分账仍在处理中时抛出业务异常，须等待终态后再发起退款。
     */
    boolean requiresProfitSharingReturn(Long orderId);

    /**
     * 发起分账回退（售后退款时调用）
     */
    void initiateProfitSharingReturn(Long orderId, Long refundApplyId, BigDecimal returnAmount);

    /**
     * 计算退款时应回退的平台服务费基准（供汇付/支付宝在各自 payRefund 内联回退，渠道无关）。
     *
     * @param orderId      订单ID
     * @param refundAmount 本次退款金额
     * @return 回退基准；无成功分账时 needsReversal=false
     */
    RefundSplitReversal getRefundSplitReversal(Long orderId, BigDecimal refundAmount);
}
