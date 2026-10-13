package com.tigshop.bean.query.salesman;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 分销内容管理列表
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Setter
@Getter
@Schema(description = "分销内容管理列表参数")
public class SalesmanContentPageQuery extends BasePage {
    // 新增字段
    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "来源")
    private String from;
}
