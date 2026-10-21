package com.tigshop.bean.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Tigshop项目组
 * @create 2025年09月19日 16:14
 */
@Schema(description = "商品推广信息")
@Data
@Builder
public class ProductPromoteVO {
    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品价格")
    private BigDecimal productPrice;

    @Schema(description = "商品图片")
    private String picUrl;

    @Schema(description = "WeChat短链")
    private String wechatShortLink;

    @Schema(description = "短链")
    private String shortLink;

    @Schema(description = "商品描述")
    private String productBrief;

    @Schema(description = "商品编号")
    private String productSn;

    @Schema(description = "店铺名称")
    private String shopName;

}