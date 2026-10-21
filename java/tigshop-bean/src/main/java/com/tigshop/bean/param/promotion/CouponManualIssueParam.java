package com.tigshop.bean.param.promotion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 优惠券手动发放参数
 */
@Data
@Schema(description = "优惠券手动发放参数")
public class CouponManualIssueParam {

    public static final String ISSUE_TYPE_USER = "USER";
    public static final String ISSUE_TYPE_RANK = "RANK";
    public static final String ISSUE_TYPE_TAG = "TAG";

    @NotNull(message = "优惠券ID不能为空")
    @Schema(description = "优惠券ID")
    private Integer couponId;

    @NotBlank(message = "发放模式不能为空")
    @Schema(description = "发放模式：USER-指定会员，TAG-会员标签，RANK-会员等级（三者互斥）")
    private String issueType;

    @Schema(description = "指定会员ID列表，仅 USER 模式使用")
    private List<Integer> userIds;

    @Schema(description = "会员等级ID列表，仅 RANK 模式使用")
    private List<Integer> rankIds;

    @Schema(description = "会员标签ID列表，仅 TAG 模式使用")
    private List<Long> tagIds;

    @Schema(description = "标签匹配：0=满足任一(默认) 1=满足全部，仅 TAG 模式使用")
    private Integer tagMatchMode;
}
