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

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 电子卡券model
 *
 * @author kidd
 * @create 2025/7/2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("e_card")
@Schema(description = "电子卡券")
public class ECard {

    @TableId(value = "card_id", type = IdType.AUTO)
    @Schema(description = "卡券分组id")
    private Integer cardId;

    @Schema(description = "卡券分组id")
    private Integer groupId;

    @Schema(description = "卡号")
    private String cardNumber;

    @Schema(description = "卡号密码")
    private String cardPwd;

    @Schema(description = "0:未使用,1:已使用")
    private Integer isUse;

    @Schema(description = "订单id")
    private Integer orderId;

    @Schema(description = "订单item_id")
    private Integer orderItemId;

    @Schema(description = "添加时间")
    private Long addTime;

    @Schema(description = "更新时间")
    private Long upTime;
}