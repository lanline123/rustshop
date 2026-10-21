package com.tigshop.common.utils;

import java.math.BigDecimal;

public class GpsDistanceUtil {

    private static final double EARTH_RADIUS = 6378137; // 地球半径（单位：米）

    /**
     * 计算两点之间的距离 (单位：米)
     */
    public static BigDecimal getDistance(BigDecimal lng1, BigDecimal lat1,
                                         BigDecimal lng2, BigDecimal lat2) {
        //如果四个参数有一个null则返回null
        if (lng1 == null || lat1 == null || lng2 == null || lat2 == null) {
            return new BigDecimal("0");
        }
        double radLat1 = Math.toRadians(lat1.doubleValue());
        double radLat2 = Math.toRadians(lat2.doubleValue());
        double radLng1 = Math.toRadians(lng1.doubleValue());
        double radLng2 = Math.toRadians(lng2.doubleValue());

        double a = radLat1 - radLat2;
        double b = radLng1 - radLng2;

        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.pow(Math.sin(b / 2), 2)));

        double distance = EARTH_RADIUS * s;
        return BigDecimal.valueOf(distance);
    }

    public static void main(String[] args) {
        BigDecimal lat1 = new BigDecimal("28.712094");
        BigDecimal lng1 = new BigDecimal("115.857963");
        BigDecimal lat2 = new BigDecimal("28.712095");
        BigDecimal lng2 = new BigDecimal("115.858000");

        BigDecimal distance = getDistance(lng1, lat1, lng2, lat2);
        System.out.println("两点之间的距离：" + distance + " 米");
    }
}
