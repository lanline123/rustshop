// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.shop;

import com.tigshop.bean.vo.shop.ShopShowCategoryConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

import static com.tigshop.common.constant.shop.ShopConstants.SHOP_NOT_NULL;

/**
  * 店铺表更新参数
  * @author Tigshop团队
  * @create 2024年12月03日 17:17
  */
@Data
@Schema(description = "店铺setting参数")
public class ShopSettingDTO {

    @Schema(description = "店铺表ID")
    @NotNull(message = SHOP_NOT_NULL)
    private Integer shopId;
    @Schema(description ="状态")
    private Integer status = 1;
    @Schema(description ="联系电话")
    private String contactMobile;
    @Schema(description ="客服电话")
    private String kefuPhone = "";
    @Schema(description ="客服微信")
    private String kefuWeixin = "";
    @Schema(description ="客服链接")
    private String kefuLink = "";
    @Schema(description ="联系客服")
    private Integer isContactKefu = 1;
    @Schema(description ="联系客服入口")
    private List<Integer> kefuInlet = new ArrayList<>();

    @Schema(description = "发货信息")
    private ShopDeliveryInfo shipInfo;

    @Schema(description = "收货信息")
    private ShopDeliveryInfo receiveInfo;

    @Schema(description = "是否使用门店分类（0不使用，1使用）")
    private Integer useShopCategory;
    @Schema(description = "门店分类")
    private ShopShowCategoryConfig shopShowCategory;

    @Data
    @Schema(description = "收发货信息")
    public static class ShopDeliveryInfo {
        @Schema(description = "联系人")
        private String sender;
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