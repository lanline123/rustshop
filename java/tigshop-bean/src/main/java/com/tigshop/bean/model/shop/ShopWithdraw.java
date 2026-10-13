// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.model.shop;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商家账户表model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@TableName("shop_withdraw")
@Schema(description = "店铺提现记录")
public class ShopWithdraw {
    @TableId(value = "shop_withdraw_log_id", type = IdType.AUTO)
    @Schema(description = "商家账户表ID")
    private Integer shopWithdrawLogId;

    @Schema(description = "所属店铺")
    private Integer shopId;

    @Schema(description = "提现金额")
    private BigDecimal amount;

    @Schema(description = "手续费")
    private BigDecimal handlingFee;

    @Schema(description = "手续费率")
    private BigDecimal handlingRate;

    @Schema(description = "状态0待审核2审核不通过3完成")
    private Integer status;

    @Schema(description = "提现时间")
    private Long addTime;

    @Schema(description = "提现备注")
    private String remark;

    @Schema(description = "提现到")
    private Integer merchantAccountId;

    @Schema(description = "提现时账户信息数据")
    private String accountData;

    @Schema(description = "审核备注")
    private String auditRemark;

    @Schema(description = "打款凭证")
    private String paymentVoucher;

    @Schema(description = "提现单据号")
    private String withdrawSn;

    @Schema(description = "提现方式：1-线下，2-线上(微信API)")
    private Integer withdrawType;

    @Schema(description = "渠道提现单号")
    private String channelWithdrawId;

    @Schema(description = "渠道侧状态")
    private String channelStatus;

    @Schema(description = "线上提现渠道：WECHAT-微信，HUIFU-汇付")
    private String payChannel;

    @Schema(description = "本申请实际冻结的线下金额：新线上为0，历史空值需核账")
    private BigDecimal offlineFrozenAmount;

    @Schema(description = "申请时的渠道子商户快照，避免绑定变更后查错资金账户")
    private String channelSubMchId;
}
