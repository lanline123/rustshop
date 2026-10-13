// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.model.finance;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 发票资质model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@TableName("user_invoice")
@Schema(description = "发票资质")
public class UserInvoice {

    @TableId(value = "invoice_id", type = IdType.AUTO)
    @Schema(description = "发票信息ID")
    private Integer invoiceId;

    @Schema(description = "会员ID")
    private Integer userId;

    @Schema(description = "专用发票审核状态：0：未申请专用发票，1：审核通过，2：已申请，待审核，3：审核未通过")
    private Integer status;

    @Schema(description = "审核失败时的回复内容")
    private String applyReply;

    @Schema(description = "添加时间")
    private Long addTime;

    @Schema(description = "抬头：1：个人，2 企业")
    private Integer titleType;

    @Schema(description = "单位名称")
    private String companyName;

    @Schema(description = "纳税人识别号")
    private String companyCode;

    @Schema(description = "单位地址")
    private String companyAddress;

    @Schema(description = "单位电话")
    private String companyPhone;

    @Schema(description = "开户银行")
    private String companyBank;

    @Schema(description = "开户账号")
    private String companyAccount;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "发票内容，统一为商品明细")
    private String invoiceContent;
}