package com.tigshop.bean.param.print;

import com.tigshop.bean.enums.print.PrintPaperSizeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 打印机更新DTO
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "打印机更新DTO")
public class PrintUpdateParam {

    @Schema(description = "打印机ID")
    @NotNull(message = "打印机ID不能为空")
    private Integer printId;

    @Schema(description = "打印机名称")
    @NotBlank(message = "打印机名称不能为空")
    private String printName;

    @Schema(description = "第三方平台对接账号")
    @NotBlank(message = "第三方平台对接账号不能为空")
    private String thirdAccount;

    @Schema(description = "第三方平台key")
    @NotBlank(message = "第三方平台key不能为空")
    private String thirdKey;

    @Schema(description = "打印机SN")
    @NotBlank(message = "打印机SN不能为空")
    private String printSn;

    @Schema(description = "打印机KEY（飞鹅云必填；商米云无此概念，可不填）")
    private String printKey;

    @Schema(description = "打印机状态 1开启 2关闭")
    @NotNull(message = "打印机状态")
    private Integer status;

    @Schema(description = "订单支付自动打印 1开启 2关闭")
    @NotNull(message = "订单支付自动打印不能为空")
    private Integer autoPrint;

    @Schema(description = "第三方平台 1=飞鹅云 2=商米云，默认飞鹅云（兼容旧前端）")
    private Integer platform;

    @Schema(description = "纸张尺寸，接受 JSON 数字 58 / 80；为空兼容为 58mm",
            implementation = Integer.class, example = "58")
    private PrintPaperSizeEnum paperSize;

    @Schema(description = "语音播报开关：1=开启，2=关闭；为空不更新", example = "1")
    private Integer voiceEnabled;

    @Schema(description = "语音播报内容：0=订单号+金额，1=仅金额；为空不更新", example = "0")
    private Integer voiceContentType;

    @Schema(description = "绑定的收银台设备码")
    private String deviceCode;

    @Schema(description = "是否为店铺默认打印机：1是，0否")
    private Integer isDefault;
}
