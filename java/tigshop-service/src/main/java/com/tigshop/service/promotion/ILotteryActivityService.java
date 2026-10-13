package com.tigshop.service.promotion;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tigshop.bean.model.promotion.LotteryActivity;
import com.tigshop.bean.param.promotion.LotteryActivityParam;
import com.tigshop.bean.query.promotion.LotteryActivityQuery;
import com.tigshop.bean.query.promotion.LotteryUserManageQuery;
import com.tigshop.bean.query.promotion.LotteryUserRecordQuery;
import com.tigshop.bean.vo.promotion.LotteryActivityListVO;
import com.tigshop.bean.vo.promotion.LotteryActivityVO;
import com.tigshop.bean.vo.promotion.LotteryUserManageVO;
import com.tigshop.bean.vo.promotion.LotteryUserRecordVO;

/**
 * 抽奖活动Service
 *
 * @author Tigshop团队
 * @create 2024年12月12日
 */
public interface ILotteryActivityService extends IService<LotteryActivity> {

    /**
     * 获取抽奖活动列表
     *
     * @param query 查询参数
     * @return 活动列表
     */
    Page<LotteryActivityListVO> getList(LotteryActivityQuery query);

    /**
     * 获取抽奖活动详情
     *
     * @param activityId 活动ID
     * @return 活动详情
     */
    LotteryActivityVO getDetail(Integer activityId);

    /**
     * 创建抽奖活动
     * 说明: 创建时会检查活动时间是否与其他活动冲突
     *
     * @param param 活动参数
     * @return 活动ID
     */
    Integer create(LotteryActivityParam param);

    /**
     * 更新抽奖活动
     * 说明: 更新时会检查活动时间是否与其他活动冲突
     *
     * @param param 活动参数
     */
    void update(LotteryActivityParam param);

    /**
     * 删除抽奖活动(软删除)
     *
     * @param activityId 活动ID
     */
    void delete(Integer activityId);

    /**
     * 结束活动
     * 说明: 立即结束活动,将结束时间设置为当前时间
     *
     * @param activityId 活动ID
     */
    void endActivity(Integer activityId);

    /**
     * 检查活动时间是否冲突
     * 说明: 检查指定时间段是否与其他活动时间交叉
     *
     * @param activityId 活动ID(编辑时需要排除自己)
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return true-有冲突, false-无冲突
     */
    boolean checkTimeConflict(Integer activityId, Long startTime, Long endTime);

    /**
     * 获取抽奖活动用户管理列表
     * 说明: 返回参与抽奖活动的用户列表，包含参与次数、最后参与时间等信息
     *
     * @param query 查询参数
     * @return 用户管理列表
     */
    Page<LotteryUserManageVO> getUserManageList(LotteryUserManageQuery query);

    /**
     * 获取用户中奖详情列表
     * 说明: 返回某个用户在某个活动中的抽奖记录
     *
     * @param query 查询参数
     * @return 中奖详情列表
     */
    Page<LotteryUserRecordVO> getUserRecordList(LotteryUserRecordQuery query);
}

