package com.tigshop.bean.vo.order;

import com.tigshop.bean.dto.order.ReturnPicDTO;
import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 软件许可服务实现
 *
 * @author Tigshop团队
 * @create 2024年11月19日 10:19
 */
@Data
@Schema(description = "售后管理参数")
public class AfterSalesClientDetailVO {
    @Schema(description = "售后管理ID")
    private Integer aftersaleId;

    @Schema(description = "售后管理名称")
    private String aftersalesName;

    @Schema(description = "售后管理图片")
    private String aftersalesPic;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "添加时间")
    private String addTime;

    @Schema(description = "售后原因")
    @JsonTranslate(dataType = 2)
    private String aftersaleReason;

    @Schema(description = "售后类型")
    private Integer aftersaleType;

    @Schema(description = "售后项目列表")
    private List<AftersalesItemVO> aftersalesItems;

    @Schema(description = "售后日志列表")
    private List<AftersalesLogVO> aftersalesLog;

    @Schema(description = "售后单号")
    private String aftersalesSn;

    @Schema(description = "售后类型名称")
    @JsonTranslate(dataType = 2)
    private String aftersalesTypeName;

    @Schema(description = "审核时间")
    private String auditTime;

    @Schema(description = "处理时间")
    private String dealTime;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "最终时间")
    private String finalTime;

    @Schema(description = "物流名称")
    private String logisticsName;

    @Schema(description = "订单ID")
    private Integer orderId;

    @Schema(description = "订单号")
    private String orderSn;

    @Schema(description = "图片列表")
    private List<ReturnPicDTO> pics;

    @Schema(description = "退款金额")
    private String refundAmount;

    @Schema(description = "回复")
    private String reply;

    @Schema(description = "退货地址")
    private String returnAddress;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "跟踪号")
    private String trackingNo;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "订单信息")
    private OrderVO orders;

    @Schema(description = "进度流程")
    private StepStatusVO stepStatus;

    @Schema(description = "是否可以取消")
    private Boolean canCancel;

    @Schema(description = "退货提示")
    private String returnGoodsTip;

    @Schema(description = "售后状态用户可见描述副文案（缺失时前端按 status 本地兜底）")
    private String statusDesc;

    @Schema(description = "自动同意退款截止时间 yyyy-MM-dd HH:mm:ss（仅处理中且已开启自动同意退货时返回，用于买家端倒计时）")
    private String autoAgreeEndTime;

    @Schema(description = "服务器当前时间 yyyy-MM-dd HH:mm:ss（用于买家端倒计时以服务端时间为准，规避本地时钟偏差）")
    private String serverTime;

    @Data
    public static class StepStatusVO {
        @Schema(description = "当前流程数")
        private Integer current;

        @Schema(description = "状态")
        private String status;

        @Schema(description = "流程")
        private List<Map<String, Object>> steps;
    }

}
