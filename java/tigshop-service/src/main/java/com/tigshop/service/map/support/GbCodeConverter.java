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

import cn.hutool.core.util.ObjectUtil;
import com.tigshop.bean.model.setting.Region;
import com.tigshop.service.setting.RegionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 天地图 9 位国标码与 {@code region.region_id} 的双向换算，以及行政层级回退链。
 * <p>
 * {@code region_id} 存的是 GB/T 2260 国标行政区划码（与高德 adcode 同一体系），
 * 天地图 gb 码为 {@code "156"} + 该码。
 * <p>
 * 回退链存在的原因：天地图行政区划收录范围小于 region 表（全量 3876 条中缺 703 条，
 * 主要是直辖市市级重复层、省直辖县等虚拟层级、开发区/新区）。
 * 实证「156 拼接 + 最多两级 parent_id 回退」可 100% 覆盖
 * （直接命中 3173 / 一级回退 288 / 二级回退 415 / 未覆盖 0），故不需要映射表。
 * <p>
 * 该回退属**同一服务商内的行政层级换算**（语义：该细分区划天地图无收录，取上级中心点），
 * 不是跨服务商兜底，与 D2「各 Provider 内部自洽单向」不冲突。
 *
 * @author Tigshop团队
 * @create 2026/8/3
 */
@Component
@RequiredArgsConstructor
public class GbCodeConverter {

    /** 中国国家码前缀 */
    private static final String CHINA_PREFIX = "156";

    /** 回退上限：自身 + 两级祖先 */
    private static final int MAX_CHAIN_SIZE = 3;

    private final RegionService regionService;

    /**
     * 行政区划码 → 天地图 gb 码。
     *
     * @return {@code "156" + regionId}；入参为 null 时返回 null
     */
    public String toGb(Integer regionId) {
        return regionId == null ? null : CHINA_PREFIX + regionId;
    }

    /**
     * 天地图 gb 码 → 国标码，{@link #toGb} 的逆运算。
     * <p>
     * 位数按输入而定：省市区县级 9 位去前缀后为 6 位，乡镇级 12 位去前缀后为 9 位
     * （与高德 towncode 位数一致）。无 {@code 156} 前缀时原样返回。
     */
    public String stripGb(String gbCode) {
        if (ObjectUtil.isEmpty(gbCode)) {
            return null;
        }
        return gbCode.startsWith(CHINA_PREFIX) ? gbCode.substring(CHINA_PREFIX.length()) : gbCode;
    }

    /**
     * 天地图 gb 码 → {@code region_id}。
     *
     * @return 非数字或空值时返回 null，交由调用方按「取不到行政区」处理
     */
    public Integer stripGbToRegionId(String gbCode) {
        String stripped = stripGb(gbCode);
        if (ObjectUtil.isEmpty(stripped)) {
            return null;
        }
        try {
            return Integer.valueOf(stripped);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 行政层级回退链，用于天地图查不到该区划时逐级向上重试。
     *
     * @return {@code [自身, 父级, 祖父级]}，最多三个元素；不足则短于三个。
     *         入参为 null 时返回空列表。链中不含重复项，故自指脏数据不会造成死循环。
     */
    public List<Integer> fallbackChain(Integer regionId) {
        if (regionId == null) {
            return Collections.emptyList();
        }
        List<Integer> chain = new ArrayList<>(MAX_CHAIN_SIZE);
        chain.add(regionId);

        Integer current = regionId;
        while (chain.size() < MAX_CHAIN_SIZE) {
            Integer parentId = findParentId(current);
            // parent_id 为 0 是 region 表顶层的约定，不是有效行政区
            if (parentId == null || parentId <= 0 || chain.contains(parentId)) {
                break;
            }
            chain.add(parentId);
            current = parentId;
        }
        return chain;
    }

    /**
     * 查某行政区的父级 id。{@code region_id} 是主键，故按主键取行。
     */
    private Integer findParentId(Integer regionId) {
        Region region = regionService.getById(regionId);
        return region == null ? null : region.getParentId();
    }
}
