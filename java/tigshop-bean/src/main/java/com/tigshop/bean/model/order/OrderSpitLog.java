// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.model.order;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 订单拆分model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@TableName("order_split_log")
@Schema(description = "订单拆分")
public class OrderSpitLog implements Serializable {
    /**
     * 拆份订单
     */
    @TableId(type = IdType.AUTO)
    private Integer logId;

    /**
     * 父订单id
     */
    private Integer parentOrderId;

    /**
     * 拆分后的订单id
     */
    private Integer orderId;

    /**
     * 拆分时间
     */
    private Long splitTime;

    /**
     * 父订单信息
     */
    private String parentOrderData;
}