package com.tigshop.bean.vo.user;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.common.config.TimestampToDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "会员标签组详情VO")
public class MemberTagGroupDetailVO {

    @Schema(description = "标签组ID")
    private Long id;

    @Schema(description = "组名称")
    private String groupName;

    @Schema(description = "创建时间")
    @JsonSerialize(using = TimestampToDateSerializer.class)
    private String createTime;

    @Schema(description = "组下标签列表")
    private List<MemberTagVO> tags;
}
