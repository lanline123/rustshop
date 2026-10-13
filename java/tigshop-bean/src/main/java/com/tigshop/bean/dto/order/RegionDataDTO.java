package com.tigshop.bean.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author wzh
 */
@Data
public class RegionDataDTO {

    @Schema(description = "区域ID列表")
    private List<List<Integer>> areaRegions;

    @Schema(description = "区域名称列表")
    private List<String> areaRegionNames;
}
