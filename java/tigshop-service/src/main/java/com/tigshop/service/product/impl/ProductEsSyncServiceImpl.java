// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.product.impl;

import com.tigshop.bean.event.ProductEsSyncEvent;
import com.tigshop.service.product.ProductEsSyncService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 产品ES同步服务实现
 *
 * <p>仅负责发布 ES 同步事件，由 ProductEsSyncEventListener 在事务
 * AFTER_COMMIT 阶段真正向 RabbitMQ 投递消息。这样可保证消费端读库时
 * 业务事务已经提交，避免读取到 null 或旧数据导致的同步失败。</p>
 *
 * @author Tigshop团队
 * @create 2025年02月12日 15:00
 */
@Slf4j
@Service
public class ProductEsSyncServiceImpl implements ProductEsSyncService {

    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void syncProductAsync(Long productId) {
        applicationEventPublisher.publishEvent(ProductEsSyncEvent.single(productId));
        log.debug("发布单个产品ES同步事件: {}", productId);
    }

    @Override
    public void batchSyncProductsAsync(List<Long> productIds) {
        applicationEventPublisher.publishEvent(ProductEsSyncEvent.batch(productIds));
        log.debug("发布批量产品ES同步事件: {}", productIds == null ? 0 : productIds.size());
    }

    @Override
    public void deleteProductAsync(Long productId) {
        applicationEventPublisher.publishEvent(ProductEsSyncEvent.delete(productId));
        log.debug("发布产品ES删除事件: {}", productId);
    }
}
