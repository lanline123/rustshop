package com.tigshop.service.deposit;

/**
 * 保证金生命周期服务（W5：双缓冲到期惩罚 + 商品下架/恢复 + 收取节点触发）。统一 owner 维度（店铺 + 供应商）。
 *
 * <p>双缓冲：收取节点缓冲（{@code collect_due_time}）+ 补缴缓冲（{@code supplement_due_time}）。
 * 收取节点触发时启动收取缓冲；应缴上调 / 缺口新增时启动补缴缓冲；定时任务每天扫描，
 * pending&gt;0 且任一缓冲到期 → 取<b>最近到期</b>那一档的 action 处罚（先到先处罚，后到期的更重档位不叠加升级）；
 * 首次处罚后冻结档位，直到补足（pending&lt;=0）后自动恢复。</p>
 *
 * <p>惩罚动作为两个档位，码值见 {@link com.tigshop.bean.enums.deposit.DepositPenaltyAction}：
 * 1 {@code FORBID_ON_SHELF}（限制上架，仅拦新上架，不下架已上架商品）；
 * 2 {@code OFF_SHELF_ALL}（全店下架，物理下架在售商品 + 记 {@code shop_deposit_offshelf_log}，
 * 并拦上架 + 拦下单——商品被下架后理应无法下单，显式拦单兜底购物车/直链/并发）。
 * 派生：拦上架 = 命中{1,2}；拦下单 = 命中{2}；物理下架 = 命中{2}。</p>
 *
 * <p>账户 {@code penalty_status} 存生效动作码（0 正常 / 1 限制上架 / 2 全店下架；取最近到期那一档的动作，
 * 首次处罚后冻结、不随后到期升级）；它作"已处罚"标记，既有消费方按 {@code != 0} 判定；
 * 实时拦截按 config 现算，但已处罚时按冻结档位现算、不再升级。</p>
 *
 * @author Tigshop团队
 * @create 2026-06-10
 */
public interface DepositLifecycleService {

    /**
     * 收取节点触发：启动收取缓冲。仅当本次触发节点与全局配置 {@code deposit_collect_node} 一致、
     * 功能开启、且该 owner 当前待补缴 &gt; 0 且收取缓冲未启动（{@code collect_due_time<=0}）时，
     * 置 {@code collect_due_time = now + collect_buffer_days * 86400}。幂等：已启动则不重复。
     *
     * <p>调用方应以最小、非阻断（try-catch 包裹）方式接入；本方法内部已对功能开关 / 节点不匹配 /
     * 无需补缴 等情况安全返回，不抛出业务异常以免影响上架/下单主流程。</p>
     *
     * @param ownerType   持有者类型 1店铺 2供应商（{@link com.tigshop.bean.enums.deposit.DepositOwnerType}）
     * @param ownerId     持有者id（店铺为 shopId，供应商为 vendorId）
     * @param triggerNode 本次触发节点 1上架时 2首单时（{@link com.tigshop.bean.enums.deposit.DepositCollectNode}）
     */
    void onCollectTrigger(Integer ownerType, Integer ownerId, Integer triggerNode);

    /**
     * 补缴触发：已建账户在初次收取之后又因应缴上调 / 缺口新增（类目升级导致 R 上调、
     * 平台手动上调应缴、赔付扣除致 paid 减少）产生新的待补缴时，启动补缴缓冲。
     * 仅当功能开启、该 owner 当前待补缴 &gt; 0、且补缴缓冲未启动（{@code supplement_due_time<=0}）时，
     * 置 {@code supplement_due_time = now + supplement_buffer_days * 86400}。
     *
     * <p>幂等（已启动则不重启不重置，遵循"先到先处罚，后面的不需要了"）；与收取缓冲可并存，
     * 到期处罚由 {@link #runDueCheck} / 实时准入按并集判定，最近到期者先生效。
     * 非阻断：功能关闭 / 无需补缴 / 已启动 等情况安全返回，不抛业务异常以免影响主流程；
     * 调用方应以最小、非阻断（try-catch 包裹）方式接入。</p>
     *
     * @param ownerType 持有者类型 1店铺 2供应商（{@link com.tigshop.bean.enums.deposit.DepositOwnerType}）
     * @param ownerId   持有者id（店铺为 shopId，供应商为 vendorId）
     */
    void onSupplementTrigger(Integer ownerType, Integer ownerId);

    /**
     * 经营类目变更触发：商品上下架导致在售类目集合变化、应缴可能变动时调用。
     * 与账户 {@code required_snapshot} 快照 diff：有变动则写一条应缴调整记录
     * （source=类目变更，adminId=0 表示系统）并刷新快照。
     *
     * <p>幂等、非阻断：功能关闭 / 手动固定（{@code required_manual} 非空，应缴不随类目变动）/
     * 无应缴且无账户 / 应缴无变化 等情况均安全返回，不抛业务异常以免影响上下架主流程。
     * 调用方应以最小、非阻断（try-catch 包裹）方式接入。</p>
     *
     * @param ownerType 持有者类型 1店铺 2供应商（{@link com.tigshop.bean.enums.deposit.DepositOwnerType}）
     * @param ownerId   持有者id（店铺为 shopId，供应商为 vendorId）
     */
    void onCategoryChange(Integer ownerType, Integer ownerId);

    /**
     * 保证金规则调整触发：平台新建 / 修改 / 启用保证金规则（{@code shop_deposit_rule}）导致应缴口径
     * 可能变动时调用。遍历现有全部保证金账户，逐个与账户 {@code required_snapshot} 快照 diff：
     * 有变动则写一条应缴调整记录（source=规则调整，adminId=0 表示系统）并刷新快照；
     * 应缴上调（after&gt;before 且 before&gt;0）时启动补缴缓冲 {@link #onSupplementTrigger}。
     *
     * <p>幂等、非阻断：功能关闭 / 无账户 / 手动固定 / 应缴无变化等情况安全返回；单账户失败不影响其余账户。
     * 仅处理<b>已存在</b>的保证金账户，不为"有在售商品但从未建账户"的存量商户回溯补建；
     * 未来生效规则（startTime 在将来）在本次不改变当前应缴，届时由自然生效后的其他事件推进。
     * 建议在规则事务提交后调用（afterCommit），避免与规则写入同事务长时间持锁。</p>
     */
    void onDepositRuleChange();

    /**
     * 双缓冲到期扫描（定时任务驱动，每天执行）。遍历所有保证金账户：
     * <ul>
     *   <li>pending &lt;= 0 且已被惩罚（{@code penalty_status != 0}）→ 自动恢复 {@link #restore}。</li>
     *   <li>pending &gt; 0：收取缓冲 / 补缴缓冲任一到期 → 取<b>最近到期</b>那一档对应 action 执行处罚
     *       （penalty_status 置动作码 1/2；动作为"全店下架"(2) 时物理下架在售商品 + 写下架日志）。
     *       首次处罚后冻结：账户已处罚则维持原档位，不随后到期升级。</li>
     * </ul>
     * 单账户处理失败不影响其余账户；处罚 / 恢复均幂等，部分失败可由次日重跑自愈。
     */
    void runDueCheck();

    /**
     * 恢复：补足（pending &lt;= 0）后解除惩罚。置 {@code penalty_status=0}、清空双缓冲到期时间，
     * 并把 {@code shop_deposit_offshelf_log} 中 status=1（已下架待恢复）的商品恢复上架、
     * 置 status=2、写 {@code restore_time}。pending 仍 &gt; 0 时安全返回不做恢复。
     *
     * @param ownerType 持有者类型 1店铺 2供应商
     * @param ownerId   持有者id（店铺为 shopId，供应商为 vendorId）
     */
    void restore(Integer ownerType, Integer ownerId);

    /**
     * 上架准入校验：当 owner 当前生效动作命中"限制上架"(1) 或"全店下架"(2) 时，
     * 抛 {@code GlobalException(5405)} 阻断上架新品；正常返回表示允许上架。
     *
     * <p>实时现算（功能关闭 / 无账户 / 双缓冲均未到期 / 已补足 → 放行），不依赖定时任务是否已打标；
     * 对正常账户廉价短路。调用方需在商品真正置在售之前调用，且<b>不得 try-catch 吞异常</b>
     * （与非阻断的 {@code onCollectTrigger} 相反）。</p>
     *
     * @param ownerType 持有者类型 1店铺 2供应商
     * @param ownerId   持有者id（店铺为 shopId，供应商为 vendorId）
     */
    void assertOnShelfAllowed(Integer ownerType, Integer ownerId);

    /**
     * 下单准入校验：当 owner 当前生效动作命中"全店下架"(2) 时，
     * 抛 {@code GlobalException(5406)} 阻断新订单；正常返回表示允许下单。
     *
     * <p>实时现算、补足后即时放行；对正常账户廉价短路。
     * 调用方需在订单真正创建之前调用，且<b>不得 try-catch 吞异常</b>。</p>
     *
     * @param ownerType 持有者类型 1店铺 2供应商
     * @param ownerId   持有者id（店铺为 shopId，供应商为 vendorId）
     */
    void assertOrderAllowed(Integer ownerType, Integer ownerId);
}
