package com.tigshop.common.constant.setting;

import cn.hutool.core.util.StrUtil;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 快递鸟编码 → 快递100 编码的常见映射兜底。
 * <p>物流公司可在 logistics_company.kuaidi100_code 单独配置；未配置时按 logistics_code 查本表。</p>
 */
public final class Kuaidi100CodeMapping {

    private static final Map<String, String> KDNIAO_TO_KUAIDI100;

    private static final Map<String, Set<String>> KUAIDI100_TO_KDNIAO;

    /** 常见承运商：快递100 编码 → 中文名，供「同步快递100物流公司」种子使用。 */
    private static final Map<String, String> COMMON_CARRIERS;

    static {
        Map<String, String> carriers = new LinkedHashMap<>();
        carriers.put("shunfeng", "顺丰速运");
        carriers.put("yuantong", "圆通速递");
        carriers.put("zhongtong", "中通快递");
        carriers.put("shentong", "申通快递");
        carriers.put("yunda", "韵达速递");
        carriers.put("ems", "EMS");
        carriers.put("youzhengguonei", "邮政快递包裹");
        carriers.put("huitongkuaidi", "百世快递");
        carriers.put("jd", "京东物流");
        carriers.put("debangwuliu", "德邦物流");
        carriers.put("jtexpress", "极兔速递");
        carriers.put("youshuwuliu", "优速快递");
        carriers.put("zhaijisong", "宅急送");
        carriers.put("annengwuliu", "安能物流");
        carriers.put("tiantian", "天天快递");
        carriers.put("kuayue", "跨越速运");
        carriers.put("cainiao", "菜鸟裹裹");
        COMMON_CARRIERS = Collections.unmodifiableMap(carriers);
    }

    static {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("SF", "shunfeng");
        map.put("SF_EXPRESS", "shunfeng");
        map.put("YTO", "yuantong");
        map.put("ZTO", "zhongtong");
        map.put("STO", "shentong");
        map.put("STO_EXPRESS", "shentong");
        map.put("YD", "yunda");
        map.put("YUNDA", "yunda");
        map.put("EMS", "ems");
        map.put("YZPY", "youzhengguonei");
        map.put("HTKY", "huitongkuaidi");
        map.put("JD", "jd");
        map.put("DBL", "debangwuliu");
        map.put("JTSD", "jtexpress");
        map.put("UC", "youshuwuliu");
        map.put("ZJS", "zhaijisong");
        map.put("ANE", "annengwuliu");
        map.put("HHTT", "tiantian");
        map.put("KYSY", "kuayue");
        map.put("CNSD", "cainiao");
        KDNIAO_TO_KUAIDI100 = Collections.unmodifiableMap(map);

        Map<String, Set<String>> reverse = new LinkedHashMap<>();
        map.forEach((kdniaoCode, kuaidi100Code) -> reverse
                .computeIfAbsent(kuaidi100Code, ignored -> new LinkedHashSet<>())
                .add(kdniaoCode));
        reverse.replaceAll((code, kdniaoCodes) -> Collections.unmodifiableSet(kdniaoCodes));
        KUAIDI100_TO_KDNIAO = Collections.unmodifiableMap(reverse);
    }

    private Kuaidi100CodeMapping() {
    }

    /**
     * 按快递鸟/系统 logistics_code 兜底解析快递100 小写编码；找不到返回 null。
     */
    public static String resolve(String logisticsCode) {
        if (StrUtil.isBlank(logisticsCode)) {
            return null;
        }
        String code = logisticsCode.trim();
        String mapped = KDNIAO_TO_KUAIDI100.get(code.toUpperCase());
        if (StrUtil.isNotBlank(mapped)) {
            return mapped;
        }
        // 已是小写拼音编码时直接复用
        if (code.equals(code.toLowerCase()) && code.matches("[a-z0-9]+")) {
            return code;
        }
        return null;
    }

    /** 常见承运商（快递100 编码 → 中文名），供同步种子使用。 */
    public static Map<String, String> commonCarriers() {
        return COMMON_CARRIERS;
    }

    /**
     * 按快递100编码反向查找受控的快递鸟编码；一个快递100编码可能对应多个别名。
     */
    public static Set<String> reverseResolve(String kuaidi100Code) {
        if (StrUtil.isBlank(kuaidi100Code)) {
            return Collections.emptySet();
        }
        return KUAIDI100_TO_KDNIAO.getOrDefault(
                kuaidi100Code.trim().toLowerCase(Locale.ROOT), Collections.emptySet());
    }

    /** 支持映射到快递100的全部受控快递鸟编码。 */
    public static Set<String> mappedLogisticsCodes() {
        return KDNIAO_TO_KUAIDI100.keySet();
    }
}
