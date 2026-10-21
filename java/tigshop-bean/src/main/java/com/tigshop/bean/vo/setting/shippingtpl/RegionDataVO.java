package com.tigshop.bean.vo.setting.shippingtpl;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 地区信息
 *
 * @author kidd
 * @since 2025/4/21 17:44
 */
@Schema(description = "地区信息")
@Data
public class RegionDataVO {

    @Schema(description = "地区id信息")
    private List<List<Long>> areaRegions;

    @Schema(description = "地区name信息")
    private List<String> areaRegionNames;


}
