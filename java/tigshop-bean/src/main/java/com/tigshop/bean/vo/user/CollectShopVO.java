package com.tigshop.bean.vo.user;

import com.tigshop.bean.model.product.Product;
import com.tigshop.common.annotation.JsonTranslate;
import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.enums.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
/**
 * 店铺收藏VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "会员收藏店铺的记录列表VO")
public class CollectShopVO {
    @Schema(description = "会员收藏店铺的记录ID")
    private Integer collectId;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "添加时间")
    private String addTime;

    @Schema(description = "商品数量")
    private Long productCount;

    @Schema(description = "收藏数量")
    private Long collectCount;

    @Schema(description = "店铺信息")
    private ShopVO shop;

    @Data
    @Schema(description = "店铺信息")
    public static class ShopVO {
        @Schema(description = "状态文本")
        private String statusText;

        @Schema(description = "店铺ID")
        private Integer shopId;

        @Schema(description = "店铺标题")
        @JsonTranslate(dataType =2)
        private String shopTitle;

        @Schema(description = "添加时间")
        private String addTime;

        @Schema(description = "店铺Logo")
        private String shopLogo;

        @Schema(description = "点击次数")
        private Integer clickCount;

        @Schema(description = "状态")
        private Integer status;

        @Schema(description = "商家ID")
        private Integer merchantId;

        @Schema(description = "店铺金额")
        private BigDecimal shopMoney;

        @Schema(description = "冻结金额")
        private BigDecimal frozenMoney;

        @Schema(description = "联系电话")
        @Sensitive(type = SensitiveType.PHONE)
        private String contactMobile;

        @Schema(description = "描述")
        private String description;

        @Schema(description = "客服电话")
        @Sensitive(type = SensitiveType.PHONE)
        private String kefuPhone;

        @Schema(description = "客服微信")
        private String kefuWeixin;

        @Schema(description = "客服链接")
        private String kefuLink;

        @Schema(description = "是否联系客服")
        private Integer isContactKefu;

        @Schema(description = "客服入口")
        private List<Integer> kefuInlet;

        @Schema(description = "热门商品")
        private List<Product> hotProduct;

        @Schema(description = "新品")
        private List<Product> newProduct;

        @Schema(description = "推荐商品")
        private List<Product> bestProduct;
    }
}

