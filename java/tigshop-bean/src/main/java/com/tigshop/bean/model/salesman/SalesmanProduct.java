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

/**
 * 分销商品model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("salesman_product")
@Schema(description = "分销商品")
public class SalesmanProduct {

    @TableId(type = IdType.AUTO)
    @Schema(description = "分销商品ID")
    private Integer salesmanProductId;

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "是否参与分销，0为不参与，1为参与")
    private Integer isJoin;

    @Schema(description = "佣金计算方式，1为默认，2为自定义比例，3为自定义金额")
    private Integer commissionType;

    @Schema(description = "佣金数据")
    private String commissionData;

    @Schema(description = "添加时间")
    private Long addTime;

    @Schema(description = "更新时间")
    private Long updateTime;

    @Schema(description = "店铺ID")
    private Integer shopId;
}
