package com.tigshop.bean.query.user;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Tigshop
 */
@Getter
@Setter
@Schema(description = "会员留言")
public class UserPointsLogListPageQuery extends BasePage {

    @Schema(description = "用户ID")
    private Integer userId;
}