// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.map.support;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.tigshop.bean.feign.amap.InputTipsResponse;
import com.tigshop.bean.feign.amap.RegeoDetailResponse;
import com.tigshop.bean.feign.tianditu.TdtGeocoderResponse;
import com.tigshop.bean.feign.tianditu.TdtSearchResponse;
import com.tigshop.bean.model.setting.Region;
import com.tigshop.service.setting.RegionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 天地图原生响应 → 高德对外契约类型的<b>单向</b>适配。
 * <p>
 * 只有天地图侧经过本类；高德侧恒等透传、零转换（见 CR-20260731-1450：
 * 对外契约本身就是高德结构，「高德原生 → 中立 → 高德原生」的往返会丢字段）。
 *
 * @author Tigshop团队
 * @create 2026/8/3
 */
@Component
@RequiredArgsConstructor
public class TiandituMapResponseAdapter {

    /** 天地图服务正常 */
    private static final int TDT_INFOCODE_OK = 1000;

    /** 天地图「没有找到数据」，属正常无结果而非故障 */
    private static final int TDT_INFOCODE_NO_DATA = 3001;

    /** 天地图普通 POI 结果集，只有该类型才有 pois */
    private static final int TDT_RESULT_TYPE_POI = 1;

    /** geocoder 的正常状态码，为字符串且与 v2 两接口互不相同 */
    private static final String TDT_GEOCODER_STATUS_OK = "0";

    /** region 表层级 */
    private static final int REGION_LEVEL_DISTRICT = 4;
    private static final int REGION_LEVEL_CITY = 3;
    private static final int REGION_LEVEL_PROVINCE = 2;

    /** 祖先链上溯上限：区县 → 市 → 省，加一层余量 */
    private static final int MAX_ANCESTOR_WALK = 4;

    /** 高德成功状态 */
    private static final String AMAP_STATUS_SUCCESS = "1";

    /** 高德失败状态 */
    private static final String AMAP_STATUS_FAILURE = "0";

    /** 高德成功时的固定信息码 */
    private static final String AMAP_INFOCODE_SUCCESS = "10000";

    private final GbCodeConverter gbCodeConverter;

    /**
     * 逆地理详情的 province / city / district 由 region 表回填标准名。
     * <p>
     * 不直接用天地图 {@code addressComponent.city}：官方对该字段的表述是
     * 「此点所在国家或城市或区县」，语义不固定，可能把区县名塞进 city，
     * 那才是真正的契约破坏。按 region 祖先链回填可保证 province=省 / city=市 / district=区县，
     * 与高德逐级对齐（REQ-TD-11.3）。
     */
    private final RegionService regionService;

    /**
     * 逆地理编码响应 → 逆地理详情响应。
     * <p>
     * 核心字段零退化（REQ-TD-11.3）：{@code formatted_address} / {@code province} /
     * {@code city} / {@code district} / {@code adcode} 均有值。允许的降级见
     * {@link #fillDegradedFields}。失败时返回 {@code status="0"} 的空对象而不抛错。
     */
    public RegeoDetailResponse toRegeoDetail(TdtGeocoderResponse source) {
        if (source == null
                || !ObjectUtil.equals(TDT_GEOCODER_STATUS_OK, source.getStatus())
                || source.getResult() == null) {
            return failedRegeo(source);
        }

        RegeoDetailResponse response = new RegeoDetailResponse();
        response.setStatus(AMAP_STATUS_SUCCESS);
        response.setInfo("OK");
        response.setInfocode(AMAP_INFOCODE_SUCCESS);
        response.setRegeocode(toRegeocode(source.getResult()));
        return response;
    }

    private RegeoDetailResponse failedRegeo(TdtGeocoderResponse source) {
        RegeoDetailResponse response = new RegeoDetailResponse();
        response.setStatus(AMAP_STATUS_FAILURE);
        response.setInfo(source == null || ObjectUtil.isEmpty(source.getMsg())
                ? "查询失败" : source.getMsg());
        return response;
    }

    private RegeoDetailResponse.Regeocode toRegeocode(TdtGeocoderResponse.Result result) {
        RegeoDetailResponse.Regeocode regeocode = new RegeoDetailResponse.Regeocode();
        regeocode.setFormatted_address(result.getFormatted_address());

        TdtGeocoderResponse.Result.AddressComponent source = result.getAddressComponent();
        Region matched = matchRegion(source);
        String adcode = matched == null ? null : String.valueOf(matched.getRegionId());

        regeocode.setAddressComponent(toAddressComponent(source, matched, adcode));
        regeocode.setPois(toPois(result, adcode));
        regeocode.setRoads(toRoads(result));
        // 天地图无面状兴趣区概念；给空数组而非 null，前端遍历不会 NPE
        regeocode.setAois(Collections.emptyList());
        return regeocode;
    }

    private RegeoDetailResponse.Regeocode.AddressComponent toAddressComponent(
            TdtGeocoderResponse.Result.AddressComponent source, Region matched, String adcode) {
        RegeoDetailResponse.Regeocode.AddressComponent target =
                new RegeoDetailResponse.Regeocode.AddressComponent();
        if (source == null) {
            return target;
        }
        target.setCountry(source.getNation());
        target.setAdcode(adcode);
        fillAdministrativeNames(target, source, matched);
        fillDegradedFields(target, source);
        return target;
    }

    /**
     * 回填 province / city / district。
     * <p>
     * 以 {@code region} 表祖先链为准，逐级按 level 落位，保证语义与高德一致。
     * 行政区码匹配不上时才退用天地图原始名称——此时 {@code city} 可能实际是区县名，
     * 属已知降级，但比整个字段为空更有用。
     */
    private void fillAdministrativeNames(RegeoDetailResponse.Regeocode.AddressComponent target,
                                         TdtGeocoderResponse.Result.AddressComponent source,
                                         Region matched) {
        if (matched == null) {
            target.setProvince(source.getProvince());
            target.setCity(source.getCity());
            target.setDistrict(source.getCounty());
            return;
        }
        Region current = matched;
        List<Integer> visited = new ArrayList<>(MAX_ANCESTOR_WALK);
        while (current != null && visited.size() < MAX_ANCESTOR_WALK) {
            visited.add(current.getRegionId());
            assignByLevel(target, current);

            Integer parentId = current.getParentId();
            // parent_id 为 0 是 region 表顶层的约定；visited 判定使自指脏数据不会死循环
            if (parentId == null || parentId <= 0 || visited.contains(parentId)) {
                break;
            }
            current = regionService.getById(parentId);
        }
    }

    private void assignByLevel(RegeoDetailResponse.Regeocode.AddressComponent target, Region region) {
        if (region.getLevel() == null) {
            return;
        }
        switch (region.getLevel()) {
            case REGION_LEVEL_DISTRICT:
                target.setDistrict(region.getRegionName());
                break;
            case REGION_LEVEL_CITY:
                target.setCity(region.getRegionName());
                break;
            case REGION_LEVEL_PROVINCE:
                target.setProvince(region.getRegionName());
                break;
            default:
                break;
        }
    }

    /**
     * 填充 spec REQ-TD-11.4 允许的降级字段。
     * <p>
     * {@code township} / {@code towncode} 是<b>完全兼容</b>的（CR-20260803-0925 §C7）：
     * 天地图 {@code town_code} 为 12 位，去 {@code 156} 前缀后与高德 {@code towncode} 位数一致。
     * {@code neighborhood} / {@code building} 用最近 POI 名称近似。
     * {@code citycode} 不可达，天地图无城市电话区号概念。
     */
    private void fillDegradedFields(RegeoDetailResponse.Regeocode.AddressComponent target,
                                    TdtGeocoderResponse.Result.AddressComponent source) {
        target.setTownship(source.getTown());
        target.setTowncode(gbCodeConverter.stripGb(source.getTown_code()));
        target.setNeighborhood(source.getPoi());
        target.setBuilding(source.getPoi());
        target.setCitycode(null);
    }

    /**
     * 按行政区码由细到粗匹配 region 表，与 provider 侧路径 A 同一约定。
     */
    private Region matchRegion(TdtGeocoderResponse.Result.AddressComponent source) {
        if (source == null) {
            return null;
        }
        String[] codes = {source.getCounty_code(), source.getCity_code(), source.getProvince_code()};
        for (String code : codes) {
            Integer regionId = gbCodeConverter.stripGbToRegionId(code);
            if (regionId == null) {
                continue;
            }
            Region region = regionService.getById(regionId);
            if (region != null) {
                return region;
            }
        }
        return null;
    }

    /**
     * 天地图 geocoder 只给一个最近 POI，故构造单元素列表；无 POI 时给空数组，
     * 不放 name 为 null 的占位元素。
     */
    private List<RegeoDetailResponse.Regeocode.Poi> toPois(TdtGeocoderResponse.Result result, String adcode) {
        TdtGeocoderResponse.Result.AddressComponent source = result.getAddressComponent();
        if (source == null || ObjectUtil.isEmpty(source.getPoi())) {
            return Collections.emptyList();
        }
        RegeoDetailResponse.Regeocode.Poi poi = new RegeoDetailResponse.Regeocode.Poi();
        poi.setName(source.getPoi());
        poi.setAddress(source.getAddress());
        poi.setLocation(formatLocation(result.getLocation()));
        poi.setDistance(toStringOrNull(source.getPoi_distance()));
        poi.setAdcode(adcode);
        return List.of(poi);
    }

    /**
     * 同 {@link #toPois}：天地图仅给一条最近道路。
     */
    private List<RegeoDetailResponse.Regeocode.Road> toRoads(TdtGeocoderResponse.Result result) {
        TdtGeocoderResponse.Result.AddressComponent source = result.getAddressComponent();
        if (source == null || ObjectUtil.isEmpty(source.getRoad())) {
            return Collections.emptyList();
        }
        RegeoDetailResponse.Regeocode.Road road = new RegeoDetailResponse.Regeocode.Road();
        road.setName(source.getRoad());
        road.setLocation(formatLocation(result.getLocation()));
        road.setDistance(toStringOrNull(source.getRoad_distance()));
        return List.of(road);
    }

    /**
     * 坐标转高德的 {@code "经度,纬度"}。
     */
    private String formatLocation(TdtGeocoderResponse.Result.Location location) {
        if (location == null || location.getLon() == null || location.getLat() == null) {
            return null;
        }
        return location.getLon() + "," + location.getLat();
    }

    private String toStringOrNull(Integer value) {
        return value == null ? null : String.valueOf(value);
    }

    /**
     * 地名搜索响应 → 输入提示响应。
     * <p>
     * 结构、字段名、嵌套层级与高德完全一致，前端零改动。失败时返回 status 为 "0"
     * 的空对象而不抛错，与既有行为一致。
     */
    public InputTipsResponse toInputTips(TdtSearchResponse source) {
        Integer infocode = source == null || source.getStatus() == null
                ? null : source.getStatus().getInfocode();

        if (!isSuccessful(infocode)) {
            return failedTips(source);
        }

        InputTipsResponse response = new InputTipsResponse();
        response.setStatus(AMAP_STATUS_SUCCESS);
        response.setInfo("OK");
        response.setInfocode(AMAP_INFOCODE_SUCCESS);
        response.setTips(toTips(source));
        response.setCount(String.valueOf(response.getTips().size()));
        return response;
    }

    /**
     * {@code 1000} 正常与 {@code 3001} 无数据都算成功——高德在无结果时同样返回 status=1
     * 加空 tips，若把无结果当失败会让前端把「搜不到」显示成「出错了」。
     */
    private boolean isSuccessful(Integer infocode) {
        return ObjectUtil.equals(TDT_INFOCODE_OK, infocode)
                || ObjectUtil.equals(TDT_INFOCODE_NO_DATA, infocode);
    }

    private InputTipsResponse failedTips(TdtSearchResponse source) {
        InputTipsResponse response = new InputTipsResponse();
        response.setStatus(AMAP_STATUS_FAILURE);
        response.setInfo(source == null || source.getStatus() == null
                || ObjectUtil.isEmpty(source.getStatus().getCndesc())
                ? "查询失败" : source.getStatus().getCndesc());
        response.setTips(Collections.emptyList());
        response.setCount("0");
        return response;
    }

    /**
     * 取 POI 列表。
     * <p>
     * 只有 {@code resultType=1} 才有 POI：输入宽泛词时天地图会返回统计(2)或行政区(3)，
     * 而高德 inputtips 恒返回 tips 数组，故此处以「无候选项」对齐，不视为错误。
     */
    private List<InputTipsResponse.Tip> toTips(TdtSearchResponse source) {
        if (!ObjectUtil.equals(TDT_RESULT_TYPE_POI, source.getResultType())
                || CollUtil.isEmpty(source.getPois())) {
            return Collections.emptyList();
        }
        List<InputTipsResponse.Tip> tips = new ArrayList<>(source.getPois().size());
        for (TdtSearchResponse.Poi poi : source.getPois()) {
            tips.add(toTip(poi));
        }
        return tips;
    }

    private InputTipsResponse.Tip toTip(TdtSearchResponse.Poi poi) {
        InputTipsResponse.Tip tip = new InputTipsResponse.Tip();
        tip.setId(poi.getHotPointID());
        tip.setName(poi.getName());
        tip.setAddress(poi.getAddress());
        tip.setLocation(normalizeLonlat(poi.getLonlat()));
        tip.setDistrict(concatDistrict(poi));
        tip.setAdcode(gbCodeConverter.stripGb(poi.getCountyCode()));
        // 天地图分类体系独立于高德，值可给出但不可跨服务商比对
        tip.setTypecode(poi.getTypeCode());
        // 天地图无城市电话区号概念，该字段不可达
        tip.setCitycode(null);
        return tip;
    }

    /**
     * 坐标归一为高德的 {@code "经度,纬度"}。天地图 v2 用逗号，旧版接口用空格，两者都收。
     */
    private String normalizeLonlat(String lonlat) {
        if (ObjectUtil.isEmpty(lonlat)) {
            return null;
        }
        return lonlat.trim().replaceAll("[\\s,]+", ",");
    }

    /**
     * 拼接省市区为高德的 {@code district}。
     * <p>
     * 这三个字段在天地图是<b>非必返回</b>项（官方响应实例中即缺席），全缺时只能给 null，
     * 见 CR-20260803-0925 §C3。
     */
    private String concatDistrict(TdtSearchResponse.Poi poi) {
        StringBuilder builder = new StringBuilder();
        appendIfPresent(builder, poi.getProvince());
        appendIfPresent(builder, poi.getCity());
        appendIfPresent(builder, poi.getCounty());
        return builder.length() == 0 ? null : builder.toString();
    }

    private void appendIfPresent(StringBuilder builder, String value) {
        if (ObjectUtil.isNotEmpty(value)) {
            builder.append(value);
        }
    }
}
