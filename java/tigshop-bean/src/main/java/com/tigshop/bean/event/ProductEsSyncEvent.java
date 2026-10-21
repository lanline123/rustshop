// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 商品 ES 同步事件
 *
 * <p>由 ProductEsSyncService 在事务内发布，由 ProductEsSyncEventListener 在事务
 * AFTER_COMMIT 阶段消费并真正向 RabbitMQ 发送同步任务，避免事务未提交时
 * MQ 消费端读到 null/旧数据导致 ES 同步失败。</p>
 *
 * @author Tigshop团队
 * @create 2026/05/18
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductEsSyncEvent {

    /**
     * 事件类型
     */
    private Type type;

    /**
     * 单个产品 ID（type=SINGLE_SYNC / DELETE 时使用）
     */
    private Long productId;

    /**
     * 产品 ID 列表（type=BATCH_SYNC 时使用）
     */
    private List<Long> productIds;

    public static ProductEsSyncEvent single(Long productId) {
        return new ProductEsSyncEvent(Type.SINGLE_SYNC, productId, null);
    }

    public static ProductEsSyncEvent delete(Long productId) {
        return new ProductEsSyncEvent(Type.DELETE, productId, null);
    }

    public static ProductEsSyncEvent batch(List<Long> productIds) {
        return new ProductEsSyncEvent(Type.BATCH_SYNC, null, productIds);
    }

    public enum Type {
        /** 单个商品同步 */
        SINGLE_SYNC,
        /** 批量商品同步 */
        BATCH_SYNC,
        /** 单个商品从 ES 删除 */
        DELETE
    }
}
