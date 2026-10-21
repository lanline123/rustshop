package com.tigshop.bean.feign.amap;

import lombok.Data;
import java.util.List;

/**
 * 高德地图逆地理编码详细信息响应
 * 
 * @author Tigshop团队
 */
@Data
public class RegeoDetailResponse {
    private String status;
    private String info;
    private String infocode;
    private Regeocode regeocode;

    @Data
    public static class Regeocode {
        private String formatted_address;
        private AddressComponent addressComponent;
        private List<Poi> pois;
        private List<Road> roads;
        private List<Aoi> aois;

        @Data
        public static class AddressComponent {
            private String country;
            private String province;
            private Object city;
            private String citycode;
            private String district;
            private String adcode;
            private String township;
            private String towncode;
            private String neighborhood;
            private String building;
        }

        @Data
        public static class Poi {
            private String id;
            private String name;
            private String type;
            private String typecode;
            private String address;
            private String location;
            private String distance;
            private String tel;
            private String postcode;
            private String website;
            private String email;
            private String pcode;
            private String pname;
            private String citycode;
            private String cityname;
            private String adcode;
            private String adname;
        }

        @Data
        public static class Road {
            private String id;
            private String name;
            private String location;
            private String direction;
            private String distance;
        }

        @Data
        public static class Aoi {
            private String id;
            private String name;
            private String adcode;
            private String location;
            private String area;
            private String distance;
            private String type;
        }
    }
}
