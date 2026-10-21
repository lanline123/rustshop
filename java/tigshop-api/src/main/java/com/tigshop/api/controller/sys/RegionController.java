package com.tigshop.api.controller.sys;

import com.tigshop.bean.feign.amap.InputTipsResponse;
import com.tigshop.bean.feign.amap.RegeoDetailResponse;
import com.tigshop.bean.model.setting.Region;
import com.tigshop.bean.vo.sys.CityRegionVO;
import com.tigshop.bean.vo.sys.RegionVO;
import com.tigshop.service.setting.RegionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 地区控制器
 *
 * @author Jayce
 * @create 2024/9/30 15:56
 */
@RestController
@RequestMapping("/api/sys/region")
@Tag(name = "地区")
@Validated
public class RegionController {

    @Resource
    private RegionService regionService;

    @GetMapping("/getRegion")
    @Operation(summary = "地区列表")
    public List<List<Region>> getRegion(@RequestParam(value = "regionIds", required = false) String regionIds) {
        return regionService.getRegionByIds(regionIds);
    }


    @GetMapping("/getProvinceList")
    @Operation(summary = "省份列表")
    public List<Region> getProvinceList() {
        return regionService.getProvinceList();
    }

    @GetMapping("/getUserRegion")
    @Operation(summary = "用户地区列表")
    public Region getUserRegion() {
        return regionService.getUserRegion();
    }

    @GetMapping("/getCityRegion")
    @Operation(summary = "用户地区列表")
    public CityRegionVO getCityRegion() {
        return regionService.getCityRegion();
    }

    @GetMapping("/getCityListByCityName")
    @Operation(summary = "用户地区列表")
    public List<Region> getCityListByCityName(String cityName) {
        return regionService.getCityListByCityName(cityName);
    }


    @GetMapping("/getRegionListByRegionId")
    @Operation(summary = "获取子集通过RegionId")
    public List<Region> getRegionListByRegionId(@RequestParam(value = "regionId") Integer regionId) {
        return regionService.getRegionByParentId(regionId);
    }


    @GetMapping("/getRegionPath")
    @Operation(summary = "根据regionId获取完整地区路径")
    public List<Region> getRegionPath(@RequestParam("regionId") Integer regionId) {
        return regionService.getRegionPath(regionId);
    }

}
