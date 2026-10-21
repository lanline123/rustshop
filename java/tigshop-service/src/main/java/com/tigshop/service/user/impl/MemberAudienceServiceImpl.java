package com.tigshop.service.user.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.dto.user.MemberAudienceDTO;
import com.tigshop.bean.dto.user.SendUserTargetDTO;
import com.tigshop.bean.enums.user.UserTypeEnum;
import com.tigshop.bean.model.user.MemberTag;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.model.user.UserRank;
import com.tigshop.bean.vo.user.TargetUserVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.mapper.user.MemberTagMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.mapper.order.OrderMapper;
import com.tigshop.mapper.finance.UserRechargeOrderMapper;
import com.tigshop.service.user.MemberAudienceService;
import com.tigshop.service.user.UserMemberTagService;
import com.tigshop.service.user.UserRankService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 统一人群圈选服务实现
 *
 * @author Tigshop团队
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberAudienceServiceImpl implements MemberAudienceService {

    private final UserMapper userMapper;
    private final UserMemberTagService userMemberTagService;
    private final UserRankService userRankService;
    private final MemberTagMapper memberTagMapper;
    private final OrderMapper orderMapper;
    private final UserRechargeOrderMapper userRechargeOrderMapper;

    // ------------------------------------------------------------------
    // 判形 / 升维 / 降维
    // ------------------------------------------------------------------

    @Override
    public SendUserTargetDTO parseScope(String memberScopeJson) {
        if (StrUtil.isBlank(memberScopeJson)) {
            return null;
        }
        SendUserTargetDTO target = JsonUtil.fromJson(memberScopeJson, SendUserTargetDTO.class);
        if (target == null) {
            log.warn("人群圈选 member_scope 解析失败，回退旧人群逻辑: {}", memberScopeJson);
            return null;
        }
        // 有 scope 字段 = 当前结构；没有 = 历史 MemberAudienceDTO 结构，需升维
        if (target.getScope() != null) {
            return target;
        }
        return upgrade(JsonUtil.fromJson(memberScopeJson, MemberAudienceDTO.class));
    }

    @Override
    public SendUserTargetDTO upgrade(MemberAudienceDTO audience) {
        SendUserTargetDTO target = new SendUserTargetDTO();
        if (audience == null || audience.typeOrDefault() == MemberAudienceDTO.TYPE_ALL) {
            target.setScope(SendUserTargetDTO.SCOPE_ALL);
            return target;
        }
        target.setScope(SendUserTargetDTO.SCOPE_PART);
        if (audience.typeOrDefault() == MemberAudienceDTO.TYPE_RANK) {
            target.setRankIds(audience.getRankIds());
        } else {
            target.setTagIds(audience.getTagIds());
            target.setTagMatchMode(audience.matchModeOrDefault());
        }
        // 历史互斥语义下「选了等级/标签但列表为空」判定为不命中任何人，
        // 升维后表现为 scope=1 且三组全空，与 matches/resolveUserIds 的约定一致。
        return target;
    }

    @Override
    public MemberAudienceDTO toLegacy(SendUserTargetDTO target) {
        if (target == null) {
            return null;
        }
        MemberAudienceDTO audience = new MemberAudienceDTO();
        if (target.scopeOrDefault() == SendUserTargetDTO.SCOPE_ALL) {
            audience.setAudienceType(MemberAudienceDTO.TYPE_ALL);
            return audience;
        }
        boolean hasUsers = CollUtil.isNotEmpty(target.getUserIds());
        boolean hasRanks = CollUtil.isNotEmpty(target.getRankIds());
        boolean hasTags = CollUtil.isNotEmpty(target.getTagIds());
        // 指定会员无法用历史结构表达；多组并存是并集语义，降维会失真
        if (hasUsers || (hasRanks && hasTags)) {
            return null;
        }
        if (hasRanks) {
            audience.setAudienceType(MemberAudienceDTO.TYPE_RANK);
            audience.setRankIds(target.getRankIds());
            return audience;
        }
        if (hasTags) {
            audience.setAudienceType(MemberAudienceDTO.TYPE_TAG);
            audience.setTagIds(target.getTagIds());
            audience.setTagMatchMode(target.matchModeOrDefault());
            return audience;
        }
        // scope=1 但三组全空：语义是「不命中任何人」，历史结构表达不了
        return null;
    }

    // ------------------------------------------------------------------
    // 判定 / 解析（当前结构，三组并集）
    // ------------------------------------------------------------------

    @Override
    public boolean matchesScope(Long userId, String memberScopeJson) {
        SendUserTargetDTO target = parseScope(memberScopeJson);
        return target == null || matches(userId, target);
    }

    @Override
    public void validatePremiumClaim(Integer userId, String memberScopeJson, Map<String, String> profile,
                                     Boolean agreementAccepted, String agreementVersion) {
        SendUserTargetDTO target = parseScope(memberScopeJson);
        if (target == null) return;
        if (!matches(userId.longValue(), target)) throw new GlobalException("当前用户不满足指定人群条件");
        if (CollUtil.isNotEmpty(target.getRequiredProfileFields())) {
            for (String field : target.getRequiredProfileFields()) {
                // 用户资料未维护身份证，领卡不校验该历史配置项。
                if ("idCard".equals(field)) continue;
                if (profile == null || StrUtil.isBlank(profile.get(field))) {
                    String message = switch (field) {
                        case "mobile" -> "领卡需要手机号，请先完善手机号后重试";
                        case "name" -> "领卡需要用户名，请先完善用户名后重试";
                        case "birthday" -> "领卡需要生日信息，请先完善生日后重试";
                        case "email" -> "领卡需要邮箱，请先完善邮箱后重试";
                        case "address" -> "领卡需要收货地址，请先添加或完善收货地址（含详细地址）后重试";
                        default -> "领卡所需资料不完整，请完善个人资料后重试；如仍无法领卡，请联系客服";
                    };
                    throw new GlobalException(message);
                }
            }
        }
        if (Boolean.TRUE.equals(target.getAgreementRequired())) {
            if (!Boolean.TRUE.equals(agreementAccepted)) throw new GlobalException("请先同意会员协议");
            if (StrUtil.isNotBlank(target.getAgreementVersion()) && !target.getAgreementVersion().equals(agreementVersion)) throw new GlobalException("会员协议版本已更新，请重新确认");
        }
        List<Boolean> checks = new ArrayList<>();
        if (target.getMinOrderAmount() != null) checks.add(orderMapper.sumConsumptionAmount(userId).compareTo(target.getMinOrderAmount()) >= 0);
        if (target.getMinOrderCount() != null) checks.add(orderMapper.countConsumptionOrders(userId) >= target.getMinOrderCount());
        if (CollUtil.isNotEmpty(target.getProductIds())) checks.add(orderMapper.getHistoryProduct(userId).stream().anyMatch(target.getProductIds()::contains));
        if (target.getMinRechargeAmount() != null) checks.add(userRechargeOrderMapper.sumPaidAmount(userId).compareTo(target.getMinRechargeAmount()) >= 0);
        if (!checks.isEmpty()) {
            boolean pass = Integer.valueOf(1).equals(target.getConditionMatchMode()) ? checks.stream().allMatch(Boolean::booleanValue) : checks.stream().anyMatch(Boolean::booleanValue);
            if (!pass) throw new GlobalException("当前用户不满足历史领卡条件");
        }
    }

    @Override
    public boolean matches(Long userId, SendUserTargetDTO target) {
        if (userId == null) {
            return false;
        }
        // null 表示「无人群约束」，与 scope=0「显式配置为全部会员」不同：
        // 后者沿用历史 TYPE_ALL 语义，仍要求对象是真实存在的会员。
        if (target == null) {
            return true;
        }
        User user = userMapper.selectById(userId);
        // 标签只在人群确实按标签圈选时才查，保持「用不到就不查」的原有开销
        Set<Long> userTagIds = user != null && CollUtil.isNotEmpty(target.getTagIds())
                ? userMemberTagService.listTagIdsByUser(userId)
                : Set.of();
        return matches(user, userTagIds, target);
    }

    @Override
    public boolean matches(User user, Set<Long> userTagIds, SendUserTargetDTO target) {
        if (target == null) {
            return true;
        }
        if (user == null || !UserTypeEnum.MEMBER.getCode().equals(user.getUserType())) {
            return false;
        }
        if (target.scopeOrDefault() == SendUserTargetDTO.SCOPE_ALL) {
            return true;
        }
        // 三组并集：命中任一组即可
        if (CollUtil.isNotEmpty(target.getUserIds()) && target.getUserIds().contains(user.getUserId())) {
            return true;
        }
        if (CollUtil.isNotEmpty(target.getRankIds()) && target.getRankIds().contains(user.getRankId())) {
            return true;
        }
        if (CollUtil.isNotEmpty(target.getTagIds()) && matchesTags(userTagIds, target)) {
            return true;
        }
        // 含「scope=1 但三组全空」：不命中任何人（与历史空列表行为一致）
        return false;
    }

    /**
     * 标签匹配的内存版，口径与 {@code UserMemberTagService#userMatchesTags} 一致：
     * 满足全部 = 用户持有目标标签全集，满足任一 = 与目标标签有交集。
     */
    private boolean matchesTags(Set<Long> userTagIds, SendUserTargetDTO target) {
        Set<Long> owned = userTagIds == null ? Set.of() : userTagIds;
        if (owned.isEmpty()) {
            return false;
        }
        if (target.matchModeOrDefault() == SendUserTargetDTO.MATCH_ALL) {
            return owned.containsAll(target.getTagIds());
        }
        return target.getTagIds().stream().anyMatch(owned::contains);
    }

    @Override
    public List<Long> resolveUserIds(SendUserTargetDTO target) {
        if (target == null || target.scopeOrDefault() == SendUserTargetDTO.SCOPE_ALL) {
            return listAllMemberIds();
        }

        Set<Long> result = new LinkedHashSet<>();

        if (CollUtil.isNotEmpty(target.getUserIds())) {
            result.addAll(filterMemberIds(target.getUserIds().stream()
                    .filter(java.util.Objects::nonNull)
                    .map(Integer::longValue)
                    .toList()));
        }

        if (CollUtil.isNotEmpty(target.getRankIds())) {
            result.addAll(selectMemberIds(q -> q.in(User::getRankId, target.getRankIds())));
        }

        if (CollUtil.isNotEmpty(target.getTagIds())) {
            List<Long> tagUserIds = userMemberTagService.resolveUserIdsByTags(
                    target.getTagIds(), target.matchModeOrDefault());
            if (!tagUserIds.isEmpty()) {
                result.addAll(filterMemberIds(tagUserIds));
            }
        }

        // 含「scope=1 但三组全空」：空集（与历史空列表行为一致）
        return new ArrayList<>(result);
    }

    @Override
    public long countUsers(SendUserTargetDTO target) {
        if (target == null || target.scopeOrDefault() == SendUserTargetDTO.SCOPE_ALL) {
            Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                    .eq(User::getUserType, UserTypeEnum.MEMBER.getCode()));
            return count == null ? 0L : count;
        }
        return resolveUserIds(target).size();
    }

    @Override
    public String describe(SendUserTargetDTO target) {
        if (target == null || target.scopeOrDefault() == SendUserTargetDTO.SCOPE_ALL) {
            return "全部会员";
        }
        List<String> parts = new ArrayList<>();
        if (CollUtil.isNotEmpty(target.getUserIds())) {
            parts.add("指定会员：" + target.getUserIds().size() + " 人");
        }
        if (CollUtil.isNotEmpty(target.getRankIds())) {
            List<UserRank> ranks = userRankService.listByIds(target.getRankIds());
            parts.add("会员等级：" + ranks.stream().map(UserRank::getRankName).collect(Collectors.joining("、")));
        }
        if (CollUtil.isNotEmpty(target.getTagIds())) {
            List<MemberTag> tags = memberTagMapper.selectBatchIds(target.getTagIds());
            String mode = target.matchModeOrDefault() == SendUserTargetDTO.MATCH_ALL ? "全部" : "任一";
            parts.add("标签（" + mode + "）：" + tags.stream().map(MemberTag::getTagName).collect(Collectors.joining("、")));
        }
        return parts.isEmpty() ? "未指定会员" : String.join("；", parts);
    }

    // ------------------------------------------------------------------
    // 回显 / 落库
    // ------------------------------------------------------------------

    @Override
    public SendUserTargetDTO fillTargetUsers(SendUserTargetDTO target) {
        if (target == null) {
            return null;
        }
        if (CollUtil.isEmpty(target.getUserIds())) {
            target.setUsers(Collections.emptyList());
            return target;
        }
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                .select(User::getUserId, User::getUsername, User::getMobile)
                .eq(User::getUserType, UserTypeEnum.MEMBER.getCode())
                .in(User::getUserId, target.getUserIds()));
        Map<Integer, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getUserId, u -> u, (a, b) -> a));
        // 按配置顺序回显，查不到的用户直接跳过（与取数口径一致：不存在的 userId 忽略不报错）
        List<TargetUserVO> detail = new ArrayList<>();
        for (Integer userId : target.getUserIds()) {
            User user = userMap.get(userId);
            if (user == null) {
                continue;
            }
            TargetUserVO vo = new TargetUserVO();
            vo.setUserId(user.getUserId());
            vo.setUsername(user.getUsername());
            vo.setMobile(maskMobile(user.getMobile()));
            detail.add(vo);
        }
        target.setUsers(detail);
        return target;
    }

    @Override
    public String toScopeJson(SendUserTargetDTO target) {
        if (target == null || target.scopeOrDefault() == SendUserTargetDTO.SCOPE_ALL) {
            // 全部会员不落库，等价旧 memberRange=0
            return null;
        }
        // 手工组装而非直接序列化 DTO：保证 scope 一定写出（判形依赖它）、users 一定不落库
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("scope", SendUserTargetDTO.SCOPE_PART);
        if (CollUtil.isNotEmpty(target.getUserIds())) {
            data.put("userIds", target.getUserIds());
        }
        if (CollUtil.isNotEmpty(target.getRankIds())) {
            data.put("rankIds", target.getRankIds());
        }
        if (CollUtil.isNotEmpty(target.getTagIds())) {
            data.put("tagIds", target.getTagIds());
            data.put("tagMatchMode", target.matchModeOrDefault());
        }
        return JsonUtil.toJson(data);
    }

    @Override
    public String resolveWriteScope(SendUserTargetDTO target, MemberAudienceDTO audience) {
        SendUserTargetDTO effective = target != null ? target : upgradeOrNull(audience);
        if (effective == null) {
            return null;
        }
        effective = normalizeExclusive(effective);
        validateTarget(effective);
        return toScopeJson(effective);
    }

    @Override
    public SendUserTargetDTO normalizeExclusive(SendUserTargetDTO target) {
        // 与 toScopeJson / validateTarget 保持同一口径：非「全部会员」一律按「部分会员」处理
        if (target == null || target.scopeOrDefault() == SendUserTargetDTO.SCOPE_ALL) {
            return target;
        }
        SendUserTargetDTO normalized = new SendUserTargetDTO();
        normalized.setScope(SendUserTargetDTO.SCOPE_PART);
        if (CollUtil.isNotEmpty(target.getUserIds())) {
            normalized.setUserIds(target.getUserIds());
        } else if (CollUtil.isNotEmpty(target.getRankIds())) {
            normalized.setRankIds(target.getRankIds());
        } else if (CollUtil.isNotEmpty(target.getTagIds())) {
            normalized.setTagIds(target.getTagIds());
            normalized.setTagMatchMode(target.matchModeOrDefault());
        }
        // 三组全空时返回的仍是空配置，交由 validateTarget 抛「请选择会员」
        logDropped(target, normalized);
        return normalized;
    }

    /** 互斥归一发生裁剪时记录被清空的组，便于排查前端多组同传 */
    private void logDropped(SendUserTargetDTO source, SendUserTargetDTO normalized) {
        if (!log.isDebugEnabled()) {
            return;
        }
        List<String> dropped = new ArrayList<>();
        if (CollUtil.isNotEmpty(source.getUserIds()) && CollUtil.isEmpty(normalized.getUserIds())) {
            dropped.add("userIds");
        }
        if (CollUtil.isNotEmpty(source.getRankIds()) && CollUtil.isEmpty(normalized.getRankIds())) {
            dropped.add("rankIds");
        }
        if (CollUtil.isNotEmpty(source.getTagIds()) && CollUtil.isEmpty(normalized.getTagIds())) {
            dropped.add("tagIds");
        }
        if (!dropped.isEmpty()) {
            log.debug("人群圈选三组互斥，已清空 {}", String.join("、", dropped));
        }
    }

    @Override
    public void validateTarget(SendUserTargetDTO target) {
        if (target == null || target.scopeOrDefault() == SendUserTargetDTO.SCOPE_ALL) {
            return;
        }
        if (CollUtil.isEmpty(target.getUserIds())
                && CollUtil.isEmpty(target.getRankIds())
                && CollUtil.isEmpty(target.getTagIds())) {
            throw new GlobalException("请选择会员");
        }
    }

    private SendUserTargetDTO upgradeOrNull(MemberAudienceDTO audience) {
        return audience == null ? null : upgrade(audience);
    }

    /** 手机号脱敏：保留前3后4，如 138****0000；非 11 位手机号原样返回 */
    private String maskMobile(String mobile) {
        if (StrUtil.isBlank(mobile) || !mobile.matches("^\\d{11}$")) {
            return mobile;
        }
        return mobile.substring(0, 3) + "****" + mobile.substring(7);
    }

    // ------------------------------------------------------------------
    // 历史结构入口：统一委托升维后的实现，行为保持不变
    // ------------------------------------------------------------------

    @Override
    public List<Long> resolveUserIds(MemberAudienceDTO audience) {
        return resolveUserIds(upgrade(audience));
    }

    @Override
    public boolean matches(Long userId, MemberAudienceDTO audience) {
        return matches(userId, upgrade(audience));
    }

    @Override
    public long countUsers(MemberAudienceDTO audience) {
        return countUsers(upgrade(audience));
    }

    @Override
    public String describe(MemberAudienceDTO audience) {
        return describe(upgrade(audience));
    }

    // ------------------------------------------------------------------
    // 查询辅助
    // ------------------------------------------------------------------

    private List<Long> listAllMemberIds() {
        return selectMemberIds(q -> {
        });
    }

    /** 从给定用户ID中筛出真实存在的会员 */
    private List<Long> filterMemberIds(List<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        return selectMemberIds(q -> q.in(User::getUserId, userIds));
    }

    private List<Long> selectMemberIds(java.util.function.Consumer<LambdaQueryWrapper<User>> customizer) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .select(User::getUserId)
                .eq(User::getUserType, UserTypeEnum.MEMBER.getCode());
        customizer.accept(wrapper);
        return userMapper.selectList(wrapper).stream()
                .map(u -> u.getUserId().longValue())
                .toList();
    }
}
