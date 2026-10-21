// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.service.finance.task;

import com.tigshop.common.utils.RedisCache;
import com.tigshop.service.finance.CreditUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 授信会员结算周期更新定时任务
 * 每天0点执行，更新所有授信会员的periodStartTime字段
 * 用于正确计算账期订单的逾期状态
 *
 * @author Tigshop团队
 * @create 2026年01月29日
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreditUserPeriodUpdateTask {

    private final CreditUserService creditUserService;
    private final RedisCache redisCache;

    /**
     * 分布式锁Key
     */
    private static final String LOCK_KEY = "credit_user_period_update_task_lock";

    /**
     * 锁超时时间（秒）
     */
    private static final int LOCK_TIMEOUT_SECONDS = 300;

    /**
     * 每天0点执行
     * cron表达式：秒 分 时 日 月 周
     * 0 0 0 * * ? 表示每天0点0分0秒执行
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void execute() {
        String uuid = UUID.randomUUID().toString();

        // 尝试获取分布式锁，保证集群环境只有一个节点执行
        boolean locked = redisCache.tryLock(LOCK_KEY, uuid, LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (!locked) {
            log.info("授信会员周期更新任务：其他节点正在执行，跳过本次执行");
            return;
        }

        try {
            log.info("========== 开始执行授信会员周期更新任务 ==========");
            creditUserService.updateAllPeriodStartTime();
            log.info("========== 授信会员周期更新任务执行完成 ==========");
        } catch (Exception e) {
            log.error("授信会员周期更新任务执行异常", e);
        } finally {
            // 释放分布式锁
            redisCache.unlock(LOCK_KEY, uuid);
        }
    }

    /**
     * 手动触发执行（用于测试或管理后台手动触发）
     */
    public void manualExecute() {
        log.info("手动触发授信会员周期更新任务");
        creditUserService.updateAllPeriodStartTime();
    }
}
