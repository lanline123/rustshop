package com.tigshop.bean.vo.shop;

import com.tigshop.bean.dto.shop.StoreExtendedDto;
import com.tigshop.bean.model.product.Product;
import com.tigshop.common.annotation.JsonTranslate;
import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.enums.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 店铺表VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "前台店铺表")
public class ClientShopVO {
    @Schema(description = "店铺状态的文本描述")
    private String statusText;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "店铺标题")
    @JsonTranslate(dataType = 2)
    private String shopTitle;

    @Schema(description = "店铺添加时间")
    private String addTime;

    @Schema(description = "店铺Logo URL")
    private String shopLogo;

    @Schema(description = "点击量")
    private Integer clickCount;

    @Schema(description = "店铺状态码")
    private Integer status;

    @Schema(description = "商家ID")
    private Integer merchantId;

    @Schema(description = "店铺可用资金")
    private BigDecimal shopMoney;

    @Schema(description = "店铺冻结资金")
    private BigDecimal frozenMoney;

    @Sensitive(type = SensitiveType.PHONE)
    @Schema(description = "店铺联系手机号")
    private String contactMobile;

    @Schema(description = "店铺描述")
    private String description;

    @Sensitive(type = SensitiveType.PHONE)
    @Schema(description = "客服电话")
    private String kefuPhone;

    @Schema(description = "客服微信")
    private String kefuWeixin;

    @Schema(description = "客服链接")
    private String kefuLink;

    @Schema(description = "是否可以联系客服")
    private Integer isContactKefu;

    @Schema(description = "客服入口")
    private String kefuInlet;

    @Schema(description = "收藏次数")
    private Long collectCount;

    @Schema(description = "上架商品数量")
    private Long listingCount;

    @Schema(description = "上架商品列表")
    private List<Product> listingProduct;

    @Schema(description = "评星")
    private BigDecimal shopStar;

    @Schema(description = "商品评分")
    private BigDecimal productScore;

    @Schema(description = "店铺类型：1店铺 2门店 3自提点")
    private Integer shopType;

    @Schema(description = "详细地址")
    private String shopDetailedAddress;

    @Schema(description = "经度")
    private BigDecimal shopLongitude;

    @Schema(description = "纬度")
    private BigDecimal shopLatitude;

    @Schema(description = "门店地域名称")
    private List<String> shopRegionNames;

    @Schema(description = "门店标签名称")
    private List<String> tipNames;

    @Schema(description = "联系方式")
    private List<StoreExtendedDto.ShopContactConfig> contactConfigs;

    @Schema(description = "营业时间")
    private StoreExtendedDto.ShopOpenCloseConfig shopOpenCloseConfig;
}
