package com.tigshop.service.finance;

/**
 * 微信线上分账定时任务 Service
 *
 * @author Tigshop团队
 */
public interface ProfitSharingScheduleService {

    /**
     * 自动分账：扫描已结算、超过等待天数的微信订单并发起分账
     */
    void executeAutoProfitSharing();

    /**
     * 状态补偿：查询 PROCESSING 分账单的最终状态
     */
    void executeStatusCompensation();

    /**
     * 完结补偿：对成功但未完结的分账单补发完结
     */
    void executeFinishCompensation();

    /**
     * 超时预警：扫描临近微信冻结上限的挂起分账单
     */
    void executeTimeoutWarning();

    /**
     * 失败重试：对失败且可重试的分账单重新发起
     */
    void executeRetryFailedProfitSharing();

    /**
     * 进件状态轮询
     */
    void executeMerchantApplicationPolling();

    /**
     * 线上提现状态轮询
     */
    void executeWithdrawStatusPolling();

    /**
     * 回退单状态补偿
     */
    void executeReturnOrderCompensation();
}
