package com.tigshop.bean.vo.shop;

import com.tigshop.bean.vo.merchant.MerchantVO;
import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.enums.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "店铺表参数")
public class ShopAccountVO {
    @Schema(description = "店铺创建时间")
    private String addTime;

    @Schema(description = "点击量")
    private Integer clickCount;

    @Sensitive(type = SensitiveType.PHONE)
    @Schema(description = "联系电话")
    private String contactMobile;

    @Schema(description = "简介")
    private String description;

    @Schema(description = "冻结资金")
    private BigDecimal frozenMoney;

    @Schema(description = "是否联系客服：1 是 0 否")
    private Integer isContactKefu;

    @Schema(description = "客服入口信息页面： 1 商品详情页 2 订单页")
    private String kefuInlet;

    @Schema(description = "客服链接")
    private String kefuLink;

    @Sensitive(type = SensitiveType.PHONE)
    @Schema(description = "客服电话")
    private String kefuPhone;

    @Schema(description = "客服微信")
    private String kefuWeixin;

    @Schema(description = "商户id")
    private Integer merchantId;

    @Schema(description = "店铺表ID")
    private Integer shopId;

    @Schema(description = "店铺logo")
    private String shopLogo;

    @Schema(description = "店铺资金")
    private BigDecimal shopMoney;

    @Schema(description = "店铺名称")
    private String shopTitle;

    @Schema(description = "店铺状态1开业  4暂停运营 10关店")
    private Integer status;

    @Schema(description = "店铺文案")
    private String statusText;

    @Schema(description = "商户数据")
    private MerchantVO merchant;
}
