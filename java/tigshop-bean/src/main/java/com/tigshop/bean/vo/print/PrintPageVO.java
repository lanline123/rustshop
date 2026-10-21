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

import com.tigshop.bean.enums.print.PrintPaperSizeEnum;
import com.tigshop.bean.enums.print.PrintPlatformEnum;
import com.tigshop.bean.enums.print.PrintStatusEnum;
import com.tigshop.bean.model.print.Print;
import com.tigshop.common.utils.TigUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 打印机列表
 *
 * @author Tigshop团队
 * @since 2025/1/15
 */
@Data
public class PrintPageVO {

    @Schema(description = "打印机ID")
    private Integer printId;

    @Schema(description = "打印机名称")
    private String printName;

    @Schema(description = "打印机SN")
    private String printSn;

    @Schema(description = "第三方平台 1=飞鹅云 2=商米云")
    private Integer platform;

    @Schema(description = "打印机类型")
    private String platformText;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "纸张尺寸（JSON 数字输出，58 / 80）；由 Service 从 print_config 回填",
            implementation = Integer.class, example = "58")
    private PrintPaperSizeEnum paperSize;

    @Schema(description = "纸张尺寸描述，例如 58mm / 80mm")
    private String paperSizeDesc;

    @Schema(description = "语音播报开关：1=开启，2=关闭", example = "1")
    private Integer voiceEnabled;

    @Schema(description = "语音播报内容：0=订单号+金额，1=仅金额", example = "0")
    private Integer voiceContentType;

    @Schema(description = "绑定的收银台设备码")
    private String deviceCode;

    @Schema(description = "是否为店铺默认打印机：1是，0否")
    private Integer isDefault;

    @Schema(description = "更新时间")
    private String updateTime;

    @Schema(description = "添加时间")
    private String addTime;

    public PrintPageVO(Print print) {
        this.printId = print.getPrintId();
        this.printName = print.getPrintName();
        this.printSn = print.getPrintSn();
        this.status = print.getStatus();
        this.statusText = PrintStatusEnum.getDescByCode(print.getStatus());
        this.platform = print.getPlatform();
        this.platformText = PrintPlatformEnum.getDescByCode(print.getPlatform());
        // 语音开关：null 兜底为 1（开启）
        this.voiceEnabled = print.getVoiceEnabled() == null ? 1 : print.getVoiceEnabled();
        // 语音内容类型：null 兜底为 0（订单号+金额）
        this.voiceContentType = print.getVoiceContentType() == null ? 0 : print.getVoiceContentType();
        this.isDefault = print.getIsDefault() == null ? 0 : print.getIsDefault();
        this.updateTime = TigUtils.handelTime(print.getUpdateTime());
        this.addTime = TigUtils.handelTime(print.getAddTime());
    }
}
