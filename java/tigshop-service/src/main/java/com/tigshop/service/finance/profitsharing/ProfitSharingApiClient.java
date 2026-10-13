package com.tigshop.service.finance.profitsharing;

import com.tigshop.bean.dto.finance.profitsharing.CreateProfitSharingOrderRequest;
import com.tigshop.bean.dto.finance.profitsharing.FinishProfitSharingRequest;
import com.tigshop.bean.dto.finance.profitsharing.MerchantApplicationRequest;
import com.tigshop.bean.dto.finance.profitsharing.MerchantApplicationResult;
import com.tigshop.bean.dto.finance.profitsharing.NotifyPayload;
import com.tigshop.bean.dto.finance.profitsharing.ProfitSharingNotifyResult;
import com.tigshop.bean.dto.finance.profitsharing.ProfitSharingOrderResult;
import com.tigshop.bean.dto.finance.profitsharing.ProfitSharingReturnRequest;
import com.tigshop.bean.dto.finance.profitsharing.ProfitSharingReturnResult;
import com.tigshop.bean.dto.finance.profitsharing.SubMerchantBalanceResult;
import com.tigshop.bean.dto.finance.profitsharing.WithdrawRequest;
import com.tigshop.bean.dto.finance.profitsharing.WithdrawResult;

import java.math.BigDecimal;

/**
 * 分账渠道API客户端接口
 *
 * @author Tigshop团队
 */
public interface ProfitSharingApiClient {

    String channel();

    ProfitSharingOrderResult createOrder(CreateProfitSharingOrderRequest request);

    /**
     * @param subMchId 二级商户号，必须由调用方按单据解析后传入，不得回退全局配置
     */
    ProfitSharingOrderResult queryOrder(String subMchId, String outOrderNo, String transactionId);

    void finishOrder(FinishProfitSharingRequest request);

    BigDecimal queryUnsplitAmount(String transactionId);

    ProfitSharingReturnResult returnOrder(ProfitSharingReturnRequest request);

    /**
     * @param subMchId 二级商户号，必须由调用方按单据解析后传入，不得回退全局配置
     */
    ProfitSharingReturnResult queryReturnOrder(String subMchId, String outOrderNo, String outReturnNo);

    SubMerchantBalanceResult queryBalance(String subMchId);

    /**
     * 校验二级商户号是否归属于当前服务商（受理关系是否存在）。
     *
     * <p>手动绑定子商户时调用，把「受理关系不存在」从用户下单时提前到运营绑定时。
     * 默认实现放行：只有能明确区分「渠道应答说不在名下」与「网络/超时」的渠道才应覆写，
     * 否则渠道抖动会让运营绑不上商户。
     *
     * @return {@code true} 归属确认或无法判定；{@code false} 渠道明确应答该商户号不在名下
     */
    default boolean isSubMerchantOwned(String subMchId) {
        return true;
    }

    WithdrawResult withdraw(WithdrawRequest request);

    /**
     * 按商户提现单号查询二级商户提现状态。
     *
     * @param subMchId     二级商户号，必须由调用方按单据解析后传入，不得回退全局配置
     * @param outRequestNo 商户提现单号（创建提现时的 out_request_no），不是微信返回的 withdraw_id
     */
    WithdrawResult queryWithdraw(String subMchId, String outRequestNo);

    ProfitSharingNotifyResult parseNotification(NotifyPayload payload);

    MerchantApplicationResult submitApplication(MerchantApplicationRequest request);

    MerchantApplicationResult queryApplicationStatus(String applymentId);

    String uploadMedia(byte[] fileBytes, String fileName);
}
