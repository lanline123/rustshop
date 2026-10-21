// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.log.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.dto.log.StatisticsBaseCreateDTO;
import com.tigshop.bean.model.log.StatisticsBase;
import com.tigshop.bean.model.log.StatisticsLog;
import com.tigshop.common.utils.IpUtils;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.log.StatisticsBaseMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.log.StatisticsBaseService;
import com.tigshop.service.log.StatisticsLogService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import static com.tigshop.common.utils.SecurityUtils.getCurrentUserId;

/**
 * 统计基础表服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class StatisticsBaseServiceImpl extends BaseServiceImpl<StatisticsBaseMapper, StatisticsBase> implements StatisticsBaseService {
   @Resource
    StatisticsLogService statisticsLogService;
    @Override
    public boolean create(StatisticsBaseCreateDTO createDTO) {
        Integer userId = getCurrentUserId();
        Integer shopId = getShopId();
        //获取年月日
        String date = DateUtil.today();
        LambdaQueryWrapper<StatisticsBase> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StatisticsBase::getDate, date).last("LIMIT 1");
        StatisticsBase statisticsBaseData = this.getOne(queryWrapper);
        if (statisticsBaseData == null) {
            //创建数据
            StatisticsBase statisticsBase = new StatisticsBase();
            statisticsBase.setDate(date);
            statisticsBase.setClickCount(1);
            if (createDTO.getIsNew() != null) {
                statisticsBase.setVisitorCount(1);
            }
            this.save(statisticsBase);
        } else {
            statisticsBaseData.setClickCount(statisticsBaseData.getClickCount() + 1);
            if (createDTO.getIsNew() != null) {
                statisticsBaseData.setVisitorCount(statisticsBaseData.getVisitorCount() + 1);
            }
            this.updateById(statisticsBaseData);
        }
        if (shopId != null && shopId > 0) {
            LambdaQueryWrapper<StatisticsBase> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(StatisticsBase::getDate, date);
            queryWrapper1.eq(StatisticsBase::getShopId, shopId);
            StatisticsBase statisticsBaseData1 = this.getOne(queryWrapper1);
            if (statisticsBaseData1 == null) {
                StatisticsBase statisticsBase = new StatisticsBase();
                statisticsBase.setDate(date);
                statisticsBase.setShopId(shopId);
                statisticsBase.setClickCount(1);
                if (createDTO.getIsNew() != null) {
                    statisticsBase.setVisitorCount(1);
                }
                this.save(statisticsBase);
            } else {
                statisticsBaseData1.setClickCount(statisticsBaseData1.getClickCount() + 1);
                if (createDTO.getIsNew() != null) {
                    statisticsBaseData1.setVisitorCount(statisticsBaseData1.getVisitorCount() + 1);
                }
                this.updateById(statisticsBaseData1);
            }
        }

        if (createDTO.getProductId() != null || createDTO.getShopCategoryId() != null) {
            StatisticsLog statisticsLog = getStatisticsLog(createDTO, shopId, userId);
            statisticsLogService.save(statisticsLog);
        }
        return true;
    }

    private static StatisticsLog getStatisticsLog(StatisticsBaseCreateDTO createDTO, Integer shopId, Integer userId) {
        StatisticsLog statisticsLog = new StatisticsLog();
        statisticsLog.setAccessTime(StringUtils.getCurrentTime());
        statisticsLog.setShopId(shopId != null ? shopId : 0);
        statisticsLog.setProductId(createDTO.getProductId() != null ? createDTO.getProductId() : 0);
        statisticsLog.setShopCategoryId(createDTO.getShopCategoryId() != null ? createDTO.getShopCategoryId() : 0);
        statisticsLog.setUser(userId != null ? userId.toString() : IpUtils.getIpAddr() );
        return statisticsLog;
    }

    @Override
    public Integer getVisitNum(String[] data, Integer accessFlag, Integer productFlag, Integer shopId) {
        if (shopId == null) {
            shopId = 0;
        }
        // 模拟查询逻辑
        StatisticsBase result = this.query()
                // 大于等于开始日期
                .ge("date", data[0])
                // 小于等于结束日期
                .le("date", data[1])
                // 店铺ID过滤
                .eq(shopId > 0, "shop_id", Math.max(shopId, 0))
                .select("sum(click_count) as click_count, sum(visitor_count) as visitor_count")
                .one();

        if (result != null) {
            return accessFlag == 1 ? result.getClickCount() : result.getVisitorCount();
        }
        return 0;
    }

}
