package com.tigshop.bean.param.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "更新会员标签组参数")
public class MemberTagGroupUpdateParam {

    @Schema(description = "标签组ID")
    @NotNull(message = "标签组ID不能为空")
    private Long id;

    @Schema(description = "组名称")
    @NotBlank(message = "组名称不能为空")
    private String groupName;

    @Schema(description = "标签列表，至少保留两个")
    @Valid
    @NotNull(message = "标签不能为空")
    @Size(min = 2, message = "至少保留两个标签")
    private List<MemberTagItemParam> tags;
}
