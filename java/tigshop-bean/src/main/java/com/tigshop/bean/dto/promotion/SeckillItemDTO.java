/**
 * ---------------------------------------------------------------------+
 * 文件 -- SeckillItemDTO
 * ---------------------------------------------------------------------+
 * 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
 * ---------------------------------------------------------------------+
 * 作者：Tigshop团队，yq@tigshop.com
 * ---------------------------------------------------------------------+
 * 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
 * ---------------------------------------------------------------------+
 */
package com.tigshop.bean.dto.promotion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Tigshop团队
 */
@Data
@Schema(description = "秒杀活动商品信息")
public class SeckillItemDTO {

    @Schema(description = "秒杀活动商品ID")
    private Integer recId;

    @Schema(description = "规格ID")
    private Integer skuId;

    @Schema(description = "秒杀价格")
    private BigDecimal seckillPrice;

    @Schema(description = "秒杀商品库存")
    private Integer seckillStock;

    @Schema(description = "是否参与秒杀")
    private Integer secondsSeckill;

}
