package com.tigshop.bean.vo.user;

import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.enums.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 会员导入成功列表 VO
 *
 * @author Tigshop团队
 * @create 2025年12月01日
 */
@Data
@Schema(name = "会员导入成功列表VO")
public class UserImportSuccessListVO {

    @Schema(description = "会员ID")
    private Integer userId;

    @Schema(description = "用户名")
    private String username;

    @Sensitive(type = SensitiveType.PHONE)
    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "会员等级名称")
    private String rankName;

    @Schema(description = "头像")
    private String avatar;
}

