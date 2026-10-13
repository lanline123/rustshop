package com.tigshop.bean.vo.promotion.card;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.bean.vo.product.ExchangeProductToCartVO;
import com.tigshop.bean.vo.product.ProductSkuVO;
import com.tigshop.common.annotation.JsonTranslate;
import com.tigshop.common.config.TimestampToDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Tigshop项目组
 * @create 2025年10月30日 16:42
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "卡信息")
public class GiftCardsVO {
    @Schema(description = "卡id")
    private Integer giftCardsId;

    @Schema(description = "所属卡组ID")
    private Integer cardGroupsId;

    @JsonTranslate(dataType = 17)
    @Schema(description = "卡组名称")
    private String cardGroupName;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "卡编号")
    private String giftCardSn;

    @Schema(description = "店铺名称")
    private String shopTitle;

    @Schema(description = "店铺logo")
    private String shopLogo;

    @JsonTranslate(dataType = 17)
    @Schema(description = "图片")
    private String imageUrl;

    @Schema(description = "卡号")
    private String cardNumber;

    @Schema(description = "卡密码")
    private String cardPassword;

    @Schema(description = "1-可用 0-不可用")
    private Integer isAvailable;

    @Schema(description = "0-未使用 1-已使用 2-已过期")
    private Integer status;

    @Schema(description = "是否售出")
    private String isSold;

    @Schema(description = "是否兑换")
    private String isExchange;

    @Schema(description = "使用时间")
    @JsonSerialize(using = TimestampToDateSerializer.class)
    private String usedTime;

    @Schema(description = "使用者ID")
    private Integer userId;

    @Schema(description = "添加时间")
    @JsonSerialize(using = TimestampToDateSerializer.class)
    private String addTime;

    @Schema(description = "过期时间")
    private Long expiredTime;

    @Schema(description = "商品信息")
    private List<ProductSkuVO> giftCardProducts;

    @Schema(description = "购物车信息")
    private ExchangeProductToCartVO exchangeProductToCart;
}