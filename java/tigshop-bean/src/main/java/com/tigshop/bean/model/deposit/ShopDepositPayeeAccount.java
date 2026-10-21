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

/**
 * 保证金收款账户表model（平台级，充值核对用）
 *
 * @author Tigshop团队
 */
@Data
@TableName("shop_deposit_payee_account")
@Schema(description = "保证金收款账户")
public class ShopDepositPayeeAccount {

    @TableId(value = "shop_deposit_payee_account_id", type = IdType.AUTO)
    @Schema(description = "收款账户ID")
    private Integer shopDepositPayeeAccountId;

    @Schema(description = "账户类型 1银行卡 2支付宝 3微信")
    private Integer accountType;

    @Schema(description = "户名")
    private String accountName;

    @Schema(description = "账号")
    private String accountNo;

    @Schema(description = "银行名称")
    private String bankName;

    @Schema(description = "银行支行")
    private String bankBranch;

    @Schema(description = "状态 1启用 0停用")
    private Integer status;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "创建时间")
    private Long addTime;

    @Schema(description = "更新时间")
    private Long updateTime;
}
