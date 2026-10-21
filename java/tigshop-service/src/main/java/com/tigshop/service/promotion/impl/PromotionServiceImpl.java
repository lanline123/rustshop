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

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.promotion.PromotionCreateDTO;
import com.tigshop.bean.dto.promotion.PromotionListDTO;
import com.tigshop.bean.dto.promotion.PromotionUpdateDTO;
import com.tigshop.bean.dto.user.SendUserTargetDTO;
import com.tigshop.bean.enums.promotion.PromotionType;
import com.tigshop.bean.model.promotion.Coupon;
import com.tigshop.bean.model.promotion.Promotion;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.model.user.UserCoupon;
import com.tigshop.bean.vo.promotion.PromotionCountVO;
import com.tigshop.bean.vo.promotion.PromotionVO;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.*;
import com.tigshop.mapper.promotion.CouponMapper;
import com.tigshop.mapper.promotion.PromotionMapper;
import com.tigshop.mapper.user.UserCouponMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.promotion.PromotionService;
import com.tigshop.service.user.UserService;
import com.tigshop.service.user.MemberAudienceService;
import com.tigshop.service.user.UserMemberTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 营销管理服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
@RequiredArgsConstructor
public class PromotionServiceImpl extends BaseServiceImpl<PromotionMapper, Promotion> implements PromotionService {
    /**
     * 存量 memberRange/memberMarks 活动「展示 + canJoin=false」的类型白名单（CR-20260730-1745）。
     * 优惠券由券列表按领取资格独立过滤（CR-20260730-1655），拼团与抽奖维持原隐藏行为，均不列入。
     * 满赠与限时折扣不列入：不在人群内时整条隐藏，前者避免展示拿不到的赠品，后者让现有前端按普通价购买。
     */
    private static final Set<Integer> LEGACY_MARK_VISIBLE_TYPES = Set.of(
            PromotionType.TYPE_SECKILL.getCode(),
            PromotionType.TYPE_REDUCE.getCode(),
            PromotionType.TYPE_DISCOUNT.getCode());

    private static final Set<Integer> AUDIENCE_MISMATCH_HIDDEN_TYPES = Set.of(
            PromotionType.TYPE_GIVE.getCode(),
            PromotionType.TYPE_LIMIT_DISCOUNT.getCode());

    private static final String MEMBER_RESTRICT_TIP = "该活动仅限指定会员参与";

    private final CouponMapper couponMapper;


    private final ObjectProvider<UserService> userService;
    private final UserCouponMapper userCouponMapper;
    private final MemberAudienceService memberAudienceService;
    private final UserMemberTagService userMemberTagService;

    @Override
    public Page<PromotionVO> list(PromotionListDTO listDTO) {
        // 分页
        Page<Promotion> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<Promotion> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 搜索关键字
        String keyword = listDTO.getKeyword();
        if (StrUtil.isNotEmpty(keyword)) {
            queryWrapper.like(Promotion::getPromotionName, keyword);
        }

        Integer shopId = getShopId();
        if (shopId != null) {
            queryWrapper.eq(Promotion::getShopId, shopId);
        }

        String type = listDTO.getType();
        if (StrUtil.isNotEmpty(type)) {
            // 字符串转数组
            List<Integer> typeList = StringUtils.str2IntList(type);
            queryWrapper.in(Promotion::getType, typeList);
        }

        Integer timeType = listDTO.getTimeType();
        if (timeType != null && timeType > 0) {
            long currentTime = StringUtils.getCurrentTime();
            if (timeType == 1) {
                // 进行中
                queryWrapper.and(i -> i.and(j -> j.le(Promotion::getStartTime, currentTime).ge(Promotion::getEndTime, currentTime))
                        .or(j -> j.eq(Promotion::getStartTime, 0).eq(Promotion::getEndTime, 0)));
            } else if (timeType == 2) {
                // 7天内到期
                queryWrapper.le(Promotion::getStartTime, currentTime).le(Promotion::getEndTime, currentTime + 7 * 24 * 60 * 60).ge(Promotion::getEndTime, currentTime).ne(Promotion::getEndTime, 0);
            } else if (timeType == 3) {
                // 未开始
                queryWrapper.gt(Promotion::getStartTime, currentTime);
            }
        }

        queryWrapper.eq(Promotion::getIsDelete, 0);
        queryWrapper.eq(Promotion::getIsAvailable, 1);

        // 执行查询
        Page<Promotion> timeDiscountPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<Promotion> timeDiscountPageRecords = timeDiscountPage.getRecords();
        // 转换为VO
        List<PromotionVO> timeDiscountVOList = timeDiscountPageRecords.stream()
                .map(this::convert).toList();
        return PageUtil.convertPage(timeDiscountPage, timeDiscountVOList);
    }

    /**
     * 格式转换
     *
     * @param promotion promotion
     */
    public PromotionVO convert(Promotion promotion) {
        boolean couponPromotion = Objects.equals(promotion.getType(), PromotionType.TYPE_COUPON.getCode())
                && promotion.getRelationId() != null;
        return convert(promotion, couponPromotion ? couponMapper.selectById(promotion.getRelationId()) : null);
    }

    /**
     * 券行由调用方传入的转换重载：促销总线整批装配时券已一次性取回，避免这里按活动逐条回查。
     *
     * @param coupon 该活动关联的券；非券活动或券已删除时传 null，时间文案回退到活动自身起止时间
     */
    private PromotionVO convert(Promotion promotion, Coupon coupon) {
        PromotionVO promotionVO = new PromotionVO();
        BeanUtils.copyProperties(promotion, promotionVO);
        // 时间转换
        Long startTime = promotion.getStartTime();
        if (startTime > 0) {
            DateTime startDate = DateUtil.date(startTime * 1000);
            promotionVO.setStartTime(DateUtil.format(startDate, "yyyy-MM-dd HH:mm:ss"));
        } else {
            promotionVO.setStartTime("0");
        }

        Long endTime = promotion.getEndTime();
        if (endTime > 0) {
            DateTime endDate = DateUtil.date(endTime * 1000);
            promotionVO.setEndTime(DateUtil.format(endDate, "yyyy-MM-dd HH:mm:ss"));
        } else {
            promotionVO.setEndTime("0");
        }
        // 优惠券：领券后生效时展示相对有效期文案，而非「长期有效」
        boolean relativeCouponValidity = Objects.equals(promotion.getType(), PromotionType.TYPE_COUPON.getCode())
                && promotion.getRelationId() != null
                && coupon != null
                && Objects.equals(coupon.getSendType(), 0);
        if (relativeCouponValidity) {
            int delayDay = coupon.getDelayDay() == null ? 0 : coupon.getDelayDay();
            int useDay = coupon.getUseDay() == null ? 0 : coupon.getUseDay();
            if (delayDay <= 0) {
                promotionVO.setTimeText("领券后立即生效，有效期" + useDay + "天");
            } else {
                promotionVO.setTimeText("领券后" + delayDay + "天生效，有效期" + useDay + "天");
            }
        } else if (promotion.getStartTime() == 0 && promotion.getEndTime() == 0) {
            promotionVO.setTimeText("长期有效");
        } else {
            promotionVO.setTimeText(TigUtils.handelTime(promotion.getStartTime()) + " 至 " +
                    TigUtils.handelTime(promotion.getEndTime()));
        }

        // json 转换
        promotionVO.setRangeData(StringUtils.str2IntList(promotion.getRangeData()));
        if (promotion.getSkuIds() != null) {
            promotionVO.setSkuIds(StringUtils.str2IntList(promotion.getSkuIds()));
        }

        // 显示类型
        promotionVO.setTypeText(PromotionType.getStatusName(promotion.getType()));

        // 适用会员范围
        promotionVO.setMemberRange(promotion.getMemberRange());
        if (promotion.getMemberMarks() != null) {
            promotionVO.setMemberMarks(StringUtils.str2IntList(promotion.getMemberMarks()));
        }
        // convert 走列表与前台促销总线，逐条调用；此处只回结构本身，
        // 指定会员明细（users）需批量查库，仅在各活动 detail 接口回填
        com.tigshop.bean.dto.user.SendUserTargetDTO sendUserTarget =
                memberAudienceService.parseScope(promotion.getMemberScope());
        promotionVO.setSendUserTarget(sendUserTarget);
        promotionVO.setMemberAudience(memberAudienceService.toLegacy(sendUserTarget));
        return promotionVO;
    }

    @Override
    public boolean create(PromotionCreateDTO createDTO) {
        if (createDTO != null) {
            Promotion promotion = new Promotion();

            Integer shopId = getShopId();
            promotion.setShopId(shopId);

            // 数组转json
            promotion.setRangeData(createDTO.getRangeData().toString());
            if (createDTO.getSkuIds() != null) {
                promotion.setSkuIds(createDTO.getSkuIds().toString());
            }
            // 适用会员范围
            if (createDTO.getMemberMarks() != null) {
                promotion.setMemberMarks(createDTO.getMemberMarks().toString());
            }

            BeanUtils.copyProperties(createDTO, promotion);
            // 会员人群圈选（copyProperties 不覆盖该字段，需手动序列化）
            promotion.setMemberScope(
                    memberAudienceService.resolveWriteScope(createDTO.getSendUserTarget(), createDTO.getMemberAudience()));
            return this.save(promotion);
        }
        return false;
    }

    @Override
    public boolean update(PromotionUpdateDTO updateDTO) {
        if (updateDTO != null) {
            Promotion promotion = new Promotion();

            // 数组转json
            promotion.setRangeData(updateDTO.getRangeData().toString());
            if (updateDTO.getSkuIds() != null) {
                promotion.setSkuIds(updateDTO.getSkuIds().toString());
            }
            // 适用会员范围
            if (updateDTO.getMemberMarks() != null) {
                promotion.setMemberMarks(updateDTO.getMemberMarks().toString());
            }

            BeanUtils.copyProperties(updateDTO, promotion);
            // 会员人群圈选
            promotion.setMemberScope(
                    memberAudienceService.resolveWriteScope(updateDTO.getSendUserTarget(), updateDTO.getMemberAudience()));
            return this.updateById(promotion);
        }
        return false;
    }

    @Override
    public PromotionCountVO getCount() {
        Integer shopId = getShopId();

        LambdaQueryWrapper<Promotion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Promotion::getIsAvailable, 1)
                .eq(Promotion::getIsDelete, 0)
                .eq(Promotion::getShopId, shopId)
                .and(i -> i.and(
                                        j -> j.le(Promotion::getStartTime, StringUtils.getCurrentTime())
                                                .ge(Promotion::getEndTime, StringUtils.getCurrentTime())
                                ).or(k -> k.eq(Promotion::getEndTime, 0))
                                .or(l -> l.eq(Promotion::getStartTime, 0))
                );
        List<Promotion> countOne = this.list(queryWrapper);

        LambdaQueryWrapper<Promotion> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(Promotion::getIsAvailable, 1)
                .eq(Promotion::getIsDelete, 0)
                .eq(Promotion::getShopId, shopId)
                .and(i -> i.and(
                                j -> j.le(Promotion::getStartTime, StringUtils.getCurrentTime())
                                        .ge(Promotion::getEndTime, StringUtils.getCurrentTime())
                        ).and(k -> k.le(Promotion::getEndTime, StringUtils.getCurrentTime() + 7 * 24 * 60 * 60))
                        .and(l -> l.ne(Promotion::getEndTime, 0)));

        List<Promotion> countTwo = this.list(queryWrapper1);

        LambdaQueryWrapper<Promotion> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.eq(Promotion::getIsAvailable, 1)
                .eq(Promotion::getIsDelete, 0)
                .eq(Promotion::getShopId, shopId)
                .and(i -> i.and(
                        j -> j.ge(Promotion::getStartTime, StringUtils.getCurrentTime())
                ));

        List<Promotion> countThree = this.list(queryWrapper2);
        return PromotionCountVO.builder()
                .timeType1Count(countOne.size())
                .timeType2Count(countTwo.size())
                .timeType3Count(countThree.size())
                .build();
    }

    /**
     * 获取所有可用的促销活动
     *
     * @param shopId shopId
     * @return List<Promotion>
     */
    @Override
    public List<PromotionVO> getAllAvailablePromotion(Integer shopId, Integer userId) {
        // shopId 为 null 表示「不限店铺、取全平台活动」，与按 shop_id IN 过滤语义不同，
        // 因此不能并入 getAllAvailablePromotionByShopIds，只与它共用下面的装配逻辑。
        List<Promotion> promotions = queryAvailablePromotion(
                shopId == null ? null : List.of(shopId), StringUtils.getCurrentTime());
        return assembleAvailablePromotion(promotions, userId);
    }

    @Override
    public Map<Integer, List<PromotionVO>> getAllAvailablePromotionByShopIds(Collection<Integer> shopIds, Integer userId) {
        if (shopIds == null || shopIds.isEmpty()) {
            return Map.of();
        }
        List<Integer> targetShopIds = shopIds.stream().filter(Objects::nonNull).distinct().toList();
        if (targetShopIds.isEmpty()) {
            return Map.of();
        }
        Map<Integer, List<PromotionVO>> result = new LinkedHashMap<>();
        // 无活动的店铺也要有条目，调用方才能区分「该店没有活动」与「没预取到」
        targetShopIds.forEach(shopId -> result.put(shopId, new ArrayList<>()));
        List<Promotion> promotions = queryAvailablePromotion(targetShopIds, StringUtils.getCurrentTime());
        // 装配后再按店铺归位，组内顺序仍是查询的 FIELD(type) 顺序
        for (PromotionVO vo : assembleAvailablePromotion(promotions, userId)) {
            result.computeIfAbsent(vo.getShopId(), k -> new ArrayList<>()).add(vo);
        }
        return result;
    }

    /**
     * 取进行中的可用活动。
     *
     * @param shopIds 需要过滤的店铺；null 表示不按店铺过滤（全平台活动）
     */
    private List<Promotion> queryAvailablePromotion(Collection<Integer> shopIds, Long currentTime) {
        return this.lambdaQuery()
                // 优惠券不受 is_available 约束：该字段是活动启用开关，历史实现曾把 coupon.is_show
                // （前台展示开关）同步写入，使「不可见」的券连已发放的持有者也无法在下单时使用。
                // 券的展示与可用改由下方 isShow + 持有判定统一裁决（CR-20260731-173400）。
                .and(q -> q.eq(Promotion::getIsAvailable, Constants.YES)
                        .or(o -> o.eq(Promotion::getType, PromotionType.TYPE_COUPON.getCode()))
                )
                .in(shopIds != null, Promotion::getShopId, shopIds == null ? List.of() : shopIds)
                .and(q -> q.le(Promotion::getStartTime, currentTime).ge(Promotion::getEndTime, currentTime)
                        .or(query -> query.eq(Promotion::getStartTime, 0).eq(Promotion::getEndTime, 0))
                )
                //用标识的放在前面,因为如果标识是list的话,6之后的活动(限时秒杀),后续不参与前端活动标记了
                .last("order by FIELD(`type`, 1,7,6,2,3,4,5,8)")
                .list();
    }

    /**
     * 活动 → 前台 VO 的整批装配。
     * <p>
     * 券行、会员资料、用户标签、券持有关系都在进入循环前一次性备好，逐个活动只做内存判定，
     * 避免「店铺活动越多、SQL 越多」的 N+1（每张券曾查 2 次、每个人群活动曾查 1~2 次会员）。
     */
    private List<PromotionVO> assembleAvailablePromotion(List<Promotion> promotions, Integer userId) {
        if (promotions.isEmpty()) {
            return new ArrayList<>();
        }
        Long currentTime = StringUtils.getCurrentTime();
        final Integer finalUserId = Optional.ofNullable(userId).orElse(SecurityUtils.getCurrentUserId());
        boolean adminCtx = SecurityUtils.isAdminUser();
        // 未登录（getCurrentUserId 未登录恒返回 0）无法做会员匹配；显式标签/指定会员活动由下方单独禁止活动价，普通活动仍放行
        boolean loggedIn = finalUserId != null && finalUserId > 0;

        Map<Integer, Coupon> couponMap = loadCoupons(promotions);
        Set<Integer> heldCouponIds = loadHeldCouponIds(couponMap.values(), finalUserId, currentTime, loggedIn);
        //如果从后台获取的话,就不进行用户标识过滤
        User user = adminCtx ? null : userService.getIfAvailable().getById(finalUserId);
        Integer mark = Optional.ofNullable(user).map(User::getMark).orElse(null);
        Set<Long> userTagIds = loadUserTagIds(promotions, finalUserId, adminCtx, loggedIn);

        // 二期（D1096）：「新建 member_scope 人群活动」通常返回 + 标记 canJoin/restrictTip（计价侧按 from 排除）。
        // CR-20260730-1745：「存量 memberRange/memberMarks 指定标记会员」活动对白名单类型同样改为返回 + 标记，
        // 使前台能「展示活动、置灰按钮」；白名单外类型维持原隐藏行为。
        // CR-20260731-173400：优惠券 isShow=0 由「整条隐藏」改为「未持有者隐藏、持有者放行」。
        // 满赠（type=5）与限时折扣（type=6）例外：不匹配时整条隐藏；后者让现有前端按普通商品购买。
        List<PromotionVO> result = new ArrayList<>();
        for (Promotion promotion : promotions) {
            Coupon couponEntity = null;
            if (Objects.equals(promotion.getType(), PromotionType.TYPE_COUPON.getCode())) {
                couponEntity = promotion.getRelationId() == null ? null : couponMap.get(promotion.getRelationId());
                //判断优惠券是否展示
                if (couponEntity != null && Objects.equals(couponEntity.getIsShow(), 0)) {
                    // isShow=0 只表示「前台不展示、不可领取」，不代表活动停用。已持有该券的用户必须放行，
                    // 否则券整条退出可用促销集合，手动发放 / 抽奖得到的券在下单时无法使用（CR-20260731-173400）。
                    if (!loggedIn || !heldCouponIds.contains(couponEntity.getCouponId())) {
                        continue;
                    }
                }
            }
            PromotionVO vo = convert(promotion, couponEntity);
            String memberScope = promotion.getMemberScope();
            if (!adminCtx && StringUtils.isNotBlank(memberScope)) {
                // 二期新建活动：通常展示 + canJoin/restrictTip；满赠/限时折扣按下方兼容策略隐藏。
                // 游客无法满足显式指定会员/会员标签人群，必须禁止活动价；普通活动仍按 canJoin=true 处理。
                boolean canJoin = loggedIn
                        ? matchAudienceScope(vo.getSendUserTarget(), finalUserId, user, userTagIds)
                        : !isGuestRestrictedAudience(vo.getSendUserTarget());
                vo.setCanJoin(canJoin);
                if (!canJoin) {
                    // 满赠：避免展示拿不到的赠品；限时折扣：不符合人群时按普通商品购买，兼容现有前端的禁购逻辑。
                    // 未登录时必须继续展示，动作接口再按登录后的实际用户口径重新计价。
                    if (shouldHideWhenAudienceMismatch(promotion, loggedIn)) {
                        continue;
                    }
                    vo.setRestrictTip(MEMBER_RESTRICT_TIP);
                }
            } else if (!adminCtx && Objects.equals(1, promotion.getMemberRange()) && loggedIn) {
                List<Integer> allowedMarks = Optional.ofNullable(JSON.parseArray(promotion.getMemberMarks(), Integer.class)).orElse(List.of());
                // 已登录但未设标记，同样不在名单内
                boolean matched = mark != null && allowedMarks.contains(mark);
                if (matched) {
                    vo.setCanJoin(true);
                } else if (shouldHideWhenAudienceMismatch(promotion, loggedIn)) {
                    // 满赠/限时折扣：不在名单内（含已登录未设标记）整条隐藏，与上面 member_scope 分支同一口径
                    continue;
                } else if (LEGACY_MARK_VISIBLE_TYPES.contains(promotion.getType())) {
                    vo.setCanJoin(false);
                    vo.setRestrictTip(MEMBER_RESTRICT_TIP);
                } else if (mark != null) {
                    // 券/拼团/抽奖：维持改造前「有标记但不匹配则隐藏」
                    continue;
                } else {
                    vo.setCanJoin(true);
                }
            } else {
                vo.setCanJoin(true);
            }
            result.add(vo);
        }
        return result;
    }

    static boolean shouldHideWhenAudienceMismatch(Promotion promotion, boolean loggedIn) {
        return loggedIn && AUDIENCE_MISMATCH_HIDDEN_TYPES.contains(promotion.getType());
    }

    /**
     * 游客对显式「指定会员/会员标签」人群没有可验证的参与资格。
     * scope=1 且仅配置会员等级时沿用原有游客展示口径，避免扩大本次修复范围。
     */
    private boolean isGuestRestrictedAudience(SendUserTargetDTO target) {
        if (target == null || target.scopeOrDefault() != SendUserTargetDTO.SCOPE_PART) {
            return false;
        }
        return (target.getUserIds() != null && !target.getUserIds().isEmpty())
                || (target.getTagIds() != null && !target.getTagIds().isEmpty());
    }

    /**
     * 整批取回券活动关联的券行。convert 的有效期文案与 isShow 展示判定都要用，
     * 逐条 selectById 会让每张券查两次。
     */
    private Map<Integer, Coupon> loadCoupons(List<Promotion> promotions) {
        List<Integer> couponIds = promotions.stream()
                .filter(p -> Objects.equals(p.getType(), PromotionType.TYPE_COUPON.getCode()))
                .map(Promotion::getRelationId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (couponIds.isEmpty()) {
            return Map.of();
        }
        return couponMapper.selectBatchIds(couponIds).stream()
                .collect(Collectors.toMap(Coupon::getCouponId, Function.identity(), (a, b) -> a));
    }

    /**
     * 当前用户持有且仍可用（未绑单、未使用、在有效期内）的券，一次查完 isShow=0 的全部候选券。
     * 判定口径与下单页枚举候选券的 {@code UserCouponMapper.listUserCouponList} 保持一致，
     * 避免出现「券在下单页列出、却不在可用促销集合里」的错位。
     */
    private Set<Integer> loadHeldCouponIds(Collection<Coupon> coupons, Integer userId, Long currentTime, boolean loggedIn) {
        if (!loggedIn) {
            return Set.of();
        }
        // 只有 isShow=0 的券需要按「是否持有」放行，展示中的券无需判定持有关系
        List<Integer> hiddenCouponIds = coupons.stream()
                .filter(c -> Objects.equals(c.getIsShow(), 0))
                .map(Coupon::getCouponId)
                .filter(Objects::nonNull)
                .toList();
        if (hiddenCouponIds.isEmpty()) {
            return Set.of();
        }
        return userCouponMapper.selectList(new LambdaQueryWrapper<UserCoupon>()
                        .select(UserCoupon::getCouponId)
                        .in(UserCoupon::getCouponId, hiddenCouponIds)
                        .eq(UserCoupon::getUserId, userId)
                        .eq(UserCoupon::getOrderId, 0)
                        .eq(UserCoupon::getUsedTime, 0)
                        .le(UserCoupon::getStartDate, currentTime)
                        .ge(UserCoupon::getEndDate, currentTime))
                .stream()
                .map(UserCoupon::getCouponId)
                .collect(Collectors.toSet());
    }

    /**
     * 整批取回用户标签，供各活动的 member_scope 人群判定共用。
     * 仅在确实存在人群活动时才查，没有人群活动的店铺不产生额外查询。
     */
    private Set<Long> loadUserTagIds(List<Promotion> promotions, Integer userId, boolean adminCtx, boolean loggedIn) {
        if (adminCtx || !loggedIn) {
            return Set.of();
        }
        boolean anyMemberScope = promotions.stream().anyMatch(p -> StringUtils.isNotBlank(p.getMemberScope()));
        return anyMemberScope ? userMemberTagService.listTagIdsByUser(userId.longValue()) : Set.of();
    }

    /**
     * 二期会员人群判定（D1096）：仅按新建活动的 member_scope（标签/等级人群）判定。
     * 未登录或人群为空 → 视为可参与（true）。存量 memberRange/memberMarks 不走此方法（保持原隐藏逻辑）。
     *
     * @param target     已解析的人群条件（取自 {@code PromotionVO#sendUserTarget}，整批只解析一次）
     * @param user       已加载的会员资料，整批共用
     * @param userTagIds 该用户持有的全部标签，整批共用
     */
    private boolean matchAudienceScope(SendUserTargetDTO target, Integer userId, User user, Set<Long> userTagIds) {
        // getCurrentUserId() 未登录返回 0 而非 null，0 继续走人群匹配会因查不到 user 恒判 false
        if (userId == null || userId <= 0) {
            return true;
        }
        return memberAudienceService.matches(user, userTagIds, target);
    }


    @Override
    public List<PromotionVO> getAllAvailablePromotion(Integer shopId) {
        return getAllAvailablePromotion(shopId, null);
    }
}
