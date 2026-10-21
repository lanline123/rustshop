package com.tigshop.bean.feign.tianditu;

import lombok.Data;

import java.util.List;

/**
 * 天地图地名搜索响应（{@code /v2/search}）。
 * <p>
 * 成功判定为 {@code status.infocode} 等于 {@code 1000}（{@code 3001} 为没有找到数据、
 * {@code 3000} 为服务器出错），既不是字符串 {@code "0"} 也不是数字 {@code 200}，
 * 见 CR-20260803-0925 §C1。
 * <p>
 * {@code resultType} 决定哪个结果集有值：只有 {@code 1}（普通 POI）才填充 {@code pois}；
 * 输入宽泛词时天地图会返回 {@code 2}（统计）或 {@code 3}（行政区），此时无 POI 可用，
 * 见 CR-20260803-0925 §C5。
 * <p>
 * POI 的行政区名称与编码字段（{@code province} / {@code city} / {@code county} 及对应 Code）
 * 均为**非必返回**，请求需带 {@code show=2} 争取返回，缺失时上层只能给出 null，
 * 见 CR-20260803-0925 §C3。
 *
 * @author Tigshop团队
 */
@Data
public class TdtSearchResponse {

    /** 1 普通 POI、2 统计、3 行政区、4 建议词、5 线路 */
    private Integer resultType;

    private Integer count;

    private String keyword;

    private List<Poi> pois;

    private Status status;

    @Data
    public static class Status {
        /** 1000 表示服务正常 */
        private Integer infocode;
        private String cndesc;
    }

    @Data
    public static class Poi {

        private String name;

        private String address;

        /** 坐标串 "经度,纬度" */
        private String lonlat;

        /** 101 POI 数据、102 公交站点 */
        private Integer poiType;

        /** POI 热点 id */
        private String hotPointID;

        private String phone;

        private String eaddress;

        private String ename;

        private String source;

        /** 天地图独立分类体系（Type.csv），与高德 typecode 不可比对 */
        private String typeCode;

        private String typeName;

        private String province;

        private String provinceCode;

        private String city;

        private String cityCode;

        private String county;

        /** 9 位国标码，去 156 前缀后即高德 adcode */
        private String countyCode;
    }
}
