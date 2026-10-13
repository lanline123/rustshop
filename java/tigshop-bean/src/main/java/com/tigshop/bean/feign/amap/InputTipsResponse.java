package com.tigshop.bean.feign.amap;

import lombok.Data;
import java.util.List;

/**
 * 高德地图输入提示响应
 * 
 * @author Tigshop团队
 */
@Data
public class InputTipsResponse {
    private String status;
    private String info;
    private String infocode;
    private String count;
    private List<Tip> tips;

    @Data
    public static class Tip {
        private String id;
        private String name;
        private String district;
        private String adcode;
        private String location;
        private String address;
        private String typecode;
        private String citycode;
    }
}
