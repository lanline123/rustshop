package com.tigshop.bean.dto.merchant;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 入驻申请model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "入驻申请")
public class MerchantApplyDTO {
    @TableId(value = "merchant_apply_id", type = IdType.AUTO)
    @Schema(description = "商家入驻申请ID")
    private Integer merchantApplyId;

    @TableField("user_id")
    @Schema(description = "用户ID")
    private Integer userId;

    @TableField("add_time")
    @Schema(description = "添加时间")
    private Integer addTime;

    @TableField("shop_title")
    @Schema(description = "店铺名称")
    private String shopTitle;

    @TableField("status")
    @Schema(description = "状态，1：待审核，10：审核通过，20：审核未通过")
    private Integer status;

    @TableField("type")
    @Schema(description = "类型，1：个人，2：企业")
    private Integer type;

    @TableField("contact_name")
    @Schema(description = "联系人名称")
    private String contactName;

    @TableField("contact_mobile")
    @Schema(description = "联系人手机")
    private String contactMobile;

    @TableField("audit_time")
    @Schema(description = "审核时间")
    private Date auditTime;

    @TableField("audit_admin_id")
    @Schema(description = "审核管理员ID(admin_user.admin_id)，0 表示系统自动通过或未审核")
    private Integer auditAdminId;

    @TableField("shop_data")
    @Schema(description = "申请资料shop信息JSON")
    private String shopData;

    @TableField("base_data")
    @Schema(description = "申请资料基本信息JSON")
    private String baseData;

    @TableField("merchant_data")
    @Schema(description = "申请资料的商户信息JSON")
    private String merchantData;

    @TableField("company_name")
    @Schema(description = "企业名称")
    private String companyName;

    @TableField("corporate_name")
    @Schema(description = "申请主体信息")
    private String corporateName;

    @TableField("audit_remark")
    @Schema(description = "审核备注")
    private String auditRemark;
}