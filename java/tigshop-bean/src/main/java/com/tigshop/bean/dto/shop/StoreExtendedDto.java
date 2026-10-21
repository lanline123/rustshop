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


import com.fasterxml.jackson.annotation.JsonInclude;
import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.enums.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Tigshop团队
 * @create 2025/8/11 14:03
 */
@Data
@Schema(description = "门店扩展字段")
public class StoreExtendedDto {

    /**
     * 店铺类型，默认门店
     */
    @Schema(description = "1店铺，2门店，3自提点")
    private Integer shopType = 1;

    @Schema(description = "自提时传入门店id")
    private Integer storeParentId;

    @Schema(description = "门店封面(门店时必填)")
    private String shopCoverPicture;

    @Schema(description = "门头照片")
    private List<PicInfo> shopShowPicture;

    @Schema(description = "简介")
    private String description;

    @Schema(description = "店铺状态1开业 4暂停运营 10关店")
    private Integer status;

    @Schema(description = "联系方式（JSON）")
    private List<ShopContactConfig> shopContactConfig;

    @Schema(description = "运营时间（JSON）")
    private ShopOpenCloseConfig shopOpenCloseConfig;

    @Schema(description = "门店regionId ")
    private List<Integer> shopRegionIds;

    @Schema(description = "门店regionId ")
    private List<String> shopRegionNames;

    @Schema(description = "门店详细地址 ")
    private String shopDetailedAddress;

    @Schema(description = "店铺经度 ")
    private BigDecimal shopLongitude;

    @Schema(description = "店铺纬度 ")
    private BigDecimal shopLatitude;

    @Schema(description = "标签Id")
    private List<Integer> shopTips;

    @Data
    @Schema(description = "图片信息")
    public static class PicInfo {
        @Schema(description = "URL")
        private Integer picId;
        @Schema(description = "图片缩略图")
        private String picThumb;
        @Schema(description = "图片URL")
        private String picUrl;
        @Schema(description = "图片名称")
        private String picName;
    }

    @Data
    public static class ShopContactConfig {
        @Schema(description = "类型 1 = 普通座机号 2 = 企业座机号 3 = 手机号码")
        private Integer type;
        @Sensitive(type = SensitiveType.PHONE)
        @Schema(description = "号码 使用 - 隔开，前面为区号，后面为号码")
        private String values;
    }

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
        private List<ShopOpenCloseConfig.TimeRange> times;

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
            private List<TimeRangeStartEnd> ranges;
        }
    }

    @Data
    public static class TimeRangeStartEnd {
        @Schema(description = "开始时间（HH:mm 格式）")
        private String start;
        @Schema(description = "结束时间（HH:mm 格式）")
        private String end;
    }
}
