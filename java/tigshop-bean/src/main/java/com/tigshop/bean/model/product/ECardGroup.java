// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.model.product;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 电子卡券组model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@TableName("e_card_group")
@Schema(description = "电子卡券组")
public class ECardGroup {
    @TableId(value = "group_id", type = IdType.AUTO)
    @Schema(description = "卡券分组id")
    private Integer groupId;

    @Schema(description = "分组名称")
    private String groupName;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "0:不使用,1:使用")
    private Integer isUse;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "添加时间")
    private Long addTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private Long upTime;
}