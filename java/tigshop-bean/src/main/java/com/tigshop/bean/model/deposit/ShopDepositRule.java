// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.model.deposit;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 店铺保证金规则表model
 *
 * @author Tigshop团队
 */
@Data
@TableName("shop_deposit_rule")
@Schema(description = "店铺保证金规则")
public class ShopDepositRule {
    @TableId(value = "shop_deposit_rule_id", type = IdType.AUTO)
    @Schema(description = "店铺保证金规则ID")
    private Integer shopDepositRuleId;

    @Schema(description = "规则名称")
    private String ruleName;

    @Schema(description = "适用主体类型,逗号分隔 1个人 2企业(MerchantType)")
    private String merchantTypes;

    @Schema(description = "适用店铺类型,逗号分隔 1店铺 2供应商(DepositRuleShopType)")
    private String shopTypes;

    @Schema(description = "保证金金额")
    private BigDecimal depositAmount;

    @Schema(description = "生效开始时间")
    private Long startTime;

    @Schema(description = "生效结束时间")
    private Long endTime;

    @Schema(description = "状态 1启用 0停用")
    private Integer status;

    @Schema(description = "是否删除")
    private Integer isDelete;

    @Schema(description = "创建时间")
    private Long addTime;

    @Schema(description = "更新时间")
    private Long updateTime;
}
