package com.tigshop.bean.feign.amap;

import lombok.Data;

@Data
public class GeoResponse {
    private String status;
    private String info;
    private String infocode;
    private String province;
    private String city;
    // 第一种结果有
    private String adcode;
    // 第二种结果才有
    private Regeocode regeocode;

    @Data
    public static class Regeocode {
        private AddressComponent addressComponent;
        private String formatted_address;
    }

    @Data
    public static class AddressComponent {
        private Object city;
        private String province;
        private String adcode;
        private String district;
        private String towncode;
        private String country;
        private String township;
        private String citycode;
    }

    /**
     * 通用方法：获取 adcode
     */
    public String getAdcodeSafe() {
        if (adcode != null) {
            // 第一种结果
            return adcode;
        }
        if (regeocode != null && regeocode.getAddressComponent() != null) {
            // 第二种结果
            return regeocode.getAddressComponent().getAdcode();
        }
        return null;
    }
}
