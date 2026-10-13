package com.tigshop.service.log;

import com.tigshop.bean.model.log.StatisticsLog;
import com.tigshop.service.common.BaseService;

import java.util.List;
import java.util.Map;

/**
 * 统计明细日志表服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface StatisticsLogService extends BaseService<StatisticsLog> {

    /**
     * 商品访客量 / 浏览量
     *
     * @param data        时间范围，格式为["startDate", "endDate"]
     * @param accessFlag  是否返回浏览量（1：是，0：否）
     * @param productFlag 是否按商品统计（暂未使用）
     * @param shopId      店铺ID，默认为0
     * @return 访问量或访客数
     */
    Integer getVisitNumByProduct(String[] data, Integer accessFlag, Integer productFlag, Integer shopId);

    /**
     * 获取商品访问量 / 浏览量
     *
     * @param data        时间范围，格式为["startDate", "endDate"]
     * @param accessFlag  是否返回浏览量（1：是，0：否）
     * @param productFlag 是否按商品统计（暂未使用）
     * @param shopId      店铺ID，默认为0
     * @return 访问量或访客数列表
     */
    List<Map<String, Object>> getVisitList(String[] data, Integer accessFlag, Integer productFlag, Integer shopId);

}
