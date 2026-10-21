package com.tigshop.service.finance.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.dto.finance.profitsharing.FinishProfitSharingRequest;
import com.tigshop.bean.dto.finance.profitsharing.ProfitSharingOrderResult;
import com.tigshop.bean.dto.finance.profitsharing.ProfitSharingReturnResult;
import com.tigshop.bean.dto.finance.profitsharing.WithdrawResult;
import com.tigshop.bean.enums.finance.MerchantApplicationStatus;
import com.tigshop.bean.enums.finance.ProfitSharingFinishStatus;
import com.tigshop.bean.enums.finance.ProfitSharingStatus;
import com.tigshop.bean.enums.finance.ProfitSharingTriggerType;
import com.tigshop.bean.enums.finance.SettlementStatus;
import com.tigshop.bean.enums.finance.StatementType;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.finance.MerchantApplication;
import com.tigshop.bean.model.finance.ProfitSharingOrder;
import com.tigshop.bean.model.finance.ProfitSharingReturnOrder;
import com.tigshop.bean.model.finance.Statement;
import com.tigshop.bean.model.finance.SubMerchant;
import com.tigshop.bean.model.shop.ShopWithdraw;
import com.tigshop.mapper.finance.ProfitSharingReturnOrderMapper;
import com.tigshop.mapper.finance.StatementMapper;
import com.tigshop.mapper.shop.ShopWithdrawMapper;
import com.tigshop.service.finance.MerchantApplicationService;
import com.tigshop.service.finance.ProfitSharingOrderService;
import com.tigshop.service.finance.ProfitSharingScheduleService;
import com.tigshop.service.finance.SubMerchantService;
import com.tigshop.service.finance.profitsharing.ProfitSharingClientRegistry;
import com.tigshop.service.setting.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 微信线上分账定时任务 Service 实现
 *
 * @author Tigshop团队
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProfitSharingScheduleServiceImpl implements ProfitSharingScheduleService {

    private static final int MAX_RETRY = 3;
    private static final String PAY_CHANNEL_WECHAT = "WECHAT";

    private final ProfitSharingOrderService profitSharingOrderService;
    private final ProfitSharingReturnOrderMapper profitSharingReturnOrderMapper;
    private final StatementMapper statementMapper;
    private final ShopWithdrawMapper shopWithdrawMapper;
    private final MerchantApplicationService merchantApplicationService;
    private final ProfitSharingClientRegistry profitSharingClientRegistry;
    private final SubMerchantService subMerchantService;
    private final ConfigService configService;

    @jakarta.annotation.Resource
    private com.tigshop.service.shop.ShopWithdrawService shopWithdrawService;

    @jakarta.annotation.Resource
    private com.tigshop.service.shop.AccountService accountService;

    @Override
    public void executeAutoProfitSharing() {
        Integer splitMethod = parseInt(configService.getConfigVal(SettingsEnum.SPLIT_PAYMENT_METHOD), 1);
        Integer autoEnabled = parseInt(configService.getConfigVal(SettingsEnum.AUTO_PROFIT_SHARING_ENABLED), 0);
        int autoDays = parseInt(configService.getConfigVal(SettingsEnum.AUTO_PROFIT_SHARING_DAYS), 7);

        if (!configService.isProfitSharingEnabled()) {
            log.info("[自动分账] 分账总开关未开启，跳过");
            return;
        }
        if (!Integer.valueOf(2).equals(splitMethod)) {
            log.info("[自动分账] 当前非线上分账模式，跳过");
            return;
        }
        if (!Integer.valueOf(1).equals(autoEnabled)) {
            log.info("[自动分账] 自动分账未开启，跳过");
            return;
        }

        long cutoff = (System.currentTimeMillis() / 1000) - (long) autoDays * 24 * 3600;

        List<Statement> serviceFeeStatements = statementMapper.selectList(new LambdaQueryWrapper<Statement>()
                .eq(Statement::getType, StatementType.SERVICE_FEE.getCode())
                .eq(Statement::getSettlementStatus, SettlementStatus.SETTLED.getCode())
                .gt(Statement::getShopId, 0)
                .gt(Statement::getRecordId, 0)
                .le(Statement::getSettlementTime, cutoff)
                // 必须先排除已有分账记录再限量，否则最早的 200 条会长期占据扫描窗口。
                .notExists("SELECT 1 FROM profit_sharing_order pso WHERE pso.order_id = statement.record_id")
                .orderByAsc(Statement::getSettlementTime, Statement::getStatementId)
                .last("limit 200"));

        if (serviceFeeStatements.isEmpty()) {
            log.info("[自动分账] 无就绪的服务费结算记录");
            return;
        }

        List<Long> orderIds = serviceFeeStatements.stream()
                .map(s -> s.getRecordId() == null ? null : s.getRecordId().longValue())
                .filter(id -> id != null && id > 0)
                .distinct()
                .collect(Collectors.toList());

        if (orderIds.isEmpty()) {
            log.info("[自动分账] 所有就绪订单已有分账记录, 跳过");
            return;
        }

        log.info("[自动分账] 扫描到 {} 个待自动分账订单", orderIds.size());
        try {
            profitSharingOrderService.submitBatchProfitSharing(orderIds, ProfitSharingTriggerType.AUTO.getCode(), null);
            log.info("[自动分账] 批量分账完成, 提交数量={}", orderIds.size());
        } catch (Exception e) {
            log.error("[自动分账] 批量分账异常: {}", e.getMessage(), e);
        }
    }

    @Override
    public void executeStatusCompensation() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, -10);
        Date before = cal.getTime();

        List<ProfitSharingOrder> processingOrders = profitSharingOrderService.lambdaQuery()
                .eq(ProfitSharingOrder::getStatus, ProfitSharingStatus.PROCESSING.getCode())
                .le(ProfitSharingOrder::getCreateTime, before)
                .list();
        if (processingOrders.isEmpty()) {
            log.info("[状态补偿] 无需补偿的 PROCESSING 分账单");
            return;
        }

        log.info("[状态补偿] 扫描到 {} 个 PROCESSING 分账单需要补偿查询", processingOrders.size());
        for (ProfitSharingOrder order : processingOrders) {
            try {
                if (StrUtil.isBlank(order.getOutOrderNo()) || StrUtil.isBlank(order.getTransactionId())) {
                    continue;
                }
                if (StrUtil.isBlank(order.getSubMchId())) {
                    log.warn("[状态补偿] 分账单[{}]缺少二级商户号，跳过补偿查询", order.getId());
                    continue;
                }
                ProfitSharingOrderResult result = profitSharingClientRegistry.get(channelOf(order))
                        .queryOrder(order.getSubMchId(), order.getOutOrderNo(), order.getTransactionId());
                if ("FINISHED".equals(result.getStatus()) || "SUCCESS".equals(result.getStatus())) {
                    profitSharingOrderService.handleNotify(order.getOutOrderNo(), result.getStatus(), result.getOrderId());
                    continue;
                } else if ("FAILED".equals(result.getStatus()) || "FAIL".equals(result.getStatus())) {
                    order.setStatus(ProfitSharingStatus.FAILED.getCode());
                    order.setResultDesc("状态补偿失败: " + result.getDescription());
                } else {
                    continue;
                }
                order.setModifyTime(new Date());
                profitSharingOrderService.update(order, new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProfitSharingOrder>()
                        .eq(ProfitSharingOrder::getId, order.getId())
                        .eq(ProfitSharingOrder::getStatus, ProfitSharingStatus.PROCESSING.getCode()));
            } catch (Exception e) {
                log.error("[状态补偿] 查询分账单[{}]状态失败: {}", order.getId(), e.getMessage(), e);
            }
        }
    }

    @Override
    public void executeFinishCompensation() {
        List<ProfitSharingOrder> unfinishedOrders = profitSharingOrderService.lambdaQuery()
                .eq(ProfitSharingOrder::getStatus, ProfitSharingStatus.SUCCESS.getCode())
                .eq(ProfitSharingOrder::getFinishStatus, ProfitSharingFinishStatus.UNFINISHED.getCode())
                .list();
        if (unfinishedOrders.isEmpty()) {
            log.info("[完结补偿] 无需完结的分账单");
            return;
        }

        log.info("[完结补偿] 扫描到 {} 个未完结的成功分账单", unfinishedOrders.size());
        for (ProfitSharingOrder order : unfinishedOrders) {
            try {
                if (StrUtil.isBlank(order.getTransactionId())) {
                    continue;
                }
                FinishProfitSharingRequest request = new FinishProfitSharingRequest();
                request.setSubMchId(order.getSubMchId());
                request.setTransactionId(order.getTransactionId());
                request.setOutOrderNo(order.getOutOrderNo());
                request.setDescription("系统自动完结");
                profitSharingClientRegistry.get(channelOf(order)).finishOrder(request);

                order.setFinishStatus(ProfitSharingFinishStatus.FINISHED.getCode());
                order.setFinishTime(new Date());
                order.setModifyTime(new Date());
                profitSharingOrderService.updateById(order);
            } catch (Exception e) {
                log.error("[完结补偿] 分账单[{}]完结失败: {}", order.getId(), e.getMessage(), e);
            }
        }
    }

    @Override
    public void executeTimeoutWarning() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -150);
        Date warningBefore = cal.getTime();

        List<ProfitSharingOrder> timeoutOrders = profitSharingOrderService.lambdaQuery()
                .eq(ProfitSharingOrder::getStatus, ProfitSharingStatus.PENDING.getCode())
                .le(ProfitSharingOrder::getCreateTime, warningBefore)
                .list();
        if (timeoutOrders.isEmpty()) {
            log.info("[超时预警] 无超时分账单");
            return;
        }
        log.warn("[超时预警] 发现 {} 个超过150天仍未完成的分账单（微信180天冻结上限）", timeoutOrders.size());
        for (ProfitSharingOrder order : timeoutOrders) {
            long daysPending = (System.currentTimeMillis() - order.getCreateTime().getTime()) / (1000L * 3600 * 24);
            log.warn("[超时预警] id={}, outOrderNo={}, shopId={}, status={}, 已挂起{}天, splitAmount={}",
                    order.getId(), order.getOutOrderNo(), order.getShopId(), order.getStatus(), daysPending, order.getSplitAmount());
        }
    }

    @Override
    public void executeRetryFailedProfitSharing() {
        List<ProfitSharingOrder> failedOrders = profitSharingOrderService.lambdaQuery()
                .eq(ProfitSharingOrder::getStatus, ProfitSharingStatus.FAILED.getCode())
                .list();
        List<ProfitSharingOrder> retryable = failedOrders.stream()
                .filter(o -> (o.getRetryCount() == null ? 0 : o.getRetryCount()) < MAX_RETRY)
                .filter(o -> StrUtil.isNotBlank(o.getTransactionId()) && StrUtil.isNotBlank(o.getSubMchId()))
                .collect(Collectors.toList());
        if (retryable.isEmpty()) {
            log.info("[失败重试] 无可重试的分账单");
            return;
        }

        log.info("[失败重试] 扫描到 {} 个可重试的失败分账单", retryable.size());
        for (ProfitSharingOrder order : retryable) {
            try {
                profitSharingOrderService.retryProfitSharing(order.getId());
            } catch (Exception e) {
                log.error("[失败重试] 分账单[{}]重试异常: {}", order.getId(), e.getMessage(), e);
            }
        }
    }

    @Override
    public void executeMerchantApplicationPolling() {
        List<MerchantApplication> pendingList = merchantApplicationService.lambdaQuery()
                .notIn(MerchantApplication::getStatus,
                        MerchantApplicationStatus.FINISH.getCode(), MerchantApplicationStatus.REJECTED.getCode())
                .isNotNull(MerchantApplication::getApplymentId)
                .ne(MerchantApplication::getApplymentId, "")
                .list();
        if (pendingList.isEmpty()) {
            log.info("[进件轮询] 无待处理进件");
            return;
        }
        log.info("[进件轮询] 扫描到 {} 条待处理进件", pendingList.size());
        for (MerchantApplication app : pendingList) {
            try {
                merchantApplicationService.refresh(app.getId());
            } catch (Exception e) {
                log.error("[进件轮询] 刷新进件状态失败: applymentId={}", app.getApplymentId(), e);
            }
        }
    }

    @Override
    public void executeWithdrawStatusPolling() {
        List<ShopWithdraw> onlineWithdraws = shopWithdrawMapper.selectList(new LambdaQueryWrapper<ShopWithdraw>()
                .eq(ShopWithdraw::getWithdrawType, 2)
                .eq(ShopWithdraw::getStatus, 1)
                .in(ShopWithdraw::getChannelStatus, "CREATE_SUCCESS", "PROCESSING", "SUBMITTING", "UNKNOWN")
                // 不可自动查询的历史支付宝/缺渠道号汇付单不能占满扫描窗口。
                .and(w -> w.and(wechat -> wechat.eq(ShopWithdraw::getPayChannel, PAY_CHANNEL_WECHAT)
                                .or().isNull(ShopWithdraw::getPayChannel).or().eq(ShopWithdraw::getPayChannel, ""))
                        .or(huifu -> huifu.eq(ShopWithdraw::getPayChannel, "HUIFU")
                                .isNotNull(ShopWithdraw::getChannelWithdrawId).ne(ShopWithdraw::getChannelWithdrawId, "")))
                .last("limit 200"));
        if (onlineWithdraws.isEmpty()) {
            log.info("[提现查询] 无待查询的线上提现");
            return;
        }
        log.info("[提现查询] 扫描到 {} 条待查询线上提现", onlineWithdraws.size());
        for (ShopWithdraw withdraw : onlineWithdraws) {
            try {
                String channel = StrUtil.blankToDefault(withdraw.getPayChannel(), PAY_CHANNEL_WECHAT);
                if ("ALIPAY".equalsIgnoreCase(channel)) {
                    // 历史支付宝实现为平台转账，不能作为子商户提现自动确认或再次出金。
                    continue;
                }
                // 新申请用快照；历史缺少快照才按旧方式查绑定，终态仍保留需核账提示。
                String subMchId = StrUtil.isNotBlank(withdraw.getChannelSubMchId()) ? withdraw.getChannelSubMchId()
                        : resolveSubMchId(withdraw.getShopId(), channel);
                if (PAY_CHANNEL_WECHAT.equalsIgnoreCase(channel) && StrUtil.isBlank(subMchId)) {
                    log.warn("[提现查询] 店铺[{}]无激活的微信二级商户，跳过, withdrawSn={}",
                            withdraw.getShopId(), withdraw.getWithdrawSn());
                    continue;
                }
                // 提现查单第二参语义按渠道不同：
                // - 微信电商查单只支持商户单号 out_request_no（创建时为 "SW"+withdrawSn），不能用 channelWithdrawId（微信 withdraw_id）
                // - 汇付/支付宝用渠道返回并回写的 channelWithdrawId 定位（汇付 hf_seq_id / 支付宝 out_biz_no）
                if (!PAY_CHANNEL_WECHAT.equalsIgnoreCase(channel) && StrUtil.isBlank(withdraw.getChannelWithdrawId())) {
                    log.warn("[提现查询] 渠道单号未知，需渠道人工核账，禁止重发: withdrawSn={}", withdraw.getWithdrawSn());
                    continue;
                }
                String withdrawLocator = PAY_CHANNEL_WECHAT.equalsIgnoreCase(channel)
                        ? "SW" + withdraw.getWithdrawSn()
                        : withdraw.getChannelWithdrawId();
                WithdrawResult result = profitSharingClientRegistry.get(channel)
                        .queryWithdraw(subMchId, withdrawLocator);
                shopWithdrawService.applyOnlineWithdrawResult(withdraw.getShopWithdrawLogId(), result);
            } catch (Exception e) {
                log.error("[提现查询] 查询线上提现状态失败: withdrawSn={}", withdraw.getWithdrawSn(), e);
            }
        }
    }

    @Override
    public void executeReturnOrderCompensation() {
        List<ProfitSharingReturnOrder> processingList = profitSharingReturnOrderMapper.selectList(
                new LambdaQueryWrapper<ProfitSharingReturnOrder>().eq(ProfitSharingReturnOrder::getStatus, 1));
        if (processingList.isEmpty()) {
            log.info("[回退补偿] 无需补偿的回退单");
            return;
        }
        log.info("[回退补偿] 扫描到 {} 条处理中的回退单", processingList.size());
        for (ProfitSharingReturnOrder returnOrder : processingList) {
            try {
                if (StrUtil.isBlank(returnOrder.getSubMchId())) {
                    log.warn("[回退补偿] 回退单[{}]缺少二级商户号，跳过补偿查询", returnOrder.getOutReturnNo());
                    continue;
                }
                ProfitSharingReturnResult result = profitSharingClientRegistry.get(channelOf(returnOrder)).queryReturnOrder(
                        returnOrder.getSubMchId(), returnOrder.getOutOrderNo(), returnOrder.getOutReturnNo());
                if ("SUCCESS".equals(result.getStatus())) {
                    ProfitSharingOrder original = profitSharingOrderService.getById(returnOrder.getProfitSharingOrderId());
                    if (original == null || original.getShopId() == null) {
                        throw new IllegalStateException("回退单缺少原分账店铺，不能确认入账");
                    }
                    // 先幂等记账，后推进成功；写状态失败时，下轮仍可安全补偿。
                    accountService.recordOnlineChange(original.getShopId().intValue(), channelOf(returnOrder),
                            "PS_RETURN:" + returnOrder.getId(), returnOrder.getReturnAmount(),
                            "线上分账回退：" + returnOrder.getOutReturnNo(), System.currentTimeMillis() / 1000);
                    returnOrder.setStatus(2);
                    returnOrder.setChannelReturnId(result.getReturnId());
                    returnOrder.setResultDesc("回退成功(补偿)");
                    returnOrder.setModifyTime(new Date());
                    profitSharingReturnOrderMapper.updateById(returnOrder);
                } else if ("FAILED".equalsIgnoreCase(result.getStatus()) || "FAIL".equalsIgnoreCase(result.getStatus())) {
                    returnOrder.setStatus(3);
                    returnOrder.setResultDesc("回退失败(补偿): " + result.getDescription());
                    returnOrder.setModifyTime(new Date());
                    profitSharingReturnOrderMapper.updateById(returnOrder);
                }
            } catch (Exception e) {
                log.error("[回退补偿] 查询回退单状态失败: outReturnNo={}", returnOrder.getOutReturnNo(), e);
            }
        }
    }

    /**
     * 按店铺+渠道现查激活中的二级商户号，避免快照到业务表（零 DDL）
     */
    private String resolveSubMchId(Integer shopId, String channel) {
        if (shopId == null || shopId <= 0) {
            return null;
        }
        SubMerchant subMerchant = subMerchantService.getActiveByShop(
                shopId.longValue(), StrUtil.blankToDefault(channel, PAY_CHANNEL_WECHAT));
        return subMerchant == null ? null : subMerchant.getSubMchId();
    }

    private Integer parseInt(String val, int defaultVal) {
        if (StrUtil.isBlank(val)) {
            return defaultVal;
        }
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private String channelOf(ProfitSharingOrder order) {
        return StrUtil.blankToDefault(order.getPayChannel(), PAY_CHANNEL_WECHAT);
    }

    private String channelOf(ProfitSharingReturnOrder returnOrder) {
        return StrUtil.blankToDefault(returnOrder.getPayChannel(), PAY_CHANNEL_WECHAT);
    }
}
