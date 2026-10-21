package com.tigshop.bean.param.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "新增会员标签组参数")
public class MemberTagGroupCreateParam {

    @Schema(description = "组名称")
    @NotBlank(message = "组名称不能为空")
    private String groupName;

    @Schema(description = "标签值列表，首次创建至少填写两个")
    @NotNull(message = "标签值不能为空")
    @Size(min = 2, message = "至少填写两个标签值")
    private List<String> tagNames;
}
