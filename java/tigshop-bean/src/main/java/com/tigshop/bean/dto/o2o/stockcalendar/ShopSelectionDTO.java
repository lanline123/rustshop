package com.tigshop.bean.dto.o2o.stockcalendar;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "门店选择配置")
public class ShopSelectionDTO {
    
    /**
     * 选择类型：1=全部门店，2=排除指定门店，3=选择指定门店
     */
    @Schema(description = "选择类型：1=全部门店，2=排除指定门店，3=选择指定门店", required = true)
    private Integer selectionType;
    
    /**
     * 门店ID列表
     * 当selectionType=2时，表示要排除的门店ID列表
     * 当selectionType=3时，表示要选择的门店ID列表
     * 当selectionType=1时，此字段可为空
     */
    @Schema(description = "门店ID列表")
    private List<Integer> shopIds;
}