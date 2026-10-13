package com.tigshop.service.promotion.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.promotion.Coupon;
import com.tigshop.bean.model.promotion.CouponIssueLog;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.model.user.UserCoupon;
import com.tigshop.bean.query.promotion.coupon.CouponIssueLogPageQuery;
import com.tigshop.bean.vo.promotion.CouponIssueLogVO;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.promotion.CouponIssueLogMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.promotion.CouponIssueLogService;
import com.tigshop.service.setting.ConfigService;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 优惠券发放日志服务实现
 */
@RequiredArgsConstructor
@Service
public class CouponIssueLogServiceImpl extends BaseServiceImpl<CouponIssueLogMapper, CouponIssueLog> implements CouponIssueLogService {

    private final ConfigService configService;

    private static final Map<Integer, String> SOURCE_TYPE_MAP = Map.of(
            0, "用户领取",
            1, "抽奖中奖",
            2, "手动发放"
    );

    @Override
    public void log(Long batchId, Integer couponId, Integer userCouponId, Integer userId, Integer sourceType) {
        CouponIssueLog log = CouponIssueLog.builder()
                .batchId(batchId)
                .couponId(couponId)
                .userCouponId(userCouponId)
                .userId(userId)
                .issueTime(StringUtils.getCurrentTime())
                .sourceType(sourceType)
                .build();
        this.save(log);
    }

    @Override
    public Page<CouponIssueLogVO> list(CouponIssueLogPageQuery query) {
        Page<CouponIssueLogVO> page = new Page<>(query.getPage(), query.getSize());

        MPJLambdaWrapper<CouponIssueLog> wrapper = new MPJLambdaWrapper<CouponIssueLog>()
                .selectAll(CouponIssueLog.class)
                .selectAs(Coupon::getCouponName, CouponIssueLogVO::getCouponName)
                .selectAs(User::getUsername, CouponIssueLogVO::getUsername)
                .selectAs(User::getNickname, CouponIssueLogVO::getNickname)
                .selectAs(User::getMobile, CouponIssueLogVO::getMobile)
                .selectAs(User::getAvatar, CouponIssueLogVO::getAvatar)
                .selectAs(UserCoupon::getUsedTime, "usedTime")
                .leftJoin(Coupon.class, Coupon::getCouponId, CouponIssueLog::getCouponId)
                .leftJoin(User.class, User::getUserId, CouponIssueLog::getUserId)
                .leftJoin(UserCoupon.class, UserCoupon::getId, CouponIssueLog::getUserCouponId)
                .eq(query.getBatchId() != null, CouponIssueLog::getBatchId, query.getBatchId())
                .eq(query.getCouponId() != null, CouponIssueLog::getCouponId, query.getCouponId())
                .eq(query.getUserId() != null, CouponIssueLog::getUserId, query.getUserId())
                .eq(query.getSourceType() != null, CouponIssueLog::getSourceType, query.getSourceType())
                .orderByDesc(CouponIssueLog::getIssueTime);

        Page<CouponIssueLogVO> result = this.selectJoinListPage(page, CouponIssueLogVO.class, wrapper);

        // 填充来源名称、使用状态、默认头像
        String defaultAvatar = configService.getConfigByCode(SettingsEnum.DEFAULT_AVATAR.getBizCode()).getBizVal();
        result.getRecords().forEach(vo -> {
            vo.setSourceTypeName(SOURCE_TYPE_MAP.getOrDefault(vo.getSourceType(), "未知"));
            vo.setUsed(vo.getUsedTime() != null && vo.getUsedTime() > 0 ? 1 : 0);
            if (StrUtil.isEmpty(vo.getAvatar())) {
                vo.setAvatar(defaultAvatar);
            }
        });

        return result;
    }
}
