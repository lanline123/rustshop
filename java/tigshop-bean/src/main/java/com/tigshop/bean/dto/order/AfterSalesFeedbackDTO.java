package com.tigshop.bean.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 软件许可服务实现
 *
 * @author Tigshop团队
 * @create 2024年11月19日 10:19
 */
@Data
@Schema(description = "售后反馈参数")
public class AfterSalesFeedbackDTO {
    @Schema(description = "售后管理ID")
    private Integer id;

    @Schema(description = "日志信息")
    private String logInfo;

    @Schema(description = "反馈图片")
    private List<ReturnPicDTO> returnPic;

    @Schema(description = "物流名称")
    private String logisticsName;

    @Schema(description = "物流单号")
    private String trackingNo;

    @Schema(description = "用户id")
    private Integer userId;
}
