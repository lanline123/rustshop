package com.tigshop.service.user;

import com.tigshop.bean.dto.user.MemberAudienceDTO;
import com.tigshop.bean.dto.user.SendUserTargetDTO;
import com.tigshop.bean.model.user.User;

import java.util.List;

/**
 * 统一人群圈选服务
 * <p>
 * 会员标签应用（二期）核心可复用能力：把"全部会员 / 指定会员 / 按会员等级 / 按会员标签"统一解析为
 * 用户集合，并提供单用户实时校验、预估人数、人群描述文案等能力，供站内信群发、优惠券、
 * 促销活动、秒杀、拼团、订单筛选等业务场景复用。
 * <p>
 * 人群载体有两代结构：{@link SendUserTargetDTO}（当前）与 {@link MemberAudienceDTO}（历史）。
 * {@code promotion.member_scope} 里两种格式并存，统一由 {@link #parseScope(String)} 判形并升维，
 * 业务侧只需面对 {@link SendUserTargetDTO}。
 * <p>
 * 写入与读取的口径刻意不对称：写入侧由 {@link #normalizeExclusive(SendUserTargetDTO)} 保证三组人群
 * 只落一组；读取侧仍按并集判定，为存量数据与异常数据兜底。两者不是实现不一致，勿"顺手统一"。
 *
 * @author Tigshop团队
 */
public interface MemberAudienceService {

    // ------------------------------------------------------------------
    // 当前结构：SendUserTargetDTO（三组并集）
    // ------------------------------------------------------------------

    /**
     * 解析 {@code promotion.member_scope} 存储的人群 JSON。
     * <p>
     * 自动兼容历史 {@link MemberAudienceDTO} 格式并升维。
     *
     * @return 人群条件；JSON 为空或解析失败时返回 null，调用方应回退旧 memberRange/memberMarks 逻辑
     */
    SendUserTargetDTO parseScope(String memberScopeJson);

    /**
     * 便捷入口：直接按 {@code member_scope} 原始 JSON 校验用户是否在人群内。
     * <p>
     * 等价于 {@code parseScope} 后 {@code matches}，人群为空（无约束）时返回 true。
     */
    boolean matchesScope(Long userId, String memberScopeJson);

    /** 校验付费会员卡领卡时的人群、历史消费、充值、资料和协议条件。 */
    void validatePremiumClaim(Integer userId, String memberScopeJson, java.util.Map<String, String> profile,
                              Boolean agreementAccepted, String agreementVersion);

    /**
     * 实时校验某个用户当前是否在人群范围内
     */
    boolean matches(Long userId, SendUserTargetDTO target);

    /**
     * 用调用方已加载的会员资料判定人群，语义与 {@link #matches(Long, SendUserTargetDTO)} 完全一致，
     * 只是把「查 User」「查用户标签」两次查询交由调用方整批准备。
     * <p>
     * 供需要对同一用户连续判定多组人群的场景使用（如前台促销总线逐个活动判人群），
     * 避免每个活动各查一次用户与标签。
     *
     * @param user       已查好的用户；null 视为用户不存在，一律不命中
     * @param userTagIds 该用户持有的全部标签ID，无标签传空集
     * @param target     人群条件；null 表示无人群约束，恒命中
     */
    boolean matches(User user, java.util.Set<Long> userTagIds, SendUserTargetDTO target);

    /**
     * 解析人群对应的会员用户ID集合
     */
    List<Long> resolveUserIds(SendUserTargetDTO target);

    /**
     * 预估人群命中人数
     */
    long countUsers(SendUserTargetDTO target);

    /**
     * 生成人群描述文案
     */
    String describe(SendUserTargetDTO target);

    /**
     * 按 userIds 批量回填「指定会员」明细（手机号脱敏），用于 detail 回显。
     * <p>
     * 查不到或非会员的 userId 不出现在结果中。
     *
     * @return 原对象（已回填 users）；入参为 null 时返回 null
     */
    SendUserTargetDTO fillTargetUsers(SendUserTargetDTO target);

    /**
     * 把人群条件序列化为 {@code member_scope} 存储 JSON。
     * <p>
     * users 明细不落库；「全部会员」返回 null（等价旧 memberRange=0）。
     */
    String toScopeJson(SendUserTargetDTO target);

    /**
     * 写入侧统一入口：优先取 {@code sendUserTarget}，回退历史 {@code memberAudience}，
     * 校验通过后升维并序列化为存储 JSON。
     *
     * @throws com.tigshop.common.exception.GlobalException scope=1 但三组人群全为空时抛出
     */
    String resolveWriteScope(SendUserTargetDTO target, MemberAudienceDTO audience);

    /**
     * 校验人群配置可用：scope=1 时三组不得全为空。
     *
     * @throws com.tigshop.common.exception.GlobalException 三组全空时抛出「请选择会员」
     */
    void validateTarget(SendUserTargetDTO target);

    /**
     * 写入侧三组互斥归一：scope=1 时只保留一组人群条件。
     * <p>
     * 优先级 userIds &gt; rankIds &gt; tagIds，命中后清空其余两组；多组同传时静默裁剪不报错。
     * 落库前必须调用，读侧的并集语义仅为存量与异常数据兜底，不代表写入允许多组。
     *
     * @return 归一后的新对象，不修改入参；入参为 null 时返回 null
     */
    SendUserTargetDTO normalizeExclusive(SendUserTargetDTO target);

    /**
     * 历史结构升维为当前结构
     */
    SendUserTargetDTO upgrade(MemberAudienceDTO audience);

    /**
     * 当前结构降维回历史结构，用于 detail 兼容回显。
     *
     * @return 无法无损表达时（含指定会员、或多组并存）返回 null
     */
    MemberAudienceDTO toLegacy(SendUserTargetDTO target);

    // ------------------------------------------------------------------
    // 历史结构：MemberAudienceDTO（互斥），保留供既有调用方使用
    // ------------------------------------------------------------------

    /**
     * 解析人群对应的会员用户ID集合
     */
    List<Long> resolveUserIds(MemberAudienceDTO audience);

    /**
     * 实时校验某个用户当前是否在人群范围内
     */
    boolean matches(Long userId, MemberAudienceDTO audience);

    /**
     * 预估人群命中人数
     */
    long countUsers(MemberAudienceDTO audience);

    /**
     * 生成人群描述文案，例如：「标签（任一）：VIP客户、高消费」「会员等级：黄金、白金」「全部会员」
     */
    String describe(MemberAudienceDTO audience);
}
