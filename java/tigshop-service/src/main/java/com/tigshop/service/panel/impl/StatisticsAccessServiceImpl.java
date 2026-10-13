package com.tigshop.service.panel.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.tigshop.bean.dto.panel.AccessStatisticsDTO;
import com.tigshop.bean.dto.panel.AccessStatisticsListDTO;
import com.tigshop.bean.model.log.StatisticsBase;
import com.tigshop.bean.vo.panel.AxisVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.mapper.log.StatisticsBaseMapper;
import com.tigshop.service.panel.StatisticsAccessService;
import com.tigshop.service.salesman.OverviewService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 访问
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
@Service
public class StatisticsAccessServiceImpl implements StatisticsAccessService {

    @Resource
    private StatisticsBaseMapper statisticsBaseMapper;

    @Resource
    private OverviewService overviewService;

    @Override
    public AxisVO getAccessStatistics(AccessStatisticsDTO accessStatisticsDTO) {

        if (accessStatisticsDTO.getStartTime() == null || accessStatisticsDTO.getEndTime() == null) {
            throw new GlobalException("请选择日期");
        }
        MPJLambdaWrapper<StatisticsBase> wrapper = new MPJLambdaWrapper<StatisticsBase>()
                .between(StatisticsBase::getDate, accessStatisticsDTO.getStartTime(), accessStatisticsDTO.getEndTime())
                .eq(StatisticsBase::getShopId, accessStatisticsDTO.getShopId());
        if (accessStatisticsDTO.getIsHits() > 0) {
            wrapper.select("click_count as access_count,date as period");
        } else {
            wrapper.select("visitor_count as access_count,date as period");
        }
        List<AccessStatisticsListDTO> accessStatisticsList = statisticsBaseMapper.selectJoinList(
                AccessStatisticsListDTO.class, wrapper
        );
        List<String> horizontalAxis = overviewService.getHorizontalAxis(0, accessStatisticsDTO.getStartTime(), accessStatisticsDTO.getEndTime());
        List<JSONObject> list = accessStatisticsList.stream()
                .map(accessStatisticsListDTO -> JSON.parseObject(JSON.toJSONString(accessStatisticsListDTO)))
                .toList();
        List<BigDecimal> longitudinalAxis = overviewService.getLongitudinalAxis(horizontalAxis, list, 0, 2);
        AxisVO result = new AxisVO();
        result.setHorizontalAxis(horizontalAxis);
        result.setLongitudinalAxis(longitudinalAxis);
        return result;
    }
}
