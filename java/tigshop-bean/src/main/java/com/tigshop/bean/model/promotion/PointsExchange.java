// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.model.promotion;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 积分商品model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@TableName("points_exchange")
@Schema(description = "积分商品")
public class PointsExchange {

    @TableId(type = IdType.AUTO)
    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "所需积分")
    private Integer exchangeIntegral;

    @Schema(description = "积分抵扣金额")
    private BigDecimal pointsDeductedAmount;

    @Schema(description = "是否热门")
    private Integer isHot;

    @Schema(description = "是否生效")
    private Integer isEnabled;

    @Schema(description = "属性ID")
    private Integer skuId;

}