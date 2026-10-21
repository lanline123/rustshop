package com.tigshop.common.core.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 基础实体类
 *
 * @author kidd
 * @since 2025/4/1 10:56
 */
@Data
public class BasePO {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Long id;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private Long createTime;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建人主键")
    private Long createById;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建人名称")
    private String createByName;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private Long updateTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "编辑人主键")
    private Long updateById;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新人名称")
    private String updateByName;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "是否删除；0-否，1-是")
    private Integer isDel;
}
