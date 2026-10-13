// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.common.utils;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * @author Tigshop团队
 * @create 2025/8/11 13:05
 * 门店工具类
 */
public class StoreUtils {

    public static boolean isStoreOpen(String json) {
        JSONObject obj = JSON.parseObject(json);
        int type = obj.getIntValue("type");
        LocalTime now = LocalTime.now();
        // 1 = 周一
        int dayOfWeek = LocalDate.now().getDayOfWeek().getValue();

        if (type == 1) {
            return true;
        } else if (type == 2) {
            return hasAnyCheck(obj.getJSONArray("times"), now);
        } else if (type == 3) {
            JSONArray weeks = obj.getJSONArray("times");
            for (int i = 0; i < weeks.size(); i++) {
                JSONObject t = weeks.getJSONObject(i);
                if (t.getString("dayOfWeek").contains(dayOfWeek + "")) {
                    return hasAnyCheck(t.getJSONArray("ranges"), now);
                }
            }
        }
        return false;
    }

    /**
     * 判断营业时间
     *
     * @param now
     * @return
     */
    private static boolean hasAnyCheck(JSONArray times, LocalTime now) {
        for (int j = 0; j < times.size(); j++) {
            JSONObject time = times.getJSONObject(j);
            if (inRange(now, time.getString("start"), time.getString("end"))) {
                // 只要有一个时间段符合，就营业
                return true;
            }
        }
        return false;
    }

    /**
     * 判断时间段 是否在时间段内
     *
     * @param now
     * @param start
     * @param end
     * @return
     */
    private static boolean inRange(LocalTime now, String start, String end) {
        LocalTime s = LocalTime.parse(start);
        LocalTime e = LocalTime.parse(end);
        return !now.isBefore(s) && !now.isAfter(e);
    }

}
