package com.tigshop.bean.dto.user;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户列表查询条件
 */
@Getter
@Setter
@Schema(description = "用户列表查询条件")
public class UserListDTO extends BasePage {

    @Schema(description = "来源")
    private Integer fromTag;

    @Schema(description = "会员等级")
    private Integer rankId;

    @Schema(description = "可用金额")
    private BigDecimal balance;

    @Schema(description = "积分大于")
    private BigDecimal pointsGt;

    @Schema(description = "积分小于")
    private BigDecimal pointsLt;

    @Schema(description = "会员类型")
    private Integer userType;

    @Schema(description = "是否分页")
    private Integer isPage;

    @Schema(description = "标记")
    private Integer mark;

    @Schema(description = "是否精确")
    private Boolean isExact;

    @Schema(description = "会员标签ID列表，筛选拥有这些标签的用户")
    private List<Long> tagIds;

    @Schema(description = "标签匹配模式：0-满足任意一个(默认)，1-满足全部(包含即可)")
    private Integer tagMatchMode;

    public UserListDTO() {
        this.isPage = 0;
        this.fromTag = 0;
    }
}
