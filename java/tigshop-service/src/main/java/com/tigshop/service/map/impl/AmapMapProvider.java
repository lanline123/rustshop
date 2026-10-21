// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.map.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.dto.map.MapLocationResult;
import com.tigshop.bean.enums.setting.MapProviderEnum;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.feign.amap.GeoCityResponse;
import com.tigshop.bean.feign.amap.GeoResponse;
import com.tigshop.bean.feign.amap.InputTipsResponse;
import com.tigshop.bean.feign.amap.RegeoDetailResponse;
import com.tigshop.bean.model.setting.Region;
import com.tigshop.common.utils.IpUtils;
import com.tigshop.feign.AmapFeignClient;
import com.tigshop.service.map.MapProvider;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.setting.RegionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.TreeMap;

import static com.tigshop.common.utils.AMapSignUtils.generateSignature;

/**
 * 高德地图服务商实现，坐标系 GCJ-02。
 * <p>
 * 本类的五段逻辑由 {@code MapPositioningServiceImpl} <b>原样搬迁</b>：取 key/secret 的方式、
 * 签名调用、feign 参数、{@code getAdcodeSafe()}、行政层级归一的 level 分支、
 * 异常降级到北京、日志级别与文案均保持既有行为，不做任何顺手优化。
 *
 * @author Tigshop团队
 * @create 2026/7/31
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AmapMapProvider implements MapProvider {

    /** 降级目标：北京市，沿用既有实现的硬编码值 */
    private static final int FALLBACK_REGION_ID = 110100;

    private final AmapFeignClient amapFeignClient;
    private final ConfigService configService;
    private final RegionService regionService;

    @Override
    public MapProviderEnum provider() {
        return MapProviderEnum.AMAP;
    }

    @Override
    public MapLocationResult locateByIp(String ip) {
        if (ObjectUtil.isEmpty(ip)) {
            ip = IpUtils.getIpAddr();
        }
        log.error("获取定位ip: " + ip);
        String key = configService.getConfigVal(SettingsEnum.AMAP_WEB_KEY);
        String secret = configService.getConfigVal(SettingsEnum.AMAP_WEB_SECRET);
        Map<String, String> params = new TreeMap<>();
        params.put("key", key);
        params.put("ip", ip);
        String sig = null;
        if (ObjectUtil.isNotEmpty(secret)) {
            sig = generateSignature(params, secret);
        }
        String jsonStr = JSONUtil.toJsonStr(amapFeignClient.getLocationByIp(ip, key, sig));
        return resolveRegion(jsonStr);
    }

    @Override
    public MapLocationResult locateByCoordinate(String location) {
        String key = configService.getConfigVal(SettingsEnum.AMAP_WEB_KEY);
        String secret = configService.getConfigVal(SettingsEnum.AMAP_WEB_SECRET);
        Map<String, String> params = new TreeMap<>();
        params.put("key", key);
        params.put("location", location);
        String sig = null;
        if (ObjectUtil.isNotEmpty(secret)) {
            sig = generateSignature(params, secret);
        }
        String jsonStr = JSONUtil.toJsonStr(amapFeignClient.regeo(location, key, sig));
        return resolveRegion(jsonStr);
    }

    /**
     * 解析高德定位响应并归一到 region 表的城市级行政区。
     * <p>
     * level 分支语义沿用既有实现：2 取首个子级（直辖市）、3 直接用、4 取父级；
     * 任何异常降级为北京市。归一不出行政区时后续取中心点会抛 NPE，
     * 与既有 {@code getRegionLocation(null)} 行为一致（已知既有缺陷，本轮不修，见 CR-20260731-1450 §四）。
     */
    private MapLocationResult resolveRegion(String jsonStr) {
        log.error("获取定位json: " + jsonStr);
        Region returnRegion = null;
        try {
            GeoResponse geoResponse = JSONUtil.toBean(jsonStr, GeoResponse.class);
            String adcodeSafe = geoResponse.getAdcodeSafe();
            if (ObjectUtil.isNotEmpty(adcodeSafe)) {
                Region region = regionService.getOne(new LambdaQueryWrapper<Region>().eq(Region::getRegionId, adcodeSafe));
                if (region.getLevel() == 2) {
                    // 直辖市
                    returnRegion = regionService.getOne(new LambdaQueryWrapper<Region>().eq(Region::getParentId, region.getRegionId()).last("limit 1"));
                }
                if (region.getLevel() == 3) {
                    returnRegion = region;
                }
                if (region.getLevel() == 4) {
                    returnRegion = regionService.getOne(new LambdaQueryWrapper<Region>().eq(Region::getRegionId, region.getParentId()));
                }
            }
        } catch (Exception e) {
            log.error("获取定位失败", e);
            // 降级处理返回北京市
            returnRegion = regionService.getOne(new LambdaQueryWrapper<Region>().eq(Region::getRegionId, FALLBACK_REGION_ID));
        }

        Integer regionId = returnRegion == null ? null : returnRegion.getRegionId();
        return MapLocationResult.of(returnRegion, resolveCenter(regionId));
    }

    @Override
    public String resolveCenter(Integer regionId) {
        String key = configService.getConfigVal(SettingsEnum.AMAP_WEB_KEY);
        String secret = configService.getConfigVal(SettingsEnum.AMAP_WEB_SECRET);
        Map<String, String> params = new TreeMap<>();
        params.put("key", key);
        params.put("keywords", regionId.toString());
        params.put("subdistrict", "0");
        String sig = null;
        if (ObjectUtil.isNotEmpty(secret)) {
            sig = generateSignature(params, secret);
        }
        String jsonStr = JSONUtil.toJsonStr(amapFeignClient.district(regionId.toString(), "0", key, sig));
        String centor = null;
        try {
            GeoCityResponse geoResponse = JSONUtil.toBean(jsonStr, GeoCityResponse.class);
            for (GeoCityResponse.District district : geoResponse.getDistricts()) {
                if (ObjectUtil.equals(district.getAdcode(), regionId.toString())) {
                    // 不提前 break：既有实现遍历全部匹配项，多匹配时以最后一项为准
                    centor = district.getCenter();
                }
            }
        } catch (Exception e) {
            log.error("获取定位失败", e);
        }
        return centor;
    }

    @Override
    public RegeoDetailResponse regeoDetail(String longitude, String latitude, Integer radius, String extensions) {
        String location = longitude + "," + latitude;
        String key = configService.getConfigVal(SettingsEnum.AMAP_WEB_KEY);
        String secret = configService.getConfigVal(SettingsEnum.AMAP_WEB_SECRET);

        Map<String, String> params = new TreeMap<>();
        params.put("key", key);
        params.put("location", location);
        if (radius != null) {
            params.put("radius", radius.toString());
        }
        if (ObjectUtil.isNotEmpty(extensions)) {
            params.put("extensions", extensions);
        }

        String sig = null;
        if (ObjectUtil.isNotEmpty(secret)) {
            sig = generateSignature(params, secret);
        }

        try {
            String jsonStr = JSONUtil.toJsonStr(amapFeignClient.regeoDetail(
                location, key, sig,
                radius != null ? radius.toString() : null,
                extensions, null, null));

            log.info("高德逆地理编码详细查询结果: " + jsonStr);
            return JSONUtil.toBean(jsonStr, RegeoDetailResponse.class);
        } catch (Exception e) {
            log.error("逆地理编码详细查询失败", e);
            // 返回空对象，避免接口异常
            RegeoDetailResponse errorResponse = new RegeoDetailResponse();
            errorResponse.setStatus("0");
            errorResponse.setInfo("查询失败");
            return errorResponse;
        }
    }

    @Override
    public InputTipsResponse inputTips(String keywords, String location) {
        String key = configService.getConfigVal(SettingsEnum.AMAP_WEB_KEY);
        String secret = configService.getConfigVal(SettingsEnum.AMAP_WEB_SECRET);

        Map<String, String> params = new TreeMap<>();
        params.put("key", key);
        params.put("keywords", keywords);
        if (ObjectUtil.isNotEmpty(location)) {
            params.put("location", location);
        }

        String sig = null;
        if (ObjectUtil.isNotEmpty(secret)) {
            sig = generateSignature(params, secret);
        }

        try {
            String jsonStr = JSONUtil.toJsonStr(amapFeignClient.inputTips(
                keywords, key, sig, null, location, null, null));

            log.info("高德地图输入提示查询结果: " + jsonStr);
            return JSONUtil.toBean(jsonStr, InputTipsResponse.class);
        } catch (Exception e) {
            log.error("输入提示查询失败", e);
            // 返回空对象，避免接口异常
            InputTipsResponse errorResponse = new InputTipsResponse();
            errorResponse.setStatus("0");
            errorResponse.setInfo("查询失败");
            return errorResponse;
        }
    }
}
