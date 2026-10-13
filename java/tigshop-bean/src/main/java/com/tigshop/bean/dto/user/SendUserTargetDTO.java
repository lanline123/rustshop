package com.tigshop.bean.dto.user;

import com.tigshop.bean.vo.user.TargetUserVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

/**
 * 统一人群圈选条件（发送会员 / 参与人群）
 * <p>
 * 站内信群发、优惠券、促销活动、秒杀、拼团、限时折扣、商品活动统一复用本结构描述目标人群。
 * 相较 {@link MemberAudienceDTO} 的单一类型语义，本结构为「全部 / 部分」两级，
 * 「部分」下再按指定会员、会员等级、会员标签三组之一圈选。
 * <p>
 * 写入侧三组互斥：落库前由 {@code MemberAudienceService#normalizeExclusive} 按
 * userIds &gt; rankIds &gt; tagIds 的优先级只保留一组，多组同传静默裁剪。
 * 读取侧仍按三组并集判定（满足任一即命中），为存量数据与异常数据兜底 —— 两侧口径不对称是有意设计。
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "发送会员 / 参与人群")
public class SendUserTargetDTO {

    /** 全部会员 */
    public static final int SCOPE_ALL = 0;
    /** 部分会员 */
    public static final int SCOPE_PART = 1;

    /** 标签匹配：满足任一 */
    public static final int MATCH_ANY = 0;
    /** 标签匹配：满足全部 */
    public static final int MATCH_ALL = 1;

    /**
     * 范围。
     * <p>
     * 刻意不给默认值：{@code scope} 是否为 null 同时用于区分 {@code promotion.member_scope} 里
     * 存的是本结构还是历史 {@link MemberAudienceDTO} 结构（后者没有该字段）。
     * 业务取值一律走 {@link #scopeOrDefault()}，缺省即「全部会员」。
     */
    @Schema(description = "范围：0=全部会员(默认) 1=部分会员")
    private Integer scope;

    @Schema(description = "指定会员的用户ID列表（scope=1 生效）")
    private List<Integer> userIds;

    @Schema(description = "会员等级ID列表（scope=1 生效）")
    private List<Integer> rankIds;

    @Schema(description = "会员标签ID列表（scope=1 生效）")
    private List<Long> tagIds;

    @Schema(description = "标签匹配方式：0=满足任一(默认) 1=满足全部")
    private Integer tagMatchMode;

    @Schema(description = "历史领卡条件匹配方式：0=满足任一，1=满足全部")
    private Integer conditionMatchMode;
    @Schema(description = "累计消费金额门槛；为空表示不启用")
    private BigDecimal minOrderAmount;
    @Schema(description = "累计消费次数门槛；为空表示不启用")
    private Integer minOrderCount;
    @Schema(description = "历史购买过的指定商品ID；为空表示不启用")
    private List<Integer> productIds;
    @Schema(description = "累计充值金额门槛；为空表示不启用")
    private BigDecimal minRechargeAmount;
    @Schema(description = "领卡时必填资料：mobile、name、birthday、idCard、address、email")
    private List<String> requiredProfileFields;
    @Schema(description = "是否要求领卡时同意协议")
    private Boolean agreementRequired;
    @Schema(description = "协议版本或标识")
    private String agreementVersion;
    @Schema(description = "协议内容，用于前端展示和同意快照")
    private String agreementText;
    @Schema(description = "协议默认勾选：true=默认勾选同意，false=用户手动勾选")
    private Boolean agreementDefaultChecked;
    @Schema(description = "协议富文本列表，最多5个")
    private List<Map<String, String>> agreements;
    @Schema(description = "各条件不满足时的跳转链接，key 可为 orderAmount、orderCount、product、recharge")
    private Map<String, String> jumpUrls;

    /**
     * 指定会员明细，仅用于 detail 回显。
     * 取数一律以 {@link #userIds} 为准，本字段不参与落库序列化。
     */
    @Schema(description = "指定会员明细（仅出参回显，入参忽略）")
    private List<TargetUserVO> users;

    public int scopeOrDefault() {
        return scope == null ? SCOPE_ALL : scope;
    }

    /** 标签匹配方式归一：非 0/1 一律按「满足任一」处理 */
    public int matchModeOrDefault() {
        return Integer.valueOf(MATCH_ALL).equals(tagMatchMode) ? MATCH_ALL : MATCH_ANY;
    }
}
