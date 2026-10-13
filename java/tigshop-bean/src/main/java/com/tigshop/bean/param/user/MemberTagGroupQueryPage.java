package com.tigshop.bean.param.user;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Schema(description = "会员标签组列表查询参数")
public class MemberTagGroupQueryPage extends BasePage {

    @Schema(description = "标签组ID")
    private Long groupId;

    @Schema(description = "组名称")
    private String groupName;
}
