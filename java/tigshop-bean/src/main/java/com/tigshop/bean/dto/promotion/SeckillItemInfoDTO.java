// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.promotion;

import com.tigshop.bean.model.promotion.Seckill;
import com.tigshop.bean.model.promotion.SeckillItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品秒杀信息
 *
 * @author Tigshop团队
 * @create 2025年02月12日 13:28
 */
@Data
public class SeckillItemInfoDTO {

    // *** Seckill ***

    @Schema(description = "秒杀活动ID")
    private Integer seckillId;

    @Schema(description = "活动名称")
    private String seckillName;

    @Schema(description = "起始时间")
    private Long seckillStartTime;

    @Schema(description = "结束时间")
    private Long seckillEndTime;

    @Schema(description = "限购数量")
    private Integer seckillLimitNum;

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "店铺ID，平台为0")
    private Integer shopId;

    // *** SeckillItem ***

    @Schema(description = "活动商品ID")
    private Integer recId;

    @Schema(description = "规格id")
    private Integer skuId;

    @Schema(description = "秒杀价格")
    private BigDecimal seckillPrice;

    @Schema(description = "活动商品库存")
    private Integer seckillStock;

    @Schema(description = "活动销量")
    private Integer seckillSales;

    @Schema(description = "当前用户是否可参与（二期会员标签人群；false 时前台展示秒杀价但置灰「抢购」按钮）")
    private Boolean canJoin = true;

    @Schema(description = "不可参与提示文案，可参与时为空")
    private String restrictTip;

    public SeckillItemInfoDTO(Seckill seckill, SeckillItem item) {
        this.seckillId = seckill.getSeckillId();
        this.seckillName = seckill.getSeckillName();
        this.seckillStartTime = seckill.getSeckillStartTime();
        this.seckillEndTime = seckill.getSeckillEndTime();
        this.seckillLimitNum = seckill.getSeckillLimitNum();
        this.productId = seckill.getProductId();
        this.shopId = seckill.getShopId();

        this.recId = item.getRecId();
        this.skuId = item.getSkuId();
        this.seckillPrice = item.getSeckillPrice();
        this.seckillStock = item.getSeckillStock();
        this.seckillSales = item.getSeckillSales();
    }
}