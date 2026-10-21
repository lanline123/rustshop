// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.service.product.listener;

import com.tigshop.bean.dto.job.BatchSyncRequest;
import com.tigshop.bean.dto.job.SingleSyncRequest;
import com.tigshop.bean.event.ProductEsSyncEvent;
import com.tigshop.common.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 商品 ES 同步事件监听器
 *
 * <p>在事务 AFTER_COMMIT 阶段才向 RabbitMQ 投递同步消息，
 * 保证消费端读库时数据已经真正可见，避免事务尚未提交即同步导致的失败。</p>
 *
 * <p>fallbackExecution=true 保证非事务上下文（脚本 / Job / 后台命令）
 * 也能正常触发 MQ 投递。</p>
 *
 * @author Tigshop团队
 * @create 2026/05/18
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEsSyncEventListener {

    private final RabbitTemplate rabbitTemplate;

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT,
            fallbackExecution = true
    )
    public void handle(ProductEsSyncEvent event) {
        if (event == null || event.getType() == null) {
            return;
        }
        switch (event.getType()) {
            case SINGLE_SYNC -> sendSingle(event.getProductId(), false);
            case DELETE -> sendSingle(event.getProductId(), true);
            case BATCH_SYNC -> sendBatch(event);
            default -> log.warn("未知的 ES 同步事件类型: {}", event.getType());
        }
    }

    private void sendSingle(Long productId, boolean delete) {
        if (productId == null) {
            log.warn("ES 同步事件缺少 productId, delete={}", delete);
            return;
        }
        SingleSyncRequest request = new SingleSyncRequest();
        request.setProductId(productId);
        request.setDelete(delete);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.DIRECT_EXCHANGE,
                RabbitMQConfig.SINGLE_SYNC_ROUTING_KEY,
                request
        );
        if (delete) {
            log.info("发送产品ES删除任务: {}", productId);
        } else {
            log.info("发送单个产品ES同步任务: {}", productId);
        }
    }

    private void sendBatch(ProductEsSyncEvent event) {
        if (event.getProductIds() == null || event.getProductIds().isEmpty()) {
            return;
        }
        BatchSyncRequest request = new BatchSyncRequest();
        request.setProductIds(event.getProductIds());
        request.setBatchSize(100);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.DIRECT_EXCHANGE,
                RabbitMQConfig.BATCH_SYNC_ROUTING_KEY,
                request
        );
        log.info("发送批量产品ES同步任务: {}", event.getProductIds().size());
    }
}
