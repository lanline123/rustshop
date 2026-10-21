package com.tigshop.bean.model.user;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 自提联系人表
 *
 * @author Tigshop
 */
@TableName(value = "pickup_contact")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "自提联系人")
public class PickupContact implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "客户ID")
    private Integer userId;

    @Schema(description = "联系人名称")
    private String contactName;

    @Schema(description = "联系人电话")
    private String contactPhone;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private Long createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private Long updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "删除标志；0-未删除，1-已删除")
    private Integer isDel;
}
