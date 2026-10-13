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
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 分销员
 *
 * @author kidd
 * @create 2025/6/21
 */
@Schema(description = "分销员")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("salesman")
public class Salesman {

    @TableId(value = "salesman_id", type = IdType.AUTO)
    @Schema(description = "分销员ID")
    private Integer salesmanId;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "等级")
    private Integer level;

    @Schema(description = "分组ID")
    private Integer groupId;

    @Schema(description = "上级分销员ID")
    private Integer pid;

    @Schema(description = "添加时间")
    private Long addTime;

    @Schema(description = "所属店铺ID")
    private Integer shopId;

    @Schema(description = "累计销售额")
    private BigDecimal saleAmount;
}