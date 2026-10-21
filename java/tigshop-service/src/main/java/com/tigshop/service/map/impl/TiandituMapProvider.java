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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.dto.map.MapLocationResult;
import com.tigshop.bean.enums.setting.MapProviderEnum;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.feign.amap.InputTipsResponse;
import com.tigshop.bean.feign.amap.RegeoDetailResponse;
import com.tigshop.bean.feign.tianditu.TdtAdministrativeResponse;
import com.tigshop.bean.feign.tianditu.TdtGeocoderResponse;
import com.tigshop.bean.feign.tianditu.TdtSearchResponse;
import com.tigshop.bean.model.setting.Region;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.Ip2RegionUtil;
import com.tigshop.common.utils.IpUtils;
import com.tigshop.feign.TiandituFeignClient;
import com.tigshop.service.map.MapProvider;
import com.tigshop.service.map.support.GbCodeConverter;
import com.tigshop.service.map.support.TiandituMapResponseAdapter;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.setting.RegionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 天地图服务商实现，坐标系 CGCS2000（近似 WGS-84）。
 * <p>
 * 与 {@link AmapMapProvider} <b>零相互依赖</b>：不引用高德的客户端、签名工具或任何
 * 高德专属解析逻辑，能力缺失时只在天地图自身体系内做行政层级回退，绝不回落高德（D2）。
 * 返回类型 {@code InputTipsResponse} / {@code RegeoDetailResponse} 虽是高德形状，
 * 但它们已是既有对外契约，由本类的适配器单向产出，不构成对高德实现的依赖。
 * <p>
 * 天地图三个接口的状态约定互不相同，成功判定必须分接口处理：
 * {@code geocoder} 为字符串 {@code "0"}、{@code v2/search} 为对象 {@code infocode=1000}、
 * {@code v2/administrative} 为数字 {@code 200}（见 CR-20260803-0925 §C1）。
 *
 * @author Tigshop团队
 * @create 2026/8/3
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TiandituMapProvider implements MapProvider {

    /** v2/administrative 的正常状态码 */
    private static final int ADMIN_STATUS_OK = 200;

    /** geocoder 的正常状态码，为字符串且与 v2 两接口互不相同 */
    private static final String GEOCODER_STATUS_OK = "0";

    /** 降级目标：北京市，沿用既有实现的硬编码值 */
    private static final int FALLBACK_REGION_ID = 110100;

    /** region 表的区县级 */
    private static final int REGION_LEVEL_DISTRICT = 4;

    /** region 表的城市级 */
    private static final int REGION_LEVEL_CITY = 3;

    /** region 表的省级 */
    private static final int REGION_LEVEL_PROVINCE = 2;

    /** 行政区划名称后缀，长者在前以免「特别行政区」被「区」先截断 */
    private static final String[] REGION_NAME_SUFFIXES = {"特别行政区", "自治区", "省", "市"};

    /** 全域检索范围，用于对齐高德 inputtips 无视野约束的语义 */
    private static final String NATIONWIDE_MAP_BOUND = "-180,-90,180,90";

    /** 搜索级别，取官方示例值 */
    private static final String SEARCH_LEVEL = "12";

    private final TiandituFeignClient tiandituFeignClient;
    private final ConfigService configService;
    private final GbCodeConverter gbCodeConverter;
    private final RegionService regionService;
    private final TiandituMapResponseAdapter responseAdapter;

    @Override
    public MapProviderEnum provider() {
        return MapProviderEnum.TIANDITU;
    }

    /**
     * IP 定位到行政区。
     * <p>
     * 天地图官方无 IP 定位接口，故走 ip2region 离线库取城市名再匹配 region 表，
     * 全程不调用高德任何接口。城市级未命中时降级到省级，仍未命中则默认北京市。
     */
    @Override
    public MapLocationResult locateByIp(String ip) {
        String clientIp = ObjectUtil.isEmpty(ip) ? IpUtils.getIpAddr() : ip;
        Region region = resolveRegionByIp(clientIp);
        if (region == null) {
            log.warn("天地图 IP 定位未匹配到行政区，默认北京市, ip={}", clientIp);
            region = regionService.getById(FALLBACK_REGION_ID);
        }
        Integer regionId = region == null ? null : region.getRegionId();
        return MapLocationResult.of(region, regionId == null ? null : resolveCenter(regionId));
    }

    /**
     * 由 ip2region 的省市名称匹配 region 表。
     * <p>
     * 必须按名称匹配 region 表（离线库只给名称），但**匹配天地图接口时一律用码**，
     * 因为名称在两侧不一致（如「广西」与「广西壮族自治区」）。
     */
    private Region resolveRegionByIp(String ip) {
        try {
            Region city = findByName(Ip2RegionUtil.getCityByIp(ip), REGION_LEVEL_CITY);
            if (city != null) {
                return city;
            }
            return findByName(Ip2RegionUtil.getProvinceByIp(ip), REGION_LEVEL_PROVINCE);
        } catch (Exception e) {
            log.error("ip2region 查询失败, ip=" + ip, e);
            return null;
        }
    }

    /**
     * 按归一后的名称前缀匹配指定层级的行政区。
     */
    private Region findByName(String rawName, int level) {
        String normalized = normalizeRegionName(rawName);
        if (ObjectUtil.isEmpty(normalized)) {
            return null;
        }
        return regionService.getOne(new LambdaQueryWrapper<Region>()
                .eq(Region::getLevel, level)
                .likeRight(Region::getRegionName, normalized)
                .last("limit 1"));
    }

    /**
     * 剥离行政区划后缀，使 ip2region 的简称能前缀匹配上 region 表的全称。
     * <p>
     * 例：ip2region 给「北京市」而 region 表存「北京市」可直接命中；
     * 但给「广西」时需剥离后以「广西」前缀匹配到「广西壮族自治区」。
     */
    private String normalizeRegionName(String name) {
        if (ObjectUtil.isEmpty(name)) {
            return null;
        }
        String normalized = name;
        for (String suffix : REGION_NAME_SUFFIXES) {
            if (normalized.endsWith(suffix) && normalized.length() > suffix.length()) {
                normalized = normalized.substring(0, normalized.length() - suffix.length());
                break;
            }
        }
        return normalized;
    }

    /**
     * 坐标逆定位到行政区，走<b>路径 A</b>：geocoder 返回的 9 位国标码去 {@code 156}
     * 前缀后与 {@code region_id} 精确匹配，与高德 {@code getAdcodeSafe()} → adcode 语义等价。
     * <p>
     * 结果必须归一到城市级（见 {@link #normalizeToCity}），否则同一坐标在两个服务商下
     * 会拿到不同层级的 {@code RegionVO}。异常时沿用既有行为降级到北京市。
     */
    @Override
    public MapLocationResult locateByCoordinate(String location) {
        String tk = requireTk();
        Region returnRegion = null;
        try {
            TdtGeocoderResponse response = requestGeocoder(location, tk);
            if (response != null) {
                returnRegion = normalizeToCity(matchRegion(response.getResult()));
            }
        } catch (Exception e) {
            log.error("获取定位失败", e);
            // 降级处理返回北京市
            returnRegion = regionService.getById(FALLBACK_REGION_ID);
        }
        Integer regionId = returnRegion == null ? null : returnRegion.getRegionId();
        return MapLocationResult.of(returnRegion, regionId == null ? null : resolveCenter(regionId));
    }

    /**
     * 调用天地图逆地理编码。
     *
     * @return 坐标串非法或业务失败时返回 null，由调用方按「取不到行政区」处理
     */
    private TdtGeocoderResponse requestGeocoder(String location, String tk) {
        JSONObject postStr = buildGeocoderPostStr(location);
        if (postStr == null) {
            log.error("天地图逆地理坐标串非法: {}", location);
            return null;
        }
        String jsonStr = JSONUtil.toJsonStr(
                tiandituFeignClient.geocoder(postStr.toString(), "geocode", tk));
        TdtGeocoderResponse response = JSONUtil.toBean(jsonStr, TdtGeocoderResponse.class);

        if (!ObjectUtil.equals(GEOCODER_STATUS_OK, response.getStatus())) {
            log.error("天地图逆地理编码异常, status={}, msg={}", response.getStatus(), response.getMsg());
            return null;
        }
        return response.getResult() == null ? null : response;
    }

    /**
     * 构造 geocoder 请求体，官方约定为 {@code {"lon":x,"lat":y,"ver":1}}。
     * <p>
     * 入参沿用高德的 {@code "经度,纬度"} 顺序。
     *
     * @return 坐标串非法时返回 null
     */
    private JSONObject buildGeocoderPostStr(String location) {
        if (ObjectUtil.isEmpty(location)) {
            return null;
        }
        String[] parts = location.split(",");
        if (parts.length != 2) {
            return null;
        }
        try {
            JSONObject postStr = new JSONObject();
            postStr.set("lon", Double.parseDouble(parts[0].trim()));
            postStr.set("lat", Double.parseDouble(parts[1].trim()));
            postStr.set("ver", 1);
            return postStr;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 按行政区码由细到粗匹配 region 表。
     * <p>
     * 只用码不用名称：名称在两侧不一致（`广西` vs `广西壮族自治区`）会失配（REQ-TD-11.6）。
     * 三级码全缺或均匹配不上时返回 null——路径 A 无码可用时不猜名称。
     */
    private Region matchRegion(TdtGeocoderResponse.Result result) {
        TdtGeocoderResponse.Result.AddressComponent component = result.getAddressComponent();
        if (component == null) {
            return null;
        }
        String[] codes = {component.getCounty_code(), component.getCity_code(), component.getProvince_code()};
        for (String code : codes) {
            Integer regionId = gbCodeConverter.stripGbToRegionId(code);
            if (regionId == null) {
                continue;
            }
            Region region = regionService.getById(regionId);
            if (region != null) {
                return region;
            }
            log.error("天地图返回的行政区码在 region 表无对应记录: {}", code);
        }
        return null;
    }

    /**
     * 归一到城市级。
     * <p>
     * level 分支语义与 {@link AmapMapProvider} 逐条对齐：2 取首个子级（直辖市/省本级）、
     * 3 直接用、4 取父级。此处不复用高德实现，因 D2 要求两个 Provider 零相互依赖；
     * 重复的代价换取的是任一 Provider 改动不会波及另一个。
     *
     * @return 层级不在 2/3/4 之内或取不到上下级时返回 null，不静默换层级
     */
    private Region normalizeToCity(Region region) {
        if (region == null || region.getLevel() == null) {
            return null;
        }
        switch (region.getLevel()) {
            case REGION_LEVEL_PROVINCE:
                return regionService.getOne(new LambdaQueryWrapper<Region>()
                        .eq(Region::getParentId, region.getRegionId())
                        .last("limit 1"));
            case REGION_LEVEL_CITY:
                return region;
            case REGION_LEVEL_DISTRICT:
                return regionService.getById(region.getParentId());
            default:
                log.error("天地图定位命中的行政区层级异常, regionId={}, level={}",
                        region.getRegionId(), region.getLevel());
                return null;
        }
    }

    /**
     * 行政区划码取中心点。
     * <p>
     * 天地图行政区划收录范围小于 region 表，故按 {@link GbCodeConverter#fallbackChain}
     * 逐级向上重试，最多两级祖先。全量实证该策略覆盖率 100%。
     *
     * @return {@code "经度,纬度"}；回退链全部落空时返回 null
     */
    @Override
    public String resolveCenter(Integer regionId) {
        String tk = requireTk();
        List<Integer> chain = gbCodeConverter.fallbackChain(regionId);
        for (Integer candidate : chain) {
            String center = queryCenter(candidate, tk);
            if (ObjectUtil.isNotEmpty(center)) {
                if (!candidate.equals(regionId)) {
                    log.info("天地图无 {} 的行政区划，回退到上级 {} 取中心点", regionId, candidate);
                }
                return center;
            }
        }
        log.error("天地图取中心点失败，回退链均无结果: {}", chain);
        return null;
    }

    /**
     * 查单个行政区划的中心点。
     *
     * @return 取不到时返回 null，由调用方决定是否继续回退
     */
    private String queryCenter(Integer regionId, String tk) {
        String gb = gbCodeConverter.toGb(regionId);
        try {
            String jsonStr = JSONUtil.toJsonStr(
                    tiandituFeignClient.administrative(gb, "0", "false", tk));
            TdtAdministrativeResponse response = JSONUtil.toBean(jsonStr, TdtAdministrativeResponse.class);

            if (!ObjectUtil.equals(ADMIN_STATUS_OK, response.getStatus())) {
                log.error("天地图行政区划查询异常, gb={}, status={}, message={}",
                        gb, response.getStatus(), response.getMessage());
                return null;
            }
            // 查不到时 status 仍为 200，仅 district 为空，故空结果只能由此判定
            if (response.getData() == null || CollUtil.isEmpty(response.getData().getDistrict())) {
                return null;
            }
            return extractCenter(response.getData().getDistrict(), gb);
        } catch (Exception e) {
            log.error("天地图行政区划查询失败, gb=" + gb, e);
            return null;
        }
    }

    /**
     * 从行政区划列表取中心点，优先精确匹配 gb 码。
     * <p>
     * 按码查询时正常只返回一条，精确匹配是为了防止关键词被当作名称做了模糊匹配。
     */
    private String extractCenter(List<TdtAdministrativeResponse.AdministrativeData.District> districts, String gb) {
        String firstAvailable = null;
        for (TdtAdministrativeResponse.AdministrativeData.District district : districts) {
            String center = formatCenter(district);
            if (center == null) {
                continue;
            }
            if (ObjectUtil.equals(gb, district.getGb())) {
                return center;
            }
            if (firstAvailable == null) {
                firstAvailable = center;
            }
        }
        return firstAvailable;
    }

    /**
     * 中心点结构转既有 {@code centor} 契约的 {@code "经度,纬度"} 字符串。
     */
    private String formatCenter(TdtAdministrativeResponse.AdministrativeData.District district) {
        TdtAdministrativeResponse.AdministrativeData.District.Center center = district.getCenter();
        if (center == null || center.getLng() == null || center.getLat() == null) {
            return null;
        }
        return center.getLng() + "," + center.getLat();
    }

    /**
     * 逆地理详情，与 {@link #locateByCoordinate} 共用 geocoder 接口，差别只在适配目标。
     * <p>
     * {@code radius} 与 {@code extensions} 是高德专属参数，天地图 geocoder 无对应能力，
     * 故不参与请求，仅保留签名兼容——这不影响返回结构，高德下 {@code extensions=all}
     * 才有的 pois / roads 天地图同样会给（单元素，见 Parity Map 4.1）。
     */
    @Override
    public RegeoDetailResponse regeoDetail(String longitude, String latitude, Integer radius, String extensions) {
        String tk = requireTk();
        try {
            JSONObject postStr = buildGeocoderPostStr(longitude + "," + latitude);
            if (postStr == null) {
                log.error("天地图逆地理详情坐标非法, longitude={}, latitude={}", longitude, latitude);
                return responseAdapter.toRegeoDetail(null);
            }
            String jsonStr = JSONUtil.toJsonStr(
                    tiandituFeignClient.geocoder(postStr.toString(), "geocode", tk));
            log.info("天地图逆地理编码详细查询结果: " + jsonStr);
            return responseAdapter.toRegeoDetail(JSONUtil.toBean(jsonStr, TdtGeocoderResponse.class));
        } catch (Exception e) {
            log.error("逆地理编码详细查询失败", e);
            // 返回空对象，避免接口异常
            return responseAdapter.toRegeoDetail(null);
        }
    }

    /**
     * 关键字地址提示，走天地图普通搜索。
     * <p>
     * {@code mapBound} 与 {@code level} 是官方必填参数：前者取全域以对齐高德 inputtips
     * 的全国检索语义，后者取官方示例值。{@code show=2} 用于争取返回省市区名称与编码
     * （这些字段非必返回，缺失时 adcode / district 只能为 null）。
     * <p>
     * {@code location} 参数天地图普通搜索不支持坐标偏向，故不参与请求，仅保留签名兼容。
     */
    @Override
    public InputTipsResponse inputTips(String keywords, String location) {
        String tk = requireTk();
        try {
            JSONObject postStr = new JSONObject();
            postStr.set("keyWord", keywords);
            postStr.set("mapBound", NATIONWIDE_MAP_BOUND);
            postStr.set("level", SEARCH_LEVEL);
            postStr.set("queryType", "1");
            postStr.set("start", "0");
            postStr.set("count", "10");
            postStr.set("show", "2");

            String jsonStr = JSONUtil.toJsonStr(
                    tiandituFeignClient.search(postStr.toString(), "query", tk));
            log.info("天地图地名搜索结果: " + jsonStr);
            return responseAdapter.toInputTips(JSONUtil.toBean(jsonStr, TdtSearchResponse.class));
        } catch (Exception e) {
            log.error("天地图地名搜索失败", e);
            return responseAdapter.toInputTips(null);
        }
    }

    /**
     * 取服务端 tk。
     * <p>
     * 未配置时抛业务异常而非回落高德——切到天地图却没配密钥属配置错误，
     * 静默换服务商会让坐标系悄悄改变，比报错更危险。
     */
    private String requireTk() {
        String tk = configService.getConfigVal(SettingsEnum.TIANDITU_WEB_TK);
        if (ObjectUtil.isEmpty(tk)) {
            throw new GlobalException("未配置天地图密钥");
        }
        return tk;
    }
}
