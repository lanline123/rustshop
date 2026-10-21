// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.print;

import com.tigshop.bean.enums.print.PrintLogStatusEnum;
import com.tigshop.bean.enums.print.PrintPlatformEnum;
import com.tigshop.bean.enums.print.PrintTriggerTypeEnum;
import com.tigshop.bean.model.print.PrintLog;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 打印日志列表行 VO
 *
 * @author Tigshop团队
 * @since 2026/4/21
 */
@Data
@Schema(description = "打印日志列表行")
public class PrintLogPageVO {

    @Schema(description = "日志ID")
    private Long logId;

    @Schema(description = "打印机ID")
    private Integer printId;

    @Schema(description = "打印机SN")
    private String printSn;

    @Schema(description = "平台 1=飞鹅云 2=商米云")
    private Integer platform;

    @Schema(description = "平台名称（展示用）")
    private String platformName;

    @Schema(description = "订单ID")
    private Integer orderId;

    @Schema(description = "触发方式 1=自动 2=手动")
    private Integer triggerType;

    @Schema(description = "触发方式描述（展示用）")
    private String triggerTypeDesc;

    @Schema(description = "状态 0=待推送 1=推送成功 2=打印成功 3=推送失败 4=打印机离线")
    private Integer status;

    @Schema(description = "状态描述（展示用）")
    private String statusDesc;

    @Schema(description = "平台错误码")
    private String errorCode;

    @Schema(description = "错误消息")
    private String errorMsg;

    @Schema(description = "重试次数")
    private Integer retryCount;

    @Schema(description = "推送时间（Unix 秒）")
    private Integer pushedTime;

    @Schema(description = "打印成功时间（Unix 秒）")
    private Integer printedTime;

    @Schema(description = "创建时间（Unix 秒）")
    private Integer addTime;

    public PrintLogPageVO() {
    }

    public PrintLogPageVO(PrintLog log) {
        this.logId = log.getLogId();
        this.printId = log.getPrintId();
        this.printSn = log.getPrintSn();
        this.platform = log.getPlatform();
        this.platformName = PrintPlatformEnum.getDescByCode(log.getPlatform());
        this.orderId = log.getOrderId();
        this.triggerType = log.getTriggerType();
        this.triggerTypeDesc = PrintTriggerTypeEnum.getDescByCode(log.getTriggerType());
        this.status = log.getStatus();
        this.statusDesc = PrintLogStatusEnum.getDescByCode(log.getStatus());
        this.errorCode = log.getErrorCode();
        this.errorMsg = log.getErrorMsg();
        this.retryCount = log.getRetryCount();
        this.pushedTime = log.getPushedTime();
        this.printedTime = log.getPrintedTime();
        this.addTime = log.getAddTime();
    }
}
