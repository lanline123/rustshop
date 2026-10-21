// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.vo.o2o;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.tigshop.bean.dto.shop.StoreExtendedDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Tigshop团队
 * @create 2025/8/28 15:35
 */
@Data
public class StoreListVO {

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "店铺Logo URL")
    private String shopLogo;

    @Schema(description = "店铺标题")
    private String shopTitle;

    @Schema(description = "店铺状态码")
    private Integer status;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "是否有优惠券")
    private Integer hasCoupon;

    @Schema(description = "标签")
    private List<String> tipNames;

    @Schema(description = "距离 单位m")
    private BigDecimal distance;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "联系方式(原始json字符串)")
    @JsonIgnore
    private String contactJson;

    @Schema(description = "联系方式")
    private List<StoreExtendedDto.ShopContactConfig> contactConfigs;

    @Schema(description = "评星")
    private BigDecimal shopStar;

    @Schema(description = "评价数量")
    private Integer shopStarNum;

    @Schema(description = "销量")
    private Integer shopSales;

    @Schema(description = "简介")
    private String description;

    @Schema(description = "店铺收藏数量")
    private Long shopCollect;

    @Schema(description = "是否收藏店铺")
    private Boolean collectShop;

    @Schema(description = "是否为选中门店（selectedShopId 命中时为 true）")
    private Boolean selected;

    @Schema(description = "用户传入经纬度是否落在该门店预约上门服务半径内；仅 deliveryTypes 含 2(上门服务) 且传了经纬度时计算，其余场景恒为 false")
    private Boolean inHomeServiceRange = false;

    @Schema(description = "用户传入经纬度是否落在该门店同城配送半径内；仅 deliveryTypes 含 4(同城配送) 且传了经纬度时计算，其余场景恒为 false")
    private Boolean inLocalDeliveryRange = false;

    @Schema(description = "营业时间(原始json字符串)")
    private ShopOpenCloseConfig shopOpenCloseConfig;

    @Schema(description = "运营时间（JSON）")
    @JsonIgnore
    private String shopOpenCloseConfigJson;

    @Data
    public static class ShopOpenCloseConfig {
        /**
         * 营业模式类型
         * 1 = 全天
         * 2 = 每日固定时间
         * 3 = 按星期时间
         */
        @Schema(description = "营业模式类型 1 = 全天 2 = 每日固定时间 3 = 按星期时间")
        private Integer type;

        /**
         * 营业时间段列表
         * type=1 时为空列表
         * type=2 时只有一个时间段
         * type=3 时按 dayOfWeek 列出（1=周一，7=周日）
         */
        private List<StoreExtendedDto.ShopOpenCloseConfig.TimeRange> times;

        @Data
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class TimeRange {
            // type 2 必填
            @Schema(description = "开始时间（HH:mm 格式）")
            private String start;
            @Schema(description = "结束时间（HH:mm 格式）")
            private String end;

            // type 3 必填
            @Schema(description = "星期（1=周一，7=周日），type=3 时必填，其他模式可为 null")
            private String dayOfWeek;
            private List<StoreExtendedDto.TimeRangeStartEnd> ranges;
        }
    }
}

