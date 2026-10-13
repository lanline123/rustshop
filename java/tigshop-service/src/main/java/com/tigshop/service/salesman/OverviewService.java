package com.tigshop.service.salesman;

import com.alibaba.fastjson.JSONObject;
import com.tigshop.bean.vo.salesman.OverviewCoreSummaryVO;
import com.tigshop.bean.vo.salesman.OverviewCoreTrendVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Tigshop
 */
public interface OverviewService {

    /**
     * 获取数据
     */
    OverviewCoreTrendVO coreTrend(Integer dateType, String startEndTime) ;

    /**
     * 获取数据
     */
    OverviewCoreSummaryVO coreSummary(Integer summaryType);

    /**
     * 获取横轴数据
     * @param dateType 时间类型
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return List
     */
    List<String> getHorizontalAxis(int dateType, String startDate, String endDate);

    /**
     * 获取纵轴数据
     * @param horizontalAxis 横轴数据
     * @param data 数据
     * @param dateType 时间类型
     * @param fromType 来源类型
     * @return List
     */
    List<BigDecimal> getLongitudinalAxis(List<String> horizontalAxis, List<JSONObject> data, int dateType, int fromType);
}
