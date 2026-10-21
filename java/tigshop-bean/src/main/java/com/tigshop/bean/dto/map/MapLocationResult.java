// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.map;

import com.tigshop.bean.model.setting.Region;
import lombok.Data;

import java.io.Serializable;

/**
 * 服务商中立的定位结果：IP 定位与坐标逆定位共用。
 * <p>
 * 服务商各自负责把原始响应归一到 region 表的行政区（行政层级换算在服务商实现内完成），
 * 上层编排只消费本结果，不感知服务商差异。
 * <p>
 * 携带 {@link Region} 实体而非仅行政区划码，是为了让编排层直接复用服务商已查出的那一行，
 * 避免按码重查（既省一次查询，也消除「重查可能取到不同行」的口子）。{@code Region} 是项目自有
 * model、与 region 表一一对应，不是任何服务商的专属类型，故不破坏本结果的服务商中立性。
 *
 * @author Tigshop团队
 * @create 2026/7/31
 */
@Data
public class MapLocationResult implements Serializable {

    /**
     * 已归一到的 region 表行政区实体。
     * <p>
     * 定位失败由各服务商实现按自身既有语义处理（高德沿用「降级到北京」与既有异常行为）。
     */
    private Region region;

    /**
     * 中心点，格式 {@code "经度,纬度"}，与 RegionVO.centor 契约一致。
     * 取不到中心点时为 {@code null}，与既有实现一致（不阻断整体返回）。
     */
    private String centor;

    /**
     * 行政区划码（6 位 GB/T 2260 国标码，与高德 adcode 同源）；未归一出行政区时为 {@code null}。
     */
    public Integer getRegionId() {
        return region == null ? null : region.getRegionId();
    }

    public static MapLocationResult of(Region region, String centor) {
        MapLocationResult result = new MapLocationResult();
        result.setRegion(region);
        result.setCentor(centor);
        return result;
    }
}
