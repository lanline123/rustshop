package com.tigshop.service.promotion;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tigshop.bean.model.promotion.LotteryPrize;
import com.tigshop.bean.param.promotion.LotteryPrizeItemParam;
import com.tigshop.bean.vo.promotion.LotteryPrizeVO;

import java.util.List;
import java.util.Map;

/**
 * 抽奖奖品Service
 * 
 * 注意：奖品跟随活动一起保存，不提供单独的增删改接口
 *
 * @author Tigshop团队
 * @create 2024年12月12日
 */
public interface ILotteryPrizeService extends IService<LotteryPrize> {

    /**
     * 根据活动ID查询奖品列表(无分页)
     *
     * @param activityId 活动ID
     * @return 奖品列表
     */
    List<LotteryPrizeVO> getListByActivity(Integer activityId);

    /**
     * 根据ID获取奖品
     *
     * @param prizeId 奖品ID
     * @return 奖品信息
     */
    LotteryPrize getPrizeById(Integer prizeId);

    /**
     * 扣减奖品库存(乐观锁)
     *
     * @param prizeId 奖品ID
     * @return 是否成功
     */
    boolean deductStock(Integer prizeId);

    /**
     * 发放奖品
     *
     * @param userId 用户ID
     * @param prize 奖品信息
     */
    void grantPrize(Integer userId, LotteryPrize prize);

    /**
     * 根据活动ID删除所有奖品（物理删除）
     * 用于编辑活动时先删除原有奖品
     *
     * @param activityId 活动ID
     */
    void deleteByActivityId(Integer activityId);

    /**
     * 批量保存奖品列表
     * 用于活动保存时一起保存奖品
     *
     * @param activityId 活动ID
     * @param prizeList 奖品参数列表
     * @return tempIndex → prizeId 映射（用于九宫格配置转换）
     */
    Map<Integer, Integer> savePrizeList(Integer activityId, List<LotteryPrizeItemParam> prizeList);
}


