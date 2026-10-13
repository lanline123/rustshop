// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.model.shop;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 店铺表model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("shop")
@Schema(description = "店铺表")
public class Shop {

    @Schema(description = "店铺表ID")
    @TableId(value = "shop_id", type = IdType.INPUT)
    private Integer shopId;

    @Schema(description = "店铺名称")
    @JsonTranslate(dataType = 2)
    private String shopTitle;

    @Schema(description = "店铺创建时间")
    private Long addTime;

    @Schema(description = "店铺logo")
    private String shopLogo;

    @Schema(description = "点击量")
    private Integer clickCount;

    @Schema(description = "店铺状态1开业  4暂停运营 10关店")
    private Integer status;

    @Schema(description = "商户id")
    private Integer merchantId;

    @Schema(description = "店铺资金")
    private BigDecimal shopMoney;

    @Schema(description = "冻结资金")
    private BigDecimal frozenMoney;

    @Schema(description = "联系电话")
    private String contactMobile;

    @Schema(description = "简介")
    private String description;

    @Schema(description = "客服电话")
    private String kefuPhone;

    @Schema(description = "客服微信")
    private String kefuWeixin;

    @Schema(description = "客服链接")
    private String kefuLink;

    @Schema(description = "是否联系客服：1 是 0 否")
    private Integer isContactKefu;

    @Schema(description = "客服入口信息页面： 1 商品详情页 2 订单页")
    private String kefuInlet;

    @Schema(description = "发货信息")
    private String shipInfo;

    @Schema(description = "收货信息")
    private String receiveInfo;

    @Schema(description = "最后登录时间")
    private Integer lastLoginTime;

    @Schema(description = "供应商设价方式(1按比例，2-按固定数值加价，3-默认售价)")
    private Integer vendorSetPriceType;

    @Schema(description = "智能设价（百分比或固定数值）")
    private String vendorSetPriceAutoValue;

    @Schema(description = "服务费比例")
    private BigDecimal serviceFeeRate;

    @Schema(description = "手续费比例")
    private BigDecimal feeRate;

    @Schema(description = "店铺类型：1 店铺，2 门店，3自提点")
    private Integer shopType;

    @Schema(description = "店铺父级id")
    private Integer storeParentId;

    @Schema(description = "门店封面")
    private String shopCoverPicture;

    @Schema(description = "门店相册")
    private String shopShowPicture;

    @Schema(description = "联系方式（JSON）")
    private String shopContactJson;

    @Schema(description = "运营时间（JSON）")
    private String shopOpenCloseJson;

    @Schema(description = "region表id")
    private String shopRegionId;

    @Schema(description = "门店详细地址")
    private String shopDetailedAddress;

    @Schema(description = "店铺经度")
    private BigDecimal shopLongitude;

    @Schema(description = "店铺纬度")
    private BigDecimal shopLatitude;

    @Schema(description = "评星")
    private BigDecimal shopStar;

    @Schema(description = "总评价分数")
    private Integer shopTotalStar;

    @Schema(description = "评价数量")
    private Integer shopStarNum;

    @Schema(description = "销量")
    private Integer shopSales;

    @Schema(description = "是否使用门店分类（多门店版）")
    private Integer useShopCategory;

    @Schema(description = "店铺选择展示分类")
    private String shopShowCategory;

    // *** 收银相关设置 ***

    @Schema(description = "店内收银自动抹零：0关闭 1开启")
    private Integer autoEraseZero;

    @Schema(description = "收银积分基于订单自动抵扣：0关闭 1开启")
    private Integer autoDeductionPoints;

    @Schema(description = "收银机默认收款方式：(默认)1-现金支付 2-扫码支付 3-余额支付 4-积分支付")
    private Integer defaultCollectionMethod;

    @Schema(description = "扫码后自动加入购物车：0关闭 1开启")
    private Integer autoAddCartAfterScan;

    @Schema(description = "线上订单自动打印：0关闭 1开启")
    private Integer autoPrintOnlineOrder;

    @Schema(description = "线下订单自动打印：0关闭 1开启")
    private Integer autoPrintOfflineOrder;

    @Schema(description = "店铺常用备注列表(JSON数组)")
    private String shopCommonRemarks;

    @Schema(description = "null或者0表示未删除 1 表示删除")
    private Integer isDelete;

    @Schema(description = "收银台商品来源(JSON数组)：0-全部商品，1-门店自建商品，2-平台分配商品，3-供应商商品")
    private String posProductSource;
}
