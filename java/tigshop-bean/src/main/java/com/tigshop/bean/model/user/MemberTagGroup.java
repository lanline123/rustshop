package com.tigshop.bean.model.user;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tigshop.common.core.entity.BasePO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("member_tag_group")
@Schema(description = "会员标签组")
public class MemberTagGroup extends BasePO implements Serializable {

    @Schema(description = "店铺ID，0=平台")
    private Integer shopId;

    @Schema(description = "组名称")
    private String groupName;

    @Schema(description = "置顶权重；0-未置顶，正整数越大表示越新置顶（排越前）")
    private Integer isTop;

    @Serial
    private static final long serialVersionUID = 1L;
}
