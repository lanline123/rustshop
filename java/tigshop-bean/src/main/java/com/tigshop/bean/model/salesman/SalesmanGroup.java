// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.model.salesman;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分销员分组model
 *
 * @author kidd
 * @since 2025/06/21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("salesman_group")
@Schema(description = "分销员分组")
public class SalesmanGroup {

    @TableId(value = "group_id", type = IdType.AUTO)
    @Schema(description = "分销员分组ID")
    private Integer groupId;

    @Schema(description = "分组名称")
    private String groupName;

    @Schema(description = "分组描述")
    @TableField(value = "`describe`")
    private String describe;

    @Schema(description = "添加时间")
    private Long addTime;

    @Schema(description = "所属店铺ID")
    private Integer shopId;
}