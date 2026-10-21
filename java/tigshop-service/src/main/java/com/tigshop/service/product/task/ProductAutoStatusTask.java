// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.service.product.task;

import com.tigshop.common.properties.ProductAutoStatusProperties;
import com.tigshop.service.product.ProductAutoStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 商品自动上下架定时任务
 *
 * @author Tigshop团队
 * @create 2025年12月2日
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductAutoStatusTask implements ApplicationRunner {

    private final ProductAutoStatusService productAutoStatusService;
    private final ProductAutoStatusProperties properties;

    /**
     * 延迟线程池
     */
    private ScheduledExecutorService scheduler;

    @Override
    public void run(ApplicationArguments args) {
        // 检查是否启用定时任务
        if (!properties.getEnabled()) {
            log.info("商品自动上下架定时任务已禁用");
            return;
        }
        // 初始化定时任务
        startScheduledTask();
    }

    /**
     * 启动定时任务
     */
    public void startScheduledTask() {
        if (scheduler == null || scheduler.isShutdown()) {
            scheduler = Executors.newScheduledThreadPool(1, r -> {
                Thread thread = new Thread(r);
                thread.setName("product-auto-status-task");
                thread.setDaemon(true);
                return thread;
            });

            // 延迟后开始执行，之后每隔指定时间执行一次
            scheduler.scheduleAtFixedRate(() -> {
                try {
                    // 检查调度器是否已关闭，避免在 shutdown 时执行
                    if (scheduler != null && !scheduler.isShutdown()) {
                        productAutoStatusService.executeAutoStatusTasks();
                    }
                } catch (Exception e) {

                }
            }, properties.getInitialDelaySeconds(), properties.getIntervalSeconds(), TimeUnit.SECONDS);

        }
    }

    /**
     * 停止定时任务
     */
    @PreDestroy
    public void stopScheduledTask() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
                log.info("商品自动上下架定时任务已停止");
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}

