// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.shop;

import com.tigshop.bean.dto.shop.StoreExtendedDto;
import com.tigshop.bean.enums.o2o.ShopTypeEnum;
import com.tigshop.bean.enums.shop.ShopType;
import com.tigshop.common.annotation.JsonTranslate;
import com.tigshop.bean.enums.shop.StorePickupType;
import com.tigshop.bean.enums.shop.StoreType;
import com.tigshop.bean.vo.merchant.MerchantVO;
import com.tigshop.bean.vo.o2o.TipListVO;
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
@Schema(description = "店铺表参数")
public class ShopVO extends StoreExtendedDto {

    @Schema(description = "店铺创建时间")
    private String addTime;

    @Schema(description = "点击量")
    private Integer clickCount;

    @Sensitive(type = SensitiveType.PHONE)
    @Schema(description = "联系电话")
    private String contactMobile;

    @Schema(description = "冻结资金")
    private BigDecimal frozenMoney;

    @Schema(description = "是否联系客服：1 是 0 否")
    private Integer isContactKefu;

    @Schema(description = "客服入口信息页面： 1 商品详情页 2 订单页")
    private List<Integer> kefuInlet;

    @Schema(description = "客服链接")
    private String kefuLink;

    @Sensitive(type = SensitiveType.PHONE)
    @Schema(description = "客服电话")
    private String kefuPhone;

    @Schema(description = "客服微信")
    private String kefuWeixin;

    @Schema(description = "发货信息")
    private ShopDeliveryInfo shipInfo;

    @Schema(description = "收货信息")
    private ShopDeliveryInfo receiveInfo;

    @Schema(description = "商户id")
    private Integer merchantId;

    @Schema(description = "店铺表ID")
    private Integer shopId;

    @Schema(description = "店铺logo")
    private String shopLogo;

    @Schema(description = "店铺资金")
    private BigDecimal shopMoney;

    @Schema(description = "店铺名称")
    @JsonTranslate
    private String shopTitle;

    @Schema(description = "店铺文案")
    private String statusText;

    @Schema(description = "商户数据")
    private MerchantVO merchant;

    @Schema(description = "用户店铺信息")
    private AdminUserShopVO adminUserShop;

    @Schema(description = "是否选中")
    private boolean check;

//    @Schema(description = "独立店铺佣金")
//    private BigDecimal shopCommission;

    @Schema(description = "上级组织名称")
    private String storeParentName;

    @Schema(description = "标签")
    private List<TipListVO> tips;

    @Schema(description = "是否使用门店分类（0不使用，1使用）")
    private Integer useShopCategory;

    @Schema(description = "门店分类")
    private ShopShowCategoryConfig shopShowCategory;

    @Schema(description = "评星")
    private BigDecimal shopStar;

    @Schema(description = "商品评分")
    private BigDecimal productScore;

    public String getStatusText() {
        if (getStatus() == null) {
            return "";
        }
        if (getShopType() == ShopTypeEnum.PICKUP.getCode()) {
            return StorePickupType.getTypeName(getStatus());
        }
        if (getShopType() == ShopTypeEnum.STORE.getCode()) {
            return StoreType.getTypeName(getStatus());
        } else {
            return ShopType.getTypeName(getStatus());
        }
    }

    @Data
    @Schema(description = "收发货信息")
    public static class ShopDeliveryInfo {
        @Schema(description = "联系人")
        private String sender;
        @Sensitive(type = SensitiveType.PHONE)
        @Schema(description = "联系方式")
        private String mobile;
        @Schema(description = "省份")
        private String provinceName;
        @Schema(description = "城市")
        private String cityName;
        @Schema(description = "地区")
        private String areaName;
        @Schema(description = "详细地址")
        private String address;
    }
}
