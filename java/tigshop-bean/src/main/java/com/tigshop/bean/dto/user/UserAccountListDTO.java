package com.tigshop.bean.dto.user;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 账户信息表数据对象
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:13
 */
@Setter
@Getter
@Schema(description = "账户信息")
public class UserAccountListDTO  extends BasePage {
    @Schema(description = "账户")
    private Boolean balance;
}
