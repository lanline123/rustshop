// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.model.product;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 库存日志model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("product_inventory_log")
@Schema(description = "库存日志")
public class ProductInventoryLog implements Serializable {

    @Schema(description = "库存日志ID")
    @TableId(type = IdType.AUTO)
    private Integer logId;

    @Schema(description = "商品id")
    private Integer productId;

    @Schema(description = "货品ID")
    private Integer specId;

    @Schema(description = "剩余库存")
    private Integer number;

    @Schema(description = "修改时间")
    private Long addTime;

    @Schema(description = "原始库存量")
    private Integer oldNumber;

    @Schema(description = "1增2减")
    private Integer type;

    @Schema(description = "变化量")
    private Integer changeNumber;

    @TableField(value = "`desc`")
    @Schema(description = "描述")
    private String desc;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}