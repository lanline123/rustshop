package com.tigshop.bean.vo.wholesale;

import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.enums.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 批发商列表 VO
 *
 * <p>CR-20260602-134800（CR-VOS-1）：回填 user / user_rank 表字段。
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Data
@Schema(description = "批发商列表项")
public class WholesalerVO {

    @Schema(description = "批发商ID")
    private Integer wholesalerId;

    @Schema(description = "关联 user_id")
    private Integer userId;

    @Schema(description = "用户名")
    private String username;

    @Sensitive(type = SensitiveType.PHONE)
    @Schema(description = "手机号")
    private String mobile;

    @Sensitive(type = SensitiveType.EMAIL)
    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "会员等级ID")
    private Integer rankId;

    @Schema(description = "会员等级名称")
    private String rankName;

    @Schema(description = "是否企业认证：0=否 1=是")
    private Integer isCompanyAuth;

    @Schema(description = "认证状态名（派生：1→已认证 0→未认证）")
    private String authStatusName;

    @Schema(description = "注册渠道：1=公众号 2=小程序 3=H5 4=PC 5=APP")
    private Integer fromTag;

    @Schema(description = "注册渠道名（派生）")
    private String fromTagName;

    @Schema(description = "会员状态：1=正常 0=禁用")
    private Integer userStatus;

    @Schema(description = "批发等级ID")
    private Integer gradeId;

    @Schema(description = "批发等级名称")
    private String gradeName;

    @Schema(description = "批发商启停：0=禁用 1=启用")
    private Integer status;

    @Schema(description = "来源：1=注册自动 2=后台导入 3=手动添加")
    private Integer source;

    @Schema(description = "来源名（派生）")
    private String sourceName;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "添加时间 yyyy-MM-dd HH:mm:ss")
    private String addTime;
}
