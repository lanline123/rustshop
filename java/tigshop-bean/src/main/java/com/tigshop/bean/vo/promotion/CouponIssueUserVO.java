package com.tigshop.bean.vo.promotion;

import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.enums.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 优惠券手动发放会员项
 */
@Data
@Schema(description = "优惠券手动发放会员项")
public class CouponIssueUserVO {

    @Schema(description = "会员ID")
    private Integer userId;

    @Schema(description = "会员名称")
    private String memberName;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Sensitive(type = SensitiveType.PHONE)
    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "会员来源")
    private Integer fromTag;

    @Schema(description = "会员来源名称")
    private String fromTagName;

    @Schema(description = "是否已认证：1-是，0-否")
    private Integer isCertified;

    @Schema(description = "是否企业认证：1-是，0-否")
    private Integer isCompanyAuth;

    @Schema(description = "会员状态：1-启用，0-禁用")
    private Integer status;

    @Schema(description = "会员状态名称")
    private String statusName;

    @Schema(description = "头像")
    private String avatar;
}
