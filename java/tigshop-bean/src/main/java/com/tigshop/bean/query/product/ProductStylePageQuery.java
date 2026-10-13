package com.tigshop.bean.query.product;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "商品样式分页查询")
public class ProductStylePageQuery extends BasePage {

    @Schema(description = "样式类型 1=边框背景 2=活动背景")
    private Integer styleType;

    @Schema(description = "状态 0=禁用 1=启用")
    private Integer status;

    @Schema(description = "活动状态 1=活动未开始 2=活动进行中 3=活动已结束")
    private Integer activityStatus;

    @Schema(description = "生效开始时间（yyyy-MM-dd HH:mm:ss），筛选 startTime >= 该值的记录")
    private String startTime;

    @Schema(description = "生效结束时间（yyyy-MM-dd HH:mm:ss），筛选 endTime <= 该值的记录")
    private String endTime;
}
