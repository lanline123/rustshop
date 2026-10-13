package com.tigshop.bean.vo.user;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.common.config.TimestampToDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户标签VO")
public class UserMemberTagVO {

    @Schema(description = "标签ID")
    private Long tagId;

    @Schema(description = "标签名称")
    private String tagName;

    @Schema(description = "标签组ID")
    private Long groupId;

    @Schema(description = "标签组名称")
    private String groupName;

    @Schema(description = "打标时间")
    @JsonSerialize(using = TimestampToDateSerializer.class)
    private String createTime;
}
