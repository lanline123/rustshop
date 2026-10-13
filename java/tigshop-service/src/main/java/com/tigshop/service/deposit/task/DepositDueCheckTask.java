package com.tigshop.service.deposit.task;

import com.tigshop.common.utils.RedisCache;
import com.tigshop.service.deposit.DepositLifecycleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 保证金双缓冲到期扫描定时任务（W5）。
 *
 * <p>每天 02:00 执行 {@link DepositLifecycleService#runDueCheck()}：扫描所有保证金账户，
 * 对待补缴且双缓冲到期的按配置 action 处罚，对已补足的解除处罚并恢复商品。
 * 集群环境用 RedisCache 分布式锁保证仅单节点执行（仿 CreditUserPeriodUpdateTask）。</p>
 *
 * @author Tigshop团队
 * @create 2026-06-10
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DepositDueCheckTask {

    private final DepositLifecycleService depositLifecycleService;
    private final RedisCache redisCache;

    /**
     * 分布式锁Key
     */
    private static final String LOCK_KEY = "deposit_due_check_task_lock";

    /**
     * 锁超时时间（秒）
     */
    private static final int LOCK_TIMEOUT_SECONDS = 1800;

    /**
     * 每天2点执行
     * cron表达式：秒 分 时 日 月 周
     * 0 0 2 * * ? 表示每天2点0分0秒执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void execute() {
        String uuid = UUID.randomUUID().toString();

        // 尝试获取分布式锁，保证集群环境只有一个节点执行
        boolean locked = redisCache.tryLock(LOCK_KEY, uuid, LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (!locked) {
            log.info("保证金到期扫描任务：其他节点正在执行，跳过本次执行");
            return;
        }

        try {
            log.info("========== 开始执行保证金双缓冲到期扫描任务 ==========");
            depositLifecycleService.runDueCheck();
            log.info("========== 保证金双缓冲到期扫描任务执行完成 ==========");
        } catch (Exception e) {
            log.error("保证金双缓冲到期扫描任务执行异常", e);
        } finally {
            // 释放分布式锁
            redisCache.unlock(LOCK_KEY, uuid);
        }
    }

    /**
     * 手动触发执行（用于测试或管理后台手动触发）。
     */
    public void manualExecute() {
        log.info("手动触发保证金双缓冲到期扫描任务");
        depositLifecycleService.runDueCheck();
    }
}
