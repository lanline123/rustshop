package com.tigshop.bean.param.salesman;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

import static com.tigshop.common.constant.salesman.SalesmanContentConstants.SALESMAN_CONTENT_ID_NOT_NULL;

/**
 * 分销内容管理更新参数
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "分销内容管理参数")
public class SalesmanContentEditParam {
    @Schema(description = "分销内容管理ID")
    @NotNull(message = SALESMAN_CONTENT_ID_NOT_NULL)
    private Integer id;

    @Schema(description = "分销内容管理标题")
    private String title;

    @Schema(description = "分销内容管理描述")
    private String describe;

    @Schema(description = "分销内容管理内容")
    private String content;

    @Schema(description = "分销内容管理图片")
    private String img;

    @Schema(description = "分销内容管理开始时间")
    private String startTime;

    @Schema(description = "分销内容管理结束时间")
    private String endTime;

    @Schema(description = "是否置顶")
    private Integer isTop;

    @Schema(description = "是否可用")
    private Integer isAvailable;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "图片列表")
    private List<String> pics;
}
