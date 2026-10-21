// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.common.utils;

import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

/**
 * 获取ip2region.xdb文件
 *
 * @author Tigshop团队
 * @create 2024年11月11日 14:39
 */
public class Ip2RegionUtil {

    /**
     * xdb 全量内容，加载后只读，可跨线程共享
     */
    private static volatile byte[] xdbBuffer;

    private static byte[] getXdbBuffer() throws IOException {
        byte[] buffer = xdbBuffer;
        if (buffer == null) {
            synchronized (Ip2RegionUtil.class) {
                buffer = xdbBuffer;
                if (buffer == null) {
                    try (InputStream inputStream = new ClassPathResource("ip2region.xdb").getInputStream()) {
                        buffer = inputStream.readAllBytes();
                    }
                    xdbBuffer = buffer;
                }
            }
        }
        return buffer;
    }

    /**
     * 根据ip获取地区信息
     * @return String
     * @throws Exception 抛出异常
     */
    public static String getRegionByIp() throws Exception {
        // Searcher 自身非线程安全，但其底层 buffer 全程只读，故共享 buffer、每次查询新建 Searcher
        Searcher searcher = Searcher.newWithBuffer(getXdbBuffer());
        try {
            String region = searcher.search(IpUtils.getIpAddr());
            if (region == null) {
                return null;
            }
            String[] parts = region.split("\\|");
            return parts.length > 2 ? parts[2] : null;
        } finally {
            searcher.close();
        }
    }

    /**
     * 省份字段下标（xdb 记录格式：国家|区域|省份|城市|ISP）
     */
    private static final int PROVINCE_INDEX = 2;

    /**
     * 城市字段下标
     */
    private static final int CITY_INDEX = 3;

    /**
     * 按指定 ip 取城市名。
     * <p>
     * 与无参的 {@link #getRegionByIp()} 并行存在：后者取省份且 ip 来源固定为当前请求，
     * 已有调用方依赖其行为，故不改动。
     *
     * @param ip 待查询的 ip
     * @return 城市名；库中无该字段或为内网标记时返回 null
     * @throws Exception 抛出异常
     */
    public static String getCityByIp(String ip) throws Exception {
        return searchPart(ip, CITY_INDEX);
    }

    /**
     * 按指定 ip 取省份名，用于城市级未命中时的降级匹配。
     *
     * @param ip 待查询的 ip
     * @return 省份名；库中无该字段或为内网标记时返回 null
     * @throws Exception 抛出异常
     */
    public static String getProvinceByIp(String ip) throws Exception {
        return searchPart(ip, PROVINCE_INDEX);
    }

    /**
     * 查询并取出 xdb 记录的指定字段。
     *
     * @return 字段缺失、为空或为内网标记 "0" 时返回 null
     */
    private static String searchPart(String ip, int index) throws Exception {
        if (ip == null || ip.isEmpty()) {
            return null;
        }
        Searcher searcher = Searcher.newWithBuffer(getXdbBuffer());
        try {
            String region = searcher.search(ip);
            if (region == null) {
                return null;
            }
            String[] parts = region.split("\\|");
            if (parts.length <= index) {
                return null;
            }
            String value = parts[index];
            // 内网或未收录时 ip2region 以 "0" 占位
            return value.isEmpty() || "0".equals(value) ? null : value;
        } finally {
            searcher.close();
        }
    }
}
