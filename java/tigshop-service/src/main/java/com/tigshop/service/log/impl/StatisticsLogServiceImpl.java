package com.tigshop.service.log.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tigshop.bean.model.log.StatisticsBase;
import com.tigshop.bean.model.log.StatisticsLog;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.mapper.log.StatisticsBaseMapper;
import com.tigshop.mapper.log.StatisticsLogMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.log.StatisticsLogService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 统计明细日志服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class StatisticsLogServiceImpl extends BaseServiceImpl<StatisticsLogMapper, StatisticsLog> implements StatisticsLogService {

    @Resource
    StatisticsBaseMapper statisticsBaseMapper;

    @Resource
    StatisticsLogMapper statisticsLogMapper;

    @Override
    public Integer getVisitNumByProduct(String[] data, Integer accessFlag, Integer productFlag, Integer shopId) {
        LambdaQueryWrapper<StatisticsLog> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.between(StatisticsLog::getAccessTime, TigUtils.toTimestampYmdHms(data[0]), TigUtils.toTimestampYmdHms(data[1]))
                .eq(shopId > 0, StatisticsLog::getShopId, Math.max(shopId, 0));

        if (productFlag > 0) {
            queryWrapper.gt(StatisticsLog::getProductId, 0);
        }

        if (accessFlag == 0) {
            // 访客量，按用户分组
            queryWrapper.groupBy(StatisticsLog::getUser);
        }
        if (accessFlag == 0) {
            // 访客量，按用户分组
            queryWrapper.select(StatisticsLog::getUser);
            return Math.toIntExact(this.list(queryWrapper).size());
        }
        return Math.toIntExact(this.count(queryWrapper));
    }

    @Override
    public List<Map<String, Object>> getVisitList(String[] data, Integer accessFlag, Integer productFlag, Integer shopId) {
        QueryWrapper<StatisticsBase> queryWrapper = new QueryWrapper<>();
        queryWrapper.between("date", data[0], data[1])
                .eq(shopId > 0, "shop_id", Math.max(shopId, 0));

        List<Map<String, Object>> resultList;
        if (productFlag > 0) {
            QueryWrapper<StatisticsLog> queryWrapperLog = new QueryWrapper<>();
            queryWrapperLog.gt("product_id", 0);
            queryWrapperLog.groupBy("DATE_FORMAT(FROM_UNIXTIME(access_time), '%Y-%m-%d')");
            queryWrapperLog.select("DATE_FORMAT(FROM_UNIXTIME(access_time), '%Y-%m-%d') AS period", "COUNT(*) AS accessCount");
            resultList = statisticsLogMapper.selectMaps(queryWrapperLog);
        } else {
            if (accessFlag == 1) {
                // 浏览量
                queryWrapper.select("click_count as accessCount", "date as period");
            } else {
                // 访客量
                queryWrapper.select("visitor_count as accessCount", "date as period");
            }
            resultList = statisticsBaseMapper.selectMaps(queryWrapper);
        }

        return resultList;
    }
}
