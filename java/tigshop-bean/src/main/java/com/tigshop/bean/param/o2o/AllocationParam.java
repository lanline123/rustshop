package com.tigshop.bean.param.o2o;

import com.tigshop.bean.enums.o2o.AllocationTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Tigshop团队
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AllocationParam {
    @Schema(description = "分配类型")
    private AllocationTypeEnum type;

    @Schema(description = "区域")
    private Integer areaId;

    @Schema(description = "门店集合")
    private List<Integer> shopIds;

    @Schema(description = "商品集合")
    private List<Integer> productIds;
}