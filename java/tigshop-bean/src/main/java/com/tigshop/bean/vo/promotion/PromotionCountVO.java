package com.tigshop.bean.vo.promotion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Tigshop团队
 * @create 2025/4/2 9:45
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "活动数量")
public class PromotionCountVO {

    private Integer timeType1Count;
    private Integer timeType2Count;
    private Integer timeType3Count;
}
