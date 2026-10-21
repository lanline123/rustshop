package com.tigshop.bean.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品推广VO
 *
 * @author Tigshop项目组
 * @since 2025/12/04
 */
@Data
@Builder
@Schema(description = "商品推广信息")
public class ProductPostVO {

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品价格")
    private BigDecimal productPrice;

    @Schema(description = "商品图片")
    private String picUrl;

    @Schema(description = "商品描述")
    private String productBrief;

    @Schema(description = "商品编号")
    private String productSn;

    @Schema(description = "店铺名称")
    private String shopName;

    @Schema(description = "H5短链")
    private String h5ShortLink;

    @Schema(description = "H5原链")
    private String h5OriginalLink;

    @Schema(description = "PC短链")
    private String pcShortLink;

    @Schema(description = "PC原链")
    private String pcOriginalLink;

    @Schema(description = "小程序二维码")
    private String miniCode;

    @Schema(description = "微信短链")
    private String wechatShortLink;

    @Schema(description = "微信短链生成失败原因")
    private String wechatShortLinkError;
}
