package com.tigshop.bean.vo.shop;

import cn.hutool.json.JSONObject;
import com.tigshop.bean.model.product.Product;
import com.tigshop.common.annotation.JsonTranslate;
import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.enums.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 店铺表详情VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "前台店铺表详情")
public class ClientShopDetailVO {
    @Schema(description = "店铺状态的文本描述")
    private String statusText;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "店铺标题")
    @JsonTranslate(dataType = 5)
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

    @Schema(description = "店铺联系手机号")
    @Sensitive(type = SensitiveType.PHONE)
    private String contactMobile;

    @Schema(description = "店铺描述")
    private String description;

    @Schema(description = "客服电话")
    @Sensitive(type = SensitiveType.PHONE)
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

    @Schema(description = "商家信息")
    private Merchant merchant;

    @Schema(description = "商品总数")
    private Long productCount;

    @Schema(description = "新商品数量")
    private Long newProductCount;

    @Schema(description = "是否收藏店铺")
    private Boolean collectShop;

    @Schema(description = "商品评分")
    private BigDecimal productScore;

    @Schema(description = "评星(店铺评分,服务评分)")
    private BigDecimal shopStar;

    @Schema(description = "入驻审核通过或平台录入的营业执照图片，无有效资料返回空串")
    private String businessLicensePic;

    @Data
    @Schema(description = "商家信息")
    public static class Merchant {
        @Schema(description = "商家类型文本描述")
        private String typeText;

        @Schema(description = "商家状态文本描述")
        private String statusText;

        @Schema(description = "商家ID")
        private Integer merchantId;

        @Schema(description = "商家申请ID")
        private Integer merchantApplyId;

        @Schema(description = "用户ID")
        private Integer userId;

        @Schema(description = "添加时间")
        private String addTime;

        @Schema(description = "商家数据")
        private JSONObject merchantData;

        @Schema(description = "商家状态码")
        private Integer status;

        @Schema(description = "商家类型")
        private Integer type;

        @Schema(description = "公司名称")
        private String companyName;

        @Schema(description = "法人名称")
        private String corporateName;

        @Schema(description = "结算周期")
        private Integer settlementCycle;
    }
}
