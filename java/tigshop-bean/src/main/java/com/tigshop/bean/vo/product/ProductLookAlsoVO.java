package com.tigshop.bean.vo.product;

import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * @author Tigshop项目组
 * @create 2025年05月20日 16:17
 */
@Data
@Builder
public class ProductLookAlsoVO {
    @Schema(description = "市场价")
    private String marketPrice;

    @Schema(description = "缩略图")
    private String picThumb;

    @Schema(description = "图片")
    private String picUrl;

    @Schema(description = "最终价格")
    private String price;

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "商品名称")
    @JsonTranslate(dataType = 2)
    private String productName;

    @Schema(description = "商品编号")
    private String productSn;

    @Schema(description = "店铺 ID（用于平台商品判定，仅批发商可见时回填）")
    private Integer shopId;

    @Schema(description = "首条批发价（仅批发商身份 + 平台商品命中时返回，BigDecimal 序列化为字符串）")
    private java.math.BigDecimal firstWholesalePrice;

    @Schema(description = "卡片可直接加购的起批数（CR-20260603-1720 / CD-035）；4 条命中规则全 true 才返回数字，否则 null")
    private Integer wholesaleCardMoq;

    @Schema(description = "起批维度（CR-20260613-0943）：1=SPU 总量起批 / 2=SKU 单独起批；与 wholesaleCardMoq 同范围回填，否则 null")
    private Integer wholesaleMoqDimension;

    @Schema(description = "倍数购买（CR-20260613-0943）：取 SPU 兜底 sku_id=0 的 multiple；null/≤1 表示无倍数约束")
    private Integer wholesaleMultiple;

}