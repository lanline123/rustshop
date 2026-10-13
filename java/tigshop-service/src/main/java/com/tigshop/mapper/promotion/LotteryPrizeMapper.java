package com.tigshop.mapper.promotion;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tigshop.bean.model.promotion.LotteryPrize;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 抽奖奖品Mapper
 *
 * @author Tigshop团队
 * @create 2024年12月12日
 */
@Mapper
public interface LotteryPrizeMapper extends BaseMapper<LotteryPrize> {

    /**
     * 扣减奖品库存(乐观锁)
     *
     * @param prizeId    奖品ID
     * @param updateTime 更新时间
     * @return 影响行数
     */
    @Update("UPDATE lottery_prize SET prize_stock_used = prize_stock_used + 1, " +
            "update_time = #{updateTime} " +
            "WHERE prize_id = #{prizeId} " +
            "AND prize_stock > prize_stock_used")
    int deductStock(@Param("prizeId") Integer prizeId,
                    @Param("updateTime") Long updateTime);
}


