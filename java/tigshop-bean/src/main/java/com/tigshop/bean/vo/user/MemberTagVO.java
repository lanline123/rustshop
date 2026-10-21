package com.tigshop.bean.vo.user;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.common.config.TimestampToDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "会员标签VO")
public class MemberTagVO {

    @Schema(description = "标签ID")
    private Long id;

    @Schema(description = "所属标签组ID")
    private Long groupId;

    @Schema(description = "标签名称")
    private String tagName;

    @Schema(description = "创建时间")
    @JsonSerialize(using = TimestampToDateSerializer.class)
    private String createTime;
}
