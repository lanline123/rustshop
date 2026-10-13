package com.tigshop.service.content.task;

import com.tigshop.service.content.WechatLiveOrderSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 微信直播订单同步任务
 *
 * @author Tigshop团队
 * @create 2026年03月08日 16:56
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WechatLiveOrderSyncTask {

    private final WechatLiveOrderSyncService wechatLiveOrderSyncService;

    @Scheduled(cron = "0 */15 * * * ?")
    public void processPendingLogs() {
        wechatLiveOrderSyncService.processPendingLogs();
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void reconcileLiveOrders() {
        wechatLiveOrderSyncService.reconcileLiveOrders();
    }
}
