package com.tigshop.bean.dto.setting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 初始化运费模板
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:13
 */
@Data
@Schema(description = "地区数据参数")
public class ShippingTplInitDataDTO {
    @Schema(description = "地区ID列表")
    private List<Integer> areaRegions;
    @Schema(description = "地区名称列表")
    private List<String> areaRegionNames;
}