package com.tigshop.bean.vo.panel;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 趋势统计图
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Getter
@Setter
@Schema(description = "趋势统计图")
public class AxisVO {

    @Schema(description = "horizontalAxis")
    private List<String> horizontalAxis;

    @Schema(description = "longitudinalAxis")
    private List<BigDecimal> longitudinalAxis;

}
