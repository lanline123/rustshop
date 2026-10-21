// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 商品自动上下架配置属性
 *
 * @author Tigshop团队
 * @create 2025年12月2日
 */
@Data
@Component
@ConfigurationProperties(prefix = "product.auto-status")
public class ProductAutoStatusProperties {

    /**
     * 定时任务执行间隔（秒），默认30秒
     */
    private Integer intervalSeconds = 30;

    /**
     * 定时任务初始延迟（秒），默认10秒
     */
    private Integer initialDelaySeconds = 10;

    /**
     * 是否启用定时任务，默认启用
     */
    private Boolean enabled = true;

    /**
     * 每批次执行的任务数量，默认10
     */
    private Integer batchSize = 10;

    /**
     * 当前批次最大重试次数
     */
    private Integer curBatchMaxRetryCount = 3;

    /**
     * 最大重试次数
     */
    private Integer maxRetryCount = 4;
}

