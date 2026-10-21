package com.tigshop.bean.vo.promotion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 优惠券手动发放预览结果
 */
@Data
@Schema(description = "优惠券手动发放预览结果")
public class CouponManualIssuePreviewVO {

    @Schema(description = "优惠券ID")
    private Integer couponId;

    @Schema(description = "发放模式")
    private String issueType;

    @Schema(description = "有效会员数")
    private Integer memberCount;

    @Schema(description = "预计发放张数")
    private Integer couponCount;

    @Schema(description = "失败会员数")
    private Integer failCount;

    @Schema(description = "失败会员明细")
    private List<CouponManualIssueFailDetailVO> failDetails = new ArrayList<>();
}
