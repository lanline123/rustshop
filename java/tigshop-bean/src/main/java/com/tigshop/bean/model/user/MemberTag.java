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
@TableName("member_tag")
@Schema(description = "会员标签")
public class MemberTag extends BasePO implements Serializable {

    @Schema(description = "店铺ID，0=平台")
    private Integer shopId;

    @Schema(description = "所属标签组ID")
    private Long groupId;

    @Schema(description = "标签名称")
    private String tagName;

    @Serial
    private static final long serialVersionUID = 1L;
}
