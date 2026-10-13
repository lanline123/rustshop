// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.vo.vendor;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.config.TimestampToDateSerializer;
import com.tigshop.common.enums.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Tigshop团队
 * @create 2025/7/4 15:04
 */
@Data
@Schema(description = "供应商配置")
public class VendorDetailConfigVO extends VendorDetailVO{

    @Schema(description = "供应商客服电话")
    @Sensitive(type = SensitiveType.PHONE)
    private String kefuPhone;

    @Schema(description = "供应商售后审核 0关闭 1开启")
    private Integer afterSalesReview;

    @Schema(description = "发货信息")
    private DeliveryInfo shipInfo;

    @Schema(description = "收货信息")
    private DeliveryInfo receiveInfo;

    @Schema(description = "简介")
    private String description;

    @Schema(description = "创建时间")
    @JsonSerialize(using = TimestampToDateSerializer.class)
    private String addTime;

    @Data
    @Schema(description = "收发货信息")
    public static class DeliveryInfo {
        @Schema(description = "联系人")
        private String sender;
        @Schema(description = "联系方式")
        @Sensitive(type = SensitiveType.PHONE)
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
