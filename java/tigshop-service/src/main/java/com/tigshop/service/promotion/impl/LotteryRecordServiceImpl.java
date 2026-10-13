package com.tigshop.service.promotion.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tigshop.bean.model.promotion.LotteryActivity;
import com.tigshop.bean.model.promotion.LotteryPrize;
import com.tigshop.bean.model.promotion.LotteryRecord;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.query.promotion.LotteryRecordQuery;
import com.tigshop.bean.vo.promotion.LotteryRecordVO;
import com.tigshop.bean.vo.promotion.LotteryWinnerVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.mapper.promotion.LotteryRecordMapper;
import com.tigshop.service.promotion.ILotteryActivityService;
import com.tigshop.service.promotion.ILotteryPrizeService;
import com.tigshop.service.promotion.ILotteryRecordService;
import com.tigshop.service.user.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 抽奖记录ServiceImpl
 *
 * @author Tigshop团队
 * @create 2024年12月12日
 */
@Service
@Slf4j
public class LotteryRecordServiceImpl extends ServiceImpl<LotteryRecordMapper, LotteryRecord>
        implements ILotteryRecordService {

    @Resource
    private ILotteryPrizeService lotteryPrizeService;

    @Resource
    private ILotteryActivityService lotteryActivityService;

    @Resource
    private UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer saveRecord(Integer activityId, Integer userId, Integer prizeId, 
                             Integer costPoints, boolean isWin) {
        LotteryRecord record = new LotteryRecord();
        record.setActivityId(activityId);
        record.setUserId(userId);
        record.setPrizeId(prizeId);
        record.setCostPoints(costPoints);
        record.setIsWin(isWin ? 1 : 0);
        record.setCreateTime(System.currentTimeMillis() / 1000);

        if (isWin && prizeId > 0) {
            // 中奖,查询奖品信息
            LotteryPrize prize = lotteryPrizeService.getById(prizeId);
            if (prize != null) {
                record.setPrizeName(prize.getPrizeName());
                record.setPrizeType(prize.getPrizeType());
                record.setPrizeValue(prize.getPrizeValue());
                record.setPrizeImage(prize.getPrizeImage());

                // 积分和优惠券自动发放,状态为已领取
                if (prize.getPrizeType() == 1 || prize.getPrizeType() == 3) {
                    // 已领取
                    record.setStatus(1);
                    record.setReceiveTime(System.currentTimeMillis() / 1000);
                } else if (prize.getPrizeType() == 2) {
                    // 商品需要用户确认下单后领取
                    // 未领取
                    record.setStatus(0);
                }
            }
        } else {
            // 未中奖
            record.setPrizeName("未中奖");
            record.setPrizeType(0);
            record.setPrizeValue("");
            record.setPrizeImage("");
            // 已领取(无需领取)
            record.setStatus(1);
        }

        this.save(record);
        return record.getRecordId();
    }

    @Override
    public Page<LotteryRecordVO> getUserRecords(Integer userId, LotteryRecordQuery query) {
        Page<LotteryRecord> pageParam = new Page<>(query.getPage(), query.getSize());

        LambdaQueryWrapper<LotteryRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LotteryRecord::getUserId, userId);
        // 只查询中奖记录
        wrapper.eq(LotteryRecord::getIsWin, 1);

        if (query.getStatus() != null) {
            wrapper.eq(LotteryRecord::getStatus, query.getStatus());
        }

        wrapper.orderByDesc(LotteryRecord::getCreateTime);

        Page<LotteryRecord> recordPage = this.page(pageParam, wrapper);

        // 转换VO
        Page<LotteryRecordVO> voPage = new Page<>();
        BeanUtil.copyProperties(recordPage, voPage, "records");

        List<LotteryRecordVO> voList = recordPage.getRecords().stream().map(record -> {
            LotteryRecordVO vo = new LotteryRecordVO();
            BeanUtil.copyProperties(record, vo);

            // 状态文本
            switch (record.getStatus()) {
                case 0:
                    vo.setStatusText("未领取");
                    break;
                case 1:
                    vo.setStatusText("已领取");
                    break;
                case 2:
                    vo.setStatusText("已过期");
                    break;
                default:
                    vo.setStatusText("未知");
            }

            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReceive(Integer recordId, Integer orderId, Integer userId) {
        LotteryRecord record = this.getById(recordId);

        // 校验
        if (record == null) {
            throw new GlobalException("中奖记录不存在");
        }
        if (!record.getUserId().equals(userId)) {
            throw new GlobalException("无权操作此记录");
        }
        if (record.getStatus() != 0) {
            throw new GlobalException("该奖品已领取");
        }
        if (record.getPrizeType() != 2) {
            throw new GlobalException("只有商品奖品需要确认领取");
        }

        // 更新状态
        record.setStatus(1);
        record.setReceiveTime(System.currentTimeMillis() / 1000);
        this.updateById(record);

        log.info("确认领取商品奖品成功,记录ID:{},订单ID:{},用户ID:{}", recordId, orderId, userId);
    }

    @Override
    public List<LotteryWinnerVO> getWinnerList(Integer activityId) {
        // 如果未指定活动ID，查询当前进行中的活动
        if (activityId == null) {
            long currentTime = System.currentTimeMillis() / 1000;
            LotteryActivity activity = lotteryActivityService.lambdaQuery()
                    .eq(LotteryActivity::getIsDelete, 0)
                    .le(LotteryActivity::getStartTime, currentTime)
                    .ge(LotteryActivity::getEndTime, currentTime)
                    .orderByDesc(LotteryActivity::getCreateTime)
                    .last("LIMIT 1")
                    .one();
            if (activity == null) {
                return new ArrayList<>();
            }
            activityId = activity.getActivityId();
        }

        // 查询中奖记录
        LambdaQueryWrapper<LotteryRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LotteryRecord::getActivityId, activityId)
                .eq(LotteryRecord::getIsWin, 1)
                .orderByDesc(LotteryRecord::getCreateTime);

        List<LotteryRecord> records = this.list(wrapper);
        if (records.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取用户ID列表
        List<Integer> userIds = records.stream()
                .map(LotteryRecord::getUserId)
                .distinct()
                .collect(Collectors.toList());

        // 批量查询用户信息
        Map<Integer, User> userMap = userService.lambdaQuery()
                .in(User::getUserId, userIds)
                .list()
                .stream()
                .collect(Collectors.toMap(User::getUserId, u -> u));

        // 转换VO
        return records.stream().map(record -> {
            LotteryWinnerVO vo = new LotteryWinnerVO();
            vo.setPrizeName(record.getPrizeName());
            vo.setPrizeImage(record.getPrizeImage());
            vo.setPrizeType(record.getPrizeType());
            vo.setWinTime(record.getCreateTime());

            // 获取用户信息并脱敏
            User user = userMap.get(record.getUserId());
            if (user != null) {
                vo.setAvatar(user.getAvatar());
                vo.setNickname(maskNickname(user.getName()));
            } else {
                vo.setNickname("用户***");
                vo.setAvatar("");
            }

            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 脱敏昵称/用户名
     * 规则：
     * - 手机号格式：138****8888
     * - 2个字符：张*
     * - 3个及以上字符：张*明、张**明
     */
    private String maskNickname(String name) {
        if (StrUtil.isBlank(name)) {
            return "用户***";
        }

        // 判断是否是手机号格式（11位数字）
        if (name.matches("^1\\d{10}$")) {
            return name.substring(0, 3) + "****" + name.substring(7);
        }

        int length = name.length();
        if (length == 1) {
            return name + "**";
        } else if (length == 2) {
            return name.charAt(0) + "*";
        } else {
            // 保留首尾，中间用*替换
            int maskLength = length - 2;
            // 最多3个*
            String mask = "*".repeat(Math.min(maskLength, 3));
            return name.charAt(0) + mask + name.charAt(length - 1);
        }
    }
}

