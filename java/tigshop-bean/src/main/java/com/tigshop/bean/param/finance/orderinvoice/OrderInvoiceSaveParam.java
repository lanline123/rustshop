// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.param.finance.orderinvoice;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 发票申请创建数据对象
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:13
 */
@Data
@Schema(description = "发票申请参数")
public class OrderInvoiceSaveParam {
    @Schema(description = "订单发票申请ID")
    private Integer id;

    @Schema(description = "会员ID")
    private Integer userId;

    @Schema(description = "订单ID/父订单ID")
    private Integer orderId;

    @Schema(description = "发票类型：1:普通发票，2:专用发票")
    private Integer invoiceType;

    @Schema(description = "发票申请状态：0:待处理，1:已开，2:失败/作废")
    private Integer status;

    @Schema(description = "申请失败时的回复内容")
    private String applyReply;

    @Schema(description = "抬头：1：个人，2企业")
    private Integer titleType;

    @Schema(description = "纳税人识别号")
    private String companyCode;

    @Schema(description = "个人/单位名称")
    @NotNull(message = "单位/个人名称不能为空")
    @Size(max = 100, message = "单位/个人名称最多100个字符")
    private String companyName;

    @Schema(description = "单位地址")
    private String companyAddress;

    @Schema(description = "单位电话")
    private String companyPhone;

    @Schema(description = "开户银行")
    private String companyBank;

    @Schema(description = "银行账号")
    private String companyAccount;

    @Schema(description = "发票内容，统一为商品明细")
    private String invoiceContent;

    @Schema(description = "发票号码")
    private String invoiceNo;

    @Schema(description = "发票金额")
    private BigDecimal amount;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "申请时间")
    private Long addTime;
}
