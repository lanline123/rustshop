// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.order.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.model.order.LogisticsApiLog;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.mapper.order.LogisticsApiLogMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.order.LogisticsApiLogService;
import org.springframework.stereotype.Service;

/**
 * @author Tigshop团队
 */
@Service
public class LogisticsApiLogServiceImpl extends BaseServiceImpl<LogisticsApiLogMapper, LogisticsApiLog>
        implements LogisticsApiLogService {

    @Override
    public LogisticsApiLog getOrderLogisticsApiLog(Integer id) {
        LambdaQueryWrapper<LogisticsApiLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LogisticsApiLog::getOrderId, id)
                .orderByDesc(LogisticsApiLog::getId)
                .last("LIMIT 1");

        LogisticsApiLog log = this.getOne(queryWrapper);
        if (log == null) {
            throw new GlobalException("未查询到该数据!");
        }
        return log;
    }
}

