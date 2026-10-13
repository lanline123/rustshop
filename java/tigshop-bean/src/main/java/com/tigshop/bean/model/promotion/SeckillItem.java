/**
 * ---------------------------------------------------------------------+
 * 文件 -- eckillItem
 * ---------------------------------------------------------------------+
 * 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
 * ---------------------------------------------------------------------+
 * 作者：Tigshop团队，yq@tigshop.com
 * ---------------------------------------------------------------------+
 * 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
 * ---------------------------------------------------------------------+
 */
package com.tigshop.bean.model.promotion;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Tigshop团队
 */
@Data
@TableName("seckill_item")
@Schema(description = "秒杀商品信息")
public class SeckillItem {

    @TableId(value = "rec_id", type = IdType.AUTO)
    @Schema(description = "活动商品ID")
    private Integer recId;

    @Schema(description = "关联活动id")
    private Integer seckillId;

    @Schema(description = "商品id")
    private Integer productId;

    @Schema(description = "规格id")
    private Integer skuId;

    @Schema(description = "秒杀价格")
    private BigDecimal seckillPrice;

    @Schema(description = "活动商品库存")
    private Integer seckillStock;

    @Schema(description = "活动销量")
    private Integer seckillSales;

    @Schema(description = "开始时间")
    private Long seckillStartTime;

    @Schema(description = "结束时间")
    private Long seckillEndTime;
}
