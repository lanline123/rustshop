package com.tigshop.bean.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 统一人群圈选条件
 * <p>
 * 会员标签应用（二期）：站内信群发、优惠券、促销活动、秒杀、拼团、订单筛选等场景
 * 统一复用本结构来描述"目标人群范围"。
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "人群圈选条件")
public class MemberAudienceDTO {

    /** 全部会员 */
    public static final int TYPE_ALL = 0;
    /** 按会员等级 */
    public static final int TYPE_RANK = 1;
    /** 按会员标签 */
    public static final int TYPE_TAG = 2;

    /** 标签匹配：满足任一 */
    public static final int MATCH_ANY = 0;
    /** 标签匹配：满足全部 */
    public static final int MATCH_ALL = 1;

    @Schema(description = "人群类型：0=全部会员(默认) 1=按会员等级 2=按会员标签")
    private Integer audienceType = TYPE_ALL;

    @Schema(description = "会员等级ID列表（audienceType=1 时使用）")
    private List<Integer> rankIds;

    @Schema(description = "会员标签ID列表（audienceType=2 时使用）")
    private List<Long> tagIds;

    @Schema(description = "标签匹配方式：0=满足任一(默认) 1=满足全部")
    private Integer tagMatchMode = MATCH_ANY;

    public int typeOrDefault() {
        return audienceType == null ? TYPE_ALL : audienceType;
    }

    public int matchModeOrDefault() {
        return tagMatchMode == null ? MATCH_ANY : tagMatchMode;
    }
}
