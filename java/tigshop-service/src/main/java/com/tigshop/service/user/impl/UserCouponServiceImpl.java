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

package com.tigshop.service.user.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.tigshop.bean.bo.CouponReceiveCountBO;
import com.tigshop.bean.dto.user.UserCouponCreateDTO;
import com.tigshop.bean.dto.user.UserCouponListDTO;
import com.tigshop.bean.dto.user.UserCouponUpdateDTO;
import com.tigshop.bean.enums.user.FromTag;
import com.tigshop.bean.enums.user.UserTypeEnum;
import com.tigshop.bean.enums.user.UserCouponStatus;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.promotion.Coupon;
import com.tigshop.bean.model.promotion.Promotion;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.model.user.UserCoupon;
import com.tigshop.bean.param.promotion.CouponManualIssueParam;
import com.tigshop.bean.param.user.UserCouponClaimParam;
import com.tigshop.bean.query.promotion.coupon.CouponIssueUserPageQuery;
import com.tigshop.bean.vo.order.OrderCheckVO;
import com.tigshop.bean.vo.promotion.CouponDetailVO;
import com.tigshop.bean.vo.promotion.CouponIssueUserVO;
import com.tigshop.bean.vo.promotion.CouponManualIssueFailDetailVO;
import com.tigshop.bean.vo.promotion.CouponManualIssuePreviewVO;
import com.tigshop.bean.vo.promotion.CouponManualIssueResultVO;
import com.tigshop.bean.vo.user.UserCouponVO;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.*;
import com.tigshop.common.i18n.MessageHelper;
import com.tigshop.mapper.order.OrderMapper;
import com.tigshop.mapper.promotion.CouponMapper;
import com.tigshop.mapper.promotion.PromotionMapper;
import com.tigshop.mapper.user.UserCouponMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.mapper.user.UserRankConfigMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.promotion.CouponIssueBatchService;
import com.tigshop.service.promotion.CouponIssueLogService;
import com.tigshop.service.user.UserCouponService;
import com.tigshop.service.user.UserService;
import com.tigshop.service.user.MemberAudienceService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.util.*;
import java.util.stream.Collectors;

import static com.tigshop.common.utils.SecurityUtils.getCurrentUserId;

/**
 * 用户优惠券服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@RequiredArgsConstructor
@Service
public class UserCouponServiceImpl extends BaseServiceImpl<UserCouponMapper, UserCoupon> implements UserCouponService {


    private final CouponMapper couponMapper;

    private final OrderMapper orderMapper;

    private final UserMapper userMapper;

    private final MessageHelper messageHelper;

    private final UserCouponMapper userCouponMapper;
    private final TigshopProperties tigshopProperties;
    private final PromotionMapper promotionMapper;
    private final ObjectProvider<UserService> userService;
    private final CouponIssueLogService couponIssueLogService;
    private final CouponIssueBatchService couponIssueBatchService;
    private final MemberAudienceService memberAudienceService;
    private final com.tigshop.service.user.UserMemberTagService userMemberTagService;

    @Override
    public void deleteByUser(Integer id, Integer userId) {
        if (id == null || userId == null || userId <= 0) {
            throw new GlobalException("用户未登录");
        }
        boolean removed = this.lambdaUpdate()
                .eq(UserCoupon::getId, id)
                .eq(UserCoupon::getUserId, userId)
                .remove();
        if (!removed) {
            throw new GlobalException("优惠券不存在");
        }
    }

    @Override
    public Page<UserCouponVO> list(UserCouponListDTO listDTO) {
        Page<UserCoupon> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 获取当前登录用户ID
        Integer userId = Optional.ofNullable(listDTO.getUserId())
                .orElse(SecurityUtils.getCurrentUserId());
        // 根据条件查询用户优惠券
        MPJLambdaWrapper<UserCoupon> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.leftJoin(Coupon.class, Coupon::getCouponId, UserCoupon::getCouponId);
        // 优惠券展示条件：领取就展示
        Long usedTime = listDTO.getUsedTime();
        if (usedTime != null) {
            queryWrapper.eq("used_time", usedTime);
        }
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByAsc("CASE " + "WHEN used_time = 0 AND start_date <= '" + StringUtils.getCurrentTime() + "' AND end_date >= '" + StringUtils.getCurrentTime() + "' THEN 0 " + "ELSE 1 END");
        queryWrapper.orderByAsc("start_date");

        Page<UserCoupon> userCouponPage = this.page(page, queryWrapper);
        List<UserCoupon> userCouponList = userCouponPage.getRecords();
        if (userCouponList != null && !userCouponList.isEmpty()) {
            List<UserCouponVO> userCouponVOList = new java.util.ArrayList<>(userCouponList.stream()
                    .map(userCoupon -> {
                        UserCouponVO userCouponVO = new UserCouponVO();
                        BeanUtils.copyProperties(userCoupon, userCouponVO);
                        userCouponVO.setStatus(getStatus(userCoupon.getStartDate(), userCoupon.getEndDate(), userCoupon.getOrderId()));
                        userCouponVO.setStatusName(getStatusName(userCoupon.getStartDate(), userCoupon.getEndDate(), userCoupon.getOrderId()));
                        userCouponVO.setStartDate(TigUtils.handelTime(userCoupon.getStartDate()));
                        userCouponVO.setEndDate(TigUtils.handelTime(userCoupon.getEndDate()));
                        //查询优惠券信息
                        LambdaQueryWrapper<Coupon> couponQueryWrapper = new LambdaQueryWrapper<>();
                        couponQueryWrapper.eq(Coupon::getCouponId, userCoupon.getCouponId());
                        Coupon coupon = couponMapper.selectOne(couponQueryWrapper);
                        if (coupon != null) {
                            BeanUtils.copyProperties(coupon, userCouponVO);
                            if (coupon.getSendRangeData() != null && !"[]".equals(coupon.getSendRangeData())) {
                                userCouponVO.setSendRangeData(JsonUtil.fromJson(coupon.getSendRangeData(), Integer[].class));
                            } else {
                                userCouponVO.setSendRangeData(new Integer[]{});
                            }
                        }
                        return userCouponVO;
                    })
                    .toList());
//            userCouponVOList.sort(Comparator.comparingInt(UserCouponVO::getStatus));
//            int page =  listDTO.getPage() == null ? 1 : listDTO.getPage();
//            int size = 9;
//            int fromIndex = (page - 1) * size;
//            int toIndex = Math.min(fromIndex + size, userCouponVOList.size());
            //return userCouponVOList.subList(fromIndex, toIndex);
            return PageUtil.convertPage(userCouponPage, userCouponVOList);
        } else {
            return new Page<>();
        }

    }

    /**
     * 获取优惠券状态
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param orderId   订单id
     * @return Integer
     */
    private Integer getStatus(Long startDate, Long endDate, Integer orderId) {
        if (orderId != null && orderId > 0) {
            return UserCouponStatus.STATUS_USED.getCode();
        }

        if (endDate < StringUtils.getCurrentTime()) {
            return UserCouponStatus.STATUS_EXPIRED.getCode();
        }

        if (startDate > StringUtils.getCurrentTime()) {
            return UserCouponStatus.STATUS_NOT_STARTED.getCode();
        }

        if (startDate < StringUtils.getCurrentTime() && StringUtils.getCurrentTime() - startDate < 3 * 24 * 60 * 60) {
            return UserCouponStatus.STATUS_EXPIRING_SOON.getCode();
        }
        return UserCouponStatus.STATUS_NORMAL.getCode();
    }

    private String getStatusName(Long startDate, Long endDate, Integer orderId) {
        if (orderId != null && orderId > 0) {
            return UserCouponStatus.STATUS_USED.getDescription();
        }

        if (endDate < StringUtils.getCurrentTime()) {
            return UserCouponStatus.STATUS_EXPIRED.getDescription();
        }

        if (startDate > StringUtils.getCurrentTime()) {
            return UserCouponStatus.STATUS_NOT_STARTED.getDescription();
        }

        if (startDate < StringUtils.getCurrentTime() && StringUtils.getCurrentTime() - startDate < 3 * 24 * 60 * 60) {
            return UserCouponStatus.STATUS_EXPIRING_SOON.getDescription();
        }
        return UserCouponStatus.STATUS_NORMAL.getDescription();
    }

    @Override
    public CouponDetailVO detail(Integer id) {
        Coupon coupon = couponMapper.selectById(id);
        if (coupon == null) {
            return null;
        }

        Integer userId = getCurrentUserId();
        long count = this.lambdaQuery()
                .eq(UserCoupon::getCouponId, coupon.getCouponId())
                .eq(UserCoupon::getUserId, userId)
                .count();

        CouponDetailVO couponDetailVO = new CouponDetailVO();
        BeanUtils.copyProperties(coupon, couponDetailVO);

        couponDetailVO.setReceiveNum(count);
        couponDetailVO.setUseStartDate(TigUtils.handelTime(coupon.getUseStartDate()));
        couponDetailVO.setUseEndDate(TigUtils.handelTime(coupon.getUseEndDate()));
        couponDetailVO.setAddTime(TigUtils.handelTime(coupon.getAddTime()));

        if (coupon.getSendRangeData() != null && !"[]".equals(coupon.getSendRangeData())) {
            couponDetailVO.setSendRangeData(JsonUtil.fromJson(coupon.getSendRangeData(), Integer[].class));
        } else {
            couponDetailVO.setSendRangeData(new Integer[]{});
        }

        if (coupon.getLimitUserRank() != null && !"[]".equals(coupon.getLimitUserRank())) {
            couponDetailVO.setLimitUserRank(JsonUtil.fromJson(coupon.getLimitUserRank(), Integer[].class));
        } else {
            couponDetailVO.setLimitUserRank(new Integer[]{});
        }

        if (userId > 0 && count > 0 && coupon.getLimitNum() > 0) {
            couponDetailVO.setIsReceive(1);
        } else {
            couponDetailVO.setIsReceive(0);
        }

        return couponDetailVO;
    }

    @Override
    public boolean create(UserCouponCreateDTO createDTO) {
        if (createDTO != null) {
            UserCoupon userCoupon = new UserCoupon();
            BeanUtils.copyProperties(createDTO, userCoupon);
            return this.save(userCoupon);
        }
        return false;
    }

    @Override
    public boolean update(UserCouponUpdateDTO updateDTO) {
        if (updateDTO != null) {
            UserCoupon userCoupon = new UserCoupon();
            BeanUtils.copyProperties(updateDTO, userCoupon);
            return this.updateById(userCoupon);
        }
        return false;
    }

    @Override
    public List<UserCoupon> getUserShopCouponList(Integer shopId) {
        return this.list(new LambdaQueryWrapper<UserCoupon>().eq(UserCoupon::getOrderId, 0)
                .eq(UserCoupon::getUsedTime, 0)
                .eq(UserCoupon::getUserId, SecurityUtils.getCurrentUserId())
                .le(UserCoupon::getStartDate, StringUtils.getCurrentTime())
                .ge(UserCoupon::getEndDate, StringUtils.getCurrentTime()));
    }

    @Override
    public void claim(UserCouponClaimParam param) {
        Integer userId = Optional.ofNullable(param.getUserId())
                .orElse(SecurityUtils.getCurrentUserId());
        Integer couponId = param.getCouponId();

        // 查询优惠券
        Coupon coupon = couponMapper.selectById(couponId);
        Assert.notNull(coupon, () -> new GlobalException(messageHelper.get("error.coupon.not.exist")));

        // 校验优惠券领取限制
        Long userCouponCount = this.lambdaQuery()
                .eq(UserCoupon::getCouponId, couponId)
                .eq(UserCoupon::getUserId, userId)
                .count();
        Assert.isTrue(userCouponCount < coupon.getLimitNum() || coupon.getLimitNum() == 0, () -> new GlobalException(messageHelper.get("error.coupon.claim.limit.exceeded")));

        // 校验优惠券发放限制
        Long sendCouponCount = this.lambdaQuery()
                .eq(UserCoupon::getCouponId, couponId)
                .count();
        Assert.isTrue(sendCouponCount < coupon.getSendNum() || coupon.getSendNum() == 0, () -> new GlobalException(messageHelper.get("error.coupon.claim.limit.exceeded")));

        // 会员是否能够领取
        if (coupon.getIsNewUser() == 1) {
            // 新人专享
            Assert.isTrue(userCouponCount == 0, () -> new GlobalException(messageHelper.get("error.coupon.claim.once.only")));

            // 判断是否是新人
            long orderCount = orderMapper.selectCount(Wrappers.lambdaQuery(Order.class)
                    .eq(Order::getUserId, userId)
                    .gt(Order::getPayStatus, 0));
            Assert.isTrue(orderCount == 0, () -> new GlobalException(messageHelper.get("error.coupon.claim.new.user.only")));
        }

        // 会员专享
        User user = userMapper.selectById(userId);

        if (coupon.getIsNewUser() == 2) {
            if (StringUtils.isNotBlank(coupon.getLimitUserRank()) && !"[]".equals(coupon.getLimitUserRank())) {
                List<Integer> rankIds = JSON.parseArray(coupon.getLimitUserRank(), Integer.class);

                // 如果 rankIds 非空，判断用户的 rankId 是否在限制范围内
                boolean rankFlag = CollUtil.isNotEmpty(rankIds) && !rankIds.contains(user.getRankId());
                Assert.isTrue(!rankFlag, () -> new GlobalException(messageHelper.get("error.coupon.claim.rank.not.allowed")));
            }
        }

        // 校验会员标记限制
        Promotion promotion = promotionMapper.selectOne(
                new LambdaQueryWrapper<Promotion>()
                        .eq(Promotion::getRelationId, couponId)
                        .eq(Promotion::getType, 2) // 优惠券类型
                        .last("limit 1")
        );
        if (Objects.nonNull(promotion)) {
            String memberScope = promotion.getMemberScope();
            if (StringUtils.isNotBlank(memberScope)) {
                // 二期：会员人群圈选优先
                boolean scopeMatched = memberAudienceService.matchesScope(userId.longValue(), memberScope);
                Assert.isTrue(scopeMatched, () -> new GlobalException(messageHelper.get("error.coupon.claim.audience.not.allowed")));
            } else if (Objects.equals(1, promotion.getMemberRange())) {
                // 兼容旧逻辑：按会员标记
                String memberMarks = promotion.getMemberMarks();
                List<Integer> allowedMarks = Optional.ofNullable(JSON.parseArray(memberMarks, Integer.class)).orElse(List.of());
                boolean markFlag = !allowedMarks.isEmpty() && !allowedMarks.contains(user.getMark());
                // 存量场景沿用改造前文案
                Assert.isTrue(!markFlag, () -> new GlobalException(messageHelper.get("error.coupon.claim.member.only")));
            }
        }

        // 计算使用期限
        long startDate, endDate;
        if (coupon.getSendType() > 0) {
            //固定时间
            startDate = coupon.getUseStartDate();
            endDate = coupon.getUseEndDate();
        } else {
            //领取天数
            startDate = StringUtils.getCurrentTime() + coupon.getDelayDay() * 24 * 60 * 60;
            endDate = startDate + coupon.getUseDay() * 24 * 60 * 60;
        }
        UserCoupon userCoupon = UserCoupon.builder()
                .couponId(coupon.getCouponId())
                .userId(userId)
                .startDate(startDate)
                .endDate(endDate)
                .build();
        this.save(userCoupon);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public boolean issueToUser(Integer couponId, Integer userId) {
        if (couponId == null || userId == null || userId <= 0) return false;
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null || Objects.equals(coupon.getIsDelete(), 1)) return false;
        if (Optional.ofNullable(coupon.getSendType()).orElse(0) > 0
                && (coupon.getUseStartDate() == null || coupon.getUseEndDate() == null)) return false;
        CouponTimeRange timeRange = buildCouponTimeRange(coupon);
        if (timeRange.endDate <= StringUtils.getCurrentTime() || timeRange.endDate <= timeRange.startDate) return false;
        User user = userMapper.selectById(userId);
        if (user == null || !Objects.equals(user.getUserType(), UserTypeEnum.MEMBER.getCode())
                || !Objects.equals(user.getStatus(), 1)) return false;
        if (getRemainingIssueCount(coupon) < 1) return false;

        if (!this.save(buildIssuedCoupon(coupon, userId, timeRange))) {
            throw new GlobalException("优惠券发放失败");
        }
        return true;
    }

    @Override
    public Page<CouponIssueUserVO> searchIssueUsers(CouponIssueUserPageQuery query) {
        Page<User> page = new Page<>(query.getPage(), query.getSize());
        page.setOrders(List.of(OrderItem.desc("user_id")));

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserType, UserTypeEnum.MEMBER.getCode())
                .and(StrUtil.isNotBlank(query.getKeyword()),
                        wrapper -> wrapper.like(User::getUsername, query.getKeyword())
                                .or()
                                .like(User::getNickname, query.getKeyword())
                                .or()
                                .like(User::getMobile, query.getKeyword())
                                .or()
                                .like(User::getEmail, query.getKeyword()));
        applyIssueUserSourceFilter(queryWrapper, query.getFromTag());

        Page<User> userPage = userMapper.selectPage(page, queryWrapper);
        List<CouponIssueUserVO> records = userPage.getRecords()
                .stream()
                .map(this::convertToIssueUserVO)
                .toList();
        return PageUtil.convertPage(userPage, records);
    }

    @Override
    public CouponManualIssuePreviewVO previewManualIssue(CouponManualIssueParam param) {
        Coupon coupon = getManualIssueCoupon(param.getCouponId());
        String issueType = normalizeIssueType(param.getIssueType());
        ManualIssueTargetResult targetResult = resolveManualIssueTargets(param, issueType);
        validateManualIssueLimits(coupon, targetResult);

        CouponManualIssuePreviewVO previewVO = new CouponManualIssuePreviewVO();
        previewVO.setCouponId(coupon.getCouponId());
        previewVO.setIssueType(issueType);
        previewVO.setMemberCount(targetResult.validUsers.size());
        previewVO.setCouponCount(targetResult.validUsers.size());
        previewVO.setFailCount(targetResult.failUserCount);
        previewVO.setFailDetails(targetResult.failDetails);
        return previewVO;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public CouponManualIssueResultVO manualIssue(CouponManualIssueParam param) {
        Coupon coupon = getManualIssueCoupon(param.getCouponId());
        String issueType = normalizeIssueType(param.getIssueType());
        ManualIssueTargetResult targetResult = resolveManualIssueTargets(param, issueType);
        validateManualIssueLimits(coupon, targetResult);

        CouponTimeRange timeRange = buildCouponTimeRange(coupon);
        List<UserCoupon> issueCoupons = targetResult.validUsers.stream()
                .map(user -> buildIssuedCoupon(coupon, user.getUserId(), timeRange))
                .toList();

        if (CollUtil.isNotEmpty(issueCoupons)) {
            this.saveBatch(issueCoupons, 200);

            // 创建发放批次
            int userCount = (int) issueCoupons.stream().map(UserCoupon::getUserId).distinct().count();
            Long batchId = couponIssueBatchService.createBatch(
                    coupon.getCouponId(), userCount, issueCoupons.size(), 2);

            // 批量记录发放日志
            for (UserCoupon uc : issueCoupons) {
                couponIssueLogService.log(batchId, uc.getCouponId(), uc.getId(), uc.getUserId(), 2);
            }
        }

        CouponManualIssueResultVO resultVO = new CouponManualIssueResultVO();
        resultVO.setCouponId(coupon.getCouponId());
        resultVO.setIssueType(issueType);
        resultVO.setSuccessCount(issueCoupons.size());
        resultVO.setCouponCount(issueCoupons.size());
        resultVO.setFailCount(0);
        return resultVO;
    }

    private void applyIssueUserSourceFilter(LambdaQueryWrapper<User> queryWrapper, Integer fromTag) {
        if (fromTag == null || fromTag <= 0) {
            return;
        }
        if (Objects.equals(fromTag, 5)) {
            queryWrapper.in(User::getFromTag, List.of(5, 6, 8));
            return;
        }
        queryWrapper.eq(User::getFromTag, fromTag);
    }

    private CouponIssueUserVO convertToIssueUserVO(User user) {
        CouponIssueUserVO userVO = new CouponIssueUserVO();
        userVO.setUserId(user.getUserId());
        userVO.setMemberName(user.getName());
        userVO.setAvatar(StringUtils.isNotBlank(user.getAvatar()) ? user.getAvatar() : userService.getIfAvailable().getUserAvatar(user.getAvatar()));
        userVO.setUsername(user.getUsername());
        userVO.setNickname(user.getNickname());
        userVO.setMobile(user.getMobile());
        userVO.setEmail(user.getEmail());
        userVO.setFromTag(user.getFromTag());
        userVO.setFromTagName(FromTag.getFromTagName(Optional.ofNullable(user.getFromTag()).orElse(0)));
        userVO.setIsCompanyAuth(Optional.ofNullable(user.getIsCompanyAuth()).orElse(0));
        userVO.setIsCertified(isCertifiedUser(user) ? 1 : 0);
        userVO.setStatus(Optional.ofNullable(user.getStatus()).orElse(0));
        userVO.setStatusName(Objects.equals(userVO.getStatus(), 1) ? "启用" : "禁用");
        return userVO;
    }

    private boolean isCertifiedUser(User user) {
        return Objects.equals(user.getIsCompanyAuth(), 1)
                || Objects.equals(user.getMobileValidated(), 1)
                || Objects.equals(user.getEmailValidated(), 1);
    }

    private Coupon getManualIssueCoupon(Integer couponId) {
        Coupon coupon = couponMapper.selectById(couponId);
        Assert.notNull(coupon, () -> new GlobalException(messageHelper.get("error.coupon.not.exist")));
        Assert.isTrue(!Objects.equals(coupon.getIsDelete(), 1), () -> new GlobalException(messageHelper.get("error.coupon.not.exist")));

        Integer currentShopId = Optional.ofNullable(getShopId()).orElse(0);
        Assert.isTrue(Objects.equals(coupon.getShopId(), currentShopId), () -> new GlobalException(messageHelper.get("error.coupon.issue.no.permission")));
        return coupon;
    }

    private String normalizeIssueType(String issueType) {
        String normalized =StrUtil.trimToEmpty(issueType).toUpperCase(Locale.ROOT);
        Assert.isTrue(Objects.equals(normalized, CouponManualIssueParam.ISSUE_TYPE_USER)
                        || Objects.equals(normalized, CouponManualIssueParam.ISSUE_TYPE_TAG)
                        || Objects.equals(normalized, CouponManualIssueParam.ISSUE_TYPE_RANK),
                () -> new GlobalException(messageHelper.get("error.coupon.issue.mode.invalid")));
        return normalized;
    }

    private ManualIssueTargetResult resolveManualIssueTargets(CouponManualIssueParam param, String issueType) {
        boolean hasUsers = CollUtil.isNotEmpty(param.getUserIds());
        boolean hasRanks = CollUtil.isNotEmpty(param.getRankIds());
        boolean hasTags = CollUtil.isNotEmpty(param.getTagIds());

        if (Objects.equals(issueType, CouponManualIssueParam.ISSUE_TYPE_USER)) {
            Assert.isTrue(hasUsers, () -> new GlobalException(messageHelper.get("error.coupon.issue.member.required")));
            Assert.isTrue(!hasTags && !hasRanks, () -> new GlobalException(messageHelper.get("error.coupon.issue.mixed.not.supported")));
            return resolveManualIssueUsers(param.getUserIds());
        }

        if (Objects.equals(issueType, CouponManualIssueParam.ISSUE_TYPE_TAG)) {
            Assert.isTrue(hasTags, () -> new GlobalException(messageHelper.get("error.coupon.issue.tag.required")));
            Assert.isTrue(!hasUsers && !hasRanks, () -> new GlobalException(messageHelper.get("error.coupon.issue.mixed.not.supported")));
            return resolveManualIssueTags(param.getTagIds(), param.getTagMatchMode());
        }

        Assert.isTrue(hasRanks, () -> new GlobalException(messageHelper.get("error.coupon.issue.rank.required")));
        Assert.isTrue(!hasUsers && !hasTags, () -> new GlobalException(messageHelper.get("error.coupon.issue.mixed.not.supported")));
        return resolveManualIssueRanks(param.getRankIds());
    }

    private ManualIssueTargetResult resolveManualIssueUsers(List<Integer> userIds) {
        ManualIssueTargetResult result = new ManualIssueTargetResult();
        List<Integer> distinctUserIds = userIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (distinctUserIds.isEmpty()) {
            return result;
        }

        Map<Integer, User> userMap = userMapper.selectBatchIds(distinctUserIds)
                .stream()
                .filter(user -> Objects.equals(user.getUserType(), UserTypeEnum.MEMBER.getCode()))
                .collect(Collectors.toMap(User::getUserId, user -> user));

        for (Integer userId : distinctUserIds) {
            User user = userMap.get(userId);
            if (user == null) {
                recordManualIssueFail(result, buildManualIssueFailDetail(userId, null, "会员不存在"));
                continue;
            }
            if (!Objects.equals(user.getStatus(), 1)) {
                recordManualIssueFail(result, buildManualIssueFailDetail(userId, user, "会员已禁用"));
                continue;
            }
            result.validUsers.add(user);
        }
        return result;
    }

    private ManualIssueTargetResult resolveManualIssueRanks(List<Integer> rankIds) {
        ManualIssueTargetResult result = new ManualIssueTargetResult();
        List<Integer> distinctRankIds = rankIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (distinctRankIds.isEmpty()) {
            return result;
        }

        List<User> rankUsers = userMapper.selectList(new LambdaQueryWrapper<User>()
                .in(User::getRankId, distinctRankIds)
                .eq(User::getUserType, UserTypeEnum.MEMBER.getCode()));

        Map<Integer, User> userMap = new LinkedHashMap<>();
        for (User user : rankUsers) {
            userMap.putIfAbsent(user.getUserId(), user);
        }
        for (User user : userMap.values()) {
            if (!Objects.equals(user.getStatus(), 1)) {
                recordManualIssueFail(result, buildManualIssueFailDetail(user.getUserId(), user, "会员已禁用"));
                continue;
            }
            result.validUsers.add(user);
        }
        return result;
    }

    /**
     * 按会员标签解析发放人群。
     * <p>
     * 标签匹配语义直接复用 {@link com.tigshop.service.user.UserMemberTagService#resolveUserIdsByTags}，
     * 与会员列表「按标签筛选」逐行一致；筛出用户后再套用与 RANK 模式相同的会员口径。
     */
    private ManualIssueTargetResult resolveManualIssueTags(List<Long> tagIds, Integer tagMatchMode) {
        ManualIssueTargetResult result = new ManualIssueTargetResult();
        List<Long> distinctTagIds = tagIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (distinctTagIds.isEmpty()) {
            return result;
        }

        // 不存在或已删除的标签自然匹配不到记录，不报错；解析为空即「有效会员 0 人」
        List<Long> tagUserIds = userMemberTagService.resolveUserIdsByTags(distinctTagIds, tagMatchMode);
        if (CollUtil.isEmpty(tagUserIds)) {
            return result;
        }

        List<User> tagUsers = userMapper.selectList(new LambdaQueryWrapper<User>()
                .in(User::getUserId, tagUserIds)
                .eq(User::getUserType, UserTypeEnum.MEMBER.getCode()));

        Map<Integer, User> userMap = new LinkedHashMap<>();
        for (User user : tagUsers) {
            userMap.putIfAbsent(user.getUserId(), user);
        }
        for (User user : userMap.values()) {
            if (!Objects.equals(user.getStatus(), 1)) {
                recordManualIssueFail(result, buildManualIssueFailDetail(user.getUserId(), user, "会员已禁用"));
                continue;
            }
            result.validUsers.add(user);
        }
        return result;
    }

    private void validateManualIssueLimits(Coupon coupon, ManualIssueTargetResult targetResult) {
        if (CollUtil.isEmpty(targetResult.validUsers)) {
            return;
        }

        long needIssueCount = targetResult.validUsers.size();
        long remainCount = getRemainingIssueCount(coupon);
        if (remainCount < needIssueCount) {
            throw new GlobalException(String.format("发放失败，券量不足（剩余 %d 张，需发 %d 张），请调整发行量后重试",
                    remainCount, needIssueCount));
        }
    }

    private long getRemainingIssueCount(Coupon coupon) {
        int sendNum = normalizeManualIssueLimit(coupon.getSendNum());
        if (sendNum == Integer.MAX_VALUE) return Long.MAX_VALUE;
        long issuedCount = userCouponMapper.selectCount(Wrappers.<UserCoupon>lambdaQuery()
                .eq(UserCoupon::getCouponId, coupon.getCouponId()));
        return Math.max((long) sendNum - issuedCount, 0L);
    }

    private Map<Integer, Long> getManualIssueUserReceiveCountMap(Integer couponId, List<User> users) {
        List<Integer> userIds = users.stream()
                .map(User::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyMap();
        }

        return this.lambdaQuery()
                .select(UserCoupon::getUserId)
                .eq(UserCoupon::getCouponId, couponId)
                .in(UserCoupon::getUserId, userIds)
                .list()
                .stream()
                .collect(Collectors.groupingBy(UserCoupon::getUserId, Collectors.counting()));
    }

    private int normalizeManualIssueLimit(Integer limit) {
        return limit == null || limit <= 0 ? Integer.MAX_VALUE : limit;
    }

    private void recordManualIssueFail(ManualIssueTargetResult result, CouponManualIssueFailDetailVO failDetail) {
        result.failDetails.add(failDetail);
        result.failUserCount++;
    }

    private void recordManualIssueBatchFail(ManualIssueTargetResult result, CouponManualIssueFailDetailVO failDetail, int failUserCount) {
        result.failDetails.add(failDetail);
        result.failUserCount += failUserCount;
    }

    private CouponManualIssueFailDetailVO buildManualIssueFailDetail(Integer userId, User user, String reason) {
        CouponManualIssueFailDetailVO detailVO = new CouponManualIssueFailDetailVO();
        detailVO.setUserId(userId);
        detailVO.setMemberName(user == null ? "" : user.getName());
        detailVO.setMobile(user == null ? "" : user.getMobile());
        detailVO.setReason(reason);
        return detailVO;
    }

    private CouponManualIssueFailDetailVO buildManualIssueSummaryFailDetail(String reason) {
        CouponManualIssueFailDetailVO detailVO = new CouponManualIssueFailDetailVO();
        detailVO.setUserId(0);
        detailVO.setMemberName("批量发放");
        detailVO.setMobile("");
        detailVO.setReason(reason);
        return detailVO;
    }

    private String buildManualIssueFailMessage(ManualIssueTargetResult targetResult) {
        List<String> detailMessages = targetResult.failDetails.stream()
                .map(this::formatManualIssueFailMessage)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .limit(10)
                .toList();

        StringBuilder message = new StringBuilder("手动发放失败，已全部回滚。");
        if (CollUtil.isNotEmpty(detailMessages)) {
            message.append("失败原因：").append(String.join("；", detailMessages)).append("。");
        }
        if (targetResult.failDetails.size() > detailMessages.size()) {
            message.append("其余").append(targetResult.failDetails.size() - detailMessages.size()).append("条失败明细可在发放预览中查看。");
        }
        return message.toString();
    }

    private String formatManualIssueFailMessage(CouponManualIssueFailDetailVO failDetail) {
        if (failDetail == null || StrUtil.isBlank(failDetail.getReason())) {
            return "";
        }

        if (Objects.equals(failDetail.getUserId(), 0)) {
            return failDetail.getReason();
        }

        String memberLabel = buildManualIssueMemberLabel(failDetail);
        return StrUtil.isBlank(memberLabel) ? failDetail.getReason() : memberLabel + failDetail.getReason();
    }

    private String buildManualIssueMemberLabel(CouponManualIssueFailDetailVO failDetail) {
        List<String> parts = new ArrayList<>();
        if (StrUtil.isNotBlank(failDetail.getMemberName())) {
            parts.add(failDetail.getMemberName());
        }
        if (StrUtil.isNotBlank(failDetail.getMobile())) {
            parts.add(failDetail.getMobile());
        }
        if (CollUtil.isEmpty(parts) && failDetail.getUserId() != null) {
            parts.add("ID:" + failDetail.getUserId());
        }
        return CollUtil.isEmpty(parts) ? "" : "会员[" + String.join("/", parts) + "]：";
    }

    private UserCoupon buildIssuedCoupon(Coupon coupon, Integer userId, CouponTimeRange timeRange) {
        return UserCoupon.builder()
                .couponId(coupon.getCouponId())
                .userId(userId)
                .orderId(0)
                .usedTime(0L)
                .startDate(timeRange.startDate)
                .endDate(timeRange.endDate)
                .sourceType(2)
                .build();
    }

    private CouponTimeRange buildCouponTimeRange(Coupon coupon) {
        long currentTime = StringUtils.getCurrentTime();
        if (Optional.ofNullable(coupon.getSendType()).orElse(0) > 0) {
            return new CouponTimeRange(coupon.getUseStartDate(), coupon.getUseEndDate());
        }

        long startDate = currentTime + Optional.ofNullable(coupon.getDelayDay()).orElse(0) * 24L * 60 * 60;
        long endDate = startDate + Optional.ofNullable(coupon.getUseDay()).orElse(0) * 24L * 60 * 60;
        return new CouponTimeRange(startDate, endDate);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void useCoupon(List<Integer> useCouponIds, List<Integer> selectUserCouponIds, Integer currentUserId, Integer orderId) {
        long currentTime = StringUtils.getCurrentTime();
        List<Integer> distinctCouponIds = Optional.ofNullable(useCouponIds)
                .orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        List<Integer> distinctUserCouponIds = Optional.ofNullable(selectUserCouponIds)
                .orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (CollUtil.isNotEmpty(distinctUserCouponIds)) {
            for (Integer userCouponId : distinctUserCouponIds) {
                UserCoupon userCoupon = this.getOne(new LambdaQueryWrapper<UserCoupon>()
                        .eq(UserCoupon::getId, userCouponId)
                        .eq(UserCoupon::getUserId, currentUserId)
                        .eq(UserCoupon::getOrderId, 0)
                        .eq(UserCoupon::getUsedTime, 0)
                        .le(UserCoupon::getStartDate, currentTime)
                        .ge(UserCoupon::getEndDate, currentTime)
                        .last("limit 1"));
                if (userCoupon == null || (CollUtil.isNotEmpty(distinctCouponIds) && !distinctCouponIds.contains(userCoupon.getCouponId()))) {
                    throw new RuntimeException(messageHelper.get("error.coupon.not.exist"));
                }
                userCoupon.setOrderId(orderId);
                userCoupon.setUsedTime(currentTime);
                this.updateById(userCoupon);
            }
            return;
        }

        for (Integer useCouponId : distinctCouponIds) {
            UserCoupon userCoupon = this.getOne(new LambdaQueryWrapper<UserCoupon>().eq(UserCoupon::getCouponId, useCouponId)
                    .eq(UserCoupon::getUserId, currentUserId)
                    .eq(UserCoupon::getOrderId, 0)
                    .eq(UserCoupon::getUsedTime, 0)
                    .le(UserCoupon::getStartDate, currentTime)
                    .ge(UserCoupon::getEndDate, currentTime)
                    .last("limit 1"));
            if (userCoupon != null) {
                userCoupon.setOrderId(orderId);
                userCoupon.setUsedTime(currentTime);
                this.updateById(userCoupon);
            } else {
                throw new RuntimeException(messageHelper.get("error.coupon.not.exist"));
            }
        }
    }

    @Override
    public Long getUserCouponCount(int userId) {
        Long time = StringUtils.getCurrentTime();
        LambdaQueryWrapper<UserCoupon> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserCoupon::getUserId, userId);
        queryWrapper.eq(UserCoupon::getOrderId, 0);
        queryWrapper.le(UserCoupon::getStartDate, time);
        queryWrapper.ge(UserCoupon::getEndDate, time);
        return this.count(queryWrapper);
    }

    @Override
    public UserCoupon getUserCouponByCouponId(Integer currentUserId, Integer relationId) {

        return this.getOne(new LambdaQueryWrapper<UserCoupon>().eq(UserCoupon::getUserId, currentUserId)
                .eq(UserCoupon::getCouponId, relationId)
                .eq(UserCoupon::getOrderId, 0)
                .le(UserCoupon::getStartDate, StringUtils.getCurrentTime())
                .ge(UserCoupon::getEndDate, StringUtils.getCurrentTime())
                .last("limit 1"));
    }

    @Override
    public List<OrderCheckVO.Coupon> listUserCouponList(Integer currentUserId, Integer shopId) {
        List<OrderCheckVO.Coupon> coupons = this.baseMapper.listUserCouponList(currentUserId, shopId);
        if (coupons == null || coupons.isEmpty()) {
            return coupons;
        }

        List<Integer> couponIds = new ArrayList<>(coupons.size());
        for (OrderCheckVO.Coupon coupon : coupons) {
            String endDate = coupon.getEndDate();
            if (StringUtils.isNotBlank(endDate)) {
                coupon.setEndDate(TigUtils.handelTime(Long.valueOf(endDate)));
            } else {
                coupon.setEndDate("");
            }
            couponIds.add(coupon.getCouponId());
        }

        //增加一个用户标记过滤
        User user = userMapper.selectById(currentUserId);
        // 需要被过滤掉的优惠券ID（用户标记不匹配）
        Set<Integer> excludedCouponIds = getUserMarkExcludedCouponIds(user, couponIds);

        // 过滤掉用户标记不匹配的优惠券
        if (!excludedCouponIds.isEmpty()) {
            coupons = coupons.stream()
                    .filter(coupon -> !excludedCouponIds.contains(coupon.getCouponId()))
                    .toList();
        }

        return coupons;
    }

    @NotNull
    private Set<Integer> getUserMarkExcludedCouponIds(User user, Collection<Integer> couponIds) {
        Set<Integer> excludedCouponIds = new HashSet<>();
        if (Objects.nonNull(user)) {
            //查询关联的促销活动
            List<Promotion> promotions = promotionMapper.selectList(
                    new LambdaQueryWrapper<Promotion>()
                            .in(Promotion::getRelationId, couponIds)
                            .eq(Promotion::getType, 2) // 优惠券类型
            );
            for (Promotion promotion : promotions) {
                if (Objects.nonNull(promotion) && Objects.equals(1, promotion.getMemberRange())) {
                    String memberMarks = promotion.getMemberMarks();
                    //当前优惠必须采用的标识列表
                    List<Integer> allowedMarks = Optional.ofNullable(JSON.parseArray(memberMarks, Integer.class)).orElse(List.of());
                    //如果用户标记不在允许列表中，则该优惠券需要被过滤
                    if (!allowedMarks.isEmpty() && !allowedMarks.contains(user.getMark())) {
                        excludedCouponIds.add(promotion.getRelationId());
                    }
                }
            }
        }
        return excludedCouponIds;
    }


    private static class ManualIssueTargetResult {
        private final List<User> validUsers = new ArrayList<>();
        private final List<CouponManualIssueFailDetailVO> failDetails = new ArrayList<>();
        private int failUserCount;
    }

    private static class CouponTimeRange {
        private final long startDate;
        private final long endDate;

        private CouponTimeRange(long startDate, long endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }
}
