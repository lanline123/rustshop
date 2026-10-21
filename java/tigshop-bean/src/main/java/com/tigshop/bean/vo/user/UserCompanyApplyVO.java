package com.tigshop.bean.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 我的申请VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "我的认证视图详情对象")
public class UserCompanyApplyVO {
    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "类型")
    private Integer type;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "类型文本")
    private String typeText;
}
