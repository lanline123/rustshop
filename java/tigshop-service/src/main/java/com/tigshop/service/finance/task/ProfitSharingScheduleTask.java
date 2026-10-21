package com.tigshop.service.finance.task;

import com.tigshop.common.utils.RedisCache;
import com.tigshop.service.finance.ProfitSharingScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 微信线上分账定时任务调度
 *
 * <p>集群环境使用 Redis 分布式锁保证只执行一次。</p>
 *
 * @author Tigshop团队
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProfitSharingScheduleTask {

    private static final int LOCK_TIMEOUT_SECONDS = 300;

    private final ProfitSharingScheduleService profitSharingScheduleService;
    private final RedisCache redisCache;

    /** 自动分账：每小时第5分钟 */
    @Scheduled(cron = "0 5 * * * ?")
    public void autoProfitSharing() {
        runWithLock("ps_auto_profit_sharing_lock", s -> s.executeAutoProfitSharing());
    }

    /** 状态补偿：每5分钟 */
    @Scheduled(cron = "0 */5 * * * ?")
    public void statusCompensation() {
        runWithLock("ps_status_compensation_lock", s -> s.executeStatusCompensation());
    }

    /** 完结补偿：每30分钟 */
    @Scheduled(cron = "0 */30 * * * ?")
    public void finishCompensation() {
        runWithLock("ps_finish_compensation_lock", s -> s.executeFinishCompensation());
    }

    /** 失败重试：每30分钟 */
    @Scheduled(cron = "0 15,45 * * * ?")
    public void retryFailed() {
        runWithLock("ps_retry_failed_lock", s -> s.executeRetryFailedProfitSharing());
    }

    /** 超时预警：每天凌晨1点 */
    @Scheduled(cron = "0 0 1 * * ?")
    public void timeoutWarning() {
        runWithLock("ps_timeout_warning_lock", s -> s.executeTimeoutWarning());
    }

    /** 进件状态轮询：每10分钟 */
    @Scheduled(cron = "0 */10 * * * ?")
    public void merchantApplicationPolling() {
        runWithLock("ps_merchant_application_polling_lock", s -> s.executeMerchantApplicationPolling());
    }

    /** 线上提现状态轮询：每10分钟 */
    @Scheduled(cron = "0 */10 * * * ?")
    public void withdrawStatusPolling() {
        runWithLock("ps_withdraw_status_polling_lock", s -> s.executeWithdrawStatusPolling());
    }

    /** 回退单补偿：每10分钟 */
    @Scheduled(cron = "0 */10 * * * ?")
    public void returnOrderCompensation() {
        runWithLock("ps_return_order_compensation_lock", s -> s.executeReturnOrderCompensation());
    }

    private void runWithLock(String lockKey, Consumer<ProfitSharingScheduleService> action) {
        String uuid = UUID.randomUUID().toString();
        boolean locked = redisCache.tryLock(lockKey, uuid, LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (!locked) {
            return;
        }
        try {
            action.accept(profitSharingScheduleService);
        } catch (Exception e) {
            log.error("分账定时任务[{}]执行异常", lockKey, e);
        } finally {
            redisCache.unlock(lockKey, uuid);
        }
    }
}
