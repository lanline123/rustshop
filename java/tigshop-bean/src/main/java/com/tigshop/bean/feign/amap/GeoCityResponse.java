package com.tigshop.bean.feign.amap;

import lombok.Data;

import java.util.List;

@Data
public class GeoCityResponse {

    //{
    //    "status": "1",
    //    "info": "OK",
    //    "infocode": "10000",
    //    "count": "2",
    //    "suggestion": {
    //        "keywords": [],
    //        "cities": []
    //    },
    //    "districts": [
    //        {
    //            "citycode": "010",
    //            "adcode": "110000",
    //            "name": "北京市",
    //            "center": "116.407387,39.904179",
    //            "level": "province",
    //            "districts": []
    //        },
    //        {
    //            "citycode": "010",
    //            "adcode": "110100",
    //            "name": "北京城区",
    //            "center": "116.405285,39.904989",
    //            "level": "city",
    //            "districts": []
    //        }
    //    ]
    //}

    private String status;
    private String info;
    private String infocode;
    private String count;
    private Suggestion suggestion;
    private List<District> districts;

    @Data
    public static class Suggestion {
        private List<String> keywords;
        private List<String> cities;
    }

    @Data
    public static class District {
        private String citycode;
        private String adcode;
        private String name;
        private String center;
        private String level;
        private List<District> districts;
    }

}
