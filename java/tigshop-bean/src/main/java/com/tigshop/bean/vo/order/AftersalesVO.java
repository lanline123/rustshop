// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.order;

import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 软件许可服务实现
 *
 * @author Tigshop团队
 * @create 2024年11月19日 10:19
 */
@Data
@Schema(description = "售后管理参数")
public class AftersalesVO {
    @Schema(description = "售后管理ID")
    private Integer aftersaleId;

    @Schema(description = "售后管理名称")
    private String aftersalesName;

    @Schema(description = "售后管理图片")
    private String aftersalesPic;

    @Schema(description = "售后管理排序")
    private Integer sortOrder;

    @Schema(description = "是否展示")
    private Integer isShow;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "添加时间")
    private String addTime;

    @Schema(description = "售后原因")
    private String aftersaleReason;

    @Schema(description = "售后类型")
    private Integer aftersaleType;

    @JsonTranslate
    @Schema(description = "售后项目列表")
    private List<AftersalesItemVO> aftersalesItems;

    @Schema(description = "售后日志列表")
    private List<AftersalesLogVO> aftersalesLog;

    @Schema(description = "售后单号")
    private String aftersalesSn;

    @JsonTranslate
    @Schema(description = "售后类型名称")
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
    @JsonTranslate(dataType = 1)
    private String logisticsName;

    @Schema(description = "订单ID")
    private Integer orderId;

    @Schema(description = "订单号")
    private String orderSn;

    @Schema(description = "图片列表")
    private List<String> pics;

    @Schema(description = "退款金额")
    private String refundAmount;

    @Schema(description = "实际退款金额(当审核状态之后)")
    private BigDecimal actualRefundAmount;

    @Schema(description = "回复")
    private String reply;

    @Schema(description = "退货地址")
    private String returnAddress;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @JsonTranslate
    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "跟踪号")
    private String trackingNo;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "建议退款金额")
    private BigDecimal suggestRefundAmount;

    @Schema(description = "退款税费金额（本次退款金额中包含的税费）")
    private BigDecimal refundTaxAmount;

    @Schema(description = "供应商id")
    private Integer vendorId;

    @Schema(description = "状态副文案（缺失时前端按 status 本地兜底）")
    private String statusDescription;

    @Schema(description = "关联订单标记")
    private Integer mark;

    @Schema(description = "关联订单商家备注（表头「标记」弹窗回显）")
    private String adminNote;

    @Schema(description = "拒绝/拒收快捷理由标签（数据源见后端待确认，缺失时前端可手填）")
    private String refuseReason;

    @Schema(description = "自动同意退款截止时间 yyyy-MM-dd HH:mm:ss（仅待审核/待供应商审核且已开启自动同意退货时返回，用于后台倒计时）")
    private String autoAgreeEndTime;

    @Schema(description = "服务器当前时间 yyyy-MM-dd HH:mm:ss（用于后台倒计时以服务端时间为准，规避本地时钟偏差）")
    private String serverTime;

}