package com.tigshop.bean.feign.tianditu;

import lombok.Data;

/**
 * 天地图逆地理编码响应（{@code /geocoder}）。
 * <p>
 * 成功判定为 {@code status} 等于字符串 {@code "0"}，与高德的 {@code "1"} 语义相反，
 * 翻转在适配层完成。天地图三个接口的状态约定互不相同（本接口为字符串 {@code "0"}、
 * {@code v2/search} 为对象 {@code infocode=1000}、{@code v2/administrative} 为数字 {@code 200}），
 * 不可共用一套判定，见 CR-20260803-0925 §C1。
 * <p>
 * 行政区码字段（{@code province_code} / {@code city_code} / {@code county_code} / {@code town_code}）
 * 未在官方返回参数表中列全，但真实响应确实返回，故一并建模（CR-20260803-0925 §C6）。
 * 码为 9 位「156 + 6 位国标码」形式，乡镇级为 12 位。
 *
 * @author Tigshop团队
 */
@Data
public class TdtGeocoderResponse {

    /** "0" 表示成功 */
    private String status;

    private String msg;

    private Result result;

    @Data
    public static class Result {

        private String formatted_address;

        private Location location;

        private AddressComponent addressComponent;

        @Data
        public static class Location {
            private Double lon;
            private Double lat;
        }

        @Data
        public static class AddressComponent {
            private String nation;
            private String province;
            /** 9 位国标码，形如 156130000 */
            private String province_code;
            /** 官方表述为「此点所在国家或城市或区县」，实际多为市级名称 */
            private String city;
            private String city_code;
            private String county;
            private String county_code;
            /** 乡镇/街道名称，可映射高德 township */
            private String town;
            /** 12 位，去 156 前缀后与高德 towncode 位数一致 */
            private String town_code;
            private String address;
            private String address_position;
            private Integer address_distance;
            /** 距此点最近的 POI 名称，用于近似高德 neighborhood / building */
            private String poi;
            private String poi_position;
            private Integer poi_distance;
            private String road;
            private Integer road_distance;
        }
    }
}
