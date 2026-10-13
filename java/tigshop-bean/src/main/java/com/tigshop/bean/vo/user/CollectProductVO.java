package com.tigshop.bean.vo.user;

import com.tigshop.bean.dto.product.ProductSkuDTO;
import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品收藏VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "商品收藏参数")
public class CollectProductVO {
    @Schema(description = "商品收藏ID")
    private Integer collectId;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "添加时间")
    private String addTime;

    @Schema(description = "商品收藏名称")
    @JsonTranslate(dataType = 2)
    private String productName;

    @Schema(description = "商品编号")
    private String productSn;

    @Schema(description = "商品图片")
    private String picThumb;

    @Schema(description = "商品大图")
    private String picUrl;

    @Schema(description = "市场价（划线价）")
    private BigDecimal marketPrice;

    @Schema(description = "成本价")
    private BigDecimal costPrice;

    @Schema(description = "是否促销")
    private Integer isPromote;

    @Schema(description = "商品价格")
    private BigDecimal productPrice;

    @Schema(description = "商品库存")
    private Integer productStock;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "等级ID")
    private Integer rankId;

    @Schema(description = "折扣")
    private BigDecimal discount;

    @Schema(description = "SKU价格")
    private BigDecimal skuPrice;

    @Schema(description = "商品SKU")
    private List<ProductSkuDTO> productSku;

    @Schema(description = "价格")
    private BigDecimal price;

    @Schema(description = "店铺 ID（用于平台商品判定，仅批发商可见时回填）")
    private Integer shopId;

    @Schema(description = "首条批发价（仅批发商身份 + 平台商品命中时返回）")
    private BigDecimal firstWholesalePrice;

    @Schema(description = "卡片可直接加购的起批数（CR-20260603-1720 / CD-035）；4 条命中规则全 true 才返回数字，否则 null")
    private Integer wholesaleCardMoq;

    @Schema(description = "起批维度（CR-20260613-0943）：1=SPU 总量起批 / 2=SKU 单独起批；与 wholesaleCardMoq 同范围回填，否则 null")
    private Integer wholesaleMoqDimension;

    @Schema(description = "倍数购买（CR-20260613-0943）：取 SPU 兜底 sku_id=0 的 multiple；null/≤1 表示无倍数约束")
    private Integer wholesaleMultiple;
}
