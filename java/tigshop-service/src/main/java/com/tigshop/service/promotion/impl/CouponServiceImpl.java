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

package com.tigshop.service.promotion.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.cart.CartPromotionParsePriceDTO;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.promotion.CouponDTO;
import com.tigshop.bean.enums.promotion.PromotionType;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.promotion.Coupon;
import com.tigshop.bean.model.promotion.Promotion;
import com.tigshop.bean.model.user.UserCoupon;
import com.tigshop.bean.model.user.UserRank;
import com.tigshop.bean.query.promotion.coupon.CouponPageQuery;
import com.tigshop.bean.vo.cart.CartVO;
import com.tigshop.bean.vo.promotion.CouponClientVO;
import com.tigshop.bean.vo.promotion.CouponDetailVO;
import com.tigshop.bean.vo.promotion.CouponPageVO;
import com.tigshop.bean.vo.promotion.CouponStatusCountVO;
import com.tigshop.bean.vo.promotion.CouponVO;
import com.tigshop.bean.vo.promotion.PromotionVO;
import com.tigshop.common.core.TranslatePackage;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.i18n.MessageHelper;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.mapper.order.OrderMapper;
import com.tigshop.mapper.promotion.CouponMapper;
import com.tigshop.mapper.user.UserCouponMapper;
import com.tigshop.service.authority.AdminLogService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.promotion.CouponService;
import com.tigshop.service.promotion.PromotionService;
import com.tigshop.service.user.UserRankService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import com.tigshop.bean.model.user.User;
import com.tigshop.mapper.promotion.PromotionMapper;
import com.tigshop.mapper.user.UserMapper;
import com.alibaba.fastjson.JSON;

import com.tigshop.common.constant.Constants;
import com.tigshop.common.constant.promotion.CouponConstants;
import static com.tigshop.common.constant.ExceptionConstants.SERVICE_ERROR;
import static com.tigshop.common.utils.SecurityUtils.getCurrentUserId;

/**
 * 优惠券服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@RequiredArgsConstructor
@Service
public class CouponServiceImpl extends BaseServiceImpl<CouponMapper, Coupon> implements CouponService {

    private final AdminLogService adminLogService;

    private final PromotionService promotionService;

    private final UserRankService userRankService;

    private final UserCouponMapper userCouponMapper;
    private final TranslatePackage translatePackage;
    private final MessageHelper messageHelper;
    private final UserMapper userMapper;
    private final PromotionMapper promotionMapper;
    private final OrderMapper orderMapper;
    private final com.tigshop.service.user.MemberAudienceService memberAudienceService;

    @Override
    public Page<CouponVO> list(CouponPageQuery listDTO) {
        long now = StringUtils.getCurrentTime();

        // 分页
        Page<Coupon> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<Coupon> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 搜索关键字
        String keyword = listDTO.getKeyword();
        if (StrUtil.isNotEmpty(keyword)) {
            queryWrapper.like(Coupon::getCouponName, keyword);
        }

        Integer shopId = getShopId();
        if (shopId != null && shopId > -1) {
            queryWrapper.eq(Coupon::getShopId, shopId);
        }
        queryWrapper.eq(Coupon::getIsDelete, 0);

        // 状态筛选（按用券时间派生：1=进行中 2=待生效 3=已失效；sendType=0 视为进行中）
        Integer status = listDTO.getStatus();
        applyStatusCondition(queryWrapper, status, now);

        // 执行查询
        Page<Coupon> couponPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<Coupon> couponPageRecords = couponPage.getRecords();

        // 批量统计当前页优惠券的已领取数量
        Map<Integer, Integer> receiveNumMap = getReceiveNumMap(couponPageRecords);

        // 转换为VO
        List<CouponVO> couponVOList = couponPageRecords.stream()
                .map(coupon -> {
                    CouponVO couponVO = new CouponVO();
                    convert(coupon, couponVO);
                    BeanUtils.copyProperties(coupon, couponVO);
                    int couponStatus = computeCouponStatus(coupon, now);
                    couponVO.setStatus(couponStatus);
                    couponVO.setStatusName(getStatusName(couponStatus));
                    couponVO.setReceiveNum(receiveNumMap.getOrDefault(coupon.getCouponId(), 0));
                    return couponVO;
                }).toList();

        // 组装结果（保留标准分页字段，附带各状态数量）
        CouponPageVO result = new CouponPageVO();
        BeanUtils.copyProperties(couponPage, result);
        result.setRecords(couponVOList);
        result.setStatusCount(countStatus(shopId, now));
        return result;
    }

    /**
     * 按状态向查询条件追加用券时间约束。
     *
     * @param queryWrapper 查询条件
     * @param status       1=进行中 2=待生效 3=已失效；其它/空表示不限
     * @param now          当前时间（秒）
     */
    private void applyStatusCondition(LambdaQueryWrapper<Coupon> queryWrapper, Integer status, long now) {
        if (status == null) {
            return;
        }
        switch (status) {
            // 待生效：固定时间段且开始时间晚于当前
            case 2 -> queryWrapper.ne(Coupon::getSendType, 0).gt(Coupon::getUseStartDate, now);
            // 已失效：固定时间段且结束时间早于当前
            case 3 -> queryWrapper.ne(Coupon::getSendType, 0).gt(Coupon::getUseEndDate, 0).lt(Coupon::getUseEndDate, now);
            // 进行中：sendType=0（按领取后N天）或处于用券时间段内
            case 1 -> queryWrapper.and(w -> w
                    .eq(Coupon::getSendType, 0)
                    .or(o -> o.le(Coupon::getUseStartDate, now).ge(Coupon::getUseEndDate, now)));
            default -> {
            }
        }
    }

    /**
     * 派生优惠券状态。
     *
     * @param coupon 优惠券
     * @param now    当前时间（秒）
     * @return 1=进行中 2=待生效 3=已失效
     */
    private int computeCouponStatus(Coupon coupon, long now) {
        // sendType=0：按领取后多少天可用，无固定结束时间，视为进行中
        if (coupon.getSendType() != null && coupon.getSendType() == 0) {
            return 1;
        }
        Long start = coupon.getUseStartDate();
        Long end = coupon.getUseEndDate();
        if (start != null && start > now) {
            return 2;
        }
        if (end != null && end > 0 && now > end) {
            return 3;
        }
        return 1;
    }

    /**
     * 状态名称。
     */
    private String getStatusName(int status) {
        return switch (status) {
            case 2 -> "待生效";
            case 3 -> "已失效";
            default -> "进行中";
        };
    }

    /**
     * 统计当前店铺各状态优惠券数量（与列表 status 入参无关，按全量统计）。
     *
     * @param shopId 店铺ID
     * @param now    当前时间（秒）
     * @return 各状态数量
     */
    private CouponStatusCountVO countStatus(Integer shopId, long now) {
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();
        if (shopId != null && shopId > -1) {
            wrapper.eq(Coupon::getShopId, shopId);
        }
        wrapper.eq(Coupon::getIsDelete, 0);
        wrapper.select(Coupon::getSendType, Coupon::getUseStartDate, Coupon::getUseEndDate);
        List<Coupon> coupons = this.list(wrapper);

        CouponStatusCountVO statusCount = new CouponStatusCountVO();
        int processing = 0;
        int pending = 0;
        int expired = 0;
        for (Coupon coupon : coupons) {
            switch (computeCouponStatus(coupon, now)) {
                case 2 -> pending++;
                case 3 -> expired++;
                default -> processing++;
            }
        }
        statusCount.setProcessing(processing);
        statusCount.setPending(pending);
        statusCount.setExpired(expired);
        return statusCount;
    }

    /**
     * 批量统计优惠券已领取数量（user_coupon 按 coupon_id 计数）。
     *
     * @param coupons 当前页优惠券
     * @return couponId -> 已领取数量
     */
    private Map<Integer, Integer> getReceiveNumMap(List<Coupon> coupons) {
        Map<Integer, Integer> receiveNumMap = new HashMap<>();
        if (CollUtil.isEmpty(coupons)) {
            return receiveNumMap;
        }
        List<Integer> couponIds = coupons.stream().map(Coupon::getCouponId).toList();
        List<Map<String, Object>> rows = userCouponMapper.selectMaps(Wrappers.<UserCoupon>query()
                .select("coupon_id", "COUNT(*) AS cnt")
                .in("coupon_id", couponIds)
                .groupBy("coupon_id"));
        for (Map<String, Object> row : rows) {
            Object couponId = row.get("coupon_id");
            Object cnt = row.get("cnt");
            if (couponId == null) {
                continue;
            }
            receiveNumMap.put(((Number) couponId).intValue(), cnt == null ? 0 : ((Number) cnt).intValue());
        }
        return receiveNumMap;
    }

    /**
     * 数据转换
     *
     * @param coupon   coupon
     * @param couponVO couponVO
     */
    public void convert(Coupon coupon, CouponVO couponVO) {
        // 时间转换
        Long useStartTime = coupon.getUseStartDate();
        if (useStartTime > 0) {
            DateTime useStartDate = DateUtil.date(useStartTime * 1000);
            couponVO.setUseStartDate(DateUtil.format(useStartDate, "yyyy-MM-dd HH:mm:ss"));
        } else {
            couponVO.setUseStartDate("0");
        }

        Long useEndTime = coupon.getUseEndDate();
        if (useEndTime > 0) {
            DateTime useEndDate = DateUtil.date(useEndTime * 1000);
            couponVO.setUseEndDate(DateUtil.format(useEndDate, "yyyy-MM-dd HH:mm:ss"));
        } else {
            couponVO.setUseEndDate("0");
        }

        DateTime addDate = DateUtil.date(coupon.getAddTime() * 1000);
        couponVO.setAddTime(DateUtil.format(addDate, "yyyy-MM-dd HH:mm:ss"));

        // json 转换
        couponVO.setSendRangeData(StringUtils.str2IntList(coupon.getSendRangeData()));
        couponVO.setLimitUserRank(StringUtils.str2IntList(coupon.getLimitUserRank()));
    }

    @Override
    public CouponVO detail(Integer id) {
        if (id != null && id > 0) {
            Coupon coupon = this.getById(id);
            CouponVO couponVO = new CouponVO();
            convert(coupon, couponVO);
            BeanUtils.copyProperties(coupon, couponVO);

            // 从 promotion 表获取适用会员范围字段
            Promotion promotion = promotionService.getOne(new LambdaQueryWrapper<Promotion>()
                    .eq(Promotion::getRelationId, id)
                    .eq(Promotion::getType, PromotionType.TYPE_COUPON.getCode()));
            if (promotion != null) {
                couponVO.setMemberRange(promotion.getMemberRange());
                couponVO.setMemberMarks(StringUtils.str2IntList(promotion.getMemberMarks()));
                com.tigshop.bean.dto.user.SendUserTargetDTO sendUserTarget =
                        memberAudienceService.parseScope(promotion.getMemberScope());
                couponVO.setSendUserTarget(memberAudienceService.fillTargetUsers(sendUserTarget));
                couponVO.setMemberAudience(memberAudienceService.toLegacy(sendUserTarget));
            }

            return couponVO;
        }
        return null;
    }

    @Override
    public List<CouponVO> getCouponListByIds(List<Integer> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<Coupon>()
                .in(Coupon::getCouponId, ids)
                .eq(Coupon::getIsDelete, 0);
        Integer shopId = getShopId();
        if (shopId != null && shopId > -1) {
            wrapper.eq(Coupon::getShopId, shopId);
        }
        return this.list(wrapper).stream().map(coupon -> {
            CouponVO vo = new CouponVO();
            convert(coupon, vo);
            BeanUtils.copyProperties(coupon, vo);
            return vo;
        }).toList();
    }

    @Override
    public List<CouponClientVO> getClientCouponListByIds(List<Integer> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<Coupon> coupons = this.lambdaQuery()
                .in(Coupon::getCouponId, ids)
                .eq(Coupon::getIsDelete, 0)
                .eq(Coupon::getIsShow, 1)
                .list();
        if (CollUtil.isEmpty(coupons)) {
            return Collections.emptyList();
        }
        // 已登录则查询用户领取记录以计算 isReceive，未登录则空列表
        Integer userId = getCurrentUserId();
        List<UserCoupon> userCoupons = Collections.emptyList();
        if (userId != null && userId > 0) {
            List<Integer> couponIds = coupons.stream().map(Coupon::getCouponId).toList();
            userCoupons = userCouponMapper.selectList(
                    Wrappers.lambdaQuery(UserCoupon.class)
                            .in(UserCoupon::getCouponId, couponIds)
                            .eq(UserCoupon::getUserId, userId)
            );
        }
        final List<UserCoupon> finalUserCoupons = userCoupons;
        return coupons.stream()
                .map(coupon -> new CouponClientVO(coupon, finalUserCoupons))
                .toList();
    }
    @Override
    public boolean updateField(UpdateFieldDTO updateField, String[] productPromotionFields) {
        Coupon coupon = this.getById(updateField.getId());
        Assert.notNull(coupon, "优惠卷不存在");

        // isShow 是前台展示开关，不再同步为 promotion.is_available（活动启用开关）：
        // is_available=0 会让券整条退出 getAllAvailablePromotion，已发放的券也无法在下单时使用。
        // 展示过滤统一由 PromotionServiceImpl 按 isShow 判定（CR-20260731-173400）。
        return super.updateField(updateField, productPromotionFields);
    }

    /**
     * 数据判断
     *
     * @param dto    dto
     * @param coupon coupon
     */
    public void dataJudge(CouponDTO dto, Coupon coupon) {
        if ((StrUtil.isNotBlank(dto.getUseStartDate()) && !"0".equals(dto.getUseStartDate())) && (StrUtil.isNotBlank(dto.getUseEndDate()) && !"0".equals(dto.getUseEndDate()))) {
            coupon.setUseStartDate(StringUtils.dateToTimestampExample(dto.getUseStartDate()));
            coupon.setUseEndDate(StringUtils.dateToTimestampExample(dto.getUseEndDate()));
        }
        if (dto.getSendType() == 0) {
            dto.setUseStartDate("0");
            dto.setUseEndDate("0");
        } else {
            dto.setDelayDay(0);
            dto.setUseDay(0);
        }
        Integer reduceType = dto.getReduceType();
        if (reduceType != null && reduceType == 2) {
            dto.setMinOrderAmount(BigDecimal.valueOf(0));
        }
        if (reduceType != null && reduceType == 1 && dto.getCouponType() != null && dto.getCouponType() == 1) {
            if (dto.getMinOrderAmount() != null && dto.getCouponMoney() != null
                    && dto.getMinOrderAmount().compareTo(dto.getCouponMoney()) <= 0) {
                throw new GlobalException(CouponConstants.COUPON_MONEY_EXCEED_MIN_ORDER_AMOUNT);
            }
        }

        Integer shopId = getShopId();
        shopId = shopId == null ? 0 : shopId;
        coupon.setShopId(shopId);

        if (dto.getCouponType() == 1) {
            dto.setCouponDiscount(BigDecimal.valueOf(0));
        }
        // 数组转json
        if (dto.getSendRangeData() == null) {
            coupon.setSendRangeData("[]");
        } else {
            coupon.setSendRangeData(dto.getSendRangeData().toString());
        }
        if (dto.getLimitUserRank() == null) {
            coupon.setLimitUserRank("[]");
        } else {
            coupon.setLimitUserRank(dto.getLimitUserRank().toString());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean create(CouponDTO createDTO) {
        if (createDTO != null) {
            Coupon coupon = new Coupon();
            dataJudge(createDTO, coupon);
            // 当前时间
            coupon.setAddTime(StringUtils.getCurrentTime());
            BeanUtils.copyProperties(createDTO, coupon);
            boolean isSave = this.save(coupon);
            if (isSave) {
                // 营销活动写入
                savePromotion(coupon, createDTO.getMemberRange(), createDTO.getMemberMarks(),
                        memberAudienceService.resolveWriteScope(createDTO.getSendUserTarget(), createDTO.getMemberAudience()));
                adminLogService.createByLogInfo(StrUtil.format("新增优惠券:{}", coupon.getCouponName()));
            }
            return isSave;
        }
        return false;
    }

    /**
     * 营销活动写入
     *
     * @param coupon coupon
     * @param memberRange 适用会员范围
     * @param memberMarks 选择的会员标记列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void savePromotion(Coupon coupon, Integer memberRange, List<Integer> memberMarks, String memberScope) {
        Integer relationId = coupon.getCouponId();
        relationId = relationId == null ? 0 : relationId;
        Long startTime = 0L;
        Long endTime = 0L;
        if (coupon.getSendType() > 0) {
            startTime = coupon.getUseStartDate();
            endTime = coupon.getUseEndDate();
        }
        String memberMarksStr = memberMarks != null ? memberMarks.toString() : null;
        // 根据 relationId 查询是否有营销活动
        if (promotionService.count(new LambdaQueryWrapper<Promotion>()
                .eq(Promotion::getRelationId, relationId)
                .eq(Promotion::getType, PromotionType.TYPE_COUPON.getCode())) > 0) {
            // 更新部分字段
            promotionService.update(new LambdaUpdateWrapper<Promotion>()
                    .eq(Promotion::getRelationId, relationId)
                    .eq(Promotion::getType, PromotionType.TYPE_COUPON.getCode())
                    .set(Promotion::getStartTime, startTime)
                    .set(Promotion::getEndTime, endTime)
                    .set(Promotion::getPromotionName, coupon.getCouponName())
                    // 券的活动行恒为启用；「是否展示」由 isShow 在展示侧判定，不压到此字段上
                    .set(Promotion::getIsAvailable, Constants.YES)
                    .set(Promotion::getRange, coupon.getSendRange())
                    .set(Promotion::getRangeData, coupon.getSendRangeData())
                    .set(Promotion::getMemberRange, memberRange)
                    .set(Promotion::getMemberMarks, memberMarksStr)
                    .set(Promotion::getMemberScope, memberScope));
        } else {
            Promotion promotion = new Promotion();
            Integer shopId = getShopId();
            shopId = shopId == null ? 0 : shopId;
            promotion.setPromotionName(coupon.getCouponName());
            promotion.setStartTime(startTime);
            promotion.setEndTime(endTime);
            promotion.setType(PromotionType.TYPE_COUPON.getCode());
            promotion.setShopId(shopId);
            promotion.setRelationId(coupon.getCouponId());
            promotion.setRange(coupon.getSendRange());
            promotion.setRangeData(coupon.getSendRangeData());
            promotion.setIsAvailable(Constants.YES);
            promotion.setMemberRange(memberRange);
            promotion.setMemberMarks(memberMarksStr);
            promotion.setMemberScope(memberScope);

            boolean isSuccess = promotionService.save(promotion);
            if (!isSuccess) {
                throw new GlobalException(SERVICE_ERROR);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(CouponDTO updateDTO) {
        Coupon coupon = new Coupon();
        dataJudge(updateDTO, coupon);
        BeanUtils.copyProperties(updateDTO, coupon);
        boolean isSuccess = this.updateById(coupon);

        if (isSuccess) {
            savePromotion(coupon, updateDTO.getMemberRange(), updateDTO.getMemberMarks(),
                    memberAudienceService.resolveWriteScope(updateDTO.getSendUserTarget(), updateDTO.getMemberAudience()));
            adminLogService.createByLogInfo(StrUtil.format("修改优惠券:{}", coupon.getCouponName()));
        }
    }

    /**
     * 删除
     *
     * @param id id
     * @return boolean
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean del(Integer id) {
        if (id == null) {
            return false;
        }
        return softDel(id);
    }

    /**
     * 软删除
     *
     * @param id id
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean softDel(Integer id) {
        boolean update = this.lambdaUpdate()
                .set(Coupon::getIsDelete, 1)
                .eq(Coupon::getCouponId, id)
                .update();
        if (update) {
            // 软删除
            promotionService.update(new LambdaUpdateWrapper<Promotion>()
                    .eq(Promotion::getRelationId, id)
                    .eq(Promotion::getType, PromotionType.TYPE_COUPON.getCode())
                    .set(Promotion::getIsDelete, 1));
        }
        return update;
    }

    /**
     * 批量操作
     *
     * @param batchDTO 批量操作参数
     * @return boolean
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batch(BatchDTO batchDTO) {
        if ("del".equals(batchDTO.getType())) {
            List<Integer> ids = batchDTO.getIds();
            // 批量软删除
            boolean isMove = this.update(new LambdaUpdateWrapper<Coupon>()
                    .in(Coupon::getCouponId, ids)
                    .set(Coupon::getIsDelete, 1));
            if (isMove) {
                // 批量软删除
                promotionService.update(new LambdaUpdateWrapper<Promotion>()
                        .in(Promotion::getRelationId, ids)
                        .eq(Promotion::getType, PromotionType.TYPE_COUPON.getCode())
                        .set(Promotion::getIsDelete, 1));
            }
            return isMove;
        }
        return false;
    }

    /**
     * 配置项
     *
     * @return PromotionConfigVO
     */
    @Override
    public List<UserRank> config() {
        List<UserRank> ranks = userRankService.list();
        if (ranks == null || ranks.isEmpty()) {
            return ranks;
        }
        // 统计每个等级的会员数量（正式会员 user_type=0，含正常与禁用；按 rank_id 分组）
        List<Map<String, Object>> rows = userMapper.selectMaps(Wrappers.<User>query()
                .select("rank_id", "COUNT(*) AS cnt")
                .eq("user_type", 0)
                .groupBy("rank_id"));
        Map<Integer, Long> countMap = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Object rid = row.get("rank_id");
            if (rid == null) {
                continue;
            }
            Object cnt = row.get("cnt");
            countMap.put(((Number) rid).intValue(), cnt == null ? 0L : ((Number) cnt).longValue());
        }
        ranks.forEach(r -> r.setUserCount(countMap.getOrDefault(r.getRankId(), 0L)));
        return ranks;
    }

    @Override
    public Page<CouponClientVO> getList(CouponPageQuery pageQuery) {
        Page<Coupon> page = buildSortOrder(pageQuery);

        Integer userId = getCurrentUserId();
        boolean shopFlag = pageQuery.getShopId() != null && pageQuery.getShopId() != -1;
        boolean validDateFlag = pageQuery.getValidDate() != null && pageQuery.getValidDate() != -1;
        boolean receiveFlag = pageQuery.getReceiveFlag() != null && pageQuery.getReceiveFlag() == 1;

        // 获取需要排除的优惠券ID（当前用户领不到的券）
        Set<Integer> excludedCouponIds = getIneligibleCouponIds(userId);
        // 游客无法参与会员身份匹配：首页领券列表也不能下推明确限定会员等级、会员标记或人群的券。
        if (userId == null || userId <= 0) {
            excludedCouponIds = getGuestRestrictedCouponIdsForHome();
        }

        Page<Coupon> couponPage = this.lambdaQuery()
                .eq(Coupon::getIsDelete, 0)
                .eq(Coupon::getIsShow, pageQuery.getIsShow())
                .eq(receiveFlag && shopFlag, Coupon::getShopId, pageQuery.getShopId())
                .notIn(CollUtil.isNotEmpty(excludedCouponIds), Coupon::getCouponId, excludedCouponIds)
                .and(receiveFlag && validDateFlag, query ->
                        query.and(query1 ->
                                        query1.ge(Coupon::getUseEndDate, StringUtils.getCurrentTime()))
                                .or()
                                .eq(Coupon::getSendType, 0)
                )
                .orderByDesc(receiveFlag, Coupon::getAddTime)
                .page(page);

        List<Coupon> coupons = couponPage.getRecords();
        if (CollUtil.isEmpty(coupons)) {
            return PageUtil.convertPage(couponPage, Collections.emptyList());
        }

        List<Integer> couponIds = coupons.stream().map(Coupon::getCouponId).toList();
        List<UserCoupon> userCoupons = userCouponMapper.selectList(
                Wrappers.lambdaQuery(UserCoupon.class).in(UserCoupon::getCouponId, couponIds).eq(UserCoupon::getUserId, userId)
        );


        List<CouponClientVO> resultCoupons = coupons.stream()
                .map(coupon -> new CouponClientVO(coupon, userCoupons))
                .toList();
        return PageUtil.convertPage(couponPage, resultCoupons);
    }

    /**
     * 收集首页领券列表中游客不应看到的会员限定券。
     * <p>
     * 在分页查询前收集 ID，避免查询后再过滤造成分页总数和页码内容不一致。
     */
    private Set<Integer> getGuestRestrictedCouponIdsForHome() {
        List<Coupon> rankRestrictedCoupons = this.lambdaQuery()
                .select(Coupon::getCouponId, Coupon::getLimitUserRank)
                .eq(Coupon::getIsDelete, 0)
                .eq(Coupon::getIsNewUser, 2)
                .list();
        List<Promotion> memberRestrictedPromotions = promotionMapper.selectList(
                new LambdaQueryWrapper<Promotion>()
                        .select(Promotion::getRelationId, Promotion::getMemberMarks, Promotion::getMemberScope)
                        .eq(Promotion::getType, PromotionType.TYPE_COUPON.getCode())
                        .and(query -> query.eq(Promotion::getMemberRange, 1)
                                .or()
                                .apply("member_scope IS NOT NULL AND member_scope <> ''"))
        );
        return collectGuestRestrictedCouponIds(rankRestrictedCoupons, memberRestrictedPromotions);
    }

    Set<Integer> collectGuestRestrictedCouponIds(List<Coupon> coupons, List<Promotion> promotions) {
        Set<Integer> excludedCouponIds = new HashSet<>();
        Optional.ofNullable(coupons).orElse(List.of()).stream()
                .filter(coupon -> Objects.equals(2, coupon.getIsNewUser()))
                .filter(coupon -> StringUtils.isNotBlank(coupon.getLimitUserRank())
                        && !"[]".equals(coupon.getLimitUserRank()))
                .map(Coupon::getCouponId)
                .forEach(excludedCouponIds::add);
        Optional.ofNullable(promotions).orElse(List.of()).stream()
                .filter(promotion -> StringUtils.isNotBlank(promotion.getMemberScope())
                        || (StringUtils.isNotBlank(promotion.getMemberMarks())
                        && !"[]".equals(promotion.getMemberMarks())))
                .map(Promotion::getRelationId)
                .forEach(excludedCouponIds::add);

        return excludedCouponIds;
    }

    /**
     * 获取当前用户领不到的优惠券ID，用于展示侧下推排除，避免出现「列表能看到、点领取必报错」的券。
     * <p>
     * 判定口径与 {@code UserCouponServiceImpl.claim} 对齐，覆盖会员人群圈选、会员标记、
     * 新人专享、会员等级专享与发放总量已领完五项。
     * <p>
     * 两项刻意不纳入：未登录（userId 无效）时不做任何限制，人群与等级无从判定，交由领取环节校验；
     * 「每人限领已达上限」由 {@link CouponClientVO#getIsReceive()} 表达，用户需要能看到自己已领过的券。
     *
     * @param userId 用户ID
     * @return 需要排除的优惠券ID集合
     */
    private Set<Integer> getIneligibleCouponIds(Integer userId) {
        Set<Integer> excludedCouponIds = new HashSet<>();
        if (userId == null || userId <= 0) {
            return excludedCouponIds;
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            return excludedCouponIds;
        }

        collectAudienceMismatchedCouponIds(userId, user, excludedCouponIds);
        collectMemberRestrictedCouponIds(userId, user, excludedCouponIds);
        excludedCouponIds.addAll(userCouponMapper.selectSoldOutCouponIds());

        return excludedCouponIds;
    }

    /**
     * 收集会员人群不匹配的优惠券：{@code member_scope} 非空时按人群圈选判定，否则回退旧版会员标记。
     * 该优先级与 {@code UserCouponServiceImpl.claim} 一致。
     */
    private void collectAudienceMismatchedCouponIds(Integer userId, User user, Set<Integer> excludedCouponIds) {
        List<Promotion> promotions = promotionMapper.selectList(
                new LambdaQueryWrapper<Promotion>()
                        .eq(Promotion::getType, PromotionType.TYPE_COUPON.getCode())
                        .and(query -> query.eq(Promotion::getMemberRange, 1)
                                .or()
                                .apply("member_scope IS NOT NULL AND member_scope <> ''"))
        );

        for (Promotion promotion : promotions) {
            String memberScope = promotion.getMemberScope();
            if (StringUtils.isNotBlank(memberScope)) {
                if (!memberAudienceService.matchesScope(userId.longValue(), memberScope)) {
                    excludedCouponIds.add(promotion.getRelationId());
                }
            } else if (Objects.equals(1, promotion.getMemberRange())) {
                List<Integer> allowedMarks = Optional.ofNullable(JSON.parseArray(promotion.getMemberMarks(), Integer.class)).orElse(List.of());
                if (!allowedMarks.isEmpty() && !allowedMarks.contains(user.getMark())) {
                    excludedCouponIds.add(promotion.getRelationId());
                }
            }
        }
    }

    /**
     * 收集发放范围受限、当前用户不满足的优惠券：{@code is_new_user=1} 新人专享、{@code is_new_user=2} 会员等级专享。
     * {@code limit_user_rank} 为空或空数组时视为不限等级，与 {@code claim} 一致。
     */
    private void collectMemberRestrictedCouponIds(Integer userId, User user, Set<Integer> excludedCouponIds) {
        List<Coupon> restrictedCoupons = this.lambdaQuery()
                .select(Coupon::getCouponId, Coupon::getIsNewUser, Coupon::getLimitUserRank)
                .eq(Coupon::getIsDelete, 0)
                .in(Coupon::getIsNewUser, 1, 2)
                .list();
        if (CollUtil.isEmpty(restrictedCoupons)) {
            return;
        }

        // 是否新人只在确实存在新人专享券时才查一次订单
        Boolean isNewUser = null;

        for (Coupon coupon : restrictedCoupons) {
            if (Objects.equals(1, coupon.getIsNewUser())) {
                if (isNewUser == null) {
                    isNewUser = orderMapper.selectCount(Wrappers.lambdaQuery(Order.class)
                            .eq(Order::getUserId, userId)
                            .gt(Order::getPayStatus, 0)) == 0;
                }
                if (!isNewUser) {
                    excludedCouponIds.add(coupon.getCouponId());
                }
                continue;
            }

            String limitUserRank = coupon.getLimitUserRank();
            if (StringUtils.isBlank(limitUserRank) || "[]".equals(limitUserRank)) {
                continue;
            }
            List<Integer> rankIds = Optional.ofNullable(JSON.parseArray(limitUserRank, Integer.class)).orElse(List.of());
            if (!rankIds.isEmpty() && !rankIds.contains(user.getRankId())) {
                excludedCouponIds.add(coupon.getCouponId());
            }
        }
    }

    @Override
    public List<CouponDetailVO> getProductCouponList(int productId, int shopId) {
        LambdaQueryWrapper<Coupon> queryWrapper = new LambdaQueryWrapper<>();
        Long currentTime = StringUtils.getCurrentTime();
        
        Integer currentUserId = SecurityUtils.getCurrentUserId();
        Set<Integer> excludedCouponIds = currentUserId != null && currentUserId > 0
                ? getIneligibleCouponIds(currentUserId)
                : Set.of();
        if (CollUtil.isNotEmpty(excludedCouponIds)) {
            queryWrapper.notIn(Coupon::getCouponId, excludedCouponIds);
        }
        
        queryWrapper.eq(Coupon::getShopId, shopId)
                .eq(Coupon::getIsDelete, 0)
                .eq(Coupon::getIsShow, 1)
                .and(i -> i.and(j -> j.le(Coupon::getUseStartDate, currentTime)
                        .ge(Coupon::getUseEndDate, currentTime)).or().eq(Coupon::getSendType, 0));

        List<Coupon> coupons = this.list(queryWrapper);
        // 商品详情的游客视图只在当前候选券内排除明确限定会员的券，避免额外扫描全部优惠券。
        Set<Integer> guestRestrictedCouponIds = currentUserId == null || currentUserId <= 0
                ? getGuestRestrictedProductCouponIds(coupons)
                : Set.of();

        // 使用 filter 筛选符合条件的优惠券
        return coupons.stream()
                .filter(coupon -> !guestRestrictedCouponIds.contains(coupon.getCouponId()))
                .filter(coupon -> {
                    String sendRangeData = coupon.getSendRangeData();
                    List<Integer> sendRangeDataList = StringUtils.str2IntList(sendRangeData);
                    Integer sendRange = coupon.getSendRange();

                    // 过滤不符合条件的优惠券
                    return (sendRange != 3 || sendRangeDataList.contains(productId)) && (sendRange != 4 || !sendRangeDataList.contains(productId));
                })
                .map(coupon -> {
                    // 复制属性到 VO 对象
                    CouponDetailVO couponDetailVO = new CouponDetailVO();
                    BeanUtils.copyProperties(coupon, couponDetailVO);
                    couponDetailVO.setReceiveNum(getReceiveNumAttr(coupon.getCouponId()));
                    couponDetailVO.setUseStartDate(TigUtils.handelTime(coupon.getUseStartDate()));
                    couponDetailVO.setUseEndDate(TigUtils.handelTime(coupon.getUseEndDate()));
                    // limitUserRank 在 Coupon 中为 JSON 字符串，BeanUtils 无法转换到 VO 的 Integer[]，此处显式解析
                    String limitUserRank = coupon.getLimitUserRank();
                    if (limitUserRank != null && !limitUserRank.isBlank() && !"[]".equals(limitUserRank)) {
                        List<Integer> rankIds = JSON.parseArray(limitUserRank, Integer.class);
                        if (CollUtil.isNotEmpty(rankIds)) {
                            couponDetailVO.setLimitUserRank(rankIds.toArray(new Integer[0]));
                        }
                    }
                    return couponDetailVO;
                })
                .collect(Collectors.toList());
    }

    /**
     * 收集商品详情游客不应看到的会员限定券。
     * <p>
     * 游客无法判定具体会员身份，因此对明确限定会员等级、会员标记或人群圈选的券直接隐藏；
     * 新人专享券不在此列，游客登录后仍可能满足新人资格。
     */
    private Set<Integer> getGuestRestrictedProductCouponIds(List<Coupon> coupons) {
        if (CollUtil.isEmpty(coupons)) {
            return new HashSet<>();
        }

        List<Integer> couponIds = coupons.stream().map(Coupon::getCouponId).toList();
        List<Promotion> memberRestrictedPromotions = promotionMapper.selectList(
                new LambdaQueryWrapper<Promotion>()
                        .in(Promotion::getRelationId, couponIds)
                        .eq(Promotion::getType, PromotionType.TYPE_COUPON.getCode())
                        .and(query -> query.eq(Promotion::getMemberRange, 1)
                                .or()
                                .apply("member_scope IS NOT NULL AND member_scope <> ''"))
        );
        return collectGuestRestrictedCouponIds(coupons, memberRestrictedPromotions);
    }

    @Override
    public CartPromotionParsePriceDTO parsePrice(List<CartVO> cartList, PromotionVO promotionVO) {
        if (promotionVO.getIsReceive() == null || promotionVO.getIsReceive() != 1) {
            return null;
        }
        Coupon coupon = this.getById(promotionVO.getRelationId());
        // 计算原始总金额（使用productPrice，不使用已被其他优惠修改的subtotal）
        BigDecimal priceAmount = cartList.stream()
                .map(cart -> cart.getProductPrice().multiply(BigDecimal.valueOf(cart.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        //看是否满足优惠券要求
        if (coupon.getCouponUnit() == 1 && (coupon.getMinOrderAmount().compareTo(priceAmount) <= 0 || coupon.getReduceType() == 2)
                || coupon.getCouponUnit() == 2 && coupon.getMinOrderAmount().compareTo(BigDecimal.valueOf(cartList.stream().mapToInt(CartVO::getQuantity).sum())) <= 0
        ) {
            promotionVO.setAmount(BigDecimal.ZERO);
            if (coupon.getCouponType() == 1) {
                // 固定金额券：直接使用优惠券面值作为优惠金额
                promotionVO.setAmount(coupon.getCouponMoney());
            } else {
                // 折扣券：计算折扣优惠金额
                BigDecimal checkDiscountAmount = priceAmount.multiply(BigDecimal.valueOf(1 - coupon.getCouponDiscount().divide(BigDecimal.valueOf(10), 6, RoundingMode.HALF_UP).doubleValue()));
                // 如果设置了最大优惠金额且超过上限，则使用上限值
                if (coupon.getMaxOrderAmount().compareTo(BigDecimal.ZERO) > 0 && checkDiscountAmount.compareTo(coupon.getMaxOrderAmount()) > 0) {
                    checkDiscountAmount = coupon.getMaxOrderAmount();
                }
                // 保留6位小数
                checkDiscountAmount = checkDiscountAmount.setScale(6, RoundingMode.HALF_UP);
                promotionVO.setAmount(checkDiscountAmount);
            }
            // 按原始金额占比分摊优惠金额
            for (CartVO cartVO : cartList) {
                // 计算该商品的原始小计（使用productPrice，不受其他优惠影响）
                BigDecimal originalItemTotal = cartVO.getProductPrice().multiply(BigDecimal.valueOf(cartVO.getQuantity()));
                
                // 计算该商品分摊的优惠金额（基于原始小计占比）
                BigDecimal itemDiscountAmount = promotionVO.getAmount()
                        .multiply(originalItemTotal)
                        .divide(priceAmount, 2, RoundingMode.HALF_UP);
                
                // 将小计的优惠金额转换为单价的优惠：优惠金额 ÷ 数量
                BigDecimal priceDiscountAmount = itemDiscountAmount
                        .divide(BigDecimal.valueOf(cartVO.getQuantity()), 2, RoundingMode.HALF_UP);
                
                // 从当前单价中减去优惠（当前price可能已被其他优惠修改）
                BigDecimal newPrice = cartVO.getPrice().subtract(priceDiscountAmount);
                
                // 设置新的单价
                cartVO.setPrice(newPrice);
            }
            CartPromotionParsePriceDTO cartPromotionParam = new CartPromotionParsePriceDTO();
            cartPromotionParam.setCartList(cartList);
            cartPromotionParam.setPromotion(promotionVO);
            return cartPromotionParam;
        }
        return null;
    }

    @Override
    public String getCouponPromotionDesc(Coupon coupon) {
        // UI 文案走 MessageSource（满/减/打/折/立减）；数值预格式化为 String 传入，避免 MessageFormat 重格式化数字
        if (coupon.getReduceType() == 1) {
            if (coupon.getCouponType() == 1) {
                return messageHelper.get("coupon.full.reduce",
                        String.valueOf(coupon.getMinOrderAmount()), String.valueOf(coupon.getCouponMoney()));
            } else {
                return messageHelper.get("coupon.full.discount",
                        String.valueOf(coupon.getMinOrderAmount()), String.valueOf(coupon.getCouponDiscount()));
            }
        } else {
            if (coupon.getCouponType() == 1) {
                return messageHelper.get("coupon.straight.reduce", String.valueOf(coupon.getCouponMoney()));
            } else {
                return messageHelper.get("coupon.discount", String.valueOf(coupon.getCouponDiscount()));
            }
        }
    }

    /**
     * 获取优惠券领取数量
     *
     * @param couponId 优惠券ID
     * @return long
     */
    public long getReceiveNumAttr(Integer couponId) {
        if (couponId != null) {
            LambdaQueryWrapper<UserCoupon> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserCoupon::getCouponId, couponId)
                    .eq(UserCoupon::getUserId, SecurityUtils.getCurrentUserId());
            return userCouponMapper.selectCount(queryWrapper);
        }
        return 0;
    }

    @Override
    public Page<CouponVO> getValidList(CouponPageQuery query) {
        long currentTime = StringUtils.getCurrentTime();

        // 分页
        Page<Coupon> pageParam = new Page<>(query.getPage(), query.getSize());

        // 构造查询构造器
        LambdaQueryWrapper<Coupon> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Coupon::getShopId, 0)
                .eq(Coupon::getIsDelete, 0)
                .le(Coupon::getUseStartDate, currentTime)
                .ge(Coupon::getUseEndDate, currentTime)
                .orderByDesc(Coupon::getCouponId);

        // 搜索关键字
        String keyword = query.getKeyword();
        if (StrUtil.isNotEmpty(keyword)) {
            queryWrapper.like(Coupon::getCouponName, keyword);
        }

        // 执行查询
        Page<Coupon> couponPage = this.page(pageParam, queryWrapper);

        // 转换为VO
        List<CouponVO> couponVOList = couponPage.getRecords().stream()
                .map(coupon -> {
                    CouponVO couponVO = new CouponVO();
                    BeanUtils.copyProperties(coupon, couponVO);
                    return couponVO;
                }).toList();

        return PageUtil.convertPage(couponPage, couponVOList);
    }
}
