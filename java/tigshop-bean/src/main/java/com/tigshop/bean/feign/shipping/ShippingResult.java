package com.tigshop.bean.feign.shipping;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 物流信息返回结果
 *
 * @author Tigshop团队
 * @create 2024年12月27日 14:19
 */
@Getter
@Setter
@Data
@Schema(description = "物流信息返回结果")
public class ShippingResult {
    @Schema(description = "返回标识")
    private String eBusinessID;

    @Schema(description = "返回code")
    private String shipperCode;

    @Schema(description = "配送方式code")
    private String logisticCode;

    @Schema(description = "状态")
    private String state;

    @Schema(description = "状态Ex")
    private String stateEx;

    @Schema(description = "定位地址")
    private String location;

    @Schema(description = "物流信息")
    private List<TraceVO> traces;

    @Schema(description = "状态")
    private String success;

    @Data
    @Schema(description = "物流信息")
    public static class TraceVO {
        @Schema(description = "动作")
        private String action;

        @Schema(description = "时间节点")
        private String acceptTime;

        @Schema(description = "节点")
        private String acceptStation;

        @Schema(description = "定位")
        private String location;
    }
}
