package com.tigshop.bean.vo.user;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.common.config.TimestampToDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "会员标签组列表VO")
public class MemberTagGroupListVO {

    @Schema(description = "标签组ID")
    private Long id;

    @Schema(description = "组名称")
    private String groupName;

    @Schema(description = "标签值，逗号分隔")
    private String tagValues;

    @Schema(description = "标签列表（含ID）")
    private List<MemberTagVO> tags;

    @Schema(description = "置顶权重；0-未置顶，正整数越大表示越新置顶（排越前）")
    private Integer isTop;

    @Schema(description = "客户人数")
    private Long customerCount;

    @Schema(description = "创建时间")
    @JsonSerialize(using = TimestampToDateSerializer.class)
    private String createTime;
}
