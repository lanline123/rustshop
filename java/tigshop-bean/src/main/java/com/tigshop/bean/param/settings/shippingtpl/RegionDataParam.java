package com.tigshop.bean.param.settings.shippingtpl;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 区域数据入参
 *
 * @author kidd
 * @since 2025/4/22 10:39
 */
@Data
@Schema(description = "区域数据")
public class RegionDataParam {

    @Schema(description = "区域ID列表")
    private List<List<Long>> areaRegions;

    @Schema(description = "区域名称列表")
    private List<String> areaRegionNames;

}
