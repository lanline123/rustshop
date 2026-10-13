package com.tigshop.service.promotion.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tigshop.bean.model.promotion.LotteryUserStat;
import com.tigshop.mapper.promotion.LotteryUserStatMapper;
import com.tigshop.service.promotion.ILotteryUserStatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * 用户抽奖统计ServiceImpl
 *
 * @author Tigshop团队
 * @create 2024年12月12日
 */
@Service
@Slf4j
public class LotteryUserStatServiceImpl extends ServiceImpl<LotteryUserStatMapper, LotteryUserStat>
        implements ILotteryUserStatService {

    @Override
    public LotteryUserStat getStat(Integer activityId, Integer userId) {
        LotteryUserStat stat = this.lambdaQuery()
                .eq(LotteryUserStat::getActivityId, activityId)
                .eq(LotteryUserStat::getUserId, userId)
                .one();

        if (stat == null) {
            // 首次参与,创建统计记录
            stat = new LotteryUserStat();
            stat.setActivityId(activityId);
            stat.setUserId(userId);
            stat.setTotalCount(0);
            stat.setWinCount(0);
            stat.setTodayCount(0);
            stat.setTodayDate(LocalDate.now().toString());
            long currentTime = System.currentTimeMillis() / 1000;
            stat.setCreateTime(currentTime);
            stat.setUpdateTime(currentTime);
            this.save(stat);
        } else {
            // 检查是否需要重置今日次数
            String today = LocalDate.now().toString();
            if (!today.equals(stat.getTodayDate())) {
                stat.setTodayCount(0);
                stat.setTodayDate(today);
                stat.setUpdateTime(System.currentTimeMillis() / 1000);
                this.updateById(stat);
            }
        }

        return stat;
    }

    @Override
    public void updateStat(Integer activityId, Integer userId, boolean isWin) {
        // 确保统计记录存在
        getStat(activityId, userId);

        long currentTime = System.currentTimeMillis() / 1000;

        // 使用lambda方式更新
        var updateWrapper = this.lambdaUpdate()
                .eq(LotteryUserStat::getActivityId, activityId)
                .eq(LotteryUserStat::getUserId, userId)
                .setIncrBy(LotteryUserStat::getTotalCount, 1)
                .setIncrBy(LotteryUserStat::getTodayCount, 1)
                .set(LotteryUserStat::getLastLotteryTime, currentTime)
                .set(LotteryUserStat::getUpdateTime, currentTime);

        if (isWin) {
            updateWrapper.setIncrBy(LotteryUserStat::getWinCount, 1);
        }

        updateWrapper.update();
    }
}


