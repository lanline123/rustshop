// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.map;

import com.tigshop.bean.dto.map.MapLocationResult;
import com.tigshop.bean.enums.setting.MapProviderEnum;
import com.tigshop.bean.feign.amap.InputTipsResponse;
import com.tigshop.bean.feign.amap.RegeoDetailResponse;

/**
 * 地图服务商能力接口（SPI）。
 * <p>
 * 每个实现内部自洽单向：<b>禁止跨服务商调用或兜底降级</b>。
 * 某服务商能力缺失时，只允许在自身体系内做行政层级回退，不得回落到另一个服务商。
 * <p>
 * 传输形状按结构复杂度分两类：
 * <ul>
 *   <li>定位类（{@link #locateByIp}、{@link #locateByCoordinate}、{@link #resolveCenter}）
 *       用服务商中立的 {@link MapLocationResult}，字段少且无损耗风险。</li>
 *   <li>富结构类（{@link #regeoDetail}、{@link #inputTips}）由各服务商自行处理原生响应，
 *       直接产出对外契约类型。对外契约本就是高德结构，强行经中立结构往返会丢字段
 *       （实测高德 Poi 17 字段会丢 10 个、city 由 JSONArray 退化为 String），
 *       故高德实现为恒等透传、天地图实现单向适配。</li>
 * </ul>
 *
 * @author Tigshop团队
 * @create 2026/7/31
 */
public interface MapProvider {

    /**
     * 本实现对应的服务商，供 {@link MapProviderRouter} 建立路由索引。
     */
    MapProviderEnum provider();

    /**
     * IP 定位到行政区（城市级精度）。
     *
     * @param ip 客户端 IP
     * @return 定位结果；未定位到有效行政区时 regionId 为 null
     */
    MapLocationResult locateByIp(String ip);

    /**
     * 坐标逆定位到行政区。
     * <p>
     * 入参保持既有 {@code getLocationByLocation} 的整串形式而非拆成经纬度两参，
     * 避免「拆分再拼接」对非常规输入改变透传内容。
     *
     * @param location 坐标串，格式 {@code "经度,纬度"}
     */
    MapLocationResult locateByCoordinate(String location);

    /**
     * 行政区划码换中心点坐标。
     *
     * @param regionId 6 位行政区划码
     * @return 中心点，格式 {@code "经度,纬度"}；取不到时返回 null
     */
    String resolveCenter(Integer regionId);

    /**
     * 逆地理详情查询。
     * <p>
     * 高德实现直接返回 feign 解析出的原生实例；天地图实现由自身适配器组装成同一结构，
     * 其中 citycode、township、towncode、aois 无数据源恒为空，pois、roads 为单元素。
     * 失败时返回 status 为 "0" 的对象而非抛错，与既有行为一致。
     *
     * @param longitude  经度
     * @param latitude   纬度
     * @param radius     查询半径（米），可为 null
     * @param extensions 返回结果控制 base/all，可为 null
     */
    RegeoDetailResponse regeoDetail(String longitude, String latitude, Integer radius, String extensions);

    /**
     * 关键字地址提示。
     * <p>
     * 失败时返回 status 为 "0" 的对象而非抛错，与既有行为一致。
     *
     * @param keywords 搜索关键字
     * @param location 参考坐标，格式 {@code "经度,纬度"}，可为 null
     */
    InputTipsResponse inputTips(String keywords, String location);
}
