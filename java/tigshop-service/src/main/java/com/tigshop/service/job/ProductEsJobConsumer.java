// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.job;

import com.tigshop.bean.dto.job.BatchSyncRequest;
import com.tigshop.bean.dto.job.SingleSyncRequest;
import com.tigshop.common.config.RabbitMQConfig;
import com.tigshop.service.product.ProductEsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 产品ES队列任务消费者
 *
 * @author Tigshop团队
 * @create 2025年02月12日 15:00
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductEsJobConsumer {

    private final ProductEsService productEsService;

    /**
     * 批量同步任务
     */
    @RabbitListener(
            queues = RabbitMQConfig.BATCH_SYNC,
            containerFactory = "productEsRabbitListenerContainerFactory"
    )
    public void handleBatchSync(BatchSyncRequest request) {
        log.info("开始执行批量同步任务: {}", request);

        // 检查是否需要初始化索引
        if (Boolean.TRUE.equals(request.getInit())) {
            requireSuccess(productEsService.initIndex(), "初始化商品ES索引");
        }

        Map<String, Object> result;
        if (request.getProductIds() != null && !request.getProductIds().isEmpty()) {
            // 指定产品ID的批量同步
            result = productEsService.batchSyncProducts(request.getProductIds(), request.getBatchSize());
        } else {
            // 全量流式同步
            result = productEsService.syncAllProductsStream(request.getBatchSize());
        }
        requireSuccess(result, "批量同步商品到ES");

        log.info("批量同步任务执行完成: {}", request);
    }

    /**
     * 单个产品同步任务
     */
    @RabbitListener(
            queues = RabbitMQConfig.SINGLE_SYNC,
            containerFactory = "productEsRabbitListenerContainerFactory"
    )
    public void handleSingleSync(SingleSyncRequest request) {
        log.info("开始执行单个产品同步任务: {}", request);

        if (Boolean.TRUE.equals(request.getDelete())) {
            // 删除操作
            requireSuccess(productEsService.deleteProduct(request.getProductId()), "从ES删除商品");
            log.info("产品删除任务执行完成: {}", request.getProductId());
        } else {
            // 同步操作
            requireSuccess(productEsService.syncProduct(request.getProductId()), "同步单个商品到ES");
            log.info("单个产品同步任务执行完成: {}", request);
        }
    }

    /**
     * ES 服务使用结果对象表达业务失败，消费者必须把失败重新抛给 Rabbit 监听容器，
     * 否则消息会被正常确认并永久丢失重试机会。
     */
    private void requireSuccess(Map<String, Object> result, String operation) {
        boolean success = result != null && Boolean.TRUE.equals(result.get("success"));
        int errorCount = result != null && result.get("error_count") instanceof Number number
                ? number.intValue() : 0;
        if (success && errorCount == 0) {
            return;
        }

        String message = result == null ? "未返回同步结果" : String.valueOf(result.get("message"));
        String detail = errorCount > 0 ? ", error_count=" + errorCount + ", errors=" + result.get("errors") : "";
        throw new IllegalStateException(operation + "失败: " + message + detail);
    }
}
